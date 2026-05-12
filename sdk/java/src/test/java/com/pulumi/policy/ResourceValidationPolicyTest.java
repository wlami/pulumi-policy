package com.pulumi.policy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceValidationPolicyTest {
  @Test
  void buildsValidPolicy() {
    ResourceValidationPolicy p = ResourceValidationPolicy.builder()
        .name("no-public-s3")
        .description("S3 buckets must not be public")
        .enforcementLevel(EnforcementLevel.MANDATORY)
        .validate((args, report) -> { /* no-op */ })
        .build();

    assertThat(p.name()).isEqualTo("no-public-s3");
    assertThat(p.description()).isEqualTo("S3 buckets must not be public");
    assertThat(p.enforcementLevel()).isEqualTo(EnforcementLevel.MANDATORY);
    assertThat(p.validate()).isNotNull();
  }

  @Test
  void requiresName() {
    assertThatThrownBy(() -> ResourceValidationPolicy.builder()
        .description("d")
        .validate((a, r) -> {})
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void requiresValidate() {
    assertThatThrownBy(() -> ResourceValidationPolicy.builder()
        .name("p").description("d").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("validate");
  }

  @Test
  void defaultEnforcementLevelIsAdvisory() {
    ResourceValidationPolicy p = ResourceValidationPolicy.builder()
        .name("p").description("d").validate((a, r) -> {}).build();
    assertThat(p.enforcementLevel()).isEqualTo(EnforcementLevel.ADVISORY);
  }

  @Test
  void validateCallbackReceivesArgsAndReporter() {
    List<String> recorded = new ArrayList<>();
    ResourceValidationPolicy p = ResourceValidationPolicy.builder()
        .name("p").description("d")
        .validate((args, report) -> {
          if ("public-read".equals(args.props().get("acl"))) {
            report.violation("not allowed");
          }
        })
        .build();

    ResourceValidationArgs args = ResourceValidationArgs.builder()
        .urn("u").type("t").name("n")
        .props(Map.of("acl", "public-read")).build();

    p.validate().accept(args, (msg, urn) -> recorded.add(msg));
    assertThat(recorded).containsExactly("not allowed");
  }
}
