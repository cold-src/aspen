package net.orbyfied.aspen.raw.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * A node which holds a list of values.
 */
@SuppressWarnings("rawtypes")
public class RawListNode extends RawValueNode {

    // the list of nodes
    final List<RawNode> nodes;

    public RawListNode() {
        this.nodes = new ArrayList<>();
    }

    public RawListNode(List<RawNode> value) {
        this.nodes = value;
    }

    public List<RawNode> getNodes() {
        return nodes;
    }

    public RawListNode addElement(RawNode elem) {
        nodes.add(elem);
        return this;
    }

    /**
     * Merge this list of nodes with
     * the given node.
     *
     * @param node The node to merge with.
     * @return The new merged node.
     */
    public RawListNode merge(RawListNode node) {
        List<RawNode> nodes = new ArrayList<>(this.nodes);
        nodes.addAll(node.nodes);
        return new RawListNode(nodes);
    }

    public void addAll(RawListNode node) {
        nodes.addAll(node.nodes);
    }
    
}
