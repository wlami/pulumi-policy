package com.pulumi.policy.internal;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.pulumi.policy.EnforcementLevel;
import com.pulumi.policy.PolicyPackArgs;
import com.pulumi.policy.ResourceValidationPolicy;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pulumirpc.AnalyzerGrpc;
import pulumirpc.AnalyzerOuterClass.AnalyzeRequest;
import pulumirpc.AnalyzerOuterClass.AnalyzeResponse;
import pulumirpc.AnalyzerOuterClass.AnalyzerInfo;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerServerTest {
  private Server server;
  private ManagedChannel channel;
  private AnalyzerGrpc.AnalyzerBlockingStub stub;

  @AfterEach
  void teardown() throws InterruptedException {
    PolicyRegistry.resetForTesting();
    if (channel != null) channel.shutdownNow();
    if (server != null) server.shutdownNow().awaitTermination();
  }

  private void start(PolicyPackArgs args, String packName) throws IOException {
    PolicyRegistry.register(packName, args);
    server = ServerBuilder.forPort(0)
        .addService(new AnalyzerServer())
        .build()
        .start();
    channel = ManagedChannelBuilder.forAddress("localhost", server.getPort())
        .usePlaintext().build();
    stub = AnalyzerGrpc.newBlockingStub(channel);
  }

  @Test
  void getAnalyzerInfoReportsPackAndPolicies() throws IOException {
    PolicyPackArgs args = PolicyPackArgs.builder()
        .enforcementLevel(EnforcementLevel.MANDATORY)
        .policies(ResourceValidationPolicy.builder()
            .name("p1").description("desc").validate((a, r) -> {}).build())
        .build();
    start(args, "my-pack");

    AnalyzerInfo info = stub.getAnalyzerInfo(com.google.protobuf.Empty.getDefaultInstance());
    assertThat(info.getName()).isEqualTo("my-pack");
    assertThat(info.getPoliciesCount()).isEqualTo(1);
    assertThat(info.getPolicies(0).getName()).isEqualTo("p1");
    assertThat(info.getPolicies(0).getEnforcementLevel())
        .isEqualTo(pulumirpc.AnalyzerOuterClass.EnforcementLevel.MANDATORY);
  }

  @Test
  void analyzeWithCleanResourceReturnsNoDiagnostics() throws IOException {
    start(PolicyPackArgs.builder()
        .policies(ResourceValidationPolicy.builder()
            .name("no-public-s3").description("").validate((a, r) -> {
              if ("public-read".equals(a.props().get("acl"))) r.violation("nope");
            }).build())
        .build(), "p");

    AnalyzeRequest req = AnalyzeRequest.newBuilder()
        .setUrn("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b")
        .setType("aws:s3/bucket:Bucket")
        .setName("b")
        .setProperties(Struct.newBuilder()
            .putFields("acl", Value.newBuilder().setStringValue("private").build()))
        .build();
    AnalyzeResponse resp = stub.analyze(req);
    assertThat(resp.getDiagnosticsCount()).isEqualTo(0);
  }

  @Test
  void analyzeWithViolatingResourceReturnsDiagnostic() throws IOException {
    start(PolicyPackArgs.builder()
        .policies(ResourceValidationPolicy.builder()
            .name("no-public-s3").description("S3 must not be public")
            .enforcementLevel(EnforcementLevel.MANDATORY)
            .validate((a, r) -> {
              if ("public-read".equals(a.props().get("acl")))
                r.violation("S3 bucket cannot be public");
            }).build())
        .build(), "p");

    AnalyzeRequest req = AnalyzeRequest.newBuilder()
        .setUrn("u").setType("aws:s3/bucket:Bucket").setName("b")
        .setProperties(Struct.newBuilder()
            .putFields("acl", Value.newBuilder().setStringValue("public-read").build()))
        .build();
    AnalyzeResponse resp = stub.analyze(req);
    assertThat(resp.getDiagnosticsCount()).isEqualTo(1);
    assertThat(resp.getDiagnostics(0).getPolicyName()).isEqualTo("no-public-s3");
    assertThat(resp.getDiagnostics(0).getMessage()).contains("cannot be public");
    assertThat(resp.getDiagnostics(0).getEnforcementLevel())
        .isEqualTo(pulumirpc.AnalyzerOuterClass.EnforcementLevel.MANDATORY);
  }

  @Test
  void policyThrowingExceptionBecomesDiagnostic() throws IOException {
    start(PolicyPackArgs.builder()
        .policies(ResourceValidationPolicy.builder()
            .name("buggy").description("")
            .validate((a, r) -> { throw new RuntimeException("oops"); }).build())
        .build(), "p");

    AnalyzeRequest req = AnalyzeRequest.newBuilder()
        .setUrn("u").setType("t").setName("n").build();
    AnalyzeResponse resp = stub.analyze(req);
    assertThat(resp.getDiagnosticsCount()).isEqualTo(1);
    assertThat(resp.getDiagnostics(0).getMessage()).contains("policy threw exception");
    assertThat(resp.getDiagnostics(0).getMessage()).contains("oops");
  }
}
