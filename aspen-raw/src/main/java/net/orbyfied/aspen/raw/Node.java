package net.orbyfied.aspen.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A node in configuration.
 */
public class Node {

    /*
        Comments
     */

    // the comment lines
    List<String> blockComment = new ArrayList<>();
    List<String> inLineComment = new ArrayList<>();
    List<String> endComment = new ArrayList<>();

    public List<String> blockCommentLines() {
        return blockComment;
    }

    public Node blockCommentLines(List<String> blockComment) {
        this.blockComment = blockComment;
        return this;
    }

    public Node blockComment(String blockComment) {
        return blockCommentLines(Arrays.asList(blockComment.split("\n")));
    }

    public List<String> inLineCommentLines() {
        return inLineComment;
    }

    public Node inLineCommentLines(List<String> list) {
        this.inLineComment = list;
        return this;
    }

    public Node inLineComment(String str) {
        return inLineCommentLines(Arrays.asList(str.split("\n")));
    }

    public List<String> endCommentLines() {
        return endComment;
    }

    public Node endCommentLines(List<String> list) {
        this.endComment = list;
        return this;
    }

    public Node endComment(String str) {
        return endCommentLines(Arrays.asList(str.split("\n")));
    }

    public String getDataString() {
        return "";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getDataString() + ")";
    }

}
