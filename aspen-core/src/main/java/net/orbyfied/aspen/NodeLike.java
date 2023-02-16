package net.orbyfied.aspen;

import net.orbyfied.aspen.raw.ValueNode;

/**
 * An object similar or related to a
 * node in the configuration.
 *
 * @author orbyfied
 */
public interface NodeLike {

    /**
     * Get the parent section of this node.
     */
    NodeLike getParent();

    /**
     * Get the name of this node.
     */
    String getName();

    /**
     * Emit a node representing the
     * value of this node like.
     *
     * @return The node.
     */
    ValueNode<?> emit();

    /**
     * Load the value of the node into
     * this node like.
     *
     * @param node The node.
     */
    void load(ValueNode<?> node);

}
