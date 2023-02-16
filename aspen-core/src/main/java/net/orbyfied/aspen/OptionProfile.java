package net.orbyfied.aspen;

import net.orbyfied.aspen.exception.ConfigurationLoadException;
import net.orbyfied.aspen.raw.MapNode;
import net.orbyfied.aspen.raw.Node;

import java.io.FileReader;
import java.io.FileWriter;
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

    ////////////////////////////////////

    OptionProfile(ConfigurationProvider provider,
                  String name,
                  String defaults,
                  Path file,
                  Object instance) throws Exception {
        this(provider, name, defaults, file, new OptionSchema(null, instance)
                .compose(provider));
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
        try (FileReader reader = new FileReader(file.toFile())) {
            // compose node
            Node node = provider.rawProvider().compose(reader);
            if (!(node instanceof MapNode mapNode))
                throw new ConfigurationLoadException("Composed configuration from file " + file + " is not a map node");

            // load schema
            schema.load(mapNode);

            // re-save defaults to update
            save();

            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Profile '" + name + "' load failed file(" + file + ")");
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
        try (FileWriter writer = new FileWriter(file.toFile())) {
            // emit to node tree
            Node node = schema.emit();

            // save node tree
            provider.rawProvider().write(node, writer);

            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Profile '" + name + "' load failed file(" + file + ")");
        }
    }

}
