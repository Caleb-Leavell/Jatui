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

import com.calebleavell.jatui.templates.NumberedModuleSelector;

/**
 * Stores either the module builder or the name of a module, which abstracts
 * module retrieving. See usage in {@link NumberedModuleSelector}
 */
public final class NameOrModule {
    private TUIModule.Builder<?> module;
    private String moduleName;

    /**
     * Sets the stored module to a concrete reference to a builder.
     * @param module The module to remember.
     */
    public NameOrModule(TUIModule.Builder<?> module) {
        this.module = module;
    }

    /**
     * Sets the stored module to the name of a builder.
     * @param moduleName The name of the module to remember.
     */
    public NameOrModule(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Fetches the module that was inputted from either the concrete reference
     * or the name. Requires the possible names to be a child of {@code app}.
     *
     * @param app The app that a potential name of the module would be a child of.
     * @return The remembered module.
     */
    public TUIModule.Builder<?> getModule(ApplicationModule app) {
        if(module != null) return module;
        else return app.getChild(moduleName);
    }

    /**
     * Creates a new instance of this {@code NameOrModule} object that remembers
     * a copy of the module if a concrete reference was stored, or the same name
     * if only the name was stored.
     *
     * @return The new copy of this instance.
     */
    public NameOrModule getCopy() {
        if(module != null) return new NameOrModule(module.getCopy());
        else return new NameOrModule(moduleName);
    }
}