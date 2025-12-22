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
        Consumer<String> exceptionHandler = s -> System.out.print("");
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");

        InputHandler original = new InputHandler("handler", module.getName())
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
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");
        FunctionModule.Builder logic = new FunctionModule.Builder("logic", () -> 5)
                .setApplication(app);

        InputHandler handler = new InputHandler("handler", module.getName())
                .setHandler(logic);

        app.setHome(handler);

        app.run();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogic() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");

        InputHandler handler = new InputHandler("handler", module.getName())
                .setHandler("logic", s -> 5);

        app.setHome(handler);

        app.run();

        assertEquals(5, app.getInput("logic", Integer.class));
    }

    @Test
    void testSetHandlerLogicExceptionHandler() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");

        InputHandler handler = new InputHandler("handler", module.getName())
                .setHandler("logic",
                        s -> {throw new RuntimeException("forced exception");},
                        s -> app.updateInput("logic", "Success!")
                );


        app.setHome(handler);
        app.run();

        assertEquals("Success!", app.getInput("logic", String.class));
    }

    @Test
    void testShallowShallowStructuralEquals() {
        Function<String, String> logic = s -> s;
        Consumer<String> exceptionHandler = s -> System.out.print("");
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");

        InputHandler handler1 = new InputHandler("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler2 = new InputHandler("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler3 = new InputHandler("handler", module.getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler4 = new InputHandler("handler", new TextInputModule.Builder("a", "b").getName())
                .setHandler("logic", logic, exceptionHandler);

        InputHandler handler5 = new InputHandler("handler", module.getName())
                .setHandler("logic", logic);

        InputHandler handler6 = new InputHandler("handler", module.getName())
                .setHandler("other-name", logic, exceptionHandler);

        InputHandler handler7 = new InputHandler("other-name", module.getName())
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
        FunctionModule.Builder logic = new FunctionModule.Builder("logic", () -> 5);
        TextInputModule.Builder module = new TextInputModule.Builder("input", "Your input: ");

        InputHandler builder = new InputHandler("handler", module.getName())
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