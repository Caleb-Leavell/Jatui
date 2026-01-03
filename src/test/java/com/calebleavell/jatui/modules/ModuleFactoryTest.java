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

import com.calebleavell.jatui.util.IOCapture;
import org.junit.jupiter.api.Test;

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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            TextModule.builder("text-1", "first"),
                            ModuleFactory.terminate("terminate-app", app),
                            TextModule.builder("text-2", "second")
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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            TextModule.builder("text-1", "first"),
                            ContainerModule.builder("group")
                                    .addChildren(
                                            TextModule.builder("text-2", "second"),
                                            ModuleFactory.terminate("terminate-group", "group", app),
                                            TextModule.builder("text-3", "third")
                                    ),
                            TextModule.builder("text-4", "fourth")
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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            TextModule.builder("text-1", "first"),
                            TextInputModule.builder("get-input", "input: ")
                                    .addSafeHandler("exit-if-d", s -> {
                                        if(s.equals("d")) app.terminate();
                                        return null;
                                    }),
                            ModuleFactory.restart("restart-app", app),
                            TextModule.builder("text-2", "second")
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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setOnExit(ModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            ContainerModule.Builder home = ContainerModule.builder("home")
                    .addChildren(
                            TextModule.builder("text-1", "first"),
                            ContainerModule.builder("group")
                                    .addChildren(
                                            TextModule.builder("text-2", "second"),
                                            TextInputModule.builder("get-input", "input: ")
                                                    .addSafeHandler("exit-if-d", s -> {
                                                        if(s.equals("d")) app.terminate();
                                                        return null;
                                                    }),
                                            ModuleFactory.restart("restart-group", app, "group"),
                                            TextInputModule.builder("test-3", "third")
                                    ),
                            TextModule.builder("text-4", "fourth")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%ninput: second%ninput: second%ninput: second%ninput: "), output);
    }

    @Test
    void testRunNameParent_ParentIsApplication() {
        String output;

        try(IOCapture io = new IOCapture()) {

            ApplicationModule app = ApplicationModule.builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("exit"))
                    .build();

            FunctionModule.Builder runText = ModuleFactory.run("run-group", app, "group");

            ContainerModule.Builder home = ContainerModule.builder("parent")
                    .addChildren(
                            TextModule.builder("text-1", "first"),
                            ContainerModule.builder("group")
                                    .addChildren(
                                            TextModule.builder("text-2", "second"),
                                            TextModule.builder("text-3", "third")
                                    ),
                            runText,
                            TextModule.builder("text-4", "fourth"));

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%nthird%nsecond%nthird%nfourth%n"), output);
    }

    @Test
    void testCounter() {
        String output;

        try(IOCapture io = new IOCapture()) {
            ApplicationModule app = ApplicationModule.builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("exit"))
                    .build();

            ContainerModule.Builder printToTen = ContainerModule.builder("print-to-ten")
                    .addChildren(
                            ModuleFactory.counter("counter", app),
                            TextModule.builder("display", "counter")
                                    .setOutputType(TextModule.OutputType.DISPLAY_APP_STATE),
                            FunctionModule.builder("exit-if-greater-than-10", () -> {
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
            ApplicationModule app = ApplicationModule.builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(ModuleFactory.empty("exit"))
                    .build();

            ContainerModule.Builder printToTen = ContainerModule.builder("print-to-ten")
                    .addChildren(
                            ModuleFactory.counter("counter", app, 5, 2),
                            TextModule.builder("display", "counter")
                                    .setOutputType(TextModule.OutputType.DISPLAY_APP_STATE),
                            FunctionModule.builder("exit-if-greater-than-10", () -> {
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

}