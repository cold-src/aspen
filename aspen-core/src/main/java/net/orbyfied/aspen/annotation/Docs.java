package net.orbyfied.aspen.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies documentation for a property,
 * division or section.
 *
 * @author orbyfied
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Docs {

    String value();

}
