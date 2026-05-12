package com.pulumi.policy;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecretsTest {
  @Test
  void secretWrapsAndUnwraps() {
    Secret<String> s = Secret.of("hunter2");
    assertThat(s.value()).isEqualTo("hunter2");
  }

  @Test
  void isSecretTrueOnSecretValue() {
    assertThat(Secrets.isSecret(Secret.of("x"))).isTrue();
  }

  @Test
  void isSecretFalseOnNonSecret() {
    assertThat(Secrets.isSecret("plain")).isFalse();
    assertThat(Secrets.isSecret(null)).isFalse();
    assertThat(Secrets.isSecret(Map.of("k", "v"))).isFalse();
  }

  @Test
  void unwrapReturnsValueForSecret() {
    assertThat(Secrets.unwrap(Secret.of("inner"))).isEqualTo("inner");
  }

  @Test
  void unwrapReturnsInputForNonSecret() {
    assertThat(Secrets.unwrap("plain")).isEqualTo("plain");
    assertThat(Secrets.unwrap(null)).isNull();
  }

  @Test
  void deepUnwrapHandlesNestedStructures() {
    Object input = Map.of(
        "a", Secret.of("topSecret"),
        "b", List.of("ok", Secret.of("listSecret")),
        "c", Map.of("inner", Secret.of("nestedSecret")));
    Object out = Secrets.deepUnwrap(input);
    assertThat(out).isEqualTo(Map.of(
        "a", "topSecret",
        "b", List.of("ok", "listSecret"),
        "c", Map.of("inner", "nestedSecret")));
  }

  @Test
  void secretEqualsAndHashCode() {
    assertThat(Secret.of("x")).isEqualTo(Secret.of("x"));
    assertThat(Secret.of("x").hashCode()).isEqualTo(Secret.of("x").hashCode());
    assertThat(Secret.of("x")).isNotEqualTo(Secret.of("y"));
  }

  @Test
  void secretToStringDoesNotLeakValue() {
    assertThat(Secret.of("hunter2").toString()).doesNotContain("hunter2");
    assertThat(Secret.of("hunter2").toString()).isEqualTo("Secret[***]");
  }
}
