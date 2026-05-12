package com.pulumi.policy;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class ResourceValidationPolicy {
  private final String name;
  private final String description;
  private final EnforcementLevel enforcementLevel;
  private final BiConsumer<ResourceValidationArgs, ReportViolation> validate;

  private ResourceValidationPolicy(Builder b) {
    if (b.name == null || b.name.isBlank()) {
      throw new IllegalArgumentException("name is required");
    }
    if (b.validate == null) {
      throw new IllegalArgumentException("validate callback is required");
    }
    this.name = b.name;
    this.description = b.description == null ? "" : b.description;
    // null means "inherit from pack"; stored as-is so AnalyzerServer can apply the pack default.
    this.enforcementLevel = b.enforcementLevel;
    this.validate = b.validate;
  }

  public String name() { return name; }
  public String description() { return description; }
  /** Returns this policy's enforcement level, defaulting to {@link EnforcementLevel#ADVISORY} if not set. */
  public EnforcementLevel enforcementLevel() {
    return enforcementLevel != null ? enforcementLevel : EnforcementLevel.ADVISORY;
  }
  /**
   * Returns the effective enforcement level for this policy, falling back to {@code packDefault}
   * when the policy does not have an explicit level set.
   */
  public EnforcementLevel effectiveEnforcementLevel(EnforcementLevel packDefault) {
    return enforcementLevel != null ? enforcementLevel : packDefault;
  }
  public BiConsumer<ResourceValidationArgs, ReportViolation> validate() { return validate; }

  public static Builder builder() { return new Builder(); }

  public static final class Builder {
    private String name;
    private String description;
    private EnforcementLevel enforcementLevel;
    private BiConsumer<ResourceValidationArgs, ReportViolation> validate;

    public Builder name(String v) { this.name = v; return this; }
    public Builder description(String v) { this.description = v; return this; }
    public Builder enforcementLevel(EnforcementLevel v) { this.enforcementLevel = v; return this; }
    public Builder validate(BiConsumer<ResourceValidationArgs, ReportViolation> v) { this.validate = v; return this; }

    public ResourceValidationPolicy build() { return new ResourceValidationPolicy(this); }
  }
}
