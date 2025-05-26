package com.cleanengine.coin.tool.annotation;

import com.cleanengine.coin.tool.helper.WithCustomMockUserSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMockUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    String name() default "user";
    int id() default 1;
}
