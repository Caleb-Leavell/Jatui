package com.calebleavell.jatui.modules;

import jdk.jshell.spi.ExecutionControl;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class TUITextInputModule extends TUIModule {
    private String input;
    private final TUIModule.Builder<?> displayText;

    public final static String INVALID = "Error: input was invalid";

    @Override
    public void run() {
        this.terminated = false;
        displayText.build().run();
        input = getScanner().nextLine();

        TUIApplicationModule app = getApplication();
        if(app != null) app.updateInput(this, input);

        super.run();
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
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing. Also, no overloaded equals methods
     *  exist since {@code displayText} and {@code handlers} are children of the builder and thus checked automatically.
     */
    public boolean equals(TUITextInputModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return TUIModule.Builder.equals(displayText, other.displayText) && super.equals(other);
    }

    public TUITextInputModule(Builder builder) {
        super(builder);
        displayText = builder.displayText;
    }

    public static class Builder extends TUIModule.Builder<Builder> {

        protected InputHandlers handlers;
        protected TUITextModule.Builder displayText;

        public Builder(String name, String displayText) {
            super(Builder.class, name);

            this.displayText = new TUITextModule.Builder(name+"display", displayText).printNewLine(false);
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
        public Builder createInstance() {
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

        public Builder addHandler(TUIFunctionModule.Builder handler) {
            handlers.addHandler(handler);
            return self();
        }

        public Builder addHandler(String name, Function<String, ?> logic) {
            handlers.addHandler(name, logic);
            return self();
        }

        public <T> Builder addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            handlers.addSafeHandler(name, logic, exceptionHandler);
            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic, String exceptionMessage) {
            handlers.addSafeHandler(name, logic, o -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return;
                System.out.println(exceptionMessage);
                app.terminateChild(this.name);
                app.runModuleAsChild(this);
            });

            return self();
        }

        public Builder addSafeHandler(String name, Function<String, ?> logic) {
            this.addSafeHandler(name, logic, "Error: Invalid Input");
            return self();
        }

        @Override
        public TUITextInputModule build() {
            // remove the display text from the children since we need it to run before the parent module
            // it's a child in the first place so that things like application() affect it as well
            this.children.remove(displayText);

            // re-add handlers to force them to the end
            // we add them before to allow for property propagation
            this.children.remove(handlers);
            this.children.add(handlers);
            this.setApplication(application);
            TUITextInputModule output = new TUITextInputModule(self());
            // re-add the child after constructing the module so that it can be edited if needed
            this.children.addFirst(displayText);
            return output;
        }
    }

    private static class InputHandlers extends TUIModule.Template<InputHandlers> {

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
        public InputHandlers createInstance() {
            return new InputHandlers();
        }

        @Override
        protected void shallowCopy(InputHandlers other) {
            super.shallowCopy(other);
            this.inputModule = other.inputModule;
            this.num = other.num;
        }


        public InputHandlers addHandler(TUIFunctionModule.Builder handler) {
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
         *     <li><strong>inputModule</strong> <i>(Note: this checks reference equality, not structural equality.)</i></li>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        public boolean equalTo(InputHandlers first, InputHandlers second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return Objects.equals(first.inputModule, second.inputModule) && super.equalTo(first, second);
        }


    }

    private static class InputHandler extends TUIModule.Template<InputHandler> {

        protected Builder inputModule;

        protected HandlerType handlerType;
        private TUIFunctionModule.Builder module;
        private Function<String, ?> logic;
        private Consumer<String> exceptionHandler;
        private String moduleName;

        enum HandlerType {
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
        public InputHandler createInstance() {
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

        public InputHandler setHandler(TUIFunctionModule.Builder handler) {
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


        private InputHandler addHandler(TUIFunctionModule.Builder handler) {
            main.addChild(handler);
            return self();
        }

        private <T> InputHandler addHandler(String name, Function<String, T> logic) {
            main.addChild(new TUIFunctionModule.Builder(name, () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return null;
                String input = app.getInput(inputModule.getName(), String.class);
                T converted = logic.apply(input);
                app.updateInput(name, converted);
                return converted;
            }));
            return self();
        }

        private <T> InputHandler addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
            main.addChild(new TUIFunctionModule.Builder(name, () -> {
                TUIApplicationModule app = this.getApplication();
                if(app == null) return null;
                String input = app.getInput(inputModule.getName(), String.class);
                T converted;
                try {
                    converted = logic.apply(input);
                }
                catch(Exception e) {
                    exceptionHandler.accept(input);
                    return app.getInput(name);
                }
                app.updateInput(name, converted);
                return converted;
            }));
            return self();
        }

        public TUIContainerModule build() {
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

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For InputHandlers, this includes: </p>
         * <ul>
         *     <li><strong>inputModule</strong> <i>(Note: this checks reference equality, not structural equality.)</i></li>
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
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        public boolean equalTo(InputHandler first, InputHandler second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return  Objects.equals(first.inputModule, second.inputModule) &&
                    Objects.equals(first.handlerType, second.handlerType) &&
                    Objects.equals(first.moduleName, second.moduleName) &&
                    super.equalTo(first, second);
        }
    }
}
