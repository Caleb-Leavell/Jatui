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

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ContainerModule;
import com.calebleavell.jatui.modules.FunctionModule;
import com.calebleavell.jatui.modules.TextModule;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TextModuleTest {

    @Test
    void testRunDisplayText() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextModule text = new TextModule.Builder("test", "Test Text")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            text.run();

            output = io.getOutput();
        }

        assertEquals(String.format("Test Text%n"), output);
    }

    @Test
    void testRunDisplayTextWithAnsi() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextModule text = new TextModule.Builder("test", "Test Text")
                    .setPrintStream(io.getPrintStream())
                    .setAnsi(ansi().bold().fgRgb(255, 0, 0))
                    .build();

            text.run();

            output = io.getOutput();
        }

        assertEquals(String.format("%sTest Text%s%n", ansi().bold().fgRgb(255, 0, 0), ansi().reset()), output);
    }

    @Test
    void testRunDisplayModuleOutput() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app").build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new FunctionModule.Builder("func", () -> 5),
                            new TextModule.Builder("test", "func")
                                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                                    .setPrintStream(io.getPrintStream())
                                    .enableAnsi(false)
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("5%n"), output);
    }

    @Test
    void testGetText() {
        TextModule text = new TextModule.Builder("text", "Test Text").build();

        assertEquals("Test Text", text.getText());
    }

    @Test
    void testEquals() {
        TextModule first = new TextModule.Builder("name", "text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false) // setting one super field to non-default to ensure that's handled as well
                .build();

        TextModule second = new TextModule.Builder("name", "text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TextModule third = new TextModule.Builder("name", "text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TextModule fourth = new TextModule.Builder("name", "other-text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TextModule fifth = new TextModule.Builder("name", "text")
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TextModule sixth = new TextModule.Builder("name", "text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .enableAnsi(false)
                .build();

        TextModule seventh = new TextModule.Builder("name", "text")
                .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .build();

        assertAll(
                () -> assertTrue(first.equals(first)),
                () -> assertTrue(first.equals(second)),
                () -> assertTrue(second.equals(first)),
                () -> assertTrue(second.equals(third)),
                () -> assertTrue(first.equals(third)),
                () -> assertFalse(first.equals(fourth)),
                () -> assertFalse(first.equals(fifth)),
                () -> assertFalse(first.equals(sixth)),
                () -> assertFalse(first.equals(seventh))
        );

    }

    @Nested
    class BuilderTest {
        @Test
        void testShallowCopy() {
            TextModule.Builder original = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false); // setting one super field to non-default to ensure that's handled as well

            TextModule.Builder copy = original.createInstance();
            copy.shallowCopy(original);

            assertTrue(original.structuralEquals(copy));
        }

        @Test
        void testPrintNewLine() {
            String output;
            boolean before;
            boolean after;

            try(IOCapture io = new IOCapture()) {
                TextModule.Builder text = new TextModule.Builder("text", "Test Text")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                before = text.getPrintNewLine();
                text.printNewLine(false);
                after = text.getPrintNewLine();
                text.build().run();

                output = io.getOutput();
            }

            assertAll(
                    () -> assertTrue(before),
                    () -> assertFalse(after),
                    () -> assertEquals("Test Text", output)
            );
        }

        @Test
        void testSetOutputType() {
            TextModule.OutputType before;
            TextModule.OutputType after;

            TextModule.Builder text = new TextModule.Builder("text", "Test Text");

            before = text.getOutputType();
            text.setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT);
            after = text.getOutputType();
            text.build().run();


            assertAll(
                    () -> assertEquals(TextModule.OutputType.DISPLAY_TEXT, before),
                    () -> assertEquals(TextModule.OutputType.DISPLAY_MODULE_OUTPUT, after)
            );
        }

        @Test
        void testSetText() {
            String before;
            String after;

            TextModule.Builder text = new TextModule.Builder("text", "before");

            before = text.getText();
            text.setText("after");
            after = text.getText();
            text.build().run();


            assertAll(
                    () -> assertEquals("before", before),
                    () -> assertEquals("after", after)
            );
        }

        @Test
        void testAppend() {
            String before;
            String after;

            TextModule.Builder text = new TextModule.Builder("text", "before");

            before = text.getText();
            text.append("after");
            after = text.getText();

            assertAll(
                    () -> assertEquals("before", before),
                    () -> assertEquals("beforeafter", after)
            );
        }

        @Test
        void testShallowStructuralStructuralEquals() {
            TextModule.Builder first = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false); // setting one super field to non-default to ensure that's handled as well

            TextModule.Builder second = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TextModule.Builder third = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TextModule.Builder fourth = new TextModule.Builder("name", "other-text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TextModule.Builder fifth = new TextModule.Builder("name", "text")
                    .printNewLine(false)
                    .enableAnsi(false);

            TextModule.Builder sixth = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .enableAnsi(false);

            TextModule.Builder seventh = new TextModule.Builder("name", "text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false);

            assertAll(
                    () -> assertTrue(first.shallowStructuralEquals(first, first)),
                    () -> assertTrue(first.shallowStructuralEquals(first, second)),
                    () -> assertTrue(first.shallowStructuralEquals(second, first)),
                    () -> assertTrue(fourth.shallowStructuralEquals(second, first)), // doing fourth.equals to make sure it doesn't matter
                    () -> assertTrue(first.shallowStructuralEquals(second, third)),
                    () -> assertTrue(first.shallowStructuralEquals(first, third)),
                    () -> assertFalse(first.shallowStructuralEquals(first, fourth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, fifth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, sixth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, seventh))
            );
        }

        @Test
        void testBuild() {
            TextModule.Builder builder = new TextModule.Builder("text", "Test Text")
                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false);

            TextModule first = builder.build();
            TextModule second = builder.build();

            assertAll(
                    () -> assertEquals("Test Text", first.getText()),
                    () -> assertEquals(TextModule.OutputType.DISPLAY_MODULE_OUTPUT, first.getOutputType()),
                    () -> assertFalse(first.getPrintNewLine()),
                    () -> assertTrue(first.equals(second))
            );
        }
    }
}