package net.orbyfied.aspen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Base option descriptor for fields.
 *
 * @author orbyfied
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    /**
     * The name of this option.
     */
    String name() default "(get)";

}
