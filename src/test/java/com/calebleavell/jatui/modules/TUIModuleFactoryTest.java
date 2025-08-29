package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TUIModuleFactoryTest {

    @Test
    void testEmpty() {
        TUIContainerModule empty;
        String output;

        try(IOCapture io = new IOCapture()) {
            empty = TUIModuleFactory.empty("empty")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            TUIModuleFactory.terminate("terminate-app", app),
                            new TUITextModule.Builder("text-2", "second")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            new TUIContainerModule.Builder("group")
                                    .addChildren(
                                            new TUITextModule.Builder("text-2", "second"),
                                            TUIModuleFactory.terminate("terminate-group", "group", app),
                                            new TUITextModule.Builder("text-3", "third")
                                    ),
                            new TUITextModule.Builder("text-4", "fourth")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%nfourth%n"), output);
    }

    @Test
    void testRunBuilder() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TUITextModule.Builder text = new TUITextModule.Builder("text", "output")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false);

            TUIFunctionModule.Builder runText = TUIModuleFactory.run("run-text", text);

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("output%n"), output);
    }

    @Test
    void testRunModule() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TUITextModule text = new TUITextModule.Builder("text", "output")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIFunctionModule.Builder runText = TUIModuleFactory.run("run-text", text);

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("output%n"), output);
    }

    @Test
    void testRunNameParent() {
        String output;

        try(IOCapture io = new IOCapture()) {

            TUIContainerModule parent = new TUIContainerModule.Builder("parent")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            new TUIContainerModule.Builder("group")
                                    .addChildren(
                                            new TUITextModule.Builder("text-2", "second"),
                                            new TUITextModule.Builder("text-3", "third")
                                    ),
                            new TUITextModule.Builder("text-4", "fourth"))
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIFunctionModule.Builder runText = TUIModuleFactory.run("run-group", parent, "group");

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("second%nthird%n"), output);
    }

    @Test
    void testRunNameParent_ParentIsApplication() {
        String output;

        try(IOCapture io = new IOCapture()) {

            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("parent")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            new TUIContainerModule.Builder("group")
                                    .addChildren(
                                            new TUITextModule.Builder("text-2", "second"),
                                            new TUITextModule.Builder("text-3", "third")
                                    ),
                            new TUITextModule.Builder("text-4", "fourth"));

            app.setHome(home);

            TUIFunctionModule.Builder runText = TUIModuleFactory.run("run-group", app, "group");

            runText.build().run();

            output = io.getOutput();
        }

        assertEquals(String.format("second%nthird%n"), output);
    }

    @Test
    void testCounter() {
        String output;

        try(IOCapture io = new IOCapture()) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .build();

            TUIContainerModule.Builder printToTen = new TUIContainerModule.Builder("print-to-ten")
                    .addChildren(
                            TUIModuleFactory.counter("counter", app),
                            new TUITextModule.Builder("display", "counter")
                                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT),
                            new TUIFunctionModule.Builder("exit-if-greater-than-10", () -> {
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .build();

            TUIContainerModule.Builder printToTen = new TUIContainerModule.Builder("print-to-ten")
                    .addChildren(
                            TUIModuleFactory.counter("counter", app, 5, 2),
                            new TUITextModule.Builder("display", "counter")
                                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT),
                            new TUIFunctionModule.Builder("exit-if-greater-than-10", () -> {
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
        void testShallowCopy() {

        }

        @Test
        void testAddListText() {

        }

        @Test
        void testSetStart() {

        }

        @Test
        void testSetStep() {

        }

        @Test
        void testCollectInputMessage() {

        }

        @Test
        void testCollectInputModule() {

        }
    }

    @Nested
    class NumberedModuleSelectorTest {

        @Test
        void testShallowCopy() {

        }

        @Test
        void testAddSceneNameOrModule() {

        }

        @Test
        void testAddSceneDisplayTextName() {

        }

        @Test
        void testAddSceneDisplayTextModule() {

        }

        @Test
        void testAddSceneName() {

        }

        @Test
        void testAddSceneModule() {

        }
    }

    @Nested
    class LineBuilderTest {

        @Test
        void testShallowCopy() {

        }

        @Test
        void testAddTextModule() {

        }

        @Test
        void testAddTextWithAnsi() {

        }

        @Test
        void testAddText() {

        }

        @Test
        void testAddModuleOutputWithAnsi() {

        }

        @Test
        void testAddModuleOutput() {

        }
    }
}