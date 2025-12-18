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

package com.calebleavell.jatui.tui;

import com.calebleavell.jatui.IOCapture;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationModuleTest {

    @Test
    void testRun_default_exit() {
        String expected = lines("Exiting...");

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .enableAnsi(false)
                    .setPrintStream(io.getPrintStream())
                    .build();
            app.run();
            assertEquals(expected, io.getOutput());
        }
    }

    @Test
    void testRun_with_text() {
        String expected = lines("Hello, World!", "Exiting...");

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .addChildren(new TextModule.Builder("hello-world", "Hello, World!"))
                    .enableAnsi(false)
                    .setPrintStream(io.getPrintStream())
                    .build();
            app.run();
            assertEquals(expected, io.getOutput());
        }

    }

    @Test
    void testRun_with_input() {
        String expected = lines(
                "What is your name? Hello, Bob!",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture("Bob")) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .addChildren(
                            new TextInputModule.Builder("input", "What is your name? "),
                            new ModuleFactory.LineBuilder("output")
                                    .addText("Hello, ").addModuleOutput("input").addText("!").newLine())
                    .enableAnsi(false)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .build();

            app.run();
            assertEquals(expected, io.getOutput());
        }


    }

    @Test
    void testRun_with_function() {
        String expected = lines(
                "10",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .addChildren(
                            new FunctionModule.Builder("5+5", () -> 5 + 5),
                            new ModuleFactory.LineBuilder("output").addModuleOutput("5+5").newLine()
                    )
                    .enableAnsi(false)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .build();

            app.run();
            assertEquals(expected, io.getOutput());
        }
    }

    @Test
    void testResetMemory() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        app.forceUpdateInput("input-1", 5);
        app.forceUpdateInput("input-2", "input");
        app.forceUpdateInput("input-3", "1234567".toCharArray());
        char[] arrInput = app.getInput("input-3", char[].class);

        app.resetMemory();

        assertAll(
                () -> assertNull(app.getInput("input-1")),
                () -> assertNull(app.getInput("input-2")),
                () -> assertNull(app.getInput("input-3")),
                () -> assertArrayEquals("       ".toCharArray(), arrInput)
        );
    }

    @Test
    void testGetInput_input_module() {
        String expected = "Bob";

        try(IOCapture io = new IOCapture(expected)) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .addChildren(
                            new TextInputModule.Builder("input", "What is your name? "))
                    .enableAnsi(false)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .build();

            app.run();
            assertAll(
                    () -> assertEquals(expected, app.getInput("input")),
                    () -> assertEquals(expected, app.getInput("input", String.class)),
                    () -> assertNull(app.getInput("input", Integer.class)),
                    () -> assertNull(app.getInput("INPUT")), // ensure naming is case-sensitive
                    () -> assertNull(app.getInput("random name")));

        }
    }

    @Test
    void testGetInput_function_module() {
        int expected = 5;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .addChildren(
                            new FunctionModule.Builder("input", () -> expected))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .build();

            app.run();
            assertAll(
                    () -> assertEquals(expected, app.getInput("input")),
                    () -> assertEquals(expected, app.getInput("input", Integer.class)),
                    () -> assertNull(app.getInput("input", String.class)),
                    () -> assertNull(app.getInput("INPUT")),
                    () -> assertNull(app.getInput("random name")));

        }
    }

    @Test
    void getInputOrDefault() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        app.forceUpdateInput("input", 5);

        assertAll(
                () -> assertEquals(5, app.getInputOrDefault("input", Integer.class, 5)),
                () -> assertEquals(2, app.getInputOrDefault("nothing", Integer.class, 2)),
                () -> assertEquals("default", app.getInputOrDefault("input", String.class, "default"))
        );
    }


    @Test
    void testUpdateInput_no_module() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        app.updateInput("input", 5);
        assertNull(app.getInput("input"));
    }

    @Test
    void testUpdateInput_module_name() {
        int expected = 5;

        ApplicationModule app = new ApplicationModule.Builder("app")
                .addChild(ModuleFactory.empty("input"))
                .build();
        app.updateInput("input", expected);
        assertAll(
                () -> assertEquals(expected, app.getInput("input")),
                () -> assertEquals(expected, app.getInput("input", Integer.class)));
    }

    @Test
    void testUpdateInput_module_object() {
        int expected = 5;

        ContainerModule.Builder input = ModuleFactory.empty("input");

        ApplicationModule app = new ApplicationModule.Builder("app")
                .addChild(input)
                .build();
        app.updateInput(input.build(), expected);
        assertAll(
                () -> assertEquals(expected, app.getInput("input")),
                () -> assertEquals(expected, app.getInput("input", Integer.class)));
    }

    @Test
    void testSetHome() {
        String expected = lines(
                "Hello, World!",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .setAnsi(ansi().bold()) // set to non-default value to test against home ansi
                    .setPrintStream(io.getPrintStream())
                    .setScanner(io.getScanner()) //also set to test against home ansi
                    .enableAnsi(false)
                    .build();

            TextModule.Builder home = new TextModule.Builder("home", "Hello, World!");

            app.setHome(home);
            app.run();
            assertAll(
                    () -> assertEquals(expected, io.getOutput()),
                    () -> assertEquals(home, app.getHome()),
                    () -> assertEquals(app, home.getApplication()),
                    () -> assertEquals(app.getScanner(), home.getScanner()),
                    () -> assertEquals(app.getPrintStream(), home.getPrintStream()),
                    () -> assertEquals(app.getAnsiEnabled(), home.getAnsiEnabled()),
                    () -> assertNotEquals(app.getAnsi(), home.getAnsi())
            );
        }
    }

    @Test
    void testGetHome() {
        ContainerModule.Builder home = new ContainerModule.Builder("home");
        ApplicationModule app = new ApplicationModule.Builder("test-app").build();
        app.setHome(home);
        assertEquals(app.getHome(), home);
    }

    @Test
    void testSetOnExit() {
        String expected = lines(
                "this should come first",
                "test exit!");

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("test-app")
                    .setAnsi(ansi().bold()) // all properties set to non-default values to test against the onExit module
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TextModule.Builder onExit = new TextModule.Builder("exit", "test exit!");

            app.setOnExit(onExit);

            // we add this manually after setOnExit to make sure it doesn't get added after the exit
            app.getChildren().add(
                    new TextModule.Builder("text", "this should come first")
                            .setPrintStream(io.getPrintStream())
                            .enableAnsi(false));

            app.run();

            assertAll(
                    () -> assertEquals(expected, io.getOutput()),
                    () -> assertEquals(app.getOnExit(), onExit),
                    () -> assertEquals(app, onExit.getApplication()),
                    () -> assertEquals(app.getScanner(), onExit.getScanner()),
                    () -> assertEquals(app.getPrintStream(), onExit.getPrintStream()),
                    () -> assertEquals(app.getAnsiEnabled(), onExit.getAnsiEnabled()),
                    () -> assertNotEquals(app.getAnsi(), onExit.getAnsi())
            );
        }
    }

    @Test
    void testGetOnExit() {
        ContainerModule.Builder onExit = new ContainerModule.Builder("exit");
        ApplicationModule app = new ApplicationModule.Builder("test-app").build();
        app.setOnExit(onExit);
        assertEquals(app.getOnExit(), onExit);
    }

    @Test
    void structuralEqualsTest() {
        ContainerModule.Builder exit = new ContainerModule.Builder("exit");
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        ApplicationModule app1 = new ApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        ApplicationModule app2 = new ApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        ApplicationModule app3 = new ApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        ApplicationModule app4 = new ApplicationModule.Builder("app")
                .setOnExit(exit.getCopy())
                .build();

        ApplicationModule app5 = new ApplicationModule.Builder("app")
                .setApplication(testApp)
                .build();

        assertAll(
            () -> assertTrue(app1.structuralEquals(app1)),
            () -> assertFalse(app1.structuralEquals(null)),
            () -> assertTrue(app1.structuralEquals(app2)),
            () -> assertTrue(app2.structuralEquals(app3)),
            () -> assertTrue(app1.structuralEquals(app3)),
            () -> assertFalse(app1.structuralEquals(app4)),
            () -> assertFalse(app1.structuralEquals(app5))

        );
    }

    @Nested
    class BuilderTest {
        @Test
        void getCopyTest() {

            var home = new ContainerModule.Builder("home");
            var onExit = new ContainerModule.Builder("onExit");
            ApplicationModule.Builder app = new ApplicationModule.Builder("test-app")
                    .setHome(home)
                    .setOnExit(onExit);

            ApplicationModule.Builder copy = app.getCopy();

            assertAll(
                    () -> assertTrue(app.structuralEquals(copy)),
                    () -> assertFalse(app.build().structuralEquals(copy.build())) // should be false since their respective applications will not have the same reference
            );

        }

        @Test
        void setOnExitTest() {
            var onExit = new ContainerModule.Builder("onExit");
            ApplicationModule.Builder app = new ApplicationModule.Builder("test-app")
                    .setOnExit(onExit);

            assertEquals(app.getOnExit(), onExit);
        }

        @Test
        void setHomeTest() {
            var home = new ContainerModule.Builder("home");
            ApplicationModule.Builder app = new ApplicationModule.Builder("test-app")
                    .setHome(home);

            assertEquals(app.getHome(), home);
        }

        @Test
        void buildTest_fields_equal() {
            ApplicationModule setApp = new ApplicationModule.Builder("setApp").build();
            Ansi ansi = ansi().bold();
            ContainerModule.Builder home = new ContainerModule.Builder("home");
            ContainerModule.Builder onExit = new ContainerModule.Builder("exit");

            try(IOCapture io = new IOCapture()) {
                ApplicationModule app = new ApplicationModule.Builder("test-app")
                        .setApplication(setApp)
                        .setAnsi(ansi)
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .setHome(home)
                        .enableAnsi(false)
                        .setOnExit(onExit)
                        .build();

                assertAll(
                        () -> assertEquals(app.getApplication(), setApp),
                        () -> assertEquals(app.getAnsi(), ansi),
                        () -> assertEquals(app.getHome(), home),
                        () -> assertEquals(app.getOnExit(), onExit),
                        () -> assertEquals(app.getPrintStream(), io.getPrintStream()),
                        () -> assertEquals(app.getScanner(), io.getScanner())
                );
            }
        }

        @Test
        void buildTest_equivalent_builds() {
            ApplicationModule setApp = new ApplicationModule.Builder("setApp").build();
            Ansi ansi = ansi().bold();
            ContainerModule.Builder home = new ContainerModule.Builder("home");
            ContainerModule.Builder onExit = new ContainerModule.Builder("exit");

            try(IOCapture io = new IOCapture()) {
                ApplicationModule.Builder appBuilder = new ApplicationModule.Builder("test-app")
                        .setApplication(setApp)
                        .setAnsi(ansi)
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .setHome(home)
                        .enableAnsi(false)
                        .setOnExit(onExit);

                ApplicationModule app1 = appBuilder.build();
                ApplicationModule app2 = appBuilder.build();

                assertTrue(app1.structuralEquals(app2));
            }
        }
    }

    // helper methods

    /**
     * <p>Depending on the system, a newline is either \n or \r\n.</p>
     * <p>This method will take multiple strings and join them with <br> <i>System.lineSeparator()</i>. <br>
     * It will also add a new System.lineSeparator() at the end.</p>
     * <p>e.g. <code>lines("Hello", "World")</code> on Windows will produce: <code>"Hello\r\nWorld\r\n"</code>.</p>
     * @param lines The strings to join into lines
     * @return A single string that has joined the inputted string into lines.
     */
    public static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}