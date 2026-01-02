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

import com.calebleavell.jatui.templates.NumberedList;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberedListTest {

    @Test
    void testCopy() {
        NumberedList original = new NumberedList("list")
                .setStart(5)
                .setStep(3)
                .addListText("item1", "item2");

        NumberedList copy = original.getCopy();

        assertTrue(original.structuralEquals(copy));
    }

    @Test
    void testAddListTextSingle() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = new NumberedList("list")
                    .addListText("item1")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n"), output);
    }

    @Test
    void testAddListTextMultiple() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = new NumberedList("list")
                    .addListText("item1", "item2")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n[2] item2%n"), output);
    }

    @Test
    void testSetStart() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = new NumberedList("list")
                    .setStart(5)
                    .addListText("item1")
                    .addListText("item2")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("[5] item1%n[6] item2%n"), output);
    }

    @Test
    void testSetStep() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = new NumberedList("list")
                    .setStep(3)
                    .addListText("item1")
                    .addListText("item2")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("[1] item1%n[4] item2%n"), output);
    }

    @Test
    void testSetStartAndStep() {
        String output;

        try(IOCapture io = new IOCapture()) {
            NumberedList list = new NumberedList("list")
                    .setStart(5)
                    .setStep(3)
                    .addListText("item1")
                    .addListText("item2")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            list.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("[5] item1%n[8] item2%n"), output);
    }

    @Test
    void testShallowShallowStructuralEquals() {
        NumberedList list1 = new NumberedList("list")
                .setStart(5)
                .setStep(2)
                .addListText("text1", "text2");

        NumberedList list2 = new NumberedList("list")
                .setStart(5)
                .setStep(2)
                .addListText("text1", "text2");

        NumberedList list3 = new NumberedList("list")
                .setStart(5)
                .setStep(2)
                .addListText("text1", "text2");

        NumberedList list4 = new NumberedList("list")
                .setStart(5)
                .setStep(2)
                .addListText("text1", "text2", "text3");

        NumberedList list5 = new NumberedList("list")
                .setStart(6)
                .setStep(2)
                .addListText("text1", "text2");

        NumberedList list6 = new NumberedList("list")
                .setStart(5)
                .setStep(3)
                .addListText("text1", "text2");

        NumberedList list7 = new NumberedList("rename-super-name")
                .setStart(5)
                .setStep(2)
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