package com.pulumi.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AssetTest {
  @Test
  void textAsset() {
    Asset a = Asset.fromText("hello", "sha256:abc");
    assertThat(a.kind()).isEqualTo(Asset.Kind.TEXT);
    assertThat(a.text()).isEqualTo("hello");
    assertThat(a.path()).isNull();
    assertThat(a.uri()).isNull();
    assertThat(a.hash()).isEqualTo("sha256:abc");
  }

  @Test
  void pathAsset() {
    Asset a = Asset.fromPath("/tmp/x.txt", "sha256:def");
    assertThat(a.kind()).isEqualTo(Asset.Kind.PATH);
    assertThat(a.path()).isEqualTo("/tmp/x.txt");
    assertThat(a.text()).isNull();
    assertThat(a.uri()).isNull();
  }

  @Test
  void uriAsset() {
    Asset a = Asset.fromUri("https://example.com/x", null);
    assertThat(a.kind()).isEqualTo(Asset.Kind.URI);
    assertThat(a.uri()).isEqualTo("https://example.com/x");
    assertThat(a.hash()).isNull();
  }

  @Test
  void toStringIncludesKindNotContents() {
    Asset a = Asset.fromText("supersecret-content", null);
    assertThat(a.toString()).contains("TEXT");
    assertThat(a.toString()).doesNotContain("supersecret-content");
  }
}
