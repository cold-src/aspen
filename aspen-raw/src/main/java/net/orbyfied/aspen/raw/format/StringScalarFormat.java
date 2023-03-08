package net.orbyfied.aspen.raw.format;

import net.orbyfied.aspen.raw.nodes.RawScalarNode;
import net.orbyfied.aspen.raw.nodes.RawValueNode;

/**
 * Specifies how to parse and dump scalars
 * into string form with an {@link ScalarStyle} associated.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public interface StringScalarFormat {

    /**
     * Load a scalar value using the
     * given string and style read into
     * the given node.
     *
     * @param representation The read representation.
     * @return The node.
     */
    RawValueNode<?> load(StringScalarRepresentation representation);

    /**
     * Dump the value of the given scalar into
     * an output string and style to write.
     *
     * @param node The node.
     * @return The output.
     */
    StringScalarRepresentation dump(RawValueNode<?> node);

}
