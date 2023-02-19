package net.orbyfied.aspen;

import net.orbyfied.aspen.context.Operation;
import net.orbyfied.aspen.raw.RawContext;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a context in which properties
 * can be loaded, modified and saves.
 */
public class Context implements RawContext {

    // the configuration provider
    final ConfigurationProvider provider;

    // the operation
    Operation operation;

    // the current schema
    Schema schema;

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

}
