package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NumberedModuleSelector extends ModuleTemplate<NumberedModuleSelector> {
    private final List<NameOrModule> modules = new ArrayList<>();
    private ApplicationModule app;
    private NumberedList list;

    public NumberedModuleSelector(String name, ApplicationModule app) {
        super(NumberedModuleSelector.class, name);
        this.app = app;
        list = new NumberedList(name + "-list");
        TextInputModule.Builder collectInput = new TextInputModule.Builder(name + "-input", "Your choice: ")
                .addSafeHandler(name + "-goto-module", input -> {
                    int index = Integer.parseInt(input);
                    NameOrModule nameOrModule = modules.get(index - 1);
                    TUIModule.Builder<?> toRun = nameOrModule.getModule(app);
                    if(toRun == null) logger.error("nameOrModule returned null module for NumberedModuleSelector \"{}\"", getName());
                    else app.runModuleAsChild(toRun);
                    return "Successfully ran selected module";
                });
        main.addChild(list);
        main.addChild(collectInput);
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
        this.app = original.app;
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

            TUIModule.Builder<?> firstModule = firstNameOrModule.getModule(this.app);
            TUIModule.Builder<?> secondModule = secondNameOrModule.getModule(this.app);

            if(!TUIModule.Builder.structuralEquals(firstModule, secondModule)) return false;
        }

        return Objects.equals(first.app, second.app) &&
                super.shallowStructuralEquals(first, second);
    }
}
