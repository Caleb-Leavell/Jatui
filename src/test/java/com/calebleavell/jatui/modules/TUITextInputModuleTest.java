package com.calebleavell.jatui.modules;

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
        void testShallowCopy() {

        }

        @Test
        void testAddHandlerModule() {

        }

        @Test
        void testAddHandler() {

        }

        @Test
        void testAddSafeHandler() {

        }

        @Test
        void testEqualTo() {

        }

        @Nested
        class InputHandlerTest {

            @Test
            void testShallowCopy() {
                Function<String, String> logic = s -> s;
                Consumer<String> exceptionHandler = s -> {System.out.print("");};
                TUITextInputModule.Builder module = new TUITextInputModule.Builder("input", "Your input: ");

                TUITextInputModule.InputHandler original = new TUITextInputModule.InputHandler("handler", module)
                        .setHandler("logic", logic, exceptionHandler);

                TUITextInputModule.InputHandler copy = original.createInstance();
                copy.shallowCopy(original);

                assertAll(
                        () -> assertTrue(copy.equals(original))
                        // TODO
                );
            }

            @Test
            void testSetHandlerModule() {

            }

            @Test
            void testSetHandlerLogic() {

            }

            @Test
            void testSetHandlerLogicExceptionHandler() {

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

            }
        }
    }
}