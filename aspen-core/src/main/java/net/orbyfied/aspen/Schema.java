package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.annotation.Section;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    ///////////////////////////////////////////

    protected final Object instance;
    protected final Class<?> klass;

    // the name of this schema
    protected final String name;

    // the parent schema
    protected final Schema parent;

    // the properties compiled
    protected final Map<String, Property> propertyMap = new HashMap<>();

    // the child sections
    protected final Map<String, Schema> sections = new HashMap<>();

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

    public Schema withProperty(Property property) {
        propertyMap.put(property.name, property);
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
        return sections.get(name);
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

    /**
     * Compile this schema from the class.
     *
     * @return This.
     */
    public Schema compile(ConfigurationProvider provider) throws Exception {
        for (Field field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;

            Property property; // result property

            // check for explicit property
            if (Property.class.isAssignableFrom(field.getType())) {
                property = (Property) field.get(instance);
                if (property == null) continue;

                withProperty(property);
                continue;
            }

            // check for option
            if (field.isAnnotationPresent(Option.class)) {
                Option optionDesc = field.getAnnotation(Option.class);
                String name;
                if (optionDesc.name().equals("(get)")) {
                    name = field.getName();
                } else {
                    name = optionDesc.name();
                }

                Class<?> type = checkPropertyType(field.getType());

                // create property
                property = provider.buildTypedProperty(
                        Property
                                .simpleBuilder(name, type)
                                .accessor(Accessor.forField(field))
                );

                withProperty(property);

                continue;
            }

            // check for section
            if (field.isAnnotationPresent(Section.class)) {

            }
        }

        return this;
    }

}
