package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TUIFunctionModuleTest {

    @Test
    void run() {
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
        TUIFunctionModule.Builder test = new TUIFunctionModule.Builder("test", () -> {
            return "Test Output";
        });

        app.setHome(test);

        test.build().run();

        assertEquals("Test Output", app.getInput("test"));
    }

    @Test
    void getFunction() {
        Supplier<Integer> func = () -> 5;
        TUIFunctionModule test = new TUIFunctionModule.Builder("test", func).build();
        assertEquals(func, test.getFunction());
    }

    @Nested
    class BuilderTest {
        @Test
        void testSetFunctionSupplier() {
            Supplier<Integer> func = () -> 5;
            TUIFunctionModule.Builder test = new TUIFunctionModule.Builder("test", () -> {});

            test.setFunction(func);

            assertEquals(func, test.getFunction());
        }

        @Test
        void testSetFunctionRunnable() {
            // can't test function equality since a supplier gets constructed from the runnable
            // so we test correct runnable functionality instead

            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUIFunctionModule.Builder test = new TUIFunctionModule.Builder("test", () -> {});

            List<Integer> testList = new ArrayList<>();

            test.setFunction(() -> {
                testList.add(5);
            });

            app.setHome(test);

            test.build().run();

            assertAll(
                    () -> assertNull(app.getInput("test")),
                    () -> assertEquals(List.of(5), testList)
            );
        }

        @Test
        void testShallowCopy() {
            Supplier<Integer> func = () -> 31415;
            TUIFunctionModule.Builder original = new TUIFunctionModule.Builder("original", func)
                    .enableAnsi(false); // this is to ensure the super method is being called

            TUIFunctionModule.Builder copy = original.createInstance();
            copy.shallowCopy(original);

            assertAll(
                    () -> assertEquals(func, copy.getFunction()),
                    () -> assertTrue(copy.equals(original))
            );
        }

        @Test
        void testBuild() {
            Supplier<Integer> func = () -> 31415;
            TUIFunctionModule.Builder builder = new TUIFunctionModule.Builder("original", func)
                    .enableAnsi(false);

            TUIFunctionModule first = builder.build();
            TUIFunctionModule second = builder.build();

            assertAll(
                    () -> assertEquals(func, first.getFunction()),
                    () -> assertEquals(func, second.getFunction()),
                    () -> assertTrue(first.equals(second))
            );

        }
    }
}