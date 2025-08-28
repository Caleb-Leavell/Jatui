package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.hierarchical.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Using TUIContainerModule as the minimal implementation of this class for testing
 */
class TUIModuleTest {

    public static TUIFunctionModule.Builder checkRunning(String name, TUIModule parent) {
        TUIFunctionModule.Builder checkRunning = new TUIFunctionModule.Builder(name, () -> {});
        checkRunning.function(() -> {
            return parent.getCurrentRunningBranch().getLast().equals(checkRunning.build());
        });
        return checkRunning;
    }

    public static TUIFunctionModule.Builder checkShallowRunning (String name, TUIModule parent) {
        TUIFunctionModule.Builder checkRunning = new TUIFunctionModule.Builder(name, () -> {});
        checkRunning.function(() -> {
            return parent.getCurrentRunningChild().equals(checkRunning.build());
        });
        return checkRunning;
    }

    @Test
    void testRun_termination() {
        TUIContainerModule test = new TUIContainerModule.Builder("test").build();
        test.terminated = true;
        test.run();
        assertFalse(test.terminated);
    }

    @Test
    void testRun_currentRunningChild() {
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();

        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
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
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();

        TUIFunctionModule.Builder checkRunning = checkRunning("check-running-2", testApp);

        TUIApplicationModule otherApp = new TUIApplicationModule.Builder("other-app")
                .setHome(checkRunning)
                .build();


        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                .addChildren(
                        checkRunning("check-running-1", testApp),
                        new TUIFunctionModule.Builder("run-other", () -> {
                            testApp.runModuleAsChild(checkRunning);
                        }),
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
        TUIContainerModule test = new TUIContainerModule.Builder("test").build();
        assertEquals("test", test.getName());
    }

    @Test
    void getChildren() {
        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new TUIContainerModule.Builder("one"),
                        new TUIContainerModule.Builder("two"),
                        new TUIContainerModule.Builder("three")));

        TUIContainerModule parent = new TUIContainerModule.Builder("parent")
                .addChildren(children)
                .build();

        assertEquals(children, parent.getChildren());
    }

    @Test
    void testGetChild() {
        TUIContainerModule.Builder one_2 = new TUIContainerModule.Builder("one-2");
        TUIContainerModule.Builder three_1_1 = new TUIContainerModule.Builder("three-1-1");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new TUIContainerModule.Builder("one")
                                .addChildren(
                                        new TUIContainerModule.Builder("one-1"),
                                        one_2
                                ),
                        new TUIContainerModule.Builder("two"),
                        new TUIContainerModule.Builder("three")
                                .addChild(
                                        new TUIContainerModule.Builder("three-1")
                                                .addChild(three_1_1)
                                )));

        TUIContainerModule parent = new TUIContainerModule.Builder("parent")
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
        TUIContainerModule.Builder one_2 = new TUIContainerModule.Builder("one-2");
        TUITextModule.Builder three_1_1 = new TUITextModule.Builder("three-1-1", "hello!");

        List<TUIModule.Builder<?>> children = new ArrayList<>(
                List.of(
                        new TUIContainerModule.Builder("one")
                                .addChildren(
                                        new TUIContainerModule.Builder("one-1"),
                                        one_2
                                ),
                        new TUIContainerModule.Builder("two"),
                        new TUIContainerModule.Builder("three")
                                .addChild(
                                        new TUIContainerModule.Builder("three-1")
                                                .addChild(three_1_1)
                                )));

        TUIContainerModule parent = new TUIContainerModule.Builder("parent")
                .addChildren(children)
                .build();

        assertAll(
                () -> assertEquals(children.getFirst(), parent.getChild("one", TUIContainerModule.Builder.class)),
                () -> assertEquals( one_2, parent.getChild("one-2", TUIContainerModule.Builder.class)),
                () -> assertEquals(three_1_1, parent.getChild("three-1-1", TUITextModule.Builder.class)),
                () -> assertNull(parent.getChild("three-1-1", TUIContainerModule.Builder.class)),
                () -> assertNull(parent.getChild("other")),
                () -> assertNull(parent.getChild("other", TUIContainerModule.Builder.class))
        );
    }

    @Test
    void testTerminate() {
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();


        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                .addChildren(
                        new TUIFunctionModule.Builder("is-run-1", () -> true),
                        new TUIFunctionModule.Builder("terminate", testApp::terminate),
                        new TUIFunctionModule.Builder("is-run-2", () -> true)
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
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();

        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                .addChildren(
                        new TUIFunctionModule.Builder("is-run-1", () -> true),
                        new TUIContainerModule.Builder("container")
                                .addChildren(
                                        new TUIFunctionModule.Builder("terminate", testApp::terminate),
                                        new TUIFunctionModule.Builder("is-run-2", () -> true)
                                ),
                        new TUIFunctionModule.Builder("is-run-3", () -> true)
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
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();

        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                .addChildren(
                        new TUIFunctionModule.Builder("is-run-1", () -> true),
                        new TUIContainerModule.Builder("container")
                                .addChildren(
                                        new TUIFunctionModule.Builder("terminate", () -> testApp.terminateChild("container")),
                                        new TUIFunctionModule.Builder("is-run-2", () -> true)
                                ),
                        new TUIFunctionModule.Builder("is-run-3", () -> true)
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
    void testIsTerminated() {
        TUIContainerModule test = new TUIContainerModule.Builder("test").build();
        boolean terminated_before = test.isTerminated();
        test.terminated = true;
        boolean terminated_after = test.isTerminated();

        assertAll(
                () -> assertFalse(terminated_before),
                () -> assertTrue(terminated_after)
        );
    }

    @Test
    void testGetCurrentRunningChild() {
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();
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
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();
        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home");

        TUIContainerModule.Builder nest_0 = new TUIContainerModule.Builder("nest-0");
        TUIContainerModule.Builder nest_1 = new TUIContainerModule.Builder("nest-1");
        TUIFunctionModule.Builder nest_2 = new TUIFunctionModule.Builder("nest-2", () -> {});
        nest_2.function(() -> {
            List<TUIModule> runningBranch = testApp.getCurrentRunningBranch();
            List<TUIModule> expectedList = List.of(testApp, home.build(), nest_0.build(), nest_1.build(), nest_2.build());

            return IntStream.range(0, expectedList.size())
                    .allMatch(i -> runningBranch.get(i).equals(expectedList.get(i)));
        });

        home.addChildren(
                        new TUIContainerModule.Builder("empty-1"),
                        nest_0.addChild(nest_1.addChild(nest_2.addChild(new TUIContainerModule.Builder("empty_2")))),
                        new TUIContainerModule.Builder("empty_3")
                );

        testApp.setHome(home);
        testApp.run();

        assertTrue(testApp.getInput("nest-2", Boolean.class));
    }

    @Test
    void testGetApplication() {
        TUIApplicationModule testApp = new TUIApplicationModule.Builder("test-app").build();
        TUIContainerModule test = new TUIContainerModule.Builder("test").setApplication(testApp).build();
        assertEquals(testApp, test.getApplication());
    }

    @Test
    void testGetAnsi() {
        Ansi ansi = ansi().bold().fgRgb(50, 50, 50);
        TUIContainerModule test = new TUIContainerModule.Builder("test").setAnsi(ansi).build();
        assertEquals(ansi, test.getAnsi());
    }

    @Test
    void testGetScanner() {
        Scanner scnr = new Scanner(new ByteArrayInputStream("test".getBytes()));
        TUIContainerModule test = new TUIContainerModule.Builder("test").setScanner(scnr).build();
        assertEquals(scnr, test.getScanner());
    }

    @Test
    void getPrintStream() {
        PrintStream strm = new PrintStream(new ByteArrayOutputStream());
        TUIContainerModule test = new TUIContainerModule.Builder("test").setPrintStream(strm).build();
        assertEquals(strm, test.getPrintStream());
    }

    @Test
    void getAnsiEnabled() {
        TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
        boolean enabledBefore = test.build().getAnsiEnabled();
        test.enableAnsi(false);
        boolean enabledAfter = test.build().getAnsiEnabled();

        assertAll(
                () -> assertTrue(enabledBefore),
                () -> assertFalse(enabledAfter)
        );
    }

    @Test
    void testToString() {
        // not tested since I want to rework toString and haven't decided how it will be changed
    }

    @Test
    void testEquals() {
        // Shared IOCapture for modules that should be equal
        IOCapture ioShared = new IOCapture();
        TUIApplicationModule app1 = new TUIApplicationModule.Builder("app1").build();

        // Base module
        TUIContainerModule module1 = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Exact copy: all properties same
        TUIContainerModule module2 = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Differences:

        // Different name
        TUIContainerModule moduleNameDiff = new TUIContainerModule.Builder("")
                .setName("different-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different application
        TUIApplicationModule app2 = new TUIApplicationModule.Builder("app2").build();
        TUIContainerModule moduleAppDiff = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app2)
                .build();

        // Different ANSI
        TUIContainerModule moduleAnsiDiff = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bgRgb(10, 10, 10))
                .enableAnsi(false)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different ANSI enabled flag
        TUIContainerModule moduleAnsiEnabledDiff = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(true)
                .setPrintStream(ioShared.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different print stream (new IOCapture)
        IOCapture ioOther = new IOCapture();
        TUIContainerModule modulePrintStreamDiff = new TUIContainerModule.Builder("")
                .setName("module-name")
                .setAnsi(ansi().bold())
                .enableAnsi(false)
                .setPrintStream(ioOther.getPrintStream())
                .setScanner(ioShared.getScanner())
                .setApplication(app1)
                .build();

        // Different scanner (new IOCapture)
        TUIContainerModule moduleScannerDiff = new TUIContainerModule.Builder("")
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
                () -> assertTrue(module1.equals(module1), "Reflexive check"),
                () -> assertFalse(module1.equals(null), "Null check"),
                () -> assertTrue(module1.equals(module2), "Exact copy should be equal"),
                () -> assertFalse(module1.equals(moduleNameDiff), "Different name"),
                () -> assertFalse(module1.equals(moduleAppDiff), "Different application"),
                () -> assertFalse(module1.equals(moduleAnsiDiff), "Different ANSI"),
                () -> assertFalse(module1.equals(moduleAnsiEnabledDiff), "Different ANSI enabled flag"),
                () -> assertFalse(module1.equals(modulePrintStreamDiff), "Different print stream"),
                () -> assertFalse(module1.equals(moduleScannerDiff), "Different scanner")
        );

    }


    @Test
    void testEqualsWithChildren() {
        // Children
        TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
        TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");

        // Parent with two children
        TUIContainerModule parent1 = new TUIContainerModule.Builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Exact copy of parent
        TUIContainerModule parent2 = new TUIContainerModule.Builder("parent")
                .addChild(child1.getCopy())
                .addChild(child2.getCopy())
                .build();

        // Parent with a modified child
        TUIContainerModule.Builder child3 = new TUIContainerModule.Builder("child3");

        TUIContainerModule parent3 = new TUIContainerModule.Builder("parent")
                .addChild(child3.getCopy())
                .addChild(child2.getCopy())
                .build();

        assertAll(
                () -> assertTrue(parent1.equals(parent2), "Parents with identical children should be equal"),
                () -> assertFalse(parent1.equals(parent3), "Parents with one different child should not be equal")
        );
    }

    @Nested
    class BuilderTest {

        @Test
        void testSetNameAndGetName() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("old-name");
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("name");
            test.prependToName("new");
            assertEquals("new-name", test.getName());
        }

        @Test
        void testSetApplicationAndGetApplication() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");
            test.addChild(child);

            TUIApplicationModule oldApp = test.getApplication();
            test.setApplication(app);
            TUIApplicationModule newApp = test.getApplication();
            TUIApplicationModule childApp = child.getApplication();

            assertAll(
                    () -> assertNull(oldApp),
                    () -> assertEquals(app, newApp),
                    () -> assertEquals(app, childApp)
            );
        }

        @Test
        void testSetAnsiAndGetAnsi() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child")
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child")
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");
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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");
            test.addChild(child1);
            test.addChild(child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildAtIndex() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");
            test.addChild(child1);
            test.addChild(0, child2);
            assertEquals(List.of(child2, child1), test.getChildren());
        }

        @Test
        void testAddChildrenVarargs() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");
            test.addChildren(child1, child2);
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testAddChildrenList() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");
            test.addChildren(new ArrayList<>(List.of(child1, child2)));
            assertEquals(List.of(child1, child2), test.getChildren());
        }

        @Test
        void testClearChildren() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");
            test.addChildren(child1, child2);
            test.clearChildren();
            assertEquals(List.of(), test.getChildren());
        }

        @Test
        void testGetChildByName() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");

            test.addChildren(
                    new TUIContainerModule.Builder("a"),
                    test,
                    new TUIContainerModule.Builder("b")
                            .addChild(child),
                    new TUIContainerModule.Builder("child")
            );

            TUIModule.Builder<?> found = test.getChild("child");

            assertEquals(child, found);
        }

        @Test
        void testGetChildByNameAndType() {
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUITextModule.Builder child = new TUITextModule.Builder("child", "child");

            test.addChildren(
                    new TUIContainerModule.Builder("a"),
                    test,
                    new TUIContainerModule.Builder("b")
                            .addChild(child),
                    new TUIContainerModule.Builder("child")
            );

            TUITextModule.Builder found = test.getChild("child", TUITextModule.Builder.class);
            TUIContainerModule.Builder other = test.getChild("child", TUIContainerModule.Builder.class);

            assertAll(
                    () -> assertEquals(child, found),
                    () -> assertNull(other)
            );
        }

        @Test
        void testSetPropertyUpdateFlag() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");
            TUIContainerModule.Builder child1 = new TUIContainerModule.Builder("child1");
            TUIContainerModule.Builder child2 = new TUIContainerModule.Builder("child2");

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder populated = new TUIContainerModule.Builder("populated")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold());

            TUIContainerModule.Builder empty = new TUIContainerModule.Builder("empty");

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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");

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
            TUIContainerModule.Builder test = new TUIContainerModule.Builder("test");

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder original = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder copied  = original.getCopy();

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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder original = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChildren(
                            new TUIContainerModule.Builder("child1"),
                            new TUIContainerModule.Builder("child2")
                                    .setApplication(app),
                            new TUIContainerModule.Builder("child3")
                                    .addChild(new TUIContainerModule.Builder("child4")),
                            new TUIContainerModule.Builder("child5")
                                    .setAnsi(ansi().a("testAnsi"))
                    );

            original.getChild("child4").addChild(original);

            TUIContainerModule.Builder copied  = original.getCopy();

            io.close();

            assertTrue(copied.equals(original));
        }

        // equality more thoroughly tested in DirectedGraphNodeTest
        @Test
        void testEqualTo() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder first = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder second = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder third = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder fourth = new TUIContainerModule.Builder("original")
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder fifth = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder sixth = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder seventh = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder eighth = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

            TUIContainerModule.Builder ninth = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold());

            io.close();

            assertAll(
                    () -> assertTrue(first.equalTo(first, first)),
                    () -> assertTrue(first.equalTo(first, second)),
                    () -> assertTrue(second.equalTo(first, third)),
                    () -> assertTrue(first.equalTo(first, third)),
                    () -> assertFalse(first.equalTo(first, fourth)),
                    () -> assertFalse(first.equalTo(first, fifth)),
                    () -> assertFalse(first.equalTo(first, sixth)),
                    () -> assertFalse(first.equalTo(first, seventh)),
                    () -> assertFalse(first.equalTo(first, eighth)),
                    () -> assertFalse(first.equalTo(first, ninth))
            );
        }

        @Test
        void testEquals() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();

            TUIContainerModule.Builder first = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new TUIContainerModule.Builder("child"));

            TUIContainerModule.Builder second = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new TUIContainerModule.Builder("child"));

            TUIContainerModule.Builder third = new TUIContainerModule.Builder("original")
                    .setApplication(app)
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setAnsi(ansi().bold())
                    .setPropertyUpdateFlag(TUIModule.Property.APPLICATION, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT)
                    .addChild(new TUIContainerModule.Builder("child").enableAnsi(false));

            io.close();

            assertAll(
                    () -> assertTrue(TUIModule.Builder.equals(first, first)),
                    () -> assertTrue(TUIModule.Builder.equals(first, second)),
                    () -> assertTrue(TUIModule.Builder.equals(second, first)),
                    () -> assertFalse(TUIModule.Builder.equals(first, third)),

                    () -> assertTrue(first.equals(first)),
                    () -> assertTrue(first.equals(second)),
                    () -> assertTrue(second.equals(first)),
                    () -> assertFalse(first.equals(third))
            );
        }

        @Test
        void testBuild() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");

            TUIModule test = new TUIContainerModule.Builder("test")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            IOCapture io = new IOCapture();
            TUIContainerModule.Builder child = new TUIContainerModule.Builder("child");

            TUIContainerModule.Builder builder = new TUIContainerModule.Builder("test")
                    .setApplication(app)
                    .addChild(child)
                    .setAnsi(ansi().bold())
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            TUIModule first = builder.build();
            TUIModule second = builder.build();

            io.close();

            assertTrue(first.equals(second));
        }
    }
}