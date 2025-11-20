package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import static org.fusesource.jansi.Ansi.ansi;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testRestartModule() {
        String output;

        try(IOCapture io = new IOCapture("a\nb\nc\nd")) {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            new TUITextInputModule.Builder("get-input", "input: ")
                                    .addSafeHandler("exit-if-d", s -> {
                                        if(s.equals("d")) app.terminate();
                                        return null;
                                    }),
                            TUIModuleFactory.restart("restart-app", app),
                            new TUITextModule.Builder("text-2", "second")
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
            TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                    .setOnExit(TUIModuleFactory.empty("exit"))
                    .setScanner(io.getScanner())
                    .setPrintStream(io.getPrintStream())
                    .enableAnsi(false)
                    .build();

            TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                    .addChildren(
                            new TUITextModule.Builder("text-1", "first"),
                            new TUIContainerModule.Builder("group")
                                    .addChildren(
                                            new TUITextModule.Builder("text-2", "second"),
                                            new TUITextInputModule.Builder("get-input", "input: ")
                                                    .addSafeHandler("exit-if-d", s -> {
                                                        if(s.equals("d")) app.terminate();
                                                        return null;
                                                    }),
                                            TUIModuleFactory.restart("restart-group", app, "group"),
                                            new TUITextInputModule.Builder("test-3", "third")
                                    ),
                            new TUITextModule.Builder("text-4", "fourth")
                    );

            app.setHome(home);
            app.run();

            output = io.getOutput();
        }

        assertEquals(String.format("first%nsecond%ninput: second%ninput: second%ninput: second%ninput: "), output);
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
        void testCopy() {
            TUIModuleFactory.NumberedList original = new TUIModuleFactory.NumberedList("list", "item1", "item2")
                    .setStart(5)
                    .setStep(3);

            TUIModuleFactory.NumberedList copy = original.getCopy();

            assertTrue(original.equals(copy));
        }

        @Test
        void testAddListText() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.NumberedList list = new TUIModuleFactory.NumberedList("list", "item1")
                        .addListText("item2")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                list.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("[1] item1%n[2] item2%n"), output);
        }

        @Test
        void testSetStart() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.NumberedList list = new TUIModuleFactory.NumberedList("list")
                        .setStart(5)
                        .addListText("item1")
                        .addListText("item2")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                list.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("[5] item1%n[6] item2%n"), output);
        }

        @Test
        void testSetStep() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.NumberedList list = new TUIModuleFactory.NumberedList("list")
                        .setStep(3)
                        .addListText("item1")
                        .addListText("item2")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                list.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("[1] item1%n[4] item2%n"), output);
        }

        @Test
        void testSetStartAndStep() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.NumberedList list = new TUIModuleFactory.NumberedList("list")
                        .setStart(5)
                        .setStep(3)
                        .addListText("item1")
                        .addListText("item2")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                list.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("[5] item1%n[8] item2%n"), output);
        }

        @Test
        void testEqualTo() {
            TUIModuleFactory.NumberedList list1 = new TUIModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            TUIModuleFactory.NumberedList list2 = new TUIModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            TUIModuleFactory.NumberedList list3 = new TUIModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            TUIModuleFactory.NumberedList list4 = new TUIModuleFactory.NumberedList("list", "text1", "text2", "text3")
                    .setStart(5)
                    .setStep(2);

            TUIModuleFactory.NumberedList list5 = new TUIModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(6)
                    .setStep(2);

            TUIModuleFactory.NumberedList list6 = new TUIModuleFactory.NumberedList("list", "text1", "text2")
                    .setStart(5)
                    .setStep(3);

            TUIModuleFactory.NumberedList list7 = new TUIModuleFactory.NumberedList("rename-super-name", "text1", "text2")
                    .setStart(5)
                    .setStep(2);

            assertAll(
                    () -> assertTrue(list1.equals(list1)),
                    () -> assertTrue(list1.equals(list2)),
                    () -> assertTrue(list2.equals(list1)),
                    () -> assertTrue(list2.equals(list3)),
                    () -> assertTrue(list1.equals(list3)),
                    () -> assertFalse(list1.equals(list4)),
                    () -> assertFalse(list1.equals(list5)),
                    () -> assertFalse(list1.equals(list6)),
                    () -> assertFalse(list1.equals(list7))
            );
        }
    }

    @Nested
    class NumberedModuleSelectorTest {

        @Test
        void testCopy() {
            TUIApplicationModule app1 = new TUIApplicationModule.Builder("app")
                    .addChildren(
                            new TUITextModule.Builder("text", "Hello, World!"),
                            new TUITextInputModule.Builder("input", "input: ")
                    )
                    .build();

            TUIContainerModule.Builder module = new TUIContainerModule.Builder("module");

            TUIModuleFactory.NumberedModuleSelector original = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector copy = original.getCopy();

            assertTrue(copy.equals(original));
        }


        @Test
        void testAddSceneDisplayTextName() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .build();

                TUIApplicationModule otherApp = new TUIApplicationModule.Builder("app1")
                        .addChildren(
                                new TUITextModule.Builder("text", "Hello, World!"),
                                new TUITextInputModule.Builder("input", "input: ")
                        )
                        .enableAnsi(false)
                        .setPrintStream(io.getPrintStream())
                        .build();

                TUIModuleFactory.NumberedModuleSelector original = new TUIModuleFactory.NumberedModuleSelector("list", otherApp)
                        .addModule("goto text module", "text");

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = TUIApplicationModuleTest.lines(
                    "[1] goto text module",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneDisplayTextModule() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .build();

                TUITextModule.Builder text = new TUITextModule.Builder("text", "Hello, World!")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                TUIModuleFactory.NumberedModuleSelector original = new TUIModuleFactory.NumberedModuleSelector("list", app)
                        .addModule("goto text module", text);

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = TUIApplicationModuleTest.lines(
                    "[1] goto text module",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneName() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .build();

                TUIApplicationModule otherApp = new TUIApplicationModule.Builder("app1")
                        .addChildren(
                                new TUITextModule.Builder("text", "Hello, World!"),
                                new TUITextInputModule.Builder("input", "input: ")
                        )
                        .enableAnsi(false)
                        .setPrintStream(io.getPrintStream())
                        .build();

                TUIModuleFactory.NumberedModuleSelector original = new TUIModuleFactory.NumberedModuleSelector("list", otherApp)
                        .addModule( "text");

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = TUIApplicationModuleTest.lines(
                    "[1] text",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testAddSceneModule() {

            String output;

            try (IOCapture io = new IOCapture("1")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .build();

                TUITextModule.Builder text = new TUITextModule.Builder("text", "Hello, World!")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                TUIModuleFactory.NumberedModuleSelector original = new TUIModuleFactory.NumberedModuleSelector("list", app)
                        .addModule(text);

                app.setHome(original);
                app.run();

                output = io.getOutput();
            }

            String expected = TUIApplicationModuleTest.lines(
                    "[1] text",
                    "Your choice: Hello, World!"
            );

            assertEquals(expected, output);
        }

        @Test
        void testCommonUseCase() {
            String output;

            try (IOCapture io = new IOCapture("1\n2\n3")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUITextModule.Builder text = new TUITextModule.Builder("text", "Hello, World!")
                        .addChild(TUIModuleFactory.run("run-home", app, "home"))
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                        .addChildren(
                                new TUIModuleFactory.NumberedModuleSelector("list", app)
                                        .addModule(text)
                                        .addModule("restart", "home")
                                        .addModule("exit", TUIModuleFactory.terminate("terminate-app", app))
                        );

                app.setHome(home);
                app.run();

                output = io.getOutput();
            }

            String expected = TUIApplicationModuleTest.lines(
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
        void testEqualTo() {
            TUIApplicationModule app1 = new TUIApplicationModule.Builder("app")
                    .addChildren(
                            new TUITextModule.Builder("text", "Hello, World!"),
                            new TUITextInputModule.Builder("input", "input: ")
                    )
                    .build();

            TUIApplicationModule app2 = new TUIApplicationModule.Builder("app")
                    .addChildren(
                            new TUITextModule.Builder("text", "Hello, World!"),
                            new TUITextInputModule.Builder("input", "input: ")
                    )
                    .build();

            TUIContainerModule.Builder module = new TUIContainerModule.Builder("module");

            TUIModuleFactory.NumberedModuleSelector list1 = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector list2 = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector list3 = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector list4 = new TUIModuleFactory.NumberedModuleSelector("list", app2)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector list5 = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("input")
                    .addModule(module)
                    .addModule("the module", module);

            TUIModuleFactory.NumberedModuleSelector list6 = new TUIModuleFactory.NumberedModuleSelector("list", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("other display text", module);

            TUIModuleFactory.NumberedModuleSelector list7 = new TUIModuleFactory.NumberedModuleSelector("other name", app1)
                    .addModule("text")
                    .addModule(module)
                    .addModule("the module", module);

            assertAll(
                    () -> assertTrue(list1.equals(list1)),
                    () -> assertTrue(list1.equals(list2)),
                    () -> assertTrue(list2.equals(list1)),
                    () -> assertTrue(list2.equals(list3)),
                    () -> assertTrue(list1.equals(list3)),
                    () -> assertFalse(list1.equals(list4)),
                    () -> assertFalse(list1.equals(list5)),
                    () -> assertFalse(list1.equals(list6)),
                    () -> assertFalse(list1.equals(list7))
            );
        }
    }

    @Nested
    class LineBuilderTest {

        @Test
        void testGetCopy() {
            TUIModuleFactory.LineBuilder original = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            TUIModuleFactory.LineBuilder copy = original.getCopy();

            assertTrue(copy.equals(original));
        }

        @Test
        void testAddTextModule() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.LineBuilder original = new TUIModuleFactory.LineBuilder("lines")
                        .addText(new TUITextModule.Builder("text", "Hello, World!"))
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                original.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("Hello, World!%n"), output);
        }

        @Test
        void testAddTextWithAnsi() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.LineBuilder original = new TUIModuleFactory.LineBuilder("lines")
                        .addText("Hello, World!", ansi().bold())
                        .setPrintStream(io.getPrintStream());

                original.build().run();

                output = io.getOutput();
            }

            assertEquals(ansi().bold().a("Hello, World!").reset().toString(), output);
        }

        @Test
        void testAddText() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.LineBuilder original = new TUIModuleFactory.LineBuilder("lines")
                        .addText("Hello, World!")
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                original.build().run();

                output = io.getOutput();
            }

            assertEquals("Hello, World!", output);
        }

        @Test
        void testAddModuleOutputWithAnsi() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .build();

                TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                        .addChildren(
                                new TUIFunctionModule.Builder("five", () -> 5),
                                new TUIModuleFactory.LineBuilder("display-five")
                                        .addText("Output: ")
                                        .addModuleOutput("five", ansi().bold())
                                        .newLine()
                        );

                app.setHome(home);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("%s%s%n", ansi().a("Output: ").reset(), ansi().bold().a("5").reset()), output);
        }

        @Test
        void testAddModuleOutput() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUIContainerModule.Builder home = new TUIContainerModule.Builder("home")
                        .addChildren(
                                new TUIFunctionModule.Builder("five", () -> 5),
                                new TUIModuleFactory.LineBuilder("display-five")
                                        .addText("Output: ")
                                        .addModuleOutput("five")
                                        .newLine()
                        );

                app.setHome(home);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("Output: 5%n"), output);
        }

        @Test
        void testNewLine() {
            String output;

            try(IOCapture io = new IOCapture()) {
                TUIModuleFactory.LineBuilder original = new TUIModuleFactory.LineBuilder("lines")
                        .addText("Hello,")
                        .newLine()
                        .addText("World!")
                        .newLine()
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false);

                original.build().run();

                output = io.getOutput();
            }

            assertEquals(String.format("Hello,%nWorld!%n"), output);
        }

        @Test
        void testEqualTo() {
            TUIModuleFactory.LineBuilder lines1 = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            TUIModuleFactory.LineBuilder lines2 = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            TUIModuleFactory.LineBuilder lines3 = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2");

            TUIModuleFactory.LineBuilder lines4 = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1_other")
                    .addText("text2");

            TUIModuleFactory.LineBuilder lines5 = new TUIModuleFactory.LineBuilder("lines")
                    .addText("text1")
                    .addText("text2")
                    .addText("text3");

            TUIModuleFactory.LineBuilder lines6 = new TUIModuleFactory.LineBuilder("other")
                    .addText("text1")
                    .addText("text2");

            assertAll(
                    () -> assertTrue(lines1.equals(lines1)),
                    () -> assertTrue(lines1.equals(lines2)),
                    () -> assertTrue(lines2.equals(lines3)),
                    () -> assertTrue(lines1.equals(lines3)),
                    () -> assertFalse(lines1.equals(lines4)),
                    () -> assertFalse(lines1.equals(lines5)),
                    () -> assertFalse(lines1.equals(lines6))
            );
        }
    }

    @Nested
    class ConfirmationPromptTest {

        @Test
        void testSetValidConfirm() {
            TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("name", "Are you sure? ")
                    .setValidConfirm("1", "2", "3");

            assertEquals(Set.of("1", "2", "3"), confirm.getValidConfirm());
        }

        @Test
        void testSetValidDeny() {
            TUIModuleFactory.ConfirmationPrompt deny = new TUIModuleFactory.ConfirmationPrompt("name", "Are you sure? ")
                    .setValidConfirm("1", "2", "3");

            assertEquals(Set.of("1", "2", "3"), deny.getValidConfirm());
        }

        @Test
        void testAddOnConfirmRunnable() {
            String output;

            try(IOCapture io = new IOCapture("yes")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals("Are you sure? confirmed", output);
        }

        @Test
        void testAddOnConfirmRunnableWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
        }

        @Test
        void testAddOnConfirmMultipleRunnableWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"));

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
            }

            assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output);
        }

        @Test
        void testAddOnConfirmMultipleSupplierWithDifferentConfirm() {
            String output;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm("on-confirm", () -> "confirmed");

                app.setHome(confirm);
                app.run();

                output = app.getInput("on-confirm").toString();
            }

            assertEquals("confirmed", output);
        }

        @Test
        void testAddOnConfirmMultipleSuppliersAndRunnables() {
            String output;
            String confirmed1;
            String confirmed2;

            try(IOCapture io = new IOCapture("yes\nyeah")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidConfirm("yeah")
                        .addOnConfirm("on-confirm-1", () -> "confirmed")
                        .addOnConfirm(() -> io.getPrintStream().print("confirmed"))
                        .addOnConfirm("on-confirm-2", () -> "confirmed-2");

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
                confirmed1 = app.getInput("on-confirm-1").toString();
                confirmed2 = app.getInput("on-confirm-2").toString();
            }

            assertAll(
                    () -> assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? confirmed"), output),
                    () -> assertEquals("confirmed", confirmed1),
                    () -> assertEquals("confirmed-2", confirmed2)
            );
        }

        @Test
        void testAddOnDenyMultipleSuppliersAndRunnables() {
            String output;
            String denied1;
            String denied2;

            try(IOCapture io = new IOCapture("no\nnah")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setOnExit(TUIModuleFactory.empty("empty"))
                        .setPrintStream(io.getPrintStream())
                        .setScanner(io.getScanner())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                        .setValidDeny("nah")
                        .addOnDeny("on-deny-1", () -> "denied")
                        .addOnDeny(() -> io.getPrintStream().print("denied"))
                        .addOnDeny("on-deny-2", () -> "denied-2");

                app.setHome(confirm);
                app.run();

                output = io.getOutput();
                denied1 = app.getInput("on-deny-1").toString();
                denied2 = app.getInput("on-deny-2").toString();
            }

            assertAll(
                    () -> assertEquals(String.format("Are you sure? Error: Invalid Input%nAre you sure? denied"), output),
                    () -> assertEquals("denied", denied1),
                    () -> assertEquals("denied-2", denied2)
            );
        }

        @Test
        void testSetName() {
            TUIModuleFactory.ConfirmationPrompt confirm = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setName("new-confirm-name");

            assertAll(
                    () -> assertEquals("new-confirm-name", confirm.getName()),
                    () -> assertNotNull(confirm.getChild("new-confirm-name-input")),
                    () -> assertNull(confirm.getChild("confirm-input"))
            );

        }

        @Test
        void testEqualTo() {
            TUIModuleFactory.ConfirmationPrompt prompt1 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt2 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt3 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt4 = new TUIModuleFactory.ConfirmationPrompt("confirm", "other text")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt5 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps", "new valid confirm")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt6 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not", "new valid deny")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt7 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm("different-on-confirm", () -> 0)
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt prompt8 = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny("different-on-deny", () -> 0);

            TUIModuleFactory.ConfirmationPrompt prompt9 = new TUIModuleFactory.ConfirmationPrompt("other-name", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            assertAll(
                    () -> assertTrue(prompt1.equals(prompt1)),
                    () -> assertTrue(prompt1.equals(prompt2)),
                    () -> assertTrue(prompt2.equals(prompt1)),
                    () -> assertTrue(prompt2.equals(prompt3)),
                    () -> assertTrue(prompt1.equals(prompt3)),
                    () -> assertFalse(prompt1.equals(prompt4)),
                    () -> assertFalse(prompt1.equals(prompt5)),
                    () -> assertFalse(prompt1.equals(prompt6)),
                    () -> assertFalse(prompt1.equals(prompt7)),
                    () -> assertFalse(prompt1.equals(prompt8)),
                    () -> assertFalse(prompt1.equals(prompt9))
            );
        }

        @Test
        void testGetCopy() {
            TUIModuleFactory.ConfirmationPrompt original = new TUIModuleFactory.ConfirmationPrompt("confirm", "Are you sure? ")
                    .setValidConfirm("mhm", "perhaps")
                    .setValidDeny("not sure", "probably not")
                    .addOnConfirm(() -> System.out.println("text"))
                    .addOnDeny(() -> System.out.println("text"));

            TUIModuleFactory.ConfirmationPrompt copy = original.getCopy();

            assertTrue(original.equals(copy));
        }
    }

    @Nested
    class PasswordInputTest {
        @Test
        void testCleanMemory() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> correct;

            try(IOCapture io = new IOCapture("my-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .storeInputAndMatch();

                app.setHome(myInput);
                app.run();

                char[] expectedInput = "my-password".toCharArray();
                char[] input = Arrays.copyOf(app.getInput("pw-input-input", char[].class), expectedInput.length);
                boolean isMatch = app.getInput("pw-input-is-matched", Boolean.class);

                myInput.cleanMemory();

                char[] expectedInputAfter = "           ".toCharArray();
                char[] inputAfter = app.getInput("pw-input-input", char[].class);
                Object isMatchAfter = app.getInput("pw-input-is-matched");

                assertAll(
                        () -> assertArrayEquals(expectedInput, input),
                        () -> assertFalse(isMatch),
                        () -> assertArrayEquals(expectedInputAfter, inputAfter),
                        () -> assertNull(isMatchAfter),
                        () -> assertSame(inputAfter, app.getInput("pw-input-input", char[].class))
                );
            }
        }

        @Test
        void testValidatePassword() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .storeInput();

                app.setHome(myInput);
                app.run();

                boolean isValid1 = myInput.validatePassword(correct);

                app.run();
                boolean isValid2 = myInput.validatePassword(correct);

                myInput.cleanImmediately();
                app.run();
                boolean isValid3 = myInput.validatePassword(correct);

                assertAll(
                        () -> assertFalse(isValid1),
                        () -> assertTrue(isValid2),
                        () -> assertFalse(isValid3)
                );
            }
        }

        @Test
        void testOnValidPassword() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password\nwrong-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .addOnValidPassword("on-valid", () -> 5);

                app.setHome(myInput);
                app.run();
                Integer input1 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input2 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input3 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input4 = app.getInput("on-valid", Integer.class);

                assertAll(
                        () -> assertNull(input1),
                        () -> assertEquals(5, input2),
                        () -> assertEquals(5, input3),
                        () -> assertNull(input4)
                );
            }
        }

        @Test
        void testOnValidPasswordMultiple() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("wrong-password\ncorrect-password\ncorrect-password\nwrong-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("on-valid", app.getInputOrDefault("on-valid", Integer.class, 0) + 1);
                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .addOnValidPassword(addOne)
                        .addOnValidPassword(addOne);

                app.setHome(myInput);
                app.run();
                Integer input1 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input2 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input3 = app.getInput("on-valid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input4 = app.getInput("on-valid", Integer.class);

                assertAll(
                        () -> assertNull(input1),
                        () -> assertEquals(2, input2),
                        () -> assertEquals(2, input3),
                        () -> assertNull(input4)
                );
            }
        }

        @Test
        void testOnInvalidPassword() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("correct-password\nwrong-password\nwrong-password\ncorrect-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .addOnInvalidPassword("on-invalid", () -> 5);

                app.setHome(myInput);
                app.run();
                Integer input1 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input2 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input3 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input4 = app.getInput("on-invalid", Integer.class);

                assertAll(
                        () -> assertNull(input1),
                        () -> assertEquals(5, input2),
                        () -> assertEquals(5, input3),
                        () -> assertNull(input4)
                );
            }
        }

        @Test
        void testOnInvalidPasswordMultiple() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("correct-password\nwrong-password\nwrong-password\ncorrect-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("on-invalid", app.getInputOrDefault("on-invalid", Integer.class, 0) + 1);
                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .addOnInvalidPassword(addOne)
                        .addOnInvalidPassword(addOne);

                app.setHome(myInput);
                app.run();
                Integer input1 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input2 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input3 = app.getInput("on-invalid", Integer.class);
                app.resetMemory();
                app.run();
                Integer input4 = app.getInput("on-invalid", Integer.class);

                assertAll(
                        () -> assertNull(input1),
                        () -> assertEquals(2, input2),
                        () -> assertEquals(2, input3),
                        () -> assertNull(input4)
                );
            }
        }

        @Test
        void testOnValidAndOnInvalidPasswordMultiple() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> Arrays.copyOf(correct, correct.length);

            try(IOCapture io = new IOCapture("correct-password\ncorrect-password\nwrong-password\nwrong-password\ncorrect-password")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .build();

                Runnable addOne = () -> app.forceUpdateInput("output", app.getInputOrDefault("output", Integer.class, 0) + 1);
                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "password: ", supplyCorrect)
                        .addOnInvalidPassword(addOne)
                        .addOnValidPassword(addOne)
                        .addOnValidPassword(addOne)
                        .addOnInvalidPassword(addOne)
                        .addOnInvalidPassword(addOne)
                        .addOnValidPassword(addOne)
                        .addOnValidPassword(addOne);

                app.setHome(myInput);
                app.run();
                Integer input1 = app.getInput("output", Integer.class);
                app.resetMemory();
                app.run();
                Integer input2 = app.getInput("output", Integer.class);
                app.resetMemory();
                app.run();
                Integer input3 = app.getInput("output", Integer.class);
                app.resetMemory();
                app.run();
                Integer input4 = app.getInput("output", Integer.class);
                app.resetMemory();
                app.run();
                Integer input5 = app.getInput("output", Integer.class);

                assertAll(
                        () -> assertEquals(4, input1),
                        () -> assertEquals(4, input2),
                        () -> assertEquals(3, input3),
                        () -> assertEquals(3, input4),
                        () -> assertEquals(4, input5)
                );
            }
        }

        @Test
        void testSetName() {
            TUIModuleFactory.PasswordInput input = new TUIModuleFactory.PasswordInput("name-1", "text", null);
            input.setName("name-2");

            assertAll(
                    () -> assertEquals("name-2", input.getName()),
                    () -> assertNotNull(input.getChild("name-2-input")),
                    () -> assertNull(input.getChild("name-1-input"))
            );
        }

        @Test
        void testSetDisplayText() {
            final char[] correct = "correct-password".toCharArray();
            Supplier<char[]> supplyCorrect = () -> correct;

            try(IOCapture io = new IOCapture("input1\ninput2")) {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app")
                        .setScanner(io.getScanner())
                        .setPrintStream(io.getPrintStream())
                        .enableAnsi(false)
                        .setOnExit(TUIModuleFactory.empty("on-exit"))
                        .build();

                TUIModuleFactory.PasswordInput myInput = new TUIModuleFactory.PasswordInput("pw-input", "text-1", supplyCorrect)
                        .storeInputAndMatch();

                app.setHome(myInput);
                app.run();
                String output1 = io.getOutput();
                myInput.setDisplayText("text-2");
                app.run();
                String output2 = io.getOutput();

                assertAll(
                        () -> assertEquals("text-1", output1),
                        () -> assertEquals("text-1text-2", output2)
                );
            }
        }

        @Test
        void testEqualTo() {
            char[] pw = {'a'};
            Supplier<char[]> first = () -> pw;
            Supplier<char[]> second = () -> pw;

            TUIModuleFactory.PasswordInput input1 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input2 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();


            TUIModuleFactory.PasswordInput input3 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input4 = new TUIModuleFactory.PasswordInput("pw-input", "other text: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input5 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInput();

            TUIModuleFactory.PasswordInput input6 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeIfMatched();

            TUIModuleFactory.PasswordInput input7 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", second)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input8 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> System.out.println("different"))
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input9 = new TUIModuleFactory.PasswordInput("pw-input", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> System.out.println("other"))
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput input10 = new TUIModuleFactory.PasswordInput("other-name", "password: ", first)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> System.out.println("other"))
                    .storeInputAndMatch();

            assertAll(
                    () -> assertTrue(input1.equals(input1)),
                    () -> assertTrue(input1.equals(input2)),
                    () -> assertTrue(input2.equals(input1)),
                    () -> assertTrue(input2.equals(input3)),
                    () -> assertTrue(input1.equals(input3)),
                    () -> assertFalse(input1.equals(input4)),
                    () -> assertFalse(input1.equals(input5)),
                    () -> assertFalse(input1.equals(input6)),
                    () -> assertTrue(input1.equals(input7)),
                    () -> assertTrue(input1.equals(input8)),
                    () -> assertTrue(input1.equals(input9)),
                    () -> assertFalse(input1.equals(input10))
            );

        }

        @Test
        void testGetCopy() {
            char[] pw = {'a'};
            Supplier<char[]> sup = () -> pw;

            TUIModuleFactory.PasswordInput original = new TUIModuleFactory.PasswordInput("pw-input", "password: ", sup)
                    .addOnInvalidPassword(() -> {})
                    .addOnValidPassword(() -> {})
                    .storeInputAndMatch();

            TUIModuleFactory.PasswordInput copy = original.getCopy();

            assertTrue(original.equals(copy));
        }
    }
}