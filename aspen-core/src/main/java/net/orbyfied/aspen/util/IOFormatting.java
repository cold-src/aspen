package net.orbyfied.aspen.util;

import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.raw.source.NodeSource;

/**
 * Formatting utilities for property input and
 * output with raw nodes.
 */
public class IOFormatting {

    public static void formatNodeSource(StringBuilder b, NodeSource source) {
        if (source == null)
            b.append("from unknown source");
        else
            b.append("from ").append(source.toPrettyString());
    }

    public static void formatNodeSource(StringBuilder b, RawNode node) {
        if (node == null)
            return;
        formatNodeSource(b, node.source());
    }

}
