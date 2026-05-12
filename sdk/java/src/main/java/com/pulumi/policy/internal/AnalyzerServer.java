package com.pulumi.policy.internal;

import com.google.protobuf.Empty;
import com.pulumi.policy.PolicyPackArgs;
import com.pulumi.policy.ReportViolation;
import com.pulumi.policy.ResourceValidationArgs;
import com.pulumi.policy.ResourceValidationPolicy;
import io.grpc.stub.StreamObserver;
import pulumirpc.AnalyzerGrpc;
import pulumirpc.AnalyzerOuterClass.AnalyzeRequest;
import pulumirpc.AnalyzerOuterClass.AnalyzeResponse;
import pulumirpc.AnalyzerOuterClass.AnalyzeDiagnostic;
import pulumirpc.AnalyzerOuterClass.AnalyzerInfo;
import pulumirpc.AnalyzerOuterClass.PolicyInfo;
import pulumirpc.AnalyzerOuterClass.AnalyzerHandshakeRequest;
import pulumirpc.AnalyzerOuterClass.AnalyzerHandshakeResponse;
import pulumirpc.AnalyzerOuterClass.AnalyzerStackConfigureRequest;
import pulumirpc.AnalyzerOuterClass.AnalyzerStackConfigureResponse;
import pulumirpc.AnalyzerOuterClass.ConfigureAnalyzerRequest;
import pulumirpc.Plugin.PluginInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public final class AnalyzerServer extends AnalyzerGrpc.AnalyzerImplBase {

  @Override
  public void handshake(AnalyzerHandshakeRequest req,
                        StreamObserver<AnalyzerHandshakeResponse> obs) {
    obs.onNext(AnalyzerHandshakeResponse.getDefaultInstance());
    obs.onCompleted();
  }

  @Override
  public void getAnalyzerInfo(Empty req, StreamObserver<AnalyzerInfo> obs) {
    PolicyRegistry.Entry e = PolicyRegistry.get();
    PolicyPackArgs args = e.args();
    AnalyzerInfo.Builder info = AnalyzerInfo.newBuilder()
        .setName(e.packName())
        .setVersion("0.0.0");  // MVP: SDK doesn't read pack version yet
    for (ResourceValidationPolicy p : args.policies()) {
      info.addPolicies(PolicyInfo.newBuilder()
          .setName(p.name())
          .setDescription(p.description())
          .setEnforcementLevel(p.enforcementLevel().toProto())
          .build());
    }
    obs.onNext(info.build());
    obs.onCompleted();
  }

  @Override
  public void getPluginInfo(Empty req, StreamObserver<PluginInfo> obs) {
    obs.onNext(PluginInfo.newBuilder().setVersion("0.0.0").build());
    obs.onCompleted();
  }

  @Override
  public void configure(ConfigureAnalyzerRequest req, StreamObserver<Empty> obs) {
    // MVP: no per-policy config support.
    obs.onNext(Empty.getDefaultInstance());
    obs.onCompleted();
  }

  @Override
  public void configureStack(AnalyzerStackConfigureRequest req,
                             StreamObserver<AnalyzerStackConfigureResponse> obs) {
    obs.onNext(AnalyzerStackConfigureResponse.getDefaultInstance());
    obs.onCompleted();
  }

  @Override
  public void analyze(AnalyzeRequest req, StreamObserver<AnalyzeResponse> obs) {
    PropertyMarshaller.Result props = PropertyMarshaller.structToMapWithFlags(req.getProperties());
    ResourceValidationArgs args = ResourceValidationArgs.builder()
        .urn(req.getUrn())
        .type(req.getType())
        .name(req.getName())
        .props(props.values())
        .build();

    List<AnalyzeDiagnostic> diagnostics = new ArrayList<>();
    PolicyPackArgs packArgs = PolicyRegistry.get().args();
    for (ResourceValidationPolicy p : packArgs.policies()) {
      pulumirpc.AnalyzerOuterClass.EnforcementLevel effectiveLevel =
          p.enforcementLevel().toProto();
      List<AnalyzeDiagnostic> perPolicy = new ArrayList<>();
      ReportViolation report = (message, urn) -> perPolicy.add(AnalyzeDiagnostic.newBuilder()
          .setPolicyName(p.name())
          .setMessage(message)
          .setEnforcementLevel(effectiveLevel)
          .setUrn(urn == null ? req.getUrn() : urn)
          .build());
      try {
        p.validate().accept(args, report);
      } catch (RuntimeException e) {
        perPolicy.clear();
        perPolicy.add(AnalyzeDiagnostic.newBuilder()
            .setPolicyName(p.name())
            .setMessage("policy threw exception: " + stackTrace(e))
            .setEnforcementLevel(effectiveLevel)
            .setUrn(req.getUrn())
            .build());
      }
      diagnostics.addAll(perPolicy);
    }
    obs.onNext(AnalyzeResponse.newBuilder().addAllDiagnostics(diagnostics).build());
    obs.onCompleted();
  }

  @Override
  public void cancel(Empty req, StreamObserver<Empty> obs) {
    obs.onNext(Empty.getDefaultInstance());
    obs.onCompleted();
  }

  private static String stackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
