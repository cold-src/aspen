package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.nodes.RawNode;

public interface Representable<C extends RawContext> {

    /**
     * Emit the representable into a
     * raw node tree using the given context.
     *
     * @param context The context.
     * @return The node tree.
     */
    RawNode emit(C context);

    /**
     * Load the given node tree as a value
     * in the provided context.
     *
     * @param context The context.
     * @param node The node.
     */
    void load(C context, RawNode node);

}
