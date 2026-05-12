package com.pulumi.policy.internal;

import com.pulumi.policy.PolicyPackArgs;
import com.pulumi.policy.ResourceValidationPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyRegistryTest {
  @AfterEach
  void reset() {
    PolicyRegistry.resetForTesting();
  }

  private static PolicyPackArgs sampleArgs() {
    return PolicyPackArgs.builder()
        .policies(ResourceValidationPolicy.builder()
            .name("p").description("").validate((a, r) -> {}).build())
        .build();
  }

  @Test
  void registerOnceAndRead() {
    PolicyRegistry.register("test-pack", sampleArgs());
    assertThat(PolicyRegistry.get().packName()).isEqualTo("test-pack");
    assertThat(PolicyRegistry.get().args().policies()).hasSize(1);
  }

  @Test
  void registerTwiceThrows() {
    PolicyRegistry.register("a", sampleArgs());
    assertThatThrownBy(() -> PolicyRegistry.register("b", sampleArgs()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("only one policy pack per process");
  }

  @Test
  void getBeforeRegisterThrows() {
    assertThatThrownBy(PolicyRegistry::get)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no policy pack registered");
  }
}
