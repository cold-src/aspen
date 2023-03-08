package net.orbyfied.aspen.raw.exception;

import net.orbyfied.aspen.exception.AspenException;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.raw.util.RawFormatting;

public class RawNodeException extends AspenException {

    // the raw node on which the error occurred
    final RawNode node;

    public RawNodeException(RawNode node) {
        this("<cause>", node);
    }

    public RawNodeException(String message, RawNode node) {
        super(message);
        this.node = node;
    }

    public RawNodeException(String message, Throwable cause, RawNode node) {
        super(message, cause);
        this.node = node;
    }

    public RawNodeException(Throwable cause, RawNode node) {
        super(cause);
        this.node = node;
    }

    public RawNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, RawNode node) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.node = node;
    }

    public RawNode getNode() {
        return node;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        String msg = super.getMessage();
        if ("<cause>".equals(msg) && getCause() != null) {
            Throwable cause = getCause();
            builder.append(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }

        builder.append("\n  ");
        RawFormatting.formatNodeSource(builder, node);
        return builder.toString();
    }

}
