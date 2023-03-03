package net.orbyfied.aspen.raw.format;

import java.util.Objects;

/**
 * Java-like Literal Scalar Standard (JLSS)
 * scalar formatting standard.
 *
 * Standard:
 *   Style(Plain):
 *   - "null"        = null
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

    public StringScalarRepresentation dumpScalar(Object in) {
        System.out.println("JLSS dumpScalar in(" + in + ") in.class(" +
                (in == null ? null : in.getClass()) + ")");
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

    public Object parseScalar(StringScalarRepresentation repr) {
        String string = repr.string();
        ScalarStyle style = repr.style();

        if (string == null)
            return null;

        switch (style) {
            case PLAIN -> {
                // switch literals
                return switch (string) {
                    case "null"  -> null;
                    case "true"  -> true;
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
            }

            case SINGLE_QUOTED, DOUBLE_QUOTED -> {
                return string;
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public Object load(StringScalarRepresentation representation) {
        return parseScalar(representation);
    }

    @Override
    public StringScalarRepresentation dump(Object value) {
        return dumpScalar(value);
    }

}
