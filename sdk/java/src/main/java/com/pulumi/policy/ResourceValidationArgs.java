package com.pulumi.policy;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class ResourceValidationArgs {
  private final String urn;
  private final String type;
  private final String name;
  private final Map<String, Object> props;

  private ResourceValidationArgs(Builder b) {
    this.urn = Objects.requireNonNull(b.urn, "urn");
    this.type = Objects.requireNonNull(b.type, "type");
    this.name = Objects.requireNonNull(b.name, "name");
    this.props = Collections.unmodifiableMap(Objects.requireNonNull(b.props, "props"));
  }

  public String urn() { return urn; }
  public String type() { return type; }
  public String name() { return name; }
  public Map<String, Object> props() { return props; }

  public boolean isType(String typeToken) {
    return type.equals(typeToken);
  }

  public static Builder builder() { return new Builder(); }

  public static final class Builder {
    private String urn;
    private String type;
    private String name;
    private Map<String, Object> props;

    public Builder urn(String v) { this.urn = v; return this; }
    public Builder type(String v) { this.type = v; return this; }
    public Builder name(String v) { this.name = v; return this; }
    public Builder props(Map<String, Object> v) { this.props = v; return this; }

    public ResourceValidationArgs build() { return new ResourceValidationArgs(this); }
  }
}
