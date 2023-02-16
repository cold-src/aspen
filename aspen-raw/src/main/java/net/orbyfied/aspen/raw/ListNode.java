package net.orbyfied.aspen.raw;

import java.util.ArrayList;
import java.util.List;

/**
 * A node which holds a list of values.
 */
@SuppressWarnings("rawtypes")
public class ListNode extends ValueNode<List<ValueNode>> {

    public ListNode() {
        setValue(new ArrayList<>());
    }

    public ListNode(List<ValueNode> value) {
        super(value);
    }

    public ListNode addElement(ValueNode elem) {
        value.add(elem);
        return this;
    }

    /**
     * Merge this list of nodes with
     * the given node.
     *
     * @param node The node to merge with.
     * @return The new merged node.
     */
    public ListNode merge(ListNode node) {
        List<ValueNode> nodes = new ArrayList<>(getValue());
        nodes.addAll(node.value);
        return new ListNode(nodes);
    }
    
}
