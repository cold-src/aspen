package net.orbyfied.aspen.raw;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in configuration.
 */
public class Node {

    // the literal nodes surrounding this node
    final List<Node> prefixNodes = new ArrayList<>();
    final List<Node> suffixNodes = new ArrayList<>();

    public List<Node> getPrefixNodes() {
        return prefixNodes;
    }

    public List<Node> getSuffixNodes() {
        return suffixNodes;
    }

    public Node withPrefixNode(Node node) {
        prefixNodes.add(node);
        return this;
    }

    public Node withSuffixNode(Node node) {
        suffixNodes.add(node);
        return this;
    }

}
