package net.orbyfied.aspen;

import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.exception.PropertyLoadException;
import net.orbyfied.aspen.raw.Node;
import net.orbyfied.aspen.raw.ValueNode;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    public static class Builder<T, P, R extends Property<T, P>> {

        /* properties */
        protected final String name;
        protected final Class<T> complexType;
        protected final Class<P> primitiveType;

        protected ConfigurationProvider provider;

        protected Consumer<Node> commenter;
        protected Accessor<T> accessor;

        protected List<PropertyComponent> components = new ArrayList<>();

        protected Supplier<T> defaultValueSupplier;

        protected boolean shared = false;

        // the instance factory
        private final Supplier<R> factory;

        public Builder(String name, Class complexType, Class primitiveType,
                       Supplier<R> factory) {
            this.name = name;
            this.complexType = complexType;
            this.primitiveType = primitiveType;
            this.factory = factory;
        }

        // creates a new anonymous builder
        // for embedded values
        public static <T, P, R extends Property<T, P>> Builder<T, P, R> newAnonymous(
                Class<T> tClass, Class<P> pClass, Supplier<R> supplier
        ) {
            return new Builder<>("<anonymous>", tClass, pClass, supplier);
        }

        /* Properties */

        public Builder<T, P, R> with(PropertyComponent component) {
            components.add(component);
            return this;
        }

        public <C extends PropertyComponent> Builder<T, P, R> with(C component, Consumer<C> consumer) {
            with(component);
            consumer.accept(component);
            return this;
        }

        public Builder<T, P, R> provider(ConfigurationProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder<T, P, R> commenter(Consumer<Node> commenter) {
            this.commenter = commenter;
            return this;
        }

        public Builder<T, P, R> accessor(Accessor<T> accessor) {
            this.accessor = accessor;
            return this;
        }

        public Builder<T, P, R> shared() {
            this.shared = true;
            return this;
        }

        public Builder<T, P, R> defaultValue(Supplier<T> supplier) {
            this.defaultValueSupplier = supplier;
            return this;
        }

        public Builder<T, P, R> defaultValue(T val) {
            return defaultValue(() -> val);
        }

        public R build() {
            // create instance
            R property = factory.get();

            // set properties
            property.provider = provider;
            property.name = name;
            property.primitiveType = primitiveType;
            property.complexType = complexType;
            if (property.accessor == null) property.accessor = accessor;
            if (property.commenter == null) property.commenter = commenter;

            // create actual accessor
            if (shared)
                property.accessor = Accessor.sharedMutable();
            property.actualAccessor = Accessor.defaulted(Accessor.dynamic(() -> property.accessor), defaultValueSupplier);

            // add components
            if (!components.isEmpty()) {
                property.componentMap = new HashMap<>();
                for (PropertyComponent component : components)
                    property.componentMap.put(component.getClass(), component);
            }

            // check conversions between primitive
            // and complex types
            if (property.getClass() == Property.class /* no special impl */) {
                if (!complexType.isAssignableFrom(primitiveType)) {
                    throw new IllegalArgumentException("default property implementation does not support conversion between T:" +
                            complexType.getName() + " -> P:" + primitiveType.getName());
                }
            }

            property.init();

            return property;
        }

    }

    //////////////////////////////////////////

    // the configuration provider
    protected ConfigurationProvider provider;

    // the name of this property
    protected String name;

    // the value type of this property
    protected Class<T> complexType;

    // the primitive type to save as
    protected Class<P> primitiveType;

    // the comment in the generated file
    protected Consumer<Node> commenter;

    // the value accessor
    protected Accessor<T> accessor;
    protected Accessor<T> actualAccessor;

    // the schema this property is located in
    protected Schema schema;

    // the components on this property
    protected Map<Class<?>, PropertyComponent> componentMap;

    /*
        Cached Values
     */

    PropertyContext localContext;

    /**
     * Constructor for builders.
     */
    protected Property() { }

    // initializes this property
    // after the builder is done
    // configuring
    protected void init() {
        localContext = new PropertyContext(
                provider,
                null,
                schema
        );
    }

    // get the property context from
    // the given context, or use the
    // local context
    protected PropertyContext getPropertyContextOrLocal(Context context) {
        if (context == null)
            return localContext;
        if (context instanceof PropertyContext pc) {
            return pc.property(this);
        }

        return PropertyContext.from(context, this);
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

    public <C extends PropertyComponent> C component(Class<C> cClass) {
        return (C) componentMap.get(cClass);
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

    /**
     * Get in the given context.
     *
     * @param context The context.
     * @return The value.
     */
    public T get(PropertyContext context) {
        return actualAccessor.get(context.property(this));
    }

    public T get() {
        return get(localContext);
    }

    /**
     * Set the value in the given context.
     *
     * @param context The context.
     * @param value The value.
     */
    public void set(PropertyContext context, T value) {
        actualAccessor.register(context.property(this), value);
    }

    public void set(T value) {
        set(localContext, value);
    }

    /**
     * Get if the property has a value set
     * in the given context.
     *
     * @param context The context.
     * @return True/false.
     */
    public boolean has(PropertyContext context) {
        return actualAccessor.has(context);
    }

    public boolean has() {
        return has(localContext);
    }

    // load value impl
    protected T loadValue0(ValueNode node) {
        return valueFromPrimitive((P) node.getValue());
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
        T val = loadValue0(node);

        // pass through components
        for (PropertyComponent component : componentMap.values()) {
            component.checkLoadedValue(val);
        }

        return val;
    }

    // emit value impl
    protected ValueNode emitValue0(T value) {
        P primitiveValue = valueToPrimitive(value);
        return new ValueNode<>(primitiveValue);
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
        return emitValue0(value);
    }

    @Override
    public ValueNode emit(Context context) {
        Node node = emitValue0(get(getPropertyContextOrLocal(context)));
        if (commenter != null)
            commenter.accept(node);
        return (ValueNode) node;
    }

    @Override
    public void load(Context context, ValueNode node) {
        set(getPropertyContextOrLocal(context), loadValue0(node));
    }

    public ValueNode emit() {
        return emit(null);
    }

    public void load(ValueNode node) {
        load(null, node);
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

    public Class<T> getComplexType() {
        return complexType;
    }

    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    public Accessor<T> getAccessor() {
        return accessor;
    }

}
