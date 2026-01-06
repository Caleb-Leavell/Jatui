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
 * Using ContainerModule as the minimal implementation of this class for testing
 */
class TUIModuleTest {

    public static FunctionModule.Builder checkRunning(String name, TUIModule parent) {
        FunctionModule.Builder checkRunning = FunctionModule.builder(name, () -> {});
        checkRunning.function(() -> parent.getCurrentRunningBranch().getLast().structuralEquals(checkRunning.build()));
        return checkRunning;
    }

    public static FunctionModule.Builder checkShallowRunning (String name, TUIModule parent) {
        FunctionModule.Builder checkRunning = FunctionModule.builder(name, () -> {});
        checkRunning.function(() -> parent.getCurrentRunningChild().structuralEquals(checkRunning.build()));
        return checkRunning;
    }

    @Test
    void testRun_currentRunningChild() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        checkRunning("check-running-1", testApp),
                        checkRunning("check-running-2", testApp)
                );

        testApp.setHome(home);
        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-2", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testNavigateTo() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        FunctionModule.Builder checkRunning = checkRunning("check-running-2", testApp);

        ApplicationModule otherApp = ApplicationModule.builder("other-app")
                .home(checkRunning)
                .build();

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        checkRunning("check-running-1", testApp),
                        FunctionModule.builder("run-other", () -> testApp.navigateTo(checkRunning)),
                        checkRunning("check-running-3", testApp)
                );

        testApp.setHome(home);
        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(otherApp.getInput("check-running-2", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-3", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void getName() {
        ContainerModule test = ContainerModule.builder("test").build();
        assertEquals("test", test.getName());
    }

    @Test
    void getChildren() {
        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        ContainerModule.builder("one"),
                        ContainerModule.builder("two"),
                        ContainerModule.builder("three")));

        ContainerModule parent = ContainerModule.builder("parent")
                .addChildren(children)
                .build();

        assertEquals(children, parent.getChildren());
    }

    @Test
    void testGetChild() {
        ContainerModule.Builder one_2 = ContainerModule.builder("one-2");
        ContainerModule.Builder three_1_1 = ContainerModule.builder("three-1-1");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        ContainerModule.builder("one")
                                .addChildren(
                                        ContainerModule.builder("one-1"),
                                        one_2
                                ),
                        ContainerModule.builder("two"),
                        ContainerModule.builder("three")
                                .addChild(
                                        ContainerModule.builder("three-1")
                                                .addChild(three_1_1)
                                )));

        ContainerModule parent = ContainerModule.builder("parent")
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
        ContainerModule.Builder one_2 = ContainerModule.builder("one-2");
        TextModule.Builder three_1_1 = TextModule.builder("three-1-1", "hello!");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        ContainerModule.builder("one")
                                .addChildren(
                                        ContainerModule.builder("one-1"),
                                        one_2
                                ),
                        ContainerModule.builder("two"),
                        ContainerModule.builder("three")
                                .addChild(
                                        ContainerModule.builder("three-1")
                                                .addChild(three_1_1)
                                )));

        ContainerModule parent = ContainerModule.builder("parent")
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
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("is-run-1", () -> true),
                        FunctionModule.builder("terminate", testApp::terminate),
                        FunctionModule.builder("is-run-2", () -> true)
                );

        testApp.setHome(home);
        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testTerminate_nested() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("is-run-1", () -> true),
                        ContainerModule.builder("container")
                                .addChildren(
                                        FunctionModule.builder("terminate", testApp::terminate),
                                        FunctionModule.builder("is-run-2", () -> true)
                                ),
                        FunctionModule.builder("is-run-3", () -> true)
                );


        testApp.setHome(home);
        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertNull(testApp.getInput("is-run-3")),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testTerminateChild() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("is-run-1", () -> true),
                        ContainerModule.builder("container")
                                .addChildren(
                                        FunctionModule.builder("terminate", () -> testApp.terminateChild("container")),
                                        FunctionModule.builder("is-run-2", () -> true)
                                ),
                        FunctionModule.builder("is-run-3", () -> true)
                );


        testApp.setHome(home);
        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("is-run-1", Boolean.class)),
                () -> assertNull(testApp.getInput("is-run-2")),
                () -> assertTrue(testApp.getInput("is-run-3", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testRestart() {
        ApplicationModule app = ApplicationModule.builder("app").build();

        int[] count = {0, 0};

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("count-1", () -> count[0] = count[0] + 1),
                        FunctionModule.builder("restart", () -> {
                            if(count[0] == 1) app.restart();
                        }),
                        FunctionModule.builder("count-2", () -> count[1] = count[1] + 1)
                );

        app.setHome(home);
        app.start();

        assertAll(
                () -> assertEquals(2, count[0]),
                () -> assertEquals(1, count[1])
        );
    }

    @Test
    void testRestartNested() {
        ApplicationModule app = ApplicationModule.builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("count-1", () -> count[0] = count[0] + 1),
                        ContainerModule.builder("container")
                                .addChildren(
                                        FunctionModule.builder("count-2", () -> count[1] = count[1] + 1),
                                        FunctionModule.builder("restart", () -> {
                                            if(count[0] == 1) app.restart();
                                        }),
                                        FunctionModule.builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        FunctionModule.builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.start();

        assertAll(
                () -> assertEquals(2, count[0]),
                () -> assertEquals(2, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testRestartMultipleTimes() {
        ApplicationModule app = ApplicationModule.builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("count-1", () -> count[0] = count[0] + 1),
                        ContainerModule.builder("container")
                                .addChildren(
                                        FunctionModule.builder("count-2", () -> count[1] = count[1] + 1),
                                        FunctionModule.builder("restart", () -> {
                                            if(count[0] < 5) app.restart();
                                        }),
                                        FunctionModule.builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        FunctionModule.builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.start();

        assertAll(
                () -> assertEquals(5, count[0]),
                () -> assertEquals(5, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testRestartChild() {
        ApplicationModule app = ApplicationModule.builder("app").build();

        int[] count = {0, 0, 0, 0};

        ContainerModule.Builder home = ContainerModule.builder("home")
                .addChildren(
                        FunctionModule.builder("count-1", () -> count[0] = count[0] + 1),
                        ContainerModule.builder("container")
                                .addChildren(
                                        FunctionModule.builder("count-2", () -> count[1] = count[1] + 1),
                                        FunctionModule.builder("restart", () -> {
                                            if(count[1] == 1) app.restartChild("container");
                                        }),
                                        FunctionModule.builder("count-3", () -> count[2] = count[2] + 1)
                                ),
                        FunctionModule.builder("count-4", () -> count[3] = count[3] + 1)
                );

        app.setHome(home);
        app.start();

        assertAll(
                () -> assertEquals(1, count[0]),
                () -> assertEquals(2, count[1]),
                () -> assertEquals(1, count[2]),
                () -> assertEquals(1, count[3])
        );
    }

    @Test
    void testGetCurrentRunningChild() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();
        testApp.getChildren().clear();
        testApp.getChildren().addAll(
                List.of(
                        checkShallowRunning("check-running-1", testApp).application(testApp),
                        checkShallowRunning("check-running-2", testApp).application(testApp)));

        testApp.start();

        assertAll(
                () -> assertTrue(testApp.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(testApp.getInput("check-running-2", Boolean.class)),
                () -> assertNull(testApp.getCurrentRunningChild())
        );
    }

    @Test
    void testGetCurrentRunningBranch() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();
        ContainerModule.Builder home = ContainerModule.builder("home");

        ContainerModule.Builder nest_0 = ContainerModule.builder("nest-0");
        ContainerModule.Builder nest_1 = ContainerModule.builder("nest-1");
        FunctionModule.Builder nest_2 = FunctionModule.builder("nest-2", () -> {});
        nest_2.function(() -> {
            List<TUIModule> runningBranch = testApp.getCurrentRunningBranch();
            List<TUIModule> expectedList = List.of(testApp, home.build(), nest_0.build(), nest_1.build(), nest_2.build());

            return IntStream.range(0, expectedList.size())
                    .allMatch(i -> runningBranch.get(i).structuralEquals(expectedList.get(i)));
        });

        home.addChildren(
                        ContainerModule.builder("empty-1"),
                        nest_0.addChild(nest_1.addChild(nest_2.addChild(ContainerModule.builder("empty_2")))),
                        ContainerModule.builder("empty_3")
                );

        testApp.setHome(home);
        testApp.start();

        assertTrue(testApp.getInput("nest-2", Boolean.class));
    }

    @Test
    void testGetApplication() {
        ApplicationModule testApp = ApplicationModule.builder("test-app").build();
        ContainerModule test = ContainerModule.builder("test").application(testApp).build();
        assertEquals(testApp, test.getApplication());
    }

    @Test
    void testGetAnsi() {
        Ansi ansi = ansi().bold().fgRgb(50, 50, 50);
        ContainerModule test = ContainerModule.builder("test").style(ansi).build();
        assertEquals(ansi, test.getAnsi());
    }

    @Test
    void testGetScanner() {
        Scanner scnr = new Scanner(new ByteArrayInputStream("test".getBytes()));
        ContainerModule test = ContainerModule.builder("test").scanner(scnr).build();
        assertEquals(scnr, test.getScanner());
    }

    @Test
    void getPrintStream() {
        PrintStream strm = new PrintStream(new ByteArrayOutputStream());
        ContainerModule test = ContainerModule.builder("test").printStream(strm).build();
        assertEquals(strm, test.getPrintStream());
    }

    @Test
    void getAnsiEnabled() {
        ContainerModule.Builder test = ContainerModule.builder("test");
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

        ContainerModule module = ContainerModule.builder("module").build();
        module.start();
    }

    @Test
    void testStructuralEquals() {
        // Shared IOCapture for modules that should be equal
        IOCapture ioShared = new IOCapture();
        ApplicationModule app1 = ApplicationModule.builder("app1").build();

        // Base module
        ContainerModule module1 = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Exact copy: all properties same
        ContainerModule module2 = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Differences:

        // Different name
        ContainerModule moduleNameDiff = ContainerModule.builder("")
                .name("different-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Different application
        ApplicationModule app2 = ApplicationModule.builder("app2").build();
        ContainerModule moduleAppDiff = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app2)
                .build();

        // Different ANSI
        ContainerModule moduleAnsiDiff = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bgRgb(10, 10, 10))
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Different ANSI enabled flag
        ContainerModule moduleAnsiEnabledDiff = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(true)
                .printStream(ioShared.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Different print stream (new IOCapture)
        IOCapture ioOther = new IOCapture();
        ContainerModule modulePrintStreamDiff = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioOther.getPrintStream())
                .scanner(ioShared.getScanner())
                .application(app1)
                .build();

        // Different scanner (new IOCapture)
        ContainerModule moduleScannerDiff = ContainerModule.builder("")
                .name("module-name")
                .style(ansi().bold())
                .enableAnsi(false)
                .printStream(ioShared.getPrintStream())
                .scanner(ioOther.getScanner())
                .application(app1)
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
        ContainerModule.Builder child1 = ContainerModule.builder("child1");
        ContainerModule.Builder child2 = ContainerModule.builder("child2");

        // Parent with two children
        ContainerModule parent1 = ContainerModule.builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Exact copy of parent
        ContainerModule parent2 = ContainerModule.builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Parent with a modified child
        ContainerModule.Builder child3 = ContainerModule.builder("child3");

        ContainerModule parent3 = ContainerModule.builder("parent")
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
            ContainerModule.Builder test = ContainerModule.builder("old-name");
            String oldName = test.getName();
            test.name("new-name");
            String newName = test.getName();

            assertAll(
                    () -> assertEquals("old-name", oldName),
                    () -> assertEquals("new-name", newName)
            );
        }

        @Test
        void testPrependToName() {
            ContainerModule.Builder test = ContainerModule.builder("name");
            test.prependToName("new");
            assertEquals("new-name", test.getName());
        }

        @Test
        void testSetApplicationAndGetApplication() {
            ApplicationModule app = ApplicationModule.builder("app").build();
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");
            test.addChild(child);

            ApplicationModule oldApp = test.getApplication();
            test.application(app);
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");
            test.addChild(child);

            Ansi oldAnsi  = test.getAnsi();
            test.style(ansi().bold());
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child")
                    .style(ansi().bold().reset());
            test.addChild(child);

            Ansi oldAnsi = child.getAnsi();
            test.prependStyle(ansi().fgRgb(3, 1, 4));
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child")
                    .style(ansi().bold().reset());
            test.addChild(child);

            Ansi oldAnsi = child.getAnsi();
            test.appendStyle(ansi().fgRgb(3, 1, 4));
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");
            test.addChild(child);

            Scanner oldScanner = test.getScanner();
            test.scanner(scanner);
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");
            test.addChild(child);

            PrintStream oldPs = test.getPrintStream();
            test.printStream(ps);
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");
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
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");
            test.addChild(child1);
            test.addChild(child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildAtIndex() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");
            test.addChild(child1);
            test.addChild(0, child2);
            assertEquals(List.of(child2, child1), test.getChildren());
        }

        @Test
        void testAddChildrenVarargs() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");
            test.addChildren(child1, child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildrenList() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");
            test.addChildren(new ArrayList<>(List.of(child1, child2)));
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testClearChildren() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");
            test.addChildren(child1, child2);
            test.clearChildren();
            assertEquals(List.of(), test.getChildren());
        }

        @Test
        void testGetChildByName() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child = ContainerModule.builder("child");

            test.addChildren(
                    ContainerModule.builder("a"),
                    test,
                    ContainerModule.builder("b")
                            .addChild(child),
                    ContainerModule.builder("child")
            );

            TUIModule.Builder<?> found = test.getChild("child");

            assertEquals(child, found);
        }

        @Test
        void testGetChildByNameAndType() {
            ContainerModule.Builder test = ContainerModule.builder("test");
            TextModule.Builder child = TextModule.builder("child", "child");

            test.addChildren(
                    ContainerModule.builder("a"),
                    test,
                    ContainerModule.builder("b")
                            .addChild(child),
                    ContainerModule.builder("child")
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
            ApplicationModule app = ApplicationModule.builder("app").build();
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.SKIP);

            test.application(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertNull(child1.getApplication()),
                    () -> assertEquals(app, child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.SKIP, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testLockProperty() {
            ApplicationModule app = ApplicationModule.builder("app").build();
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.lockProperty(TUIModule.Property.APPLICATION);

            test.application(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertNull(child1.getApplication()),
                    () -> assertNull(child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.HALT, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testUnlockProperty() {
            ApplicationModule app = ApplicationModule.builder("app").build();
            ContainerModule.Builder test = ContainerModule.builder("test");
            ContainerModule.Builder child1 = ContainerModule.builder("child1");
            ContainerModule.Builder child2 = ContainerModule.builder("child2");

            test.addChild(child1);
            child1.addChild(child2);

            child1.lockProperty(TUIModule.Property.APPLICATION);
            child1.unlockProperty(TUIModule.Property.APPLICATION);

            test.application(app);

            assertAll(
                    () -> assertEquals(app, test.getApplication()),
                    () -> assertEquals(app, child1.getApplication()),
                    () -> assertEquals(app, child2.getApplication()),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.UPDATE, child1.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION))
            );
        }

        @Test
        void testUpdateProperties() {
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder populated = ContainerModule.builder("populated")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold());

            ContainerModule.Builder empty = ContainerModule.builder("empty");

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
            ContainerModule.Builder test = ContainerModule.builder("test");

            DirectedGraphNode.PropertyUpdateFlag oldFlag = test.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION);
            test.updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.SKIP);
            DirectedGraphNode.PropertyUpdateFlag newFlag = test.getPropertyUpdateFlags().get(TUIModule.Property.APPLICATION);

            assertAll(
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.UPDATE, oldFlag),
                    () -> assertEquals(DirectedGraphNode.PropertyUpdateFlag.SKIP, newFlag)
            );
        }

        @Test
        void testPropertyUpdateFlagDefaults() {
            ContainerModule.Builder test = ContainerModule.builder("test");

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
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder original = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

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
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder original = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChildren(
                            ContainerModule.builder("child1"),
                            ContainerModule.builder("child2")
                                    .application(app),
                            ContainerModule.builder("child3")
                                    .addChild(ContainerModule.builder("child4")),
                            ContainerModule.builder("child5")
                                    .style(ansi().a("testAnsi"))
                    );

            original.getChild("child4").addChild(original);

            ContainerModule.Builder copied  = original.getCopy();

            io.close();

            assertTrue(copied.structuralEquals(original));
        }

        // equality more thoroughly tested in DirectedGraphNodeTest
        @Test
        void testShallowStructuralStructuralEquals() {
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder first = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder second = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder third = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder fourth = ContainerModule.builder("original")
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder fifth = ContainerModule.builder("original")
                    .application(app)
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder sixth = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder seventh = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder eighth = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            ContainerModule.Builder ninth = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold());

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
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();

            ContainerModule.Builder first = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(ContainerModule.builder("child"));

            ContainerModule.Builder second = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(ContainerModule.builder("child"));

            ContainerModule.Builder third = ContainerModule.builder("original")
                    .application(app)
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false)
                    .style(ansi().bold())
                    .updateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(ContainerModule.builder("child").enableAnsi(false));

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
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder child = ContainerModule.builder("child");

            TUIModule test = ContainerModule.builder("test")
                    .application(app)
                    .addChild(child)
                    .style(ansi().bold())
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
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
            ApplicationModule app = ApplicationModule.builder("app").build();
            IOCapture io = new IOCapture();
            ContainerModule.Builder child = ContainerModule.builder("child");

            ContainerModule.Builder builder = ContainerModule.builder("test")
                    .application(app)
                    .addChild(child)
                    .style(ansi().bold())
                    .scanner(io.getScanner())
                    .printStream(io.getPrintStream())
                    .enableAnsi(false);

            TUIModule first = builder.build();
            TUIModule second = builder.build();

            io.close();

            assertTrue(first.structuralEquals(second));
        }
    }

}