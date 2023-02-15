package net.orbyfied.aspen;

import java.util.function.Function;

/**
 * Defines behaviour for complex properties
 * with the given complex type to function.
 *
 * @param <T> The value type.
 * @param <P> The primitive type.
 *
 * @author orbyfied
 */
public record PropertyBehaviour<T, P>(Class<T> complexClass, Class<P> primitiveClass,
                                      Function<T, P> t2pConverter,
                                      Function<P, T> p2tConverter) {

}
