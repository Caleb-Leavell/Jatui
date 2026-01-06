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

import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextInputModuleTest {
    @Test
    void testRun() {
        ApplicationModule app = ApplicationModule.builder("app")
                .build();

        String output;
        try(IOCapture io = new IOCapture("test")) {
            TextInputModule.Builder input = TextInputModule.builder("test-input", "input: ")
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .application(app);

            app.setHome(input);

            input.build().start();

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
            input = TextInputModule.builder("test-input", "input: ")
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();


            input.start();
        }

        assertEquals("test", input.getInput());
    }

    @Test
    void testStructuralEquals() {
        TextInputModule input1 = TextInputModule.builder("input", "input: ")
                .build();

        TextInputModule input2 = TextInputModule.builder("input", "input: ")
                .build();

        TextInputModule input3 = TextInputModule.builder("input", "input: ")
                .build();

        TextInputModule input4 = TextInputModule.builder("other", "input: ")
                .build();

        TextInputModule input5 = TextInputModule.builder("input", "other: ")
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
            TextInputModule.Builder original = TextInputModule.builder("input", "input: ")
                    .addHandler("logic", _ -> 5);

            TextInputModule.Builder copy = original.getCopy();

            assertAll(
                    () -> assertTrue(copy.structuralEquals(original)),
                    () -> assertTrue(copy.handlers.structuralEquals(original.handlers)),
                    () -> assertTrue(copy.getDisplayText().structuralEquals(original.getDisplayText())));
        }

        @Test
        void testAddHandlerModule() {
            ApplicationModule app = ApplicationModule.builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                FunctionModule.Builder logic = FunctionModule.builder("logic", () -> 5)
                        .application(app);
                TextInputModule.Builder input = TextInputModule.builder("input", "input: ")
                        .scanner(io.getScanner())
                        .addHandler(logic);

                app.setHome(input);

                app.start();
            }

            assertEquals(5, app.getInput("logic"));

        }

        @Test
        void testAddHandler() {
            ApplicationModule app = ApplicationModule.builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                TextInputModule.Builder input = TextInputModule.builder("input", "input: ")
                        .scanner(io.getScanner())
                        .addHandler("logic", _ -> 5);

                app.setHome(input);

                app.start();
            }

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddSafeHandlerExceptionHandler() {
            ApplicationModule app = ApplicationModule.builder("app").build();

            try(IOCapture io = new IOCapture("a")) {
                TextInputModule.Builder input = TextInputModule.builder("input", "input: ")
                        .scanner(io.getScanner())
                        .addSafeHandler("logic",
                                _ -> {throw new RuntimeException("force throw");},
                                _ -> app.updateInput("logic", 5));

                app.setHome(input);

                app.start();
            }

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddSafeHandlerExceptionMessage() {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("do-nothing"))
                    .build();

            String output;
            try(IOCapture io = new IOCapture("a\n5")) {
                TextInputModule.Builder input = TextInputModule.builder("input", "input: ")
                        .enableAnsi(false)
                        .addSafeHandler("logic",
                                Integer::parseInt,
                                "Success!")
                        .scanner(io.getScanner())
                        .printStream(io.getPrintStream());

                app.setHome(input);
                app.start();

                output = io.getOutput();
            }

            assertAll(
                    () -> assertEquals(5, app.getInput("logic")),
                    () -> assertEquals(String.format("input: Success!%ninput: "), output)
            );

        }

        @Test
        void testAddSafeHandler() {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("do-nothing"))
                    .build();

            String output;
            try(IOCapture io = new IOCapture("a\n5")) {
                TextInputModule.Builder input = TextInputModule.builder("input", "input: ")
                        .enableAnsi(false)
                        .addSafeHandler("logic", Integer::parseInt)
                        .scanner(io.getScanner())
                        .printStream(io.getPrintStream());

                app.setHome(input);

                app.start();

                output = io.getOutput();
            }

            assertAll(
                    () -> assertEquals(5, app.getInput("logic")),
                    () -> assertEquals(String.format("input: Error: Invalid Input%ninput: "), output)
            );
        }

        @Test
        void testBuild() {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .build();

            FunctionModule.Builder logic = FunctionModule.builder("logic", () -> 5)
                    .application(app);

            TextInputModule.Builder builder = TextInputModule.builder("input", "input: ")
                    .addHandler(logic);


            String first = runInputModule(builder);
            int firstOutput = app.getInput("logic", Integer.class);
            logic.function(() -> 6);
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
            builder.scanner(io.getScanner());
            builder.printStream(io.getPrintStream());

            builder.build().start();

            return io.getOutput();
        }
    }
}