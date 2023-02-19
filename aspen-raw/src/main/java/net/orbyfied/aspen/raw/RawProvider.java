package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.nodes.RawNode;

import java.io.Reader;
import java.io.Writer;

/**
 * The raw configuration provider.
 *
 * This is usually some kind of configuration
 * format library like SnakeYAML.
 */
public interface RawProvider {

    /**
     * Attempts to serialize the given node
     * tree to the writer.
     *
     * @param node The node.
     * @param writer The writer.
     */
    void write(RawNode node, Writer writer);

    /**
     * Parses the input from the reader into
     * a node tree to be loaded.
     *
     * @param reader The reader.
     * @return The node.
     */
    RawNode compose(Reader reader);

}
