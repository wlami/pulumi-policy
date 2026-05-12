package com.pulumi.policy.internal;

import com.pulumi.policy.EnforcementLevel;
import com.pulumi.policy.PolicyPack;
import com.pulumi.policy.PolicyPackArgs;
import com.pulumi.policy.ResourceValidationPolicy;

public class SamplePack {
  public static void main(String[] args) throws Exception {
    PolicyPack.run("sample-pack",
        PolicyPackArgs.builder()
            .enforcementLevel(EnforcementLevel.MANDATORY)
            .policies(ResourceValidationPolicy.builder()
                .name("no-public-s3")
                .description("S3 buckets must not be public")
                .enforcementLevel(EnforcementLevel.MANDATORY)
                .validate((a, r) -> {
                  if (a.isType("aws:s3/bucket:Bucket")
                      && "public-read".equals(a.props().get("acl"))) {
                    r.violation("S3 bucket cannot be public-read");
                  }
                })
                .build())
            .build(),
        args);
  }
}
