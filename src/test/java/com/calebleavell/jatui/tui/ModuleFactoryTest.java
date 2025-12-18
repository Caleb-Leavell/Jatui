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

package com.calebleavell.jatui.tui;

import com.calebleavell.jatui.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleFactoryTest {

    @Test
    void testEmpty() {
        ContainerModule empty;
        String output;

        try(IOCapture io = new IOCapture()) {
            empty = ModuleFactory.empty("empty")
                    .setPrintStream(io.getPrintStream())
                    .build();

            empty.run();
            output = io.getOutput();
        }

        assertAll(
                () -> assertEquals("", output),
                () -> assertEquals("empty", empty.getName()),
                () -> assertEquals(0, empty.getChildren().size())
        );
    }


    @Test
    void testTerminateModule() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            ModuleFactory.terminate("terminate-app", app),
                            new TextModule.Builder("text-2", "second")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%n"), output);
    }

    @Test
    void testTerminateNameParent() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            new ContainerModule.Builder("group")
                                    .addChildren(
                                            new TextModule.Builder("text-2", "second"),
                                            ModuleFactory.terminate("terminate-group", "group", app),
                                            new TextModule.Builder("text-3", "third")
                                    ),
                            new TextModule.Builder("text-4", "fourth")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%nfourth%n"), output);
    }

    @Test
    void testRestartModule() {
        String output;

        try(IOCapture io = new IOCapture("a\nb\nc\nd")) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            new TextInputModule.Builder("get-input", "input: ")
                                    .addSafeHandler("exit-if-d", s -> {
                                        if(s.equals("d")) app.terminate();
                                        return null;
                                    }),
                            ModuleFactory.restart("restart-app", app),
                            new TextModule.Builder("text-2", "second")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%ninput: first%ninput: first%ninput: first%ninput: "), output);
    }

    @Test
    void testRestartNameParent() {
        String output;

        try(IOCapture io = new IOCapture("a\nb\nc\nd")) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("home")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            new ContainerModule.Builder("group")
                                    .addChildren(
                                            new TextModule.Builder("text-2", "second"),
                                            new TextInputModule.Builder("get-input", "input: ")
                                                    .addSafeHandler("exit-if-d", s -> {
                                                        if(s.equals("d")) app.terminate();
                                                        return null;
                                                    }),
                                            ModuleFactory.restart("restart-group", app, "group"),
                                            new TextInputModule.Builder("test-3", "third")
                                    ),
                            new TextModule.Builder("text-4", "fourth")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%ninput: second%ninput: second%ninput: second%ninput: "), output);
    }

    @Test
    void testRunBuilder() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextModule.Builder text = new TextModule.Builder("text", "output")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            FunctionModule.Builder runText = ModuleFactory.run("run-text", text);

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("output%n"), output);
    }

    @Test
    void testRunModule() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TextModule text = new TextModule.Builder("text", "output")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            FunctionModule.Builder runText = ModuleFactory.run("run-text", text);

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("output%n"), output);
    }

    @Test
    void testRunNameParent() {
        String output;

        try(IOCapture io = new IOCapture()) {

            ContainerModule parent = new ContainerModule.Builder("parent")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            new ContainerModule.Builder("group")
                                    .addChildren(
                                            new TextModule.Builder("text-2", "second"),
                                            new TextModule.Builder("text-3", "third")
                                    ),
                            new TextModule.Builder("text-4", "fourth"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            FunctionModule.Builder runText = ModuleFactory.run("run-group", parent, "group");

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("second%nthird%n"), output);
    }

    @Test
    void testRunNameParent_ParentIsApplication() {
        String output;

        try(IOCapture io = new IOCapture()) {

            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = new ContainerModule.Builder("parent")
                    .addChildren(
                            new TextModule.Builder("text-1", "first"),
                            new ContainerModule.Builder("group")
                                    .addChildren(
                                            new TextModule.Builder("text-2", "second"),
                                            new TextModule.Builder("text-3", "third")
                                    ),
                            new TextModule.Builder("text-4", "fourth"));

            app.setHome(home);

            FunctionModule.Builder runText = ModuleFactory.run("run-group", app, "group");

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("second%nthird%n"), output);
    }

    @Test
    void testCounter() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("exit"))
                    .build();

            ContainerModule.Builder printToTen = new ContainerModule.Builder("print-to-ten")
                    .addChildren(
                            ModuleFactory.counter("counter", app),
                            new TextModule.Builder("display", "counter")
                                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT),
                            new FunctionModule.Builder("exit-if-greater-than-10", () -> {
                                int n = app.getInput("counter", Integer.class);

                                if(n >= 10) {
                                    app.terminate();
                                }
                            })
                    );

            printToTen.addChild(printToTen);

            app.setHome(printToTen);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("1%n2%n3%n4%n5%n6%n7%n8%n9%n10%n"), output);
    }

    @Test
    void testCounterBeginStep() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = new ApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("exit"))
                    .build();

            ContainerModule.Builder printToTen = new ContainerModule.Builder("print-to-ten")
                    .addChildren(
                            ModuleFactory.counter("counter", app, 5, 2),
                            new TextModule.Builder("display", "counter")
                                    .setOutputType(TextModule.OutputType.DISPLAY_MODULE_OUTPUT),
                            new FunctionModule.Builder("exit-if-greater-than-10", () -> {
                                int n = app.getInput("counter", Integer.class);

                                if(n >= 15) {
                                    app.terminate();
                                }
                            })
                    );

            printToTen.addChild(printToTen);

            app.setHome(printToTen);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("5%n7%n9%n11%n13%n15%n"), output);
    }

    @Nested
    class NumberedListTest {

        @Test
        void testCopy() {
            ModuleFactory.NumberedList original = new ModuleFactory.NumberedList("list", "item1", "item2")
                    .setStart(5)
                    .setStep(3);

            ModuleFactory.NumberedList copy = original.getCopy();

            assertTrue(original.structuralEquals(copy));
        }

        @Test
        void testAddListText() {
            String output;

            try(IOCapture io = new IOCapture()) {
                ModuleFactory.NumberedList list = new ModuleFactory.NumberedList("list", "item1")
                        .addListText("item2")
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
                ModuleFactory.NumberedList list = new ModuleFactory.NumberedList("list")
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
                ModuleFactory.NumberedList list = new ModuleFactory.NumberedList("list")
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
                ModuleFactory.NumberedList list = new ModuleFactory.NumberedList("list")
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
            ModuleFactory.NumberedList list1 = new ModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            ModuleFactory.NumberedList list2 = new ModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            ModuleFactory.NumberedList list3 = new ModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            ModuleFactory.NumberedList list4 = new ModuleFactory.NumberedList("list", "text1", "text2", "text3")
                    .setStart(5)
                    .setStep(2);

            ModuleFactory.NumberedList list5 = new ModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(6)
                    .setStep(2);

            ModuleFactory.NumberedList list6 = new ModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(3);

            ModuleFactory.NumberedList list7 = new ModuleFactory.NumberedList("rename-super-name", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

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

    @Nested
    class NumberedModuleSelectorTest {

        @Test
        void testCopy() {
            ApplicationModule app1 = new ApplicationModule.Builder("app")
                    .addChildren(
                            new TextModule.Builder("text", "Hello, World!"),
                            new TextInputModule.Builder("input", "input: ")
                    )
                    .build();

            ContainerModule.Builder module = new ContainerModule.Builder("module");

            ModuleFactory.NumberedModuleSelector original = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector copy = original.getCopy();

            assertTrue(copy.structuralEquals(original));
        }


        @Test
        void testAddSceneDisplayTextName() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(ModuleFactory.empty("empty"))
                        .build();

                ModuleFactory.NumberedModuleSelector selector = new ModuleFactory.NumberedModuleSelector("list", app)
                        .addModule("goto text module", "text");

                ContainerModule.Builder content = new ContainerModule.Builder("home")
                        .addChildren(
                                selector,
                                ModuleFactory.terminate("terminate-app", app),
                                new TextModule.Builder("text", "Hello, World!"),
                                new TextInputModule.Builder("input", "input: ")
                        )
                        .enableAnsi(false)
                        .setPrintStream(io.getPrintStream());

                app.setHome(content);
                app.run();

                output = io.getOutput();
            }

            String expected = ApplicationModuleTest.lines(
                    "[1] goto text module",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneDisplayTextModule() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(ModuleFactory.empty("empty"))
                        .build();

                TextModule.Builder text = new TextModule.Builder("text", "Hello, World!")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                ModuleFactory.NumberedModuleSelector original = new ModuleFactory.NumberedModuleSelector("list", app)
                        .addModule("goto text module", text);

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = ApplicationModuleTest.lines(
                    "[1] goto text module",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneName() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(ModuleFactory.empty("empty"))
                        .build();

                ModuleFactory.NumberedModuleSelector selector = new ModuleFactory.NumberedModuleSelector("list", app)
                        .addModule("text");

                ContainerModule.Builder content = new ContainerModule.Builder("home")
                        .addChildren(
                                selector,
                                ModuleFactory.terminate("terminate-app", app),
                                new TextModule.Builder("text", "Hello, World!"),
                                new TextInputModule.Builder("input", "input: ")
                        )
                        .enableAnsi(false)
                        .setPrintStream(io.getPrintStream());

                app.setHome(content);
                app.run();

                output = io.getOutput();
            }

            String expected = ApplicationModuleTest.lines(
                    "[1] text",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneModule() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(ModuleFactory.empty("empty"))
                        .build();

                TextModule.Builder text = new TextModule.Builder("text", "Hello, World!")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                ModuleFactory.NumberedModuleSelector original = new ModuleFactory.NumberedModuleSelector("list", app)
                        .addModule(text);

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = ApplicationModuleTest.lines(
                    "[1] text",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testCommonUseCase() {
            String output;

            try (IOCapture io = new IOCapture("1\n2\n3")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TextModule.Builder text = new TextModule.Builder("text", "Hello, World!")
                        .addChild(ModuleFactory.run("run-home", app, "home"))
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                ContainerModule.Builder home = new ContainerModule.Builder("home")
                        .addChildren(
                                new ModuleFactory.NumberedModuleSelector("list", app)
                                        .addModule(text)
                                        .addModule("restart", "home")
                                        .addModule("exit", ModuleFactory.terminate("terminate-app", app))
                        );

                app.setHome(home);
                app.run();

                output = io.getOutput();
            }

            String expected = ApplicationModuleTest.lines(
                    "[1] text",
                    "[2] restart",
                    "[3] exit",
                    "Your choice: Hello, World!",
                    "[1] text",
                    "[2] restart",
                    "[3] exit",
                    "Your choice: [1] text",
                    "[2] restart",
                    "[3] exit",
                    "Your choice: Exiting..."
            );

            assertEquals(expected, output);
        }

        @Test
        void testShallowShallowStructuralEquals() {
            ApplicationModule app1 = new ApplicationModule.Builder("app")
                    .addChildren(
                            new TextModule.Builder("text", "Hello, World!"),
                            new TextInputModule.Builder("input", "input: ")
                    )
                    .build();

            ApplicationModule app2 = new ApplicationModule.Builder("app")
                    .addChildren(
                            new TextModule.Builder("text", "Hello, World!"),
                            new TextInputModule.Builder("input", "input: ")
                    )
                    .build();

            ContainerModule.Builder module = new ContainerModule.Builder("module");

            ModuleFactory.NumberedModuleSelector list1 = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector list2 = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector list3 = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector list4 = new ModuleFactory.NumberedModuleSelector("list", app2)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector list5 = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("input")
                    .addModule(module)
                    .addModule("the module", module);

            ModuleFactory.NumberedModuleSelector list6 = new ModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("other display text", module);

            ModuleFactory.NumberedModuleSelector list7 = new ModuleFactory.NumberedModuleSelector("other name", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

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

    @Nested
    class LineBuilderTest {

        @Test
        void testGetCopy() {
            ModuleFactory.LineBuilder original = new ModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            ModuleFactory.LineBuilder copy = original.getCopy();

            assertTrue(copy.structuralEquals(original));
        }

        @Test
        void testAddTextModule() {
            String output;

            try(IOCapture io = new IOCapture()) {
                ModuleFactory.LineBuilder original = new ModuleFactory.LineBuilder("lines")
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
                ModuleFactory.LineBuilder original = new ModuleFactory.LineBuilder("lines")
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
                ModuleFactory.LineBuilder original = new ModuleFactory.LineBuilder("lines")
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
                                new ModuleFactory.LineBuilder("display-five")
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
                                new ModuleFactory.LineBuilder("display-five")
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
                ModuleFactory.LineBuilder original = new ModuleFactory.LineBuilder("lines")
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
            ModuleFactory.LineBuilder lines1 = new ModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            ModuleFactory.LineBuilder lines2 = new ModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            ModuleFactory.LineBuilder lines3 = new ModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            ModuleFactory.LineBuilder lines4 = new ModuleFactory.LineBuilder("lines")
                    .addText("text1_other")
                    .addText("text2");

            ModuleFactory.LineBuilder lines5 = new ModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2")
                    .addText("text3");

            ModuleFactory.LineBuilder lines6 = new ModuleFactory.LineBuilder("other")
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

    @Nested
    class ConfirmationPromptTest {

        @Test
        void testSetValidConfirm() {
            ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("name", "Are you sure? ")
                    .setValidConfirm("1", "2", "3");

            assertEquals(Set.of("1", "2", "3"), confirm.getValidConfirm());
        }

        @Test
        void testSetValidDeny() {
            ModuleFactory.ConfirmationPrompt deny = new ModuleFactory.ConfirmationPrompt("name", "Are you sure? ")
                    .setValidConfirm("1", "2", "3");

            assertEquals(Set.of("1", "2", "3"), deny.getValidConfirm());
        }

        @Test
        void testAddOnConfirmRunnable() {
            String output;

            try(IOCapture io = new IOCapture("yes")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals("Are you sure? confirmed", output);
        }

        @Test
        void testAddOnConfirmRunnableWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
        }

        @Test
        void testAddOnConfirmMultipleRunnableWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
        }

        @Test
        void testAddOnConfirmMultipleSupplierWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm("on-confirm", () -> "confirmed");

                app.setHome(confirm);
                app.run();

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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm("on-confirm-1", () -> "confirmed")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"))
                        .addOnConfirm("on-confirm-2", () -> "confirmed-2");

                app.setHome(confirm);
                app.run();

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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setOnExit(ModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidDeny("nah")
                        .addOnDeny("on-deny-1", () -> "denied")
                        .addOnDeny(() -> io.getPrintStream().print("denied"))
                        .addOnDeny("on-deny-2", () -> "denied-2");

                app.setHome(confirm);
                app.run();

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
            ModuleFactory.ConfirmationPrompt confirm = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setName("new-confirm-name");

            assertAll(
                    () -> assertEquals("new-confirm-name", confirm.getName()),
                    () -> assertNotNull(confirm.getChild("new-confirm-name-input")),
                    () -> assertNull(confirm.getChild("confirm-input"))
            );

        }

        @Test
        void testShallowShallowStructuralEquals() {
            ModuleFactory.ConfirmationPrompt prompt1 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt2 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt3 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt4 = new ModuleFactory.ConfirmationPrompt("confirm", "other text")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt5 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps", "new valid confirm")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt6 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not", "new valid deny")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt7 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm("different-on-confirm", () -> 0)
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt prompt8 = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny("different-on-deny", () -> 0);

            ModuleFactory.ConfirmationPrompt prompt9 = new ModuleFactory.ConfirmationPrompt("other-name", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
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
            ModuleFactory.ConfirmationPrompt original = new ModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            ModuleFactory.ConfirmationPrompt copy = original.getCopy();

            assertTrue(original.structuralEquals(copy));
        }
    }

    @Nested
    class PasswordInputTest {
        @Test
        void testCleanMemory() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> correct;

            try(IOCapture io = new IOCapture("my-password")) {
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("on-valid", app.getInputOrDefault("on-valid", Integer.class, 0) + 1);
                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("on-invalid", app.getInputOrDefault("on-invalid", Integer.class, 0) + 1);
                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("output", app.getInputOrDefault("output", Integer.class, 0) + 1);
                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
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
            ModuleFactory.PasswordInput input = new ModuleFactory.PasswordInput("name-1", "text", null);
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
                ApplicationModule app = new ApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(ModuleFactory.empty("on-exit"))
                        .build();

                ModuleFactory.PasswordInput myInput = new ModuleFactory.PasswordInput("pw-input", "text-1", supplyCorrect)
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

            ModuleFactory.PasswordInput input1 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input2 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();


            ModuleFactory.PasswordInput input3 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input4 = new ModuleFactory.PasswordInput("pw-input", "other text: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input5 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInput();

            ModuleFactory.PasswordInput input6 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeIfMatched();

            ModuleFactory.PasswordInput input7 = new ModuleFactory.PasswordInput("pw-input", "password: ", second)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input8 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> System.out.println("different"))
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input9 = new ModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> System.out.println("other"))
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput input10 = new ModuleFactory.PasswordInput("other-name", "password: ", first)
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

            ModuleFactory.PasswordInput original = new ModuleFactory.PasswordInput("pw-input", "password: ", sup)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            ModuleFactory.PasswordInput copy = original.getCopy();

            assertTrue(original.structuralEquals(copy));
        }
    }
}