package com.calebleavell.tuiava.modules;

import java.util.*;

public class TUIModuleFactory {

    /**
     * Returns an empty ContainerModule - simply wraps the basic logic to allow for consistent code (utilizing TUIModuleFactor) if desired.
     * @param name The name of the module
     * @return The empty ContainerModule
     */
    public static TUIContainerModule.Builder Empty(String name) {
        return new TUIContainerModule.Builder(name);
    }

    /**
     * Builds a Function Module that calls another module's terminate method.
     * @param moduleToTerminate  The module to terminate (it will terminate when this module is run)
     * @param name The name of the module to be returned
     * @return The Function Module that terminates the inputted module
     */
    public static TUIFunctionModule.Builder Terminate(TUIModule moduleToTerminate, String name) {
        return new TUIFunctionModule.Builder(name, moduleToTerminate::terminate);
    }

    /**
     * Builds a Function Module that finds the child of a parent by name then terminates it.
     * Will do nothing no if the parent's module tree does not have a child with the given name.
     * @param moduleToTerminate The name of the module to terminate (it will terminate when this module is run)
     * @param parentModule The module that the <strong>module to terminate</strong> is the child of
     * @param name The name of the module to be returned
     * @return The Function Module that terminates the inputted module
     */
    public static TUIFunctionModule.Builder Terminate(String moduleToTerminate, TUIModule parentModule, String name) {
        return new TUIFunctionModule.Builder(name, () -> {
            TUIModule toTerminate = parentModule.getChild(moduleToTerminate);
            if(toTerminate != null) toTerminate.terminate();
        });
    }

    /**
     * Builds a Function Module that calls another module's run method.
     * @param moduleToRun The module to run (it will run when this module is run)
     * @param name The name of the module to be returned
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder Run(TUIModule moduleToRun, String name) {
        return new TUIFunctionModule.Builder(name, moduleToRun::run);
    }

    /**
     * Builds a Function Module that finds the child of a parent by name then runs it.
     * Will do nothing no if the parent's module tree does not have a child with the given name.
     * @param moduleToRun The name of the module to run (it will run when this module is run)
     * @param parentModule The module that the <strong>module to run</strong> is the child of
     * @param name The name of the module to be returned
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder Run(String moduleToRun, TUIModule parentModule, String name) {
        return new TUIFunctionModule.Builder(name, () -> {
            TUIModule toRun = parentModule.getChild(moduleToRun);
            if(toRun != null) toRun.run();
        });
    }

    /**
     *
     * @param name
     * @param inputName
     * @param selectorModuleName
     * @param app
     * @param modules
     * @return
     */
    public static TUIFunctionModule.Builder Run(String name, String inputName, String selectorModuleName, TUIApplicationModule app, List<TUIModule.NameOrModule> modules) {
        return new TUIFunctionModule.Builder(name, () -> {
            int choice;
            TUIModule.NameOrModule moduleChoice;
            TUIModule moduleToRun;
            try {
                String choiceInput = app.getInput(inputName, String.class);
                if(choiceInput == null) return "Error: incorrect inputName \"" + inputName + "\" or improper updating of app input";
                choice = Integer.parseInt(choiceInput);
                moduleChoice = modules.get(choice - 1);
                moduleToRun = moduleChoice.getModule(app);
                if(moduleToRun == null) return "Error: The selected scene doesn't exist";
            }
            catch(NumberFormatException|IndexOutOfBoundsException e) {
                System.out.println("Error: Invalid Input (1-" + modules.size() + ")");
                TUIModule thisModule = app.getChild(selectorModuleName);
                if(thisModule == null) return "Error: module \"" + name + "\" didn't get added to the app";
                // update directly instead of returning after because otherwise recursion would make the error the final update
                app.updateInput(name, "Error: Invalid User Input");
                thisModule.run();
                return app.getInput(name);
            }
            moduleToRun.run();
            return "Success: retrieved and ran scene " + moduleToRun.getName();
        });
    }

    /**
     * Builds a Function Module that increments a counter every time it's run (starts at 1).
     * This is useful for building things like lists dynamically.
     * To access the counter, call <app>.getInput(<name>, Integer.class). Note that this will likely be null before this module is run.
     * @param name The name of the module to be returned
     * @param app The Application Module that this module will be the child of
     * @return The Function Module that increments a counter
     */
    public static TUIFunctionModule.Builder Counter(TUIApplicationModule app, String name) {
        return Counter(app, name, 1, 1);
    }

    /**
     * Builds a Function Module that increments a counter every time it's run (starts at 1).
     * To access the counter, call <app>.getInput(<name>, Integer.class). Note that this will likely be null before this module is run.
     * @param name The name of the module to be returned
     * @param app The Application Module that this module will be the child of
     * @param begin The number to begin at (eg. begin = 5 -> 5, 6, 7, 8, ...)
     * @param step The amount to increment each time (eg. step = 5 -> 1, 6, 11, ...)
     * @return The Function Module that increments a counter
     */
    public static TUIFunctionModule.Builder Counter(TUIApplicationModule app, String name, int begin, int step) {
        return new TUIFunctionModule.Builder(name, () -> {
            Integer counter = app.getInput(name, Integer.class);
            if(counter != null)  return counter + step;
            else return begin;
        });
    }

    public static class NumberedList extends TUIGenericModule.Builder<NumberedList> {
        private List<String> listText;
        private int start = 1;
        private int step = 1;
        private String inputVariableName;
        private List<TUITextInputModule> getInput = new ArrayList<>();

        public NumberedList(String name, String... listText) {
            super(NumberedList.class, name);
            this.listText = new ArrayList<String>(Arrays.asList(listText));
        }

        public NumberedList addListText(String listText) {
            this.listText.add(listText);
            return this;
        }

        public NumberedList start(int start) {
            this.start = start;
            return this;
        }

        public NumberedList step(int step) {
            this.step = step;
            return this;
        }

        /**
         * Collect input after the list is displayed (you can add more than 1).
         * @param inputMessage The input message displayed to the user
         * @param inputIdentifier The variable name of the input used when calling TUIApplicationModule.getInput()
         * @return self
         */
        public NumberedList collectInput(String inputMessage, String inputIdentifier) {
            TUITextInputModule input = new TUITextInputModule.Builder(inputMessage, inputIdentifier).build();
            getInput.add(input);
            return self();
        }

        @Override
        public TUIContainerModule build() {
            TUIContainerModule.Builder main = new TUIContainerModule.Builder(name + "-main");
            for(int i = 0; i < listText.size(); i ++) {
                int currentNum = (i * step) + start;
                main.addChild(
                        new TUITextModule.Builder(name + "-" + currentNum, currentNum + ". " + listText.get(i))
                );
            }
            for(var input : getInput) {
                main.addChild(input);
            }
            this.addChild(0, main);
            return new TUIContainerModule(this);
        }
    }

    public static class NumberedModuleSelector extends TUIGenericModule.Builder<NumberedModuleSelector> {
        private List<TUIModule.NameOrModule> modules = new ArrayList<>();
        private List<String> listText = new ArrayList<>();
        private TUIApplicationModule app;

        public NumberedModuleSelector(String name, TUIApplicationModule app, String... moduleNames) {
            super(NumberedModuleSelector.class, name);
            Arrays.asList(moduleNames).forEach(m -> modules.add(new TUIModule.NameOrModule(m)));
            this.app = app;
        }

        public NumberedModuleSelector(String name, TUIApplicationModule app, TUIModule... modules) {
            super(NumberedModuleSelector.class, name);
            Arrays.asList(modules).forEach(m -> this.modules.add(new TUIModule.NameOrModule(m)));
            this.app = app;
        }

        public NumberedModuleSelector listText(String... listText) {
            this.listText.addAll(Arrays.asList(listText));
            return self();
        }

        public NumberedModuleSelector addScene(String moduleName) {
            this.modules.add(new TUIModule.NameOrModule(moduleName));
            return self();
        }

        public NumberedModuleSelector addScene(TUIModule module) {
            this.modules.add(new TUIModule.NameOrModule(module));
            return self();
        }

        public NumberedModuleSelector addScenes(String... moduleNames) {
            Arrays.asList(moduleNames).forEach(m -> modules.add(new TUIModule.NameOrModule(m)));
            return self();
        }

        public NumberedModuleSelector addScenes(TUIModule... modules) {
            Arrays.asList(modules).forEach(m -> this.modules.add(new TUIModule.NameOrModule(m)));
            return self();
        }

        @Override
        public TUIContainerModule build() {
            NumberedList list = new NumberedList(name + "-list");

            if(listText != null && listText.size() == modules.size()) {
                listText.forEach(list::addListText);
            }
            else {
                for(TUIModule.NameOrModule m : modules) {
                    list.addListText(m.getModule(app).getName());
                }
            }

            list.collectInput("Your choice: ", name + "-input");

            list.addChild(TUIModuleFactory.Run(name+"-goto-module", name+"-input", name, app, modules));

            this.addChild(0, list);

            return new TUIContainerModule(this);
        }
    }


//    public static abstract class ModuleBuilder extends TUIContainerModule.Builder {
//        public ModuleBuilder(String name) {
//            super(TUIContainerModule.Builder.class, name);
//        }
//
//        public TUIContainerModule build() {
//            return new TUIContainerModule(self());
//        }
//    }
}
