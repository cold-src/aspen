package net.orbyfied.aspen.raw;

/**
 * A node which holds a value.
 */
public class ValueNode<T> extends Node {

    // the value of the node
    T value;

    public ValueNode() { }

    public ValueNode(T value) {
        this.value = value;
    }

    /** Set the value of this node. */
    public ValueNode<T> setValue(T value) {
        this.value = value;
        return this;
    }

    /** Get the value of this node. */
    public T getValue() {
        return value;
    }

}
