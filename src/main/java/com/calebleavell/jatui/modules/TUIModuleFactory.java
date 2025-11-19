package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.fusesource.jansi.Ansi.ansi;

public class TUIModuleFactory {

    protected static Logger logger = LoggerFactory.getLogger(TUIModuleFactory.class);

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

    public static TUIFunctionModule.Builder restart(String name, TUIModule moduleToRestart) {
        return new TUIFunctionModule.Builder(name, () -> {
            if(moduleToRestart != null) moduleToRestart.restart();
        });
    }

    public static TUIFunctionModule.Builder restart(String name, TUIModule parent, String moduleToRestart) {
        return new TUIFunctionModule.Builder(name, () -> {
            parent.restartChild(moduleToRestart);
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

        public NumberedList(String name, String... listText) {

            super(NumberedList.class, name);
            for(String text : listText) {
                this.addListText(text);
            }
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
            super.shallowCopy(original);
        }

        public NumberedList addListText(String listText) {
            logger.trace("adding list text \"{}\" to {}", listText, getName());
            int currentNum = (i * step) + start;
            main.addChild(
                    new LineBuilder(name + "-" + currentNum)
                            .addText("[" + currentNum + "] ", ansi().bold())
                            .addText(listText)
                            .newLine());
            i ++;
            return this;
        }

        public NumberedList setStart(int start) {
            logger.trace("adding start of {} to {}", getName(), start);
            this.start = start;
            return this;
        }

        public NumberedList setStep(int step) {
            logger.trace("adding step of {} to {}", getName(), step);
            this.step = step;
            return this;
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For InputHandlers, this includes: </p>
         * <ul>
         *     <li><strong>start</strong>/li>
         *     <li><strong>step</strong>
         *     <li><strong>i</strong>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        public boolean equalTo(NumberedList first, NumberedList second) {
            if(first == second) return true;
            if(first == null || second == null) return false;


            return  Objects.equals(first.start, second.start) &&
                    Objects.equals(first.step, second.step) &&
                    Objects.equals(first.i, second.i) &&
                    super.equalTo(first, second);
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
            for(TUIModule.NameOrModule m : original.modules) {
                this.modules.add(m.getCopy());
            }
            this.app = original.app;
            this.list = original.list.getCopy();
            super.shallowCopy(original);
        }

        private NumberedModuleSelector addModule(String displayText, TUIModule.NameOrModule module){
            logger.trace("adding module with displayText \"{}\" to NumberedModuleSelector \"{}\"", displayText, getName());
            this.modules.add(module);
            list.addListText(displayText);
            return self();
        }

        public NumberedModuleSelector addModule(String displayText, String moduleName) {
            return addModule(displayText, new TUIModule.NameOrModule(moduleName));
        }

        public NumberedModuleSelector addModule(String displayText, TUIModule.Builder<?> module) {
            return addModule(displayText, new TUIModule.NameOrModule(module));
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        @Override
        public boolean equalTo(NumberedModuleSelector first, NumberedModuleSelector second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            if(first.modules.size() != second.modules.size()) return false;

            for(int i = 0; i < first.modules.size(); i ++) {
                TUIModule.NameOrModule firstNameOrModule = first.modules.get(i);
                TUIModule.NameOrModule secondNameOrModule = second.modules.get(i);

                if(firstNameOrModule == secondNameOrModule) continue;
                else if(firstNameOrModule == null || secondNameOrModule == null) return false;

                TUIModule.Builder<?> firstModule = firstNameOrModule.getModule(this.app);
                TUIModule.Builder<?> secondModule = secondNameOrModule.getModule(this.app);

                if(!TUIModule.Builder.equals(firstModule, secondModule)) return false;
            }

            return Objects.equals(first.app, second.app) &&
                    super.equalTo(first, second);
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
            logger.trace("adding text to LineBuilder \"{}\" that displays \"{}\" (output type is \"{}\")", getName(), text.getText(), text.getOutputType());
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
            logger.trace("adding newline to LineBuilder \"{}\"", getName());
            if(current != null) current.printNewLine(true);
            return self();
        }

        protected TUITextModule.Builder getCurrent() {
            return current;
        }


        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For LineBuilder, this includes: </p>
         * <ul>
         *     <li><strong>current</strong> (the most recent text module added) </li>
         *     <li><strong>iterator</strong> (the number of text modules added so far) </li>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        @Override
        public boolean equalTo(LineBuilder first, LineBuilder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            if(!TUIModule.Builder.equals(first.current, second.current)) return false;

            return Objects.equals(first.iterator, second.iterator) &&
                    super.equalTo(first, second);
        }
    }

    public static class ConfirmationPrompt extends TUIModule.Template<ConfirmationPrompt> {

        private final Set<String> confirm = new HashSet<>(List.of("yes", "y"));
        private final Set<String> deny = new HashSet<>(List.of("no", "n"));
        private int confirmIter = 1;
        private int denyIter = 2;


        public ConfirmationPrompt(String name, String displayText) {
            super(ConfirmationPrompt.class, name);
            main.addChild(
                    new TUITextInputModule.Builder(name + "-input", displayText)
            );
        }

        protected ConfirmationPrompt() {
            super(ConfirmationPrompt.class);
        }

        @Override
        protected ConfirmationPrompt createInstance() {
            return new ConfirmationPrompt();
        }

        /**
         * Sets the strings that will count as a confirmation. Note, this clears the current default confirmation strings <br>
         * Note: spaces are stripped from both these strings and the input,
         * and both these strings and teh input are converted to lowercase.
         * @param confirmStrings The strings that count as confirmation
         * @return self
         */
        public ConfirmationPrompt setValidConfirm(String... confirmStrings) {
            confirm.clear();
            if(confirmStrings.length == 0) return self();
            confirm.addAll(Arrays.asList(confirmStrings));

            return self();
        }

        public Set<String> getValidConfirm() {
            return confirm;
        }

        /**
         * Sets the strings that will count as a denial. Note, this clears the current default denial strings <br>
         * Note: spaces are stripped from both these strings and the input,
         * and both these strings and teh input are converted to lowercase.
         * @param denyStrings The strings that count as denial
         * @return self
         */
        public ConfirmationPrompt setValidDeny(String... denyStrings) {
            deny.clear();
            if(denyStrings.length == 0) return self();
            deny.addAll(Arrays.asList(denyStrings));

            return self();
        }

        public Set<String> getValidDeny() {
            return deny;
        }

        public ConfirmationPrompt addOnConfirm(Runnable logic) {
            return this.addOnConfirm(this.name + "-onConfirm-" + confirmIter++, () -> {
                logic.run();
                return null;
            });
        }

        public ConfirmationPrompt addOnConfirm(String name, Supplier<?> logic) {
            TUITextInputModule.Builder input = this.getChild(this.name+"-input",
                    TUITextInputModule.Builder.class);

            input.addSafeHandler(name, s -> {
                String in = s.toLowerCase().replace("\n", "").replace(String.format("%n"), "");
                if(confirm.contains(in)) {
                    return logic.get();
                } else if(deny.contains(in))
                    return null;
                else throw new RuntimeException();
            });

            return self();
        };

        public ConfirmationPrompt addOnDeny(Runnable logic) {
            return this.addOnDeny(this.name + "-onDeny-" + denyIter++,() -> {
                logic.run();
                return null;
            });
        }


        public ConfirmationPrompt addOnDeny(String name, Supplier<?> logic) {
            TUITextInputModule.Builder input = this.getChild(this.name+"-input",
                    TUITextInputModule.Builder.class);

            input.addSafeHandler(name, s -> {
                String in = s.toLowerCase().strip().replace("\n", "").replace(String.format("%n"), "");
                if(confirm.contains(in))
                    return null;
                else if(deny.contains(in)) {
                    return logic.get();
                } else throw new RuntimeException();
            });

            return self();
        };

        @Override
        public ConfirmationPrompt setName(String name) {
            if(this.name != null) this.getChild(this.name + "-input").setName(name + "-input");
            super.setName(name);
            return self();
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For ConfirmationPrompt, this includes: </p>
         * <ul>
         *     <li><strong>confirm (set)</strong></li>
         *     <li><strong>deny (set)</strong></li>
         *     <li><strong>confirmIter</strong></li>
         *     <li><strong>denyIter</strong></li>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        @Override
        public boolean equalTo(ConfirmationPrompt first, ConfirmationPrompt second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return  Objects.equals(first.confirm, second.confirm) &&
                    Objects.equals(first.deny, second.deny) &&
                    Objects.equals(first.confirmIter, second.confirmIter) &&
                    Objects.equals(first.denyIter, second.denyIter) &&
                    super.equalTo(first, second);
        }

        @Override
        public void shallowCopy(ConfirmationPrompt original) {
            this.confirm.clear();
            this.confirm.addAll(original.confirm);
            this.deny.clear();
            this.deny.addAll(original.deny);
            this.confirmIter = original.confirmIter;
            this.denyIter = original.denyIter;
            super.shallowCopy(original);
        }
    }

    public static class PasswordInput extends TUIModule.Template<PasswordInput> {
        String displayText;
        private Supplier<char[]> passwordSupplier;

        final List<TUIFunctionModule.Builder> onValidPassword = new ArrayList<>();
        final List<TUIFunctionModule.Builder> onInvalidPassword = new ArrayList<>();
        private boolean storeInput = false;
        private boolean storeMatch = false;

        public PasswordInput(String name, String displayText, Supplier<char[]> passwordSupplier) {
            super(PasswordInput.class, name);
            this.displayText = displayText;
            this.passwordSupplier = passwordSupplier;
            main.addChild(new TUIFunctionModule.Builder(name+"-input", createPasswordInput()));
        }

        protected PasswordInput() {
            super(PasswordInput.class);
        }

        @Override
        protected PasswordInput createInstance() {return new PasswordInput();}

        /**
         * Cleans any memory that may have been stored from this password input
         * (including the input and whether the password was a match). <br>
         * Note: Memory is automatically cleaned unless you explicitly set this
         * module to store it via {@link PasswordInput#storeInput()}, {@link PasswordInput#storeIfMatched()},
         * or {@link PasswordInput#storeInputAndMatch()}.
         */
        public void cleanMemory() {
            char[] password = application.getInput(this.name+"-input", char[].class);
            if(password != null) Arrays.fill(password, ' ');
            password = null;

            application.forceUpdateInput(this.name+"-is-matched", null);
        }

        /**
         * Validates a password that's been stored in memory. <br>
         * Note: this will always return false if the behavior hasn't been set to store the password.
         * @param correct
         * @return
         */
        public boolean validatePassword(char[] correct) {
            char[] inputtedPassword = application.getInput(this.name+"-input", char[].class);
            if (inputtedPassword == null || correct == null || correct.length != inputtedPassword.length) return false;

            for(int i = 0; i < inputtedPassword.length; i ++) {
                if(inputtedPassword[i] != correct[i]) return false;
            }

            return true;
        }

        public PasswordInput addOnValidPassword(Runnable onValidPassword) {
            this.onValidPassword.add(new TUIFunctionModule.Builder("", () -> {
                onValidPassword.run();
                return null;
            }));
            return self();
        }

        public PasswordInput addOnValidPassword(String name, Supplier<?> onValidPassword) {
            this.onValidPassword.add(new TUIFunctionModule.Builder(name, onValidPassword));
            return self();
        }

        public PasswordInput addOnInvalidPassword(Runnable onInvalidPassword) {
            this.onInvalidPassword.add(new TUIFunctionModule.Builder("", () -> {
                onInvalidPassword.run();
                return null;
            }));
            return self();
        }

        public PasswordInput addOnInvalidPassword(String name, Supplier<?> onInvalidPassword) {
            this.onInvalidPassword.add(new TUIFunctionModule.Builder(name, onInvalidPassword));
            return self();
        }

        public PasswordInput cleanImmediately() {
            this.storeInput = false;
            this.storeMatch = false;
            return self();
        }

        public PasswordInput storeIfMatched() {
            this.storeInput = false;
            this.storeMatch = true;
            return self();
        }

        public PasswordInput storeInput() {
            this.storeInput = true;
            this.storeMatch = false;
            return self();
        }

        public PasswordInput storeInputAndMatch() {
            this.storeInput = true;
            this.storeMatch = true;
            return self();
        }

        @Override
        public PasswordInput setName(String name) {
            if(this.name == null) return super.setName(name);
            TUIFunctionModule.Builder input = main.getChild(this.name+"-input", TUIFunctionModule.Builder.class);
            input.setName(name + "-input");
            return super.setName(name);
        }

        public PasswordInput setDisplayText(String displayText) {
            this.displayText = displayText;
            return self();
        }

        public String getDisplayText() {return displayText;};

        @Override
        public TUIContainerModule build() {
            // update the input function to reflect the most recent name, input, and application
            TUIFunctionModule.Builder input = main.getChild(this.name+"-input", TUIFunctionModule.Builder.class);
            input.setFunction(createPasswordInput());
            return super.build();
        }

        private Supplier<char[]> createPasswordInput() {
            return () -> {
                Console console = System.console();

                this.getPrintStream().print(this.displayText);

                boolean match = true;
                char[] input = new char[0];
                char[] correct = new char[0];
                try {
                    // just reads the input normally if the Scanner was set to something custom or if Console is null
                    // otherwise, read as a password from console
                    if(this.getScanner() != TUIModule.DEFAULT_SCANNER || console == null) {
                        input = this.getScanner().nextLine().toCharArray();
                    }
                    else {
                        input = console.readPassword();
                    }

                    correct = this.passwordSupplier.get();

                    if (input == null || correct == null || correct.length != input.length) match = false;
                    else {
                        for(int i = 0; i < input.length; i ++) {
                            if (input[i] != correct[i]) {
                                match = false;
                                break;
                            }
                        }
                    }
                }
                finally {
                    if(input != null && !this.storeInput) Arrays.fill(input, ' ');
                    if(correct != null) Arrays.fill(correct, ' ');
                }

                List<TUIFunctionModule.Builder> functions = match ? onValidPassword : onInvalidPassword;
                for(TUIFunctionModule.Builder func : functions) {
                    func.setApplication(this.application); // application could be null but that's ok
                    func.build().run();
                }

                if(this.application == null) {
                    match = false; // helps avoid brute-forcing by removing match from memory (low severity threat)
                    return null;
                }
                else {
                    if(this.storeMatch) this.application.forceUpdateInput(this.name+"-is-matched", match);
                    match = false;
                    if(this.storeInput) return input != null ? input : new char[0];
                    else return null;
                }
            };
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For ConfirmationPrompt, this includes: </p>
         * <ul>
         *     <li><strong>displayText</strong></li>
         *     <li><strong>onValidPassword</strong></li>
         *     <li><strong>onInvalidPassword</strong></li>
         *     <li><strong>storeInput</strong></li>
         *     <li><strong>storeMatch</strong></li>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        @Override
        public boolean equalTo(PasswordInput first, PasswordInput second) {
            if(first == second) return true;
            if(first.getClass() != second.getClass()) return false;

            // note: these fields are guaranteed to be non-null
            if(first.onValidPassword.size() != second.onValidPassword.size()) return false;
            if(first.onInvalidPassword.size() != second.onInvalidPassword.size()) return false;
            for(int i = 0; i < first.onInvalidPassword.size(); i ++) {
                if(!TUIModule.Builder.equals(first.onInvalidPassword.get(i), second.onInvalidPassword.get(i))) {
                    return false;
                }
            }
            for(int i = 0; i < first.onInvalidPassword.size(); i ++) {
                if(!TUIModule.Builder.equals(first.onInvalidPassword.get(i), second.onInvalidPassword.get(i))) {
                    return false;
                }
            }

            return  Objects.equals(first.displayText, second.displayText)
                    && Objects.equals(first.storeInput, second.storeInput)
                    && Objects.equals(first.storeMatch, second.storeMatch)
                    && super.equalTo(first, second);
        }

        @Override
        public void shallowCopy(PasswordInput original) {
            this.displayText = original.displayText;
            this.onValidPassword.clear();
            this.onInvalidPassword.clear();
            for(TUIFunctionModule.Builder module: original.onValidPassword) {
                this.onValidPassword.add(module.getCopy());
            }
            for(TUIFunctionModule.Builder module: original.onInvalidPassword) {
                this.onInvalidPassword.add(module.getCopy());
            }
            this.storeInput = original.storeInput;
            this.storeMatch = original.storeMatch;
            super.shallowCopy(original);
        }
    }

}
