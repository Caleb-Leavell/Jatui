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

package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.templates.InputHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles collection of text for the application. Generally this means collecting user input from the console,
 * but {@link TUIModule.Builder#setScanner(Scanner)} can be used to collect input from other places as well.
 * <br>
 * A TextInputModule can't do much unless it belongs to an {@link ApplicationModule}.
 * If it is tied to one, whatever input is collected is updated in the app's inputMap and can
 * be accessed via {@link ApplicationModule#getInput(String)}.
 */
public class TextInputModule extends TUIModule {

    /** The most recently collected input **/
    private String input;

    /** The {@link TextModule} that displays text for getting input (e.g., "Your Input: "). **/
    private final TextModule.Builder displayText;

    /** A basic default error message if input is determined to be invalid via an InputHandler or something else. **/
    public final static String INVALID = "Error: Invalid Input";

    /**
     * Displays the displayText given in the constructor for {@link TextInputModule.Builder}, collects input
     * from the scanner given in {@link TUIModule.Builder#setScanner(Scanner)} then updates the application
     * it's tied to store the input (This can be accessed via {@link ApplicationModule#getInput(String)}).
     * <br>
     * If InputHandlers are provided via {@link TextInputModule.Builder#addHandler(FunctionModule.Builder)} or a corresponding method,
     * those are run immediately after this.
     */
    @Override
    public void shallowRun() {
        logger.info("Running TextInputModule {}", getName());
        displayText.build().run();
        logger.info("collecting input...");
        input = getScanner().nextLine();
        logger.info("input collected: \"{}\"", input);

        ApplicationModule app = getApplication();
        if(app != null) app.updateInput(this, input);
    }

    /**
     * Retrieve the input collected on {@link TextInputModule#shallowRun()}.
     * Alternatively, this can be collected via {@link ApplicationModule#getInput(String)},
     * or {@link ApplicationModule#getInput(String, Class)} with the class set to {@code String.class}.
     *
     * @return The string collected when running. Null if this module hasn't run yet.
     */
    public String getInput() {
        return input;
    }

    /**
     * Checks equality for properties given by the builder. For {@link TextInputModule}, this includes
     * {@code displayText}, as well as other requirements provided by {@link TUIModule#structuralEquals(TUIModule)}.
     */
    public boolean structuralEquals(TextInputModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return TUIModule.Builder.structuralEquals(displayText, other.displayText) && super.structuralEquals(other);
    }

    /**
     * Constructs a new {@link TextInputModule} given a builder. Copies {@code displayText} from the builder.
     * @param builder The builder to construct the new module from.
     */
    public TextInputModule(Builder builder) {
        super(builder);
        displayText = builder.displayText;
    }

    /**
     * Constructs a new {@link TextInputModule} builder.
     *
     * @param name The name of the builder.
     * @param displayText The text that displays before getting input (e.g., "Your Input: ").
     * @return The new builder.
     */
    public static Builder builder(String name, String displayText) {
        return new Builder(name, displayText);
    }

    /**
     * Builder for {@link TextInputModule}.
     * <br><br>
     * Required fields: {@code name}, {@code displayText} <br>
     * Optional fields: {@code handlers}
     *
     * @implNote The names for the {@link InputHandler} objects monotonically increase based on an iterator {@code handlerNum},
     * so the name format follows {@code "<this.name>-<this.handlerNum>"}.
     */
    public static class Builder extends TUIModule.Builder<Builder> {

        /** The container for the {@link InputHandler} objects that will operate on the input collected by this module.**/
        protected ContainerModule.Builder handlers;

        /** The {@link TextModule} that displays text for getting input (e.g., "Your Input: "). **/
        protected TextModule.Builder displayText;

        /** The iterator that ensures every {@link InputHandler} has a unique name **/
        private int handlerNum = 0;

        protected Builder(String name, String displayText) {
            super(Builder.class, name);

            this.displayText = TextModule.builder(name+"-display", displayText).printNewLine(false);
            this.children.add(this.displayText);

            handlers = ContainerModule.builder(this.name + "-handlers");
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

        /**
         * Copies {@code handlerNum} and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
         * @param original The builder to copy from
         */
        @Override
        protected void shallowCopy(Builder original) {
            this.handlerNum = original.handlerNum;
            super.shallowCopy(original);
        }

        /**
         * In order to maintain the reference to the new children, access to the {@code visited} map is used,
         * and thus {@link TUIModule.Builder#deepCopy(TUIModule.Builder, Map)} needs to be overridden.
         *
         * @param original The module to copy from.
         * @param visited All children that have already been deep-copied.
         * @return The instance that was copied into (self if {@code original} hasn't been visited yet).
         */
        @Override
        protected Builder deepCopy(Builder original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
            Builder result = super.deepCopy(original, visited);

            // Only initialize if this is the canonical instance
            if (result == this) {
                this.displayText = original.displayText.getType().cast(visited.get(original.displayText));
                this.handlers = original.handlers.getType().cast(visited.get(original.handlers));
            }

            return result;
        }

        /**
         * Checks equality for properties given by the builder. For {@link TextInputModule}, this includes
         * {@code displayText}, as well as other requirements provided by {@link TUIModule.Builder#shallowStructuralEquals(TUIModule.Builder, TUIModule.Builder)}.
         */
        public boolean shallowStructuralEquals(Builder first, Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return  first.handlerNum == second.handlerNum &&
                    super.shallowStructuralEquals(first, second);
        }

        /**
         * Sets the name of the module. Also updates the input name to check for any declared
         * {@link InputHandler} objects attached via {@link TextInputModule.Builder#addHandler(FunctionModule.Builder)} or
         * similar.
         * @param name The unique name of this module.
         * @return self
         */
        @Override
        public Builder setName(String name) {
            super.setName(name);
            if(this.handlers == null) return self();
            this.handlers.getChildren().forEach(child -> {
                if (child instanceof InputHandler handler) {
                    handler.setInputName(name);
                }
            });
            return self();
        }

        /**
         * The {@link TextModule} that displays text for getting input (e.g., "Your Input: ").
         *
         * @return The display text for this module.
         **/

        public TextModule.Builder getDisplayText() {
            return displayText;
        }

        /**
         * Adds a function module to execute after input is collected. <br>
         * <b>Note:</b> this module will not update application or other properties for {@code handler};
         * this must be done manually. This is because {@code handler} gets wrapped in an {@link InputHandler}.
         *
         * @param handler The Function Module builder to execute after input is collected
         * @return self
         */
        public Builder addHandler(FunctionModule.Builder handler) {
            logger.trace("adding handler via FunctionModule \"{}\"", handler.getName());
            handlers.addChild(InputHandler.builder(this.name + "-" + handlerNum, this.name).setHandler(handler));
            handlerNum ++;
            return self();
        }

        /**
         * Adds an {@link InputHandler} based on the provided logic.
         *
         * @param name The name of the function module that the {@link InputHandler} will wrap. This means whatever
         *             {@code logic} returns can be accessed via {@code app.getInput(<name>)} or equivalent.
         * @param logic The {@link Function} to execute, that receives the input the built {@link TextInputModule} collects
         *              and outputs some object that is saved to the application with {@code name} as the identifier.
         * @return self
         */
        public Builder addHandler(String name, Function<String, ?> logic) {
            logger.trace("adding handler \"{}\" via inputted logic", name);
            handlers.addChild(InputHandler.builder(this.name + "-" + handlerNum, this.name).setHandler(name, logic));
            handlerNum ++;
            return self();
        }

        /**
         * Adds an {@link InputHandler} based on the provided logic. If a {@link RuntimeException} is thrown
         * during the execution of {@code logic}, it is caught and {@code exceptionHandler} can perform some
         * recovery action. If you just want to print some error message and rerun the input, use
         * {@link TextInputModule.Builder#addSafeHandler(String, Function, String)} or
         * {@link TextInputModule.Builder#addSafeHandler(String, Function)} for a default message ({@link TextInputModule#INVALID}).
         *
         * @param name The name of the function module that the {@link InputHandler} will wrap. This means whatever
         *             this {@code logic} returns can be accessed via {@code app.getInput(<name>)} or equivalent.
         * @param logic The {@link Function} to execute, that receives the input the built {@link TextInputModule} collects
         *              and outputs some object that is saved to the application with {@code name} as the identifier.
         * @param exceptionHandler The recovery mechanism if {@code logic} throws a {@link RuntimeException}.
         * @return self
         */
        public <T> Builder addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            logger.trace("adding safe handler \"{}\" via inputted logic and exception handler", name);
            handlers.addChild(InputHandler.builder(this.name + "-" + handlerNum, this.name).setHandler(name, logic, exceptionHandler));
            handlerNum++;
            return self();
        }

        /**
         * Adds an {@link InputHandler} based on the provided logic. If a {@link RuntimeException} is thrown
         * during the execution of {@code logic}, it is caught and the input is recollected.
         * Use {@link TextInputModule.Builder#addSafeHandler(String, Function)} for a default message ({@link TextInputModule#INVALID}).
         *
         * @param name The name of the function module that the {@link InputHandler} will wrap. This means whatever
         *             this {@code logic} returns can be accessed via {@code app.getInput(<name>)} or equivalent.
         * @param logic The {@link Function} to execute, that receives the input the built {@link TextInputModule} collects
         *              and outputs some object that is saved to the application with {@code name} as the identifier.
         * @param exceptionMessage The message to display if {@code logic} throws a {@link RuntimeException}, before the input gets
         *                         recollected.
         * @return self
         */
        public Builder addSafeHandler(String name, Function<String, ?> logic, String exceptionMessage) {
            logger.trace("adding safe handler \"{}\" via inputted logic and exception message", name);
            handlers.addChild(InputHandler.builder(this.name + "-" + handlerNum, this.name).setHandler(name, logic, _ -> {
                ApplicationModule app = this.getApplication();
                if(app == null) return;
                this.getPrintStream().println(exceptionMessage);
                app.terminateChild(this.name);
                app.navigateTo(this);
            }));
            handlerNum ++;
            return self();
        }

        /**
         * Adds an {@link InputHandler} based on the provided logic. If a {@link RuntimeException} is thrown
         * during the execution of {@code logic}, it is caught and the input is recollected. The message
         * provided by {@link TextInputModule#INVALID} is shown before rerunning. For a custom invalid message,
         * use {@link TextInputModule.Builder#addSafeHandler(String, Function, String)}.
         *
         * @param name The name of the function module that the {@link InputHandler} will wrap. This means whatever
         *             this {@code logic} returns can be accessed via {@code app.getInput(<name>)} or equivalent.
         * @param logic The {@link Function} to execute, that receives the input the built {@link TextInputModule} collects
         *              and outputs some object that is saved to the application with {@code name} as the identifier.

         * @return self
         */
        public Builder addSafeHandler(String name, Function<String, ?> logic) {
            logger.trace("adding safe handler \"{}\" via inputted logic", name);
            this.addSafeHandler(name, logic, INVALID);
            return self();
        }

        /**
         * Builds a new {@link TextInputModule} based on the configuration of this builder.
         * @return The new {@link TextInputModule}.
         * @implNote Removes and re-adds {@code displayText} and {@code handlers} to the
         * children in order to allow {@code displayText}
         * to a be a field of the built module, and for {@code handlers}
         * to be at the end.
         */
        @Override
        public TextInputModule build() {
            logger.trace("Building TextInputModule {}", getName());

            // remove the display text from the children since we need it to run before the parent module
            // it's a child in the first place so that things like setApplication() affect it as well
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

}
