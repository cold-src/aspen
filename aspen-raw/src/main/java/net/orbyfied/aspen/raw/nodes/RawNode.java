package net.orbyfied.aspen.raw.nodes;

import net.orbyfied.aspen.raw.source.NodeSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A node in configuration.
 */
public class RawNode {

    // the source of this node
    NodeSource source;

    public RawNode source(NodeSource source) {
        this.source = source;
        return this;
    }

    public NodeSource source() {
        return source;
    }

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

    public RawNode blockCommentLines(List<String> blockComment) {
        this.blockComment = blockComment;
        return this;
    }

    public RawNode blockComment(String blockComment) {
        return blockCommentLines(Arrays.asList(blockComment.split("\n")));
    }

    public List<String> inLineCommentLines() {
        return inLineComment;
    }

    public RawNode inLineCommentLines(List<String> list) {
        this.inLineComment = list;
        return this;
    }

    public RawNode inLineComment(String str) {
        return inLineCommentLines(Arrays.asList(str.split("\n")));
    }

    public List<String> endCommentLines() {
        return endComment;
    }

    public RawNode endCommentLines(List<String> list) {
        this.endComment = list;
        return this;
    }

    public RawNode endComment(String str) {
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
