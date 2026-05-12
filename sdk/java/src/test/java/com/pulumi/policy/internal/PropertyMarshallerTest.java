package com.pulumi.policy.internal;

import com.google.protobuf.ListValue;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyMarshallerTest {
  @Test
  void emptyStructRoundTripsToEmptyMap() {
    Map<String, Object> m = PropertyMarshaller.structToMap(Struct.getDefaultInstance());
    assertThat(m).isEmpty();
  }

  @Test
  void scalarsConvert() {
    Struct s = Struct.newBuilder()
        .putFields("s", Value.newBuilder().setStringValue("hello").build())
        .putFields("n", Value.newBuilder().setNumberValue(42.0).build())
        .putFields("b", Value.newBuilder().setBoolValue(true).build())
        .putFields("nil", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
        .build();
    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    assertThat(m).containsEntry("s", "hello");
    assertThat(m).containsEntry("n", 42.0);
    assertThat(m).containsEntry("b", true);
    assertThat(m).containsEntry("nil", null);
  }

  @Test
  void nestedListAndMapConvert() {
    Struct inner = Struct.newBuilder()
        .putFields("k", Value.newBuilder().setStringValue("v").build())
        .build();
    Struct s = Struct.newBuilder()
        .putFields("list", Value.newBuilder().setListValue(
            ListValue.newBuilder()
                .addValues(Value.newBuilder().setNumberValue(1.0).build())
                .addValues(Value.newBuilder().setStringValue("two").build())
                .build()).build())
        .putFields("nested", Value.newBuilder().setStructValue(inner).build())
        .build();

    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    assertThat(m.get("list")).isEqualTo(List.of(1.0, "two"));
    assertThat(m.get("nested")).isEqualTo(Map.of("k", "v"));
  }

  @Test
  void unknownSentinelFlagsContainsUnknowns() {
    // Pulumi marshals computed/unknown values as the string sentinel
    // "04da6b54-80e4-46f7-96ec-b56ff0331ba9"
    Struct s = Struct.newBuilder()
        .putFields("unk", Value.newBuilder()
            .setStringValue("04da6b54-80e4-46f7-96ec-b56ff0331ba9").build())
        .build();
    PropertyMarshaller.Result r = PropertyMarshaller.structToMapWithFlags(s);
    assertThat(r.containsUnknowns()).isTrue();
    // value present as null placeholder in map for MVP
    assertThat(r.values()).containsEntry("unk", null);
  }
}
