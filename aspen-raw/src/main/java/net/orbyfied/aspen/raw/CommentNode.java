package net.orbyfied.aspen.raw;

import java.util.ArrayList;
import java.util.List;

/**
 * A node which holds a comment.
 */
public class CommentNode extends Node {

    // the comment lines
    List<String> lines = new ArrayList<>();

    /** Set the lines of the comment. */
    public CommentNode setLines(List<String> lines) {
        this.lines = lines;
        return this;
    }

    /** Get the lines of the comment. */
    public List<String> getLines() {
        return lines;
    }

}
