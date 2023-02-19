package net.orbyfied.aspen.exception;

import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.util.IOFormatting;

/**
 * An exception which occurs when loading a property.
 */
@SuppressWarnings("rawtypes")
public class PropertyLoadException extends AspenException {

    // the property
    final Property property;

    // the node
    final RawNode node;

    public PropertyLoadException(Property property, RawNode node) {
        this.property = property;
        this.node = node;
    }

    public PropertyLoadException(Property property, String message, RawNode node) {
        super(message);
        this.property = property;
        this.node = node;
    }

    public PropertyLoadException(String message, Throwable cause, Property property, RawNode node) {
        super(message, cause);
        this.property = property;
        this.node = node;
    }

    public PropertyLoadException(Throwable cause, Property property, RawNode node) {
        super(cause);
        this.property = property;
        this.node = node;
    }

    public PropertyLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Property property, RawNode node) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.property = property;
        this.node = node;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        String msg = super.getMessage();
        if ("<cause>".equals(msg) && getCause() != null) {
            Throwable cause = getCause();
            builder.append(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }

        if (property != null)
            builder.append("\n  in property(" + property.getPath() + ")");
        builder.append("\n  ");
        IOFormatting.formatNodeSource(builder, node);
        return builder.toString();
    }

}
