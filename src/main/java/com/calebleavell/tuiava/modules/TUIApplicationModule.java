package com.calebleavell.tuiava.modules;

import java.util.Map;
import java.util.HashMap;

public class TUIApplicationModule extends TUIGenericModule {

    private final Map<String, Object> inputMap; // maps module names to the input object

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
        setChildrenApplication(this);
    }

    public TUIModule.Builder<?> getHome() {
        if(this.getChildren().isEmpty()) return null;
        return this.getChildren().getFirst();
    }

    public TUIApplicationModule(Builder builder) {
        super(builder);
        this.inputMap = builder.inputMap;
    }

    public static class Builder extends TUIGenericModule.Builder<Builder> {
        private final Map<String, Object> inputMap = new HashMap<>();

        public Builder(String name) {
            super(Builder.class, name);
            this.children.add(TUIModuleFactory.Empty("home"));
        }

        public Builder home(TUIModule.Builder<?> home) {
            this.children.set(0, home);
            return self();
        }

        @Override
        public TUIApplicationModule build() {
            TUIApplicationModule app = new TUIApplicationModule(self());
            app.setChildrenApplication(app);
            return app;
        }
    }
}
