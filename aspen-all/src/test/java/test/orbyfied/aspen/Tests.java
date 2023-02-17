package test.orbyfied.aspen;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.raw.YamlRawProvider;
import org.yaml.snakeyaml.DumperOptions;

import java.nio.file.Path;

public class Tests {

    private static final ConfigurationProvider CONFIGURATION_PROVIDER =
            ConfigurationProvider.newGlobalUnderPackage("test.orbyfied.aspen")
            .rawProvider(YamlRawProvider.builder()
                    .setMapKeyStyle(DumperOptions.ScalarStyle.PLAIN)
                    .setListFlowStyle(DumperOptions.FlowStyle.BLOCK)
                    .setMapFlowStyle(DumperOptions.FlowStyle.BLOCK)
                    .setSpacedComments(true)
                    .build()
            );

    public static ConfigurationProvider configurationProvider() {
        return CONFIGURATION_PROVIDER;
    }

    static final Path DIR = Path.of(".test/");

    public static Path file(String file) {
        return DIR.resolve(file);
    }

}
