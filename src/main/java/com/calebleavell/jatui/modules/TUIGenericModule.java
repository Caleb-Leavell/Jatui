package com.calebleavell.jatui.modules;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public abstract class TUIGenericModule implements TUIModule {

    private String name;
    private TUIApplicationModule application;
    private List<TUIModule.Builder<?>> children;

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
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<TUIModule.Builder<?>> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<TUIModule.Builder<?>> children) {
        this.children = children;
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
    }

    public abstract static class Builder<B extends Builder<B>> implements TUIModule.Builder<Builder<B>> {
        protected String name;
        protected TUIApplicationModule application;
        protected List<TUIModule.Builder<?>> children = new ArrayList<>();
        protected TUIModule.Builder<?> parent;

        protected boolean alterChildNames = false;
        protected final Class<B> type;


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
        public String getName() {
            return name;
        }

        /**
         * <p>Whether the name of this module will be automatically prepended to the beginning of each child's name. </p>
         * <p>Must be called before .children() or .addChild() is called to have any effect.</p>
         *
         * @param uniqueName If true, child names will be altered
         * @return self
         */
        @Override
        public B alterChildNames(boolean uniqueName) {
            this.alterChildNames = true;
            return self();
        }

        /**
         * Finds a child matching the name.
         * It is recommended to name all modules uniquely so this returns a unique module every time.
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
