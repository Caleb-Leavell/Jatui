package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.IOCapture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class TUITextInputModuleTest {

    // TODO

    @Test
    void testRun() {
    }

    @Test
    void testGetInput() {
    }

    @Test
    void testEquals() {
    }

    @Nested
    class BuilderTest {

        @Test
        void testDeepCopy() {

        }

        @Test
        void testAddHandlerModule() {

        }

        @Test
        void testAddHandler() {

        }

        @Test
        void testAddSafeHandlerExceptionHandler() {

        }

        @Test
        void testAddSafeHandlerExceptionMessage() {

        }

        @Test
        void testAddSafeHandler() {

        }

        @Test
        void testBuild() {

        }
    }

    @Nested
    class InputHandlersTest {

        @Test
        void testCopy() {
            Function<String, String> logic = s -> s;
            Consumer<String> exceptionHandler = s -> {System.out.print("");};
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

            TUITextInputModule.InputHandlers original = new TUITextInputModule.InputHandlers("handler", module)
                    .addSafeHandler("logic", logic, exceptionHandler);

            TUITextInputModule.InputHandlers copy = original.getCopy();

            assertTrue(copy.equals(original));
        }

        @Test
        void testAddHandlerModule() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");
            TUIFunctionModule.Builder logic = new TUIFunctionModule.Builder("logic", () -> 5)
                    .setApplication(app);

            TUITextInputModule.InputHandlers handlers = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic);

            app.setHome(handlers);
            app.run();

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddHandler() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

            TUITextInputModule.InputHandlers handlers = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler("logic", s -> 5);

            app.setHome(handlers);
            app.run();

            assertEquals(5, app.getInput("logic"));
        }

        @Test
        void testAddSafeHandler() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

            TUITextInputModule.InputHandlers handlers = new TUITextInputModule.InputHandlers("handlers", module)
                    .addSafeHandler("logic",
                            s -> {throw new RuntimeException("forced exception");},
                            s -> app.updateInput("logic", "Success!"));

            app.setHome(handlers);
            app.run();

            assertEquals("Success!", app.getInput("logic"));
        }

        @Test
        void addMultipleHandlers() {
            TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");
            TUIFunctionModule.Builder logic = new TUIFunctionModule.Builder("logic1", () -> 5)
                    .setApplication(app);

            TUITextInputModule.InputHandlers handlers = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic)
                    .addHandler("logic2", s -> 10)
                    .addSafeHandler("logic3",
                            s -> {throw new RuntimeException("forced exception");},
                            s -> app.updateInput("logic3", "Success!"));

            app.setHome(handlers);
            app.run();

            assertAll(
                    () -> assertEquals(5, app.getInput("logic1")),
                    () -> assertEquals(10, app.getInput("logic2")),
                    () -> assertEquals("Success!", app.getInput("logic3"))
            );
        }

        @Test
        void testEqualTo() {
            TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");
            TUIFunctionModule.Builder logic = new TUIFunctionModule.Builder("logic", () -> 5);

            TUITextInputModule.InputHandlers handlers1 = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic);

            TUITextInputModule.InputHandlers handlers2 = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic);

            TUITextInputModule.InputHandlers handlers3 = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic);

            TUITextInputModule.InputHandlers handlers4 = new TUITextInputModule.InputHandlers("handlers", new TUITextInputModule.Builder())
                    .addHandler(logic);

            TUITextInputModule.InputHandlers handlers5 = new TUITextInputModule.InputHandlers("handlers", module);

            TUIFunctionModule.Builder other = new TUIFunctionModule.Builder("other", () -> 5);
            TUITextInputModule.InputHandlers handlers6 = new TUITextInputModule.InputHandlers("handlers", module)
                    .addHandler(logic)
                    .addHandler(other);
            // this is just to get the num to something different, everything else should be the same
            handlers6.getChild("handlers-main").getChildren().remove(handlers6.getChild("handlers-2"));

            TUITextInputModule.InputHandlers handlers7 = new TUITextInputModule.InputHandlers("other", module)
                    .addHandler(logic);


            assertAll(
                    () -> assertTrue(handlers1.equalTo(handlers1, handlers1)),
                    () -> assertTrue(handlers1.equalTo(handlers1, handlers2)),
                    () -> assertTrue(handlers1.equalTo(handlers2, handlers1)),
                    () -> assertTrue(handlers5.equalTo(handlers2, handlers1)),
                    () -> assertTrue(handlers1.equalTo(handlers2, handlers3)),
                    () -> assertTrue(handlers1.equalTo(handlers1, handlers3)),
                    () -> assertFalse(handlers1.equalTo(handlers1, handlers4)),
                    () -> assertFalse(handlers1.equalTo(handlers1, handlers5)),
                    () -> assertFalse(handlers1.equalTo(handlers1, handlers6)),
                    () -> assertFalse(handlers1.equalTo(handlers1, handlers7))
            );


        }

        @Nested
        class InputHandlerTest {

            @Test
            void testCopy() {
                Function<String, String> logic = s -> s;
                Consumer<String> exceptionHandler = s -> {System.out.print("");};
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler original = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler copy = original.getCopy();
                assertAll(
                        () -> assertTrue(copy.equals(original)),
                        () -> assertTrue(copy.getModule() == original.getModule() || copy.getModule().equals(original.getModule())),
                        () -> assertEquals(copy.getLogic(), original.getLogic()),
                        () -> assertEquals(copy.getExceptionHandler(), original.getExceptionHandler())
                );
            }

            @Test
            void testSetHandlerModule() {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");
                TUIFunctionModule.Builder logic = new TUIFunctionModule.Builder("logic", () -> 5)
                        .setApplication(app);

                TUITextInputModule.InputHandler handler = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler(logic);

                app.setHome(handler);

                app.run();

                assertEquals(5, app.getInput("logic", Integer.class));
            }

            @Test
            void testSetHandlerLogic() {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler handler = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", s -> 5);

                app.setHome(handler);

                app.run();

                assertEquals(5, app.getInput("logic", Integer.class));
            }

            @Test
            void testSetHandlerLogicExceptionHandler() {
                TUIApplicationModule app = new TUIApplicationModule.Builder("app").build();
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler handler = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic",
                                s -> {throw new RuntimeException("forced exception");},
                                s -> app.updateInput("logic", "Success!")
                        );


                app.setHome(handler);
                app.run();

                assertEquals("Success!", app.getInput("logic", String.class));
            }

            @Test
            void testEqualTo() {
                Function<String, String> logic = s -> s;
                Consumer<String> exceptionHandler = s -> {System.out.print("");};
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler handler1 = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler handler2 = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler handler3 = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler handler4 = new TUITextInputModule.InputHandler("handler", new TUITextInputModule.Builder("a", "b"))
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler handler5 = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic);

                TUITextInputModule.InputHandler handler6 = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("other-name", logic, exceptionHandler);

                TUITextInputModule.InputHandler handler7 = new TUITextInputModule.InputHandler("other-name", module)
                        .setHandler("logic", logic, exceptionHandler);

                assertAll(
                        () -> assertTrue(handler1.equalTo(handler1, handler1)),
                        () -> assertTrue(handler1.equalTo(handler1, handler2)),
                        () -> assertTrue(handler1.equalTo(handler2, handler1)),
                        () -> assertTrue(handler5.equalTo(handler2, handler1)),
                        () -> assertTrue(handler1.equalTo(handler2, handler3)),
                        () -> assertTrue(handler1.equalTo(handler1, handler3)),
                        () -> assertFalse(handler1.equalTo(handler1, handler4)),
                        () -> assertFalse(handler1.equalTo(handler1, handler5)),
                        () -> assertFalse(handler1.equalTo(handler1, handler6)),
                        () -> assertFalse(handler1.equalTo(handler1, handler7))

                );

            }

            @Test
            void testBuild() {
                TUIFunctionModule.Builder logic = new TUIFunctionModule.Builder("logic", () -> 5);
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler builder = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler(logic);

                TUIContainerModule first = builder.build();
                TUIContainerModule second = builder.build();

                assertAll(
                        () -> assertTrue(first.equals(second)),
                        () -> assertTrue(first.getChild("handler-main").getChildren().contains(logic)),
                        () -> assertEquals(1, first.getChild("handler-main").getChildren().size())
                );
            }
        }
    }
}