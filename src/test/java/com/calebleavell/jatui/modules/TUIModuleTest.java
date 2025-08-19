package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Using TUIContainerModule as the minimal implementation of this class for testing
 */
class TUIModuleTest {

    // TODO - all these tests.. good luck o7

    @Test
    void testRun_termination() {
        TUIContainerModule test = new TUIContainerModule.Builder("test").build();
        test.terminated = true;
        test.run();
        assertFalse(test.terminated);
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