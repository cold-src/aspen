package net.orbyfied.aspen;

import net.orbyfied.aspen.context.PropertyContext;

/**
 * Provides simple, contextualized access
 * to a declared property to allow easy retrieval
 * and registration of the value.
 *
 * @param <T> The value type.
 */
public interface PropertyAccess<T> {

    /**
     * Creates a placeholder access which can
     * be used to specify the exact property which
     * needs to be accessed.
     *
     * @param property The property.
     * @param <T> The value type.
     * @return The future access instance.
     */
    @SuppressWarnings("unchecked")
    static <T> PropertyAccess<T> future(Property<T, ?> property) {
        return new Future(property);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    class Future implements PropertyAccess {
        Property property;
        PropertyAccess access;

        Future(Property property) {
            this.property = property;
        }

        @Override
        public void set(Object value) {
            if (access == null)
                throw new IllegalStateException("Uncompleted accessor");
            access.set(value);
        }

        @Override
        public Object get() {
            if (access == null)
                throw new IllegalStateException("Uncompleted accessor");
            return access.get();
        }

        @Override
        public boolean has() {
            if (access == null)
                throw new IllegalStateException("Uncompleted accessor");
            return access.has();
        }
    }

    static <T> PropertyAccess<T> constant(Property<T, ?> property,
                                          ConfigurationProvider provider,
                                          Schema schema) {
        return new PropertyAccess<>() {
            // create context
            final PropertyContext context = new PropertyContext(provider, null, schema);

            @Override
            public void set(T value) {
                property.set(context, value);
            }

            @Override
            public T get() {
                return property.get(context);
            }

            @Override
            public boolean has() {
                return property.has(context);
            }
        };
    }

    //////////////////////////////////////////

    void set(T value);

    T get();

    boolean has();

}
