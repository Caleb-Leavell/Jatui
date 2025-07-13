package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Scanner;

public interface TUIModule {

    static final Scanner SYSTEM_IN = new Scanner(System.in);

    /**
     * <p> For TUITextModule: display text </p>
     * <p> For TUIInputModule: collect input </p>
     * <p> For TUIFunctionModule: execute function </p>
     * <p>For all: linearly run all child modules </p>
     */
    void run();

    String getName();

    List<Builder<?>> getChildren();

    TUIApplicationModule getApplication();

    void setChildrenApplication(TUIApplicationModule app);

    TUIModule.Builder<?> getChild(String name);

    <T extends Builder<?>> T getChild(String name, Class<T> builderType);

    void terminate();
    boolean isTerminated();

    TUIModule getCurrentRunningChild();

    TUIModule getCurrentRunningChild(String name);

    List<TUIModule> getCurrentRunningBranch();

    String toString(int indent, boolean displayChildren);

    Ansi getAnsi();

    public final static class NameOrModule {
        private TUIModule.Builder<?> module;
        private String moduleName;

        public NameOrModule(TUIModule.Builder<?> module) {
            this.module = module;
        }
        public NameOrModule(String moduleName) {
            this.moduleName = moduleName;
        }

        public TUIModule.Builder<?> getModule(TUIApplicationModule app) {
            if(module != null) return module;
            else return app.getChild(moduleName);
        }
    }

    public abstract static class Template<B extends Template<B>> extends TUIGenericModule.Builder<B> {
        protected final TUIContainerModule.Builder main;

        public Template(Class<B> type, String name) {
            super(type, name);
            main = new TUIContainerModule.Builder(name + "-main");
            this.addChild(main);
        }

        protected Template(Template<B> original) {
            super(original);
            this.main = original.main.getCopy();
        }

        /**
         * <p>Builds the finalized ContainerModule</p>
         * <p><strong>Note:</strong> If you are going to override this method, ensure any changes made to main or other are reset each time it's called.
         *          We want to ensure calling build() multiple times returns the same output.</p>
         * @return The built ContainerModule
         */
        @Override
        public TUIContainerModule build() {
            return new TUIContainerModule(self());
        }
    }

    public static interface Builder<B extends Builder<B>> extends DirectedGraph<Builder<?>> {

        public B application(TUIApplicationModule application);

        public List<Builder<?>> applicationHelper(TUIApplicationModule application, List<Builder<?>> visited);

        public B children(List<Builder<?>> children);

        public B children(Builder<?>... children);

        public B addChild(Builder<?> child);

        public B addChild(int index, Builder<?> child);

        public B clearChildren();

        public B alterChildNames(boolean alterChildNames);

        public B setAnsi(Ansi ansi);

        public B prependAnsi(Ansi ansi);

        public B appendAnsi(Ansi ansi);

        public B hardSetAnsi(Ansi ansi);

        public B allowAnsiOverride(boolean allow);

        public String getName();

        public B setName(String name);

        public Builder<?> getChild(String name);

        public TUIApplicationModule getApplication();

        public void prependToName(String name);

        public B self();

        public TUIModule build();

        /**
         * <p>Returns a deep copy of the application.</p>
         * <p>Note: The inputMap is deep-copied, but all entries in the map are shallow-copied.</p>
         * @return The copy of this object.
         */
        public B getCopy();
    }
}
