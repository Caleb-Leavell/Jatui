package com.calebleavell.jatui.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface DirectedGraph<T extends DirectedGraph<T>> {

    public List<? extends T> getChildren();

    /**
     * Finds a child matching the name.
     * It is recommended to name all modules uniquely so this returns a unique module every time.
     * Also checks protected children.
     *
     * @param criteria A function that checks whether a child should be returned
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default T dfs(Function<T, Boolean> criteria) {
        List<T> visited = new ArrayList<>();
        return dfs(criteria, visited);
    }

    /**
     * Executes a DFS.
     * Helper used so we can keep track of the modules we've visited and thus support cycles
     *
     * @param criteria A function that checks whether a child should be returned
     * @param visited The list of visited modules
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

}
