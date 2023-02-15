package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Defaults;
import net.orbyfied.aspen.annotation.Docs;
import net.orbyfied.aspen.properties.EnumStringProperty;
import net.orbyfied.aspen.properties.ListArrayProperty;
import net.orbyfied.aspen.util.Throwables;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Main manager/service provider for the
 * configuration system.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConfigurationProvider {

    // the registered property behaviours
    private final Map<Class<?>, PropertyBehaviour> propertyBehaviourMap = new HashMap<>();

    // the registered profiles
    private final Map<String, OptionProfile> profileMap = new HashMap<>();

    /**
     * Get a registered option profile by
     * name.
     *
     * @param name The name.
     * @return The profile.
     */
    public OptionProfile getProfile(String name) {
        return profileMap.get(name);
    }

    /**
     * Create and register a new
     * option profile.
     *
     * @param name The name.
     * @param instance The instance.
     * @param defaults The path to the default resource.
     *                 If null, no default file will be loaded.
     * @param path The path.
     * @return The profile.
     * @throws RuntimeException If it fails.
     */
    public OptionProfile newProfile(String name,
                                    Object instance,
                                    String defaults,
                                    Path path) {
        try {
            OptionProfile profile = new OptionProfile(this, name, defaults, path, instance);
            profileMap.put(profile.name(), profile);
            return profile;
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    /**
     * Attempts to parse properties from
     * the given instance and then
     * {@link #newProfile(String, Object, String, Path)}.
     *
     * @return The profile.
     */
    public OptionProfile parseProfile(String name,
                                      Object instance,
                                      Path path) {
        try {
            Class<?> klass = instance.getClass();

            String defaults = null;
            if (klass.isAnnotationPresent(Defaults.class)) {
                defaults = klass.getAnnotation(Defaults.class).resource();
            }

            OptionProfile profile =
                    newProfile(name, instance, defaults, path);

            if (klass.isAnnotationPresent(Docs.class)) {
                profile.schema().setComment(klass.getAnnotation(Docs.class).value());
            }

            return profile;
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    /**
     * Get property behaviour by complex type.
     */
    public <T, P> PropertyBehaviour<T, P> getPropertyBehaviour(Class<T> tClass) {
        return propertyBehaviourMap.get(tClass);
    }

    /**
     * Register property behaviour.
     */
    public ConfigurationProvider withPropertyBehaviour(PropertyBehaviour propertyType) {
        propertyBehaviourMap.put(propertyType.complexClass(), propertyType);
        return this;
    }

    /**
     * Creates a new typed property with the
     * appropriate behaviour associated.
     *
     * @param builder The builder.
     * @return The special property or the default if no special behaviour could be found.
     */
    public <T, P> Property<T, P> buildTypedProperty(Property.Builder builder) {
        PropertyBehaviour<T, P> behaviour = getPropertyBehaviour(builder.complexType);
        if (behaviour == null) {
            // check for special cases
            if (builder instanceof Property.TypedBuilder typedBuilder) {
                Class<T> complexType = builder.complexType;
                if (complexType.isEnum()) {
                    Class<Enum> enumClass = (Class<Enum>) complexType;
                    return (Property<T, P>) new EnumStringProperty<Enum>(
                            builder.name,
                            enumClass,
                            builder.comment,
                            builder.accessor
                    );
                }

                if (complexType.isArray()) {
                    Class<Object[]> arrayClass = (Class<Object[]>) complexType;
                    return (Property<T, P>) new ListArrayProperty<Object>(
                            builder.name,
                            arrayClass,
                            builder.comment,
                            builder.accessor
                    );
                }
            }

            // no special behaviour,
            // build the default property
            // from the builder
            return builder.build();
        }

        return new TypedProperty<>(
                behaviour,

                builder.name,
                builder.complexType,
                builder.primitiveType,
                builder.comment,
                builder.accessor
        );
    }

    /**
     * Generic internal property implementation for
     * handling {@link PropertyBehaviour} implemented behaviour.
     */
    static class TypedProperty<T, P> extends Property<T, P> {

        final PropertyBehaviour<T, P> propertyType;

        TypedProperty(PropertyBehaviour<T, P> propertyType,
                      String name, Class<T> type, Class<P> primitiveType, String comment,
                      Accessor<T> accessor) {
            super(name, type, primitiveType, comment, accessor);
            this.propertyType = propertyType;
        }

        @Override
        public P valueToPrimitive(T value) {
            return propertyType.t2pConverter().apply(value);
        }

        @Override
        public T valueFromPrimitive(P primitive) {
            return propertyType.p2tConverter().apply(primitive);
        }

    }

}
