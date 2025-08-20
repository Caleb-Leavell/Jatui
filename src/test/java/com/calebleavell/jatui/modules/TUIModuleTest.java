package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

        TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                .addChildren(
                        checkShallowRunning("check-running-1", testApp),
                        checkShallowRunning("check-running-2", testApp)
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
    void getCurrentRunningBranch() {
    }

    @Test
    void getApplication() {
    }

    @Test
    void getAnsi() {
    }

    @Test
    void getScanner() {
    }

    @Test
    void getPrintStream() {
    }

    @Test
    void getAnsiEnabled() {
    }

    @Test
    void testToString() {
    }


    @Test
    void testEquals() {
    }
}