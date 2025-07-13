package com.calebleavell.jatui.modules;

import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TUIApplicationModuleTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach // For JUnit 5, use @Before for JUnit 4
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach // For JUnit 5, use @After for JUnit 4
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @org.junit.jupiter.api.Test
    void testRun_empty() {
        String expected = "Exiting..."; // TODO - either strip ansi or figure out how to test with ansi
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();
        testApp.run();
        assertEquals(expected, outContent.toString());
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
}