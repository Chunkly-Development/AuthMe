package org.chunkly.authme.lib.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

    String label();
    String parent();
    String permission() default "";
    String[] aliases() default {};
    boolean appendStrings() default false;
}
