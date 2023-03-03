package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.format.StringScalarFormat;
import net.orbyfied.aspen.raw.format.StringScalarRepresentation;
import net.orbyfied.aspen.raw.impl.NodeSpecProvider;
import net.orbyfied.aspen.raw.impl.StringScalarProvider;
import net.orbyfied.aspen.raw.nodes.*;
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

import static net.orbyfied.aspen.raw.YamlSupport.*;

/**
 * An {@link NodeSpecProvider} which utilizes SnakeYAML.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class YamlRawProvider
        extends NodeSpecProvider<RawIOContext, Node>
        implements StringScalarProvider<RawIOContext>
{

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
        DumperOptions.ScalarStyle mapKeyStyle = DumperOptions.ScalarStyle.PLAIN;
        DumperOptions.FlowStyle mapFlowStyle = DumperOptions.FlowStyle.AUTO;
        DumperOptions.FlowStyle listFlowStyle = DumperOptions.FlowStyle.AUTO;
        boolean spacedComments = true;
        StringScalarFormat stringScalarFormat;

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

        public StringScalarFormat getStringScalarFormat() {
            return stringScalarFormat;
        }

        public Builder setStringScalarFormat(StringScalarFormat stringScalarFormat) {
            this.stringScalarFormat = stringScalarFormat;
            return this;
        }

        /**
         * Builds the instance.
         */
        public YamlRawProvider build() {
            if (constructor == null)
                constructor = new Constructor(loaderOptions);
            if (representer == null)
                representer = new Representer(dumperOptions);

            YamlRawProvider i = new YamlRawProvider(
                    new Yaml(constructor, representer, dumperOptions, loaderOptions)
            );

            // set properties
            i.mapKeyStyle = mapKeyStyle;
            i.mapFlowStyle = mapFlowStyle;
            i.listFlowStyle = listFlowStyle;
            i.spacedComments = spacedComments;

            return i;
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
    StringScalarFormat stringScalarFormat;

    /*
        TODO: fix bug with saving scalar style
         which causes it to not be applied idk
         why that happens but it does for some
         fucking reason im explicitly setting the
         style on the fucking node stupid snakeyaml
         but no that not enough i probably have to
         extend or configure some stupid serializer
         object or something idk
     */

    YamlRawProvider(Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public StringScalarFormat stringScalarFormat() {
        return stringScalarFormat;
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

    org.yaml.snakeyaml.nodes.Node putProperties(RawNode src, org.yaml.snakeyaml.nodes.Node res) {
        // comments //
        if (src.blockCommentLines() != null) res.setBlockComments(toCommentLines(src.blockCommentLines(), CommentType.BLOCK));
        if (src.inLineCommentLines() != null) res.setInLineComments(toCommentLines(src.inLineCommentLines(), CommentType.IN_LINE));
        if (src.endCommentLines() != null) res.setEndComments(toCommentLines(src.endCommentLines(), CommentType.BLOCK));

        if (res instanceof ScalarNode scalarNode) {
            System.out.println("dumped scalar yaml(" + res + ") style(" + scalarNode.getScalarStyle() + ")");
        }

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
            return putProperties(rawNode, mappingNode);
        }

        // list node
        if (rawNode instanceof RawListNode listNode) {
            List<org.yaml.snakeyaml.nodes.Node> values = new ArrayList<>(listNode.getNodes().size());
            for (RawNode node1 : listNode.getNodes()) {
                values.add(fromRaw(context, node1));
            }

            SequenceNode sequenceNode = new SequenceNode(Tag.SEQ, true, values, null, null, listFlowStyle);
            return putProperties(rawNode, sequenceNode);
        }

        // scalar node
        if (rawNode instanceof RawScalarNode valueNode) {
            StringScalarRepresentation repr = stringScalarFormat()
                    .dump(valueNode);
            return putProperties(rawNode, new ScalarNode(Tag.STR, repr.string(), null, null,
                    toYamlScalarStyle(repr.style())));
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
            StringScalarRepresentation repr = new StringScalarRepresentation(
                    scalarNode.getValue(),
                    fromYamlScalarStyle(scalarNode.getScalarStyle())
            );

            RawScalarNode<?> rawScalarNode = new RawScalarNode<>();
            stringScalarFormat().load(repr, rawScalarNode);
            return rawScalarNode.source(newNodeSource(scalarNode.getStartMark()));
        }

        // throw exception
        throw new IllegalArgumentException("Unsupported YAML node encountered: " + yamlNode.getClass());
    }

}
