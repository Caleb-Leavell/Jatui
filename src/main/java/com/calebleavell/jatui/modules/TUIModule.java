package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public interface TUIModule {

    static final Scanner SYSTEM_IN = new Scanner(System.in);

    public static final String UNNAMED_ERROR = "[ERROR: This module was never named!]";

    /**
     * <p> For TUITextModule: display text </p>
     * <p> For TUIInputModule: collect input </p>
     * <p> For TUIFunctionModule: execute function </p>
     * <p>For all: linearly run all child modules </p>
     */
    void run();
    
    void runModuleAsChild(Builder<?> module);

    String getName();

    List<Builder<?>> getChildren();

    TUIApplicationModule getApplication();

    TUIModule.Builder<?> getChild(String name);

    <T extends Builder<?>> T getChild(String name, Class<T> builderType);

    void terminate();
    boolean isTerminated();

    TUIModule getCurrentRunningChild();

    TUIModule getCurrentRunningChild(String name);

    List<TUIModule> getCurrentRunningBranch();

    String toString(int indent, boolean displayChildren);

    Ansi getAnsi();

    Scanner getScanner();

    PrintStream getPrintStream();

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

    public static interface Builder<B extends Builder<B>> extends DirectedGraphNode<Builder<?>> {

        B setApplication(TUIApplicationModule app);

        B setApplicationRecursive(TUIApplicationModule app);

        B addChildren(List<Builder<?>> children);

        B addChildren(Builder<?>... children);

        B addChild(Builder<?> child);

        B addChild(int index, Builder<?> child);

        B clearChildren();

        B alterChildNames(boolean alterChildNames);

        B setAnsi(Ansi ansi);

        B prependAnsi(Ansi ansi);

        B appendAnsi(Ansi ansi);

        B hardSetAnsi(Ansi ansi);

        B allowAnsiOverride(boolean allow);

        B enableAnsi(boolean enable);

        B enableAnsiRecursive(boolean enable);

        B setScanner(Scanner scanner);

        B setScannerRecursive(Scanner scanner);

        B setPrintStream(PrintStream printStream);

        B setPrintStreamRecursive(PrintStream printStream);

        String getName();

        B setName(String name);

        Builder<?> getChild(String name);

        TUIApplicationModule getApplication();

        void prependToName(String name);

        B self();

        TUIModule build();

        /**
         * <p>Returns a deep copy of the application.</p>
         * @return The copy of this object.
         */
        B getCopy();
    }
}
