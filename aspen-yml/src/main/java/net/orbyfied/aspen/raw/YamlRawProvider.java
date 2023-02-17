package net.orbyfied.aspen.raw;

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
import java.util.Map;
import java.util.Objects;

import static net.orbyfied.aspen.raw.YamlSupport.*;

/**
 * An {@link RawProvider} which utilizes SnakeYAML.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class YamlRawProvider implements RawProvider {

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
    public void write(Node node, Writer writer) {
        org.yaml.snakeyaml.nodes.Node yamlNode = toYamlNode(node);

        yaml.serialize(yamlNode, writer);
    }

    @Override
    public Node compose(Reader reader) {
        org.yaml.snakeyaml.nodes.Node yamlNode = yaml.compose(reader);

        Node node = fromYamlNode(yamlNode);
        return node;
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

    org.yaml.snakeyaml.nodes.Node addComments(Node src, org.yaml.snakeyaml.nodes.Node res) {
        if (src.blockComment != null) res.setBlockComments(toCommentLines(src.blockComment, CommentType.BLOCK));
        if (src.inLineComment != null) res.setInLineComments(toCommentLines(src.inLineComment, CommentType.IN_LINE));
        if (src.endComment != null) res.setEndComments(toCommentLines(src.endComment, CommentType.BLOCK));
        return res;
    }

    // convert a raw node to a snakeyaml node
    org.yaml.snakeyaml.nodes.Node toYamlNode(Node node) {
        // mapping node
        if (node instanceof MapNode mapNode) {
            List<NodeTuple> outList = new ArrayList<>();
            for (Map.Entry<String, ValueNode> entry : mapNode.getValue().entrySet()) {
                ScalarNode keyNode = new ScalarNode(Tag.STR, entry.getKey(), null, null, mapKeyStyle);
                org.yaml.snakeyaml.nodes.Node valueNode = toYamlNode(entry.getValue());

                outList.add(new NodeTuple(keyNode, valueNode));
            }

            MappingNode mappingNode = new MappingNode(Tag.MAP, true, outList, null, null, mapFlowStyle);
            return addComments(node, mappingNode);
        }

        // list node
        if (node instanceof ListNode listNode) {
            List<org.yaml.snakeyaml.nodes.Node> values = new ArrayList<>(listNode.getValue().size());
            for (ValueNode node1 : listNode.getValue()) {
                values.add(toYamlNode(node1));
            }

            SequenceNode sequenceNode = new SequenceNode(Tag.SEQ, true, values, null, null, listFlowStyle);
            return addComments(node, sequenceNode);
        }

        // scalar node
        if (node instanceof ValueNode valueNode) {
            Object value = valueNode.getValue();
            if (value == null)
                return addComments(node, new ScalarNode(Tag.NULL, "null", null, null,
                        toScalarStyle(valueNode.style)));

            // represent value
            Tag tag; Class<?> vt = valueNode.value.getClass();
            tag = getScalarTypeTag(vt);

            return addComments(node, new ScalarNode(tag, Objects.toString(value), null, null,
                    toScalarStyle(valueNode.style)));
        }

        throw new IllegalArgumentException("Unsupported node " + node.getClass());
    }

    // converts a snakeyaml node to a raw node
    Node fromYamlNode(org.yaml.snakeyaml.nodes.Node node) {
        // mapping node
        if (node instanceof MappingNode mappingNode) {
            MapNode mapNode = new MapNode();
            for (NodeTuple tuple : mappingNode.getValue()) {
                mapNode.value.put(((ScalarNode)tuple.getKeyNode()).getValue(),
                        (ValueNode) fromYamlNode(tuple.getValueNode()));
            }

            return mapNode;
        }

        // collection node
        if (node instanceof CollectionNode collectionNode) {
            ListNode listNode = new ListNode();
            for (Object item : collectionNode.getValue()) {
                if (item instanceof org.yaml.snakeyaml.nodes.Node node1)
                    listNode.addElement((ValueNode) fromYamlNode(node1));
                else
                    listNode.addElement(new ValueNode(item));
            }

            return listNode;
        }

        // value node
        if (node instanceof ScalarNode scalarNode) {
            if (node.getTag() == Tag.NULL)
                return new ValueNode<>(null);
            String strValue = scalarNode.getValue();

            if (scalarNode.isPlain() && strValue.equalsIgnoreCase("null"))
                return new ValueNode<>(null);

            return new ValueNode<>(yaml.load(strValue));
        }

        // throw exception
        throw new IllegalArgumentException("Unsupported YAML node encountered: " + node.getClass());
    }

}
