package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.fusesource.jansi.Ansi.ansi;
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
}