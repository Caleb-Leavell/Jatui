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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

class TUITextModuleTest {

    @Test
    void testRunDisplayText() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TUITextModule text = new TUITextModule.Builder("test", "Test Text")
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
            TUITextModule text = new TUITextModule.Builder("test", "Test Text")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                    .addChildren(
                            new TUIFunctionModule.Builder("func", () -> 5),
                            new TUITextModule.Builder("test", "func")
                                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
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
        TUITextModule text = new TUITextModule.Builder("text", "Test Text").build();

        assertEquals("Test Text", text.getText());
    }

    @Test
    void testEquals() {
        TUITextModule first = new TUITextModule.Builder("name", "text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false) // setting one super field to non-default to ensure that's handled as well
                .build();

        TUITextModule second = new TUITextModule.Builder("name", "text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TUITextModule third = new TUITextModule.Builder("name", "text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TUITextModule fourth = new TUITextModule.Builder("name", "other-text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TUITextModule fifth = new TUITextModule.Builder("name", "text")
                .printNewLine(false)
                .enableAnsi(false)
                .build();

        TUITextModule sixth = new TUITextModule.Builder("name", "text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                .enableAnsi(false)
                .build();

        TUITextModule seventh = new TUITextModule.Builder("name", "text")
                .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
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
            TUITextModule.Builder original = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false); // setting one super field to non-default to ensure that's handled as well

            TUITextModule.Builder copy = original.createInstance();
            copy.shallowCopy(original);

            assertTrue(original.equalTo(copy));
        }

        @Test
        void testPrintNewLine() {
            String output;
            boolean before;
            boolean after;

            try(IOCapture io = new IOCapture()) {
                TUITextModule.Builder text = new TUITextModule.Builder("text", "Test Text")
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
            TUITextModule.OutputType before;
            TUITextModule.OutputType after;

            TUITextModule.Builder text = new TUITextModule.Builder("text", "Test Text");

            before = text.getOutputType();
            text.setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT);
            after = text.getOutputType();
            text.build().run();


            assertAll(
                    () -> assertEquals(TUITextModule.OutputType.DISPLAY_TEXT, before),
                    () -> assertEquals(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT, after)
            );
        }

        @Test
        void testSetText() {
            String before;
            String after;

            TUITextModule.Builder text = new TUITextModule.Builder("text", "before");

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

            TUITextModule.Builder text = new TUITextModule.Builder("text", "before");

            before = text.getText();
            text.append("after");
            after = text.getText();

            assertAll(
                    () -> assertEquals("before", before),
                    () -> assertEquals("beforeafter", after)
            );
        }

        @Test
        void testEqualTo() {
            TUITextModule.Builder first = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false); // setting one super field to non-default to ensure that's handled as well

            TUITextModule.Builder second = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TUITextModule.Builder third = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TUITextModule.Builder fourth = new TUITextModule.Builder("name", "other-text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .enableAnsi(false);

            TUITextModule.Builder fifth = new TUITextModule.Builder("name", "text")
                    .printNewLine(false)
                    .enableAnsi(false);

            TUITextModule.Builder sixth = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .enableAnsi(false);

            TUITextModule.Builder seventh = new TUITextModule.Builder("name", "text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false);

            assertAll(
                    () -> assertTrue(first.equalTo(first, first)),
                    () -> assertTrue(first.equalTo(first, second)),
                    () -> assertTrue(first.equalTo(second, first)),
                    () -> assertTrue(fourth.equalTo(second, first)), // doing fourth.equals to make sure it doesn't matter
                    () -> assertTrue(first.equalTo(second, third)),
                    () -> assertTrue(first.equalTo(first, third)),
                    () -> assertFalse(first.equalTo(first, fourth)),
                    () -> assertFalse(first.equalTo(first, fifth)),
                    () -> assertFalse(first.equalTo(first, sixth)),
                    () -> assertFalse(first.equalTo(first, seventh))
            );
        }

        @Test
        void testBuild() {
            TUITextModule.Builder builder = new TUITextModule.Builder("text", "Test Text")
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false);

            TUITextModule first = builder.build();
            TUITextModule second = builder.build();

            assertAll(
                    () -> assertEquals("Test Text", first.getText()),
                    () -> assertEquals(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT, first.getOutputType()),
                    () -> assertFalse(first.getPrintNewLine()),
                    () -> assertTrue(first.equals(second))
            );
        }
    }
}