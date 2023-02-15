package net.orbyfied.aspen;

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
     * Get the comment to set.
     * Null if none.
     */
    String getComment();

}
