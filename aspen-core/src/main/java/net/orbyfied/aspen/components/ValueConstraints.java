package net.orbyfied.aspen.components;

import net.orbyfied.aspen.PropertyComponent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ValueConstraints implements PropertyComponent {

    public static ValueConstraints minMax(double min, double max) {
        return new ValueConstraints().with(value -> {
            if (!(value instanceof Number number))
                return false;
            double asDouble = number.doubleValue();

            return asDouble >= min && asDouble <= max;
        });
    }

    ///////////////////////////////////

    /** A constraint for checking values. */
    public interface Constraint<T> {
        boolean check(T value);
    }

    List<Constraint> constraints = new ArrayList<>();

    public ValueConstraints with(Constraint constraint) {
        constraints.add(constraint);
        return this;
    }

    @Override
    public Object checkLoadedValue(Object val) {
        for (Constraint constraint : constraints) {
            if (!constraint.check(val)) {
                throw new IllegalArgumentException("Constraint prohibits value " + val);
            }
        }

        return val;
    }
}
