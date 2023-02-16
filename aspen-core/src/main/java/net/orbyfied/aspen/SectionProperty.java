package net.orbyfied.aspen;

import net.orbyfied.aspen.raw.ValueNode;
import net.orbyfied.aspen.util.Throwables;

import java.lang.reflect.Constructor;

/**
 * Special type of property denoting
 * a section.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SectionProperty extends Property<SectionSchema, Void> {

    public static Builder builder(String name, Class<?> sectionClass) {
        return new Builder(name, sectionClass);
    }

    public static class Builder extends Property.Builder<SectionSchema, Void, Builder, SectionProperty> {
        protected Builder(String name, Class<?> sectionClass) {
            super(name, SectionSchema.class, Void.class);
            this.sectionClass = sectionClass;

            provider = ConfigurationProvider.getGlobal(sectionClass);
        }

        // the section class
        final Class<?> sectionClass;
        Object sectionInstance;

        public Builder instance(Object sectionInstance) {
            this.sectionInstance = sectionInstance;
            return this;
        }

        @Override
        public SectionProperty build() {
            SectionSchema schema;
            try {
                if (sectionInstance == null) {
                    // create instance
                    Constructor constructor = sectionClass.getDeclaredConstructor();
                    sectionInstance = constructor.newInstance();
                }

                // create schema
                schema = new SectionSchema(null, name, sectionInstance);
                schema.compose(provider);
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                return null;
            }

            return new SectionProperty(
                    name,
                    comment,
                    schema
            );
        }
    }

    ///////////////////////////////////////////

    protected SectionProperty(String name, String comment, SectionSchema schema) {
        super(name, SectionSchema.class, Void.class, comment,
                Accessor.special(schema));
    }

    @Override
    public void load(ValueNode node) {
        schema.load(node);
    }

    @Override
    public ValueNode emit() {
        return schema.emit();
    }

}
