/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.FunctionModule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class FunctionModuleTest {

    @Test
    void testRun() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        FunctionModule.Builder test = new FunctionModule.Builder("test", () -> {
            return "Test Output";
        });

        app.setHome(test);

        test.build().run();

        assertEquals("Test Output", app.getInput("test"));
    }

    @Test
    void testGetFunction() {
        Supplier<Integer> func = () -> 5;
        FunctionModule test = new FunctionModule.Builder("test", func).build();
        assertEquals(func, test.getFunction());
    }

    @Nested
    class BuilderTest {
        @Test
        void testSetFunctionSupplier() {
            Supplier<Integer> func = () -> 5;
            FunctionModule.Builder test = new FunctionModule.Builder("test", () -> {});

            test.setFunction(func);

            assertEquals(func, test.getFunction());
        }

        @Test
        void testSetFunctionRunnable() {
            // can't test function equality since a supplier gets constructed from the runnable
            // so we test correct runnable functionality instead

            ApplicationModule app = new ApplicationModule.Builder("app").build();
            FunctionModule.Builder test = new FunctionModule.Builder("test", () -> {});

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
            FunctionModule.Builder original = new FunctionModule.Builder("original", func)
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
            FunctionModule.Builder builder = new FunctionModule.Builder("original", func)
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