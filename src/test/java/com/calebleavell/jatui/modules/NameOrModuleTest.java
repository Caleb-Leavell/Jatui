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

package com.calebleavell.jatui.modules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.*;

public class NameOrModuleTest {

    @Test
    void testGetModuleFromModule() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        ContainerModule.Builder container = ContainerModule.builder("module");

        NameOrModule module = new NameOrModule(container);

        assertEquals(container, module.getModule(app));
    }

    @Test
    void testGetModuleFromName() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        ContainerModule.Builder container = ContainerModule.builder("module");
        app.setHome(container);

        NameOrModule module = new NameOrModule("module");

        assertEquals(container, module.getModule(app));
    }

    @Test
    void testGetCopyFromModule() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        ContainerModule.Builder container = ContainerModule.builder("module");

        NameOrModule original = new NameOrModule(container);
        NameOrModule copy = original.getCopy();

        assertAll(
                () -> assertTrue(original.getModule(app).structuralEquals(copy.getModule(app))),
                () -> assertNotEquals(original.getModule(app), copy.getModule(app))
        );
    }

    @Test
    void testGetCopyFromName() {
        ApplicationModule app = ApplicationModule.builder("app").build();
        ContainerModule.Builder container = ContainerModule.builder("module");
        app.setHome(container);

        NameOrModule original = new NameOrModule("module");
        NameOrModule copy = original.getCopy();

        assertEquals(original.getModule(app), copy.getModule(app));
    }
}
