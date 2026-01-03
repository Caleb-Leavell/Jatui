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
            TextChain original = TextChain.builder("lines")
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
            TextChain original = TextChain.builder("lines")
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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("empty"))
                    .setPrintStream(io.getPrintStream())
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
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("%s%s%n", ansi().a("Output: ").reset(), ansi().bold().a("5").reset()), output);
    }

    @Test
    void testAddModuleOutput() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("empty"))
                    .setPrintStream(io.getPrintStream())
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
            app.run();

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
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            original.build().run();

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