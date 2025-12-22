package com.calebleavell.jatui.modules;

import com.calebleavell.jatui.templates.NumberedModuleSelector;

/**
 * Stores either the module builder or the name of a module, which abstracts
 * module retrieving. See usage in {@link NumberedModuleSelector}
 */
public final class NameOrModule {
    private TUIModule.Builder<?> module;
    private String moduleName;

    /**
     * Sets the stored module to a concrete reference to a builder.
     * @param module The module to remember.
     */
    public NameOrModule(TUIModule.Builder<?> module) {
        this.module = module;
    }

    /**
     * Sets the stored module to the name of a builder.
     * @param moduleName The name of the module to remember.
     */
    public NameOrModule(String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Fetches the module that was inputted from either the concrete reference
     * or the name. Requires the possible names to be a child of {@code app}.
     *
     * @param app The app that a potential name of the module would be a child of.
     * @return The remembered module.
     */
    public TUIModule.Builder<?> getModule(ApplicationModule app) {
        if(module != null) return module;
        else return app.getChild(moduleName);
    }

    /**
     * Creates a new instance of this {@code NameOrModule} object that remembers
     * a copy of the module if a concrete reference was stored, or the same name
     * if only the name was stored.
     *
     * @return The new copy of this instance.
     */
    public NameOrModule getCopy() {
        if(module != null) return new NameOrModule(module.getCopy());
        else return new NameOrModule(moduleName);
    }
}