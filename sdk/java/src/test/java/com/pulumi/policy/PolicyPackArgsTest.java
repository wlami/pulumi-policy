package com.pulumi.policy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyPackArgsTest {
  private static ResourceValidationPolicy stub(String name) {
    return ResourceValidationPolicy.builder()
        .name(name).description("").validate((a, r) -> {}).build();
  }

  @Test
  void buildsWithSinglePolicy() {
    PolicyPackArgs args = PolicyPackArgs.builder()
        .policies(stub("p1"))
        .build();
    assertThat(args.policies()).extracting(ResourceValidationPolicy::name)
        .containsExactly("p1");
    assertThat(args.enforcementLevel()).isEqualTo(EnforcementLevel.ADVISORY);
  }

  @Test
  void buildsWithMultiplePolicies() {
    PolicyPackArgs args = PolicyPackArgs.builder()
        .policies(stub("a"), stub("b"))
        .enforcementLevel(EnforcementLevel.MANDATORY)
        .build();
    assertThat(args.policies()).extracting(ResourceValidationPolicy::name)
        .containsExactly("a", "b");
    assertThat(args.enforcementLevel()).isEqualTo(EnforcementLevel.MANDATORY);
  }

  @Test
  void rejectsEmptyPolicies() {
    assertThatThrownBy(() -> PolicyPackArgs.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least one policy");
  }

  @Test
  void rejectsDuplicatePolicyNames() {
    assertThatThrownBy(() -> PolicyPackArgs.builder()
        .policies(stub("dup"), stub("dup"))
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("duplicate");
  }

  @Test
  void policiesListIsUnmodifiable() {
    PolicyPackArgs args = PolicyPackArgs.builder().policies(stub("p")).build();
    assertThatThrownBy(() -> args.policies().add(stub("q")))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
