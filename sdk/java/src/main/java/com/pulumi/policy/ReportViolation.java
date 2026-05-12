package com.pulumi.policy;

/**
 * Callback handed to policy validation lambdas. Each invocation records one
 * policy violation against the resource currently being analyzed.
 */
@FunctionalInterface
public interface ReportViolation {
  /**
   * Record a violation. {@code urn} may be null to attach the violation to the
   * resource being analyzed by default.
   */
  void violation(String message, String urn);

  /**
   * Convenience: record a violation against the resource being analyzed.
   */
  default void violation(String message) {
    violation(message, null);
  }
}
