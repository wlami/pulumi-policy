package com.pulumi.policy;

/**
 * Sentinel value placed in a resource's property map for any property whose
 * value is computed and not yet known at the time of analysis (typical during
 * {@code pulumi preview}). Policies can detect this case by comparing
 * with {@code Unknown.INSTANCE} or via
 * {@link ResourceValidationArgs#containsUnknowns()}.
 */
public final class Unknown {
  public static final Unknown INSTANCE = new Unknown();

  private Unknown() {}

  @Override
  public String toString() {
    return "<unknown>";
  }
}
