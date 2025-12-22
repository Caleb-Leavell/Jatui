package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.*;

public class NameOrModuleTest {

    @Test
    void testGetModuleFromModule() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        ContainerModule.Builder container = new ContainerModule.Builder("module");

        NameOrModule module = new NameOrModule(container);

        assertEquals(container, module.getModule(app));
    }

    @Test
    void testGetModuleFromName() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        ContainerModule.Builder container = new ContainerModule.Builder("module");
        app.setHome(container);

        NameOrModule module = new NameOrModule("module");

        assertEquals(container, module.getModule(app));
    }

    @Test
    void testGetCopyFromModule() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        ContainerModule.Builder container = new ContainerModule.Builder("module");

        NameOrModule original = new NameOrModule(container);
        NameOrModule copy = original.getCopy();

        assertAll(
                () -> assertTrue(original.getModule(app).structuralEquals(copy.getModule(app))),
                () -> assertNotEquals(original.getModule(app), copy.getModule(app))
        );
    }

    @Test
    void testGetCopyFromName() {
        ApplicationModule app = new ApplicationModule.Builder("app").build();
        ContainerModule.Builder container = new ContainerModule.Builder("module");
        app.setHome(container);

        NameOrModule original = new NameOrModule("module");
        NameOrModule copy = original.getCopy();

        assertEquals(original.getModule(app), copy.getModule(app));
    }
}
