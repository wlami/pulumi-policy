package com.pulumi.policy;

import java.util.Objects;

/**
 * Read-only view of a Pulumi resource reference value. Captures the URN,
 * the resource ID (which may be {@code null} for component resources or
 * {@link Unknown#INSTANCE} for not-yet-computed values), and the package
 * version that defined the resource.
 */
public final class ResourceReference {
  private final String urn;
  private final Object id;
  private final String packageVersion;

  private ResourceReference(String urn, Object id, String packageVersion) {
    this.urn = Objects.requireNonNull(urn, "urn");
    this.id = id;
    this.packageVersion = packageVersion;
  }

  public static ResourceReference of(String urn, Object id, String packageVersion) {
    return new ResourceReference(urn, id, packageVersion);
  }

  public String urn() { return urn; }

  /** Resource ID. {@code null} for component resources, {@link Unknown#INSTANCE} when computed-not-known. */
  public Object id() { return id; }

  public String packageVersion() { return packageVersion; }

  public boolean idIsUnknown() {
    return id == Unknown.INSTANCE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ResourceReference)) return false;
    ResourceReference r = (ResourceReference) o;
    return Objects.equals(urn, r.urn)
        && Objects.equals(id, r.id)
        && Objects.equals(packageVersion, r.packageVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(urn, id, packageVersion);
  }

  @Override
  public String toString() {
    return "ResourceReference[urn=" + urn + ", id=" + id + ", version=" + packageVersion + "]";
  }
}
