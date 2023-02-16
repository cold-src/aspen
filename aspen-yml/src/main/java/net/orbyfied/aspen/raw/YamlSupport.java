package net.orbyfied.aspen.raw;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;

public class YamlSupport {

    public static DumperOptions.ScalarStyle toScalarStyle(ValueStyle style) {
        return switch (style) {
            case PLAIN -> DumperOptions.ScalarStyle.PLAIN;
            case SINGLE_QUOTED -> DumperOptions.ScalarStyle.SINGLE_QUOTED;
            case DOUBLE_QUOTED -> DumperOptions.ScalarStyle.DOUBLE_QUOTED;
        };
    }

    public static Tag getScalarTypeTag(Class<?> type) {
        if (true) // better
            return Tag.STR;

        if (type == Integer.class || type == Long.class || type == Byte.class || type == Short.class) return Tag.INT;
        if (type == Double.class || type == Float.class) return Tag.FLOAT;
        if (type == Character.class || type == String.class) return Tag.STR;
        if (type == Boolean.class) return Tag.BOOL;

        // only boxed types should be used
        // anyways
        throw new IllegalArgumentException("Unrepresentable scalar type " + type);
    }

}
