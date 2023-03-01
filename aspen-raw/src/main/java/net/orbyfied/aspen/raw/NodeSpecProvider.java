package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.raw.source.EmissionNodeSource;
import net.orbyfied.aspen.raw.source.ReadNodeSource;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A raw provider based on another node tree
 * specification.
 *
 * @param <N> The base node type.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class NodeSpecProvider<IC extends RawIOContext, N> implements RawProvider<IC> {

    public interface RawTransformer<IC extends RawIOContext,B, N extends B, R extends RawNode> {
        N fromRaw(IC context, NodeSpecProvider<IC, B> provider, R src, N result);
        R toRaw(IC context, NodeSpecProvider<IC, B> provider, N src, R result);
    }

    // the registered transformers
    final Map<Class<?>, List<RawTransformer>> transformersByNative = new HashMap<>();
    final Map<Class<?>, List<RawTransformer>> transformersByRaw = new HashMap<>();

    public <N1 extends N, R extends RawNode> void withTransformer(Class<N1> nClass,
                                                                  Class<R> rClass,
                                                                  RawTransformer<IC, N, N1, R> transformer) {
        if (nClass != null)
            transformersByNative.computeIfAbsent(nClass, __ -> new ArrayList<>()).add(transformer);
        if (rClass != null)
            transformersByRaw.computeIfAbsent(rClass, __ -> new ArrayList<>()).add(transformer);
    }

    /* IO */
    protected abstract void writeTree(IC context, N node, Writer writer);
    protected abstract N readTree(IC context, Reader reader);

    /* Base Converters */
    protected abstract N fromRawBase(IC context, RawNode node);
    protected abstract RawNode toRawBase(IC context, N node);

    @Override
    public RawNode compose(IC context, Reader reader) {
        return toRaw(context, readTree(context, reader));
    }

    @Override
    public void write(IC context, RawNode node, Writer writer) {
        writeTree(context, fromRaw(context, node), writer);
    }

    /**
     * Convert from the given raw node to
     * a native node.
     *
     * @param rawNode The source node.
     * @return The result node.
     */
    public N fromRaw(IC context, RawNode rawNode) {
        N result = fromRawBase(context, rawNode);
        List<RawTransformer> transformers = transformersByRaw.get(rawNode.getClass());
        if (transformers != null) {
            for (RawTransformer transformer : transformers) {
                result = (N) transformer.fromRaw(context, this, rawNode, result);
            }
        }

        return result;
    }

    /**
     * Convert from the given native node
     * to a raw node.
     *
     * @param nativeNode The native node.
     * @return The raw node.
     */
    public RawNode toRaw(IC context, N nativeNode) {
        RawNode result = toRawBase(context, nativeNode);
        List<RawTransformer> transformers = transformersByNative.get(nativeNode.getClass());
        if (transformers != null) {
            for (RawTransformer transformer : transformers) {
                result = transformer.toRaw(context, this, nativeNode, result);
            }
        }

        System.out.println("hey guys! 1 @ " + result);
        if (result.source() instanceof ReadNodeSource source) {
            System.out.println("hey guys! 2 @ " + result);
            String fn = context.fileName();
            if (fn != null) {
                System.out.println("hey guys! 3 @ " + result);
                source.location().file().setName(context.fileName());
            }
        }

        return result;
    }

}
