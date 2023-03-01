package net.orbyfied.aspen.raw;

/**
 * A context for raw data operations related to
 * (file-based) input and output.
 */
public interface RawIOContext extends RawContext {

    /**
     * Get the virtual file name.
     * This can be the actual name of the file or
     * a virtual name.
     *
     * @return The virtual file name.
     */
    String fileName();

}
