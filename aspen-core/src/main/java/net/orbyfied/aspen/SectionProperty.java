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

    public static Builder builder(
            ConfigurationProvider provider,
            String name,
            Class<?> sectionClass,
            final Object instance
    ) {
        return new Builder(name, sectionClass, Void.class, () -> {
            SectionSchema schema;
            Object sectionInstance = instance;
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
                    schema
            );
        });
    }

    ///////////////////////////////////////////

    protected SectionProperty(SectionSchema schema) {
        accessor = Accessor.special(schema);
    }

    @Override
    public void load(ValueNode node) {
        get().load(node);
    }

    @Override
    public ValueNode emit() {
        return get().emit();
    }

}
