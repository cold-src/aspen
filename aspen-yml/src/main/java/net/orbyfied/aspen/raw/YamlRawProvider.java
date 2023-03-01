package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.nodes.*;
import net.orbyfied.aspen.raw.source.NodeSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.orbyfied.aspen.raw.YamlSupport.*;

/**
 * An {@link NodeSpecProvider} which utilizes SnakeYAML.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class YamlRawProvider extends NodeSpecProvider<RawIOContext, Node> {

    public static Builder builder() {
        return new Builder();
    }

    /** Builder for the provider. */
    public static class Builder {

        /* YAML properties */
        BaseConstructor constructor;
        Representer representer;
        LoaderOptions loaderOptions = new LoaderOptions();
        DumperOptions dumperOptions = new DumperOptions();
        DumperOptions.ScalarStyle mapKeyStyle;
        DumperOptions.FlowStyle mapFlowStyle;
        DumperOptions.FlowStyle listFlowStyle;
        boolean spacedComments;

        {
            loaderOptions.setProcessComments(true);
            dumperOptions.setProcessComments(true);
        }

        public BaseConstructor getConstructor() {
            return constructor;
        }

        public Builder setConstructor(BaseConstructor constructor) {
            this.constructor = constructor;
            return this;
        }

        public Representer getRepresenter() {
            return representer;
        }

        public Builder setRepresenter(Representer representer) {
            this.representer = representer;
            return this;
        }

        public LoaderOptions getLoaderOptions() {
            return loaderOptions;
        }

        public Builder setLoaderOptions(LoaderOptions loaderOptions) {
            this.loaderOptions = loaderOptions;
            return this;
        }

        public DumperOptions getDumperOptions() {
            return dumperOptions;
        }

        public Builder setDumperOptions(DumperOptions dumperOptions) {
            this.dumperOptions = dumperOptions;
            return this;
        }

        public DumperOptions.ScalarStyle getMapKeyStyle() {
            return mapKeyStyle;
        }

        public Builder setMapKeyStyle(DumperOptions.ScalarStyle mapKeyStyle) {
            this.mapKeyStyle = mapKeyStyle;
            return this;
        }

        public DumperOptions.FlowStyle getMapFlowStyle() {
            return mapFlowStyle;
        }

        public Builder setMapFlowStyle(DumperOptions.FlowStyle mapFlowStyle) {
            this.mapFlowStyle = mapFlowStyle;
            return this;
        }

        public DumperOptions.FlowStyle getListFlowStyle() {
            return listFlowStyle;
        }

        public Builder setListFlowStyle(DumperOptions.FlowStyle listFlowStyle) {
            this.listFlowStyle = listFlowStyle;
            return this;
        }

        public boolean isSpacedComments() {
            return spacedComments;
        }

        public Builder setSpacedComments(boolean spacedComments) {
            this.spacedComments = spacedComments;
            return this;
        }

        public YamlRawProvider build() {
            if (constructor == null)
                constructor = new Constructor(loaderOptions);
            if (representer == null)
                representer = new Representer(dumperOptions);
            return new YamlRawProvider(
                    new Yaml(constructor, representer, dumperOptions, loaderOptions),
                    mapKeyStyle,
                    mapFlowStyle,
                    listFlowStyle,
                    spacedComments
            );
        }

    }

    ///////////////////////////////

    // the yaml instance
    final Yaml yaml;

    /* settings */
    DumperOptions.ScalarStyle mapKeyStyle;
    DumperOptions.FlowStyle mapFlowStyle;
    DumperOptions.FlowStyle listFlowStyle;
    boolean spacedComments;

    YamlRawProvider(Yaml yaml,
                    DumperOptions.ScalarStyle mapKeyStyle,
                    DumperOptions.FlowStyle mapFlowStyle,
                    DumperOptions.FlowStyle listFlowStyle,
                    boolean spacedComments) {
        this.yaml = yaml;

        this.mapKeyStyle = mapKeyStyle;
        this.mapFlowStyle = mapFlowStyle;
        this.listFlowStyle = listFlowStyle;
        this.spacedComments = spacedComments;
    }

    @Override
    protected void writeTree(RawIOContext context, Node node, Writer writer) {
        yaml.serialize(node, writer);
    }

    @Override
    protected Node readTree(RawIOContext context, Reader reader) {
        return yaml.compose(reader);
    }

    /*
        Node Tree Conversions
     */

    List<CommentLine> toCommentLines(List<String> lines, CommentType type) {
        List<CommentLine> out = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.length() == 0)
                out.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
            out.add(new CommentLine(null, null, (spacedComments ? " " : "") + line, type));
        }
        return out;
    }

    org.yaml.snakeyaml.nodes.Node addComments(RawNode src, org.yaml.snakeyaml.nodes.Node res) {
        if (src.blockCommentLines() != null) res.setBlockComments(toCommentLines(src.blockCommentLines(), CommentType.BLOCK));
        if (src.inLineCommentLines() != null) res.setInLineComments(toCommentLines(src.inLineCommentLines(), CommentType.IN_LINE));
        if (src.endCommentLines() != null) res.setEndComments(toCommentLines(src.endCommentLines(), CommentType.BLOCK));
        return res;
    }

    // convert a raw node to a snakeyaml node
    @Override
    protected org.yaml.snakeyaml.nodes.Node fromRawBase(RawIOContext context, RawNode rawNode) {
        // mapping node
        if (rawNode instanceof RawObjectNode mapNode) {
            List<NodeTuple> outList = new ArrayList<>();
            for (RawNode node1 : mapNode.getNodes()) {
                // check for pair
                if (node1 instanceof RawPairNode pairNode) {
                    ScalarNode keyNode = new ScalarNode(Tag.STR, ((RawScalarNode<String>)pairNode.getKey()).getValue(), null, null, mapKeyStyle);
                    org.yaml.snakeyaml.nodes.Node valueNode = fromRaw(context, pairNode.getValue());

                    outList.add(new NodeTuple(keyNode, valueNode));
                }
            }

            MappingNode mappingNode = new MappingNode(Tag.MAP, true, outList, null, null, mapFlowStyle);
            return addComments(rawNode, mappingNode);
        }

        // list node
        if (rawNode instanceof RawListNode listNode) {
            List<org.yaml.snakeyaml.nodes.Node> values = new ArrayList<>(listNode.getNodes().size());
            for (RawNode node1 : listNode.getNodes()) {
                values.add(fromRaw(context, node1));
            }

            SequenceNode sequenceNode = new SequenceNode(Tag.SEQ, true, values, null, null, listFlowStyle);
            return addComments(rawNode, sequenceNode);
        }

        // scalar node
        if (rawNode instanceof RawScalarNode valueNode) {
            Object value = valueNode.getValue();
            if (value == null)
                return addComments(rawNode, new ScalarNode(Tag.NULL, "null", null, null,
                        toScalarStyle(valueNode.getStyle())));

            // represent value
            Tag tag; Class<?> vt = valueNode.getValue().getClass();
            tag = getScalarTypeTag(vt);

            return addComments(rawNode, new ScalarNode(tag, Objects.toString(value), null, null,
                    toScalarStyle(valueNode.getStyle())));
        }

        throw new IllegalArgumentException("Unsupported node " + rawNode.getClass());
    }

    // converts a snakeyaml node to a raw node
    @Override
    protected RawNode toRawBase(RawIOContext context, org.yaml.snakeyaml.nodes.Node yamlNode) {
        // mapping node
        if (yamlNode instanceof MappingNode mappingNode) {
            RawObjectNode mapNode = new RawObjectNode();
            for (NodeTuple tuple : mappingNode.getValue()) {
                mapNode.getNodes().add(
                        new RawPairNode(
                                toRaw(context, tuple.getKeyNode()),
                                toRaw(context, tuple.getValueNode()))
                );
            }

            return mapNode.source(newNodeSource(mappingNode.getStartMark()));
        }

        // collection node
        if (yamlNode instanceof CollectionNode collectionNode) {
            RawListNode listNode = new RawListNode();
            for (Object item : collectionNode.getValue()) {
                if (item instanceof org.yaml.snakeyaml.nodes.Node node1)
                    listNode.addElement(toRaw(context, node1));
                else
                    listNode.addElement(new RawScalarNode(item));
            }

            return listNode.source(newNodeSource(collectionNode.getStartMark()));
        }

        // value node
        if (yamlNode instanceof ScalarNode scalarNode) {
            NodeSource source = newNodeSource(scalarNode.getStartMark());
            if (yamlNode.getTag() == Tag.NULL)
                return new RawScalarNode<>(null).source(source);
            String strValue = scalarNode.getValue();

            if (scalarNode.isPlain() && strValue.equalsIgnoreCase("null"))
                return new RawScalarNode<>(null).source(source);

            return new RawScalarNode<>(yaml.load(strValue)).source(source);
        }

        // throw exception
        throw new IllegalArgumentException("Unsupported YAML node encountered: " + yamlNode.getClass());
    }

}
