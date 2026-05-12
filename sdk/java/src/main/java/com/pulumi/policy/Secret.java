package com.pulumi.policy;

import java.util.Objects;

/**
 * Wraps a property value that the engine marshalled as a secret. Policies
 * that care about secrecy can detect this via {@link Secrets#isSecret(Object)}
 * or by checking {@code instanceof Secret}. Use {@link #value()} to read the
 * underlying value when comparing against expected content.
 *
 * <p>{@code toString()} deliberately does not include the value, so log lines
 * that include the props map will not leak secrets.
 */
public final class Secret<T> {
  private final T value;

  private Secret(T value) {
    this.value = value;
  }

  public static <T> Secret<T> of(T value) {
    return new Secret<>(value);
  }

  public T value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Secret<?>)) return false;
    return Objects.equals(value, ((Secret<?>) o).value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return "Secret[***]";
  }
}
