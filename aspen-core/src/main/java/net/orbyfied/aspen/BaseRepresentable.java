package net.orbyfied.aspen;

import net.orbyfied.aspen.raw.Representable;

/**
 * An object similar or related to a
 * node in the configuration.
 *
 * @author orbyfied
 */
public interface BaseRepresentable extends Representable<Context> {

    /**
     * Get the parent section of this node.
     */
    BaseRepresentable getParent();

    /**
     * Get the name of this node.
     */
    String getName();

}
