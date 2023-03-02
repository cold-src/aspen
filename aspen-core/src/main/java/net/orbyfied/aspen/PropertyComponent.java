package net.orbyfied.aspen;

import net.orbyfied.aspen.context.PropertyContext;
import net.orbyfied.aspen.util.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A component which can be added to a property
 * to add extra functionality.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface PropertyComponent<T, P> {

    static Pipeline pipeline() {
        return new Pipeline();
    }

    static Pipeline pipeline(PropertyComponent... components) {
        Pipeline pipeline = pipeline();
        pipeline.list.addAll(Arrays.asList(components));
        return pipeline;
    }

    /**
     * A pipeline of components, to allow multiple
     * components of the same type.
     */
    class Pipeline implements PropertyComponent {
        /* Components */
        final List<PropertyComponent> list = new ArrayList<>();

        public Pipeline with(PropertyComponent component) {
            list.add(component);
            return this;
        }

        public <T extends PropertyComponent> Pipeline with(T component, Placement<T> placement) {
            placement.add((List<T>) list, component);
            return this;
        }

        public <T extends PropertyComponent> Pipeline withAll(List<T> components) {
            list.addAll(components);
            return this;
        }

        /* Overwrite */

        @Override
        public Object checkLoadedValue(PropertyContext context, Object val) {
            for (var comp : list)
                val = comp.checkLoadedValue(context, val);
            return val;
        }
    }

    //////////////////////////////////

    default T checkLoadedValue(PropertyContext context, T val) {
        return val;
    }

}
