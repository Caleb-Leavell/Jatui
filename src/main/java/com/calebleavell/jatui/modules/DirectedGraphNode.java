package com.calebleavell.jatui.modules;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DirectedGraphNode<P extends Enum<?>, T extends DirectedGraphNode<P, T>> {

    /**
     * <p>Specifies behavior for updating a property of this node. <br>
     * When a recursive call to update a property is called, the flag for the property is checked.</p>
     * <p>The flags are:</p>
     * <ol>
     *     <li><strong>UPDATE:</strong> Update this node and continue the recursion</li>
     *     <li><strong>UPDATE_THEN_HALT:</strong> Update this node but don't continue the recursion</li>
     *     <li><strong>SKIP:</strong> Don't update this node but continue the recursion</li>
     *     <li><strong>HALT:</strong> Don't update this node but don't continue the recursion</li>
     * </ol>
     */
    enum PropertyUpdateFlag {
        /** Update this node and continue the recursion */
        UPDATE,
        /** Update this node but don't continue the recursion */
        UPDATE_THEN_HALT,
        /** Don't update this node but continue the recursion */
        SKIP,
        /** Don't update this node but don't continue the recursion **/
        HALT
    }

    /**
     * <p>Connections between nodes are established by giving a node "children". <br>
     * The direction goes from this node to the child nodes.</p>
     * @return The list of children of this node.
     */
    List<? extends T> getChildren();

    Map<P, PropertyUpdateFlag> getPropertyUpdateFlags();

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     *
     * @param criteria A function that checks whether a child should be returned
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default T dfs(Function<T, Boolean> criteria) {
        Set<T> visited = new HashSet<>();
        return dfs(criteria, visited);
    }

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     *
     * @param criteria A function that checks whether a child should be returned
     * @param visited used so we can keep track of the modules we've visited and thus support cycles
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default T dfs(Function<T, Boolean> criteria, Set<T> visited) {
        @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
        T self = (T) this;

        if(criteria.apply(self)) return self;

        for(T child : getChildren()) {
            if(visited.contains(child)) continue;
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
        this.dfs(node -> {
            function.accept(node);
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
        this.dfs(node -> {
            if(node == this) return false;
            function.accept(node);
            return false; // ensures the dfs won't terminate early
        });
    }

    default void updateProperty(P property, Consumer<T> updater, Set<T> visited) {
        @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
        T self = (T) this;

        if(visited.contains(self)) return;
        visited.add(self);

        PropertyUpdateFlag flag = this.getPropertyUpdateFlags().get(property);
        switch(flag) {
            case UPDATE -> {
                updater.accept(self);
                break;
            }
            case UPDATE_THEN_HALT -> {
                updater.accept(self);
                return;
            }
            case SKIP -> {
                break; // do nothing
            }
            case HALT -> {
                return;
            }
        }


        for(T child : getChildren()) {
            child.updateProperty(property, updater, visited);
        }
    }

    default void updateProperty(P property, Consumer<T> updater) {
        Set<T> visited = new HashSet<>();
        updateProperty(property, updater, visited);
    }

}
