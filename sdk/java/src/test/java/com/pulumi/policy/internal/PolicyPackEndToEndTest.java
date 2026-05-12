package com.pulumi.policy.internal;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import pulumirpc.AnalyzerGrpc;
import pulumirpc.AnalyzerOuterClass.AnalyzeRequest;
import pulumirpc.AnalyzerOuterClass.AnalyzeResponse;
import pulumirpc.AnalyzerOuterClass.AnalyzerInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyPackEndToEndTest {
  @Test
  void subprocessHostsAnalyzerAndDetectsViolation() throws Exception {
    String classpath = System.getProperty("java.class.path");
    ProcessBuilder pb = new ProcessBuilder(
        javaBin(), "-cp", classpath,
        "com.pulumi.policy.internal.PolicyMain",
        "com.pulumi.policy.internal.SamplePack");
    pb.redirectErrorStream(false);
    Process proc = pb.start();

    // Drain stderr in background to prevent potential buffer deadlock.
    Thread stderrDrain = new Thread(() -> {
      try {
        proc.getErrorStream().transferTo(java.io.OutputStream.nullOutputStream());
      } catch (Exception ignored) {}
    }, "stderr-drain");
    stderrDrain.setDaemon(true);
    stderrDrain.start();

    BufferedReader stdout = new BufferedReader(
        new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
    String portLine = stdout.readLine();
    assertThat(portLine).matches("\\d+");
    int port = Integer.parseInt(portLine);

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
        .usePlaintext().build();
    try {
      AnalyzerGrpc.AnalyzerBlockingStub stub = AnalyzerGrpc.newBlockingStub(channel);

      AnalyzerInfo info = stub.getAnalyzerInfo(com.google.protobuf.Empty.getDefaultInstance());
      assertThat(info.getName()).isEqualTo("sample-pack");

      AnalyzeRequest req = AnalyzeRequest.newBuilder()
          .setUrn("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b")
          .setType("aws:s3/bucket:Bucket")
          .setName("b")
          .setProperties(Struct.newBuilder()
              .putFields("acl", Value.newBuilder().setStringValue("public-read").build()))
          .build();
      AnalyzeResponse resp = stub.analyze(req);
      assertThat(resp.getDiagnosticsCount()).isEqualTo(1);
      assertThat(resp.getDiagnostics(0).getPolicyName()).isEqualTo("no-public-s3");

      // Drive shutdown via Cancel (MVP: Cancel is just an ack, so this
      // returns immediately but does NOT actually shut down the subprocess).
      stub.cancel(com.google.protobuf.Empty.getDefaultInstance());
    } finally {
      channel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
    }

    // Wait briefly; in MVP, Cancel does not cause the subprocess to exit.
    // Force it down so the test releases its resources.
    if (!proc.waitFor(2, TimeUnit.SECONDS)) {
      proc.destroyForcibly().waitFor(2, TimeUnit.SECONDS);
    }
  }

  private static String javaBin() {
    String javaHome = System.getProperty("java.home");
    return javaHome + "/bin/java" + (isWindows() ? ".exe" : "");
  }

  private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
  }
}
