package test.orbyfied.aspen;

import net.orbyfied.aspen.Accessor;
import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.PropertyAccess;
import net.orbyfied.aspen.SectionSchema;
import net.orbyfied.aspen.annotation.Option;
import net.orbyfied.aspen.properties.SimpleProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualSectionTest {

    @Test
    void test() {
        TestProfile1 i1 = new TestProfile1();
        OptionProfile profile1 = Tests.compose(i1, profile -> {
            // create virtual section
            SectionSchema sec = profile.schema().virtualSection("my-virtual-section");
            sec.withProperty(SimpleProperty.builder("b", Float.class)
                    .accessor(Accessor.sharedMutable())
                    .build());
        });

        i1.a = 1;
        i1.b.set(2f);

        System.out.println(i1.a);
        System.out.println(i1.b.get());

        profile1.save();
        profile1.load();

        Assertions.assertEquals(1, i1.a);
        Assertions.assertEquals(2f, i1.b.get());
    }

    static class TestProfile1 {

        @Option
        Integer a = 69;

        PropertyAccess<Float> b = PropertyAccess.find("my-virtual-section/b");

    }

}
