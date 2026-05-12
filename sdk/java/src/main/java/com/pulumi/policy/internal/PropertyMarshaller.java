package com.pulumi.policy.internal;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.pulumi.policy.Archive;
import com.pulumi.policy.Asset;
import com.pulumi.policy.ResourceReference;
import com.pulumi.policy.Secret;
import com.pulumi.policy.Unknown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PropertyMarshaller {
  private static final String UNKNOWN_SENTINEL = "04da6b54-80e4-46f7-96ec-b56ff0331ba9";
  private static final String SIG_KEY          = "4dabf18193072939515e22adb298388d";
  private static final String SIG_SECRET       = "1b47061264138c4ac30d75fd1eb44270";
  private static final String SIG_ASSET        = "c44067f5952c0a294b673a41bacd8c17";
  private static final String SIG_ARCHIVE      = "0def7320c3a5731c473e5ecbe6d01bc7";
  private static final String SIG_RESOURCE_REF = "5cf8f73096256a8f31e491e813e4eb8e";

  private PropertyMarshaller() {}

  public static Map<String, Object> structToMap(Struct s) {
    return structToMapWithFlags(s).values();
  }

  public static Result structToMapWithFlags(Struct s) {
    Flags flags = new Flags();
    Map<String, Object> m = convertTopLevelStruct(s, flags);
    return new Result(m, flags.unknowns);
  }

  /** Top-level property bag is never a sig-shape; iterate fields plainly. */
  private static Map<String, Object> convertTopLevelStruct(Struct s, Flags flags) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<String, Value> e : s.getFieldsMap().entrySet()) {
      out.put(e.getKey(), convertValue(e.getValue(), flags));
    }
    return Collections.unmodifiableMap(out);
  }

  private static Object convertValue(Value v, Flags flags) {
    switch (v.getKindCase()) {
      case NULL_VALUE:   return null;
      case BOOL_VALUE:   return v.getBoolValue();
      case NUMBER_VALUE: return v.getNumberValue();
      case STRING_VALUE:
        String s = v.getStringValue();
        if (UNKNOWN_SENTINEL.equals(s)) {
          flags.unknowns = true;
          return Unknown.INSTANCE;
        }
        return s;
      case LIST_VALUE:   return convertList(v.getListValue(), flags);
      case STRUCT_VALUE: return convertStructOrSig(v.getStructValue(), flags);
      case KIND_NOT_SET:
      default:           return null;
    }
  }

  /** Nested struct: check for sig key and dispatch, else plain map. */
  private static Object convertStructOrSig(Struct s, Flags flags) {
    Value sigVal = s.getFieldsMap().get(SIG_KEY);
    if (sigVal != null && sigVal.getKindCase() == Value.KindCase.STRING_VALUE) {
      switch (sigVal.getStringValue()) {
        case SIG_SECRET:       return convertSecret(s, flags);
        case SIG_ASSET:        return convertAsset(s);
        case SIG_ARCHIVE:      return convertArchive(s, flags);
        case SIG_RESOURCE_REF: return convertResourceReference(s, flags);
        default: /* fall through to plain map */
      }
    }
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<String, Value> e : s.getFieldsMap().entrySet()) {
      out.put(e.getKey(), convertValue(e.getValue(), flags));
    }
    return Collections.unmodifiableMap(out);
  }

  private static Secret<Object> convertSecret(Struct s, Flags flags) {
    Value inner = s.getFieldsMap().get("value");
    return Secret.of(inner == null ? null : convertValue(inner, flags));
  }

  private static Asset convertAsset(Struct s) {
    String hash = getStringOrNull(s, "hash");
    String text = getStringOrNull(s, "text");
    String path = getStringOrNull(s, "path");
    String uri  = getStringOrNull(s, "uri");
    if (text != null) return Asset.fromText(text, hash);
    if (path != null) return Asset.fromPath(path, hash);
    if (uri  != null) return Asset.fromUri(uri, hash);
    // Empty/unknown asset: synthesise a TEXT asset with empty content so policies don't NPE.
    return Asset.fromText("", hash);
  }

  private static Archive convertArchive(Struct s, Flags flags) {
    String hash  = getStringOrNull(s, "hash");
    Value assets = s.getFieldsMap().get("assets");
    String path  = getStringOrNull(s, "path");
    String uri   = getStringOrNull(s, "uri");
    if (assets != null && assets.getKindCase() == Value.KindCase.STRUCT_VALUE) {
      Map<String, Object> nested = new LinkedHashMap<>();
      for (Map.Entry<String, Value> e : assets.getStructValue().getFieldsMap().entrySet()) {
        nested.put(e.getKey(), convertValue(e.getValue(), flags));
      }
      return Archive.fromAssets(nested, hash);
    }
    if (path != null) return Archive.fromPath(path, hash);
    if (uri  != null) return Archive.fromUri(uri, hash);
    return Archive.fromAssets(Collections.emptyMap(), hash);
  }

  private static ResourceReference convertResourceReference(Struct s, Flags flags) {
    String urn  = getStringOrNull(s, "urn");
    String pkg  = getStringOrNull(s, "packageVersion");
    Value idVal = s.getFieldsMap().get("id");
    Object id = null;
    if (idVal != null) {
      id = convertValue(idVal, flags); // may be Unknown.INSTANCE, a String, or null
    }
    return ResourceReference.of(urn == null ? "" : urn, id, pkg);
  }

  private static String getStringOrNull(Struct s, String field) {
    Value v = s.getFieldsMap().get(field);
    if (v == null || v.getKindCase() != Value.KindCase.STRING_VALUE) return null;
    return v.getStringValue();
  }

  private static List<Object> convertList(ListValue lv, Flags flags) {
    List<Object> out = new ArrayList<>(lv.getValuesCount());
    for (Value v : lv.getValuesList()) {
      out.add(convertValue(v, flags));
    }
    return Collections.unmodifiableList(out);
  }

  private static final class Flags {
    boolean unknowns;
  }

  public static final class Result {
    private final Map<String, Object> values;
    private final boolean containsUnknowns;

    Result(Map<String, Object> values, boolean containsUnknowns) {
      this.values = values;
      this.containsUnknowns = containsUnknowns;
    }

    public Map<String, Object> values() { return values; }
    public boolean containsUnknowns() { return containsUnknowns; }
  }
}
