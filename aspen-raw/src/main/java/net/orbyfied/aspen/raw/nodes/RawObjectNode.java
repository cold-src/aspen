package net.orbyfied.aspen.raw.nodes;

import java.util.*;

/**
 * A node which holds a section, object or just map.
 */
@SuppressWarnings("unchecked")
public class RawObjectNode extends RawSeqNode<Map<String, Object>> {

    // the cached map
    LinkedHashMap<String, RawNode> cachedMap;

    public RawObjectNode() {

    }

    public RawObjectNode(List<RawNode> value) {
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
        if (cachedMap == null) {
            cachedMap = new LinkedHashMap<>();
            toMap(cachedMap);
        }

        return cachedMap;
    }

    public void putEntry(Object key, RawNode node) {
        nodes.add(new RawPairNode(new RawScalarNode<>(key), node));
        invalidateCache();
    }

    public RawObjectNode merge(RawSeqNode<?> node) {
        List<RawNode> nodes = new ArrayList<>(this.nodes);
        nodes.addAll(node.nodes);
        return new RawObjectNode(nodes);
    }

    @Override
    protected Map<String, Object> toValue0() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (RawNode node : nodes) {
            if (node instanceof RawPairNode pairNode) {
                map.put(
                        (String) pairNode.getKey().require(RawScalarNode.class).getValueAs(),
                        pairNode.getValue().require(RawValueNode.class).toValue()
                );
            }
        }

        return map;
    }

}
