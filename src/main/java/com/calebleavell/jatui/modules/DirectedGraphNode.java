package com.calebleavell.jatui.modules;

import java.util.*;
import java.util.function.BiFunction;
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
     *     <li><strong>HALT:</strong> Don't update this node and don't continue the recursion</li>
     * </ol>
     */
    enum PropertyUpdateFlag {
        /** Update this node and continue the recursion */
        UPDATE,

        /** Update this node but don't continue the recursion */
        UPDATE_THEN_HALT,

        /** Don't update this node but continue the recursion */
        SKIP,

        /** Don't update this node and don't continue the recursion **/
        HALT
    }

    /**
     * <p>Connections between nodes are established by giving a node "children". <br>
     * The direction goes from this node to the child nodes.</p>
     * @return The list of children of this node.
     * @implSpec Must return a non-null list (may be empty).
     */
    List<? extends T> getChildren();

    /**
     * <p>Get the map of the flag set for each property.</p>
     * @return The map that associates each property with its corresponding update flag.
     * @implSpec Every property that may be used should have a default flag
     * (e.g., {@link PropertyUpdateFlag#UPDATE}) present in this map. Implementations
     * must ensure that this map is non-null and contains a flag for every property
     * that will be passed to {@code updateProperty}.
     */
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
        T self = self();

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

    /**
     * <p>Updates this node based on the flag of a property. Utilizes the flag for each property to determine traversal.</p>
     * <p>Assuming all flags are set to UPDATE, traversal is depth-first.</p>
     * @param property The property to check the flag for.
     * @param updater The function that updates this node.
     * @param visited The set of nodes that have already been visited.
     */
    default void updateProperty(P property, Consumer<T> updater, Set<T> visited) {
        T self = self();

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

    /**
     * <p>Updates this node based on the flag of a property. Utilizes the flag for each property to determine traversal.</p>
     * <p>Assuming all flags are set to UPDATE, traversal is depth-first.</p>
     * @param property The property to check the flag for.
     * @param updater The function that updates this node.
     */
    default void updateProperty(P property, Consumer<T> updater) {
        Set<T> visited = new HashSet<>();
        updateProperty(property, updater, visited);
    }

    /**
     * <p>Checks for equality with another node based on equalityCriteria. The children are also checked recursively.</p>
     * @param other The other node to check.
     * @param equalityCriteria A BiFunction that returns whether two inputted nodes are equal.
     * @param visited The list of visited nodes (prevents infinite recursion).
     * @return Whether this node equals other based on equalityCriteria.
     */
    default boolean equals(T other, BiFunction<T, T, Boolean> equalityCriteria, Set<T> visited) {
        if(other == null) return false;
        if(this == other) return true;

        // enforce equivalent structure if there's cycles
        if(visited.contains(self()) && visited.contains(other)) return true;
        if(visited.contains(self()) || visited.contains(other)) return false;

        visited.add(self());
        visited.add(other);

        if(!equalityCriteria.apply(self(), other)) return false;

        List<? extends T> children = this.getChildren();
        List<? extends T> otherChildren = other.getChildren();

        if(children.size() != otherChildren.size()) return false;

        for(int i = 0; i < children.size(); i ++) {
            // ensure if one of the children are null, the corresponding child for other also is
            if(children.get(i) == null && otherChildren.get(i) == null) continue;
            if(children.get(i) == null || otherChildren.get(i) == null) return false;

            if(!children.get(i).equals(otherChildren.get(i), equalityCriteria, visited)) return false;
        }

        return true;
    }

    /**
     * <p>Checks for equality with another node based on equalityCriteria. The children are also checked recursively.</p>
     * @param other The other node to check.
     * @param equalityCriteria A BiFunction that returns whether two inputted nodes are equal.
     * @return Whether this node equals other based on equalityCriteria.
     */
    default boolean equals(T other, BiFunction<T, T, Boolean> equalityCriteria) {
        return equals(other, equalityCriteria, new HashSet<>());
    }

    @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
    default T self() {
        return (T) this;
    }
}
