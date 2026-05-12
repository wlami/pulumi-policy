package com.pulumi.policy;

public enum EnforcementLevel {
  ADVISORY,
  MANDATORY,
  REMEDIATE,
  DISABLED;

  public pulumirpc.AnalyzerOuterClass.EnforcementLevel toProto() {
    switch (this) {
      case ADVISORY:  return pulumirpc.AnalyzerOuterClass.EnforcementLevel.ADVISORY;
      case MANDATORY: return pulumirpc.AnalyzerOuterClass.EnforcementLevel.MANDATORY;
      case REMEDIATE: return pulumirpc.AnalyzerOuterClass.EnforcementLevel.REMEDIATE;
      case DISABLED:  return pulumirpc.AnalyzerOuterClass.EnforcementLevel.DISABLED;
      default: throw new IllegalStateException("unreachable: " + this);
    }
  }

  public static EnforcementLevel fromProto(pulumirpc.AnalyzerOuterClass.EnforcementLevel proto) {
    switch (proto) {
      case ADVISORY:  return ADVISORY;
      case MANDATORY: return MANDATORY;
      case REMEDIATE: return REMEDIATE;
      case DISABLED:  return DISABLED;
      default: throw new IllegalArgumentException("unknown enforcement level: " + proto);
    }
  }
}
