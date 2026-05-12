package com.pulumi.policy;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnforcementLevelTest {
  @Test
  void allFourLevelsExist() {
    assertThat(EnforcementLevel.values())
        .containsExactly(
            EnforcementLevel.ADVISORY,
            EnforcementLevel.MANDATORY,
            EnforcementLevel.REMEDIATE,
            EnforcementLevel.DISABLED);
  }

  @Test
  void toProtoRoundTrip() {
    for (EnforcementLevel level : EnforcementLevel.values()) {
      pulumirpc.AnalyzerOuterClass.EnforcementLevel proto = level.toProto();
      assertThat(EnforcementLevel.fromProto(proto)).isEqualTo(level);
    }
  }
}
