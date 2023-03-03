package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.format.ScalarStyle;
import net.orbyfied.aspen.raw.source.FileInfo;
import net.orbyfied.aspen.raw.source.FileLocation;
import net.orbyfied.aspen.raw.source.NodeSource;
import net.orbyfied.aspen.raw.source.ReadNodeSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * SnakeYAML utility methods.
 */
public class YamlSupport {

    public static NodeSource newNodeSource(Mark a) {
        if (a == null)
            return new ReadNodeSource();
        return new ReadNodeSource()
                .location(new FileLocation(
                        new FileInfo().setName(a.getName()),
                        a.getLine(),
                        a.getColumn()
                ));
    }

    public static DumperOptions.ScalarStyle toYamlScalarStyle(ScalarStyle style) {
        return switch (style) {
            case PLAIN -> DumperOptions.ScalarStyle.PLAIN;
            case SINGLE_QUOTED -> DumperOptions.ScalarStyle.SINGLE_QUOTED;
            case DOUBLE_QUOTED -> DumperOptions.ScalarStyle.DOUBLE_QUOTED;
        };
    }

    public static ScalarStyle fromYamlScalarStyle(DumperOptions.ScalarStyle style) {
        return switch (style) {
            case PLAIN -> ScalarStyle.PLAIN;
            case SINGLE_QUOTED -> ScalarStyle.SINGLE_QUOTED;
            case DOUBLE_QUOTED -> ScalarStyle.DOUBLE_QUOTED;

            default -> throw new IllegalArgumentException("" + style);
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
