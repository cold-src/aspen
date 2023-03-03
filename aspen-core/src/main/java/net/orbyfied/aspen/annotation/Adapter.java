package net.orbyfied.aspen.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes a field with a configuration processing
 * adapter. An adapter is a piece of code which is
 * run without abstraction in an operation.
 *
 * TODO
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Adapter {

}
