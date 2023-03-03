package net.orbyfied.aspen.raw.nodes;

import net.orbyfied.aspen.raw.format.ScalarStyle;

import java.util.Objects;

/**
 * A node which holds a value.
 */
@SuppressWarnings("unchecked")
public class RawScalarNode<T> extends RawValueNode<T> {

    public static <T> RawScalarNode<T> nullNode() {
        return new RawScalarNode<>(null);
    }

    /////////////////////////////////////

    // the value of the node
    T value;

    // the value style of this node
    ScalarStyle style = null;

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

    @SuppressWarnings("unchecked")
    public <T2> T2 getValueAs() {
        return (T2) value;
    }

    public <T2> RawScalarNode<T2> expect(Class<T2> typeClass) {
        if (!typeClass.isInstance(value))
            throw new IllegalArgumentException("Value Error: expected " + typeClass + ", got " + (value == null ? "null" : value.getClass()));
        return (RawScalarNode<T2>) this;
    }

    public <T2> RawScalarNode<T2> expectNullable(Class<T2> typeClass) {
        if (value == null)
            return (RawScalarNode<T2>) this;
        return expect(typeClass);
    }

    public ScalarStyle getStyle() {
        return style;
    }

    public RawScalarNode<T> setStyle(ScalarStyle style) {
        this.style = style;
        return this;
    }

    @Override
    public String getDataString() {
        return Objects.toString(value);
    }

    @Override
    protected T toValue0() {
        return value;
    }

    @Override
    public T toValue() {
        return value;
    }

}
