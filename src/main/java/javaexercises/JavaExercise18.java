package javaexercises;

import org.slf4j.Logger;

import simplebuilder.HasLogging;

/**
 * 18. The Tree Sort Problem In the program outlined below, class Node is has
 * three data fields. The middle one, val , holds a value if type int . The
 * outer ones point to further nodes so that a tree structure is formed.The tree
 * is built up one node at a time. A new node is put on the tree in su h a way
 * that the tree is ordered. Thus 4 < 8 < 64. If the next new node is to
 * incorporate 32, it will hang from the left of the 64 node. Complete the
 * methods in class Node .
 */
public final class JavaExercise18 {
    private static final Logger LOGGER = HasLogging.log();
	private JavaExercise18() {
	}

	public static void main(String[] args) {
		Node tree = new Node(16);
		tree.put(8);
		tree.put(4);
		tree.put(64);
		tree.put(32);
        LOGGER.info("Tree elements: {}", tree);
        LOGGER.info("Element sum: {}", tree.sum());
	}

}