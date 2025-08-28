package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

public class TUIModuleFactory {

    /**
     * Returns an empty ContainerModule - simply wraps <pre><code>new TUIContainerModule.Builder([name])</code></pre>
     * to allow for code that consistently uses TUIModuleFactory if desired.
     * @param name The name of the module
     * @return The empty ContainerModule
     */
    public static TUIContainerModule.Builder empty(String name) {
        return new TUIContainerModule.Builder(name);
    }

    /**
     * Returns a Function Module that calls another module's terminate method.
     *
     * @param name              The name of the module to be returned
     * @param moduleToTerminate The module to terminate (it will terminate when this module is run)
     * @return The Function Module that terminates the inputted module
     */
    public static TUIFunctionModule.Builder terminate(String name, TUIModule moduleToTerminate) {
        return new TUIFunctionModule.Builder(name, moduleToTerminate::terminate);
    }

    /**
     * Returns a Function Module that terminates the child of name {@code moduleToTerminate} that is a child of {@code parent}. <br>
     * Note: {@code moduleToTerminate} doesn't have to be a direct child of {@code parent}. It can be multiple layers deep.
     *
     * @param name The name of the module to be returned
     * @param moduleToTerminate The name of the module to terminate
     * @param parent The parent module that will terminate the module
     * @return The Function Module that terminates the module corresponding to {@code moduleToTerminate}
     */
    public static TUIFunctionModule.Builder terminate(String name, String moduleToTerminate, TUIModule parent) {
        return new TUIFunctionModule.Builder(name, () -> parent.terminateChild(moduleToTerminate));
    }

    /**
     * Returns a Function Module that calls another module's run method.
     *
     * @param name        The name of the module to be returned
     * @param moduleToRun The module to run (it will run when this module is run)
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder run(String name, TUIModule.Builder<?> moduleToRun) {
        return new TUIFunctionModule.Builder(name, () -> moduleToRun.build().run());
    }

    /**
     * Returns a Function Module that calls another module's run method.
     *
     * @param name        The name of the module to be returned
     * @param moduleToRun The module to run (it will run when this module is run)
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder run(String name, TUIModule moduleToRun) {
        return new TUIFunctionModule.Builder(name, moduleToRun::run);
    }

    /**
     * Returns a Function Module that finds the child of a parent by name then runs it.
     * Will do nothing no if the parent's module tree does not have a child with the given name.
     *
     * @param name         The name of the module to be returned
     * @param parentModule The module that the <strong>module to run</strong> is the child of
     * @param moduleToRun  The name of the module to run (it will run when this module is run)
     * @return The Function Module that calls another module's run method
     */
    public static TUIFunctionModule.Builder run(String name, TUIModule parentModule, String moduleToRun) {
        return new TUIFunctionModule.Builder(name, () -> {
            TUIModule.Builder<?> toRun = parentModule.getChild(moduleToRun);
            if(toRun != null) toRun.build().run();
        });
    }

    /**
     * Returns a Function Module that increments a counter every time it's run (starts at 1).
     * To access the counter, call <br>
     * {@code [app].getInput([name], Integer.class)}). <br>
     * Note that this will likely be null before this module is run.
     *
     * @param name The name of the module to be returned
     * @param app  The Application Module that this module will be the child of
     * @return The Function Module that increments a counter
     */
    public static TUIFunctionModule.Builder counter(String name, TUIApplicationModule app) {
        return counter(name, app, 1, 1);
    }

    /**
     * Returns a Function Module that increments a counter every time it's run (starts at 1).
     * To access the counter, call <br>
     * {@code [app].getInput([name], Integer.class)}). <br>
     * Note that this will likely be null before this module is run.
     *
     * @param name  The name of the module to be returned
     * @param app   The Application Module that this module will be the child of
     * @param begin The number to begin at (e.g. begin = 5 -> 5, 6, 7, 8, ...)
     * @param step  The amount to increment each time (e.g. step = 5 -> 1, 6, 11, ...)
     * @return The Function Module that increments a counter
     */
    public static TUIFunctionModule.Builder counter(String name, TUIApplicationModule app, int begin, int step) {
        return new TUIFunctionModule.Builder(name, () -> {
            Integer counter = app.getInput(name, Integer.class);
            if(counter != null)  return counter + step;
            else return begin;
        });
    }

    public static class NumberedList extends TUIModule.Template<NumberedList> {
        private int start = 1;
        private int step = 1;
        private int i = 0;
        private String inputVariableName;

        public NumberedList(String name, String... listText) {
            super(NumberedList.class, name);
        }

        protected NumberedList() {
            super(NumberedList.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected NumberedList createInstance() {
            return new NumberedList();
        }

        @Override
        public void shallowCopy(NumberedList original) {
            this.start = original.start;
            this.step = original.step;
            this.i = original.i;
            this.inputVariableName = original.inputVariableName;
            super.shallowCopy(original);
        }

        public NumberedList addListText(String listText) {
            int currentNum = (i * step) + start;
            main.addChild(
                    new LineBuilder(name + currentNum)
                            .addText("[" + currentNum + "] ", ansi().bold())
                            .addText(listText)
                            .newLine());
            i ++;
            return this;
        }

        public NumberedList setStart(int start) {
            this.start = start;
            return this;
        }

        public NumberedList setStep(int step) {
            this.step = step;
            return this;
        }

        /**
         * <p>Collect input after the list is displayed (you can add more than 1).</p>
         * <p>Note: you CAN collect input before displaying all list items.</p>
         * @param inputMessage The input message displayed to the user
         * @param inputIdentifier The variable name of the input used when calling TUIApplicationModule.getInput()
         * @return self
         */
        public NumberedList collectInput(String inputIdentifier, String inputMessage) {
            TUITextInputModule.Builder input = new TUITextInputModule.Builder(inputIdentifier, inputMessage);
            main.addChild(input);
            return self();
        }

        /**
         * <p>Collect input after the list is displayed (you can add more than 1).</p>
         * <p>Note: you CAN collect input before displaying all list items.</p>
         * @param input The TUITextInputModule Builder that will be collecting the input.
         * @return self
         */
        public NumberedList collectInput(TUITextInputModule.Builder input) {
            main.addChild(input);
            return self();
        }
    }

    public static class NumberedModuleSelector extends TUIModule.Template<NumberedModuleSelector> {
        private final List<TUIModule.NameOrModule> modules = new ArrayList<>();
        private TUIApplicationModule app;
        private NumberedList list;

        public NumberedModuleSelector(String name, TUIApplicationModule app) {
            super(NumberedModuleSelector.class, name);
            this.app = app;
            list = new NumberedList(name + "-list");
            TUITextInputModule.Builder collectInput = new TUITextInputModule.Builder(name + "-input", "Your choice: ")
                    .addSafeHandler(name + "-goto-module", input -> {
                        int index = Integer.parseInt(input);
                        TUIModule.NameOrModule nameOrModule = modules.get(index - 1);
                        TUIModule.Builder<?> toRun = nameOrModule.getModule(app);
                        app.runModuleAsChild(toRun);
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
            for(TUIModule.NameOrModule m : original.modules) {
                this.modules.add(m.getCopy());
            }
            this.app = original.app;
            this.list = original.list.getCopy();
            super.shallowCopy(original);
        }

        private NumberedModuleSelector addScene(String displayText, TUIModule.NameOrModule module){
            this.modules.add(module);
            list.addListText(displayText);
            return self();
        }

        public NumberedModuleSelector addScene(String displayText, String moduleName) {
            return addScene(displayText, new TUIModule.NameOrModule(moduleName));
        }

        public NumberedModuleSelector addScene(String displayText, TUIModule.Builder<?> module) {
            return addScene(displayText, new TUIModule.NameOrModule(module));
        }

        public NumberedModuleSelector addScene(String moduleName) {
            return addScene(moduleName, moduleName);
        }

        public NumberedModuleSelector addScene(TUIModule.Builder<?> module) {
            return addScene(module.getName(), module);
        }
    }

    /**
     * <p>LineBuilder simplifies chaining text together that is meant to live on the same line.</p>
     * <p>Ansi is supported with method overloads.</p>
     * <p><strong>Usage:</strong>
     * <pre><code>
     * LineBuilder text = new LineBuilder("name")
     *     .addText("Regular text: ")
     *     // this will display what the inputted module outputs
     *     .addModuleOutput("This string is the name of another module")
     *     .newLine() // end of line 1
     *     .addText("Text on the next line.")
     *     .newLine(); // end of line 2
     * </code></pre>
     */
    public static class LineBuilder extends TUIModule.Template<LineBuilder> {
        private TUITextModule.Builder current;
        protected int iterator;

        public LineBuilder(String name) {
            super(LineBuilder.class, name);
        }

        protected LineBuilder() {
            super(LineBuilder.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected LineBuilder createInstance() {
            return new LineBuilder();
        }


        @Override
        public void shallowCopy(LineBuilder original) {
            this.current = original.current.getCopy();
            this.iterator = original.iterator;
            super.shallowCopy(original);
        }

        public LineBuilder addText(TUITextModule.Builder text) {
            main.addChild(text);
            current = text;
            iterator ++;
            return self();
        }

        public LineBuilder addText(String text, Ansi ansi) {
            this.addText(new TUITextModule.Builder(main.getName() + "-" + iterator, text)
                    .setAnsi(ansi)
                    .printNewLine(false));
            return self();
        }

        public LineBuilder addText(String text) {
            return addText(text, ansi());
        }

        public LineBuilder addModuleOutput(String moduleName, Ansi ansi) {
            this.addText(new TUITextModule.Builder(main.getName() + "-" + iterator, moduleName)
                    .setOutputType(TUITextModule.OutputType.DISPLAY_MODULE_OUTPUT)
                    .printNewLine(false)
                    .setAnsi(ansi));
            return self();
        }

        public LineBuilder addModuleOutput(String moduleName) {
            return this.addModuleOutput(moduleName, ansi());
        }

        public LineBuilder newLine() {
            if(current != null) current.printNewLine(true);
            return self();
        }

        protected TUITextModule.Builder getCurrent() {
            return current;
        }

    }


}
