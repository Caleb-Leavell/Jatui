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
