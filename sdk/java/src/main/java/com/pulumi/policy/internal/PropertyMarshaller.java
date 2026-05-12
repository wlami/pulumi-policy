package com.pulumi.policy.internal;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PropertyMarshaller {
  // Sentinel value used by Pulumi to mark a computed/unknown property.
  // See sdk/go/common/resource/properties.go (UnknownStringValue, etc.)
  private static final String UNKNOWN_SENTINEL = "04da6b54-80e4-46f7-96ec-b56ff0331ba9";

  private PropertyMarshaller() {}

  public static Map<String, Object> structToMap(Struct s) {
    return structToMapWithFlags(s).values();
  }

  public static Result structToMapWithFlags(Struct s) {
    Flags flags = new Flags();
    Map<String, Object> m = convertStruct(s, flags);
    return new Result(m, flags.unknowns);
  }

  private static Map<String, Object> convertStruct(Struct s, Flags flags) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<String, Value> e : s.getFieldsMap().entrySet()) {
      out.put(e.getKey(), convertValue(e.getValue(), flags));
    }
    return Collections.unmodifiableMap(out);
  }

  private static Object convertValue(Value v, Flags flags) {
    switch (v.getKindCase()) {
      case NULL_VALUE: return null;
      case BOOL_VALUE: return v.getBoolValue();
      case NUMBER_VALUE: return v.getNumberValue();
      case STRING_VALUE:
        String s = v.getStringValue();
        if (UNKNOWN_SENTINEL.equals(s)) {
          flags.unknowns = true;
          return null;
        }
        return s;
      case LIST_VALUE: return convertList(v.getListValue(), flags);
      case STRUCT_VALUE: return convertStruct(v.getStructValue(), flags);
      case KIND_NOT_SET:
      default: return null;
    }
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
