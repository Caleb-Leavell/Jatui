package com.calebleavell.jatui.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DirectedGraphNode<T extends DirectedGraphNode<T>> {

    public List<? extends T> getChildren();

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     *
     * @param criteria A function that checks whether a child should be returned
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default T dfs(Function<T, Boolean> criteria) {
        List<T> visited = new ArrayList<>();
        return dfs(criteria, visited);
    }

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     *
     * @param criteria A function that checks whether a child should be returned
     * @param visited used so we can keep track of the modules we've visited and thus support cycles
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default T dfs(Function<T, Boolean> criteria, List<T> visited) {
        @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
        T self = (T) this;

        if(criteria.apply(self)) return self;

        for(T child : getChildren()) {
            if(visited.contains(child)) continue;
            if(criteria.apply(child)) return child;
            visited.add(child);
            T found = child.dfs(criteria, visited);

            if(found != null) return found;
        }

        return null;
    }

    /**
     * Executes the Consumer on self and every accessible node of the graph.
     * Cycles are supported.
     *
     * @param function The Consumer that accepts every accessible node.
     */
    default void forEach(Consumer<T> function) {
        this.dfs(t -> {
            function.accept(t);
            return false; // ensures the dfs won't terminate early
        });
    }

    /**
     * Executes the Consumer on every accessible node of the graph, excluding this one.
     * Cycles are supported.
     *
     * @param function The Consumer that accepts every accessible node.
     */
    default void forEachChild(Consumer<T> function) {
        this.dfs(t -> {
            if(t == this) return false;
            function.accept(t);
            return false; // ensures the dfs won't terminate early
        });
    }

}
