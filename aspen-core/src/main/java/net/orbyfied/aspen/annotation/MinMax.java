package net.orbyfied.aspen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// may be temporary, to be replaced by
// a proper implementation of @Range
@Retention(RetentionPolicy.RUNTIME)
public @interface MinMax {

    double min();

    double max();

}
