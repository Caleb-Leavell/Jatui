package com.calebleavell.jatui.modules;

import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * <p>TUIGenericModule provides an abstract implementation of TUIModule.</p>
 * <p>TUIContainerModule provides a basic implementation of this class.</p>
 */
public abstract class TUIGenericModule implements TUIModule {

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
     * If the for loop is run() is currently active, this will be the child that is currently running.
     */
    private TUIModule currentRunningChild;

    private boolean terminated = false;

    public int MAX_ITERATIONS_ON_TO_STRING = 6;

    @Override
    public void run() {
        terminated = false;

        //List<TUIModule.Builder<?>> childrenIterable = new ArrayList<>(children);

        for(TUIModule.Builder<?> child : children) {
            if(terminated) break;
            TUIModule toRun = child.build();
            currentRunningChild = toRun;
            toRun.run();
            currentRunningChild = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TUIModule.Builder<?>> getChildren() {
        return children;
    }

    @Override
    public TUIModule.Builder<?> getChild(String name) {
        for(TUIModule.Builder<?> child : children) {
            TUIModule.Builder<?> returned = child.getChild(name);
            if(returned != null) return returned;
        }

        return null;
    }

    @Override
    public <T extends TUIModule.Builder<?>> T getChild(String name, Class<T> type) {
        TUIModule.Builder<?> child = getChild(name);
        if(child.getClass() == type) {
            return type.cast(child);
        }
        else return null;
    }


    @Override
    public TUIApplicationModule getApplication() {
        return application;
    }

    @Override
    public void setChildrenApplication(TUIApplicationModule app) {
        for(TUIModule.Builder<?> child : children) child.application(app);
    }

    @Override
    public void terminate() {
        if(this.terminated) return;
        this.terminated = true;
        if(this.currentRunningChild != null) currentRunningChild.terminate();
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public TUIModule getCurrentRunningChild() {
        return currentRunningChild;
    }

    @Override
    public TUIModule getCurrentRunningChild(String name) {
        List<TUIModule> branch = getCurrentRunningBranch();

        for(TUIModule m : branch) {
            if(m.getName().equals(name)) return m;
        }

        return null;
    }

    @Override
    public List<TUIModule> getCurrentRunningBranch() {
        List<TUIModule> currentRunningBranch = new ArrayList<>();
        currentRunningBranch.add(this);

        if(currentRunningChild != null) {
            currentRunningBranch.addAll(currentRunningChild.getCurrentRunningBranch());
        }

        return currentRunningBranch;
    }

    @Override
    public Ansi getAnsi() {
        return this.ansi;
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
    @Override
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

    protected TUIGenericModule(Builder<?> builder) {
        this.name = builder.name;
        this.application = builder.application;
        this.children = new ArrayList<>(builder.children);
        this.ansi = builder.ansi;
    }

    public abstract static class Builder<B extends Builder<B>> implements TUIModule.Builder<Builder<B>> {
        protected String name;
        protected TUIApplicationModule application;
        protected List<TUIModule.Builder<?>> children = new ArrayList<>();
        protected TUIModule.Builder<?> parent;
        protected Ansi ansi = ansi();
        private boolean allowAnsiOverride = true;

        protected boolean alterChildNames = false;
        protected final Class<B> type;

        /**
         * <p>These are children that are only accessible via Builders, but all child operations are also performed on these.</p>
         * <p>What is done with protectedChildren is not defined here - all that is done in TUIGenericModule.Builder is performing child operations on them.</p>
         * <p>This means protectedChildren are NOT automatically run when running the built module.</p>
         */
        protected List<TUIModule.Builder<?>> protectedChildren = new ArrayList<>();


        public Builder(Class<B> type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public B application(TUIApplicationModule app) {
            applicationHelper(app, new ArrayList<>());

            return self();
        }

        @Override
        public List<TUIModule.Builder<?>> applicationHelper(TUIApplicationModule app, List<TUIModule.Builder<?>> visited) {

            if(visited.contains(this)) return new ArrayList<>();

            this.application = app;

            visited.add(this);

            for(TUIModule.Builder<?> child : children) {
                visited.addAll(child.applicationHelper(app, visited));
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                visited.addAll(child.applicationHelper(app, visited));
            }

            return visited;
        }

        @Override
        public B children(List<TUIModule.Builder<?>> children) {
            for(TUIModule.Builder<?> child : children) addChild(child);
            return self();
        }

        @Override
        public B children(TUIModule.Builder<?>... children) {
            for(TUIModule.Builder<?> child : children) addChild(child);
            return self();
        }

        @Override
        public B addChild(TUIModule.Builder<?> child) {
            this.children.add(child);
            if(alterChildNames) child.prependToName(name);
            return self();
        }

        @Override
        public B addChild(int index, TUIModule.Builder<?> child) {
            this.children.add(index, child);
            if(alterChildNames) child.prependToName(name);
            return self();
        }

        @Override
        public Builder<B> clearChildren() {
            this.children.clear();

            return self();
        }

        @Override
        public String getName() {
            return name;
        }

        /**
         * <p>Whether the name of this module will be automatically prepended to the beginning of each child's name. </p>
         * <p>Must be called before .children() or .addChild() is called to have any effect.</p>
         * <p>More testing is required, but it currently seems more convenient in general to keep this as the default (false).</p>
         *
         * @param alterChildNames If true, child names will be altered (false by default)
         * @return self
         */
        @Override
        public B alterChildNames(boolean alterChildNames) {
            this.alterChildNames = alterChildNames;
            return self();
        }

        /**
         * <p>Sets the ansi for the module (using Jansi).</p>
         * <p>Also sets the ansi for all children that are currently added .</p>
         * <p>The ansi value for this and its children can be overriden if setAnsi is called on a parent module, or if setAnsi is called again.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        @Override
        public B setAnsi(Ansi ansi) { // named setAnsi to avoid collision with Jansi's ansi()

            if(!allowAnsiOverride) return self();

            this.ansi = ansi;

            for(TUIModule.Builder<?> child : children) {
                child.setAnsi(ansi);
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                child.setAnsi(ansi);
            }

            return self();
        }

        /**
         * <p>Prepends the ansi to the module's ansi (using Jansi).</p>
         * <p>Also prepends the ansi for all children that are currently added .</p>
         * <p>The ansi value for this and its children can be overriden if setAnsi is called on a parent module, or if setAnsi is called again.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        @Override
        public B prependAnsi(Ansi ansi) {

            if(!allowAnsiOverride) return self();

            this.ansi = ansi().a(ansi).a(this.ansi);

            for(TUIModule.Builder<?> child : children) {
                child.prependAnsi(ansi);
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                child.prependAnsi(ansi);
            }

            return self();
        }

        /**
         * <p>Appends the ansi to the module's ansi (using Jansi).</p>
         * <p>Also appends the ansi for all children that are currently added .</p>
         * <p>The ansi value for this and its children can be overriden if setAnsi is called on a parent module, or if setAnsi is called again.</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        @Override
        public B appendAnsi(Ansi ansi) {

            if(!allowAnsiOverride) return self();

            this.ansi = ansi.a(this.ansi).a(ansi);

            for(TUIModule.Builder<?> child : children) {
                child.setAnsi(ansi);
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                child.setAnsi(ansi);
            }

            return self();
        }

        /**
         * <p>Permanently sets the ansi for the module (using Jansi). This value won't be overriden later.</p>
         * <p>Also permanently sets the ansi for all children that are currently added .</p>
         * <p>Note: ansi for TUITextModules is automatically reset after running </p>
         * @param ansi The Jansi provided Ansi object
         * @return self
         */
        @Override
        public B hardSetAnsi(Ansi ansi) { // named setAnsi to avoid collision with Jansi's ansi()

            if(!allowAnsiOverride) return self();

            this.ansi = ansi;
            this.allowAnsiOverride = false;

            for(TUIModule.Builder<?> child : children) {
                child.hardSetAnsi(ansi);
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                child.hardSetAnsi(ansi);
            }

            return self();
        }

        /**
         * <p>If true, setAnsi may override this module and it's children's ansi values</p>
         * <p>If false, setAni will be blocked for this module and it's children</p>
         * @param allowed - whether setAnsi can override this module and it's children's ansi values
         * @return self
         */
        @Override
        public B allowAnsiOverride(boolean allowed) {
            this.allowAnsiOverride = allowed;

            return self();
        }

        /**
         * Finds a child matching the name.
         * It is recommended to name all modules uniquely so this returns a unique module every time.
         * Also checks protected children.
         *
         * @param name The name of the child
         * @return The first found child (DFS), or <i>null</i> if none is found
         */
        @Override
        public TUIModule.Builder<?> getChild(String name) {
            List<TUIModule.Builder<?>> visited = new ArrayList<>();
            return getChildHelper(name, visited);
        }

        /**
         * Executes a DFS.
         * Helper used so we can keep track of the modules we've visited and thus support cycles
         *
         * @param name    The name of the child
         * @param visited The list of visited modules
         * @return The first found child (DFS), or <i>null</i> if none is found
         */
        @Override
        public TUIModule.Builder<?> getChildHelper(String name, List<TUIModule.Builder<?>> visited) {
            if(this.name.equals(name)) return this;
            visited.add(this);

            for(TUIModule.Builder<?> child : children) {
                if(visited.contains(child)) continue;

                TUIModule.Builder<?> found = child.getChildHelper(name, visited);
                if(found != null) return found;
            }

            for(TUIModule.Builder<?> child : protectedChildren) {
                if(visited.contains(child)) continue;

                TUIModule.Builder<?> found = child.getChildHelper(name, visited);
                if(found != null) return found;
            }

            return null;
        }

        @Override
        public TUIApplicationModule getApplication() {
            return this.application;
        }

        @Override
        public void prependToName(String name) {
            this.name = name + "-" + this.name;
        }

        @Override
        public B self() {
            return type.cast(this);
        }

        @Override
        public abstract TUIModule build();
    }

}
