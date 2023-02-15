package net.orbyfied.aspen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a number range for an
 * option.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {

    /**
     * The range value.
     *
     * Format:
     * value: [group]+
     * group: min;max
     *
     * Example: [0;500][1000.6;1500.7]
     * - 0 to 500 or 1000.6 to 1500.7
     *
     */
    String value() default "";

}
