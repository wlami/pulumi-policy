package com.pulumi.policy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helpers for working with {@link Secret} values in resource property maps.
 */
public final class Secrets {
  private Secrets() {}

  /** True iff the value is a {@link Secret}. */
  public static boolean isSecret(Object value) {
    return value instanceof Secret<?>;
  }

  /**
   * Returns the value inside a {@link Secret} wrapper, or the input unchanged
   * if it is not a secret. Only unwraps the outermost layer; use
   * {@link #deepUnwrap(Object)} to strip secrets nested inside maps and lists.
   */
  public static Object unwrap(Object value) {
    return value instanceof Secret<?> ? ((Secret<?>) value).value() : value;
  }

  /**
   * Recursively unwraps every {@link Secret} found inside the value, walking
   * into {@link Map}s and {@link List}s. Useful for comparing a property bag
   * against an expected plain-data map in tests.
   */
  public static Object deepUnwrap(Object value) {
    if (value instanceof Secret<?>) {
      return deepUnwrap(((Secret<?>) value).value());
    }
    if (value instanceof Map<?, ?>) {
      Map<?, ?> m = (Map<?, ?>) value;
      Map<Object, Object> out = new LinkedHashMap<>(m.size());
      for (Map.Entry<?, ?> e : m.entrySet()) {
        out.put(e.getKey(), deepUnwrap(e.getValue()));
      }
      return out;
    }
    if (value instanceof List<?>) {
      List<?> l = (List<?>) value;
      List<Object> out = new ArrayList<>(l.size());
      for (Object o : l) {
        out.add(deepUnwrap(o));
      }
      return out;
    }
    return value;
  }
}
