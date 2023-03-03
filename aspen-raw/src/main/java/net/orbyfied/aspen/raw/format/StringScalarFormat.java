package net.orbyfied.aspen.raw.format;

import net.orbyfied.aspen.raw.nodes.RawScalarNode;

/**
 * Specifies how to parse and dump scalars
 * into string form with an {@link ScalarStyle} associated.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface StringScalarFormat {

    /**
     * Load a scalar value using the
     * given string and style read.
     *
     * @param representation The read representation.
     * @return The value.
     */
    Object load(StringScalarRepresentation representation);

    /**
     * Dump the given scalar value into
     * an output string and style to write.
     *
     * @param value The value.
     * @return The output.
     */
    StringScalarRepresentation dump(Object value);

    default void load(StringScalarRepresentation representation,
                      RawScalarNode node) {
        node.setValue(load(representation));
    }

    default StringScalarRepresentation dump(RawScalarNode<?> node) {
        return dump(node.getValue());
    }

}
