package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Using TUIContainerModule as the minimal implementation of this class for testing
 */
class TUIModuleTest {

    @Test
    void testRun_termination() {
        TUIContainerModule test = new TUIContainerModule.Builder("test").build();
        test.terminated = true;
        test.run();
        assertFalse(test.terminated);
    }

    @Test
    void testRun_currentRunningChild() {
        TUIFunctionModule.Builder checkRunning1 = new TUIFunctionModule.Builder("check-running-1", () -> {});
        TUIFunctionModule.Builder checkRunning2 = new TUIFunctionModule.Builder("check-running-2", () -> {});

        TUIApplicationModule test = new TUIApplicationModule.Builder("test")
                .addChildren(checkRunning1, checkRunning2)
                .build();

        checkRunning1.function(() -> {
            return test.getCurrentRunningChild().equals(checkRunning1.build()) && !test.getCurrentRunningChild().equals(checkRunning2.build());
        });

        checkRunning2.function(() -> {
            return test.getCurrentRunningChild().equals(checkRunning2.build()) && !test.getCurrentRunningChild().equals(checkRunning1.build());
        });

        test.run();

        assertAll(
                () -> assertTrue(test.getInput("check-running-1", Boolean.class)),
                () -> assertTrue(test.getInput("check-running-2", Boolean.class)),
                () -> assertNull(test.getCurrentRunningChild())
        );
    }

    @Test
    void runModuleAsChild() {
    }

    @Test
    void getName() {
    }

    @Test
    void getChildren() {
    }

    @Test
    void getChild() {
    }

    @Test
    void testGetChild() {
    }

    @Test
    void terminate() {
    }

    @Test
    void terminateChild() {
    }

    @Test
    void isTerminated() {
    }

    @Test
    void getCurrentRunningChild() {
    }

    @Test
    void testGetCurrentRunningChild() {
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