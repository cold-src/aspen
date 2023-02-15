package net.orbyfied.aspen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes the default values for an option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Defaults {

    /**
     * The path to the archive resource
     * containing the default content.
     */
    String resource() default "";

}
