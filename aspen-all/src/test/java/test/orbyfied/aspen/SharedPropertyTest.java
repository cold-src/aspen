package test.orbyfied.aspen;

import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.PropertyAccess;
import net.orbyfied.aspen.annotation.Docs;
import net.orbyfied.aspen.properties.SimpleProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testsrc.Tests;

public class SharedPropertyTest {

    @Test
    void test() {
        Options1 o1 = new Options1();
        Options2 o2 = new Options2();
        OptionProfile p1 = Tests.compose(o1);
        OptionProfile p2 = Tests.compose(o2);

        o1.a.set(true);
        o2.a.set(false);

        p1.save();
        p2.save();

        o2.a.set(true);
        o1.a.set(false);

        p1.load();
        p2.load();

        Assertions.assertEquals(true, o1.a.get());
        Assertions.assertEquals(false, o2.a.get());
    }

    static final SimpleProperty<Boolean> A = SimpleProperty.builder("a", Boolean.class)
            .shared()
            .commenter(node -> node.inLineComment("This property is shared"))
            .defaultValue(false)
            .build();

    @Docs(inLine = "Options 1")
    static class Options1 {
        PropertyAccess<Boolean> a = PropertyAccess.future(A);
    }

    @Docs(inLine = "Options 2")
    static class Options2 {
        PropertyAccess<Boolean> a = PropertyAccess.future(A);
    }

}
