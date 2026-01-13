/*
    Copyright (c) 2026 Caleb Leavell

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.modules.ApplicationModule;
import com.calebleavell.jatui.modules.ContainerModule;
import com.calebleavell.jatui.modules.FunctionModule;
import com.calebleavell.jatui.modules.TextInputModule;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class InputHandlerTest {

    @Test
    void testCopy() {
        Function<String, String> logic = s -> s;
        Consumer<String> exceptionHandler = ignored -> System.out.print("");
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler original = InputHandler.builder("handler", module.getName())
                .handler("logic", logic, exceptionHandler);

        InputHandler copy = original.getCopy();
        assertAll(
                () -> assertTrue(copy.structuralEquals(original)),
                () -> assertTrue(copy.getModule() == original.getModule() || copy.getModule().structuralEquals(original.getModule())),
                () -> assertEquals(copy.getLogic(), original.getLogic()),
                () -> assertEquals(copy.getExceptionHandler(), original.getExceptionHandler())
        );
    }

    @Test
    void testSetHandlerModule() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");
        FunctionModule.Builder logic = FunctionModule.builder("logic", () -> 5)
                .application(app);

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .handler(logic);

        app.setHome(handler);

        app.start();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogic() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .handler("logic", ignored -> 5);

        app.setHome(handler);

        app.start();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogicExceptionHandler() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .handler("logic",
                        ignored -> {throw new RuntimeException("forced exception");},
                        ignored -> app.updateInput("logic", "Success!")
                );


        app.setHome(handler);
        app.start();

        assertEquals("Success!", app.getInput("logic", String.class));
    }

    @Test
    void testShallowShallowStructuralEquals() {
        Function<String, String> logic = s -> s;
        Consumer<String> exceptionHandler = ignored -> System.out.print("");
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler1 = InputHandler.builder("handler", module.getName())
                .handler("logic", logic, exceptionHandler);

        InputHandler handler2 = InputHandler.builder("handler", module.getName())
                .handler("logic", logic, exceptionHandler);

        InputHandler handler3 = InputHandler.builder("handler", module.getName())
                .handler("logic", logic, exceptionHandler);

        InputHandler handler4 = InputHandler.builder("handler", TextInputModule.builder("a", "b").getName())
                .handler("logic", logic, exceptionHandler);

        InputHandler handler5 = InputHandler.builder("handler", module.getName())
                .handler("logic", logic);

        InputHandler handler6 = InputHandler.builder("handler", module.getName())
                .handler("other-name", logic, exceptionHandler);

        InputHandler handler7 = InputHandler.builder("other-name", module.getName())
                .handler("logic", logic, exceptionHandler);

        assertAll(
                () -> assertTrue(handler1.shallowStructuralEquals(handler1, handler1)),
                () -> assertTrue(handler1.shallowStructuralEquals(handler1, handler2)),
                () -> assertTrue(handler1.shallowStructuralEquals(handler2, handler1)),
                () -> assertTrue(handler5.shallowStructuralEquals(handler2, handler1)),
                () -> assertTrue(handler1.shallowStructuralEquals(handler2, handler3)),
                () -> assertTrue(handler1.shallowStructuralEquals(handler1, handler3)),
                () -> assertFalse(handler1.shallowStructuralEquals(handler1, handler4)),
                () -> assertFalse(handler1.shallowStructuralEquals(handler1, handler5)),
                () -> assertFalse(handler1.shallowStructuralEquals(handler1, handler6)),
                () -> assertFalse(handler1.shallowStructuralEquals(handler1, handler7))
        );
    }

    @Test
    void testBuild() {
        FunctionModule.Builder logic = FunctionModule.builder("logic", () -> 5);
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler builder = InputHandler.builder("handler", module.getName())
                .handler(logic);

        ContainerModule first = builder.build();
        ContainerModule second = builder.build();

        assertAll(
                () -> assertTrue(first.structuralEquals(second)),
                () -> assertTrue(first.getChild("handler-main").getChildren().contains(logic)),
                () -> assertEquals(1, first.getChild("handler-main").getChildren().size())
        );
    }
}