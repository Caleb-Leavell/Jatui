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

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.core.RunFrame;
import com.calebleavell.jatui.templates.InputHandler;

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

        protected ContainerModule.Builder handlers;
        protected TextModule.Builder displayText;

        private int handlerNum = 0;

        public Builder(String name, String displayText) {
            super(Builder.class, name);

            this.displayText = new TextModule.Builder(name+"-display", displayText).printNewLine(false);
            this.children.add(this.displayText);

            handlers = new ContainerModule.Builder(this.name + "-handlers");
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
        protected void shallowCopy(Builder copyFrom) {
            this.handlerNum = copyFrom.handlerNum;
            super.shallowCopy(copyFrom);
        }

        @Override
        protected Builder deepCopy(Builder original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
            super.deepCopy(original, visited);
            this.displayText = original.displayText.getType().cast(visited.get(original.displayText));
            this.handlers = original.handlers.getType().cast(visited.get(original.handlers));
            return this;
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For TextInputModule.Builder, this includes: </p>
         * <ul>
         *     <li><strong>handlerNum<strong></li>
         *     <li><strong>handlers</strong></li>
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
         * @param first The first Builder to compare
         * @param second The second Builder to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         *
         * @implNote
         * This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#structuralEquals(DirectedGraphNode)}
         * Handlers are checked via checking children.
         */
        @Override
        public boolean shallowStructuralEquals(Builder first, Builder second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return  Objects.equals(first.handlerNum, second.handlerNum) &&
                    super.shallowStructuralEquals(first, second);
        }

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
            handlers.addChild(new InputHandler(this.name + "-" + handlerNum, this.name).setHandler(handler));
            handlerNum ++;
            return self();
        }

        public Builder addHandler(String name, Function<String, ?> logic) {
            logger.trace("adding handler \"{}\" via inputted logic", name);
            handlers.addChild(new InputHandler(this.name + "-" + handlerNum, this.name).setHandler(name, logic));
            handlerNum ++;
            return self();
        }

        public <T> Builder addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            logger.trace("adding safe handler \"{}\" via inputted logic and exception handler", name);
            handlers.addChild(new InputHandler(this.name + "-" + handlerNum, this.name).setHandler(name, logic, exceptionHandler));
            handlerNum++;
            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic, String exceptionMessage) {
            logger.trace("adding safe handler \"{}\" via inputted logic and exception message", name);
            handlers.addChild(new InputHandler(this.name + "-" + handlerNum, this.name).setHandler(name, logic, o -> {
                ApplicationModule app = this.getApplication();
                if(app == null) return;
                this.getPrintStream().println(exceptionMessage);
                app.terminateChild(this.name);
                app.runModuleAsChild(this);
            }));
            handlerNum ++;
            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic) {
            logger.trace("adding safe handler \"{}\" via inputted logic", name);
            this.addSafeHandler(name, logic, "Error: Invalid Input");
            handlerNum++;
            return self();
        }

        @Override
        public TextInputModule build() {
            logger.trace("Building TUITextInputModule {}", getName());

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
