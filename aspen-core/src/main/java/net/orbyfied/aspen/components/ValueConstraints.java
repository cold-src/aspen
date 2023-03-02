package net.orbyfied.aspen.components;

import net.orbyfied.aspen.PropertyComponent;
import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.exception.PropertyExceptions;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ValueConstraints implements PropertyComponent {

    public static ValueConstraints empty() {
        return new ValueConstraints();
    }

    public static <N extends Number> Constraint<N> minMaxConstraint(double min, double max) {
        return new Constraint() {
            @Override
            public boolean check(PropertyContext context, Object value) {
                if (!(value instanceof Number number))
                    return false;
                double asDouble = number.doubleValue();

                return asDouble >= min && asDouble <= max;
            }

            @Override
            public String expectation(PropertyContext context) {
                return "number must be between " + min + " and " + max;
            }
        };
    }

    public static ValueConstraints minMax(double min, double max) {
        return empty().with(minMaxConstraint(min, max));
    }

    public static Constraint<Object> notNullConstraint() {
        return new Constraint<Object>() {
            @Override
            public boolean check(PropertyContext context, Object value) {
                return value != null;
            }

            @Override
            public String expectation(PropertyContext context) {
                return "value can not be null";
            }
        };
    }

    public static ValueConstraints notNull() {
        return empty().with(notNullConstraint());
    }

    ///////////////////////////////////

    /** A constraint for checking values. */
    public interface Constraint<T> {

        /**
         * Check if the given value is allowed in
         * the current property context.
         *
         * @param context The context.
         * @param value The value.
         * @return If the value is allowed.
         */
        boolean check(PropertyContext context, T value);

        /**
         * Get the expectation string for this
         * constraint.
         *
         * @param context The context.
         * @return The string.
         */
        String expectation(PropertyContext context);

    }

    /**
     * All constraints to apply to each value.
     */
    final List<Constraint> constraints = new ArrayList<>();

    public ValueConstraints with(Constraint constraint) {
        constraints.add(constraint);
        return this;
    }

    @Override
    public Object checkLoadedValue(PropertyContext context, Object val) {
        for (Constraint constraint : constraints) {
            if (!constraint.check(context, val)) {
                PropertyExceptions.failIllegalValue(val,
                        constraint.expectation(context));
            }
        }

        return val;
    }
}
