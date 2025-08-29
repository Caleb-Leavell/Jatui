package com.calebleavell.jatui.modules;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.Map;
import java.util.HashMap;

public class TUIApplicationModule extends TUIModule {

    private final Map<String, Object> inputMap; // maps module names to the input object
    private TUIModule.Builder<?> onExit;

    @Override
    public void run() {
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
        if(child != null) updateInput(child.build(), input);
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
     * <ul>
     *     <li><strong>onExit</strong> (Note: checks structural equality, not reference equality)</li>
     *     <li>name</li>
     *     <li>application</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul>
     * <p>Note: Runtime properties (e.g., inputMap, currentRunningChild, terminated), are not considered.</p>
     * @param other The TUIApplicationModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     *  Also, There is no need for an equalTo method that overrides {@link TUIModule.Builder#equalTo(TUIModule.Builder, TUIModule.Builder)} in {@link TUIApplicationModule.Builder} due to the fact that onExit is a
     * child within the Builder, but not in the built module. This ensures property propagation is applied to onExit before building, but
     * after building it is run last.
     */
    public boolean equals(TUIApplicationModule other) {
        if(this == other) return true;
        if(other == null) return false;

        return TUIModule.Builder.equals(onExit, other.onExit) && super.equals(other);
    }

    public TUIApplicationModule(Builder builder) {
        super(builder);
        this.inputMap = builder.inputMap;
        this.onExit = builder.onExit;

        for(TUIModule.Builder<?> child : getChildren()) {
            if(this.getApplication() == null) child.setApplication(this);
            else child.setApplication(this.getApplication());

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
            this.children.add(TUIModuleFactory.empty("home"));
            this.children.add(onExit);
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
            return new TUIApplicationModule.Builder();
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
            this.children.remove(this.onExit);
            this.onExit = onExit;
            this.children.add(onExit);
            return self();
        }

        @Override
        public TUIApplicationModule build() {
            return new TUIApplicationModule(self());
        }
    }
}
