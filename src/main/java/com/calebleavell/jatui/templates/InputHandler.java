package com.calebleavell.jatui.templates;

import com.calebleavell.jatui.core.DirectedGraphNode;
import com.calebleavell.jatui.modules.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class InputHandler extends ModuleTemplate<InputHandler> {

    protected String inputName;

    protected InputHandler.HandlerType handlerType;
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

    public InputHandler(String name, String inputName) {
        super(InputHandler.class, name);
        this.inputName = inputName;
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
        this.inputName = other.inputName;
        this.handlerType = other.handlerType;
        if(other.module != null) this.module = other.module.getCopy();
        this.logic = other.logic;
        this. exceptionHandler = other.exceptionHandler;
        this.moduleName = other.moduleName;
    }

    public String getInputName(String inputName) {
        return this.inputName;
    }

    public InputHandler setInputName(String inputName) {
        this.inputName = inputName;
        return self();
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
        this.handlerType = InputHandler.HandlerType.MODULE;
        this.module = handler;
        return self();
    }

    public InputHandler setHandler(String name, Function<String, ?> logic) {
        this.handlerType = InputHandler.HandlerType.HANDLER;
        this.moduleName = name;
        this.logic = logic;
        return self();
    }

    public InputHandler setHandler(String name, Function<String, ?> logic, Consumer<String> exceptionHandler) {
        this.handlerType = InputHandler.HandlerType.SAFE_HANDLER;
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
            String input = app.getInput(inputName, String.class);
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


        return  Objects.equals(first.inputName, second.inputName) &&
                Objects.equals(first.handlerType, second.handlerType) &&
                Objects.equals(first.moduleName, second.moduleName) &&
                super.shallowStructuralEquals(first, second);
    }

    public ContainerModule build() {
        if(handlerType == InputHandler.HandlerType.HANDLER || handlerType == InputHandler.HandlerType.SAFE_HANDLER) {
            for(TUIModule.Builder<?> child : main.getChildren()) {
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
