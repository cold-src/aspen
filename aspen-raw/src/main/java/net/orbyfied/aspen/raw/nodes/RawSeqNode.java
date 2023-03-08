package net.orbyfied.aspen.raw.nodes;

import java.util.ArrayList;
import java.util.List;

@RawNodeTypeDesc(typeName = "seq")
public abstract class RawSeqNode<V> extends RawValueNode<V> {

    // the list of nodes
    final List<RawNode> nodes;

    public RawSeqNode() {
        this.nodes = new ArrayList<>();
    }

    public RawSeqNode(List<RawNode> value) {
        this.nodes = value;
    }

    public List<RawNode> getNodes() {
        return nodes;
    }

    public int getSize() {
        return nodes.size();
    }

    public RawNode getElement(int key) {
        return nodes.get(key);
    }

    public RawSeqNode<V> addElement(RawNode elem) {
        nodes.add(elem);
        invalidateCache();
        return this;
    }

    public RawSeqNode<V> setElement(int key, RawNode elem) {
        int size = getSize();
        boolean keyInRange = !(key < 0 || key >= size);
        if (keyInRange) {
            nodes.set(key, elem);
            return this;
        }

        int diff = size - key;
        for (int i = 0; i < diff; i++) {
            nodes.set(i, null);
        }

        nodes.set(key, elem);
        invalidateCache();
        return this;
    }

    public void addAll(RawSeqNode<V> node) {
        nodes.addAll(node.nodes);
        invalidateCache();
    }
    
}
