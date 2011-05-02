package CSE4705_final.State;

import java.util.*;

import CSE4705_final.Client.ClientMove;

/**
 *
 * @author Ethan Levine
 */
public class PartitionSet {
    
    private final List<Partition> _deadParts;
    private final List<Partition> _whiteOwnedParts;
    private final List<Partition> _blackOwnedParts;
    private final List<Partition> _contestedParts;
    public static final int SAVE_DEPTH = 3;

    
    public PartitionSet() {
        _deadParts = new LinkedList<Partition>();
        _whiteOwnedParts = new LinkedList<Partition>();
        _blackOwnedParts = new LinkedList<Partition>();
        _contestedParts = new LinkedList<Partition>();
    }

    private String getFullName() {
        Map<Integer, NodeState> currentStates = new HashMap<Integer, NodeState>();
        for (Partition part : _deadParts) {
            for (int i : part.getEnclosedSet()) {
                currentStates.put(i, part.getNodeState(i));
            }
        }
        for (Partition part : _whiteOwnedParts) {
            for (int i : part.getEnclosedSet()) {
                currentStates.put(i, part.getNodeState(i));
            }
        }
        for (Partition part : _blackOwnedParts) {
            for (int i : part.getEnclosedSet()) {
                currentStates.put(i, part.getNodeState(i));
            }
        }
        for (Partition part : _contestedParts) {
            for (int i : part.getEnclosedSet()) {
                currentStates.put(i, part.getNodeState(i));
            }
        }
        NodeSet printSet = NodeSet.BLOCKED_NODE_SET;
        int gen = 0;
        for (int i : currentStates.keySet()) {
            gen = printSet.forkNode(i, gen, currentStates.get(i));
        }
        //return printSet.printGen(gen);
      StringBuilder ret = new StringBuilder();
      for (int index = 0 ; index <= 99; index++) {
        NodeState iState = printSet.getNodeState(index, gen, true);
        if (!iState.equals(iState.BLOCKED)) {
            if (index <= 9) {
                ret.append(0);
            }
            ret.append(index);
            if(iState.equals(iState.EMPTY))
              ret.append('E');
            else
              ret.append(Node.stateToChar(iState));
        }
      }
      return ret.toString();
    }

    public String getNamePrefix() {
        String fullName = getFullName();
        return fullName.substring(0,fullName.length() - (fullName.length() % SAVE_DEPTH));
    }

    public String getNameSuffix() {
        String fullName = getFullName();
        String suffix = fullName.substring(fullName.length() - (fullName.length() % SAVE_DEPTH));
        
        //if the suffix is empty, return a default suffix that will never normally show up.
        //This allows me to parse it the data a little easier when reading from the
        //file.  If you would rather me do this on my end, let me know.
        if (suffix.length()==0)
          return("xx");
        
        return suffix;
    }

    public PartitionSet(Partition part) {
        this();
        addArbitraryPartition(part);
    }
    
    public PartitionSet(Collection<Partition> parts) {
        this();
        for (Partition part : parts) {
            addArbitraryPartition(part);
        }
    }
    
    public PartitionSet(List<Partition> deadParts,
                        List<Partition> whiteOwnedParts,
                        List<Partition> blackOwnedParts,
                        List<Partition> contestedParts) {
        _deadParts = deadParts;
        _whiteOwnedParts = whiteOwnedParts;
        _blackOwnedParts = blackOwnedParts;
        _contestedParts = contestedParts;
    }
    
    public List<Partition> getDeadParts() {
        return Collections.unmodifiableList(_deadParts);
    }
    
    public List<Partition> getWhiteOwnedParts() {
        return Collections.unmodifiableList(_whiteOwnedParts);
    }
    
    public List<Partition> getBlackOwnedParts() {
        return Collections.unmodifiableList(_blackOwnedParts);
    }
    
    public List<Partition> getContestedParts() {
        return Collections.unmodifiableList(_contestedParts);
    }
    
    public boolean areAnyContestedParts() {
        return _contestedParts.size() > 0;
    }
    
    private void addArbitraryPartition(Partition part) {
        switch (part.getPartitionState()) {
            case DEAD:
                _deadParts.add(part);
                break;
            case BLACK_OWNED:
                _blackOwnedParts.add(part);
                break;
            case WHITE_OWNED:
                _whiteOwnedParts.add(part);
                break;
            case CONTESTED:
                _contestedParts.add(part);
                break;
            default:
                throw new StateException ("Partition has an invalid state.");
        }
    }
    
    private PartitionSet forkPartitionSetInternal(Partition active, List<Partition> splitParts) {
        List<Partition> deadSet = null;
        List<Partition> whiteOwnedSet = null;
        List<Partition> blackOwnedSet = null;
        List<Partition> contestedSet = null;
        switch (active.getPartitionState()) {
            case DEAD:
                deadSet = new LinkedList(_deadParts);
                if (!deadSet.remove(active)) {
                    throw new StateException ("Partition \"active\" given to forkPartitionSet() is not registered!");
                }
                break;
            case WHITE_OWNED:
                whiteOwnedSet = new LinkedList(_whiteOwnedParts);
                if (!whiteOwnedSet.remove(active)) {
                    throw new StateException ("Partition \"active\" given to forkPartitionSet() is not registered!");
                }
                break;
            case BLACK_OWNED:
                blackOwnedSet = new LinkedList(_blackOwnedParts);
                if (!blackOwnedSet.remove(active)) {
                    throw new StateException ("Partition \"active\" given to forkPartitionSet() is not registered!");
                }
                break;
            case CONTESTED:
                contestedSet = new LinkedList(_contestedParts);
                if (!contestedSet.remove(active)) {
                    throw new StateException ("Partition \"active\" given to forkPartitionSet() is not registered!");
                }
                break;
            default:
                throw new StateException ("Partition has an invalid state.");
        }
        
        // Categorize these changes.
        for (Partition splitPart : splitParts) {
            switch (splitPart.getPartitionState()) {
                case DEAD:
                    if (deadSet == null) {
                        deadSet = new LinkedList(_deadParts);
                    }
                    deadSet.add(splitPart);
                    break;
                case WHITE_OWNED:
                    if (whiteOwnedSet == null) {
                        whiteOwnedSet = new LinkedList(_whiteOwnedParts);
                    }
                    whiteOwnedSet.add(splitPart);
                    break;
                case BLACK_OWNED:
                    if (blackOwnedSet == null) {
                        blackOwnedSet = new LinkedList(_blackOwnedParts);
                    }
                    blackOwnedSet.add(splitPart);
                    break;
                case CONTESTED:
                    if (contestedSet == null) {
                        contestedSet = new LinkedList(_contestedParts);
                    }
                    contestedSet.add(splitPart);
                    break;
                default:
                    throw new StateException ("Partition has an invalid state.");
            }
        }
        
        // If any partition lists are unchanged, simply pass the reference to
        // the old one!
        if (deadSet == null) {
            deadSet = _deadParts;
        }
        if (whiteOwnedSet == null) {
            whiteOwnedSet = _whiteOwnedParts;
        }
        if (blackOwnedSet == null) {
            blackOwnedSet = _blackOwnedParts;
        }
        if (contestedSet == null) {
            contestedSet = _contestedParts;
        }
        
        // Make and return the new partition set!
        return new PartitionSet(deadSet, whiteOwnedSet, blackOwnedSet, contestedSet);
    }
    
    public Partition getContainingPartition(int index) {
        for (Partition part : _deadParts) {
            if (part.containsNode(index)) {
                return part;
            }
        }
        for (Partition part : _whiteOwnedParts) {
            if (part.containsNode(index)) {
                return part;
            }
        }
        for (Partition part : _blackOwnedParts) {
            if (part.containsNode(index)) {
                return part;
            }
        }
        for (Partition part : _contestedParts) {
            if (part.containsNode(index)) {
                return part;
            }
        }
        // Not found in any partition...
        throw new StateException ("Node with index [" + index + "] cannot be found in the partition set.");
    }
    
    public PartitionSet forkPartitionSet(Partition active, int index, NodeState newState) {
        return forkPartitionSetInternal(active, active.forkNode(index, newState));
    }
    
    public PartitionSet forkPartitionSet(Partition active, ClientMove move,
            boolean isMovingPlayerBlack) {
        return forkPartitionSetInternal(active, active.forkMove(move, isMovingPlayerBlack));
    }
    
    public PartitionSet forkPartitionSet(int index, NodeState newState) {
        return forkPartitionSet(getContainingPartition(index), index, newState);
    }
    
    public PartitionSet forkPartitionSet(ClientMove move, boolean isMovingPlayerBlack) {
        return forkPartitionSet(getContainingPartition(Node.getIndex(move.getFromRow(),
                move.getFromCol())), move, isMovingPlayerBlack);
    }
    
    public List<ClientMove> getPossibleContestedMoves(boolean isPlayerBlack) {
        List<ClientMove> moveList = new LinkedList<ClientMove>();
        for (Partition contestedPart : _contestedParts) {
            if (isPlayerBlack) {
                for (int index : contestedPart.getBlackQueens()) {
                    moveList.addAll(contestedPart.getPossibleMoves(index));
                }
            } else {
                for (int index : contestedPart.getWhiteQueens()) {
                    moveList.addAll(contestedPart.getPossibleMoves(index));
                }
            }
        }
        return moveList;
    }

    public String getPrintout() {
        Map<Integer, NodeState> currentStates = new HashMap<Integer, NodeState>();
        for (Partition part : _deadParts) {
            for (int index : part.getEnclosedSet()) {
                currentStates.put(index, part.getNodeState(index));
            }
        }
        for (Partition part : _whiteOwnedParts) {
            for (int index : part.getEnclosedSet()) {
                currentStates.put(index, part.getNodeState(index));
            }
        }
        for (Partition part : _blackOwnedParts) {
            for (int index : part.getEnclosedSet()) {
                currentStates.put(index, part.getNodeState(index));
            }
        }
        for (Partition part : _contestedParts) {
            for (int index : part.getEnclosedSet()) {
                currentStates.put(index, part.getNodeState(index));
            }
        }
        NodeSet printSet = NodeSet.BLOCKED_NODE_SET;
        int gen = 0;
        for (int index : currentStates.keySet()) {
            gen = printSet.forkNode(index, gen, currentStates.get(index));
        }
        return printSet.printGen(gen);
    }
}
