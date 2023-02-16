package net.orbyfied.aspen;

import net.orbyfied.aspen.exception.PropertyLoadException;
import net.orbyfied.aspen.raw.ValueNode;
import org.jetbrains.annotations.Contract;

/**
 * A property associated with a key in a
 * schema. This can be on an object or
 * on a configuration section.
 *
 * This holds general settings that can
 * be applied to any key-value pair stored,
 * like comments.
 *
 * For storing more complex types/special,
 * enum-like types like Minecraft blocks there
 * is a value type T and a primitive type P.
 *
 * The implementation of special properties
 * should override the {@link #valueFromPrimitive(Object)}
 * and {@link #valueToPrimitive(Object)} to
 * properly convert.
 *
 * @param <T> The value/complex type.
 * @param <P> The primitive type.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Property<T, P> implements NodeLike {

    /**
     * Builder classifier for typed properties, which need
     * to be mappable to special property types.
     */
    public interface TypedBuilder {

    }

    /** Builder Class */
    public static abstract class Builder<T, P, S, R extends Property<T, P>> {

        /* properties */
        protected final String name;
        protected final Class<T> complexType;
        protected final Class<P> primitiveType;

        protected ConfigurationProvider provider;

        protected String comment;
        protected Accessor<T> accessor = /* todo: default accessor */ null;

        protected Builder(String name, Class<T> complexType, Class<P> primitiveType) {
            this.name = name;
            this.complexType = complexType;
            this.primitiveType = primitiveType;
        }

        // get this casted to S
        protected final S self() {
            return (S) this;
        }

        /* Properties */

        public S provider(ConfigurationProvider provider) {
            this.provider = provider;
            return self();
        }

        public S comment(String str) {
            this.comment = str;
            return self();
        }

        public S accessor(Accessor<T> accessor) {
            this.accessor = accessor;
            return self();
        }

        /**
         * Build the new property.
         *
         * @return The property.
         */
        public abstract R build();

    }

    //////////////////////////////////////////

    // the name of this property
    protected final String name;

    // the value type of this property
    protected final Class<T> type;

    // the primitive type to save as
    protected final Class<P> primitiveType;

    // the comment in the generated file
    protected final String comment;

    // the value accessor
    protected Accessor<T> accessor;

    // the schema this property is located in
    protected Schema schema;

    /**
     * Constructor for builders.
     *
     * By default (if the instance is instance of the
     * default implementation) it will throw an error
     * if the complex and primitive types are
     * not equal, and just return the primitive
     * back.
     */
    protected Property(String name, Class<?> type,
                       Class<?> primitiveType, String comment,
                       Accessor<T> accessor) {
        this.name = name;
        this.type = (Class<T>) type;
        this.primitiveType = (Class<P>) primitiveType;
        this.comment = comment;
        this.accessor = accessor;

        // check conversions between primitive
        // and complex types
        if (getClass() == Property.class /* no special impl */) {
            if (!type.isAssignableFrom(primitiveType)) {
                throw new IllegalArgumentException("default property implementation does not support conversion between T:" +
                        type.getName() + " -> P:" + primitiveType.getName());
            }
        }
    }

    public Schema getSchema() {
        return schema;
    }

    // fail parsing of the property
    // with a message
    @Contract("_ -> fail")
    protected <T2> T2 failLoad(String s) {
        throw new PropertyLoadException(getClass().getSimpleName() + "(" + name + "): " + s);
    }



    /**
     * Converts the primitive value to
     * a complex value.
     *
     * By default it will throw an error
     * if the complex and primitive types are
     * not equal, and just return the primitive
     * back.
     *
     * @param primitive The primitive value.
     * @return The value.
     */
    public T valueFromPrimitive(P primitive) {
        return (T) primitive;
    }

    /**
     * Converts the complex value to
     * a primitive value.
     *
     * By default it will throw an error
     * if the complex and primitive types are
     * not equal, and just return the primitive
     * back.
     *
     * @param value The value.
     * @return The primitive value.
     */
    public P valueToPrimitive(T value) {
        return (P) value;
    }

    public T get() {
        return accessor.get(schema, this);
    }

    public void set(T value) {
        accessor.register(schema, this, value);
    }

    /**
     * Load the value from the given node.
     *
     * Utilizes {@link #valueFromPrimitive(Object)}
     * to convert the saved value by default.
     *
     * @param node The node.
     * @return The value.
     */
    public T loadValue(ValueNode node) {
        return valueFromPrimitive((P) node.getValue());
    }

    /**
     * Emit a node for the given value.
     *
     * Utilizes {@link #valueToPrimitive(Object)}
     * to convert the value by default.
     *
     * @param value The value.
     * @return The node.
     */
    public ValueNode emitValue(T value) {
        P primitiveValue = valueToPrimitive(value);
        return new ValueNode<>(primitiveValue);
    }

    @Override
    public ValueNode emit() {
        return emitValue(get());
    }

    @Override
    public void load(ValueNode node) {
        set(loadValue(node));
    }

    /* Getters */

    @Override
    public NodeLike getParent() {
        return null; // TODO
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public Class<T> getType() {
        return type;
    }

    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    public Accessor<T> getAccessor() {
        return accessor;
    }

}
