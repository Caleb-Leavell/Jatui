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
import com.calebleavell.jatui.templates.NumberedModuleSelector;
import com.calebleavell.jatui.util.IOCapture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

        NumberedModuleSelector original = new NumberedModuleSelector("list", app1)
                .addModule("text")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector copy = original.getCopy();

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

            NumberedModuleSelector selector = new NumberedModuleSelector("list", app)
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

        String expected = lines(
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

            NumberedModuleSelector original = new NumberedModuleSelector("list", app)
                    .addModule("goto text module", text);

            app.setHome(original);
            app.run();

            output = io.getOutput();
        }

        String expected = lines(
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

            NumberedModuleSelector selector = new NumberedModuleSelector("list", app)
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

        String expected = lines(
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

            NumberedModuleSelector original = new NumberedModuleSelector("list", app)
                    .addModule(text);

            app.setHome(original);
            app.run();

            output = io.getOutput();
        }

        String expected = lines(
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
                            new NumberedModuleSelector("list", app)
                                    .addModule(text)
                                    .addModule("restart", "home")
                                    .addModule("exit", ModuleFactory.terminate("terminate-app", app))
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        String expected = lines(
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

        ApplicationModule app2 = new ApplicationModule.Builder("app2")
                .addChildren(
                        new TextModule.Builder("text", "Hello, World!"),
                        new TextInputModule.Builder("input", "input: ")
                )
                .build();

        ContainerModule.Builder module = new ContainerModule.Builder("module");

        NumberedModuleSelector list1 = new NumberedModuleSelector("list", app1)
                .addModule("text")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector list2 = new NumberedModuleSelector("list", app1)
                .addModule("text")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector list3 = new NumberedModuleSelector("list", app1)
                .addModule("text")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector list4 = new NumberedModuleSelector("list", app2)
                .addModule("text")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector list5 = new NumberedModuleSelector("list", app1)
                .addModule("input")
                .addModule(module)
                .addModule("the module", module);

        NumberedModuleSelector list6 = new NumberedModuleSelector("list", app1)
                .addModule("text")
                .addModule(module)
                .addModule("other display text", module);

        NumberedModuleSelector list7 = new NumberedModuleSelector("other name", app1)
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

    /**
     * <p>Depending on the system, a newline is either \n or \r\n.</p>
     * <p>This method will take multiple strings and join them with <br> <i>System.lineSeparator()</i>. <br>
     * It will also add a new System.lineSeparator() at the end.</p>
     * <p>e.g. <code>lines("Hello", "World")</code> on Windows will produce: <code>"Hello\r\nWorld\r\n"</code>.</p>
     * @param lines The strings to join into lines
     * @return A single string that has joined the inputted string into lines.
     */
    public static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
