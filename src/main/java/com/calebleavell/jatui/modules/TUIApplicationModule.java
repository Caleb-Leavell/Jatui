package com.calebleavell.jatui.modules;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class TUIApplicationModule extends TUIModule {

    private final Map<String, Object> inputMap; // maps module names to the input object
    private TUIModule.Builder<?> onExit;

    @Override
    public void run() {
        terminated = false;
        super.run();
        onExit.build().run();
    }

    public Object getInput(String moduleName) {
        return inputMap.get(moduleName);
    }

    /**
     * Returns the input for the given module only if 1. it exists 2. it is of the correct type.
     * @param moduleName The name of the module that collected the input
     * @param type The class type of the input
     * @return The input, which will be the same type as the "type" parameter, or null if it cannot be returned correctly.
     * @param <T> The type to safely cast to.
     */
    public <T> T getInput(String moduleName, Class<T> type) {
        if(inputMap.get(moduleName) == null) return null;
        if(inputMap.get(moduleName).getClass().equals(type)) return type.cast(inputMap.get(moduleName));
        else return null;
    }

    public void updateInput(TUIModule module, Object input) {
        inputMap.put(module.getName(), input);
    }

    public void updateInput(String moduleName, Object input) {
        TUIModule.Builder<?> child = getChild(moduleName);
        if(child == null) return;
        else updateInput(child.build(), input);
    }

    public void setHome(TUIModule.Builder<?> home) {
        this.getChildren().set(0, home);

        for(TUIModule.Builder<?> child : getChildren()) {
            child.setApplication(this);
            child.setPrintStream(this.getPrintStream());
            child.setScanner(this.getScanner());
            child.enableAnsi(this.getAnsiEnabled());
        }
    }

    public TUIModule.Builder<?> getHome() {
        if(this.getChildren().isEmpty()) return null;
        return this.getChildren().getFirst();
    }

    public void setOnExit(TUIModule.Builder<?> onExit) {

        this.onExit = onExit;
        onExit.setApplication(this);
        onExit.setPrintStream(this.getPrintStream());
        onExit.setScanner(this.getScanner());
        onExit.enableAnsi(this.getAnsiEnabled());
    }

    public TUIModule.Builder<?> getOnExit() {
        return onExit;
    }

    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUIApplicationModule, this includes: </p>
     * <strong><ul>
     *     <li>onExit</li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul></strong>
     * <p>Note: Runtime properties (e.g., inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param o The object to compare (must be a TUIModule object)
     * @return Whether this object equals o
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(getClass() != o.getClass()) return false;

        TUIApplicationModule other = (TUIApplicationModule) o;

        return Objects.equals(onExit, other.onExit) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(onExit, super.hashCode());
    }

    public TUIApplicationModule(Builder builder) {
        super(builder);
        this.inputMap = builder.inputMap;
        this.onExit = builder.onExit;

        for(TUIModule.Builder<?> child : getChildren()) {
            child.setApplication(this);
            child.setPrintStream(this.getPrintStream());
            child.setScanner(this.getScanner());
            child.enableAnsi(this.getAnsiEnabled());
        }

        this.getChildren().remove(onExit);
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        private final Map<String, Object> inputMap = new HashMap<>();
        private TUIModule.Builder<?> onExit = new TUITextModule.Builder("exit", "Exiting...")
                .setAnsi(ansi().fgRgb(125, 100, 100));

        public Builder(String name) {
            super(Builder.class, name);
            this.children.add(TUIModuleFactory.Empty("home"));
            this.children.add(onExit);
        }

        protected Builder(Builder original) {
            super(original);
            this.inputMap.putAll(original.inputMap);
            this.onExit = original.onExit.getCopy();
        }

        /**
         * <p>Returns a deep copy of the application.</p>
         * <p>Note: The inputMap is deep-copied, but all entries in the map are shallow-copied.</p>
         * @return The copy of this object.
         */
        @Override
        public Builder getCopy() {
            return new Builder(this);
        }

        public TUIModule.Builder<?> getHome() {
            return this.children.getFirst();
        }

        public TUIModule.Builder<?> getOnExit() {
            return this.onExit;
        }

        public Builder setHome(TUIModule.Builder<?> home) {
            this.children.set(0, home);
            return self();
        }

        public Builder setOnExit(TUIModule.Builder<?> onExit) {
            this.onExit = onExit;
            return self();
        }

        @Override
        public TUIApplicationModule build() {
            return new TUIApplicationModule(self());
        }
    }
}
