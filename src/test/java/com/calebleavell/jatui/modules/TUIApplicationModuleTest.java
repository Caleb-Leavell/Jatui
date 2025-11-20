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

import com.calebleavell.jatui.IOCapture;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TUIApplicationModuleTest {

    @Test
    void testRun_default_exit() {
        String expected = lines("Exiting...");

        try(IOCapture io = new IOCapture()) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(new TUITextModule.Builder("hello-world", "Hello, World!"))
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUITextInputModule.Builder("input", "What is your name? "),
                            new TUIModuleFactory.LineBuilder("output")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUIFunctionModule.Builder("5+5", () -> 5 + 5),
                            new TUIModuleFactory.LineBuilder("output").addModuleOutput("5+5").newLine()
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
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUITextInputModule.Builder("input", "What is your name? "))
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUIFunctionModule.Builder("input", () -> expected))
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
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

        app.forceUpdateInput("input", 5);

        assertAll(
                () -> assertEquals(5, app.getInputOrDefault("input", Integer.class, 5)),
                () -> assertEquals(2, app.getInputOrDefault("nothing", Integer.class, 2)),
                () -> assertEquals("default", app.getInputOrDefault("input", String.class, "default"))
        );
    }


    @Test
    void testUpdateInput_no_module() {
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
        app.updateInput("input", 5);
        assertNull(app.getInput("input"));
    }

    @Test
    void testUpdateInput_module_name() {
        int expected = 5;

        TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                .addChild(TUIModuleFactory.empty("input"))
                .build();
        app.updateInput("input", expected);
        assertAll(
                () -> assertEquals(expected, app.getInput("input")),
                () -> assertEquals(expected, app.getInput("input", Integer.class)));
    }

    @Test
    void testUpdateInput_module_object() {
        int expected = 5;

        TUIContainerModule.Builder input = TUIModuleFactory.empty("input");

        TUIApplicationModule app = new TUIApplicationModule.Builder("app")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .setAnsi(ansi().bold()) // set to non-default value to test against home ansi
                    .setPrintStream(io.getPrintStream())
                    .setScanner(io.getScanner()) //also set to test against home ansi
                    .enableAnsi(false)
                    .build();

            TUITextModule.Builder home = new TUITextModule.Builder("home", "Hello, World!");

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
        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home");
        TUIApplicationModule app = new TUIApplicationModule.Builder("test-app").build();
        app.setHome(home);
        assertEquals(app.getHome(), home);
    }

    @Test
    void testSetOnExit() {
        String expected = lines(
                "this should come first",
                "test exit!");

        try(IOCapture io = new IOCapture()) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .setAnsi(ansi().bold()) // all properties set to non-default values to test against the onExit module
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUITextModule.Builder onExit = new TUITextModule.Builder("exit", "test exit!");

            app.setOnExit(onExit);

            // we add this manually after setOnExit to make sure it doesn't get added after the exit
            app.getChildren().add(
                    new TUITextModule.Builder("text", "this should come first")
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
        TUIContainerModule.Builder onExit = new TUIContainerModule.Builder("exit");
        TUIApplicationModule app = new TUIApplicationModule.Builder("test-app").build();
        app.setOnExit(onExit);
        assertEquals(app.getOnExit(), onExit);
    }

    @Test
    void equalsTest() {
        TUIContainerModule.Builder exit = new TUIContainerModule.Builder("exit");
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();

        TUIApplicationModule app1 = new TUIApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        TUIApplicationModule app2 = new TUIApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        TUIApplicationModule app3 = new TUIApplicationModule.Builder("app")
                .setApplication(testApp)
                .setOnExit(exit.getCopy())
                .build();

        TUIApplicationModule app4 = new TUIApplicationModule.Builder("app")
                .setOnExit(exit.getCopy())
                .build();

        TUIApplicationModule app5 = new TUIApplicationModule.Builder("app")
                .setApplication(testApp)
                .build();

        assertAll(
            () -> assertTrue(app1.equals(app1)),
            () -> assertFalse(app1.equals(null)),
            () -> assertTrue(app1.equals(app2)),
            () -> assertTrue(app2.equals(app3)),
            () -> assertTrue(app1.equals(app3)),
            () -> assertFalse(app1.equals(app4)),
            () -> assertFalse(app1.equals(app5))

        );
    }

    @Nested
    class BuilderTest {
        @Test
        void getCopyTest() {

            var home = new TUIContainerModule.Builder("home");
            var onExit = new TUIContainerModule.Builder("onExit");
            TUIApplicationModule.Builder app = new TUIApplicationModule.Builder("test-app")
                    .setHome(home)
                    .setOnExit(onExit);

            TUIApplicationModule.Builder copy = app.getCopy();

            assertAll(
                    () -> assertTrue(app.equals(copy)),
                    () -> assertFalse(app.build().equals(copy.build())) // should be false since their respective applications will not have the same reference
            );

        }

        @Test
        void setOnExitTest() {
            var onExit = new TUIContainerModule.Builder("onExit");
            TUIApplicationModule.Builder app = new TUIApplicationModule.Builder("test-app")
                    .setOnExit(onExit);

            assertEquals(app.getOnExit(), onExit);
        }

        @Test
        void setHomeTest() {
            var home = new TUIContainerModule.Builder("home");
            TUIApplicationModule.Builder app = new TUIApplicationModule.Builder("test-app")
                    .setHome(home);

            assertEquals(app.getHome(), home);
        }

        @Test
        void buildTest_fields_equal() {
            TUIApplicationModule setApp = new TUIApplicationModule.Builder("setApp").build();
            Ansi ansi = ansi().bold();
            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home");
            TUIContainerModule.Builder onExit = new TUIContainerModule.Builder("exit");

            try(IOCapture io = new IOCapture()) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
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
            TUIApplicationModule setApp = new TUIApplicationModule.Builder("setApp").build();
            Ansi ansi = ansi().bold();
            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home");
            TUIContainerModule.Builder onExit = new TUIContainerModule.Builder("exit");

            try(IOCapture io = new IOCapture()) {
                TUIApplicationModule.Builder appBuilder = new TUIApplicationModule.Builder("test-app")
                        .setApplication(setApp)
                        .setAnsi(ansi)
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .setHome(home)
                        .enableAnsi(false)
                        .setOnExit(onExit);

                TUIApplicationModule app1 = appBuilder.build();
                TUIApplicationModule app2 = appBuilder.build();

                assertTrue(app1.equals(app2));
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