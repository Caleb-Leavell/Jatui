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
    void getInput() {

    }

    @org.junit.jupiter.api.Test
    void testGetInput() {
    }

    @org.junit.jupiter.api.Test
    void updateInput() {
    }

    @org.junit.jupiter.api.Test
    void testUpdateInput() {
    }

    @org.junit.jupiter.api.Test
    void setHome() {
    }

    @org.junit.jupiter.api.Test
    void getHome() {
    }

    @org.junit.jupiter.api.Test
    void setOnExit() {
    }

    @org.junit.jupiter.api.Test
    void getOnExit() {
    }

    @org.junit.jupiter.api.Test
    void terminateChild() {
    }

    static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}