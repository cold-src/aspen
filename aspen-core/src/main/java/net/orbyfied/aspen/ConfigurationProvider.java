package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Defaults;
import net.orbyfied.aspen.annotation.Docs;
import net.orbyfied.aspen.annotation.MinMax;
import net.orbyfied.aspen.components.ValueConstraints;
import net.orbyfied.aspen.context.*;
import net.orbyfied.aspen.properties.NumberProperty;
import net.orbyfied.aspen.properties.SimpleProperty;
import net.orbyfied.aspen.raw.RawProvider;
import net.orbyfied.aspen.raw.format.JLSSFormat;
import net.orbyfied.aspen.raw.format.StringScalarFormat;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.util.Pair;
import net.orbyfied.aspen.util.Throwables;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Main manager/service provider for the
 * configuration system.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ConfigurationProvider {

    static <P extends Property, B extends Property.Builder>
    OptionComposer builtInProcessor(int exactness,
                                    Predicate<OptionComposeContext> predicate,
                                    Predicate<OptionComposeContext> opener,
                                    Consumer<OptionComposeContext> configureFunc) {
        return new OptionComposer() {
            @Override
            public int exactness() {
                return exactness;
            }

            @Override
            public boolean matches(OptionComposeContext context) {
                return predicate.test(context);
            }

            @Override
            public boolean open(OptionComposeContext context) throws Exception {
                return opener.test(context);
            }

            @Override
            public void configure(OptionComposeContext context) throws Exception {
                configureFunc.accept(context);
            }
        };
    }

    static final OptionComposer BASE_OPTION_COMPOSER = new OptionComposer() {
        @Override
        public int exactness() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean matches(OptionComposeContext context) {
            return true;
        }

        @Override
        public boolean open(OptionComposeContext context) throws Exception {
            context.builder(SimpleProperty.builder(context.name(), context.type()));
            return true;
        }

        @Override
        public void configure(OptionComposeContext context) throws Exception {

        }
    };

    static final SchemaComposer BASE_SCHEMA_COMPOSER = new SchemaComposer() {

        @Override
        public int exactness() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean matches(ComposeContext context) {
            return true;
        }

        @Override
        public void compose(ComposeContext context) throws Throwable {
            Schema.composeBase(context);
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

    // the raw provider to use
    private RawProvider rawProvider = null;

    // the registered property behaviours
    private Map<Class<?>, PropertyBehaviour> propertyBehaviourMap = new HashMap<>();

    // the registered profiles
    private final Map<String, OptionProfile> profileMap = new HashMap<>();

    // the registered option composers
    private List<OptionComposer> optionComposers = new ArrayList<>();

    // the registered schema composers
    private List<SchemaComposer> schemaComposers = new ArrayList<>();

    // the raw processors
    private List<NodeTransformer> rawTransformers = new ArrayList<>();

    {
        /* default option processors */
        withOptionComposer(OptionComposer.composeAllOfType(Number.class, context -> {
            context.builder(NumberProperty.builder(context.name(), context.type()));
            return true;
        }, context -> {
            context.processIfPresent(MinMax.class, minMax -> {
                context.builder().with(ValueConstraints.minMax(minMax.min(), minMax.max()));
            });
        }));
    }

    /* Settings */

    boolean settingProcessAnnotations = true;

    // TODO: settings system
    //  for now we just use fields
    //  but for more customization maybe in
    //  the future allow Property-based custom settings

    private Field findSettingField(String name) {
        try {
            String s = Character.toLowerCase(name.charAt(0)) +
                    name.substring(1);

            Field f = getClass().getDeclaredField("setting" + s);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No setting by name '" + name + "' in impl " +
                    getClass().getSimpleName());
        }
    }

    /*
        Current Settings:

     */

    /**
     * Put a settings value by name, will
     * throw an error if the settings is absent.
     *
     * @param name The settings name.
     * @param value The value to set.
     * @throws IllegalArgumentException If there is no setting by the given name.
     * @return This.
     */
    public ConfigurationProvider setting(String name, Object value) {
        try {
            findSettingField(name).set(this, value);
            return this;
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return this;
        }
    }

    /**
     * Get a settings value by name, will
     * throw an error if the settings is absent.
     *
     * @param name The settings name.
     * @throws IllegalArgumentException If there is no setting by the given name.
     * @return The value.
     */
    public <T> T setting(String name) {
        try {
            return (T) findSettingField(name).get(this);
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    public ConfigurationProvider processAnnotations(boolean b) {
        this.settingProcessAnnotations = b;
        return this;
    }

    public boolean processAnnotations() {
        return settingProcessAnnotations;
    }

    public ConfigurationProvider rawProvider(RawProvider provider) {
        this.rawProvider = provider;
        return this;
    }

    public RawProvider rawProvider() {
        return rawProvider;
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
     * the given context and builds a pipeline.
     *
     * @param context The context.
     * @return The pipeline.
     */
    public OptionComposer findOptionComposerPipeline(OptionComposeContext context) {
        List<OptionComposer> processors = new ArrayList<>();
        for (OptionComposer processor : this.optionComposers) {
            if (processor.matches(
                    context
            )) {
                processors.add(processor);
            }
        }

        return OptionComposer.orderedPipeline(processors);
    }

    /**
     * Finds the correct schema composers for
     * the given context and returns it as an
     * ordered pipeline.
     *
     * @param context The context.
     * @return The pipeline.
     */
    public SchemaComposer findSchemaComposerPipeline(ComposeContext context) {
        List<SchemaComposer> composers = new ArrayList<>();
        for (SchemaComposer composer : this.schemaComposers) {
            if (composer.matches(
                    context
            )) {
                composers.add(composer);
            }
        }

        return SchemaComposer.orderedPipeline(composers);
    }

    public ConfigurationProvider withSchemaComposer(SchemaComposer composer) {
        schemaComposers.add(composer);
        return this;
    }

    public ConfigurationProvider removeSchemaComposer(SchemaComposer composer) {
        schemaComposers.remove(composer);
        return this;
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

    public OptionProfile composeProfile(String name,
                                        Object instance,
                                        Path path) {
        return composeProfile(name, instance, path, null);
    }

    /**
     * Attempts to parse properties from
     * the given instance and then
     * {@link #newProfile(String, Object, String, Path)}.
     *
     * @return The profile.
     */
    public OptionProfile composeProfile(String name,
                                        Object instance,
                                        Path path,
                                        Consumer<OptionProfile> preCompose) {
        try {
            Class<?> klass = instance.getClass();

            String defaults = null;
            if (klass.isAnnotationPresent(Defaults.class)) {
                defaults = klass.getAnnotation(Defaults.class).resource();
            }

            OptionProfile profile =
                    newProfile(name, instance, defaults, path);

            if (klass.isAnnotationPresent(Docs.class)) {
                profile.schema().setComment(klass.getAnnotation(Docs.class).inLine());
            }

            if (preCompose != null)
                preCompose.accept(profile);

            profile.compose();

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
     * Create a new context to execute
     * a schema compose operation.
     *
     * @return The context.
     */
    public ComposeContext newSchemaComposeContext(Schema schema) {
        return new ComposeContext(this, null, schema);
    }

    /**
     * Create a new context to execute
     * an option compose operation.
     *
     * @return The context.
     */
    public OptionComposeContext newOptionComposeContext(Schema schema,
                                                        String name,
                                                        AnnotatedElement element,
                                                        Class<?> type) {
        return new OptionComposeContext(this, null, schema,
                name, type, element);
    }

    /**
     * Create a new context to execute
     * a load operation.
     *
     * @return The context.
     */
    public Context newLoadContext(OptionProfile profile) {
        return new Context(this, new ProfileLoadOperation(profile), profile.schema());
    }

    /**
     * Create a new context to execute
     * a save operation.
     *
     * @return The context.
     */
    public Context newEmitContext(OptionProfile profile) {
        return new Context(this, new ProfileEmitOperation(profile), profile.schema());
    }

    public IOContext newReadContext(OptionProfile profile, String fileName) {
        return new IOContext(this, profile.schema(), fileName);
    }

    public IOContext newWriteContext(OptionProfile profile, String fileName) {
        return new IOContext(this, profile.schema(), fileName);
    }

    /**
     * Fork into a new instance with all
     * settings retained but unlinked.
     *
     * Does not copy over the registered option
     * profiles.
     *
     * Will not deep copy the raw provider, or
     * the values in the collections.
     *
     * @return The new instance.
     */
    public ConfigurationProvider fork() {
        ConfigurationProvider res = new ConfigurationProvider();

        res.rawProvider = rawProvider;
        res.settingProcessAnnotations = settingProcessAnnotations;
        res.schemaComposers = new ArrayList<>(schemaComposers);
        res.optionComposers = new ArrayList<>(optionComposers);
        res.propertyBehaviourMap = new HashMap<>(propertyBehaviourMap);
        res.rawTransformers = new ArrayList<>(rawTransformers);

        return res;
    }

    public ConfigurationProvider withRawProcessor(NodeTransformer transformer) {
        rawTransformers.add(transformer);
        return this;
    }

    public ConfigurationProvider removeRawProcessor(NodeTransformer transformer) {
        rawTransformers.remove(transformer);
        return this;
    }

    /**
     * Pre-processes the input data through
     * the raw transformer pipeline.
     *
     * @param node The input node.
     * @return The processed input node.
     */
    public RawNode preProcessRaw(RawNode node) {
        for (NodeTransformer transformer : rawTransformers) {
            node = transformer.preProcess(node);
        }

        return node;
    }

    /**
     * Post-processes the given output data
     * through the raw transformer pipeline.
     *
     * @param node The output node.
     * @return The processed output node.
     */
    public RawNode postProcessRaw(RawNode node) {
        for (NodeTransformer transformer : rawTransformers) {
            node = transformer.postProcess(node);
        }

        return node;
    }

}
