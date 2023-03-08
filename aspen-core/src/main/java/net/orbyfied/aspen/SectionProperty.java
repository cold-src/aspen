package net.orbyfied.aspen;

import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.exception.PropertyExceptions;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.raw.nodes.RawObjectNode;
import net.orbyfied.aspen.raw.nodes.RawScalarNode;
import net.orbyfied.aspen.util.Throwables;

import java.lang.reflect.Constructor;

/**
 * Special type of property denoting
 * a section.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SectionProperty extends Property<Schema, Void> {

    public static Builder<Schema, Void, SectionProperty> virtual(
            ConfigurationProvider provider,
            Schema parent,
            String name
    ) {
        return new Builder<>(name, Schema.class, Void.class, () ->
                new SectionProperty(new SectionSchema(parent, name, null)))
                .provider(provider);
    }

    public static Builder<Schema, Void, SectionProperty> builder(
            ConfigurationProvider provider,
            Schema parent,
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
                schema = new SectionSchema(parent, name, sectionInstance);
                schema.compose(provider);
            } catch (Exception e) {
                Throwables.sneakyThrow(e);
                return null;
            }

            return new SectionProperty(
                    schema
            );
        }).provider(provider);
    }

    ///////////////////////////////////////////

    protected SectionProperty(SectionSchema schema) {
        accessor = Accessor.special(schema);
    }

    @Override
    protected Schema loadValue0(PropertyContext context, RawNode node) {
        node.expect(RawObjectNode.class);

        Schema schema = get(context);
        schema.load(context, node);
        return schema;
    }

    @Override
    protected RawNode emitValue0(PropertyContext context, Schema value) {
        Schema schema = get(context);
        return schema.emit(context);
    }

}
