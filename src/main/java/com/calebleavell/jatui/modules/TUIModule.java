package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.*;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * <p>TUIContainerModule provides a basic implementation of this class.</p>
 */
public abstract class TUIModule {

    /** reads from System.in **/
    static final Scanner DEFAULT_SCANNER = new Scanner(System.in);

    /** The standard message for when a module isn't named **/
    public static final String UNNAMED_ERROR = "[ERROR: This module was never named!]";

    public enum Property {
        APPLICATION,
        /** Deals with replacing the ansi completely with setAnsi() **/
        SET_ANSI,
        /** Deals with appending to or prepending to the ansi */
        MERGE_ANSI,
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
     * <p>Whether ansi will be displayed or not.</p>
     */
    private boolean enableAnsi;

    /**
     * If the for loop in {@link TUIModule#run() } is currently active, this will be the child that is currently running.
     */
    private TUIModule currentRunningChild;

    protected boolean terminated = false;

    public int MAX_ITERATIONS_ON_TO_STRING = 6;

    public void run() {
        terminated = false;

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
        if(child == null) return null;
        if(child.getClass() == type) {
            return type.cast(child);
        }
        else return null;
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

    public TUIApplicationModule getApplication() { return application; }

    public Ansi getAnsi() {
        return this.ansi;
    }

    public Scanner getScanner() {
        return this.scanner;
    }

    public PrintStream getPrintStream() {
        return this.printStream;
    }

    public boolean getAnsiEnabled() {return this.enableAnsi; }

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


    /**
     * <p>Checks equality for properties given by the builder.</p>
     *
     * <p>For TUIModule, this includes: </p>
     * <strong><ul>
     *     <li>name</li>
     *     <li>application (Note: checks reference equality, not structural equality) </li>
     *     <li>children</li>
     *     <li>ansi</li>
     *     <li>scanner</li>
     *     <li>printStream</li>
     *     <li>enableAnsi</li>
     * </ul></strong>
     * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered.</p>
     * @param other The TUIModule to compare
     * @return true if this module equals {@code other} according to builder-provided properties
     * @implNote This method intentionally does not override {@link Object#equals(Object)} so that things like HashMaps still check by method reference.
     *  This method is merely for checking structural equality, which is generally only necessary for manual testing.
     */
    public boolean equals(TUIModule other) {
        if(this == other) return true;
        if(other == null) return false;

        // check equality of children
        List<TUIModule.Builder<?>> children = this.getChildren();
        List<TUIModule.Builder<?>> otherChildren = other.getChildren();

        if(children.size() != otherChildren.size()) return false;

        for(int i = 0; i < children.size(); i ++) {
            // ensure if one of the children are null, the corresponding child for other also is
            if(children.get(i) == null && otherChildren.get(i) == null) continue;
            if(children.get(i) == null || otherChildren.get(i) == null) return false;

            if(!children.get(i).equals(otherChildren.get(i))) return false;
        }

        return (Objects.equals(name, other.name) &&
                Objects.equals(application, other.application) && // this is intentionally a reference equality check
                Objects.equals(ansi.toString(), other.ansi.toString()) &&
                Objects.equals(scanner, other.scanner) &&
                Objects.equals(printStream, other.printStream) &&
                enableAnsi == other.enableAnsi);
    }


    protected TUIModule(Builder<?> builder) {
        this.name = builder.name;
        this.application = builder.application;
        this.children = new ArrayList<>(builder.children);
        this.ansi = builder.ansi;
        this.scanner = builder.scanner;
        this.printStream = builder.printStream;
        this.enableAnsi = builder.enableAnsi;
    }

    public abstract static class Builder<B extends Builder<B>> implements DirectedGraphNode<Property, Builder<?>, B> {
        protected String name;
        protected List<TUIModule.Builder<?>> children = new ArrayList<>();
        protected Map<Property, PropertyUpdateFlag> propertyUpdateFlags = new HashMap<>();

        // properties
        protected TUIApplicationModule application;
        protected Ansi ansi = ansi();
        protected Scanner scanner = TUIModule.DEFAULT_SCANNER;
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

        protected Builder(Class<B> type) {
            this.type = type;
        }

        protected void deepCopy(B original, Map<Builder<?>, Builder<?>> visited) {
            if(visited.get(original) != null) return;
            visited.put(original, this);

            shallowCopy(type.cast(original));

            for(Builder<?> child : original.getChildren()) {
                Builder<?> newChild = child.createInstance();
                Builder.deepCopyHelper(child, newChild, visited);

                // we don't even need to check that child and newChild are of the same type since createInstance returns T
                getChildren().add(newChild);
            }
        }

        private static <T extends Builder<T>> void deepCopyHelper(Builder<T> original, Builder<?> copyInto, Map<Builder<?>, Builder<?>> visited) {
            Builder<T> toCopy = original.getType().cast(copyInto);
            toCopy.deepCopy(original.self(), visited);
        }

        protected Map<Builder<?>, Builder<?>> deepCopy(B original) {
            Map<Builder<?>, Builder<?>> copyMap = new HashMap<>();
            deepCopy(original, copyMap);
            return copyMap;
        }

        /**
         * Creates a deep copy of this node and it's children
         * @return A deep copy of self
         */
        public B getDeepCopy() {
            B copy = createInstance();
            copy.deepCopy(self());
            return copy;
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        public abstract B createInstance();

        protected void shallowCopy(B original) {
            this.name = original.name;
            for(Property key : original.propertyUpdateFlags.keySet()) {
                this.propertyUpdateFlags.put(key, original.propertyUpdateFlags.get(key));
            }
            this.setApplicationNonRecursive(original.application);
            this.ansi = original.ansi;
            this.scanner = original.scanner;
            this.printStream = original.printStream;
            this.enableAnsi = original.enableAnsi;
        }

        /**
         * Creates a deep copy of this node and it's children; delegates to getDeepCopy(), created for simplicity.
         * @return A deep copy of self
         */
        public B getCopy() {
            return getDeepCopy();
        }

        public Class<B> getType() {
            return type;
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

        public B updateProperties(TUIModule module) {
            this.setApplication(module.getApplication());
            this.setAnsi(module.getAnsi());
            this.setScanner(module.getScanner());
            this.setPrintStream(module.getPrintStream());
            this.enableAnsi(module.getAnsiEnabled());

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
         *
         * @param name The name of the child
         * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
         */
        public TUIModule.Builder<?> getChild(String name) {
            return dfs(m -> m.getName().equals(name));
        }

        public <T extends TUIModule.Builder<?>> T getChild(String name, Class<T> type) {
            TUIModule.Builder<?> child = getChild(name);
            if(child.getClass() == type) {
                return type.cast(child);
            }
            else return null;
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

        public Ansi getAnsi() {
            return this.ansi;
        }

        public Scanner getScanner() {
            return this.scanner;
        }

        public PrintStream getPrintStream() {
            return this.printStream;
        }

        public boolean getAnsiEnabled() {
            return this.enableAnsi;
        }

        public B setApplication(TUIApplicationModule app) {
            this.updateProperty(Property.APPLICATION, n -> n.setApplicationNonRecursive(app));

            return self();
        }

        private B setApplicationNonRecursive(TUIApplicationModule app) {
            if(this.application != null && app == null) return self();
            this.application = app;
            return self();
        }

        /**
         * <p>Recursively replaces the ansi with the provided ansi.</p>
         * <p>Automatically locks the ansi after calling.</p>
         * @param ansi The ansi to recursively replace
         * @return self
         */
        public B setAnsi(Ansi ansi) {
            this.updateProperty(Property.SET_ANSI, n -> n.ansi = ansi);
            this.lockProperty(Property.SET_ANSI);
            return self();
        }

        /**
         * <p>Prepends the ansi to the module's ansi.</p>
         * <p>Also prepends the ansi for all children that are currently added .</p>
         * <p>Whether this can be overridden depends on the ANSI property flag.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>;
         * <p>Note: no property is automatically locked after calling this method.</p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        public B prependAnsi(Ansi ansi) {
            this.updateProperty(Property.MERGE_ANSI, n -> n.ansi = ansi().a(ansi).a(n.ansi));

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
            this.updateProperty(Property.MERGE_ANSI, n -> n.ansi = ansi().a(n.ansi).a(ansi));

            return self();
        }

        public B setScanner(Scanner scanner) {
            this.updateProperty(TUIModule.Property.SCANNER, n -> n.scanner = scanner);
            this.lockProperty(Property.SCANNER);

            return self();
        }

        public B setPrintStream(PrintStream printStream) {
            this.updateProperty(TUIModule.Property.PRINTSTREAM, n -> n.printStream = printStream);
            this.lockProperty(Property.PRINTSTREAM);

            return self();
        }

        public B enableAnsi(boolean enable) {
            this.updateProperty(Property.ENABLE_ANSI, n -> n.enableAnsi = enable);
            this.lockProperty(Property.ENABLE_ANSI);
            return self();
        }

        public B self() {
            return type.cast(this);
        }

        /**
         * <p>Checks equality for properties given by the builder.</p>
         *
         * <p>For TUIModule, this includes: </p>
         * <strong><ul>
         *     <li>name</li>
         *     <li>application</li>
         *     <li>ansi</li>
         *     <li>scanner</li>
         *     <li>printStream</li>
         *     <li>enableAnsi</li>
         * </ul></strong>
         * <p>Note: Runtime properties (e.g., currentRunningChild, terminated), are not considered. Children are also not considered here,
         *  but are considered in equals().
         * @param first The first TUIModule to compare
         * @param second The second TUIModule to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         * @implNote This is the {@code Function<TUIModule<?>, TUIModule.Builder<?>, Boolean>} that is passed into {@link DirectedGraphNode#equals(DirectedGraphNode)}
         */
        @Override
        public boolean equalTo(B first, B second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            String firstAnsi = first.ansi != null ? first.ansi.toString() : null;
            String secondAnsi = second.ansi != null ? second.ansi.toString() : null;

            return (Objects.equals(first.name, second.name) &&
                    Objects.equals(first.application, second.application) && // intentionally checks by reference
                    Objects.equals(firstAnsi, secondAnsi) &&
                    Objects.equals(first.scanner, second.scanner) &&
                    Objects.equals(first.printStream, second.printStream) &&
                    first.enableAnsi == second.enableAnsi);
        }

//        @Override
//        public String toString() {
//            return this.name;
//        }

        /**
         * This is the same as equalTo, but it's static and does include a recursive children check.
         *
         * @param first The first TUIModule to compare
         * @param second The second TUIModule to compare
         * @return {@code true} if {@code first} and {@code second} are equal according to builder-provided properties
         * @implNote Polymorphism is automatic here and thus this method does not generally need to be overloaded.
         */
        public static boolean equals(TUIModule.Builder<?> first, TUIModule.Builder<?> second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return first.equals(second);
        }

        public abstract TUIModule build();
    }

    public abstract static class Template<B extends Template<B>> extends TUIModule.Builder<B> {
        protected TUIContainerModule.Builder main;

        public Template(Class<B> type, String name) {
            super(type, name);
            main = new TUIContainerModule.Builder(name + "-main");
            this.addChild(main);
        }

        protected Template(Class<B> type) {
            super(type);
        }

        @Override
        public void deepCopy(B original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
            super.deepCopy(original, visited);
            main = (TUIContainerModule.Builder) visited.get(original.main);
        }

        /**
         * <p>Builds the finalized ContainerModule</p>
         * <p><strong>Note:</strong> If you are going to override this method, ensure any changes made to main or other are reset each time it's called.
         *          We want to ensure calling build() multiple times returns the same output.
         *          Most likely, you'll want to call main.clearChildren() as the first line of the override.
         *          </p>
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

        public NameOrModule getCopy() {
            if(module != null) return new NameOrModule(module.getCopy());
            else return new NameOrModule(moduleName);
        }
    }

}
