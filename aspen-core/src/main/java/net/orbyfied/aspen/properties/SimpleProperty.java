package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.Property;

public class SimpleProperty<T> extends Property<T, T> {

    public static <T> Builder<T> builder(String name, Class<T> tClass) {
        return new Builder<>(name, tClass);
    }

    public static class Builder<T>
            extends Property.Builder<T, T, Builder<T>, SimpleProperty<T>>
            implements TypedBuilder {

        protected Builder(String name, Class<T> complexType) {
            super(name, complexType, complexType);
        }

        @Override
        public SimpleProperty<T> build() {
            return new SimpleProperty<>(
                    name,
                    complexType,
                    comment,
                    accessor
            );
        }
    }

    ///////////////////////////////

    protected SimpleProperty(String name, Class<T> type, String comment, Accessor<T> accessor) {
        super(name, type, type, comment, accessor);
    }

}
