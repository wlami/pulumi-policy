package com.pulumi.policy.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class PolicyMain {
  // Matches sdk/nodejs/cmd/run-policy-pack/run.ts:36 -
  // when we've already printed an actionable message, exit 32 so the
  // language plugin host suppresses its own generic error.
  private static final int ACTIONABLE_EXIT = 32;

  private PolicyMain() {}

  public static void main(String[] args) {
    // Disable Netty's use of sun.misc.Unsafe. grpc-netty-shaded's bundled
    // Netty would otherwise call Unsafe.objectFieldOffset during static
    // initialization, which JDK 23+ flags with a "terminally deprecated"
    // warning. Setting this property before any io.netty.* class loads
    // (PolicyMain runs before the user entrypoint and before
    // com.pulumi.policy.PolicyPack#run pulls in ServerBuilder) makes Netty
    // skip the Unsafe-based code paths entirely.
    if (System.getProperty("io.netty.noUnsafe") == null) {
      System.setProperty("io.netty.noUnsafe", "true");
    }

    if (args.length < 1) {
      System.err.println("usage: PolicyMain <fully.qualified.EntrypointClass> [args...]");
      System.exit(ACTIONABLE_EXIT);
    }
    String entrypoint = args[0];
    String[] rest = Arrays.copyOfRange(args, 1, args.length);

    Class<?> cls;
    try {
      cls = Class.forName(entrypoint);
    } catch (ClassNotFoundException e) {
      System.err.println("policy entrypoint '" + entrypoint
          + "' not found on classpath; check options.main in PulumiPolicy.yaml");
      System.exit(ACTIONABLE_EXIT);
      return;
    }

    Method main;
    try {
      main = cls.getMethod("main", String[].class);
    } catch (NoSuchMethodException e) {
      System.err.println(entrypoint + ".main(String[]) not found or not public static");
      System.exit(ACTIONABLE_EXIT);
      return;
    }

    if (!Modifier.isStatic(main.getModifiers()) || !Modifier.isPublic(main.getModifiers())) {
      System.err.println(entrypoint + ".main(String[]) must be public static");
      System.exit(ACTIONABLE_EXIT);
      return;
    }

    try {
      main.invoke(null, (Object) rest);
    } catch (IllegalAccessException e) {
      System.err.println("could not invoke " + entrypoint + ".main: " + e.getMessage());
      System.exit(ACTIONABLE_EXIT);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      System.err.println("policy entrypoint threw: " + (cause == null ? e : cause));
      if (cause != null) cause.printStackTrace(System.err);
      System.exit(ACTIONABLE_EXIT);
    }

    // If main() returned without calling PolicyPack.run, surface that:
    try {
      PolicyRegistry.get();
    } catch (IllegalStateException e) {
      System.err.println("policy pack did not register any policies; "
          + "did you call PolicyPack.run(...)?");
      System.exit(ACTIONABLE_EXIT);
    }
  }
}
