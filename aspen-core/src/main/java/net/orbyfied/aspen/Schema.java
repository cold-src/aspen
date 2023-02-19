package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.annotation.Section;
import net.orbyfied.aspen.context.ComposeContext;
import net.orbyfied.aspen.context.OptionComposeContext;
import net.orbyfied.aspen.exception.AspenException;
import net.orbyfied.aspen.exception.SchemaComposeException;
import net.orbyfied.aspen.raw.nodes.RawMapNode;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.raw.nodes.RawPairNode;
import net.orbyfied.aspen.raw.nodes.RawScalarNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A compilable schema holding properties
 * on a schema class.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class Schema implements BaseRepresentable {

    public static Class<?> checkPropertyType(Class<?> ty) {
        if (ty.isPrimitive())
            throw new IllegalArgumentException("can not have property of type " + ty + ", class is primitive");
        return ty;
    }

    // base composer function
    // for all schema's
    static void composeBase(ComposeContext context) throws Throwable {
        context.schema().composeBase(context.provider(), context);
    }

    ///////////////////////////////////////////

    protected final ConfigurationProvider provider;

    protected final Object instance;
    protected final Class<?> klass;

    // the name of this schema
    protected final String name;

    // the parent schema
    protected Schema parent;

    // the properties compiled
    protected final LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<>();

    /**
     * The comment on this schema.
     *
     * In the case of a options schema, so not
     * nesting into another section the aim is to
     * put the comment as a prefix to the group
     * of properties.
     *
     * In the case of a section, the comment will
     * be appended to the start of the definition
     * in code.
     */
    protected String comment;

    public Schema(ConfigurationProvider provider, Schema parent, String name, Object instance) {
        this.provider = provider;
        this.parent   = parent;
        this.name     = name;
        this.instance = instance;
        if (instance != null)
            this.klass = instance.getClass();
        else
            this.klass = null;
    }

    public Class<?> getSchemaClass() {
        return klass;
    }

    /**
     * Get an unmodifiable copy of the property map.
     */
    public Map<String, Property> allProperties() {
        return Collections.unmodifiableMap(propertyMap);
    }

    public Property getProperty(String name) {
        return propertyMap.get(name);
    }

    public Schema withProperty(Property property) {
        propertyMap.put(property.name, property);
        property.schema = this;
        return this;
    }

    public Schema setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Schema getParent() {
        return parent;
    }

    /**
     * Build the path from root to this schema.
     */
    public String getPath() {
        if (parent == null)
            return name;
        return parent.getPath() + "/" + name;
    }

    public Schema getRoot() {
        Schema curr = this;
        while (curr.parent != null)
            curr = curr.parent;
        return curr;
    }

    public Schema getSection(String name) {
        Property property = propertyMap.get(name);
        if (property == null || property.getClass() != SectionProperty.class)
            return null;
        return ((SectionProperty) property).get();
    }

    /**
     * Get a section by name or create a virtual one.
     *
     * A virtual section is a section with a virtual schema.
     * This means it is not backed by an instance or class
     * and is purely temporary and code created.
     *
     * @param name The name.
     * @return The section or null if a property with a different type was present.
     */
    public SectionSchema virtualSection(String name) {
        Property property = propertyMap.get(name);
        if (property != null && property.getClass() != SectionProperty.class)
            return null;
        if (property == null) {
            property = SectionProperty
                    .virtual(provider, this, name)
                    .build();
            withProperty(property);
        }

        return ((SectionProperty) property).get();
    }

    /**
     * Get a child section recursively
     * through a path.
     *
     * @param path The path to traverse.
     * @return The section or null if absent.
     */
    public SectionSchema findSection(String path) {
        if (path.isEmpty())
            return null;
        Schema current = this;
        int i = 0;
        if (path.charAt(0) == '/') {
            current = getRoot();
            i++;
        }

        StringBuilder segment = new StringBuilder();
        for (; i < path.length(); i++) {
            segment.delete(0, segment.length());
            for (; i < path.length() && path.charAt(i) != '/'; i++) {
                segment.append(path.charAt(i));
            }

            current = current.getSection(segment.toString());

            if (current == null)
                return null;
        }

        return (SectionSchema) current;
    }

    /**
     * Get a property recursively
     * through a path.
     *
     * @param path The path to traverse.
     * @return The section or null if absent.
     */
    public Property findProperty(String path) {
        if (path.isEmpty())
            return null;
        Schema current = this;
        int i = 0;
        if (path.charAt(0) == '/') {
            current = getRoot();
            i++;
        }

        StringBuilder segment = new StringBuilder();
        for (; i < path.length(); i++) {
            segment.delete(0, segment.length());
            for (; i < path.length() && path.charAt(i) != '/'; i++) {
                segment.append(path.charAt(i));
            }

            Property property = current.getProperty(segment.toString());
            if (property instanceof SectionProperty sectionProperty) {
                current = sectionProperty.get();
            } else {
                return property;
            }
        }

        return null;
    }

    // base composer for the schema's
    // an instance method because i just
    // copied over the initial code lol
    protected void composeBase(ConfigurationProvider provider,
                               ComposeContext context) throws Exception {
        for (Field field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);

            Property property = null; // result property

            // check for explicit property
            if (Property.class.isAssignableFrom(field.getType())) {
                property = (Property) field.get(instance);
                if (property == null) continue;
                if (property.accessor == null) {
                    property.accessor = Accessor.memoryLocal();
                }

                withProperty(property);
            }

            // check for property access
            Object pa;
            if (PropertyAccess.class.isAssignableFrom(field.getType()) &&
                    (pa = field.get(instance)) != null && pa instanceof PropertyAccess.Future propertyAccess) {
                // get property to access
                Property p = null;
                if (propertyAccess instanceof PropertyAccess.PropertyFuture future) {
                    p = future.property;
                    if (propertyMap.get(p.name) != p) {
                        withProperty(p);
                    }
                } else if (propertyAccess instanceof PropertyAccess.FindFuture future) {
                    p = findProperty(future.path);
                }

                // create access
                propertyAccess.access = PropertyAccess.constant(
                        p,
                        provider,
                        p.schema
                );
            }

            // check for option
            if (provider.processAnnotations()) {
                if (property == null &&
                        field.isAnnotationPresent(Option.class)) {
                    Option optionDesc = field.getAnnotation(Option.class);
                    String name;
                    if (optionDesc.name().equals("(get)")) {
                        name = field.getName();
                    } else {
                        name = optionDesc.name();
                    }

                    Class<?> type = checkPropertyType(field.getType());

                    // check for property accessor
                    if (PropertyAccess.class.isAssignableFrom(type)) {
                        context.schedulePost(__ -> {
                            // get property
                            Property p = getProperty(optionDesc.name());

                            PropertyAccess access = PropertyAccess.constant(
                                    p,
                                    context.provider(), Schema.this
                            );

                            field.set(instance, access);
                        });

                        continue;
                    }

                    // create property
                    OptionComposeContext composeContext = provider.newOptionComposeContext(this, name, field, type);
                    OptionComposer composerPipeline = provider.findOptionComposerPipeline(composeContext);
                    if (/* open context */ !composerPipeline.open(composeContext)) {
                        throw new SchemaComposeException("Failed to open compose of option " + name + " on field " + field);
                    }

                    composeContext.builder() // standard accessor
                            .accessor(Accessor.forField(this, field));
                    composerPipeline.configure(composeContext); // configure context

                    property = composeContext.builder().build();
                    withProperty(property);
                }

                if (property != null) {
                    property.schema = this;
                }

            }

            // check for section
            if (field.isAnnotationPresent(Section.class)) {
                Class<?> klass = field.getType();
                String name = field.getAnnotation(Section.class).name();

                Object instance = field.get(this.instance);
                if (instance == null) {
                    Constructor constructor =
                            klass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    instance = constructor.newInstance();
                    field.set(this.instance, instance);
                }

                withProperty(
                        SectionProperty.builder(provider, this, name, klass, instance)
                        .build()
                );
            }
        }
    }

    /**
     * Compile this schema from the class.
     *
     * @return This.
     */
    public Schema compose(ConfigurationProvider provider) throws Exception {
        try {
            // make and call composer pipeline
            ComposeContext context = provider.newSchemaComposeContext(this);
            provider
                    .findSchemaComposerPipeline(context)
                    .compose(context);
            context.runPost();
        } catch (Throwable t) {
            if (t instanceof AspenException e)
                throw e;
            throw new SchemaComposeException("Uncaught Error", t);
        }

        return this;
    }

    /*
        Raw
     */

    @Override
    public RawMapNode emit(Context context) {
        context.schema = this;
        Context forked = context.fork();
        RawMapNode node = new RawMapNode();
        for (Property property : propertyMap.values()) {
            node.putEntry(property.name, property.emit(forked));
        }

        return node;
    }

    @Override
    public void load(Context context, RawNode node) {
        context.schema = this;
        Context forked = context.fork();
        if (!(node instanceof RawMapNode mapNode))
            throw new IllegalStateException("Not a map node");
        for (RawNode node1 : mapNode.getNodes()) {
            if (node1 instanceof RawPairNode entry) {
                Property property = getProperty(((RawScalarNode<String>)entry.getKey()).getValue());
                if (property == null)
                    continue;

                property.load(forked, entry.getValue());
            }
        }
    }

}
