package CSE4705_final.State;

/**
 *
 * @author Ethan Levine
 */
public class Edge {

    private final int _lesserIndex;
    private final int _greaterIndex;

    public Edge(int endA, int endB) {
        if (endA > endB) {
            _greaterIndex = endA;
            _lesserIndex = endB;
        } else {
            _greaterIndex = endB;
            _lesserIndex = endA;
        }
    }

    public int lesserIndex() {
        return _lesserIndex;
    }

    public int greaterIndex() {
        return _greaterIndex;
    }

    @Override
    public boolean equals(Object other) {
        if (other.getClass().equals(Edge.class)) {
            Edge otherEdge = (Edge) other;
            return otherEdge.lesserIndex() == _lesserIndex &&
                   otherEdge.greaterIndex() == _greaterIndex;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (_lesserIndex << 16) + _greaterIndex;
    }
}
