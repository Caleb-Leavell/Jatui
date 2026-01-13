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

import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberedListTest {

    @Test
    void testCopy() {
        NumberedList original = NumberedList.builder("list")
                .start(5)
                .step(3)
                .addListText("item1", "item2");

        NumberedList copy = original.getCopy();

        assertTrue(original.structuralEquals(copy));
    }

    @Test
    void testAddListTextSingle() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = NumberedList.builder("list")
                    .addListText("item1")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n"), output);
    }

    @Test
    void testAddListTextMultiple() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = NumberedList.builder("list")
                    .addListText("item1", "item2")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n[2] item2%n"), output);
    }

    @Test
    void testSetStart() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = NumberedList.builder("list")
                    .start(5)
                    .addListText("item1")
                    .addListText("item2")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("[5] item1%n[6] item2%n"), output);
    }

    @Test
    void testSetStep() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = NumberedList.builder("list")
                    .step(3)
                    .addListText("item1")
                    .addListText("item2")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n[4] item2%n"), output);
    }

    @Test
    void testSetStartAndStep() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = NumberedList.builder("list")
                    .start(5)
                    .step(3)
                    .addListText("item1")
                    .addListText("item2")
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().start();

            output = io.getOutput();
        }

        assertEquals(String.format("[5] item1%n[8] item2%n"), output);
    }

    @Test
    void testShallowShallowStructuralEquals() {
        NumberedList list1 = NumberedList.builder("list")
                .start(5)
                .step(2)
                .addListText("text1", "text2");

        NumberedList list2 = NumberedList.builder("list")
                .start(5)
                .step(2)
                .addListText("text1", "text2");

        NumberedList list3 = NumberedList.builder("list")
                .start(5)
                .step(2)
                .addListText("text1", "text2");

        NumberedList list4 = NumberedList.builder("list")
                .start(5)
                .step(2)
                .addListText("text1", "text2", "text3");

        NumberedList list5 = NumberedList.builder("list")
                .start(6)
                .step(2)
                .addListText("text1", "text2");

        NumberedList list6 = NumberedList.builder("list")
                .start(5)
                .step(3)
                .addListText("text1", "text2");

        NumberedList list7 = NumberedList.builder("rename-super-name")
                .start(5)
                .step(2)
                .addListText("text1", "text2");

        assertAll(
                () -> assertTrue(list1.structuralEquals(list1)),
                () -> assertTrue(list1.structuralEquals(list2)),
                () -> assertTrue(list2.structuralEquals(list1)),
                () -> assertTrue(list2.structuralEquals(list3)),
                () -> assertTrue(list1.structuralEquals(list3)),
                () -> assertFalse(list1.structuralEquals(list4)),
                () -> assertFalse(list1.structuralEquals(list5)),
                () -> assertFalse(list1.structuralEquals(list6)),
                () -> assertFalse(list1.structuralEquals(list7))
        );
    }
}