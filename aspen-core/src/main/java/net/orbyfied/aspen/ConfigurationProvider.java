package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Defaults;
import net.orbyfied.aspen.annotation.Docs;
import net.orbyfied.aspen.properties.SimpleProperty;
import net.orbyfied.aspen.raw.RawProvider;
import net.orbyfied.aspen.util.Pair;
import net.orbyfied.aspen.util.Throwables;

import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static net.orbyfied.aspen.OptionComposer.processIfPresent;

/**
 * Main manager/service provider for the
 * configuration system.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConfigurationProvider {

    static <P extends Property, B extends Property.Builder>
    OptionComposer<P, B> builtInProcessor(int exactness,
                                          Predicate<Class<?>> predicate,
                                          BiFunction<String, Class<?>, B> opener,
                                          BiConsumer<B, AnnotatedElement> configureFunc) {
        return new OptionComposer<P, B>() {
            @Override
            public int exactness() {
                return exactness;
            }

            @Override
            public boolean matches(ConfigurationProvider provider, Schema schema, Class<?> type) {
                return predicate.test(type);
            }

            @Override
            public B open(ConfigurationProvider provider, Schema schema, String name, Class<?> type, AnnotatedElement field) throws Exception {
                return opener.apply(name, type);
            }

            @Override
            public void configure(ConfigurationProvider provider, Schema schema, AnnotatedElement field, B builder) throws Exception {
                configureFunc.accept(builder, field);
            }
        };
    }

    static final OptionComposer BASE_OPTION_COMPOSER = new OptionComposer() {
        @Override
        public int exactness() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean matches(ConfigurationProvider provider, Schema schema, Class type) {
            return true;
        }

        @Override
        public Property.Builder open(ConfigurationProvider provider, Schema schema, String name, Class type, AnnotatedElement field) throws Exception {
            return SimpleProperty.builder(name, type);
        }

        @Override
        public void configure(ConfigurationProvider provider, Schema schema, AnnotatedElement field, Property.Builder builder) throws Exception {

        }
    };

    static final SchemaComposer BASE_SCHEMA_COMPOSER = new SchemaComposer() {
        @Override
        public int exactness() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean matches(ConfigurationProvider provider, Schema schema) {
            return true;
        }

        @Override
        public void compose(ConfigurationProvider provider, Schema schema) throws Throwable {
            Schema.composeBase(provider, schema);
        }
    };

    /*
        Global Providers
     */

    static final List<Pair<Predicate<Class<?>>, ConfigurationProvider>> GLOBAL_PROVIDERS =
            new ArrayList<>();
    static final Map<Class<?>, ConfigurationProvider> GLOBAL_PROVIDER_CACHE =
            new HashMap<>();

    /**
     * Create a new global provider for all classes
     * matching the given predicate.
     *
     * @param predicate The predicate.
     * @return The provider.
     */
    public static ConfigurationProvider newGlobal(Predicate<Class<?>> predicate) {
        ConfigurationProvider provider = new ConfigurationProvider();
        GLOBAL_PROVIDERS.add(new Pair<>(predicate, provider));
        return provider;
    }

    /**
     * Creates a new configuration provider for
     * all classes under the given package.
     *
     * @param packageName The package name.
     * @return The provider.
     */
    public static ConfigurationProvider newGlobalUnderPackage(final String packageName) {
        final String fName;
        if (!packageName.endsWith("."))
            fName = packageName + ".";
        else
            fName = packageName;
        return newGlobal(klass -> klass.getName().startsWith(fName));
    }

    /**
     * Get a global configuration provider for the
     * given class.
     *
     * Indexes the cache first.
     *
     * @param klass The class to find for.
     * @return The provider or null if absent.
     */
    public static ConfigurationProvider getGlobal(Class<?> klass) {
        ConfigurationProvider provider = GLOBAL_PROVIDER_CACHE.get(klass);
        if (provider != null) {
            return provider;
        }

        for (Pair<Predicate<Class<?>>, ConfigurationProvider> pair : GLOBAL_PROVIDERS) {
            if (pair.first.test(klass)) {
                provider = pair.second;
                GLOBAL_PROVIDER_CACHE.put(klass, provider);
                return provider;
            }
        }

        return null;
    }

    ///////////////////////////////////

    /* settings */
    boolean processAnnotations = true;

    // the raw provider to use
    private RawProvider rawProvider = null;

    // the registered property behaviours
    private final Map<Class<?>, PropertyBehaviour> propertyBehaviourMap = new HashMap<>();

    // the registered profiles
    private final Map<String, OptionProfile> profileMap = new HashMap<>();

    // the registered option composers
    private final List<OptionComposer> optionComposers = new ArrayList<>();

    // the schema composers
    private final List<SchemaComposer> schemaComposers = new ArrayList<>();

    {
        /* default option processors */

    }

    /* Settings */

    public ConfigurationProvider rawProvider(RawProvider provider) {
        this.rawProvider = provider;
        return this;
    }

    public RawProvider rawProvider() {
        return rawProvider;
    }

    public ConfigurationProvider processAnnotations(boolean b) {
        this.processAnnotations = b;
        return this;
    }

    public boolean processAnnotations() {
        return processAnnotations;
    }

    /**
     * Register a new option processor to this
     * configuration provider.
     *
     * @param processor The option processor.
     * @return This.
     */
    public ConfigurationProvider withOptionComposer(OptionComposer processor) {
        optionComposers.add(processor);
        return this;
    }

    public ConfigurationProvider removeOptionComposer(OptionComposer composer) {
        optionComposers.remove(composer);
        return this;
    }

    /**
     * Finds the correct option composers for
     * the given type and builds a pipeline.
     *
     * @param schema The schema for context.
     * @param type The type.
     * @return The pipeline.
     */
    public OptionComposer findOptionComposerPipeline(Schema schema, Class<?> type) {
        List<OptionComposer> processors = new ArrayList<>();
        for (OptionComposer processor : this.optionComposers) {
            if (processor.matches(
                    this, schema,
                    type
            )) {
                processors.add(processor);
            }
        }

        return OptionComposer.orderedPipeline(processors);
    }

    /**
     * Finds the correct schema composers for
     * the given schema and returns it as an
     * ordered pipeline.
     *
     * @param schema The schema.
     * @return The pipeline.
     */
    public SchemaComposer findSchemaComposerPipeline(Schema schema) {
        List<SchemaComposer> composers = new ArrayList<>();
        for (SchemaComposer composer : this.schemaComposers) {
            if (composer.matches(
                    this, schema
            )) {
                composers.add(composer);
            }
        }

        return SchemaComposer.orderedPipeline(composers);
    }

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

}
