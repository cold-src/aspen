package net.orbyfied.aspen.raw.nodes;

import net.orbyfied.aspen.raw.exception.RawExceptions;
import net.orbyfied.aspen.raw.source.NodeSource;
import net.orbyfied.aspen.util.Throwables;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A node in configuration.
 */
@RawNodeTypeDesc(typeName = "node")
public abstract class RawNode {

    public record NodeTypeData(Class<?> klass, String typeName) { }
    static final Map<Class<?>, NodeTypeData> DATA_MAP = new HashMap<>();

    public static NodeTypeData getTypeData(Class<?> type) {
        return DATA_MAP.get(type);
    }

    public static void registerTypeData(Class<?> type, NodeTypeData data) {
        DATA_MAP.put(type, data);
    }

    public static NodeTypeData compileTypeData(Class<?> type) {
        try {
            NodeTypeData data = null;

            try {
                Field f = type.getDeclaredField("NODE_TYPE_DATA");
                f.setAccessible(true);
                data = (NodeTypeData) f.get(null);
            } catch (NoSuchFieldException ignored) { }

            if (type.isAnnotationPresent(RawNodeTypeDesc.class)) {
                RawNodeTypeDesc desc = type.getAnnotation(RawNodeTypeDesc.class);

                data = new NodeTypeData(type,
                        desc.typeName()
                );
            }

            // check if we have data
            if (data == null)
                throw new IllegalStateException("Node type " + type + " does not specify NodeTypeData in any way (static final NODE_TYPE_DATA or annotations)");

            registerTypeData(type, data);
            return data;
        } catch (Throwable t) {
            Throwables.sneakyThrow(t);
            return null;
        }
    }

    public static NodeTypeData getOrCompileTypeData(Class<?> type) {
        NodeTypeData data = getTypeData(type);
        if (data == null)
            data = compileTypeData(type);
        return data;
    }

    /////////////////////////////////////////////////

    // the source of this node
    NodeSource source;

    public RawNode source(NodeSource source) {
        this.source = source;
        return this;
    }

    public NodeSource source() {
        return source;
    }

    @SuppressWarnings("unchecked")
    public <N extends RawNode> N expect(Class<N> nClass) {
        if (!nClass.isInstance(this)) {
            RawExceptions.failUnexpectedNode("expected " + getOrCompileTypeData(nClass).typeName() + ", got " +
                    getTypeName());
        }

        return (N) this;
    }

    /**
     * Get the type name of this node.
     *
     * @return The name.
     */
    public String getTypeName() {
        return getOrCompileTypeData(getClass()).typeName();
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
