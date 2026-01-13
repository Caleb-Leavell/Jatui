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

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ModuleFactory;
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .storeInputAndMatch();

            app.setHome(myInput);
            app.start();

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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .storeInput();

            app.setHome(myInput);
            app.start();

            boolean isValid1 = myInput.validatePassword(correct);

            app.start();
            boolean isValid2 = myInput.validatePassword(correct);

            myInput.cleanImmediately();
            app.start();
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnValidPassword("on-valid", () -> 5);

            app.setHome(myInput);
            app.start();
            Integer input1 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input2 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input3 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            Runnable addOne = () -> app.forceUpdateInput("on-valid", app.getInputOrDefault("on-valid", Integer.class, 0) + 1);
            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnValidPassword(addOne)
                    .addOnValidPassword(addOne);

            app.setHome(myInput);
            app.start();
            Integer input1 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input2 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input3 = app.getInput("on-valid", Integer.class);
            app.resetMemory();
            app.start();
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnInvalidPassword("on-invalid", () -> 5);

            app.setHome(myInput);
            app.start();
            Integer input1 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input2 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input3 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            Runnable addOne = () -> app.forceUpdateInput("on-invalid", app.getInputOrDefault("on-invalid", Integer.class, 0) + 1);
            PasswordInput myInput = PasswordInput.builder("pw-input", "password: ", supplyCorrect)
                    .addOnInvalidPassword(addOne)
                    .addOnInvalidPassword(addOne);

            app.setHome(myInput);
            app.start();
            Integer input1 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input2 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
            Integer input3 = app.getInput("on-invalid", Integer.class);
            app.resetMemory();
            app.start();
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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
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
            app.start();
            Integer input1 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.start();
            Integer input2 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.start();
            Integer input3 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.start();
            Integer input4 = app.getInput("output", Integer.class);
            app.resetMemory();
            app.start();
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
        input.name("name-2");

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
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .onExit(ModuleFactory.empty("on-exit"))
                    .build();

            PasswordInput myInput = PasswordInput.builder("pw-input", "text-1", supplyCorrect)
                    .storeInputAndMatch();

            app.setHome(myInput);
            app.start();
            String output1 = io.getOutput();
            myInput.setDisplayText("text-2");
            app.start();
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