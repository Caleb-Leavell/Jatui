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

import com.calebleavell.jatui.templates.TextChain;
import com.calebleavell.jatui.util.IOCapture;
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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .enableAnsi(false)
                    .printStream(io.getPrintStream())
                    .build();
            app.start();
            assertEquals(expected, io.getOutput());
        }
    }

    @Test
    void testRun_with_text() {
        String expected = lines("Hello, World!", "Exiting...");

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .addChildren(TextModule.builder("hello-world", "Hello, World!"))
                    .enableAnsi(false)
                    .printStream(io.getPrintStream())
                    .build();
            app.start();
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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .addChildren(
                            TextInputModule.builder("input", "What is your name? "),
                            TextChain.builder("output")
                                    .addText("Hello, ").addModuleOutput("input").addText("!").newLine())
                    .enableAnsi(false)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .build();

            app.start();
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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .addChildren(
                            FunctionModule.builder("5+5", () -> 5 + 5),
                            TextChain.builder("output").addModuleOutput("5+5").newLine()
                    )
                    .enableAnsi(false)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .build();

            app.start();
            assertEquals(expected, io.getOutput());
        }
    }

    @Test
    void testResetMemory() {
        ApplicationModule app = ApplicationModule.builder("app").build();

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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .addChildren(
                            TextInputModule.builder("input", "What is your name? "))
                    .enableAnsi(false)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .build();

            app.start();
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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .addChildren(
                            FunctionModule.builder("input", () -> expected))
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .build();

            app.start();
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
        ApplicationModule app = ApplicationModule.builder("app").build();

        app.forceUpdateInput("input", 5);

        assertAll(
                () -> assertEquals(5, app.getInputOrDefault("input", Integer.class, 5)),
                () -> assertEquals(2, app.getInputOrDefault("nothing", Integer.class, 2)),
                () -> assertEquals("default", app.getInputOrDefault("input", String.class, "default"))
        );
    }


    @Test
    void testUpdateInput_no_module() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        app.updateInput("input", 5);
        assertNull(app.getInput("input"));
    }

    @Test
    void testUpdateInput_module_name() {
        int expected = 5;

        ApplicationModule app = ApplicationModule.builder("app")
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

        ApplicationModule app = ApplicationModule.builder("app")
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
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .style(ansi().bold()) // set to non-default value to test against home ansi
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner()) //also set to test against home ansi
                    .enableAnsi(false)
                    .build();

            TextModule.Builder home = TextModule.builder("home", "Hello, World!");

            app.setHome(home);
            app.start();
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
        ContainerModule.Builder home = ContainerModule.builder("home");
        ApplicationModule app = ApplicationModule.builder("test-app").build();
        app.setHome(home);
        assertEquals(app.getHome(), home);
    }

    @Test
    void testSetOnExit() {
        String expected = lines(
                "this should come first",
                "test exit!");

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("test-app")
                    .style(ansi().bold()) // all properties set to non-default values to test against the onExit module
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TextModule.Builder onExit = TextModule.builder("exit", "test exit!");

            app.setOnExit(onExit);

            // we add this manually after setOnExit to make sure it doesn't get added after the exit
            app.getChildren().add(
                    TextModule.builder("text", "this should come first")
                            .printStream(io.getPrintStream())
                            .enableAnsi(false));

            app.start();

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
        ContainerModule.Builder onExit = ContainerModule.builder("exit");
        ApplicationModule app = ApplicationModule.builder("test-app").build();
        app.setOnExit(onExit);
        assertEquals(app.getOnExit(), onExit);
    }

    @Test
    void structuralEqualsTest() {
        ContainerModule.Builder exit = ContainerModule.builder("exit");
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        ApplicationModule app1 = ApplicationModule.builder("app")
                .application(testApp)
                .onExit(exit.getCopy())
                .build();

        ApplicationModule app2 = ApplicationModule.builder("app")
                .application(testApp)
                .onExit(exit.getCopy())
                .build();

        ApplicationModule app3 = ApplicationModule.builder("app")
                .application(testApp)
                .onExit(exit.getCopy())
                .build();

        ApplicationModule app4 = ApplicationModule.builder("app")
                .onExit(exit.getCopy())
                .build();

        ApplicationModule app5 = ApplicationModule.builder("app")
                .application(testApp)
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

            var home = ContainerModule.builder("home");
            var onExit = ContainerModule.builder("onExit");
            ApplicationModule.Builder app = ApplicationModule.builder("test-app")
                    .home(home)
                    .onExit(onExit);

            ApplicationModule.Builder copy = app.getCopy();

            assertAll(
                    () -> assertTrue(app.structuralEquals(copy)),
                    () -> assertFalse(app.build().structuralEquals(copy.build())) // should be false since their respective applications will not have the same reference
            );

        }

        @Test
        void setOnExitTest() {
            var onExit = ContainerModule.builder("onExit");
            ApplicationModule.Builder app = ApplicationModule.builder("test-app")
                    .onExit(onExit);

            assertEquals(app.getOnExit(), onExit);
        }

        @Test
        void setHomeTest() {
            var home = ContainerModule.builder("home");
            ApplicationModule.Builder app = ApplicationModule.builder("test-app")
                    .home(home);

            assertEquals(app.getHome(), home);
        }

        @Test
        void buildTest_fields_equal() {
            ApplicationModule setApp = ApplicationModule.builder("setApp").build();
            Ansi ansi = ansi().bold();
            ContainerModule.Builder home = ContainerModule.builder("home");
            ContainerModule.Builder onExit = ContainerModule.builder("exit");

            try(IOCapture io = new IOCapture()) {
                ApplicationModule app = ApplicationModule.builder("test-app")
                        .application(setApp)
                        .style(ansi)
                        .scanner(io.getScanner())
                        .printStream(io.getPrintStream())
                        .home(home)
                        .enableAnsi(false)
                        .onExit(onExit)
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
            ApplicationModule setApp = ApplicationModule.builder("setApp").build();
            Ansi ansi = ansi().bold();
            ContainerModule.Builder home = ContainerModule.builder("home");
            ContainerModule.Builder onExit = ContainerModule.builder("exit");

            try(IOCapture io = new IOCapture()) {
                ApplicationModule.Builder appBuilder = ApplicationModule.builder("test-app")
                        .application(setApp)
                        .style(ansi)
                        .scanner(io.getScanner())
                        .printStream(io.getPrintStream())
                        .home(home)
                        .enableAnsi(false)
                        .onExit(onExit);

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