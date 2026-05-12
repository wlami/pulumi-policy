package com.pulumi.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UnknownTest {
  @Test
  void singletonInstance() {
    assertThat(Unknown.INSTANCE).isSameAs(Unknown.INSTANCE);
  }

  @Test
  void toStringIsStable() {
    assertThat(Unknown.INSTANCE.toString()).isEqualTo("<unknown>");
  }

  @Test
  void instanceOfCheckWorks() {
    Object o = Unknown.INSTANCE;
    assertThat(o).isInstanceOf(Unknown.class);
  }
}
