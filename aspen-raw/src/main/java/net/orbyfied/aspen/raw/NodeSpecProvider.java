package net.orbyfied.aspen.raw;

import net.orbyfied.aspen.raw.nodes.RawNode;

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
public abstract class NodeSpecProvider<N> implements RawProvider {

    public interface RawTransformer<B, N extends B, R extends RawNode> {
        N fromRaw(NodeSpecProvider<B> provider, R src, N result);
        R toRaw(NodeSpecProvider<B> provider, N src, R result);
    }

    // the registered transformers
    final Map<Class<?>, List<RawTransformer>> transformersByNative = new HashMap<>();
    final Map<Class<?>, List<RawTransformer>> transformersByRaw = new HashMap<>();

    public <N1 extends N, R extends RawNode> void withTransformer(Class<N1> nClass,
                                                                  Class<R> rClass,
                                                                  RawTransformer<N, N1, R> transformer) {
        if (nClass != null)
            transformersByNative.computeIfAbsent(nClass, __ -> new ArrayList<>()).add(transformer);
        if (rClass != null)
            transformersByRaw.computeIfAbsent(rClass, __ -> new ArrayList<>()).add(transformer);
    }

    /* IO */
    protected abstract void writeTree(N node, Writer writer);
    protected abstract N readTree(Reader reader);

    /* Base Converters */
    protected abstract N fromRawBase(RawNode node);
    protected abstract RawNode toRawBase(N node);

    @Override
    public RawNode compose(Reader reader) {
        return toRaw(readTree(reader));
    }

    @Override
    public void write(RawNode node, Writer writer) {
        writeTree(fromRaw(node), writer);
    }

    /**
     * Convert from the given raw node to
     * a native node.
     *
     * @param rawNode The source node.
     * @return The result node.
     */
    public N fromRaw(RawNode rawNode) {
        N result = fromRawBase(rawNode);
        List<RawTransformer> transformers = transformersByRaw.get(rawNode.getClass());
        if (transformers != null) {
            for (RawTransformer transformer : transformers) {
                result = (N) transformer.fromRaw(this, rawNode, result);
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
    public RawNode toRaw(N nativeNode) {
        RawNode result = toRawBase(nativeNode);
        List<RawTransformer> transformers = transformersByNative.get(nativeNode.getClass());
        if (transformers != null) {
            for (RawTransformer transformer : transformers) {
                result = transformer.toRaw(this, nativeNode, result);
            }
        }

        return result;
    }

}
