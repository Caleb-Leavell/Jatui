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

package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.modules.*;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TextChainTest {

    @Test
    void testGetCopy() {
        TextChain original = TextChain.builder("lines")
                .addText("text1")
                .addText("text2");

        TextChain copy = original.getCopy();

        assertTrue(copy.structuralEquals(original));
    }

    @Test
    void testAddTextModule() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextChain original = TextChain.builder("lines")
                    .addText(TextModule.builder("text", "Hello, World!"))
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("Hello, World!%n"), output);
    }

    @Test
    void testAddTextWithAnsi() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextChain original = TextChain.builder("lines")
                    .addText("Hello, World!", ansi().bold())
                    .printStream(io.getPrintStream());

            original.build().start();

            output = io.getOutput();
        }

        assertEquals(ansi().bold().a("Hello, World!").reset().toString(), output);
    }

    @Test
    void testAddText() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextChain original = TextChain.builder("lines")
                    .addText("Hello, World!")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().start();

            output = io.getOutput();
        }

        assertEquals("Hello, World!", output);
    }

    @Test
    void testAddModuleOutputWithAnsi() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            FunctionModule.builder("five", () -> 5),
                            TextChain.builder("display-five")
                                    .addText("Output: ")
                                    .addModuleOutput("five", ansi().bold())
                                    .newLine()
                    );

            app.setHome(home);
            app.start();

            output = io.getOutput();
        }

        assertEquals(String.format("%s%s%n", ansi().a("Output: ").reset(), ansi().bold().a("5").reset()), output);
    }

    @Test
    void testAddModuleOutput() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            FunctionModule.builder("five", () -> 5),
                            TextChain.builder("display-five")
                                    .addText("Output: ")
                                    .addModuleOutput("five")
                                    .newLine()
                    );

            app.setHome(home);
            app.start();

            output = io.getOutput();
        }

        assertEquals(String.format("Output: 5%n"), output);
    }

    @Test
    void testNewLine() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextChain original = TextChain.builder("lines")
                    .addText("Hello,")
                    .newLine()
                    .addText("World!")
                    .newLine()
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("Hello,%nWorld!%n"), output);
    }

    @Test
    void testShallowStructuralStructuralEquals() {
        TextChain lines1 = TextChain.builder("lines")
                .addText("text1")
                .addText("text2");

        TextChain lines2 = TextChain.builder("lines")
                .addText("text1")
                .addText("text2");

        TextChain lines3 = TextChain.builder("lines")
                .addText("text1")
                .addText("text2");

        TextChain lines4 = TextChain.builder("lines")
                .addText("text1_other")
                .addText("text2");

        TextChain lines5 = TextChain.builder("lines")
                .addText("text1")
                .addText("text2")
                .addText("text3");

        TextChain lines6 = TextChain.builder("other")
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