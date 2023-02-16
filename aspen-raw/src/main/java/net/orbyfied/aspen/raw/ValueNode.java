package net.orbyfied.aspen.raw;

import java.util.Objects;

/**
 * A node which holds a value.
 */
public class ValueNode<T> extends Node {

    // the value of the node
    T value;

    // the value style of this node
    ValueStyle style = ValueStyle.PLAIN;

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

    public ValueStyle getStyle() {
        return style;
    }

    public ValueNode<T> setStyle(ValueStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public String getDataString() {
        return Objects.toString(value);
    }

}
