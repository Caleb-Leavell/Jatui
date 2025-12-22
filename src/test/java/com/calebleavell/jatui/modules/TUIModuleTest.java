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

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.util.IOCapture;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Using TUIContainerModule as the minimal implementation of this class for testing
 */
class TUIModuleTest {

    public static FunctionModule.Builder checkRunning(String name, TUIModule parent) {
        FunctionModule.Builder checkRunning = new FunctionModule.Builder(name, () -> {});
        checkRunning.setFunction(() -> parent.getCurrentRunningBranch().getLast().structuralEquals(checkRunning.build()));
        return checkRunning;
    }

    public static FunctionModule.Builder checkShallowRunning (String name, TUIModule parent) {
        FunctionModule.Builder checkRunning = new FunctionModule.Builder(name, () -> {});
        checkRunning.setFunction(() -> parent.getCurrentRunningChild().structuralEquals(checkRunning.build()));
        return checkRunning;
    }

    @Test
    void testRun_currentRunningChild() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        checkRunning("check-running-1", testApp),
                        checkRunning("check-running-2", testApp)
                );

        testApp.setHome(home);
        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-2", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testRunModuleAsChild() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        FunctionModule.Builder checkRunning = checkRunning("check-running-2", testApp);

        ApplicationModule otherApp = new ApplicationModule.Builder("other-app")
                .setHome(checkRunning)
                .build();

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        checkRunning("check-running-1", testApp),
                        new FunctionModule.Builder("run-other", () -> testApp.runModuleAsChild(checkRunning)),
                        checkRunning("check-running-3", testApp)
                );

        testApp.setHome(home);
        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(otherApp.getInput("check-running-2", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-3", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void getName() {
        ContainerModule test = new ContainerModule.Builder("test").build();
        assertEquals("test", test.getName());
    }

    @Test
    void getChildren() {
        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new ContainerModule.Builder("one"),
                        new ContainerModule.Builder("two"),
                        new ContainerModule.Builder("three")));

        ContainerModule parent = new ContainerModule.Builder("parent")
                .addChildren(children)
                .build();

        assertEquals(children, parent.getChildren());
    }

    @Test
    void testGetChild() {
        ContainerModule.Builder one_2 = new ContainerModule.Builder("one-2");
        ContainerModule.Builder three_1_1 = new ContainerModule.Builder("three-1-1");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new ContainerModule.Builder("one")
                                .addChildren(
                                        new ContainerModule.Builder("one-1"),
                                        one_2
                                ),
                        new ContainerModule.Builder("two"),
                        new ContainerModule.Builder("three")
                                .addChild(
                                        new ContainerModule.Builder("three-1")
                                                .addChild(three_1_1)
                                )));

        ContainerModule parent = new ContainerModule.Builder("parent")
                .addChildren(children)
                .build();

        assertAll(
                () -> assertEquals(children.getFirst(), parent.getChild("one")),
                () -> assertEquals( one_2, parent.getChild("one-2")),
                () -> assertEquals(three_1_1, parent.getChild("three-1-1")),
                () -> assertNull(parent.getChild("other"))
        );
    }

    @Test
    void testGetChild_with_class() {
        ContainerModule.Builder one_2 = new ContainerModule.Builder("one-2");
        TextModule.Builder three_1_1 = new TextModule.Builder("three-1-1", "hello!");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new ContainerModule.Builder("one")
                                .addChildren(
                                        new ContainerModule.Builder("one-1"),
                                        one_2
                                ),
                        new ContainerModule.Builder("two"),
                        new ContainerModule.Builder("three")
                                .addChild(
                                        new ContainerModule.Builder("three-1")
                                                .addChild(three_1_1)
                                )));

        ContainerModule parent = new ContainerModule.Builder("parent")
                .addChildren(children)
                .build();

        assertAll(
                () -> assertEquals(children.getFirst(), parent.getChild("one", ContainerModule.Builder.class)),
                () -> assertEquals( one_2, parent.getChild("one-2", ContainerModule.Builder.class)),
                () -> assertEquals(three_1_1, parent.getChild("three-1-1", TextModule.Builder.class)),
                () -> assertNull(parent.getChild("three-1-1", ContainerModule.Builder.class)),
                () -> assertNull(parent.getChild("other")),
                () -> assertNull(parent.getChild("other", ContainerModule.Builder.class))
        );
    }

    @Test
    void testTerminate() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("is-run-1", () -> true),
                        new FunctionModule.Builder("terminate", testApp::terminate),
                        new FunctionModule.Builder("is-run-2", () -> true)
                );

        testApp.setHome(home);
        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testTerminate_nested() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("is-run-1", () -> true),
                        new ContainerModule.Builder("container")
                                .addChildren(
                                        new FunctionModule.Builder("terminate", testApp::terminate),
                                        new FunctionModule.Builder("is-run-2", () -> true)
                                ),
                        new FunctionModule.Builder("is-run-3", () -> true)
                );


        testApp.setHome(home);
        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertNull(testApp.getInput("is-run-3")),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testTerminateChild() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("is-run-1", () -> true),
                        new ContainerModule.Builder("container")
                                .addChildren(
                                        new FunctionModule.Builder("terminate", () -> testApp.terminateChild("container")),
                                        new FunctionModule.Builder("is-run-2", () -> true)
                                ),
                        new FunctionModule.Builder("is-run-3", () -> true)
                );


        testApp.setHome(home);
        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertTrue(testApp.getInput("is-run-3", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testRestart() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        int[] count = {0, 0};

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("count-1", () -> count[0] = count[0] + 1),
                        new FunctionModule.Builder("restart", () -> {
                            if(count[0] == 1) app.restart();
                        }),
                        new FunctionModule.Builder("count-2", () -> count[1] = count[1] + 1)
                );

        app.setHome(home);
        app.run();

        assertAll(
                () -> assertEquals(2, count[0]),
                () -> assertEquals(1, count[1])
        );
    }

    @Test
    void testRestartNested() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("count-1", () -> count[0] = count[0] + 1),
                        new ContainerModule.Builder("container")
                                .addChildren(
                                        new FunctionModule.Builder("count-2", () -> count[1] = count[1] + 1),
                                        new FunctionModule.Builder("restart", () -> {
                                            if(count[0] == 1) app.restart();
                                        }),
                                        new FunctionModule.Builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        new FunctionModule.Builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.run();

        assertAll(
                () -> assertEquals(2, count[0]),
                () -> assertEquals(2, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testRestartMultipleTimes() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("count-1", () -> count[0] = count[0] + 1),
                        new ContainerModule.Builder("container")
                                .addChildren(
                                        new FunctionModule.Builder("count-2", () -> count[1] = count[1] + 1),
                                        new FunctionModule.Builder("restart", () -> {
                                            if(count[0] < 5) app.restart();
                                        }),
                                        new FunctionModule.Builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        new FunctionModule.Builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.run();

        assertAll(
                () -> assertEquals(5, count[0]),
                () -> assertEquals(5, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testRestartChild() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = new ContainerModule.Builder("home")
                .addChildren(
                        new FunctionModule.Builder("count-1", () -> count[0] = count[0] + 1),
                        new ContainerModule.Builder("container")
                                .addChildren(
                                        new FunctionModule.Builder("count-2", () -> count[1] = count[1] + 1),
                                        new FunctionModule.Builder("restart", () -> {
                                            if(count[1] == 1) app.restartChild("container");
                                        }),
                                        new FunctionModule.Builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        new FunctionModule.Builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.run();

        assertAll(
                () -> assertEquals(1, count[0]),
                () -> assertEquals(2, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testGetCurrentRunningChild() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();
        testApp.getChildren().clear();
        testApp.getChildren().addAll(
                List.of(
                        checkShallowRunning("check-running-1", testApp).setApplication(testApp),
                        checkShallowRunning("check-running-2", testApp).setApplication(testApp)));

        testApp.run();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-2", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testGetCurrentRunningBranch() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();
        ContainerModule.Builder home = new ContainerModule.Builder("home");

        ContainerModule.Builder nest_0 = new ContainerModule.Builder("nest-0");
        ContainerModule.Builder nest_1 = new ContainerModule.Builder("nest-1");
        FunctionModule.Builder nest_2 = new FunctionModule.Builder("nest-2", () -> {});
        nest_2.setFunction(() -> {
            List<TUIModule> runningBranch = testApp.getCurrentRunningBranch();
            List<TUIModule> expectedList = List.of(testApp, home.build(), nest_0.build(), nest_1.build(), nest_2.build());

            return IntStream.range(0, expectedList.size())
                    .allMatch(i -> runningBranch.get(i).structuralEquals(expectedList.get(i)));
        });

        home.addChildren(
                        new ContainerModule.Builder("empty-1"),
                        nest_0.addChild(nest_1.addChild(nest_2.addChild(new ContainerModule.Builder("empty_2")))),
                        new ContainerModule.Builder("empty_3")
                );

        testApp.setHome(home);
        testApp.run();

        assertTrue(testApp.getInput("nest-2", Boolean.class));
    }

    @Test
    void testGetApplication() {
        ApplicationModule testApp = new ApplicationModule.Builder("test-app").build();
        ContainerModule test = new ContainerModule.Builder("test").setApplication(testApp).build();
        assertEquals(testApp, test.getApplication());
    }

    @Test
    void testGetAnsi() {
        Ansi ansi = ansi().bold().fgRgb(50, 50, 50);
        ContainerModule test = new ContainerModule.Builder("test").setAnsi(ansi).build();
        assertEquals(ansi, test.getAnsi());
    }

    @Test
    void testGetScanner() {
        Scanner scnr = new Scanner(new ByteArrayInputStream("test".getBytes()));
        ContainerModule test = new ContainerModule.Builder("test").setScanner(scnr).build();
        assertEquals(scnr, test.getScanner());
    }

    @Test
    void getPrintStream() {
        PrintStream strm = new PrintStream(new ByteArrayOutputStream());
        ContainerModule test = new ContainerModule.Builder("test").setPrintStream(strm).build();
        assertEquals(strm, test.getPrintStream());
    }

    @Test
    void getAnsiEnabled() {
        ContainerModule.Builder test = new ContainerModule.Builder("test");
        boolean enabledBefore = test.build().getAnsiEnabled();
        test.enableAnsi(false);
        boolean enabledAfter = test.build().getAnsiEnabled();

        assertAll(
                () -> assertTrue(enabledBefore),
                () -> assertFalse(enabledAfter)
        );
    }

    @Test
    void testLogger() {

        ContainerModule module = new ContainerModule.Builder("module").build();
        module.run();
    }

    @Test
    void testStructuralEquals() {
        // Shared IOCapture for modules that should be equal
        IOCapture ioShared = new IOCapture();
        ApplicationModule app1 = new ApplicationModule.Builder("app1").build();

        // Base module
        ContainerModule module1 = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Exact copy: all properties same
        ContainerModule module2 = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Differences:

        // Different name
        ContainerModule moduleNameDiff = new ContainerModule.Builder("")
                .setName("different-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different application
        ApplicationModule app2 = new ApplicationModule.Builder("app2").build();
        ContainerModule moduleAppDiff = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app2)
                .build();

        // Different ANSI
        ContainerModule moduleAnsiDiff = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bgRgb(10, 10, 10))
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different ANSI enabled flag
        ContainerModule moduleAnsiEnabledDiff = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(true)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different print stream (new IOCapture)
        IOCapture ioOther = new IOCapture();
        ContainerModule modulePrintStreamDiff = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioOther.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different scanner (new IOCapture)
        ContainerModule moduleScannerDiff = new ContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioOther.getScanner())
                .setApplication(app1)
                .build();

        ioShared.close();
        ioOther.close();

        // Assertions
        assertAll(
                () -> assertTrue(module1.structuralEquals(module1), "Reflexive check"),
                () -> assertFalse(module1.structuralEquals(null), "Null check"),
                () -> assertTrue(module1.structuralEquals(module2), "Exact copy should be equal"),
                () -> assertFalse(module1.structuralEquals(moduleNameDiff), "Different name"),
                () -> assertFalse(module1.structuralEquals(moduleAppDiff), "Different application"),
                () -> assertFalse(module1.structuralEquals(moduleAnsiDiff), "Different ANSI"),
                () -> assertFalse(module1.structuralEquals(moduleAnsiEnabledDiff), "Different ANSI enabled flag"),
                () -> assertFalse(module1.structuralEquals(modulePrintStreamDiff), "Different print stream"),
                () -> assertFalse(module1.structuralEquals(moduleScannerDiff), "Different scanner")
        );

    }


    @Test
    void testStructuralEqualsWithChildren() {
        // Children
        ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
        ContainerModule.Builder child2 = new ContainerModule.Builder("child2");

        // Parent with two children
        ContainerModule parent1 = new ContainerModule.Builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Exact copy of parent
        ContainerModule parent2 = new ContainerModule.Builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Parent with a modified child
        ContainerModule.Builder child3 = new ContainerModule.Builder("child3");

        ContainerModule parent3 = new ContainerModule.Builder("parent")
                .addChild(child3.getCopy())
                .addChild(child2.getCopy())
                .build();

        assertAll(
                () -> assertTrue(parent1.structuralEquals(parent2), "Parents with identical children should be equal"),
                () -> assertFalse(parent1.structuralEquals(parent3), "Parents with one different child should not be equal")
        );
    }

    @Nested
    class BuilderTest {

        @Test
        void testSetNameAndGetName() {
            ContainerModule.Builder test = new ContainerModule.Builder("old-name");
            String oldName = test.getName();
            test.setName("new-name");
            String newName = test.getName();

            assertAll(
                    () -> assertEquals("old-name", oldName),
                    () -> assertEquals("new-name", newName)
            );
        }

        @Test
        void testPrependToName() {
            ContainerModule.Builder test = new ContainerModule.Builder("name");
            test.prependToName("new");
            assertEquals("new-name", test.getName());
        }

        @Test
        void testSetApplicationAndGetApplication() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");
            test.addChild(child);

            ApplicationModule oldApp = test.getApplication();
            test.setApplication(app);
            ApplicationModule newApp = test.getApplication();
            ApplicationModule childApp = child.getApplication();

            assertAll(
                    () -> assertNull(oldApp),
                    () -> assertEquals(app, newApp),
                    () -> assertEquals(app, childApp)
            );
        }

        @Test
        void testSetAnsiAndGetAnsi() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");
            test.addChild(child);

            Ansi oldAnsi  = test.getAnsi();
            test.setAnsi(ansi().bold());
            Ansi newAnsi = test.getAnsi();
            Ansi childAnsi = child.getAnsi();

            assertAll(
                    () -> assertEquals(ansi().toString(), oldAnsi.toString()),
                    () -> assertEquals(ansi().bold().toString(), newAnsi.toString()),
                    () -> assertEquals(ansi().bold().toString(), childAnsi.toString())
            );
        }

        @Test
        void testPrependAnsi() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child")
                    .setAnsi(ansi().bold().reset());
            test.addChild(child);

            Ansi oldAnsi = child.getAnsi();
            test.prependAnsi(ansi().fgRgb(3, 1, 4));
            Ansi newAnsi = test.getAnsi();
            Ansi childAnsi = child.getAnsi();

            assertAll(
                    () -> assertEquals(ansi().bold().reset().toString(), oldAnsi.toString()),
                    () -> assertEquals(ansi().fgRgb(3, 1, 4).toString(), newAnsi.toString()),
                    () -> assertEquals(ansi().a(ansi().fgRgb(3, 1, 4)).a(ansi().bold().reset()).toString(), childAnsi.toString())
            );
        }

        @Test
        void testAppendAnsi() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child")
                    .setAnsi(ansi().bold().reset());
            test.addChild(child);

            Ansi oldAnsi = child.getAnsi();
            test.appendAnsi(ansi().fgRgb(3, 1, 4));
            Ansi newAnsi = test.getAnsi();
            Ansi childAnsi = child.getAnsi();

            assertAll(
                    () -> assertEquals(ansi().bold().reset().toString(), oldAnsi.toString()),
                    () -> assertEquals(ansi().fgRgb(3, 1, 4).toString(), newAnsi.toString()),
                    () -> assertEquals(ansi().a(ansi().bold().reset()).a(ansi().fgRgb(3, 1, 4)).toString(), childAnsi.toString())
            );
        }

        @Test
        void testSetScannerAndGetScanner() {
            Scanner scanner = new Scanner("test");
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");
            test.addChild(child);

            Scanner oldScanner = test.getScanner();
            test.setScanner(scanner);
            Scanner newScanner = test.getScanner();
            Scanner childScanner = child.getScanner();

            assertAll(
                    () -> assertEquals(TUIModule.DEFAULT_SCANNER, oldScanner),
                    () -> assertEquals(scanner, newScanner),
                    () -> assertEquals(scanner, childScanner)
            );
        }

        @Test
        void testSetPrintStreamAndGetPrintStream() {
            PrintStream ps = new PrintStream(new ByteArrayOutputStream());
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");
            test.addChild(child);

            PrintStream oldPs = test.getPrintStream();
            test.setPrintStream(ps);
            PrintStream newPs = test.getPrintStream();
            PrintStream childPs = child.getPrintStream();

            assertAll(
                    () -> assertEquals(System.out, oldPs),
                    () -> assertEquals(ps, newPs),
                    () -> assertEquals(ps, childPs)
            );
        }

        @Test
        void testEnableAnsiAndGetAnsiEnabled() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");
            test.addChild(child);

            boolean oldAnsi = test.getAnsiEnabled();
            test.enableAnsi(false);
            boolean newAnsi = test.getAnsiEnabled();
            boolean childAnsi = child.getAnsiEnabled();

            assertAll(
                    () -> assertTrue(oldAnsi),
                    () -> assertFalse(newAnsi),
                    () -> assertFalse(childAnsi)
            );
        }

        @Test
        void testAddChild() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");
            test.addChild(child1);
            test.addChild(child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildAtIndex() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");
            test.addChild(child1);
            test.addChild(0, child2);
            assertEquals(List.of(child2, child1), test.getChildren());
        }

        @Test
        void testAddChildrenVarargs() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");
            test.addChildren(child1, child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildrenList() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");
            test.addChildren(new ArrayList<>(List.of(child1, child2)));
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testClearChildren() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");
            test.addChildren(child1, child2);
            test.clearChildren();
            assertEquals(List.of(), test.getChildren());
        }

        @Test
        void testGetChildByName() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child = new ContainerModule.Builder("child");

            test.addChildren(
                    new ContainerModule.Builder("a"),
                    test,
                    new ContainerModule.Builder("b")
                            .addChild(child),
                    new ContainerModule.Builder("child")
            );

            TUIModule.Builder<?> found = test.getChild("child");

            assertEquals(child, found);
        }

        @Test
        void testGetChildByNameAndType() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            TextModule.Builder child = new TextModule.Builder("child", "child");

            test.addChildren(
                    new ContainerModule.Builder("a"),
                    test,
                    new ContainerModule.Builder("b")
                            .addChild(child),
                    new ContainerModule.Builder("child")
            );

            TextModule.Builder found = test.getChild("child", TextModule.Builder.class);
            ContainerModule.Builder other = test.getChild("child", ContainerModule.Builder.class);

            assertAll(
                    () -> assertEquals(child, found),
                    () -> assertNull(other)
            );
        }

        @Test
        void testSetPropertyUpdateFlag() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.SKIP);

            test.setApplication(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertNull(child1.getApplication()),
                    () -> assertEquals(app, child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.SKIP, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testLockProperty() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.lockProperty(TUIModule.Property.APPLICATION);

            test.setApplication(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertNull(child1.getApplication()),
                    () -> assertNull(child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.HALT, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testUnlockProperty() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            ContainerModule.Builder test = new ContainerModule.Builder("test");
            ContainerModule.Builder child1 = new ContainerModule.Builder("child1");
            ContainerModule.Builder child2 = new ContainerModule.Builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.lockProperty(TUIModule.Property.APPLICATION);
            child1.unlockProperty(TUIModule.Property.APPLICATION);

            test.setApplication(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertEquals(app, child1.getApplication()),
                    () -> assertEquals(app, child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.UPDATE, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testUpdateProperties() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder populated = new ContainerModule.Builder("populated")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold());

            ContainerModule.Builder empty = new ContainerModule.Builder("empty");

            empty.updateProperties(populated.build());

            io.close();

            assertAll(
                    () -> assertEquals(app, empty.getApplication()),
                    () -> assertEquals(io.getScanner(), empty.getScanner()),
                    () -> assertEquals(io.getPrintStream(), empty.getPrintStream()),
                    () -> assertFalse(empty.getAnsiEnabled()),
                    () -> assertEquals(ansi().bold().toString(), empty.getAnsi().toString()),
                    () -> assertEquals("empty", empty.getName()) // name shouldn't be replaced
            );

        }

        @Test
        void testGetPropertyUpdateFlags() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");

            DirectedGraphNode.PropertyUpdateFlag oldFlag = test.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION);
            test.setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.SKIP);
            DirectedGraphNode.PropertyUpdateFlag newFlag = test.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION);

            assertAll(
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.UPDATE, oldFlag),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.SKIP, newFlag)
            );
        }

        @Test
        void testPropertyUpdateFlagDefaults() {
            ContainerModule.Builder test = new ContainerModule.Builder("test");

            boolean allUpdate = true;
            for(TUIModule.Property flag : test.getPropertyUpdateFlags().keySet()) {
                if(test.getPropertyUpdateFlags().get(flag) != DirectedGraphNode.PropertyUpdateFlag.UPDATE) {
                    allUpdate = false;
                }
            }

            assertTrue(allUpdate);

        }

        @Test
        void testShallowCopy() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder original = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder copied  = original.getCopy();

            io.close();

            assertAll(
                    () -> assertEquals(app, copied.getApplication()),
                    () -> assertEquals(io.getScanner(), copied.getScanner()),
                    () -> assertEquals(io.getPrintStream(), copied.getPrintStream()),
                    () -> assertFalse(copied.getAnsiEnabled()),
                    () -> assertEquals(ansi().bold().toString(), copied.getAnsi().toString()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT, copied.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION)),
                    () -> assertEquals("original", copied.getName())
            );
        }

        @Test
        void testGetDeepCopy() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder original = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChildren(
                            new ContainerModule.Builder("child1"),
                            new ContainerModule.Builder("child2")
                                    .setApplication(app),
                            new ContainerModule.Builder("child3")
                                    .addChild(new ContainerModule.Builder("child4")),
                            new ContainerModule.Builder("child5")
                                    .setAnsi(ansi().a("testAnsi"))
                    );

            original.getChild("child4").addChild(original);

            ContainerModule.Builder copied  = original.getCopy();

            io.close();

            assertTrue(copied.structuralEquals(original));
        }

        // equality more thoroughly tested in DirectedGraphNodeTest
        @Test
        void testShallowStructuralStructuralEquals() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder first = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder second = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder third = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder fourth = new ContainerModule.Builder("original")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder fifth = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder sixth = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder seventh = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder eighth = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder ninth = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold());

            io.close();

            assertAll(
                    () -> assertTrue(first.shallowStructuralEquals(first, first)),
                    () -> assertTrue(first.shallowStructuralEquals(first, second)),
                    () -> assertTrue(second.shallowStructuralEquals(first, third)),
                    () -> assertTrue(first.shallowStructuralEquals(first, third)),
                    () -> assertFalse(first.shallowStructuralEquals(first, fourth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, fifth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, sixth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, seventh)),
                    () -> assertFalse(first.shallowStructuralEquals(first, eighth)),
                    () -> assertFalse(first.shallowStructuralEquals(first, ninth))
            );
        }

        @Test
        void testStructuralEquals() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();

            ContainerModule.Builder first = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new ContainerModule.Builder("child"));

            ContainerModule.Builder second = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new ContainerModule.Builder("child"));

            ContainerModule.Builder third = new ContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new ContainerModule.Builder("child").enableAnsi(false));

            io.close();

            assertAll(
                    () -> assertTrue(TUIModule.Builder.structuralEquals(first, first)),
                    () -> assertTrue(TUIModule.Builder.structuralEquals(first, second)),
                    () -> assertTrue(TUIModule.Builder.structuralEquals(second, first)),
                    () -> assertFalse(TUIModule.Builder.structuralEquals(first, third)),

                    () -> assertTrue(first.structuralEquals(first)),
                    () -> assertTrue(first.structuralEquals(second)),
                    () -> assertTrue(second.structuralEquals(first)),
                    () -> assertFalse(first.structuralEquals(third))
            );
        }

        @Test
        void testBuild() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder child = new ContainerModule.Builder("child");

            TUIModule test = new ContainerModule.Builder("test")
                    .setApplication(app)
                    .addChild(child)
                    .setAnsi(ansi().bold())
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            io.close();

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertEquals(List.of(child), test.getChildren()),
                    () -> assertEquals(ansi().bold().toString(), test.getAnsi().toString()),
                    () -> assertEquals(io.getScanner(), test.getScanner()),
                    () -> assertEquals(io.getPrintStream(), test.getPrintStream()),
                    () -> assertFalse(test.getAnsiEnabled())
            );
        }

        @Test
        void testBuildRepeated() {
            ApplicationModule app = new ApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder child = new ContainerModule.Builder("child");

            ContainerModule.Builder builder = new ContainerModule.Builder("test")
                    .setApplication(app)
                    .addChild(child)
                    .setAnsi(ansi().bold())
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            TUIModule first = builder.build();
            TUIModule second = builder.build();

            io.close();

            assertTrue(first.structuralEquals(second));
        }
    }

}