package test.orbyfied.aspen;

import net.orbyfied.aspen.ConfigurationProvider;
import net.orbyfied.aspen.OptionComposer;
import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.PropertyComponent;
import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.exception.ConfigurationLoadException;
import net.orbyfied.aspen.exception.PropertyExceptions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;

public class CustomComposerTest {

    // test component class
    static class NotNullComponent implements PropertyComponent<Object, Object> {
        @Override
        public Object checkLoadedValue(Object val) {
            if (val == null)
                PropertyExceptions.failIllegalValue(null, "value can not be null");
            return val;
        }
    }

    /*
        1. Basic Option Composer
     */

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotNullOption { }

    @Test
    void testBasicOptionComposer() {
        ConfigurationProvider provider = Tests.configurationProvider().fork();
        provider.withOptionComposer(OptionComposer.configureAllAnnotated(NotNullOption.class,
                ((context, notNullOption) -> {
                    context.builder().with(new NotNullComponent());
                })));

        // compose profile
        TestProfile1 i1 = new TestProfile1();
        OptionProfile profile = Tests.compose(provider, i1);

        i1.nullableOption1 = null;
        i1.nullableOption2 = 400;
        i1.notNullOption1 = null;
        i1.notNullOption2 = 600;

        profile.save();

        profile.load();
    }

    static class TestProfile1 {
        @Option
        Integer nullableOption1;

        @Option
        Integer nullableOption2;

        @Option
        @NotNullOption
        Integer notNullOption1;

        @Option
        @NotNullOption
        Integer notNullOption2;
    }

}
