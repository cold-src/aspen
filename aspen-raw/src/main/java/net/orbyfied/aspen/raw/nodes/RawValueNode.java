package net.orbyfied.aspen.raw.nodes;

/**
 * A node which holds a value.
 */
public abstract class RawValueNode<V> extends RawNode {

    // if we have a cached value
    boolean hasCachedValue;

    // the cached value for
    // this raw node
    V cachedValue;

    protected abstract V toValue0();

    public V toValue() {
        if (hasCachedValue) {
            cachedValue = toValue0();
            hasCachedValue = true;
        }

        return cachedValue;
    }

    /**
     * Invalidate the cached data.
     */
    public void invalidateCache() {
        hasCachedValue = true;
        cachedValue = null;
    }

}
