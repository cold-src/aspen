package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.annotation.Section;
import net.orbyfied.aspen.exception.SchemaComposeException;
import net.orbyfied.aspen.raw.MapNode;
import net.orbyfied.aspen.raw.ValueNode;

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
public abstract class Schema implements NodeLike {

    public static Class<?> checkPropertyType(Class<?> ty) {
        if (ty.isPrimitive())
            throw new IllegalArgumentException("can not have property of type " + ty + ", class is primitive");
        return ty;
    }

    // base composer function
    // for all schema's
    static void composeBase(ConfigurationProvider provider,
                            Schema schema) throws Throwable {
        schema.composeBase(provider);
    }

    ///////////////////////////////////////////

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

    public Schema(Schema parent, String name, Object instance) {
        this.parent   = parent;
        this.name     = name;
        this.instance = instance;
        this.klass    = instance.getClass();
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

    @Override
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

    public Schema getSectionFlat(String name) {
        Property property = propertyMap.get(name);
        if (property == null || property.getClass() != SectionProperty.class)
            return null;
        return ((SectionProperty) property).get();
    }

    /**
     * Get a child section recursively
     * through a path.
     *
     * @param path The path to traverse.
     * @return The section or null if absent.
     */
    public Schema getSection(String path) {
        if (path.isEmpty())
            return null;
        Schema current = this;
        int i = 0;
        if (path.charAt(0) == '/') {
            current = getRoot();
            i++;
        }

        for (; i < path.length(); i++) {
            StringBuilder segment = new StringBuilder();
            for (; i < path.length() && path.charAt(i) != '/'; i++) {
                segment.append(path.charAt(i));
            }
            current = current.getSectionFlat(segment.toString());
        }

        return current;
    }

    // base composer for the schema's
    // an instance method because i just
    // copied over the initial code lol
    protected void composeBase(ConfigurationProvider provider) throws Exception {
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

                    // create property
                    OptionComposer processor = provider.findOptionComposerPipeline(this, type);
                    Property.Builder builder = processor.open(
                            provider, this, name, type,
                            field
                    );
                    builder.accessor(Accessor.foreignField(this, field));
                    processor.configure(provider, this, field, builder);
                    property = builder.build();

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
                        SectionProperty.builder(name, instance.getClass())
                                .provider(provider)
                                .instance(instance)
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
            provider
                    .findSchemaComposerPipeline(this)
                    .compose(provider, this);
        } catch (Throwable t) {
            if (t instanceof SchemaComposeException schemaComposeException)
                throw schemaComposeException;
            throw new SchemaComposeException("Uncaught Error", t);
        }

        return this;
    }

    /*
        Raw
     */

    @Override
    public MapNode emit() {
        MapNode node = new MapNode();
        for (Property property : propertyMap.values()) {
            node.putEntry(property.name, property.emit());
        }

        return node;
    }

    @Override
    public void load(ValueNode node) {
        if (!(node instanceof MapNode mapNode))
            throw new IllegalStateException("Not a map node");
        for (Map.Entry<String, ValueNode> entry : mapNode.getValue().entrySet()) {
            Property property = getProperty(entry.getKey());
            if (property == null)
                continue;

            property.load(entry.getValue());
        }
    }

}
