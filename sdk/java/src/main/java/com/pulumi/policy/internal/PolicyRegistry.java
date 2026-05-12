package com.pulumi.policy.internal;

import com.pulumi.policy.PolicyPackArgs;

import java.util.Objects;

public final class PolicyRegistry {
  private static volatile Entry entry;

  private PolicyRegistry() {}

  public static synchronized void register(String packName, PolicyPackArgs args) {
    if (entry != null) {
      throw new IllegalStateException(
          "only one policy pack per process; '" + entry.packName + "' already registered");
    }
    entry = new Entry(Objects.requireNonNull(packName), Objects.requireNonNull(args));
  }

  public static Entry get() {
    Entry e = entry;
    if (e == null) {
      throw new IllegalStateException(
          "no policy pack registered; did you call PolicyPack.run(...)?");
    }
    return e;
  }

  public static synchronized void resetForTesting() {
    entry = null;
  }

  public static final class Entry {
    private final String packName;
    private final PolicyPackArgs args;

    Entry(String packName, PolicyPackArgs args) {
      this.packName = packName;
      this.args = args;
    }

    public String packName() { return packName; }
    public PolicyPackArgs args() { return args; }
  }
}
