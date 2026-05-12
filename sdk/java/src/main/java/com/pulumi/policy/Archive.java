package com.pulumi.policy;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Read-only view of a Pulumi archive value. An Archive is exactly one of:
 * a nested map of name-to-asset entries, a filesystem path, or a URI. Use
 * {@link #kind()} to discriminate.
 */
public final class Archive {
  public enum Kind { ASSETS, PATH, URI }

  private final Kind kind;
  private final Map<String, Object> assets;
  private final String path;
  private final String uri;
  private final String hash;

  private Archive(Kind kind, Map<String, Object> assets, String path, String uri, String hash) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.assets = assets;
    this.path = path;
    this.uri = uri;
    this.hash = hash;
  }

  public static Archive fromAssets(Map<String, Object> assets, String hash) {
    return new Archive(Kind.ASSETS,
        Collections.unmodifiableMap(Objects.requireNonNull(assets, "assets")),
        null, null, hash);
  }

  public static Archive fromPath(String path, String hash) {
    return new Archive(Kind.PATH, null, Objects.requireNonNull(path, "path"), null, hash);
  }

  public static Archive fromUri(String uri, String hash) {
    return new Archive(Kind.URI, null, null, Objects.requireNonNull(uri, "uri"), hash);
  }

  public Kind kind() { return kind; }
  public Map<String, Object> assets() { return assets; }
  public String path() { return path; }
  public String uri() { return uri; }
  public String hash() { return hash; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Archive)) return false;
    Archive a = (Archive) o;
    return kind == a.kind
        && Objects.equals(assets, a.assets)
        && Objects.equals(path, a.path)
        && Objects.equals(uri, a.uri)
        && Objects.equals(hash, a.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, assets, path, uri, hash);
  }

  @Override
  public String toString() {
    String body;
    switch (kind) {
      case ASSETS: body = "assets=" + (assets == null ? 0 : assets.size()) + " entries"; break;
      case PATH:   body = "path=" + path; break;
      case URI:    body = "uri=" + uri; break;
      default:     body = "?";
    }
    return "Archive[" + kind + ", " + body + (hash == null ? "" : ", hash=" + hash) + "]";
  }
}
