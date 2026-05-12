package com.pulumi.policy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class PolicyPackArgs {
  private final List<ResourceValidationPolicy> policies;
  private final EnforcementLevel enforcementLevel;

  private PolicyPackArgs(Builder b) {
    if (b.policies.isEmpty()) {
      throw new IllegalArgumentException("policy pack must contain at least one policy");
    }
    Set<String> names = new HashSet<>();
    for (ResourceValidationPolicy p : b.policies) {
      if (!names.add(p.name())) {
        throw new IllegalArgumentException("duplicate policy name: " + p.name());
      }
    }
    this.policies = Collections.unmodifiableList(new ArrayList<>(b.policies));
    this.enforcementLevel = Objects.requireNonNullElse(b.enforcementLevel, EnforcementLevel.ADVISORY);
  }

  public List<ResourceValidationPolicy> policies() { return policies; }
  public EnforcementLevel enforcementLevel() { return enforcementLevel; }

  public static Builder builder() { return new Builder(); }

  public static final class Builder {
    private final List<ResourceValidationPolicy> policies = new ArrayList<>();
    private EnforcementLevel enforcementLevel;

    public Builder policies(ResourceValidationPolicy... ps) {
      for (ResourceValidationPolicy p : ps) {
        this.policies.add(Objects.requireNonNull(p, "policy"));
      }
      return this;
    }

    public Builder policies(List<ResourceValidationPolicy> ps) {
      for (ResourceValidationPolicy p : ps) {
        this.policies.add(Objects.requireNonNull(p, "policy"));
      }
      return this;
    }

    public Builder enforcementLevel(EnforcementLevel v) { this.enforcementLevel = v; return this; }

    public PolicyPackArgs build() { return new PolicyPackArgs(this); }
  }
}
