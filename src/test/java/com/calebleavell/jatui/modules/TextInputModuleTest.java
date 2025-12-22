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

import com.calebleavell.jatui.modules.*;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TextInputModuleTest {
    @Test
    void testRun() {
        ApplicationModule app = new ApplicationModule.Builder("app")
                .build();

        String output;
        try(IOCapture io = new IOCapture("test")) {
            TextInputModule.Builder input = new TextInputModule.Builder("test-input", "input: ")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setApplication(app);

            app.setHome(input);

            input.build().run();

            output = io.getOutput();
        }

        assertAll(
                () -> assertEquals("input: ", output),
                () -> assertEquals("test", app.getInput("test-input"))
        );
    }

    @Test
    void testGetInput() {
        TextInputModule input;

        try(IOCapture io = new IOCapture("test")) {
            input = new TextInputModule.Builder("test-input", "input: ")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();


            input.run();
        }

        assertEquals("test", input.getInput());
    }

    @Test
    void testStructuralEquals() {
        TextInputModule input1 = new TextInputModule.Builder("input", "input: ")
                .build();

        TextInputModule input2 = new TextInputModule.Builder("input", "input: ")
                .build();

        TextInputModule input3 = new TextInputModule.Builder("input", "input: ")
                .build();

        TextInputModule input4 = new TextInputModule.Builder("other", "input: ")
                .build();

        TextInputModule input5 = new TextInputModule.Builder("input", "other: ")
                .build();

        assertAll(
                () -> assertTrue(input1.structuralEquals(input2)),
                () -> assertTrue(input2.structuralEquals(input1)),
                () -> assertTrue(input2.structuralEquals(input3)),
                () -> assertTrue(input1.structuralEquals(input3)),
                () -> assertFalse(input1.structuralEquals(input4)),
                () -> assertFalse(input1.structuralEquals(input5))
        );

    }

    @Nested
    class BuilderTest {

        @Test
        void testCopy() {
            TextInputModule.Builder original = new TextInputModule.Builder("input", "input: ")
                    .addHandler("logic", s -> 5);

            TextInputModule.Builder copy = original.getCopy();

            assertAll(
                    () -> assertTrue(copy.structuralEquals(original)),
                    () -> assertTrue(copy.handlers.structuralEquals(original.handlers)),
                    () -> assertTrue(copy.getDisplayText().structuralEquals(original.getDisplayText())));
        }

        @Test
        void testAddHandlerModule() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                FunctionModule.Builder logic = new FunctionModule.Builder("logic", () -> 5)
                        .setApplication(app);
                TextInputModule.Builder input = new TextInputModule.Builder("input", "input: ")
                        .setScanner(io.getScanner())
                        .addHandler(logic);

                app.setHome(input);

                app.run();
            }

            assertEquals(5, app.getInput("logic"));

        }

        @Test
        void testAddHandler() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                TextInputModule.Builder input = new TextInputModule.Builder("input", "input: ")
                        .setScanner(io.getScanner())
                        .addHandler("logic", s -> 5);

                app.setHome(input);

                app.run();
            }

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddSafeHandlerExceptionHandler() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                TextInputModule.Builder input = new TextInputModule.Builder("input", "input: ")
                        .setScanner(io.getScanner())
                        .addSafeHandler("logic",
                                s -> {throw new RuntimeException("force throw");},
                                s -> app.updateInput("logic", 5));

                app.setHome(input);

                app.run();
            }

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddSafeHandlerExceptionMessage() {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("do-nothing"))
                    .build();

            String output;
            try(IOCapture io = new IOCapture("a\n5")) {
                TextInputModule.Builder input = new TextInputModule.Builder("input", "input: ")
                        .enableAnsi(false)
                        .addSafeHandler("logic",
                                Integer::parseInt,
                                "Success!")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream());

                app.setHome(input);
                app.run();

                output = io.getOutput();
            }

            assertAll(
                    () -> assertEquals(5, app.getInput("logic")),
                    () -> assertEquals(String.format("input: Success!%ninput: "), output)
            );

        }

        @Test
        void testAddSafeHandler() {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("do-nothing"))
                    .build();

            String output;
            try(IOCapture io = new IOCapture("a\n5")) {
                TextInputModule.Builder input = new TextInputModule.Builder("input", "input: ")
                        .enableAnsi(false)
                        .addSafeHandler("logic", Integer::parseInt)
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream());

                app.setHome(input);

                app.run();

                output = io.getOutput();
            }

            assertAll(
                    () -> assertEquals(5, app.getInput("logic")),
                    () -> assertEquals(String.format("input: Error: Invalid Input%ninput: "), output)
            );
        }

        @Test
        void testBuild() {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("empty"))
                    .build();

            FunctionModule.Builder logic = new FunctionModule.Builder("logic", () -> 5)
                    .setApplication(app);

            TextInputModule.Builder builder = new TextInputModule.Builder("input", "input: ")
                    .addHandler(logic);


            String first = runInputModule(builder);
            int firstOutput = app.getInput("logic", Integer.class);
            logic.setFunction(() -> 6);
            String second = runInputModule(builder);
            int secondOutput = app.getInput("logic", Integer.class);

            assertAll(
                    () -> assertTrue(builder.build().structuralEquals(builder.build())),
                    () -> assertEquals(first, second),
                    () -> assertEquals(5, firstOutput),
                    () -> assertEquals(6, secondOutput)
            );
        }
    }

    private static String runInputModule(TextInputModule.Builder builder) {
        try(IOCapture io = new IOCapture("input\ninput\ninput")) {
            builder.unlockProperty(TUIModule.Property.SCANNER);
            builder.unlockProperty(TUIModule.Property.PRINTSTREAM);
            builder.setScanner(io.getScanner());
            builder.setPrintStream(io.getPrintStream());

            builder.build().run();

            return io.getOutput();
        }
    }
}