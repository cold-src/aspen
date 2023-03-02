package test.orbyfied.aspen;

import net.orbyfied.aspen.OptionProfile;
import net.orbyfied.aspen.PropertyAccess;
import net.orbyfied.aspen.components.ValueConstraints;
import net.orbyfied.aspen.exception.PropertyLoadException;
import net.orbyfied.aspen.properties.NumberProperty;
import net.orbyfied.aspen.properties.SimpleProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstraintComponentTest {

    @Test
    void test() {
        TestProfile1 i1 = new TestProfile1();
        OptionProfile p1 = Tests.compose(i1);

        /*
            1: All Constraints Met
         */

        i1.notNullOption.set("hello");
        i1.minMaxOption.set(6f);

        p1.save();
        Assertions.assertDoesNotThrow(p1::load);
        Assertions.assertEquals("hello", i1.notNullOption.get());
        Assertions.assertEquals(6f, i1.minMaxOption.get());

        /*
            2: Null Constraint Fails
         */

        i1.notNullOption.set(null);
        p1.save();

        Assertions.assertThrows(PropertyLoadException.class, p1::load);

        /*
            3: MinMax Constraint Fails
         */

        i1.notNullOption.set("hello2");
        i1.minMaxOption.set(3f); // 3 < 5

        Assertions.assertThrows(PropertyLoadException.class, p1::load);
    }

    static class TestProfile1 {
        final SimpleProperty<String> pNotNullOption = SimpleProperty.builder("not-null-option", String.class)
                .with(ValueConstraints.notNull())
                .commenter(node -> node.endComment("not null lol"))
                .build();

        final PropertyAccess<String> notNullOption = PropertyAccess.future(pNotNullOption);

        final NumberProperty<Float> pMinMaxOption = NumberProperty.builder("min-max-option", Float.class)
                .with(ValueConstraints.minMax(5, 10))
                .commenter(node -> node.endComment("min max lol"))
                .build();

        final PropertyAccess<Float> minMaxOption = PropertyAccess.future(pMinMaxOption);
    }

}
