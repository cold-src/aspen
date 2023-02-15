package test.orbyfied.aspen;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.Property;
import net.orbyfied.aspen.annotation.*;
import net.orbyfied.aspen.properties.SimpleProperty;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class BasicConfigTest {

    final Path testDir = Path.of("./.test");

    static final ConfigurationProvider CONFIGURATION_PROVIDER =
            new ConfigurationProvider();

    @Test
    void test() {
        MyClass myClass = new MyClass();
        CONFIGURATION_PROVIDER.parseProfile(
                "test",
                myClass,
                testDir.resolve("test.yml")
        ).load();

        System.out.println(myClass.a.get());
    }

    @Docs("Test configuration")
    static class MyClass {
        static class Config {
            static class MySec {
                @Option
                Integer b = 69;
            }

            @Section(name = "my-sec")
            MySec sec;
        }

        @Options
        Config config;

        SimpleProperty<Integer> a = SimpleProperty.builder("a", Integer.class)
                .comment("Hello Guys!")
                .build();
    }

}
