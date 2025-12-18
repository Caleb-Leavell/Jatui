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

package com.calebleavell.jatui.tui;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextInputModule extends TUIModule {
    private String input;
    private final TUIModule.Builder<?> displayText;

    public final static String INVALID = "Error: input was invalid";

    @Override
    public void shallowRun(RunFrame frame) {
        logger.info("Running TUITextInputModule {}", getName());
        displayText.build().run();
        logger.info("collecting input...");
        input = getScanner().nextLine();
        logger.info("input collected: \"{}\"", input);

        ApplicationModule app = getApplication();
        if(app != null) app.updateInput(this, input);

        super.shallowRun(frame);
    }

    public String getInput() {
        return input;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUITextInputModule, this includes: </p>
     * <ul>
     *     <li><strong>displayText</strong> (Note: this checks structural equality, not reference equality)</li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     * <p>Note: Runtime properties (e.g., input, inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param other The TUITextInputModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     *
     * @implNote
     * This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     * This method is merely for checking structural equality, which is generally only necessary for manual testing. Also, no overloaded equals methods
     * exist since {@code displayText} and {@code handlers} are children of the builder and thus checked automatically.
     */
    public boolean structuralEquals(TextInputModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return TUIModule.Builder.structuralEquals(displayText, other.displayText) && super.structuralEquals(other);
    }

    public TextInputModule(Builder builder) {
        super(builder);
        displayText = builder.displayText;
    }

    public static class Builder extends TUIModule.Builder<Builder> {

        protected InputHandlers handlers;
        protected TextModule.Builder displayText;

        public Builder(String name, String displayText) {
            super(Builder.class, name);

            this.displayText = new TextModule.Builder(name+"-display", displayText).printNewLine(false);
            this.children.add(this.displayText);

            handlers = new InputHandlers(this.name + "-handlers", this);
            this.children.add(handlers);
        }

        protected Builder() {
            super(Builder.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected Builder createInstance() {
            return new Builder();
        }

        @Override
        protected Builder deepCopy(Builder original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
            super.deepCopy(original, visited);
            this.displayText = original.displayText.getType().cast(visited.get(original.displayText));
            this.handlers = original.handlers.getType().cast(visited.get(original.handlers));
            this.handlers.inputModule = this;
            return this;
        }

        public TextModule.Builder getDisplayText() {
            return displayText;
        }

        /**
         * Adds a function module to execute after input is collected. <br>
         * <b>Note:</b> this module will not update application or other properties for {@code handler};
         * this must be done manually.
         *
         * @param handler The Function Module builder to execute after input is collected
         * @return self
         */
        public Builder addHandler(FunctionModule.Builder handler) {
            logger.trace("adding handler via TUIFunctionModule \"{}\"", handler.getName());
            handlers.addHandler(handler);
            return self();
        }

        public Builder addHandler(String name, Function<String, ?> logic) {
            logger.trace("adding handler \"{}\" via inputted logic", name);
            handlers.addHandler(name, logic);
            return self();
        }

        public <T> Builder addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            logger.trace("adding safe handler \"{}\" via inputted logic and exception handler", name);
            handlers.addSafeHandler(name, logic, exceptionHandler);
            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic, String exceptionMessage) {
            handlers.addSafeHandler(name, logic, o -> {
                logger.trace("adding safe handler \"{}\" via inputted logic and exception message", name);
                ApplicationModule app = this.getApplication();
                if(app == null) return;
                this.getPrintStream().println(exceptionMessage);
                app.terminateChild(this.name);
                app.runModuleAsChild(this);
            });

            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic) {
            logger.trace("adding safe handler \"{}\" via inputted logic", name);
            this.addSafeHandler(name, logic, "Error: Invalid Input");
            return self();
        }

        @Override
        public TextInputModule build() {
            logger.trace("Building TUITextInputModule {}", getName());

            // remove the display text from the children since we need it to run before the parent module
            // it's a child in the first place so that things like application() affect it as well
            this.children.remove(displayText);

            // re-add handlers to force them to the end
            // we add them before to allow for property propagation
            this.children.remove(handlers);
            this.children.add(handlers);
            this.setApplication(application);
            TextInputModule output = new TextInputModule(self());
            // re-add the child after constructing the module so that it can be edited if needed
            this.children.addFirst(displayText);
            return output;
        }
    }

    protected static class InputHandlers extends TUIModule.Template<InputHandlers> {

        protected Builder inputModule;

        int num = 1;

        public InputHandlers(String name, Builder inputModule) {
            super(InputHandlers.class, name);
            this.inputModule = inputModule;
        }

        protected InputHandlers() {
            super(InputHandlers.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected InputHandlers createInstance() {
            return new InputHandlers();
        }

        @Override
        protected void shallowCopy(InputHandlers other) {
            super.shallowCopy(other);
            this.inputModule = other.inputModule;
            this.num = other.num;
        }

        /**
         * Adds a function module to execute after input is collected. <br>
         * <b>Note:</b> this module will not update application or other properties for {@code handler};
         * this must be done manually.
         *
         * @param handler The Function Module builder to execute after input is collected
         * @return self
         */
        public InputHandlers addHandler(FunctionModule.Builder handler) {
            main.addChild(new InputHandler(this.name + "-" + num, inputModule).setHandler(handler));
            num ++;
            return self();
        }

        public <T> InputHandlers addHandler(String name, Function<String, T> logic) {
            main.addChild(new InputHandler(this.name + "-" + num, inputModule).setHandler(name, logic));
            num ++;
            return self();
        }

        public <T> InputHandlers addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            main.addChild(new InputHandler(this.name + "-" + num, inputModule).setHandler(name, logic, exceptionHandler));
            num ++;
            return self();
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For InputHandlers, this includes: </p>
         * <ul>
         *     <li><strong>inputModule</strong> <i>(Note: this checks shallow structural equality, and doesn't check children.)</i></li>
         *     <li><strong>num</strong></li>
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
         * @param first The first InputHandlers to compare
         * @param second The second InputHandlers to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         *
         * @implNote
         * This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
         */
        @Override
        public boolean shallowStructuralEquals(InputHandlers first, InputHandlers second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            if(first.inputModule != second.inputModule && !first.inputModule.shallowStructuralEquals(first.inputModule, second.inputModule)) return false;

            return  Objects.equals(first.num, second.num) &&
                    super.shallowStructuralEquals(first, second);
        }


    }

    protected static class InputHandler extends TUIModule.Template<InputHandler> {

        protected Builder inputModule;

        protected HandlerType handlerType;
        private FunctionModule.Builder module;
        private Function<String, ?> logic;
        private Consumer<String> exceptionHandler;
        private String moduleName;

        private static Set<String> handlerNames = new HashSet<>();

        protected enum HandlerType {
            MODULE,
            HANDLER,
            SAFE_HANDLER
        }

        public InputHandler(String name, Builder inputModule) {
            super(InputHandler.class, name);
            this.inputModule = inputModule;
        }

        protected InputHandler() {
            super(InputHandler.class);
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        @Override
        protected InputHandler createInstance() {
            return new InputHandler();
        }

        @Override
        protected void shallowCopy(InputHandler other) {
            super.shallowCopy(other);
            this.inputModule = other.inputModule;
            this.handlerType = other.handlerType;
            if(other.module != null) this.module = other.module.getCopy();
            this.logic = other.logic;
            this. exceptionHandler = other.exceptionHandler;
            this.moduleName = other.moduleName;
        }

        protected FunctionModule.Builder getModule() {
            return module;
        }

        protected Function<String, ?> getLogic() {
            return logic;
        }

        protected Consumer<String> getExceptionHandler() {
            return exceptionHandler;
        }

        protected String getModuleName() {
            return moduleName;
        }

        // note that handler does not get added as a child until this module is built
        // so property updates won't be propagated to it from this module
        public InputHandler setHandler(FunctionModule.Builder handler) {
            this.handlerType = HandlerType.MODULE;
            this.module = handler;
            return self();
        }

        public InputHandler setHandler(String name, Function<String, ?> logic) {
            this.handlerType = HandlerType.HANDLER;
            this.moduleName = name;
            this.logic = logic;
            return self();
        }

        public InputHandler setHandler(String name, Function<String, ?> logic, Consumer<String> exceptionHandler) {
            this.handlerType = HandlerType.SAFE_HANDLER;
            this.moduleName = name;
            this.logic = logic;
            this.exceptionHandler = exceptionHandler;
            return self();
        }

        /**
         * Adds a function module to execute after input is collected. <br>
         * <b>Note:</b> this module will not update application or other properties for {@code handler};
         * this must be done manually.
         *
         * @param handler The Function Module builder to execute after input is collected
         * @return self
         */
        private InputHandler addHandler(FunctionModule.Builder handler) {
            main.addChild(handler);
            checkForHandlerDuplicates(handler.getName(), handler);
            return self();
        }

        private <T> InputHandler addHandler(String name, Function<String, T> logic) {
            FunctionModule.Builder handler = new FunctionModule.Builder(name, () -> {
                ApplicationModule app = this.getApplication();
                if(app == null) {
                    logger.warn("tried to run logic for handler \"{}\" but app was null", name);
                    return null;
                }
                String input = app.getInput(inputModule.getName(), String.class);
                logger.info("running logic on handler \"{}\" with input \"{}\"", name, input);
                return logic.apply(input);
            }).setApplication(getApplication());
            main.addChild(handler);
            checkForHandlerDuplicates(name, handler);
            return self();
        }

        private <T> InputHandler addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            FunctionModule.Builder handler = new FunctionModule.Builder(name, () -> {
                ApplicationModule app = this.getApplication();
                if(app == null) {
                    logger.warn("tried to run logic for safe handler \"{}\" but app was null", name);
                    return null;
                }
                String input = app.getInput(inputModule.getName(), String.class);
                logger.info("running logic on safe handler \"{}\" with input \"{}\"", name, input);
                T converted;
                try {
                    converted = logic.apply(input);
                }
                catch(Exception e) {
                    logger.info("caught exception \"{}\" for safe handler \"{}\": \"{}\"", e.getClass().getSimpleName(), name, e.getMessage());
                    logger.info("running exception handler for safe handler \"{}\"", name);
                    exceptionHandler.accept(input);
                    return app.getInput(name);
                }
                return converted;
            }).setApplication(getApplication());
            main.addChild(handler);
            checkForHandlerDuplicates(name, handler);
            return self();
        }

        protected static void checkForHandlerDuplicates(String name, TUIModule.Builder<?> handler) {
            if(TUIModule.Builder.usedNames.get(name) >= 2)
                logger.error("Duplicate names detected: at least {} module builders have same name as built Input Handler \"{}\"",
                        TUIModule.Builder.usedNames.get(name) - 1, name);

            handlerNames.add(name);
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For InputHandlers, this includes: </p>
         * <ul>
         *     <li><strong>inputModule</strong> <i>(Note: this checks shallow structural equality, and doesn't check children.)</i></li>
         *     <li><strong>handlerType</strong>
         *     <li><strong>moduleName</strong>
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
         * @param first The first InputHandlers to compare
         * @param second The second InputHandlers to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         *
         * @implNote
         * This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
         */
        public boolean shallowStructuralEquals(InputHandler first, InputHandler second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            if(first.inputModule != second.inputModule && !first.inputModule.shallowStructuralEquals(first.inputModule, second.inputModule)) return false;

            return  Objects.equals(first.handlerType, second.handlerType) &&
                    Objects.equals(first.moduleName, second.moduleName) &&
                    super.shallowStructuralEquals(first, second);
        }

        public ContainerModule build() {
            if(handlerType == HandlerType.HANDLER || handlerType == HandlerType.SAFE_HANDLER) {
                for(TUIModule.Builder<?> child : main.children) {
                    child.setName(""); //prevent duplicate name warning
                }
            }

            main.clearChildren();

            switch(handlerType) {
                case MODULE -> {
                    addHandler(module);
                    break;
                }
                case HANDLER -> {
                    addHandler(moduleName, logic);
                    break;
                }
                case SAFE_HANDLER -> {
                    addSafeHandler(moduleName, logic, exceptionHandler);
                    break;
                }
            }

            return super.build();
        }
    }
}
