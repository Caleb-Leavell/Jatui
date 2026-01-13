/*
    Copyright (c) 2026 Caleb Leavell

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class FunctionModuleTest {

    @Test
    void testRun() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        FunctionModule.Builder test = FunctionModule.builder("test", () -> {
            return "Test Output";
        });

        app.setHome(test);

        test.build().start();

        assertEquals("Test Output", app.getInput("test"));
    }

    @Test
    void testGetFunction() {
        Supplier<Integer> func = () -> 5;
        FunctionModule test = FunctionModule.builder("test", func).build();
        assertEquals(func, test.getFunction());
    }

    @Nested
    class BuilderTest {
        @Test
        void testSetFunctionSupplier() {
            Supplier<Integer> func = () -> 5;
            FunctionModule.Builder test = FunctionModule.builder("test", () -> {});

            test.function(func);

            assertEquals(func, test.getFunction());
        }

        @Test
        void testSetFunctionRunnable() {
            // can't test function equality since a supplier gets constructed from the runnable
            // so we test correct runnable functionality instead

            ApplicationModule app = ApplicationModule.builder("app").build();
            FunctionModule.Builder test = FunctionModule.builder("test", () -> {});

            List<Integer> testList = new ArrayList<>();

            test.function(() -> {
                testList.add(5);
            });

            app.setHome(test);

            test.build().start();

            assertAll(
                    () -> assertNull(app.getInput("test")),
                    () -> assertEquals(List.of(5), testList)
            );
        }

        @Test
        void testShallowCopy() {
            Supplier<Integer> func = () -> 31415;
            FunctionModule.Builder original = FunctionModule.builder("original", func)
                    .enableAnsi(false); // this is to ensure the super method is being called

            FunctionModule.Builder copy = original.createInstance();
            copy.shallowCopy(original);

            assertAll(
                    () -> assertEquals(func, copy.getFunction()),
                    () -> assertTrue(copy.structuralEquals(original))
            );
        }

        @Test
        void testBuild() {
            Supplier<Integer> func = () -> 31415;
            FunctionModule.Builder builder = FunctionModule.builder("original", func)
                    .enableAnsi(false);

            FunctionModule first = builder.build();
            FunctionModule second = builder.build();

            assertAll(
                    () -> assertEquals(func, first.getFunction()),
                    () -> assertEquals(func, second.getFunction()),
                    () -> assertTrue(first.structuralEquals(second))
            );

        }
    }
}