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
  void unknownEmitsUnknownInstance() {
    Struct s = Struct.newBuilder()
        .putFields("unk", Value.newBuilder()
            .setStringValue("04da6b54-80e4-46f7-96ec-b56ff0331ba9").build())
        .build();
    PropertyMarshaller.Result r = PropertyMarshaller.structToMapWithFlags(s);
    assertThat(r.containsUnknowns()).isTrue();
    assertThat(r.values().get("unk")).isSameAs(com.pulumi.policy.Unknown.INSTANCE);
  }

  @Test
  void secretEmitsSecretWrapper() {
    Struct s = Struct.newBuilder()
        .putFields("password", Value.newBuilder().setStructValue(
            Struct.newBuilder()
                .putFields("4dabf18193072939515e22adb298388d",
                    Value.newBuilder().setStringValue("1b47061264138c4ac30d75fd1eb44270").build())
                .putFields("value",
                    Value.newBuilder().setStringValue("hunter2").build())
                .build()).build())
        .build();
    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    assertThat(m.get("password")).isInstanceOf(com.pulumi.policy.Secret.class);
    assertThat(((com.pulumi.policy.Secret<?>) m.get("password")).value()).isEqualTo("hunter2");
  }

  @Test
  void textAssetEmitsAssetWrapper() {
    Struct s = Struct.newBuilder()
        .putFields("body", Value.newBuilder().setStructValue(
            Struct.newBuilder()
                .putFields("4dabf18193072939515e22adb298388d",
                    Value.newBuilder().setStringValue("c44067f5952c0a294b673a41bacd8c17").build())
                .putFields("text",
                    Value.newBuilder().setStringValue("hello").build())
                .putFields("hash",
                    Value.newBuilder().setStringValue("sha256:abc").build())
                .build()).build())
        .build();
    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    com.pulumi.policy.Asset a = (com.pulumi.policy.Asset) m.get("body");
    assertThat(a.kind()).isEqualTo(com.pulumi.policy.Asset.Kind.TEXT);
    assertThat(a.text()).isEqualTo("hello");
    assertThat(a.hash()).isEqualTo("sha256:abc");
  }

  @Test
  void pathArchiveEmitsArchiveWrapper() {
    Struct s = Struct.newBuilder()
        .putFields("z", Value.newBuilder().setStructValue(
            Struct.newBuilder()
                .putFields("4dabf18193072939515e22adb298388d",
                    Value.newBuilder().setStringValue("0def7320c3a5731c473e5ecbe6d01bc7").build())
                .putFields("path",
                    Value.newBuilder().setStringValue("/tmp/x.tar.gz").build())
                .build()).build())
        .build();
    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    com.pulumi.policy.Archive a = (com.pulumi.policy.Archive) m.get("z");
    assertThat(a.kind()).isEqualTo(com.pulumi.policy.Archive.Kind.PATH);
    assertThat(a.path()).isEqualTo("/tmp/x.tar.gz");
  }

  @Test
  void resourceReferenceEmitsRefWrapper() {
    Struct s = Struct.newBuilder()
        .putFields("ref", Value.newBuilder().setStructValue(
            Struct.newBuilder()
                .putFields("4dabf18193072939515e22adb298388d",
                    Value.newBuilder().setStringValue("5cf8f73096256a8f31e491e813e4eb8e").build())
                .putFields("urn",
                    Value.newBuilder().setStringValue("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b").build())
                .putFields("id",
                    Value.newBuilder().setStringValue("bucket-abc").build())
                .putFields("packageVersion",
                    Value.newBuilder().setStringValue("5.0.0").build())
                .build()).build())
        .build();
    Map<String, Object> m = PropertyMarshaller.structToMap(s);
    com.pulumi.policy.ResourceReference r = (com.pulumi.policy.ResourceReference) m.get("ref");
    assertThat(r.urn()).isEqualTo("urn:pulumi:dev::p::aws:s3/bucket:Bucket::b");
    assertThat(r.id()).isEqualTo("bucket-abc");
    assertThat(r.packageVersion()).isEqualTo("5.0.0");
  }
}
