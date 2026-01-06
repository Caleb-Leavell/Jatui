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
    private final List<NameOrModule> modules = new ArrayList<>();
    private NumberedList list;

    protected NumberedModuleSelector(String name, ApplicationModule app) {
        super(NumberedModuleSelector.class, name);
        this.application(app);
        list = NumberedList.builder(name + "-list");
        TextInputModule.Builder collectInput = TextInputModule.builder(name + "-input", "Your choice: ")
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


    @Override
    public void shallowCopy(NumberedModuleSelector original) {
        for(NameOrModule m : original.modules) {
            this.modules.add(m.getCopy());
        }
        this.list = original.list.getCopy();
        super.shallowCopy(original);
    }

    private NumberedModuleSelector addModule(String displayText, NameOrModule module){
        logger.trace("adding module with displayText \"{}\" to NumberedModuleSelector \"{}\"", displayText, getName());
        this.modules.add(module);
        list.addListText(displayText);
        return self();
    }

    public NumberedModuleSelector addModule(String displayText, String moduleName) {
        return addModule(displayText, new NameOrModule(moduleName));
    }

    public NumberedModuleSelector addModule(String displayText, TUIModule.Builder<?> module) {
        return addModule(displayText, new NameOrModule(module));
    }

    public NumberedModuleSelector addModule(String moduleName) {
        return addModule(moduleName, moduleName);
    }

    public NumberedModuleSelector addModule(TUIModule.Builder<?> module) {
        return addModule(module.getName(), module);
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For InputHandlers, this includes: </p>
     * <ul>
     *     <li><strong>modules</strong> (the actual list of modules that are selected from) </li>
     *     <li><strong>list</strong> (the NumberedList that displays the options and collects input) </li>
     *     <li><strong>app</strong> (the app that the list of modules goes to, which may or may not be the same as the app for this module)</li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     *
     * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered. Children are also not considered here,
     *  but are considered in equals()
     * @param first The first NumberedList to compare
     * @param second The second NumberedList to compare
     * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
     *
     * @implNote
     *This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
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
