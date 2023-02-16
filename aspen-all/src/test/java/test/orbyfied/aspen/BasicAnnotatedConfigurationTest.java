package test.orbyfied.aspen;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.annotation.Docs;
import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.annotation.Options;
import net.orbyfied.aspen.annotation.Section;
import net.orbyfied.aspen.components.ValueConstraints;
import net.orbyfied.aspen.properties.CollectionProperty;
import net.orbyfied.aspen.properties.SimpleProperty;
import net.orbyfied.aspen.raw.YamlRawProvider;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;

import java.nio.file.Path;
import java.util.ArrayList;

public class BasicAnnotatedConfigurationTest {

    final Path testDir = Path.of("./.test");

    static final ConfigurationProvider CONFIGURATION_PROVIDER =
            new ConfigurationProvider()
                    /* use yaml provider */
                    .rawProvider(YamlRawProvider.builder()
                            .setListFlowStyle(DumperOptions.FlowStyle.BLOCK)
                            .setMapFlowStyle(DumperOptions.FlowStyle.BLOCK)
                            .setMapKeyStyle(DumperOptions.ScalarStyle.PLAIN)
                            .setSpacedComments(true)
                            .build()
                    );

    @Test
    void test() {
        MyClass myClass = new MyClass();
        OptionProfile p = CONFIGURATION_PROVIDER.parseProfile(
                "test",
                myClass,
                testDir.resolve("test.yml")
        );

        myClass.fList.get().add(500.0f);
        System.out.println(myClass.fList.get());
        System.out.println(myClass.fList.emit());

        p.save();
    }

    @Docs("Test configuration")
    static class MyClass {
        static class Config {
            static class MySec {
                @Option
                Integer b;
            }

            @Section(name = "my-sec")
            MySec sec;
        }

        @Options
        Config config;

        SimpleProperty<Double> a = SimpleProperty.builder("a", Double.class)
                .with(ValueConstraints.minMax(0, 500))
                .commenter(node -> node.blockComment("Hey guys!"))
                .build();

        CollectionProperty<Float> fList = CollectionProperty.arrayList("floats",
                SimpleProperty.anonymous(Float.class).with(ValueConstraints.minMax(0, 500)).build()
        ).defaultValue(ArrayList::new).build();
    }

}
