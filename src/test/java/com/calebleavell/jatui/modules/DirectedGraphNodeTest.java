package com.calebleavell.jatui.modules;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.awt.desktop.AppReopenedEvent;
import java.io.File;
import java.sql.Array;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DirectedGraphNodeTest {

    private static class TestNode implements DirectedGraphNode<TestNode.Property, TestNode, TestNode> {
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

        @Override
        public Class<TestNode> getType() {
            return TestNode.class;
        }

        @Override
        public boolean equalTo(TestNode first, TestNode second) {
            if(first == second) return true;
            if(first == null || second == null) return false;

            return  Objects.equals(first.id, second.id) &&
                    Objects.equals(first.data, second.data);
        }

        String getId() {
            return id;
        }

        int getData() {
            return data;
        }

        void setData(int data) {
            this.data = data;
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

        assertEquals(List.of(node2, node3), node1.getChildren());
    }

    @Test
    void testGetUpdatePropertyFlags() {
        Map<TestNode.Property, DirectedGraphNode.PropertyUpdateFlag> expected = new HashMap<>();
        expected.put(TestNode.Property.ID, DirectedGraphNode.PropertyUpdateFlag.HALT);
        expected.put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.UPDATE);

        TestNode node = new TestNode("one", 1);
        node.getPropertyUpdateFlags().put(TestNode.Property.ID, DirectedGraphNode.PropertyUpdateFlag.HALT);

        assertEquals(expected, node.getPropertyUpdateFlags());
    }

    @Test
    void testDfs_empty() {
        TestNode node = new TestNode("empty", 1);

        TestNode findSelf = node.dfs(n -> n == node);
        TestNode findOther = node.dfs(n -> n.getData() == 2);

        assertAll(
                () -> assertEquals(node, findSelf),
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
                () -> assertEquals(node, findSelf),
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
                () -> assertEquals(root, findRoot),
                () -> assertEquals(child, findChild),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_equivalent_children() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);
        TestNode childCopy = new TestNode("child", 2);

        root.getChildren().addAll(List.of(child, childCopy));

        TestNode findRoot = root.dfs(n -> n == root);
        TestNode findChild = root.dfs(n -> n.getId().equals("child"));
        TestNode findChildCopy = root.dfs(n -> n == childCopy);
        TestNode findOther = root.dfs(n -> n.getData() == 3);

        assertAll(
                () -> assertEquals(root, findRoot),
                () -> assertEquals(child, findChild),
                () -> assertEquals(childCopy, findChildCopy),
                () -> assertNull(findOther)
        );
    }

    @Test
    void testDfs_multiple_paths() {
        TestNode root = new TestNode("root", 1);
        TestNode child1 = new TestNode("child1", 2);
        TestNode child2 = new TestNode("child2", 3);
        TestNode child3 = new TestNode("child3", 4);

        root.getChildren().addAll(List.of(child1, child2));
        child1.getChildren().add(child3);
        child2.getChildren().add(child3);

        TestNode found = root.dfs(n -> n == child3);

        assertEquals(child3, found);
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
                () -> assertEquals(node1, foundOne),
                () -> assertEquals(node2, foundTwo),
                () -> assertEquals(node3, foundThree),
                () -> assertEquals(node4, foundFour),
                () -> assertNull(foundFive),
                () -> assertEquals(node2, foundTwoAlternate)
        );
    }

    // for each

    @Test
    void testForEach_empty() {
        TestNode node = new TestNode("node", 1);

        node.forEach(n -> n.setData(n.getData() + 1));

        assertEquals(2, node.getData());
    }

    @Test
    void testForEach_self_loop() {
        TestNode node = new TestNode("node", 1);
        node.getChildren().add(node);

        node.forEach(n -> n.setData(n.getData() + 1));

        assertEquals(2, node.getData());
    }

    @Test
    void testForEach_with_child() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);

        root.getChildren().add(child);

        root.forEach(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(3, child.getData())
        );
    }

    @Test
    void testForEach_equivalent_children() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);
        TestNode childCopy = new TestNode("child", 2);

        root.getChildren().addAll(List.of(child, childCopy));

        root.forEach(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(3, child.getData()),
                () -> assertEquals(3, childCopy.getData())
        );
    }

    @Test
    void testForEach_multiple_paths() {
        TestNode root = new TestNode("root", 1);
        TestNode child1 = new TestNode("child1", 2);
        TestNode child2 = new TestNode("child2", 3);
        TestNode child3 = new TestNode("child3", 4);

        root.getChildren().addAll(List.of(child1, child2));
        child1.getChildren().add(child3);
        child2.getChildren().add(child3);

        root.forEach(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(3, child1.getData()),
                () -> assertEquals(4, child2.getData()),
                () -> assertEquals(5, child3.getData())
        );
    }

    @Test
    void testForEach_branching() {
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

        node1.forEach(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, node1.getData()),
                () -> assertEquals(3, node2.getData()),
                () -> assertEquals(4, node3.getData()),
                () -> assertEquals(5, node4.getData()),
                () -> assertEquals(5, node5.getData()),
                () -> assertEquals(6, node6.getData())
        );
    }



    @Test
    void testForEachChild_empty() {
        TestNode node = new TestNode("node", 1);

        node.forEachChild(n -> n.setData(n.getData() + 1));

        assertEquals(1, node.getData());
    }

    @Test
    void testForEachChild_self_loop() {
        TestNode node = new TestNode("node", 1);
        node.getChildren().add(node);

        node.forEachChild(n -> n.setData(n.getData() + 1));

        assertEquals(1, node.getData());
    }

    @Test
    void testForEachChild_with_child() {
        TestNode root = new TestNode("root", 1);
        TestNode child = new TestNode("child", 2);

        root.getChildren().add(child);

        root.forEachChild(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(1, root.getData()),
                () -> assertEquals(3, child.getData())
        );
    }

    @Test
    void testForEachChild_branching() {
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

        node1.forEachChild(n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(1, node1.getData()),
                () -> assertEquals(3, node2.getData()),
                () -> assertEquals(4, node3.getData()),
                () -> assertEquals(5, node4.getData()),
                () -> assertEquals(5, node5.getData()),
                () -> assertEquals(6, node6.getData())
        );
    }

    @Test
    void testUpdateProperty_Update() {
        TestNode root = new TestNode("one", 1);
        TestNode child1 = new TestNode("two", 2);
        TestNode child2 = new TestNode("three", 3);

        root.getChildren().add(child1);
        child1.getChildren().add(child2);

        root.updateProperty(TestNode.Property.DATA, n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(3, child1.getData()),
                () -> assertEquals(4, child2.getData())
        );
    }

    @Test
    void testUpdateProperty_UpdateThenHalt() {
        TestNode root = new TestNode("one", 1);
        TestNode child1 = new TestNode("two", 2);
        TestNode child2 = new TestNode("three", 3);

        root.getChildren().add(child1);
        child1.getChildren().add(child2);
        child1.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

        root.updateProperty(TestNode.Property.DATA, n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(3, child1.getData()),
                () -> assertEquals(3, child2.getData())
        );
    }

    @Test
    void testUpdateProperty_Skip() {
        TestNode root = new TestNode("one", 1);
        TestNode child1 = new TestNode("two", 2);
        TestNode child2 = new TestNode("three", 3);

        root.getChildren().add(child1);
        child1.getChildren().add(child2);
        child1.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.SKIP);

        root.updateProperty(TestNode.Property.DATA, n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(2, child1.getData()),
                () -> assertEquals(4, child2.getData())
        );
    }

    @Test
    void testUpdateProperty_Halt() {
        TestNode root = new TestNode("one", 1);
        TestNode child1 = new TestNode("two", 2);
        TestNode child2 = new TestNode("three", 3);

        root.getChildren().add(child1);
        child1.getChildren().add(child2);
        child1.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.HALT);

        root.updateProperty(TestNode.Property.DATA, n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, root.getData()),
                () -> assertEquals(2, child1.getData()),
                () -> assertEquals(3, child2.getData())
        );
    }

    @Test
    void testUpdateProperty_branching() {
        TestNode node1 = new TestNode("one", 1);
        TestNode node2 = new TestNode("two", 2);
        TestNode node3 = new TestNode("three", 3);
        TestNode node4 = new TestNode("four", 4);
        TestNode node5 = new TestNode("five", 5);
        TestNode node6 = new TestNode("six", 6);
        TestNode node7 = new TestNode("seven", 7);
        TestNode node3Alternate = new TestNode("three-alternate", 3);

        node1.getChildren().add(node3);
        node2.getChildren().add(node7);
        node3.getChildren().addAll(List.of(node4, node3Alternate));
        node4.getChildren().addAll(List.of(node1, node2));
        node5.getChildren().add(node1);

        node3.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.SKIP);
        node2.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

        node1.updateProperty(TestNode.Property.DATA, n -> n.setData(n.getData() + 1));

        assertAll(
                () -> assertEquals(2, node1.getData()),
                () -> assertEquals(3, node2.getData()),
                () -> assertEquals(3, node3.getData()),
                () -> assertEquals(5, node4.getData()),
                () -> assertEquals(5, node5.getData()),
                () -> assertEquals(6, node6.getData()),
                () -> assertEquals(7, node7.getData())

        );
    }

    // Note: this test is not very thorough since equals matters more for TUIModule
    @Test
    void testEquals() {
        TestNode node1 = new TestNode("one", 1);
        TestNode node1Copy = new TestNode("one", 1);
        TestNode node2 = new TestNode("two", 2);
        TestNode node3 = new TestNode("three", 3);
        TestNode node4 = new TestNode("four", 4);
        TestNode node5 = new TestNode("five", 5);
        TestNode node6 = new TestNode("six", 6);
        TestNode node7 = new TestNode("seven", 7);
        TestNode node3Alternate = new TestNode("three-alternate", 3);

        node1.getChildren().add(node3);
        node1Copy.getChildren().add(node3);
        node2.getChildren().add(node7);
        node3.getChildren().addAll(List.of(node4, node3Alternate, node1));
        node4.getChildren().add(node2);
        node5.getChildren().add(node4);

        node3.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.SKIP);
        node2.getPropertyUpdateFlags().put(TestNode.Property.DATA, DirectedGraphNode.PropertyUpdateFlag.UPDATE_THEN_HALT);

        assertAll(
                () -> assertTrue(node1.equals(node1)),
                () -> assertTrue(node1.equals(node1Copy)),
                () -> assertFalse(node1.equals(node2))
        );

        node1Copy.setData(5);

        assertFalse(node1.equals(node1Copy));
    }
}