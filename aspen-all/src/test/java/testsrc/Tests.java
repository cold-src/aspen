package testsrc;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.raw.YamlRawProvider;
import org.yaml.snakeyaml.DumperOptions;

import java.nio.file.Path;
import java.util.function.Consumer;

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

    public static OptionProfile compose(Object instance) {
        Class<?> cl = instance.getClass();
        String name = cl.getSimpleName().toLowerCase();
        if (cl.getDeclaringClass() != null)
            name = cl.getDeclaringClass().getSimpleName() + "$" + name;
        return Tests.configurationProvider()
                .composeProfile(
                        name,
                        instance,
                        file(name + ".yml")
                );
    }

    public static OptionProfile compose(ConfigurationProvider provider, Object instance) {
        String name = instance.getClass().getSimpleName().toLowerCase();
        return provider
                .composeProfile(
                        name,
                        instance,
                        Tests.file(name + ".yml")
                );
    }

    public static OptionProfile compose(Object instance, Consumer<OptionProfile> consumer) {
        String name = instance.getClass().getSimpleName().toLowerCase();
        return Tests.configurationProvider()
                .composeProfile(
                        name,
                        instance,
                        Tests.file(name + ".yml"),
                        consumer
                );
    }

    public static OptionProfile compose(ConfigurationProvider provider, Object instance,
                                        Consumer<OptionProfile> consumer) {
        String name = instance.getClass().getSimpleName().toLowerCase();
        return provider
                .composeProfile(
                        name,
                        instance,
                        Tests.file(name + ".yml"),
                        consumer
                );
    }

    public static ConfigurationProvider configurationProvider() {
        return CONFIGURATION_PROVIDER;
    }

    static final Path DIR = Path.of(".test/");

    public static Path file(String file) {
        return DIR.resolve(file);
    }

}
