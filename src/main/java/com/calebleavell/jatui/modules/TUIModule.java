package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.io.PrintStream;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fusesource.jansi.Ansi.ansi;

// TODO - copyright info
/**
 * The abstract class for all TUIModules. <br>
 * A TUIModule is an immutable, analyzable runtime unit of a TUI that can have children and be run. <br>
 * Use {@link TUIModule.Builder} to construct a TUIModule (note: all concrete subclasses will have their own builder). <br>
 * <br>
 * Use {@link TUIContainerModule} as the minimal implementation for this class. <br>
 * <br>
 * This class contains {@link TUIModule.Builder}, {@link TUIModule.Template}, and {@link TUIModule.NameOrModule} as subclasses <br>
 *
 *
 */
public abstract class TUIModule {

    /** reads from System.in **/
    static final Scanner DEFAULT_SCANNER = new Scanner(System.in);

    /** The standard message for when a module isn't named **/
    public static final String UNNAMED_ERROR = "[ERROR: This module was never named!]";

    /**
     * Fields that can be recursively updated in the Builder. <br>
     * You can change the recursion flags for these fields (see {@link DirectedGraphNode.PropertyUpdateFlag}). <br>
     * <br>
     * To update the flag for a property on a builder, call {@link TUIModule.Builder#setPropertyUpdateFlag(Property, DirectedGraphNode.PropertyUpdateFlag)} <br>
     * You can also call {@link TUIModule.Builder#lockProperty(Property)} or {@link TUIModule.Builder#unlockProperty(Property)}. <br>
     *  <br>
     * Note: setting the ansi, scanner, print stream, or enabling ansi will automatically lock those property flags,
     * but setting application or merging ansi (appending/prepending) will not automatically lock the corresponding flags.
     *
     */
    public enum Property {
        APPLICATION,
        /** Deals with replacing the ansi completely with setAnsi() **/
        SET_ANSI,
        /** Deals with appending to or prepending to the ansi */
        MERGE_ANSI,
        SCANNER,
        PRINTSTREAM,
        ENABLE_ANSI,
        LOGGER
    }

    /**
     * <p>The identifier for this module.</p>
     * <p>It is recommended to try and keep this unique in order to allow identification methods (e.g., via {@link TUIApplicationModule#getInput}) to function properly.</p>
     */
    private final String name;

    /**
     * The application the module is a child of.
     */
    private final TUIApplicationModule application;

    /**
     * Every child module that should be run.
     */
    private final List<TUIModule.Builder<?>> children;

    /**
     * <p>The ansi that may be displayed (Jansi object)</p>
     * <p>Currently, only TUITextModule displays the ansi</p>
     */
    private final Ansi ansi;

    /**
     * <p>The Scanner that reads input from the user-defined source</p>
     * <p>Is set to System.in by default (defined as SYSTEM_IN in TUIModule) </p>
     */
    private final Scanner scanner;

    /**
     * <p>The PrintStream that outputs data to the user-defined location</p>
     * <p>Is set to System.out by default</p>
     */
    private final PrintStream printStream;

    /**
     * <p>Whether ansi will be displayed or not.</p>
     */
    private final boolean enableAnsi;

    /** The Logger for the module, provided by the slf4j facade **/
    protected static final Logger logger = LoggerFactory.getLogger(TUIModule.class);

    /**
     * If there is a child currently running while {@link TUIModule#run() } is active, this will reference that child.
     */
    private TUIModule currentRunningChild;

    /**
     * Whether this module is currently terminated. <br>
     * If a module is terminated, it will stop running its children. <br>
     * Running a module will automatically cause it to be no longer terminated.
     */
    protected boolean terminated = false;

    /**
     * How deep in the recursion to go on toString()
     */
    public int MAX_ITERATIONS_ON_TO_STRING = 6;


    /**
     * Sets terminated to false, then linearly runs children. <br>
     * If there is a child currently running, you can access it via {@link TUIModule#getCurrentRunningChild()}. <br>
     */
    public void run() {
        logger.debug("Running children for module \"{}\"", name);
        terminated = false;

        for(TUIModule.Builder<?> child : children) {
            if(terminated) break;
            TUIModule toRun = child.build();
            currentRunningChild = toRun;
            toRun.run();
            currentRunningChild = null;
        }
    }

    /**
     * Runs the module and updates {@link TUIModule#currentRunningChild} to be {@code module}.
     * @param module The module to run as the child of this module.
     */
    public void runModuleAsChild(TUIModule.Builder<?> module) {
        logger.debug("Running module \"{}\" as child", module.name);
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

    /**
     * @return {@link TUIModule#name}
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@link TUIModule#children}
     */
    public List<TUIModule.Builder<?>> getChildren() {
        return children;
    }

    /**
     * Recursively searches for a child and returns it if it exists.
     *
     * @param name The name of the child to search for.
     * @return The child, if it exists.
     */
    public TUIModule.Builder<?> getChild(String name) {
        for(TUIModule.Builder<?> child : children) {
            TUIModule.Builder<?> returned = child.getChild(name);
            if(returned != null) return returned;
        }

        return null;
    }

    /**
     * Recursively searches for a child and returns it as T, if it exists as type T.
     *
     * @param name The name of the child to search for.
     * @param type The type of the child to search for <br> (e.g., {@code TUIContainerModule.Builder.class})
     * @return The child as T, if it exists.
     * @param <T> The type of the child
     */
    public <T extends TUIModule.Builder<?>> T getChild(String name, Class<T> type) {
        TUIModule.Builder<?> child = getChild(name);
        if(child == null) return null;
        if(child.getClass() == type) {
            return type.cast(child);
        }
        else return null;
    }

    /**
     * Stop this module from running any more children. <br>
     * Recursively propagates the termination to its children. <br>
     * If a terminated module is run again, it will no longer be terminated.
     */
    public void terminate() {
        this.terminated = true;
        if(this.currentRunningChild != null) currentRunningChild.terminate();
    }

    /**
     * Terminates a currently running child of this module (see {@link TUIModule#terminate}). <br>
     * All children higher up in the running branch will not be terminated. <br>
     *
     * @param moduleName The name of the child to terminate.
     */
    public void terminateChild(String moduleName) {
        getCurrentRunningBranch().forEach(m -> {
            if(m.getName().equals(moduleName)) m.terminate();
        });
    }

    /**
     * @return Whether this module is {@link TUIModule#terminated}
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @return The {@link TUIModule#currentRunningChild}. Will be null if there is no currently running child.
     */
    public TUIModule getCurrentRunningChild() {
        return currentRunningChild;
    }

    /**
     * Searches for a child in the current running branch and returns it, if it exists. <br>
     * (see {@link TUIModule#getCurrentRunningBranch()})
     *
     * @param name The name of the child to search for
     * @return The matching child in the current running branch, or null if not found.
     */
    public TUIModule getCurrentRunningChild(String name) {
        List<TUIModule> branch = getCurrentRunningBranch();

        for(TUIModule m : branch) {
            if(m.getName().equals(name)) return m;
        }

        return null;
    }

    /**
     * When a module is running it runs its children, and its currently running child
     * runs its children, and that child's currently running child runs its children, etc. <br>
     * This creates a "branch" of children that are currently running, stemming from this module
     * to the leaf child at the very end. <br>
     * Note: the leaf child may also have children but simply isn't running them yet.
     *
     * @return The current running branch of modules stemming from this module.
     */
    public List<TUIModule> getCurrentRunningBranch() {
        List<TUIModule> currentRunningBranch = new ArrayList<>();
        currentRunningBranch.add(this);

        if(currentRunningChild != null) {
            currentRunningBranch.addAll(currentRunningChild.getCurrentRunningBranch());
        }

        return currentRunningBranch;
    }

    /**
     * Some modules can do interesting things if it is tied to an application. <br>
     * For example, you can set a {@link TUITextModule} to display the output of another module,
     * and it will look for that output in the application's input map.
     *
     * @return The {@link TUIApplicationModule} that this module is tied to.
     */
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

        protected static final Logger logger = LoggerFactory.getLogger(Builder.class);

        protected static final Map<String, Integer> usedNames = new HashMap<>();

        public Builder(Class<B> type, String name) {
            this.type = type;
            this.setName(name);
            for(Property property : Property.values()) {
                propertyUpdateFlags.put(property, PropertyUpdateFlag.UPDATE);
            }
        }

        protected Builder(Class<B> type) {
            this.type = type;
        }

        /*
            Ideally, this complex copying logic could be abstracted into DirectedGraphNode.
            But, due to the limitations of Java generics,
            it becomes more difficult on the concrete implementations if we want to return copied
            objects as type B.
         */

        protected B deepCopy(B original, Map<Builder<?>, Builder<?>> visited) {
            if(visited.get(original) != null) return original.getType().cast(visited.get(original));
            visited.put(original, this);

            shallowCopy(type.cast(original));

            for(Builder<?> child : original.getChildren()) {
                Builder<?> newChild = child.createInstance();
                getChildren().add(Builder.deepCopyHelper(child, newChild, visited));
            }

            return self();
        }

        private static <T extends Builder<T>> Builder<T> deepCopyHelper(Builder<T> original, Builder<?> copyInto, Map<Builder<?>, Builder<?>> visited) {
            Builder<T> toCopy = original.getType().cast(copyInto);
            return toCopy.deepCopy(original.self(), visited);
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
            logger.trace("get a deep copy of module \"{}\"", name);
            return copy;
        }

        /**
         * Gets a fresh instance of this type of Builder.
         *  Note, this is intended only for copying utility and may have unknown consequences if used in other ways.
         * @return A fresh, empty instance.
         */
        protected abstract B createInstance();

        protected void shallowCopy(B original) {
            logger.trace("get a shallow copy of module \"{}\"", name);

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
            logger.debug("locking property \"{}\" for \"{}\"", property.name(), name);
            propertyUpdateFlags.put(property, PropertyUpdateFlag.HALT);

            return self();
        }

        public B unlockProperty(Property property) {
            logger.debug("unlocking property \"{}\" for module \"{}\"", property.name(), name);
            propertyUpdateFlags.put(property, PropertyUpdateFlag.UPDATE);

            return self();
        }

        public B updateProperties(TUIModule module) {
            logger.debug("updating properties for module \"{}\" based on module \"{}\"", name, module.name);
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
            logger.debug("adding child \"{}\" to module \"{}\"", child.name, name);
            this.children.add(child);
            return self();
        }

        public B addChild(int index, TUIModule.Builder<?> child) {
            logger.debug("adding child \"{}\" to module \"{}\" at index \"{}\"", child.name, name, index);
            this.children.add(index, child);
            return self();
        }

        public Builder<B> clearChildren() {
            logger.debug("clearing children of module \"{}\"", name);
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
            logger.debug("setting name for module \"{}\" to \"{}\"", this.name, name);

            usedNames.putIfAbsent(name, 0);
            if(usedNames.get(name) != 0 && !name.equals(this.name)) logger.warn("Builders with duplicate name detected: \"{}\"", name);
            if(this.name != null) usedNames.put(this.name, usedNames.get(this.name) - 1);
            this.name = name;
            usedNames.put(this.name, usedNames.get(this.name) + 1);

            return self();
        }

        public void prependToName(String name) {
            logger.debug("prepending \"{}\" to the name of module \"{}\" to become \"{}\"", name, this.name, name + "-" + this.name);
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
            logger.debug("setting app for module \"{}\" to \"{}\"", name, (app == null) ? "null" : app.getName());
            this.updateProperty(Property.APPLICATION, n -> n.setApplicationNonRecursive(app));

            return self();
        }

        private B setApplicationNonRecursive(TUIApplicationModule app) {
            logger.trace("setting app for module \"{}\" to \"{}\"", name, (app == null) ? "null" : app.getName());
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
            logger.debug("setting ansi for \"{}\"", name);
            this.updateProperty(Property.SET_ANSI, n -> {
                logger.trace("setting ansi for \"{}\"", n.name);
                n.ansi = ansi;
            });
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
            logger.debug("prepending ansi to module \"{}\"", name);
            this.updateProperty(Property.MERGE_ANSI, n -> {
                logger.trace("prepending ansi to module \"{}\"", n.name);
                n.ansi = ansi().a(ansi).a(n.ansi);
            });

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
            logger.debug("appending ansi to module \"{}\"", name);
            this.updateProperty(Property.MERGE_ANSI, n -> {
                logger.trace("appending ansi to module \"{}\"", n.name);
                n.ansi = ansi().a(n.ansi).a(ansi);
            });

            return self();
        }

        public B setScanner(Scanner scanner) {
            logger.debug("setting scanner for module \"{}\"", name);
            this.updateProperty(TUIModule.Property.SCANNER, n -> {
                logger.trace("setting scanner for module \"{}\"", n.name);
                n.scanner = scanner;
            });
            this.lockProperty(Property.SCANNER);

            return self();
        }

        public B setPrintStream(PrintStream printStream) {
            logger.debug("setting print stream for module \"{}\"", name);
            this.updateProperty(TUIModule.Property.PRINTSTREAM, n -> {
                logger.trace("setting print stream for module \"{}\"", n.name);
                n.printStream = printStream;
            });
            this.lockProperty(Property.PRINTSTREAM);

            return self();
        }

        public B enableAnsi(boolean enable) {
            logger.debug("setting ansi enabled for module \"{}\" to {}", name, enable);
            this.updateProperty(Property.ENABLE_ANSI, n -> {
                logger.trace("setting ansi enabled for module \"{}\" to {}", n.name, enable);
                n.enableAnsi = enable;
            });
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
                    Objects.equals(first.propertyUpdateFlags, second.propertyUpdateFlags) &&
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
        public B deepCopy(B original, Map<TUIModule.Builder<?>, TUIModule.Builder<?>> visited) {
            super.deepCopy(original, visited);
            main = (TUIContainerModule.Builder) visited.get(original.main);
            return self();
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
