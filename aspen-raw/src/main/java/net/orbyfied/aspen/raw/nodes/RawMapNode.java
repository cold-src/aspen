package net.orbyfied.aspen.raw.nodes;

import java.util.*;

/**
 * A node which holds a section, object or just map.
 */
@SuppressWarnings("unchecked")
public class RawMapNode extends RawListNode {

    public RawMapNode() {

    }

    public RawMapNode(List<RawNode> value) {
        super(value);
    }

    public void toMap(Map<String, RawNode> map) {
        for (RawNode node : nodes) {
            // check for tuple/pair
            if (node instanceof RawPairNode pairNode) {
                map.put(((RawScalarNode<String>)pairNode.getKey()).getValue(),
                        pairNode.getValue());
            }
        }
    }

    public Map<String, RawNode> toMap() {
        Map<String, RawNode> map = new HashMap<>();
        toMap(map);
        return map;
    }

    public void putEntry(Object key, RawNode node) {
        nodes.add(new RawPairNode(new RawScalarNode<>(key), node));
    }

    @Override
    public RawMapNode merge(RawListNode node) {
        List<RawNode> nodes = new ArrayList<>(this.nodes);
        nodes.addAll(node.nodes);
        return new RawMapNode(nodes);
    }
}
