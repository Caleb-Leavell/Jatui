package com.calebleavell.jatui.modules;

import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Scanner;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TUIApplicationModuleTest {


    @org.junit.jupiter.api.Test
    void testRun_default_exit() {
        String expected = lines("Exiting...");

        try(IOCapture io = new IOCapture("");) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .enableAnsiRecursive(false)
                    .setPrintStreamRecursive(io.getPrintStream())
                    .build();
            app.run();
            assertEquals(expected, io.getOutput());
        }
    }

    @org.junit.jupiter.api.Test
    void testRun_with_text() {
        String expected = lines("Hello, World!", "Exiting...");

        try(IOCapture io = new IOCapture("");) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(new TUITextModule.Builder("hello-world", "Hello, World!"))
                    .enableAnsiRecursive(false)
                    .setPrintStreamRecursive(io.getPrintStream())
                    .build();
            app.run();
            assertEquals(expected, io.getOutput());
        }

    }

    @org.junit.jupiter.api.Test
    void testRun_with_input() {
        String expected = lines(
                "What is your name? Hello, Bob!",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture("Bob");) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUITextInputModule.Builder("input", "What is your name? "),
                            new TUIModuleFactory.LineBuilder("output")
                                    .addText("Hello, ").addModuleOutput("input").addText("!").newLine())
                    .enableAnsiRecursive(false)
                    .setScannerRecursive(io.getScanner())
                    .setPrintStreamRecursive(io.getPrintStream())
                    .build();

            app.run();
            assertEquals(expected, io.getOutput());
        }


    }

    @org.junit.jupiter.api.Test
    void testRun_with_function() {
        String expected = lines(
                "10",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture("")) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUIFunctionModule.Builder("5+5", () -> {
                                return 5 + 5;
                            }),
                            new TUIModuleFactory.LineBuilder("output").addModuleOutput("5+5").newLine()
                    )
                    .enableAnsiRecursive(false)
                    .setScannerRecursive(io.getScanner())
                    .setPrintStreamRecursive(io.getPrintStream())
                    .build();

            app.run();
            assertEquals(expected, io.getOutput());
        }
    }


    @org.junit.jupiter.api.Test
    void testGetInput_input_module() {
        String expected = "Bob";

        try(IOCapture io = new IOCapture(expected);) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUITextInputModule.Builder("input", "What is your name? "))
                    .enableAnsiRecursive(false)
                    .setScannerRecursive(io.getScanner())
                    .setPrintStreamRecursive(io.getPrintStream())
                    .build();

            app.run();
            assertAll(
                    () -> assertEquals(expected, app.getInput("input")),
                    () -> assertEquals(expected, app.getInput("input", String.class)),
                    () -> assertNull(app.getInput("input", Integer.class)),
                    () -> assertNull(app.getInput("INPUT")),
                    () -> assertNull(app.getInput("random name")));

        }
    }

    @org.junit.jupiter.api.Test
    void testGetInput_function_module() {
        int expected = 5;

        try(IOCapture io = new IOCapture("");) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .addChildren(
                            new TUIFunctionModule.Builder("input", () -> {return expected;}))
                    .setScannerRecursive(io.getScanner())
                    .setPrintStreamRecursive(io.getPrintStream())
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

    @org.junit.jupiter.api.Test
    void testUpdateInput_no_module() {
        TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
        app.updateInput("input", 5);
        assertNull(app.getInput("input"));
    }

    @org.junit.jupiter.api.Test
    void testUpdateInput_module_name() {
        int expected = 5;

        TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                .addChild(TUIModuleFactory.Empty("input"))
                .build();
        app.updateInput("input", expected);
        assertAll(
                () -> assertEquals(expected, app.getInput("input")),
                () -> assertEquals(expected, app.getInput("input", Integer.class)));
    }

    @org.junit.jupiter.api.Test
    void testUpdateInput_module_object() {
        int expected = 5;

        TUIContainerModule.Builder input = TUIModuleFactory.Empty("input");

        TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                .addChild(input)
                .build();
        app.updateInput(input.build(), expected);
        assertAll(
                () -> assertEquals(expected, app.getInput("input")),
                () -> assertEquals(expected, app.getInput("input", Integer.class)));
    }

    @org.junit.jupiter.api.Test
    void testSetHome() {
        String expected = lines(
                "Hello, World!",
                "Exiting..."
        );

        try(IOCapture io = new IOCapture("")) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("test-app")
                    .enableAnsiRecursive(false)
                    .build();

            TUITextModule.Builder home = new TUITextModule.Builder("home", "Hello, World!")
                    .enableAnsiRecursive(false)
                    .setScannerRecursive(io.getScanner())
                    .setPrintStreamRecursive(io.getPrintStream());

            app.setHome(home);
            app.run();
            assertEquals(expected, io.getOutput());
        }
    }

    @org.junit.jupiter.api.Test
    void testGetHome() {
    }

    @org.junit.jupiter.api.Test
    void testSetOnExit() {
    }

    @org.junit.jupiter.api.Test
    void testGetOnExit() {
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
    static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}