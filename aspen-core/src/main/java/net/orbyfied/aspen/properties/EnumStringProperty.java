package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.Property;

/**
 * Property which stores enum values as string
 * literals.
 * @param <T> The enum type.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnumStringProperty<T extends Enum> extends Property<T, String> {

    public static class Builder<T extends Enum> extends Property.Builder<T, String, Builder<T>, EnumStringProperty<T>> {
        protected Builder(String name, Class<T> complexType, Class<String> primitiveType) {
            super(name, complexType, primitiveType);
        }

        @Override
        public EnumStringProperty<T> build() {
            return new EnumStringProperty<>(
                    name,
                    complexType,
                    comment,
                    accessor
            );
        }
    }

    //////////////////////////////////////////

    public EnumStringProperty(String name, Class<T> type, String comment, Accessor<T> accessor) {
        super(name, type, String.class, comment, accessor);
    }

    @Override
    public String valueToPrimitive(T value) {
        if (value == null)
            return null;
        return value.toString();
    }

    @Override
    public T valueFromPrimitive(String primitive) {
        if (primitive == null || primitive.equals("null") || primitive.isEmpty())
            return null;
        try {
            return (T) Enum.valueOf(type, primitive.toUpperCase());
        } catch (IllegalArgumentException e) {
            return failLoad("No enum value for " + type + " by name '" + primitive + "'");
        }
    }

}
