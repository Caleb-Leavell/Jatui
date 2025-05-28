package com.calebleavell.tuiava.modules;

import java.util.List;

public interface TUIModule {

    /**
     * <p> For TUITextModule: display text </p>
     * <p> For TUIInputModule: collect input </p>
     * <p> For TUIFunctionModule: execute function </p>
     * <p>For all: linearly run all child modules </p>
     */
    void run();

    String getName();
    void setName(String name);

    List<TUIModule> getChildren();
    void setChildren(List<TUIModule> children);

    TUIModule getChild(String name);

    TUIModule getChildHelper(String name, List<TUIModule> visited);

    TUIApplicationModule getApplication();
    void setApplication(TUIApplicationModule application);

    /**
     * Same as SetApplicationRecursive, but it doesn't edit the current app.
     * @param app The application to apply
     */
    void setChildrenApplication(TUIApplicationModule app);

    /**
     * Sets the module app and the app of all its children to the inputted app.
     * Only sets the app if it's currently null.
     * @param app The application to apply
     */
    void setApplicationRecursive(TUIApplicationModule app);

    void terminate();
    boolean isTerminated();

    String toString(int indent, boolean displayChildren);

    public static class NameOrModule {
        private TUIModule module;
        private String moduleName;

        public NameOrModule(TUIModule module) {
            this.module = module;
        }
        public NameOrModule(String moduleName) {
            this.moduleName = moduleName;
        }

        public TUIModule getModule(TUIApplicationModule app) {
            if(module != null) return module;
            else return app.getChild(moduleName);
        }
    }
}
