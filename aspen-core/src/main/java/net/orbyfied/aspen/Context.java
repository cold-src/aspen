package net.orbyfied.aspen;

import net.orbyfied.aspen.context.Operation;
import net.orbyfied.aspen.raw.RawContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a context in which properties
 * can be loaded, modified and saves.
 */
public class Context {

    // the configuration provider
    final ConfigurationProvider provider;

    // the operation
    Operation operation;

    // the current schema
    Schema schema;

    // extended key-value storage
    // in the context for extended flexibility
    // lazily created when a value is put
    Map<String, Object> extended;

    public Context(ConfigurationProvider provider,
                   Operation operation,
                   Schema schema) {
        this.provider  = provider;
        this.operation = operation;
        this.schema    = schema;
    }

    public ConfigurationProvider provider() {
        return provider;
    }

    public Schema schema() {
        return schema;
    }

    @Nullable
    public Operation operation() {
        return operation;
    }

    public Context fork() {
        return new Context(provider, operation, schema);
    }

    /*
        Extended Values
     */

    /**
     * Put an extended meta value on this context.
     *
     * @param key The key.
     * @param value The value.
     * @return This.
     */
    public Context put(String key, Object value) {
        if (extended == null)
            extended = new HashMap<>();
        extended.put(key, value);
        return this;
    }

    /**
     * Get an extended meta value on this context.
     *
     * @param key The key.
     * @param <T> The value type.
     * @return The value or null if absent.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (extended == null)
            return null;
        return (T) extended.get(key);
    }

    /**
     * Check if a given extended meta key is
     * present on this context.
     *
     * @param key The key.
     * @return If it is present.
     */
    public boolean has(String key) {
        if (extended == null)
            return false;
        return extended.containsKey(key);
    }

}
