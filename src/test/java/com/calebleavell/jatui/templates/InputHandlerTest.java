/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
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
        Consumer<String> exceptionHandler = _ -> System.out.print("");
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler original = InputHandler.builder("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

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
                .setApplication(app);

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .setHandler(logic);

        app.setHome(handler);

        app.run();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogic() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .setHandler("logic", _ -> 5);

        app.setHome(handler);

        app.run();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogicExceptionHandler() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler = InputHandler.builder("handler", module.getName())
                .setHandler("logic",
                        _ -> {throw new RuntimeException("forced exception");},
                        _ -> app.updateInput("logic", "Success!")
                );


        app.setHome(handler);
        app.run();

        assertEquals("Success!", app.getInput("logic", String.class));
    }

    @Test
    void testShallowShallowStructuralEquals() {
        Function<String, String> logic = s -> s;
        Consumer<String> exceptionHandler = _ -> System.out.print("");
        TextInputModule.Builder module = TextInputModule.builder("input", "Your input: ");

        InputHandler handler1 = InputHandler.builder("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler2 = InputHandler.builder("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler3 = InputHandler.builder("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler4 = InputHandler.builder("handler", TextInputModule.builder("a", "b").getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler5 = InputHandler.builder("handler", module.getName())
                .setHandler("logic", logic);

        InputHandler handler6 = InputHandler.builder("handler", module.getName())
                .setHandler("other-name", logic, exceptionHandler);

        InputHandler handler7 = InputHandler.builder("other-name", module.getName())
                .setHandler("logic", logic, exceptionHandler);

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
                .setHandler(logic);

        ContainerModule first = builder.build();
        ContainerModule second = builder.build();

        assertAll(
                () -> assertTrue(first.structuralEquals(second)),
                () -> assertTrue(first.getChild("handler-main").getChildren().contains(logic)),
                () -> assertEquals(1, first.getChild("handler-main").getChildren().size())
        );
    }
}