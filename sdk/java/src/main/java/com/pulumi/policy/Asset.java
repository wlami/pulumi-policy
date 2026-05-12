package com.pulumi.policy;

import java.util.Objects;

/**
 * Read-only view of a Pulumi asset value as it appears in a resource's
 * property map. An Asset is exactly one of: text content, a filesystem path,
 * or a URI. Use {@link #kind()} to discriminate, then the corresponding
 * accessor. All other accessors return {@code null}.
 */
public final class Asset {
  public enum Kind { TEXT, PATH, URI }

  private final Kind kind;
  private final String text;
  private final String path;
  private final String uri;
  private final String hash;

  private Asset(Kind kind, String text, String path, String uri, String hash) {
    this.kind = Objects.requireNonNull(kind, "kind");
    this.text = text;
    this.path = path;
    this.uri = uri;
    this.hash = hash;
  }

  public static Asset fromText(String text, String hash) {
    return new Asset(Kind.TEXT, Objects.requireNonNull(text, "text"), null, null, hash);
  }

  public static Asset fromPath(String path, String hash) {
    return new Asset(Kind.PATH, null, Objects.requireNonNull(path, "path"), null, hash);
  }

  public static Asset fromUri(String uri, String hash) {
    return new Asset(Kind.URI, null, null, Objects.requireNonNull(uri, "uri"), hash);
  }

  public Kind kind() { return kind; }
  public String text() { return text; }
  public String path() { return path; }
  public String uri() { return uri; }
  public String hash() { return hash; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Asset)) return false;
    Asset a = (Asset) o;
    return kind == a.kind
        && Objects.equals(text, a.text)
        && Objects.equals(path, a.path)
        && Objects.equals(uri, a.uri)
        && Objects.equals(hash, a.hash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, text, path, uri, hash);
  }

  @Override
  public String toString() {
    String body;
    switch (kind) {
      case TEXT: body = "text<" + (text == null ? 0 : text.length()) + " chars>"; break;
      case PATH: body = "path=" + path; break;
      case URI:  body = "uri=" + uri; break;
      default:   body = "?";
    }
    return "Asset[" + kind + ", " + body + (hash == null ? "" : ", hash=" + hash) + "]";
  }
}
