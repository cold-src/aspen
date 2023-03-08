package test.orbyfied.aspen;

import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.annotation.Section;
import net.orbyfied.aspen.components.ValueConstraints;
import net.orbyfied.aspen.exception.PropertyExceptions;
import net.orbyfied.aspen.exception.PropertyLoadException;
import net.orbyfied.aspen.properties.SimpleProperty;
import org.junit.jupiter.api.Test;
import testsrc.MoreAssertions;
import testsrc.Tests;

import static org.junit.jupiter.api.Assertions.*;

public class BasicSectionTest {

    @Test
    void testBasicLoad() {
        TestProfile   o = new TestProfile();
        OptionProfile p = Tests.compose(o);

        /* 1: set up values */
        o.sec.a = 69;
        o.sec.b = "420";
        o.sec.c.set("hello");
        p.save();

        /* 1: test loaded */
        p.load();
        assertEquals(69, o.sec.a);
        assertEquals("420", o.sec.b);
        assertEquals("hello", o.sec.c.get());

        /* 2: set up values */
        o.sec.c.set(null);
        p.save();

        /* 2: test loaded */
        MoreAssertions.assertThrows(t -> t.getClass() == PropertyLoadException.class &&
                t.getCause().getClass() == PropertyExceptions.ValueException.class,
                p::load);

    }

    static class TestProfile {
        static class Sec {
            @Option
            Integer a = 69;

            @Option
            String b = "420";

            SimpleProperty<String> c = SimpleProperty.builder("c", String.class)
                    .defaultValue("hi!")
                    .with(ValueConstraints.notNull())
                    .build();
        }

        @Section(name = "my-sec")
        Sec sec;
    }

}
