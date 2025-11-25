/*
 * Copyright (c) 2025 Caleb Leavell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.calebleavell.jatui.modules;

import java.util.*;
import java.util.function.*;

/**
 * Provides a mechanism for working with nodes in a directed graph. Uses a recursive structure to simplify attaching nodes as "children."
 * <br><br>
 * <strong>This class is not Thread-Safe.</strong>
 *
 * @param <P> Property Type
 * @param <A> Abstract Type that all nodes will have
 * @param <T> Specific Type of this node
 */
public interface DirectedGraphNode<P extends Enum<?>, A extends DirectedGraphNode<P, A, ?>, T extends DirectedGraphNode<P, A, T>> {

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
    List<A> getChildren();

    /**
     * <p>Get the map of the flag set for each property.</p>
     * @return The map that associates each property with its corresponding update flag.
     * @implSpec Every property that may be used should have a default flag
     * (e.g., {@link PropertyUpdateFlag#UPDATE}) present in this map. Implementations
     * must ensure that this map is non-null and contains a flag for every property
     * that will be passed to {@code updateProperty}.
     */
    Map<P, PropertyUpdateFlag> getPropertyUpdateFlags();

    /** Get the {@code Class<T>} type of the current Node **/
    Class<T> getType();

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     * Null Nodes are skipped.
     *
     * @param criteria A function that checks whether a child should be returned
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default A dfs(Function<A, Boolean> criteria) {
        Set<A> visited = new HashSet<>();
        return dfs(criteria, visited);
    }

    /**
     * Executes a DFS on self and all accessible children of the graph. Cycles are supported.
     *
     * @param criteria A function that checks whether a child should be returned
     * @param visited used so we can keep track of the modules we've visited and thus support cycles
     * @return The first found child (DFS), or <i><strong>null</strong></i> if none is found
     */
    default A dfs(Function<A, Boolean> criteria, Set<A> visited) {
        A self = abstractSelf();

        if(visited.contains(self)) return null;
        visited.add(self);

        if(criteria.apply(self)) return self;

        for(A child : getChildren()) {
            if(child == null) continue;

            A found = child.dfs(criteria, visited);

            if(found != null) return found;
        }

        return null;
    }

    /**
     * Executes the Consumer on self and every accessible node of the graph.
     * Cycles are supported. Null Nodes are skipped.
     *
     * @param function The Consumer that accepts every accessible node.
     */
    default void forEach(Consumer<A> function) {
        this.dfs(node -> {
            function.accept(node);
            return false; // ensures the dfs won't terminate early
        });
    }

    /**
     * Executes the Consumer on every accessible node of the graph, excluding this one.
     * Cycles are supported.
     *
     * <p><strong>Note:</strong> This node will not be updated even if cycled back to by another node.</p>
     *
     * @param function The Consumer that accepts every accessible node.
     */
    default void forEachChild(Consumer<A> function) {
        this.dfs(node -> {
            if(node == this) return false;
            function.accept(node);
            return false; // ensures the dfs won't terminate early
        });
    }

    /**
     * Traverses through every node accessible from this node and returns true if
     * one of the nodes is null. Cycles are supported.
     *
     * @return Whether a null node is accessible (directly or indirectly) from this node.
     */
    default boolean containsNullNode() {
        Set<A> visited = new HashSet<>();
        return containsNullNode(visited);
    }

    /**
     * Traverses through every node accessible from this node and returns true if
     * one of the nodes is null. Cycles are supported.
     *
     * @param visited used so we can keep track of the modules we've visited and thus support cycles
     * @return Whether a null node is accessible (directly or indirectly) from this node.
     */
    default boolean containsNullNode(Set<A> visited) {
        A self = abstractSelf();

        if(visited.contains(self)) return false;
        visited.add(self);

        for(A child : getChildren()) {
            if(child == null) return true;
            else if (child.containsNullNode(visited)) return true;
        }

        return false;
    }

    /**
     * <p>Updates this node based on the flag of a property. Utilizes the flag for each property to determine traversal.</p>
     * <p>Assuming all flags are set to UPDATE, traversal is depth-first.</p>
     * @param property The property to check the flag for.
     * @param updater The function that updates this node.
     * @param visited The set of nodes that have already been visited.
     */
    default void updateProperty(P property, Consumer<A> updater, Set<A> visited) {
        A self = abstractSelf();

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

        for(A child : getChildren()) {
            child.updateProperty(property, updater, visited);
        }
    }

    /**
     * <p>Updates this node based on the flag of a property. Utilizes the flag for each property to determine traversal.</p>
     * <p>Assuming all flags are set to UPDATE, traversal is depth-first.</p>
     * @param property The property to check the flag for.
     * @param updater The function that updates this node.
     */
    default void updateProperty(P property, Consumer<A> updater) {
        Set<A> visited = new HashSet<>();
        updateProperty(property, updater, visited);
    }

    /**
     *  @return Structural equality between {@code first} and {@code second}, as defined by<br>
     *  {@link DirectedGraphNode#equalTo(DirectedGraphNode)} and concrete implementations.
     */
    public boolean equalTo(T first, T second);

    /**
     * <p>Checks for equality with another node based on equalTo. The children are also checked recursively.</p>
     * @param other The other node to check.
     * @param visited The list of visited nodes (prevents infinite recursion).
     * @return Whether this node equals other based on equalityCriteria.
     */
    default boolean equalTo(A other, Set<A> visited) {
        if(other == null) return false;
        if(this == other) return true;

        A self = abstractSelf();

        // enforce equivalent structure if there's cycles
        if(visited.contains(self) && visited.contains(other)) return true;
        if(visited.contains(self) || visited.contains(other)) return false;

        visited.add(self);
        visited.add(other);

        if(other.getType() != getType()) return false;
        if(!equalTo(self(), getType().cast(other))) return false;

        List<? extends A> children = this.getChildren();
        List<? extends A> otherChildren = other.getChildren();

        if(children.size() != otherChildren.size()) return false;

        for(int i = 0; i < children.size(); i ++) {
            // ensure if one of the children are null, the corresponding child for other also is
            if(children.get(i) == null && otherChildren.get(i) == null) continue;
            if(children.get(i) == null || otherChildren.get(i) == null) return false;

            if(!children.get(i).equalTo(otherChildren.get(i), visited)) return false;

        }

        return true;
    }

    /**
     * <p>Checks for structural equality with another node based on equalityCriteria. The children are also checked recursively.</p>
     * @param other The other node to check. <strong>Must be the same type as this node to return true.</strong>
     * @return Whether this node equals other based on equalityCriteria.
     */
    default boolean equalTo(A other) {
        return equalTo(other, new HashSet<>());
    }

    @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
    default T self() {
        return (T) this;
    }

    @SuppressWarnings("unchecked") // safe to suppress since "this" will always be an instance of T
    default A abstractSelf() {
        return (A) this;
    }
}
