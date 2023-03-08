package net.orbyfied.aspen.raw.format;

import net.orbyfied.aspen.raw.nodes.RawScalarNode;
import net.orbyfied.aspen.raw.nodes.RawUndefinedNode;
import net.orbyfied.aspen.raw.nodes.RawValueNode;

import java.util.Objects;

/**
 * Java-like Literal Scalar Standard (JLSS)
 * scalar formatting standard.
 *
 * Standard:
 *   Style(Plain):
 *   - null          = null
 *   - undefined     = undefined
 *   - { empty }     = undefined
 *   - [0-9]+        = long
 *   - [0-9\\.]+     = double
 *   - (true)(false) = boolean
 *   - *             = string
 *
 *   Style(Single Quoted):
 *   - * = string
 *
 *   Style(Double Quoted):
 *   - * = string
 */
@SuppressWarnings("rawtypes")
public final class JLSSFormat implements StringScalarFormat {

    // the style to use for
    // string dumping
    ScalarStyle stringDumpStyle = ScalarStyle.SINGLE_QUOTED;

    public JLSSFormat stringDumpStyle(ScalarStyle stringDumpStyle) {
        this.stringDumpStyle = stringDumpStyle;
        return this;
    }

    public ScalarStyle stringDumpStyle() {
        return stringDumpStyle;
    }

    @Override
    public RawValueNode<?> load(StringScalarRepresentation repr) {
        String string = repr.string();
        ScalarStyle style = repr.style();

        if (string == null)
            return RawScalarNode.nullNode();
        if (style == ScalarStyle.PLAIN &&
                (string.equals("undefined") || string.isBlank()))
            return RawUndefinedNode.undefined();

        return new RawScalarNode<>(switch (style) {
            case PLAIN -> // switch literals
                    switch (string) {
                        case "null" -> null;
                        case "true" -> true;
                        case "false" -> false;
                        default -> {
                            try {
                                double numResult = Double.parseDouble(string);
                                if (numResult % 1 == 0) // produce long if an integer
                                    yield (long) numResult;
                                else yield numResult;
                            } catch (NumberFormatException ignored) { }

                            // yield string literal
                            yield string;
                        }
                    };

            case SINGLE_QUOTED, DOUBLE_QUOTED -> string;
        });
    }

    @Override
    public StringScalarRepresentation dump(RawValueNode<?> node) {
        if (node instanceof RawUndefinedNode)
            return new StringScalarRepresentation("undefined", ScalarStyle.PLAIN);
        Object in = ((RawScalarNode)node).getValue();
        if (in == null)
            return new StringScalarRepresentation("null", ScalarStyle.PLAIN);
        if (in instanceof Boolean bool)
            return new StringScalarRepresentation("" + bool, ScalarStyle.PLAIN);
        if (in instanceof Number number) {
            double d = number.doubleValue();
            if (d % 1 == 0)
                return new StringScalarRepresentation(Long.toString((long)d), ScalarStyle.PLAIN);
            else
                return new StringScalarRepresentation(Double.toString(d), ScalarStyle.PLAIN);
        }

        return new StringScalarRepresentation(Objects.toString(in), stringDumpStyle);
    }
}
