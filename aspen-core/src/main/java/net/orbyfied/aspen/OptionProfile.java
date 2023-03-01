package net.orbyfied.aspen;

import net.orbyfied.aspen.context.IOContext;
import net.orbyfied.aspen.exception.AspenException;
import net.orbyfied.aspen.exception.ConfigurationLoadException;
import net.orbyfied.aspen.raw.nodes.RawObjectNode;
import net.orbyfied.aspen.raw.nodes.RawNode;
import net.orbyfied.aspen.util.Throwables;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * A configuration profile for a specific
 * instance.
 *
 * @author orbyfied
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public record OptionProfile(ConfigurationProvider provider,
                            String name,
                            String defaults,
                            Path file,
                            OptionSchema schema) {

    static final Logger LOGGER = Logger.getLogger("OptionProfile");

    static Path createIfAbsent(Path path) {
        try {
            if (!Files.exists(path)) {
                Path parent = path.getParent();
                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }

                Files.createFile(path);
            }

            return path;
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
            return path;
        }
    }

    ////////////////////////////////////

    OptionProfile(ConfigurationProvider provider,
                  String name,
                  String defaults,
                  Path file,
                  Object instance) throws Exception {
        this(provider, name, defaults, file, new OptionSchema(provider, instance));
    }

    public OptionProfile compose() throws Exception {
        schema.compose(provider);
        return this;
    }

    /**
     * Get the root instance.
     */
    public Object instance() {
        return schema.instance;
    }

    /**
     * Load the options from the persistent
     * storage file.
     *
     * @return This.
     * @throws IllegalStateException If an error occurs.
     */
    public OptionProfile load() {
        if (!Files.exists(file)) // skip loading if the file is absent
            return this;

        try (FileReader reader = new FileReader(file.toFile())) {
            // compose node
            IOContext ioContext = provider.newReadContext(this, file.getFileName().toString());
            RawNode node = provider.rawProvider().compose(ioContext, reader);
            node = provider.preProcessRaw(node);
            if (!(node instanceof RawObjectNode mapNode))
                throw new ConfigurationLoadException("Composed configuration from file " + file + " is not a map node");

            // load schema
            Context context = provider.newLoadContext(this);
            schema.load(context, mapNode);

            return this;
        } catch (Exception e) {
            if (e instanceof AspenException aspenException)
                throw aspenException;
            throw new ConfigurationLoadException("Profile '" + name + "' load failed file(" + file + ")", e);
        }
    }

    /**
     * Saves the options to the persistent
     * storage file.
     *
     * @return This.
     * @throws IllegalStateException If an error occurs.
     */
    public OptionProfile save() {
        try (FileWriter writer = new FileWriter(createIfAbsent(file).toFile())) {
            // emit to node tree
            Context context = provider.newEmitContext(this);
            RawNode node = schema.emit(context);
            node = provider.postProcessRaw(node);

            // save node tree
            IOContext ioContext = provider.newWriteContext(this, file.getFileName().toString());
            provider.rawProvider().write(ioContext, node, writer);

            return this;
        } catch (Exception e) {
            if (e instanceof AspenException aspenException)
                throw aspenException;
            throw new IllegalStateException("Profile '" + name + "' save failed file(" + file + ")", e);
        }
    }

}
