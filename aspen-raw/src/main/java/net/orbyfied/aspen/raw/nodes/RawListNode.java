package net.orbyfied.aspen.raw.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * A node which holds a list of values.
 */
@SuppressWarnings("rawtypes")
@RawNodeTypeDesc(typeName = "list")
public class RawListNode extends RawSeqNode<List<Object>> {

    public RawListNode() {

    }

    public RawListNode(List<RawNode> value) {
        super(value);
    }

    public RawListNode merge(RawSeqNode<?> node) {
        List<RawNode> nodes = new ArrayList<>(this.nodes);
        nodes.addAll(node.nodes);
        return new RawListNode(nodes);
    }

    @Override
    protected List<Object> toValue0() {
        ArrayList<Object> list = new ArrayList<>();
        for (RawNode node : nodes) {
            if (node instanceof RawValueNode valueNode) {
                list.add(valueNode.toValue());
            }
        }

        return list;
    }

}
