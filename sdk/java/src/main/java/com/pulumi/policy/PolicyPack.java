package com.pulumi.policy;

import com.pulumi.policy.internal.AnalyzerServer;
import com.pulumi.policy.internal.PolicyRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class PolicyPack {
  private PolicyPack() {}

  /**
   * Register the policy pack and host it as an Analyzer gRPC plugin. Blocks
   * until the engine disconnects.
   *
   * @param packName   pack name, used as Analyzer.name
   * @param args       pack contents
   * @param processArgs the args[] passed to your main() method - reserved for
   *                    future flag parsing; currently unused
   */
  public static void run(String packName, PolicyPackArgs args, String[] processArgs)
      throws IOException, InterruptedException {
    PolicyRegistry.register(packName, args);

    Server server = ServerBuilder.forPort(0)
        .addService(new AnalyzerServer())
        .build()
        .start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        server.shutdownNow();
      }
    }, "policy-pack-shutdown"));

    // Plugin handshake: first stdout line is the port. The Pulumi plugin
    // host reads-until-newline and connects.
    System.out.println(server.getPort());
    System.out.flush();

    server.awaitTermination();
  }
}
