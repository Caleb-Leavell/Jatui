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

import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Handles management of the application state by running logic
 * on saved inputs from the application.
 * <br><br>
 * Example Usage:
 * <pre><code>
 *       InputHandler handler = InputHandler.builder("handler-name", "input_identifier")
 *                     .setHandler("return_value_identifier",
 *                             // run main logic on input
 *                             s -> {
 *                         if(!s.equals("valid_input")) throw new RuntimeException("invalid input!!");
 *                         else return 10;
 *                     },
 *                             // handle the RuntimeException thrown above (optional)
 *                             s -> {
 *                         app.restart();
 *                     })
 * </code></pre>
 *
 * <strong>Note: </strong> You will likely want to use the wrapper methods provided by
 * {@link TextInputModule.Builder} (e.g, {@link TextInputModule.Builder#addSafeHandler(String, Function, String)})
 * if interfacing with user input. This class should be
 * used if more fine-grained control is required, or if implementing a custom {@link ModuleTemplate}
 * that works closely with application state.
 */
public class InputHandler extends ModuleTemplate<InputHandler> {

    /** The name of the app state to read **/
    protected String inputName;

    /** The type of the handler (as provided by {@link HandlerType}) **/
    protected InputHandler.HandlerType handlerType;

    /**
     * The module that provides logic if {@link InputHandler#handlerType} is
     * {@link HandlerType#MODULE}.
     * **/
    private FunctionModule.Builder module;

    /**
     * The logic that runs on input if {@link InputHandler#handlerType}
     * is {@link HandlerType#HANDLER} or {@link HandlerType#SAFE_HANDLER}.
     **/
    private Function<String, ?> logic;

    /**
     * Handles instances of {@link RuntimeException} thrown by {@link InputHandler#logic}
     * if {@link InputHandler#handlerType} is {@link HandlerType#SAFE_HANDLER}.
     **/
    private Consumer<String> exceptionHandler;

    /**
     * The name of the {@link FunctionModule} to create and thus the identifier
     * of the returned value for {@link InputHandler#logic} if {@link InputHandler#handlerType}
     * is {@link HandlerType#HANDLER} or {@link HandlerType#SAFE_HANDLER}.
     **/
    private String moduleName;

    /**
     * Specifies how to build the handler.
     */
    protected enum HandlerType {
        /**
         * Builds the module with the provided {@link FunctionModule} as the logic runner.
         * Set via {@link InputHandler#setHandler(FunctionModule.Builder)}.
         **/
        MODULE,

        /** Builds the module with logic provided by {@link InputHandler#setHandler(String, Function)}. **/
        HANDLER,

        /** Builds the module with logic provided by {@link InputHandler#setHandler(String, Function, Consumer)}. **/
        SAFE_HANDLER
    }

    protected InputHandler(String name, String inputName) {
        super(InputHandler.class, name);
        this.inputName = inputName;
    }

    /**
     * Constructs a new {@link InputHandler} builder.
     *
     * @param name The name of the builder.
     * @param inputName The name of the app state to read.
     * @return The new builder.
     */
    public static InputHandler builder(String name, String inputName) {
        return new InputHandler(name, inputName);
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
    /**
     * Copies {@code inputName}, {@code handlerType},  {@code module},
     * {@code logic}, {@code exceptionHandler}, and {@code moduleName},
     * and delegates to {@link TUIModule.Builder#shallowCopy(TUIModule.Builder)}.
     * @param original The builder to copy from.
     */
    @Override
    protected void shallowCopy(InputHandler original) {
        super.shallowCopy(original);
        this.inputName = original.inputName;
        this.handlerType = original.handlerType;
        if(original.module != null) this.module = original.module.getCopy();
        this.logic = original.logic;
        this.exceptionHandler = original.exceptionHandler;
        this.moduleName = original.moduleName;
    }

    /**
     * {@code inputName} is the name of the app state to read.
     * @return {@code inputName}.
     **/
    public String getInputName() {
        return this.inputName;
    }

    /**
     * {@code inputName} is the name of the app state to read.
     * @return self.
     **/
    public InputHandler setInputName(String inputName) {
        this.inputName = inputName;
        return self();
    }

    /**
     * {@code module} is the name of the {@link FunctionModule} to create and thus the identifier
     * of the returned value for {@link InputHandler#logic} if {@link InputHandler#handlerType}
     * is {@link HandlerType#HANDLER} or {@link HandlerType#SAFE_HANDLER}.
     *
     * @return {@code module}.
     **/
    protected FunctionModule.Builder getModule() {
        return module;
    }

    /**
     * {@code logic} is the logic that runs on input if {@link InputHandler#handlerType}
     * is {@link HandlerType#HANDLER} or {@link HandlerType#SAFE_HANDLER}.
     *
     * @return {@code logic}.
     **/
    protected Function<String, ?> getLogic() {
        return logic;
    }

    /**
     * {@code exceptionHandler} handles instances of {@link RuntimeException} thrown by {@link InputHandler#logic}
     * if {@link InputHandler#handlerType} is {@link HandlerType#SAFE_HANDLER}.
     *
     * @return {@code exceptionHandler}.
     **/
    protected Consumer<String> getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * {@code moduleName} is the name of the {@link FunctionModule} to create and thus the identifier
     * of the returned value for {@link InputHandler#logic} if {@link InputHandler#handlerType}
     * is {@link HandlerType#HANDLER} or {@link HandlerType#SAFE_HANDLER}.
     *
     * @return {@code moduleName}.
     **/
    protected String getModuleName() {
        return moduleName;
    }

    /**
     * Configure the logic for this {@link InputHandler}. This overload
     * does so via a {@link FunctionModule}, but logic can be directly inputted
     * via {@link InputHandler#setHandler(String, Function)} or similar. The app input
     * is not injected into {@code handler} for this overload and must be handled manually.
     * <br><br>
     * Note that {@code handler} does not get added as a child until the module is built,
     * so property updates (e.g., {@link TUIModule.Builder#setApplication(ApplicationModule)})
     * won't propagate to it from this module.
     *
     * @param handler The module defining the logic for the handler.
     * @return self
     */
    public InputHandler setHandler(FunctionModule.Builder handler) {
        this.handlerType = InputHandler.HandlerType.MODULE;
        this.module = handler;
        return self();
    }

    /**
     * Configure the logic for this {@link InputHandler}. The app input
     * is injected into to {@code logic} and updated at {@code name}.
     *
     * @param name The name of the {@link FunctionModule} that will be built, and thus the input
     *             identifier for {@link ApplicationModule#getInput(String)} for what {@code logic}
     *             returns.
     * @param logic The {@link Function} that runs on the input collected from
     *              {@link ApplicationModule#getInput(String, Class)} and returns some value
     *              with the identifier provided by {@code name}.
     * @return self
     */
    public InputHandler setHandler(String name, Function<String, ?> logic) {
        this.handlerType = InputHandler.HandlerType.HANDLER;
        this.moduleName = name;
        this.logic = logic;
        return self();
    }

    /**
     * Configure the logic for this {@link InputHandler}. The app input
     * is injected into to {@code logic} and updated at {@code name}.
     *
     * @param name The name of the {@link FunctionModule} that will be built, and thus the input
     *             identifier for {@link ApplicationModule#getInput(String)} for what {@code logic}
     *             returns.
     * @param logic The {@link Function} that runs on the input collected from
     *              {@link ApplicationModule#getInput(String, Class)} and returns some value
     *              with the identifier provided by {@code name}.
     * @param exceptionHandler Fallback logic if a {@link RuntimeException} is thrown during
     *                         execution of {@code logic}.
     * @return self
     */
    public InputHandler setHandler(String name, Function<String, ?> logic, Consumer<String> exceptionHandler) {
        this.handlerType = InputHandler.HandlerType.SAFE_HANDLER;
        this.moduleName = name;
        this.logic = logic;
        this.exceptionHandler = exceptionHandler;
        return self();
    }

    /**
     * Adds a {@link FunctionModule} to execute after input is collected. <br>
     * <b>Note:</b> this module will not update application or other properties for {@code handler};
     * this must be done manually.
     * <br><br>
     * This is the lazy mutator that is called at build-time and configured via
     * {@link InputHandler#setHandler(FunctionModule.Builder)}.
     * <br><br>
     * Also checks for name duplicates.
     *
     * @param handler The Function Module builder to execute after input is collected
     * @return self
     */
    private InputHandler addHandler(FunctionModule.Builder handler) {
        main.addChild(handler);
        checkForHandlerDuplicates(handler.getName());
        return self();
    }

    /**
     * Adds a {@link FunctionModule} with name given by {@code name} to execute after input is collected.
     * The module collects input from the application via {@link InputHandler#inputName}.
     * It then runs {@code logic} on the input and returns to the application with
     * identifier given by {@code name}. Does <i>not</i> handle exceptions.
     * <br><br>
     * This is the lazy mutator that is called at build-time and configured via
     * {@link InputHandler#setHandler(String, Function)}.
     * <br><br>
     * Also checks for name duplicates.
     *
     * @param name The name of the {@link FunctionModule} to construct and the identifier for
     *             what {@code logic} returns to the application.
     * @param logic The logic to run on the input.
     * @return self
     */
    private InputHandler addHandler(String name, Function<String, ?> logic) {
        FunctionModule.Builder handler = FunctionModule.builder(name, () -> {
            ApplicationModule app = this.getApplication();
            if(app == null) {
                logger.warn("tried to run logic for handler \"{}\" but app was null", name);
                return null;
            }
            String input = app.getInput(inputName, String.class);
            logger.info("running logic on handler \"{}\" with input \"{}\"", name, input);
            return logic.apply(input);
        }).setApplication(getApplication());
        main.addChild(handler);
        checkForHandlerDuplicates(name);
        return self();
    }

    /**
     * Adds a {@link FunctionModule} with name given by {@code name} to execute after input is collected.
     * The module collects input from the application via {@link InputHandler#inputName}.
     * It then runs {@code logic} on the input and returns to the application with
     * identifier given by {@code name}. {@code exceptionHandler} catches
     * {@link RuntimeException} if that is thrown by {@code logic}.
     * <br><br>
     * This is the lazy mutator that is called at build-time and configured via
     * {@link InputHandler#setHandler(String, Function)}.
     * <br><br>
     * Also checks for name duplicates.
     *
     * @param name The name of the {@link FunctionModule} to construct and the identifier for
     *             what {@code logic} returns to the application.
     * @param logic The logic to run on the input.
     * @param exceptionHandler The logic to run if {@link RuntimeException} is thrown by {@code logic}.
     * @return self
     */
    private <T> InputHandler addSafeHandler(String name, Function<String, T> logic, Consumer<String> exceptionHandler) {
        FunctionModule.Builder handler = FunctionModule.builder(name, () -> {
            ApplicationModule app = this.getApplication();
            if(app == null) {
                logger.warn("tried to run logic for safe handler \"{}\" but app was null", name);
                return null;
            }
            String input = app.getInput(inputName, String.class);
            logger.debug("running logic on safe handler \"{}\" with input \"{}\"", name, input);
            T converted;
            try {
                converted = logic.apply(input);
            }
            catch(RuntimeException e) {
                logger.debug("caught exception \"{}\" for safe handler \"{}\": \"{}\"", e.getClass().getSimpleName(), name, e.getMessage());
                logger.trace("running exception handler for safe handler \"{}\"", name);
                exceptionHandler.accept(input);
                // revert to last
                return app.getInput(name);
            }
            return converted;
        }).setApplication(getApplication());
        main.addChild(handler);
        checkForHandlerDuplicates(name);
        return self();
    }

    /**
     * Logs an error at build-time if multiple modules have names that
     * collide with the name of the input to handle.
     *
     * @param name The name of the input that's being handled.
     */
    private void checkForHandlerDuplicates(String name) {
        if(TUIModule.Builder.usedNames.get(name) >= 2)
            logger.error("Duplicate names detected: Input Handler \"{}\" is attempting to handle \"{}\", but {} modules have that name.",
                    this.name, name, TUIModule.Builder.usedNames.get(name) - 1);
    }

    /**
     * Checks equality for properties given by the builder. For {@link InputHandler}, this includes
     * {@code inputName}, {@code handlerType}, and {@code moduleName},
     * as well as other requirements provided by {@link TUIModule#structuralEquals(TUIModule)}.
     */
    public boolean shallowStructuralEquals(InputHandler first, InputHandler second) {
        if(first == second) return true;
        if(first == null || second == null) return false;


        return  Objects.equals(first.inputName, second.inputName) &&
                Objects.equals(first.handlerType, second.handlerType) &&
                Objects.equals(first.moduleName, second.moduleName) &&
                super.shallowStructuralEquals(first, second);
    }

    /**
     * Builds a new {@link InputHandler} based on this configuration of this builder.
     * Adds the handling logic based on how it was set, e.g., via {@link InputHandler#setHandler(String, Function, Consumer)}.
     *
     * @return The built {@link InputHandler}.
     *
     * @implNote Clears the children first to ensure consistency when building multiple times.
     */
    public ContainerModule build() {
        if(handlerType == InputHandler.HandlerType.HANDLER || handlerType == InputHandler.HandlerType.SAFE_HANDLER) {
            for(TUIModule.Builder<?> child : main.getChildren()) {
                child.setName(""); //prevent duplicate name warning
            }
        }

        main.clearChildren();

        switch(handlerType) {
            case MODULE -> addHandler(module);
            case HANDLER -> addHandler(moduleName, logic);
            case SAFE_HANDLER -> addSafeHandler(moduleName, logic, exceptionHandler);
        }

        return super.build();
    }
}
