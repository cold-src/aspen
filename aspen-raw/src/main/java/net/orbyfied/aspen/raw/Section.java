package net.orbyfied.aspen.raw;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A section inside a configuration, this can be
 * an object (map) or a list.
 *
 * @param <K> The key type, for example integer (index) for lists.
 */
public interface Section<K> extends Iterable<Object> {

    /**
     * Count the values in this section.
     *
     * @return The amount of values.
     */
    int size();

    /**
     * Get a value from this section.
     *
     * @param key The key.
     * @param <T> The value type.
     * @return The value or null if absent.
     */
    <T> T get(K key);

    /**
     * Set a value by key in this section.
     *
     * @param key The key.
     * @param val The value.
     * @return This or a new section.
     */
    Section<K> set(K key, Object val);

    /**
     * Get a value from this section if present,
     * else use the default value provided.
     *
     * @param key The key.
     * @param def The default value.
     * @param <T> The value type.
     * @return The value or null if absent.
     */
    default <T> T getOr(K key, T def) {
        boolean has = has(key);
        return has ? get(key) : def;
    }

    /**
     * Get a value from this section if present,
     * else get the default value from the supplier.
     *
     * @param key The key.
     * @param supplier The default supplier function.
     * @param <T> The value type.
     * @return The value.
     */
    default <T> T getOrCompute(K key, Function<K, T> supplier) {
        boolean has = has(key);
        return has ? get(key) : supplier.apply(key);
    }

    /**
     * Get a value from this section if present,
     * else get the default value from the supplier.
     *
     * @param key The key.
     * @param supplier The default supplier function.
     * @param <T> The value type.
     * @return The value.
     */
    default <T> T getOrCompute(K key, Supplier<T> supplier) {
        return getOrCompute(key, k -> supplier.get());
    }

    /**
     * Check if this section contains an element
     * by the provided key.
     *
     * @param key The key.
     * @return If it is present.
     */
    boolean has(K key);

    /**
     * Get all keys in the section.
     *
     * @return The collection of keys.
     */
    Collection<K> keys();

    /**
     * Get all values in the section.
     *
     * @return The collection of values.
     */
    Collection<Object> values();

    /**
     * Get an object section.
     *
     * @param key The key.
     * @return The object section or null if absent.
     */
    Section<String> object(K key);

    /**
     * Get a list section.
     *
     * @param key The key.
     * @return The list section or null if absent.
     */
    Section<Integer> list(K key);

    @Override
    default Iterator<Object> iterator() {
        return values().iterator();
    }

}
