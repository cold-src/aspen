package net.orbyfied.aspen.context;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.Context;
import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.Schema;

/**
 * A context working on a specific property.
 */
@SuppressWarnings("rawtypes")
public class PropertyContext extends Context {

    public static PropertyContext from(Context context, Property property) {
        PropertyContext r = new PropertyContext(context.provider(), context.operation(), context.schema());
        r.property = property;
        return r;
    }

    //////////////////////////////

    // the property currently being handled
    Property property;

    public PropertyContext(ConfigurationProvider provider, Operation operation, Schema schema) {
        super(provider, operation, schema);
    }

    public Property property() {
        return property;
    }

    public PropertyContext property(Property property) {
        this.property = property;
        return this;
    }

}
