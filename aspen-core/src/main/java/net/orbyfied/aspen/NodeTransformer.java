package net.orbyfied.aspen;

import net.orbyfied.aspen.raw.nodes.RawNode;

/**
 * Allows processing of raw node data.
 */
public interface NodeTransformer {

    /**
     * Called before a node tree is
     * loaded to transform the input
     * data.
     *
     * @param node The input node.
     * @return The processed input node.
     */
    RawNode preProcess(RawNode node);

    /**
     * Called after a node tree was emitted
     * to transform the written data.
     *
     * @param node The output node.
     * @return The processed output node.
     */
    RawNode postProcess(RawNode node);

}
