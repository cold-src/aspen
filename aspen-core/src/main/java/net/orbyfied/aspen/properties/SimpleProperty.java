package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Property;

@SuppressWarnings("rawtypes")
public class SimpleProperty<T> extends Property<T, T> {

    public static <T> Builder<T, T, SimpleProperty<T>> builder(String name, Class<T> tClass) {
        return new Builder<>(name, tClass, tClass, SimpleProperty::new);
    }

    public static <T> Builder<T, T, SimpleProperty<T>> anonymous(Class<T> tClass) {
        return Builder.newAnonymous(tClass, tClass, SimpleProperty::new);
    }

}
