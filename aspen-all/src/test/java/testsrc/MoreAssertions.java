package testsrc;

import org.junit.jupiter.api.Assertions;

import java.util.function.Predicate;

public class MoreAssertions {

    public static void assertThrows(Predicate<Throwable> expected,
                                    Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            if (!expected.test(t))
                Assertions.fail("Thrown error " + t.getClass().getName() + " did not match expected predicate");
            return;
        }

        Assertions.fail("No exception was thrown");
    }

}
