package net.orbyfied.aspen;

import net.orbyfied.aspen.annotation.Options;
import net.orbyfied.aspen.raw.MapNode;
import net.orbyfied.aspen.raw.ValueNode;
import net.orbyfied.aspen.util.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Schema describing the properties of
 * an option container.
 */
@SuppressWarnings("rawtypes")
public class OptionSchema extends Schema {

    static final Logger LOGGER = Logger.getLogger("OptionSchema");
    static final Unsafe unsafe = UnsafeUtil.getUnsafe();

    ///////////////////////////////////////

    // the 'source' of this schema,
    // comparable to the parent but it
    // is not actually a level up in the
    // resulting configuration, just as
    // organization
    protected final OptionSchema source;

    // the child option schema's
    protected final List<OptionSchema> providedChildren = new ArrayList<>();

    public OptionSchema(OptionSchema source,
                        Object instance) {
        super(null, "", instance);
        this.source = source;
    }

    /**
     * Attempts to compile/load this schema
     * from the set class.
     *
     * @return This.
     */
    @Override
    public OptionSchema compose(ConfigurationProvider provider) throws Exception {
        super.compose(provider);

        for (Field field : klass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);

            {
                Options desc = field.getAnnotation(Options.class);
                if (desc != null) {
                    // get or create instance
                    Object chInstance = field.get(this.instance);
                    if (chInstance == null) {
                        Constructor<?> constructor = field.getType()
                                .getDeclaredConstructor();
                        constructor.setAccessible(true);
                        chInstance = constructor.newInstance();
                        field.set(this.instance, chInstance);
                    }

                    OptionSchema schema = new OptionSchema(this, chInstance);
                    schema.compose(provider);
                    providedChildren.add(schema);

                    continue;
                }
            }
        }

        return this;
    }

    @Override
    public MapNode emit() {
        MapNode node = new MapNode();
        for (OptionSchema schema : providedChildren) {
            node.addAll(schema.emit());
        }

        return super.emit().merge(node);
    }

    @Override
    public void load(ValueNode node) {
        for (OptionSchema schema : providedChildren) {
            schema.load(node);
        }

        super.load(node);
    }

}
