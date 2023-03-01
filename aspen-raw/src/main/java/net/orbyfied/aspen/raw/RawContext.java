package net.orbyfied.aspen.raw;

/**
 * A context in which raw data operations
 * can be executed. This provides data to
 * the operations handlers.
 */
public interface RawContext {

    RawProvider<?> rawProvider();

}
