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

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ModuleFactory;
import com.calebleavell.jatui.templates.PasswordInput;
import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PasswordInputTest {
    @Test
    void testCleanMemory() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> correct;

        try(IOCapture io = new IOCapture("my-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .storeInputAndMatch();

            app.setHome(myInput);
            app.run();

            char[] expectedInput = "my-password".toCharArray();
            char[] input = Arrays.copyOf(app.getInput("pw-input-input", char[].class), expectedInput.length);
            boolean isMatch = app.getInput("pw-input-is-matched", Boolean.class);

            myInput.cleanMemory();

            char[] expectedInputAfter = "           ".toCharArray();
            char[] inputAfter = app.getInput("pw-input-input", char[].class);
            Object isMatchAfter = app.getInput("pw-input-is-matched");

            assertAll(
                    () -> assertArrayEquals(expectedInput, input),
                    () -> assertFalse(isMatch),
                    () -> assertArrayEquals(expectedInputAfter, inputAfter),
                    () -> assertNull(isMatchAfter),
                    () -> assertSame(inputAfter, app.getInput("pw-input-input", char[].class))
            );
        }
    }

    @Test
    void testValidatePassword() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .storeInput();

            app.setHome(myInput);
            app.run();

            boolean isValid1 = myInput.validatePassword(correct);

            app.run();
            boolean isValid2 = myInput.validatePassword(correct);

            myInput.cleanImmediately();
            app.run();
            boolean isValid3 = myInput.validatePassword(correct);

            assertAll(
                    () -> assertFalse(isValid1),
                    () -> assertTrue(isValid2),
                    () -> assertFalse(isValid3)
            );
        }
    }

    @Test
    void testOnValidPassword() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password\nwrong-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnValidPassword("on-valid", () -> 5);

            app.setHome(myInput);
            app.run();
            Integer input1 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input2 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input3 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input4 = app.getInput("on-valid", Integer.class);

            assertAll(
                    () -> assertNull(input1),
                    () -> assertEquals(5, input2),
                    () -> assertEquals(5, input3),
                    () -> assertNull(input4)
            );
        }
    }

    @Test
    void testOnValidPasswordMultiple() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password\nwrong-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            Runnable addOne = () -> app.forceUpdateInput("on-valid", app.getInputOrDefault("on-valid", Integer.class, 0) + 1);
            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnValidPassword(addOne)
                    .addOnValidPassword(addOne);

            app.setHome(myInput);
            app.run();
            Integer input1 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input2 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input3 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input4 = app.getInput("on-valid", Integer.class);

            assertAll(
                    () -> assertNull(input1),
                    () -> assertEquals(2, input2),
                    () -> assertEquals(2, input3),
                    () -> assertNull(input4)
            );
        }
    }

    @Test
    void testOnInvalidPassword() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("correct-password\nwrong-password\nwrong-password\ncorrect-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnInvalidPassword("on-invalid", () -> 5);

            app.setHome(myInput);
            app.run();
            Integer input1 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input2 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input3 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input4 = app.getInput("on-invalid", Integer.class);

            assertAll(
                    () -> assertNull(input1),
                    () -> assertEquals(5, input2),
                    () -> assertEquals(5, input3),
                    () -> assertNull(input4)
            );
        }
    }

    @Test
    void testOnInvalidPasswordMultiple() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("correct-password\nwrong-password\nwrong-password\ncorrect-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            Runnable addOne = () -> app.forceUpdateInput("on-invalid", app.getInputOrDefault("on-invalid", Integer.class, 0) + 1);
            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnInvalidPassword(addOne)
                    .addOnInvalidPassword(addOne);

            app.setHome(myInput);
            app.run();
            Integer input1 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input2 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input3 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.run();
            Integer input4 = app.getInput("on-invalid", Integer.class);

            assertAll(
                    () -> assertNull(input1),
                    () -> assertEquals(2, input2),
                    () -> assertEquals(2, input3),
                    () -> assertNull(input4)
            );
        }
    }

    @Test
    void testOnValidAndOnInvalidPasswordMultiple() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

        try(IOCapture io = new IOCapture("correct-password\ncorrect-password\nwrong-password\nwrong-password\ncorrect-password")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            Runnable addOne = () -> app.forceUpdateInput("output", app.getInputOrDefault("output", Integer.class, 0) + 1);
            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnInvalidPassword(addOne)
                    .addOnValidPassword(addOne)
                    .addOnValidPassword(addOne)
                    .addOnInvalidPassword(addOne)
                    .addOnInvalidPassword(addOne)
                    .addOnValidPassword(addOne)
                    .addOnValidPassword(addOne);

            app.setHome(myInput);
            app.run();
            Integer input1 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.run();
            Integer input2 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.run();
            Integer input3 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.run();
            Integer input4 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.run();
            Integer input5 = app.getInput("output", Integer.class);

            assertAll(
                    () -> assertEquals(4, input1),
                    () -> assertEquals(4, input2),
                    () -> assertEquals(3, input3),
                    () -> assertEquals(3, input4),
                    () -> assertEquals(4, input5)
            );
        }
    }

    @Test
    void testSetName() {
        PasswordInput input = PasswordInput.builder("name-1", "text", null);
        input.setName("name-2");

        assertAll(
                () -> assertEquals("name-2", input.getName()),
                () -> assertNotNull(input.getChild("name-2-input")),
                () -> assertNull(input.getChild("name-1-input"))
        );
    }

    @Test
    void testSetDisplayText() {
        final char[] correct = "correct-password".toCharArray();
        Supplier<char[]> supplyCorrect = () -> correct;

        try(IOCapture io = new IOCapture("input1\ninput2")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("on-exit"))
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "text-1", supplyCorrect)
                    .storeInputAndMatch();

            app.setHome(myInput);
            app.run();
            String output1 = io.getOutput();
            myInput.setDisplayText("text-2");
            app.run();
            String output2 = io.getOutput();

            assertAll(
                    () -> assertEquals("text-1", output1),
                    () -> assertEquals("text-1text-2", output2)
            );
        }
    }

    @Test
    void testShallowShallowStructuralEquals() {
        char[] pw = {'a'};
        Supplier<char[]> first = () -> pw;
        Supplier<char[]> second = () -> pw;

        PasswordInput input1 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput input2 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();


        PasswordInput input3 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput input4 = PasswordInput.builder("pw-input", "other text: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput input5 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInput();

        PasswordInput input6 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeIfMatched();

        PasswordInput input7 = PasswordInput.builder("pw-input", "password: ", second)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput input8 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> System.out.println("different"))
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput input9 = PasswordInput.builder("pw-input", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> System.out.println("other"))
                .storeInputAndMatch();

        PasswordInput input10 = PasswordInput.builder("other-name", "password: ", first)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> System.out.println("other"))
                .storeInputAndMatch();

        assertAll(
                () -> assertTrue(input1.structuralEquals(input1)),
                () -> assertTrue(input1.structuralEquals(input2)),
                () -> assertTrue(input2.structuralEquals(input1)),
                () -> assertTrue(input2.structuralEquals(input3)),
                () -> assertTrue(input1.structuralEquals(input3)),
                () -> assertFalse(input1.structuralEquals(input4)),
                () -> assertFalse(input1.structuralEquals(input5)),
                () -> assertFalse(input1.structuralEquals(input6)),
                () -> assertTrue(input1.structuralEquals(input7)),
                () -> assertTrue(input1.structuralEquals(input8)),
                () -> assertTrue(input1.structuralEquals(input9)),
                () -> assertFalse(input1.structuralEquals(input10))
        );

    }

    @Test
    void testGetCopy() {
        char[] pw = {'a'};
        Supplier<char[]> sup = () -> pw;

        PasswordInput original = PasswordInput.builder("pw-input", "password: ", sup)
                .addOnInvalidPassword(() -> {})
                .addOnValidPassword(() -> {})
                .storeInputAndMatch();

        PasswordInput copy = original.getCopy();

        assertTrue(original.structuralEquals(copy));
    }
}