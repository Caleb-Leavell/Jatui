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

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles navigating to a module based on a user's decision.
 * <br><br>
 * Example usage:
 * <pre><code>
 * ApplicationModule app = ApplicationModule.builder("app").build();
 *
 * NumberedModuleSelector selector = NumberedModuleSelector.builder("selector", app)
 *  .addModule("Display Hello", TextModule.builder("display-hello", "Hello"))
 *  .addModule("Display World", TextModule.builder("display-world", "World"));
 *
 * app.setHome(selector);
 * app.run();
 * </code></pre>
 *
 * Output:
 * <pre>
 * [1] Display Hello
 * [2] Display World
 * Your choice: 1
 * Hello
 * Exiting...
 * </pre>
 */
public class NumberedModuleSelector extends ModuleTemplate<NumberedModuleSelector> {
    /**
     * The {@link NameOrModule} objects that keep a reference to the modules that can be selected.
     */
    private final List<NameOrModule> modules = new ArrayList<>();

    /**
     * The {@link NumberedList} that displays all the selection options.
     */
    private NumberedList list;

    /**
     * Constructor for {@link NumberedModuleSelector}.
     *
     * @param name The name of the module.
     * @param app The application to work with. Calls {@link TUIModule.Builder#application(ApplicationModule)}.
     */
    protected NumberedModuleSelector(String name, ApplicationModule app) {
        super(NumberedModuleSelector.class, name);
        this.application(app);
        list = NumberedList.builder(name + "-list");
        TextInputModule.Builder collectInput = TextInputModule.builder(name + "-input", "Your choice: ")
                .application(app)
                .addSafeHandler(name + "-goto-module", input -> {
                    int index = Integer.parseInt(input);
                    NameOrModule nameOrModule = modules.get(index - 1);
                    TUIModule.Builder<?> toRun = nameOrModule.getModule(app);
                    if(toRun == null) logger.error("nameOrModule returned null module for NumberedModuleSelector \"{}\"", getName());
                    else app.navigateTo(toRun);
                    return "Successfully ran selected module";
                });
        main.addChild(list);
        main.addChild(collectInput);
    }

    /**
     * Constructs a new {@link NumberedModuleSelector} builder.
     *
     * @param name The name of the builder.
     * @param app The {@link ApplicationModule} this module will be tied to.
     * @return The new builder.
     */
    public static NumberedModuleSelector builder(String name, ApplicationModule app) {
        return new NumberedModuleSelector(name, app);
    }

    protected NumberedModuleSelector() {
        super(NumberedModuleSelector.class);
    }

    /**
     * Gets a fresh instance of this type of Builder.
     *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
     * @return A fresh, empty instance.
     */
    @Override
    protected NumberedModuleSelector createInstance() {
        return new NumberedModuleSelector();
    }

    /**
     * Copies the {@link NameOrModule} objects and the {@link NumberedList} object,
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
    @Override
    public void shallowCopy(NumberedModuleSelector original) {
        for(NameOrModule m : original.modules) {
            this.modules.add(m.getCopy());
        }
        this.list = original.list.getCopy();
        super.shallowCopy(original);
    }

    /**
     * Adds a module option to the selector. Adds {@code displayText} via {@link NumberedList#addListText(String)}.
     *
     * @param displayText The text to display (e.g., if displayText="myText" "<b>[1]</b> myText).
     * @param module The {@link NameOrModule} with a reference to the module the user may navigate to.
     * @return self
     */
    private NumberedModuleSelector addModule(String displayText, NameOrModule module){
        logger.trace("adding module with displayText \"{}\" to NumberedModuleSelector \"{}\"", displayText, getName());
        this.modules.add(module);
        list.addListText(displayText);
        return self();
    }

    /**
     * Adds a module option to the selector. Adds {@code displayText} via {@link NumberedList#addListText(String)}.
     *
     * @param displayText The text to display (e.g., if displayText="myText" "<b>[1]</b> myText).
     * @param moduleName The name of the module the user may navigate to
     *                   that is connected the {@link ApplicationModule} tied to this module
     *                   (via either {@link ApplicationModule#setHome(TUIModule.Builder)} or
     *                   {@link TUIModule.Builder#addChild(TUIModule.Builder)}). The module doesn't have
     *                   to be a direct child.
     * @return self
     */
    public NumberedModuleSelector addModule(String displayText, String moduleName) {
        return addModule(displayText, new NameOrModule(moduleName));
    }

    /**
     * Adds a module option to the selector. Adds {@code displayText} via {@link NumberedList#addListText(String)}.
     *
     * @param displayText The text to display (e.g., if displayText="myText" "<b>[1]</b> myText).
     * @param module The {@link TUIModule.Builder} the user may navigate to.
     * @return self
     */
    public NumberedModuleSelector addModule(String displayText, TUIModule.Builder<?> module) {
        return addModule(displayText, new NameOrModule(module));
    }

    /**
     * Adds a module option to the selector. Uses the name of the module as the displayed list option.
     * e.g., if the name of the module is "myModule", the option could be "<b>[1]</b> myModule."
     *
     * @param moduleName The name of the module the user may navigate to
     *                   that is connected the {@link ApplicationModule} tied to this module
     *                   (via either {@link ApplicationModule#setHome(TUIModule.Builder)} or
     *                   {@link TUIModule.Builder#addChild(TUIModule.Builder)}). The module doesn't have
     *                   to be a direct child.
     * @return self
     */
    public NumberedModuleSelector addModule(String moduleName) {
        return addModule(moduleName, moduleName);
    }


    /**
     * Adds a module option to the selector. Uses the name of the module as the displayed list option.
     * e.g., if the name of the module is "myModule", the option could be "<b>[1]</b> myModule."
     *
     * @param module The {@link TUIModule.Builder} the user may navigate to.
     * @return self
     */
    public NumberedModuleSelector addModule(TUIModule.Builder<?> module) {
        return addModule(module.getName(), module);
    }

    /**
     * Checks equality for properties given by the builder. For {@link NumberedModuleSelector}, this includes
     * {@code modules},
     * as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
     */
    @Override
    public boolean shallowStructuralEquals(NumberedModuleSelector first, NumberedModuleSelector second) {
        if(first == second) return true;
        if(first == null || second == null) return false;

        if(first.modules.size() != second.modules.size()) return false;

        for(int i = 0; i < first.modules.size(); i ++) {
            NameOrModule firstNameOrModule = first.modules.get(i);
            NameOrModule secondNameOrModule = second.modules.get(i);

            if(firstNameOrModule == secondNameOrModule) continue;
            else if(firstNameOrModule == null || secondNameOrModule == null) return false;

            TUIModule.Builder<?> firstModule = firstNameOrModule.getModule(this.getApplication());
            TUIModule.Builder<?> secondModule = secondNameOrModule.getModule(this.getApplication());

            if(!TUIModule.Builder.structuralEquals(firstModule, secondModule)) return false;
        }

        return super.shallowStructuralEquals(first, second);
    }
}
