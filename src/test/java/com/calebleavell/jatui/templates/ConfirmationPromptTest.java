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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfirmationPromptTest {

    @Test
    void testSetValidConfirm() {
        ConfirmationPrompt confirm = ConfirmationPrompt.builder("name", "Are you sure? ")
                .validConfirm("1", "2", "3");

        assertEquals(Set.of("1", "2", "3"), confirm.getValidConfirm());
    }

    @Test
    void testSetValidDeny() {
        ConfirmationPrompt deny = ConfirmationPrompt.builder("name", "Are you sure? ")
                .validDeny("1", "2", "3");

        assertEquals(Set.of("1", "2", "3"), deny.getValidDeny());
    }

    @Test
    void testAddOnConfirmRunnable() {
        String output;

        try(IOCapture io = new IOCapture("yes")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

            app.setHome(confirm);
            app.start();

            output = io.getOutput();
        }

        assertEquals("Are you sure? confirmed", output);
    }

    @Test
    void testAddOnConfirmRunnableWithDifferentConfirm() {
        String output;

        try(IOCapture io = new IOCapture("yes\nyeah")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .validConfirm("yeah")
                    .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

            app.setHome(confirm);
            app.start();

            output = io.getOutput();
        }

        assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
    }

    @Test
    void testAddOnConfirmMultipleRunnableWithDifferentConfirm() {
        String output;

        try(IOCapture io = new IOCapture("yes\nyeah")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .validConfirm("yeah")
                    .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

            app.setHome(confirm);
            app.start();

            output = io.getOutput();
        }

        assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
    }

    @Test
    void testAddOnConfirmMultipleSupplierWithDifferentConfirm() {
        String output;

        try(IOCapture io = new IOCapture("yes\nyeah")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .validConfirm("yeah")
                    .addOnConfirm("on-confirm", () -> "confirmed");

            app.setHome(confirm);
            app.start();

            output = app.getInput("on-confirm").toString();
        }

        assertEquals("confirmed", output);
    }

    @Test
    void testAddOnConfirmMultipleSuppliersAndRunnables() {
        String output;
        String confirmed1;
        String confirmed2;

        try(IOCapture io = new IOCapture("yes\nyeah")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .validConfirm("yeah")
                    .addOnConfirm("on-confirm-1", () -> "confirmed")
                    .addOnConfirm(() -> io.getPrintStream().print("confirmed"))
                    .addOnConfirm("on-confirm-2", () -> "confirmed-2");

            app.setHome(confirm);
            app.start();

            output = io.getOutput();
            confirmed1 = app.getInput("on-confirm-1").toString();
            confirmed2 = app.getInput("on-confirm-2").toString();
        }

        assertAll(
                () -> assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output),
                () -> assertEquals("confirmed", confirmed1),
                () -> assertEquals("confirmed-2", confirmed2)
        );
    }

    @Test
    void testAddOnDenyMultipleSuppliersAndRunnables() {
        String output;
        String denied1;
        String denied2;

        try(IOCapture io = new IOCapture("no\nnah")) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .onExit(ModuleFactory.empty("empty"))
                    .printStream(io.getPrintStream())
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .build();

            ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                    .validDeny("nah")
                    .addOnDeny("on-deny-1", () -> "denied")
                    .addOnDeny(() -> io.getPrintStream().print("denied"))
                    .addOnDeny("on-deny-2", () -> "denied-2");

            app.setHome(confirm);
            app.start();

            output = io.getOutput();
            denied1 = app.getInput("on-deny-1").toString();
            denied2 = app.getInput("on-deny-2").toString();
        }

        assertAll(
                () -> assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? denied"), output),
                () -> assertEquals("denied", denied1),
                () -> assertEquals("denied-2", denied2)
        );
    }

    @Test
    void testSetName() {
        ConfirmationPrompt confirm = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .name("new-confirm-name");

        assertAll(
                () -> assertEquals("new-confirm-name", confirm.getName()),
                () -> assertNotNull(confirm.getChild("new-confirm-name-input")),
                () -> assertNull(confirm.getChild("confirm-input"))
        );

    }

    @Test
    void testShallowShallowStructuralEquals() {
        ConfirmationPrompt prompt1 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt2 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt3 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt4 = ConfirmationPrompt.builder("confirm", "other text")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt5 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps", "new valid confirm")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt6 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not", "new valid deny")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt7 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm("different-on-confirm", () -> 0)
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt prompt8 = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny("different-on-deny", () -> 0);

        ConfirmationPrompt prompt9 = ConfirmationPrompt.builder("other-name", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        assertAll(
                () -> assertTrue(prompt1.structuralEquals(prompt1)),
                () -> assertTrue(prompt1.structuralEquals(prompt2)),
                () -> assertTrue(prompt2.structuralEquals(prompt1)),
                () -> assertTrue(prompt2.structuralEquals(prompt3)),
                () -> assertTrue(prompt1.structuralEquals(prompt3)),
                () -> assertFalse(prompt1.structuralEquals(prompt4)),
                () -> assertFalse(prompt1.structuralEquals(prompt5)),
                () -> assertFalse(prompt1.structuralEquals(prompt6)),
                () -> assertFalse(prompt1.structuralEquals(prompt7)),
                () -> assertFalse(prompt1.structuralEquals(prompt8)),
                () -> assertFalse(prompt1.structuralEquals(prompt9))
        );
    }

    @Test
    void testGetCopy() {
        ConfirmationPrompt original = ConfirmationPrompt.builder("confirm", "Are you sure? ")
                .validConfirm("mhm", "perhaps")
                .validDeny("not sure", "probably not")
                .addOnConfirm(() -> System.out.println("text"))
                .addOnDeny(() -> System.out.println("text"));

        ConfirmationPrompt copy = original.getCopy();

        assertTrue(original.structuralEquals(copy));
    }
}