package com.pulumi.policy;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceValidationArgsTest {
  @Test
  void buildsAndExposesFields() {
    ResourceValidationArgs a = ResourceValidationArgs.builder()
        .urn("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b")
        .type("aws:s3/bucket:Bucket")
        .name("b")
        .props(Map.of("acl", "public-read"))
        .build();

    assertThat(a.urn()).isEqualTo("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b");
    assertThat(a.type()).isEqualTo("aws:s3/bucket:Bucket");
    assertThat(a.name()).isEqualTo("b");
    assertThat(a.props()).isEqualTo(Map.of("acl", "public-read"));
  }

  @Test
  void isTypeMatches() {
    ResourceValidationArgs a = ResourceValidationArgs.builder()
        .urn("u").type("aws:s3/bucket:Bucket").name("b").props(Map.of()).build();
    assertThat(a.isType("aws:s3/bucket:Bucket")).isTrue();
    assertThat(a.isType("aws:ec2/instance:Instance")).isFalse();
  }

  @Test
  void propsIsUnmodifiable() {
    ResourceValidationArgs a = ResourceValidationArgs.builder()
        .urn("u").type("t").name("n").props(Map.of("k", "v")).build();
    assertThatThrownBy(() -> a.props().put("x", "y"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void containsUnknownsDefaultsToFalse() {
    ResourceValidationArgs a = ResourceValidationArgs.builder()
        .urn("u").type("t").name("n")
        .props(java.util.Map.of("k", "v"))
        .build();
    assertThat(a.containsUnknowns()).isFalse();
  }

  @Test
  void containsUnknownsHonoursBuilder() {
    ResourceValidationArgs a = ResourceValidationArgs.builder()
        .urn("u").type("t").name("n")
        .props(java.util.Map.of("k", Unknown.INSTANCE))
        .containsUnknowns(true)
        .build();
    assertThat(a.containsUnknowns()).isTrue();
  }
}
