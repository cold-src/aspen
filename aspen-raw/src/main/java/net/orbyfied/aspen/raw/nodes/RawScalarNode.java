package net.orbyfied.aspen.raw.nodes;

import java.util.Objects;

/**
 * A node which holds a value.
 */
public class RawScalarNode<T> extends RawValueNode {

    public static <T> RawScalarNode<T> nullNode() {
        return new RawScalarNode<>(null);
    }

    // the value of the node
    T value;

    // the value style of this node
    ValueStyle style = ValueStyle.PLAIN;

    public RawScalarNode() { }

    public RawScalarNode(T value) {
        this.value = value;
    }

    /** Set the value of this node. */
    public RawScalarNode<T> setValue(T value) {
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

    public RawScalarNode<T> setStyle(ValueStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public String getDataString() {
        return Objects.toString(value);
    }

}
