package com.calebleavell.jatui.modules;

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
     * Builds a Function Module that calls another module's run method.
     * @param moduleToRun The module to run (it will run when this module is run)
     * @param name The name of the module to be returned
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder Run(TUIModule.Builder<?> moduleToRun, String name) {
        return new TUIFunctionModule.Builder(name, () -> moduleToRun.build().run());
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
            TUIModule.Builder<?> toRun = parentModule.getChild(moduleToRun);
            if(toRun != null) toRun.build().run();
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
            TUIModule.Builder<?> moduleToRun;
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
                TUIModule.Builder<?> thisModule = app.getChild(selectorModuleName);
                if(thisModule == null) return "Error: module \"" + name + "\" didn't get added to the app";
                // update directly instead of returning after because otherwise recursion would make the error the final update
                app.updateInput(name, "Error: Invalid User Input");
                thisModule.build().run();
                return app.getInput(name);
            }
            moduleToRun.build().run();
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
     * @param begin The number to begin at (e.g. begin = 5 -> 5, 6, 7, 8, ...)
     * @param step The amount to increment each time (e.g. step = 5 -> 1, 6, 11, ...)
     * @return The Function Module that increments a counter
     */
    public static TUIFunctionModule.Builder Counter(TUIApplicationModule app, String name, int begin, int step) {
        return new TUIFunctionModule.Builder(name, () -> {
            Integer counter = app.getInput(name, Integer.class);
            if(counter != null)  return counter + step;
            else return begin;
        });
    }

    public static class NumberedList extends TUIModule.Template<NumberedList> {
        private List<String> listText;
        private int start = 1;
        private int step = 1;
        private String inputVariableName;
        private List<TUITextInputModule.Builder> getInput = new ArrayList<>();

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
        public NumberedList collectInput(String inputIdentifier, String inputMessage) {
            TUITextInputModule.Builder input = new TUITextInputModule.Builder(inputIdentifier, inputMessage);
            getInput.add(input);
            return self();
        }

        @Override
        public TUIContainerModule build() {
            for(int i = 0; i < listText.size(); i ++) {
                int currentNum = (i * step) + start;
                main.addChild(
                        new TUITextModule.Builder(name + "-" + currentNum, currentNum + ". " + listText.get(i))
                );
            }
            for(var input : getInput) {
                main.addChild(input);
            }

            return super.build();
        }
    }

    public static class NumberedModuleSelector extends TUIModule.Template<NumberedModuleSelector> {
        private List<TUIModule.NameOrModule> modules = new ArrayList<>();
        private List<String> listText = new ArrayList<>();
        private TUIApplicationModule app;

        public NumberedModuleSelector(String name, TUIApplicationModule app, String... moduleNames) {
            super(NumberedModuleSelector.class, name);
            Arrays.asList(moduleNames).forEach(m -> modules.add(new TUIModule.NameOrModule(m)));
            this.app = app;
        }

        public NumberedModuleSelector(TUIApplicationModule app, String name, TUIModule.Builder<?>... modules) {
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

        public NumberedModuleSelector addScene(TUIModule.Builder<?> module) {
            this.modules.add(new TUIModule.NameOrModule(module));
            return self();
        }

        public NumberedModuleSelector addScenes(String... moduleNames) {
            Arrays.asList(moduleNames).forEach(m -> modules.add(new TUIModule.NameOrModule(m)));
            return self();
        }

        public NumberedModuleSelector addScenes(TUIModule.Builder<?>... modules) {
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

            list.collectInput(name + "-input", "Your choice: ");

            list.addChild(TUIModuleFactory.Run(name+"-goto-module", name+"-input", name, app, modules));

            main.addChild(0, list);

            return super.build();
        }
    }

    public static class TextBuilder extends TUIModule.Template<TextBuilder> {
        private int textCounter = 0;

        public TextBuilder(String name) {
            super(TextBuilder.class, name);
        }

        public TextBuilder addText(String text, boolean printNewLine, TUITextModule.OutputType outputType) {
            TUITextModule.Builder module = new TUITextModule.Builder(name + "-" + textCounter, text)
                    .printNewLine(printNewLine)
                    .outputType(outputType);

            main.addChild(module);

            textCounter ++;

            return self();
        }

        public TextBuilder addText(String text, boolean printNewLine) {
            return addText(text, printNewLine, TUITextModule.OutputType.TEXT);
        }

        public TextBuilder addText(String text) {
            return addText(text, false);
        }

        public TextBuilder addModuleOutputDisplay(String moduleName) {
            return addText(moduleName, true, TUITextModule.OutputType.OUTPUT_OF_MODULE_NAME);
        }
    }


}
