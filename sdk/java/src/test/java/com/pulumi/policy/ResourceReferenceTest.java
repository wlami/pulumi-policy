package com.pulumi.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceReferenceTest {
  @Test
  void buildsWithAllFields() {
    ResourceReference r = ResourceReference.of(
        "urn:pulumi:dev::p::aws:s3/bucket:Bucket::b",
        "bucket-abc123",
        "5.18.0");
    assertThat(r.urn()).isEqualTo("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b");
    assertThat(r.id()).isEqualTo("bucket-abc123");
    assertThat(r.packageVersion()).isEqualTo("5.18.0");
    assertThat(r.idIsUnknown()).isFalse();
  }

  @Test
  void componentResourceWithoutId() {
    ResourceReference r = ResourceReference.of("urn:...", null, "1.0.0");
    assertThat(r.id()).isNull();
    assertThat(r.idIsUnknown()).isFalse();
  }

  @Test
  void unknownId() {
    ResourceReference r = ResourceReference.of("urn:...", Unknown.INSTANCE, "1.0.0");
    assertThat(r.idIsUnknown()).isTrue();
    assertThat(r.id()).isSameAs(Unknown.INSTANCE);
  }

  @Test
  void equality() {
    ResourceReference a = ResourceReference.of("urn:x", "id-1", "1.0");
    ResourceReference b = ResourceReference.of("urn:x", "id-1", "1.0");
    assertThat(a).isEqualTo(b);
  }
}
