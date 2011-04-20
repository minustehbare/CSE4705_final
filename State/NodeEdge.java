package CSE4705_final.State;

/**
 *
 * @author Ethan Levine
 */
public class NodeEdge {

    private final Node _lesserNode;
    private final Node _greaterNode;

    public NodeEdge(Node nodeA, Node nodeB) {
        if (nodeA.getIndex() > nodeB.getIndex()) {
            _greaterNode = nodeA;
            _lesserNode = nodeB;
        } else {
            _greaterNode = nodeB;
            _lesserNode = nodeA;
        }
    }

    public Node lesserNode() {
        return _lesserNode;
    }

    public Node greaterNode() {
        return _greaterNode;
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass().equals(NodeEdge.class)) {
            NodeEdge otherEdge = (NodeEdge) other;
            return otherEdge.lesserNode().getIndex() == _lesserNode.getIndex() &&
                   otherEdge.greaterNode().getIndex() == _greaterNode.getIndex();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (_lesserNode.getIndex() << 16) + _greaterNode.getIndex();
    }
}
