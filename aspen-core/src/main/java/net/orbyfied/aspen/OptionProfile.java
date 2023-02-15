package net.orbyfied.aspen;

import net.orbyfied.aspen.raw.Configuration;
import net.orbyfied.aspen.raw.ConfigurationSection;
import org.yaml.snakeyaml.Yaml;

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
                            OptionSchema schema,
                            Configuration configuration) {

    static final Logger LOGGER = Logger.getLogger("OptionProfile");
    static final Yaml YAML = new Yaml();

    static {
        YAML.setName("OptionProfile");
    }

    ////////////////////////////////////

    OptionProfile(ConfigurationProvider provider,
                  String name,
                  String defaults,
                  Path file,
                  Object instance) throws Exception {
        this(provider, name, defaults, file, new OptionSchema(null, instance)
                .compile(provider),
                new Configuration(file, f -> instance.getClass()
                    .getResourceAsStream(f)));
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
        try {
            if (defaults != null) {
                configuration.reloadOrDefaultThrowing(defaults);
            } else {
                if (Files.exists(file)) {
                    configuration.reload();
                }
            }

            loadSchema(schema, configuration);

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
        try {
            saveSchema(schema, configuration);

            // save to file
            configuration.save();

            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Profile '" + name + "' load failed file(" + file + ")");
        }
    }

    // load the given schema/section
    // from the provided config section
    protected void loadSchema(Schema schema,
                              ConfigurationSection section) {
        // load all properties
        for (Property property : schema.propertyMap.values()) {
            String key = property.name;
            if (!section.contains(key))
                continue;

            Object primVal = section.get(key);
            Object val = property.valueFromPrimitive(primVal);
            property.schema = schema;

            property.set(val);
        }

        // load all option sections
        if (schema instanceof OptionSchema optionSchema) {
            for (OptionSchema childSchema : optionSchema.providedChildren) {
                loadSchema(childSchema, section);
            }
        }

        // load all sections
        for (Schema sectionSchema : schema.sections.values()) {
            loadSchema(sectionSchema, section.section(sectionSchema.name));
        }
    }

    // save the given schema/section
    // to the provided config section
    protected void saveSchema(Schema schema,
                              ConfigurationSection section) {
        // save all properties
        for (Property property : schema.propertyMap.values()) {
            property.schema = schema;

            Object val = property.get();
            Object primVal = property.valueToPrimitive(val);

            section.set(property.name, primVal);
        }

        // save all option schemas
        if (schema instanceof OptionSchema optionSchema) {
            for (OptionSchema childSchema : optionSchema.providedChildren) {
                saveSchema(childSchema, section);
            }
        }

        // save all sections
        for (Schema sectionSchema : schema.sections.values()) {
            saveSchema(sectionSchema,
                    section.section(sectionSchema.name));
        }
    }

}
