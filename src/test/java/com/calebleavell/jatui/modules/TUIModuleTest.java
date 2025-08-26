package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
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
            // TODO: test setName and getName
        }

        @Test
        void testPrependToName() {
            // TODO: test prependToName
        }

        @Test
        void testSetApplicationAndGetApplication() {
            // TODO: test setApplication and getApplication
        }

        @Test
        void testSetAnsiAndGetAnsi() {
            // TODO: test setAnsi and getAnsi
        }

        @Test
        void testPrependAnsi() {
            // TODO: test prependAnsi
        }

        @Test
        void testAppendAnsi() {
            // TODO: test appendAnsi
        }

        @Test
        void testSetScannerAndGetScanner() {
            // TODO: test setScanner and getScanner
        }

        @Test
        void testSetPrintStreamAndGetPrintStream() {
            // TODO: test setPrintStream and getPrintStream
        }

        @Test
        void testEnableAnsiAndGetAnsiEnabled() {
            // TODO: test enableAnsi and getAnsiEnabled
        }

        @Test
        void testAddChild() {
            // TODO: test addChild (single)
        }

        @Test
        void testAddChildAtIndex() {
            // TODO: test addChild at specific index
        }

        @Test
        void testAddChildrenVarargs() {
            // TODO: test addChildren with varargs
        }

        @Test
        void testAddChildrenList() {
            // TODO: test addChildren with list
        }

        @Test
        void testClearChildren() {
            // TODO: test clearChildren
        }

        @Test
        void testGetChildByName() {
            // TODO: test getChild(String)
        }

        @Test
        void testGetChildByNameAndType() {
            // TODO: test getChild(String, Class)
        }

        @Test
        void testGetChildren() {
            // TODO: test getChildren list is returned
        }

        @Test
        void testSetPropertyUpdateFlag() {
            // TODO: test setPropertyUpdateFlag
        }

        @Test
        void testLockProperty() {
            // TODO: test lockProperty
        }

        @Test
        void testUnlockProperty() {
            // TODO: test unlockProperty
        }

        @Test
        void testUpdateProperties() {
            // TODO: test updateProperties from TUIModule
        }

        @Test
        void testGetPropertyUpdateFlags() {
            // TODO: test getPropertyUpdateFlags
        }

        @Test
        void testGetCopy() {
            // TODO: test getCopy
        }

        @Test
        void testGetDeepCopy() {
            // TODO: test getDeepCopy
        }

        @Test
        void testEqualTo() {
            // TODO: test equalTo for shallow equality (properties only)
        }

        @Test
        void testEqualsWithChildren() {
            // TODO: test Builder.equals recursively includes children
        }

        @Test
        void testBuild() {
            // TODO: test build() returns proper TUIModule
        }
    }
}