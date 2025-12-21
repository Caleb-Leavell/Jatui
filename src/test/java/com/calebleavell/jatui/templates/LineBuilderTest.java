package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.modules.*;
import com.calebleavell.jatui.templates.LineBuilder;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class LineBuilderTest {

    @Test
    void testGetCopy() {
        LineBuilder original = new LineBuilder("lines")
                .addText("text1")
                .addText("text2");

        LineBuilder copy = original.getCopy();

        assertTrue(copy.structuralEquals(original));
    }

    @Test
    void testAddTextModule() {
        String output;

        try(IOCapture io = new IOCapture()) {
            LineBuilder original = new LineBuilder("lines")
                    .addText(new TextModule.Builder("text", "Hello, World!"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("Hello, World!%n"), output);
    }

    @Test
    void testAddTextWithAnsi() {
        String output;

        try(IOCapture io = new IOCapture()) {
            LineBuilder original = new LineBuilder("lines")
                    .addText("Hello, World!", ansi().bold())
                    .setPrintStream(io.getPrintStream());

            original.build().run();

            output = io.getOutput();
        }

        assertEquals(ansi().bold().a("Hello, World!").reset().toString(), output);
    }

    @Test
    void testAddText() {
        String output;

        try(IOCapture io = new IOCapture()) {
            LineBuilder original = new LineBuilder("lines")
                    .addText("Hello, World!")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().run();

            output = io.getOutput();
        }

        assertEquals("Hello, World!", output);
    }

    @Test
    void testAddModuleOutputWithAnsi() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("empty"))
                    .setPrintStream(io.getPrintStream())
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new FunctionModule.Builder("five", () -> 5),
                            new LineBuilder("display-five")
                                    .addText("Output: ")
                                    .addModuleOutput("five", ansi().bold())
                                    .newLine()
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("%s%s%n", ansi().a("Output: ").reset(), ansi().bold().a("5").reset()), output);
    }

    @Test
    void testAddModuleOutput() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("empty"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new FunctionModule.Builder("five", () -> 5),
                            new LineBuilder("display-five")
                                    .addText("Output: ")
                                    .addModuleOutput("five")
                                    .newLine()
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("Output: 5%n"), output);
    }

    @Test
    void testNewLine() {
        String output;

        try(IOCapture io = new IOCapture()) {
            LineBuilder original = new LineBuilder("lines")
                    .addText("Hello,")
                    .newLine()
                    .addText("World!")
                    .newLine()
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("Hello,%nWorld!%n"), output);
    }

    @Test
    void testShallowStructuralStructuralEquals() {
        LineBuilder lines1 = new LineBuilder("lines")
                .addText("text1")
                .addText("text2");

        LineBuilder lines2 = new LineBuilder("lines")
                .addText("text1")
                .addText("text2");

        LineBuilder lines3 = new LineBuilder("lines")
                .addText("text1")
                .addText("text2");

        LineBuilder lines4 = new LineBuilder("lines")
                .addText("text1_other")
                .addText("text2");

        LineBuilder lines5 = new LineBuilder("lines")
                .addText("text1")
                .addText("text2")
                .addText("text3");

        LineBuilder lines6 = new LineBuilder("other")
                .addText("text1")
                .addText("text2");

        assertAll(
                () -> assertTrue(lines1.structuralEquals(lines1)),
                () -> assertTrue(lines1.structuralEquals(lines2)),
                () -> assertTrue(lines2.structuralEquals(lines3)),
                () -> assertTrue(lines1.structuralEquals(lines3)),
                () -> assertFalse(lines1.structuralEquals(lines4)),
                () -> assertFalse(lines1.structuralEquals(lines5)),
                () -> assertFalse(lines1.structuralEquals(lines6))
        );
    }
}