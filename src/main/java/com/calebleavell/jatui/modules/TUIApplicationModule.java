package com.calebleavell.jatui.modules;

import static com.calebleavell.jatui.modules.TUIModule.Property.ANSI;
import static org.fusesource.jansi.Ansi.ansi;

import java.util.Map;
import java.util.HashMap;

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
            child.updateProperties(this);
        }
    }

    public TUIModule.Builder<?> getHome() {
        if(this.getChildren().isEmpty()) return null;
        return this.getChildren().getFirst();
    }

    public void setOnExit(TUIModule.Builder<?> onExit) {
        this.onExit = onExit;
    }

    public TUIModule.Builder<?> getOnExit() {
        return onExit;
    }

    public TUIApplicationModule(Builder builder) {
        super(builder);
        this.inputMap = builder.inputMap;
        this.onExit = builder.onExit;
        this.onExit.setApplication(this);
    }

    public static class Builder extends TUIModule.Builder<Builder> {
        private final Map<String, Object> inputMap = new HashMap<>();
        private TUIModule.Builder<?> onExit = new TUITextModule.Builder("exit", "Exiting...")
                .setAnsi(ansi().fgRgb(125, 100, 100))
                .lockProperty(ANSI);

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

        public Builder home(TUIModule.Builder<?> home) {
            this.children.set(0, home);
            return self();
        }

        public Builder onExit(TUIModule.Builder<?> onExit) {
            this.onExit = onExit;
            return self();
        }

        @Override
        public TUIApplicationModule build() {
            this.getChildren().remove(onExit);
            TUIApplicationModule app = new TUIApplicationModule(self());
            this.getChildren().add(onExit);
            this.updateProperties(app);
            this.setApplication(app);
            return app;
        }
    }
}
