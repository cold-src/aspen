package net.orbyfied.aspen.properties;

import net.orbyfied.aspen.Accessor;

public class NumberProperty<T extends Number> extends SimpleProperty<T> {

//    public static class Builder<T extends Number> extends Builder

    //////////////////////////////////

    protected NumberProperty(String name, Class<T> type, String comment, Accessor<T> accessor) {
        super(name, type, comment, accessor);
    }

}
