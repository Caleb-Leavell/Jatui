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

import java.util.Map;

/**
 * This class is an extension of {@link TUIModule.Builder} that provides a {@code main} field
 * which will be accessible inside the class extending this. The purpose of {@code main} is
 * to separate children added from inside the class from children added from whoever is
 * instantiating the class. For example, suppose MyTemplate extends Template, and a user
 * calls it as follows:
 * <pre><code>MyTemplate = new MyTemplate("template").addChild(child, 2)</code></pre>
 * <br>
 * This adds the child at index 2, potentially puts the new child inside the automatically added children
 * and breaking the atomicity of the template (i.e., that it runs as intended <i>before</i> user-added logic
 * runs).
 * Putting the children inside {@code main} ensures all automatically added children
 * run as expected and are organized.
 * <br><br>
 * Template also enforces building to a {@link ContainerModule}, which improves modularity.
 * @param <B> The class extending this (e.g., {@code class MyTemplate extends ModuleTemplate<MyTemplate>}).
 */
public abstract class ModuleTemplate<B extends ModuleTemplate<B>> extends TUIModule.Builder<B> {

    /**
     * A container for separating children essential to the function of the class
     * from children added after instantiation. See {@link TUIModule} for intuition.
     **/
    protected ContainerModule.Builder main;

    /**
     * Creates the new Template and adds {@code main} as the first child.
     *
     * @param type The type of the class extending {@link ModuleTemplate}
     * @param name The unique identifier for the module.
     */
    public ModuleTemplate(Class<B> type, String name) {
        super(type, name);
        main = ContainerModule.builder(name + "-main");
        this.addChild(main);
    }

    /**
     * Creates a fresh instance for copying utility.
     *
     * @param type The type of the class extending {@link ModuleTemplate}
     */
    protected ModuleTemplate(Class<B> type) {
        super(type);
    }

    /**
     * Copies all data of {@code original} into this module, including a deep copy
     * of all children.
     *
     * @param original The module to copy from.
     * @param visited All children that have already been deep-copied.
     * @return The instance that was copied into (self if {@code original} hasn't been visited yet).
     *
     * @implNote
     * Re-assigns {@code main} to the copy that was created from the recursive children copying.
     */
    @Override
    protected B deepCopy(B original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
        super.deepCopy(original, visited);
        main = (ContainerModule.Builder) visited.get(original.main);
        return self();
    }

    /**
     * Builds the finalized ContainerModule
     * <br><br>
     * <strong>Note:</strong> If you are going to override this method,
     * ensure any changes made to main or other children are reset each time it's called.
     * We want to ensure calling build() multiple times returns the same output.
     * Most likely, you'll want to call main.clearChildren() as the first line of the override.
     *
     * @return The built ContainerModule
     */
    @Override
    public ContainerModule build() {
        main.updateProperties(this);
        return new ContainerModule(self());
    }
}
