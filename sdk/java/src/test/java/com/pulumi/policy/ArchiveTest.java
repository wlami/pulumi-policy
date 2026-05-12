package com.pulumi.policy;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveTest {
  @Test
  void assetsArchive() {
    Map<String, Object> assets = Map.of("a.txt", "hello", "nested/b.txt", "world");
    Archive a = Archive.fromAssets(assets, "sha256:abc");
    assertThat(a.kind()).isEqualTo(Archive.Kind.ASSETS);
    assertThat(a.assets()).isEqualTo(assets);
    assertThat(a.path()).isNull();
    assertThat(a.uri()).isNull();
  }

  @Test
  void pathArchive() {
    Archive a = Archive.fromPath("/tmp/x.tar.gz", null);
    assertThat(a.kind()).isEqualTo(Archive.Kind.PATH);
    assertThat(a.path()).isEqualTo("/tmp/x.tar.gz");
  }

  @Test
  void uriArchive() {
    Archive a = Archive.fromUri("file:///tmp/x.zip", "sha256:def");
    assertThat(a.kind()).isEqualTo(Archive.Kind.URI);
    assertThat(a.uri()).isEqualTo("file:///tmp/x.zip");
    assertThat(a.hash()).isEqualTo("sha256:def");
  }

  @Test
  void assetsMapIsUnmodifiable() {
    Archive a = Archive.fromAssets(Map.of("k", "v"), null);
    org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
        () -> a.assets().put("x", "y"));
  }
}
