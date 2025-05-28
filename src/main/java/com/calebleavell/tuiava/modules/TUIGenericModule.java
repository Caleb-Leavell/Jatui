package com.calebleavell.tuiava.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public abstract class TUIGenericModule implements TUIModule {

    private String name;
    private TUIApplicationModule application;
    private List<TUIModule> children;

    private boolean terminated = false;

    public int MAX_ITERATIONS_ON_TO_STRING = 6;


    @Override
    public void run() {
        terminated = false;
        for(TUIModule child : children) {
            if(terminated) return;
            child.run();
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
    public List<TUIModule> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<TUIModule> children) {
        this.children = children;
    }

    /**
     * Finds a child matching the name.
     * It is recommended to name all modules uniquely so this returns a unique module every time.
     *
     * @param name The name of the child
     * @return The first found child (DFS), or <i>null</i> if none is found
     */
    @Override
    public TUIModule getChild(String name) {
        List<TUIModule> visited = new ArrayList<>();
        return getChildHelper(name, visited);
    }

    /**
     * Executes a DFS.
     * Helper used so we can keep track of the modules we've visited and thus support cycles
     *
     * @param name The name of the child
     * @param visited The list of visited modules
     * @return The first found child (DFS), or <i>null</i> if none is found
     */
    public TUIModule getChildHelper(String name, List<TUIModule> visited) {
        if(this.name.equals(name)) return this;
        visited.add(this);

        for(TUIModule child : children) {
            if(visited.contains(child)) continue;

            TUIModule found = child.getChildHelper(name, visited);
            if(found != null) return found;
        }

        return null;
    }

    @Override
    public TUIApplicationModule getApplication() {
        return application;
    }

    @Override
    public void setApplication(TUIApplicationModule application) {
        this.application = application;
    }

    @Override
    public void setChildrenApplication(TUIApplicationModule app) {
        for(TUIModule child : children) child.setApplicationRecursive(app);
    }

    @Override
    public void setApplicationRecursive(TUIApplicationModule app) {
        if(this.application == null) this.application = app;

        for(TUIModule child : children) child.setApplicationRecursive(app);
    }



    @Override
    public void terminate() {
        if(this.terminated) return;
        this.terminated = true;
        children.forEach(TUIModule::terminate);
    }

    @Override
    public boolean isTerminated() {
        return terminated;
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
            for (TUIModule child : children) {
                output.append("\n").append(child.toString(indent + 1, true));
            }
        }

        return output.toString();
    }

    protected TUIGenericModule(Builder<?> builder) {
        this.name = builder.name;
        this.application = builder.application;
        this.children = builder.children;
    }

    public abstract static class Builder<B extends Builder<B>> {
        protected String name;
        protected TUIApplicationModule application;
        protected List<TUIModule> children = new ArrayList<>();
        protected Class<B> type;

        public Builder(Class<B> type, String name) {
            this.type = type;
            this.name = name;
        }

        public B application(TUIApplicationModule application) {
            this.application = application;
            return self();
        }

        public B children(List<TUIModule> children) {
            for(TUIModule child : children) addChild(child);
            return self();
        }

        public B children(TUIModule... children) {
            for(TUIModule child : children) addChild(child);
            return self();
        }

        public B children(Builder<?>... children) {
            for(Builder<?> child : children) addChild(child.build());
            return self();
        }

        public B addChild(TUIModule child) {
            this.children.add(child);
            return self();
        }

        public B addChild(int index, TUIModule child) {
            this.children.add(index, child);
            return self();
        }

        public B addChild(Builder<?> child) {
            return addChild(child.build());
        }

        public B addChild(int index, Builder<?> child) {
            return addChild(index, child.build());
        }

        public B self() {
            return type.cast(this);
        }

        public abstract TUIModule build();
    }

}
