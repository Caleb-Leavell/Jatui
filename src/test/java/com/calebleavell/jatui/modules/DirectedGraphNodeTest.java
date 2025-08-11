package com.calebleavell.jatui.modules;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DirectedGraphNodeTest {

    private static class TestNode implements DirectedGraphNode<TestNode.Property, TestNode> {
        public enum Property {
            ID,
            DATA
        }

        List<TestNode> children;
        Map<Property, PropertyUpdateFlag> propertyUpdateFlags;

        String id;
        int data;

        public TestNode(String id, int data) {
            this.id = id;
            this.data = data;

            children = new ArrayList<>();
            propertyUpdateFlags = new HashMap<>();
            propertyUpdateFlags.put(Property.ID, PropertyUpdateFlag.UPDATE);
            propertyUpdateFlags.put(Property.DATA, PropertyUpdateFlag.UPDATE);
        }

        @Override
        public List<TestNode> getChildren() {
            return children;
        }

        @Override
        public Map<Property, PropertyUpdateFlag> getPropertyUpdateFlags() {
            return propertyUpdateFlags;
        }

        String getId() {
            return id;
        }

        int getData() {
            return data;
        }

    }

    @Test
    void testGetChildren() {
        TestNode node1 = new TestNode("one", 1);
        TestNode node2 = new TestNode("two", 2);
        TestNode node3 = new TestNode("three", 3);
        TestNode node4 = new TestNode("four", 4);

        node1.getChildren().addAll(List.of(node2, node3));
        node3.getChildren().add(node4);

        assertEquals(node1.getChildren(), List.of(node2, node3));
    }

    @Test
    void testGetUpdatePropertyFlags() {
        Map<TestNode.Property, DirectedGraphNode.PropertyUpdateFlag> expected = new HashMap<>();
        expected.put(TestNode.Property.ID, DirectedGraphNode.PropertyUpdateFlag.HALT);
        expected.put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.UPDATE);

        TestNode node = new TestNode("one", 1);
        node.getPropertyUpdateFlags().put(TestNode.Property.ID, DirectedGraphNode.PropertyUpdateFlag.HALT);

        assertEquals(node.getPropertyUpdateFlags(), expected);
    }

    @Test
    void testDfs_empty() {
        TestNode node = new TestNode("empty", 1);

        TestNode findSelf = node.dfs(n -> n == node);
        TestNode findOther = node.dfs(n -> n.getData() == 2);

        assertAll(
                () -> assertEquals(findSelf, node),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_self_loop() {
        TestNode node = new TestNode("empty", 1);
        node.getChildren().add(node);

        TestNode findSelf = node.dfs(n -> n == node);
        TestNode findOther = node.dfs(n -> n.getData() == 2);

        assertAll(
                () -> assertEquals(findSelf, node),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_with_child() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);

        root.getChildren().add(child);

        TestNode findRoot = root.dfs(n -> n == root);
        TestNode findChild = root.dfs(n -> n.getId().equals("child"));
        TestNode findOther = root.dfs(n -> n.getData() == 3);

        assertAll(
                () -> assertEquals(findRoot, root),
                () -> assertEquals(findChild, child),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_with_equivalent_children() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);
        TestNode childCopy = new TestNode("child", 2);

        root.getChildren().addAll(List.of(child, childCopy));

        TestNode findRoot = root.dfs(n -> n == root);
        TestNode findChild = root.dfs(n -> n.getId().equals("child"));
        TestNode findChildCopy = root.dfs(n -> n == childCopy);
        TestNode findOther = root.dfs(n -> n.getData() == 3);

        assertAll(
                () -> assertEquals(findRoot, root),
                () -> assertEquals(findChild, child),
                () -> assertEquals(findChildCopy, childCopy),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_branching() {
        TestNode node1 = new TestNode("one", 1);
        TestNode node2 = new TestNode("two", 2);
        TestNode node3 = new TestNode("three", 3);
        TestNode node4 = new TestNode("four", 4);
        TestNode node5 = new TestNode("five", 5);
        TestNode node6 = new TestNode("six", 6);
        TestNode node3Alternate = new TestNode("three-alternate", 3);

        node1.getChildren().add(node3);
        node3.getChildren().addAll(List.of(node4, node3Alternate));
        node4.getChildren().addAll(List.of(node1, node2));
        node5.getChildren().add(node1);

        TestNode foundOne = node1.dfs(node -> node.getData() == 1);
        TestNode foundTwo = node1.dfs(node -> node.getData() == 2);
        TestNode foundThree = node1.dfs(node -> node.getId().equals("three"));
        TestNode foundFour = node1.dfs(node -> node.getId().equals("four"));
        TestNode foundFive = node1.dfs(node -> node.getData() == 5);
        TestNode foundTwoAlternate = node5.dfs(node -> node == node2);

        assertAll(
                () -> assertEquals(foundOne, node1),
                () -> assertEquals(foundTwo, node2),
                () -> assertEquals(foundThree, node3),
                () -> assertEquals(foundFour, node4),
                () -> assertNull(foundFive),
                () -> assertEquals(foundTwoAlternate, node2)
        );
    }

    @Test
    void testForEach() {
    }

    @Test
    void testForEachChild() {
    }

    @Test
    void testUpdateProperty() {
    }

    @Test
    void testEquals() {
    }

    @Test
    void testSelf() {
    }
}