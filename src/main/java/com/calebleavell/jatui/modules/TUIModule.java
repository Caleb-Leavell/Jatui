package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * <p>TUIContainerModule provides a basic implementation of this class.</p>
 */
public abstract class TUIModule {

    static final Scanner SYSTEM_IN = new Scanner(System.in);

    public static final String UNNAMED_ERROR = "[ERROR: This module was never named!]";

    public enum Property {
        APPLICATION,
        ANSI,
        SCANNER,
        PRINTSTREAM,
        ENABLE_ANSI
    }

    /**
     * <p>The identifier for this module.</p>
     * <p>It is recommended to try and keep this unique in order to allow identification methods (e.g. TUIApplicationModule.getInput()) to function properly.</p>
     */
    private String name;

    /**
     * The application the module is a child of.
     */
    private TUIApplicationModule application;

    /**
     * Every child module that should be run.
     */
    private List<TUIModule.Builder<?>> children;

    /**
     * <p>The ansi that may be displayed (Jansi object)</p>
     * <p>Currently, only TUITextModule displays the ansi</p>
     */
    private Ansi ansi;

    /**
     * <p>The Scanner that reads input from the user-defined source</p>
     * <p>Is set to System.in by default (defined as SYSTEM_IN in TUIModule) </p>
     */
    private Scanner scanner;

    /**
     * <p>The PrintStream that outputs data to the user-defined location</p>
     * <p>Is set to System.out by default</p>
     */
    private PrintStream printStream;

    /**
     * If the for loop is run() is currently active, this will be the child that is currently running.
     */
    private TUIModule currentRunningChild;

    protected boolean terminated = false;

    public int MAX_ITERATIONS_ON_TO_STRING = 6;

    public void run() {
        for(TUIModule.Builder<?> child : children) {
            if(terminated) break;
            TUIModule toRun = child.build();
            currentRunningChild = toRun;
            toRun.run();
            currentRunningChild = null;
        }
    }

    public void runModuleAsChild(TUIModule.Builder<?> module) {
        if(currentRunningChild == null) {
            TUIModule built = module.build();
            currentRunningChild = built;
            built.run();
            currentRunningChild = null;
            return;
        }

        TUIModule currentRunningModule = getCurrentRunningBranch().getLast();
        currentRunningModule.runModuleAsChild(module);
    }

    public String getName() {
        return name;
    }

    public List<TUIModule.Builder<?>> getChildren() {
        return children;
    }

    public TUIModule.Builder<?> getChild(String name) {
        for(TUIModule.Builder<?> child : children) {
            TUIModule.Builder<?> returned = child.getChild(name);
            if(returned != null) return returned;
        }

        return null;
    }

    public <T extends TUIModule.Builder<?>> T getChild(String name, Class<T> type) {
        TUIModule.Builder<?> child = getChild(name);
        if(child.getClass() == type) {
            return type.cast(child);
        }
        else return null;
    }

    public TUIApplicationModule getApplication() {
        return application;
    }

    public void terminate() {
        this.terminated = true;
        if(this.currentRunningChild != null) currentRunningChild.terminate();
    }

    public void terminateChild(String moduleName) {
        getCurrentRunningBranch().forEach(m -> {
            if(m.getName().equals(moduleName)) m.terminate();
        });
    }

    public boolean isTerminated() {
        return terminated;
    }

    public TUIModule getCurrentRunningChild() {
        return currentRunningChild;
    }

    public TUIModule getCurrentRunningChild(String name) {
        List<TUIModule> branch = getCurrentRunningBranch();

        for(TUIModule m : branch) {
            if(m.getName().equals(name)) return m;
        }

        return null;
    }

    public List<TUIModule> getCurrentRunningBranch() {
        List<TUIModule> currentRunningBranch = new ArrayList<>();
        currentRunningBranch.add(this);

        if(currentRunningChild != null) {
            currentRunningBranch.addAll(currentRunningChild.getCurrentRunningBranch());
        }

        return currentRunningBranch;
    }

    public Ansi getAnsi() {
        return this.ansi;
    }

    public Scanner getScanner() {
        return this.scanner;
    }

    public PrintStream getPrintStream() {
        return this.printStream;
    }

    /**
     * Recursively generates toString with info for this scene and all children.
     * Will only go as deep as MAX_ITERATIONS_ON_TOSTRING
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        return toString(0, true);
    }

    /**
     * recursive helper method for toString()
     */
    public String toString(int indent, boolean displayChildren) {
        if(indent > MAX_ITERATIONS_ON_TO_STRING) {
            return "";
        }

        StringBuilder output = new StringBuilder();
        output.append("\t".repeat(Math.max(0, indent)));

        output.append(this.name).append(" -- ").append(this.getClass().getSimpleName());

        if (displayChildren) {
            for (TUIModule.Builder<?> child : children) {
                output.append("\n").append(child.build().toString(indent + 1, true));
            }
        }

        return output.toString();
    }

    protected TUIModule(Builder<?> builder) {
        this.name = builder.name;
        this.application = builder.application;
        this.children = new ArrayList<>(builder.children);
        this.ansi = builder.ansi;
        this.scanner = builder.scanner;
        this.printStream = builder.printStream;
    }

    public abstract static class Builder<B extends Builder<B>> implements DirectedGraphNode<Property, Builder<?>> {
        protected String name;
        protected List<TUIModule.Builder<?>> children = new ArrayList<>();
        protected Map<Property, PropertyUpdateFlag> propertyUpdateFlags = new HashMap<>();

        // properties
        protected TUIApplicationModule application;
        protected Ansi ansi = ansi();
        protected Scanner scanner = TUIModule.SYSTEM_IN;
        protected PrintStream printStream = System.out;
        protected boolean enableAnsi = true;

        protected final Class<B> type;

        public Builder(Class<B> type, String name) {
            this.type = type;
            this.name = name;
            for(Property property : Property.values()) {
                propertyUpdateFlags.put(property, PropertyUpdateFlag.UPDATE);
            }
        }

        protected Builder(Builder<B> original) {
            this.name = original.name;
            for(TUIModule.Builder<?> child : original.children) {
                this.children.add(child.getCopy());
            }
            for(Property key : original.propertyUpdateFlags.keySet()) {
                this.propertyUpdateFlags.put(key, original.propertyUpdateFlags.get(key));
            }
            this.application = original.application;
            this.ansi = original.ansi;
            this.scanner = original.scanner;
            this.printStream = original.printStream;
            this.type = original.type;
        }

        @Override
        public List<TUIModule.Builder<?>> getChildren() {
            return children;
        }

        @Override
        public Map<Property, PropertyUpdateFlag> getPropertyUpdateFlags() {
            return propertyUpdateFlags;
        }

        public B setPropertyUpdateFlag(Property property, PropertyUpdateFlag flag) {
            propertyUpdateFlags.put(property, flag);

            return self();
        }

        public B lockProperty(Property property) {
            propertyUpdateFlags.put(property, PropertyUpdateFlag.HALT);

            return self();
        }

        public B unlockProperty(Property property) {
            propertyUpdateFlags.put(property, PropertyUpdateFlag.UPDATE);

            return self();
        }

        public B addChildren(List<TUIModule.Builder<?>> children) {
            for(TUIModule.Builder<?> child : children) addChild(child);
            return self();
        }

        public B addChildren(TUIModule.Builder<?>... children) {
            for(TUIModule.Builder<?> child : children) addChild(child);
            return self();
        }

        public B addChild(TUIModule.Builder<?> child) {
            this.children.add(child);
            return self();
        }

        public B addChild(int index, TUIModule.Builder<?> child) {
            this.children.add(index, child);
            return self();
        }

        public Builder<B> clearChildren() {
            this.children.clear();

            return self();
        }

        /**
         * Finds a child matching the name.
         * It is recommended to name all modules uniquely so this returns a unique module every time.
         * Also checks protected children.
         *
         * @param name The name of the child
         * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
         */
        public TUIModule.Builder<?> getChild(String name) {
            return dfs(m -> {
                return m.getName().equals(name);
            });
        }

        public String getName() {
            return name;
        }

        public B setName(String name) {
            this.name = name;
            return self();
        }

        public void prependToName(String name) {
            this.name = name + "-" + this.name;
        }

        public TUIApplicationModule getApplication() {
            return this.application;
        }

        public B setApplication(TUIApplicationModule app) {
            this.updateProperty(Property.APPLICATION, n -> {n.application = app;});

            return self();
        }

        public B setAnsi(Ansi ansi) {
            this.updateProperty(Property.ANSI, n -> {n.ansi = ansi;});
            return self();
        }

        /**
         * <p>Prepends the ansi to the module's ansi.</p>
         * <p>Also prepends the ansi for all children that are currently added .</p>
         * <p>Whether this can be overridden depends on the ANSI property flag.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        public B prependAnsi(Ansi ansi) {
            this.updateProperty(Property.ANSI, n -> {n.ansi = ansi().a(ansi).a(n.ansi);});

            return self();
        }

        /**
         * <p>Appends the ansi to the module's ansi (using Jansi).</p>
         * <p>Also appends the ansi for all children that are currently added .</p>
         * <p>Whether this can be overridden depends on the ANSI property flag.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        public B appendAnsi(Ansi ansi) {
            this.updateProperty(Property.ANSI, n -> {n.ansi = ansi().a(n.ansi).a(ansi);});

            return self();
        }

        public B setScanner(Scanner scanner) {
            this.updateProperty(TUIModule.Property.SCANNER, n -> {
                n.scanner = scanner;
            });

            return self();
        }

        public B setPrintStream(PrintStream printStream) {
            this.updateProperty(TUIModule.Property.PRINTSTREAM, n -> {
                n.printStream = printStream;
            });

            return self();
        }

        public B enableAnsi(boolean enable) {
            this.updateProperty(Property.ENABLE_ANSI, n -> {
                n.enableAnsi = enable;
            });
            return self();
        }

        public B self() {
            return type.cast(this);
        }

        public abstract B getCopy();

        public abstract TUIModule build();
    }

    public abstract static class Template<B extends Template<B>> extends TUIModule.Builder<B> {
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

}
