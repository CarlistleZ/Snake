import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Node {
    public State state;
    public Node parent;
    public List<Node> childArray;

    public Node() {
        this.state = new State();
        childArray = new ArrayList<>();
    }

    public Node(State state) {
        this.state = state;
        childArray = new ArrayList<>();
    }

    public Node(State state, Node parent, List<Node> childArray) {
        this.state = state;
        this.parent = parent;
        this.childArray = childArray;
    }

    public Node(Node node) {
        this.childArray = new ArrayList<>();
        this.state = new State(node.state);
        if (node.parent != null)
            this.parent = node.parent;
        List<Node> childArray = node.childArray;
        for (Node child : childArray) {
            this.childArray.add(new Node(child));
        }
    }

    public Node getRandomChildNode() {
        int noOfPossibleMoves = this.childArray.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        Node res = this.childArray.get(selectRandom);
        return res;
    }

    public String toString(){
        return (state.isAI ? "AI" : "Player") + " node: " + " Score:( " + state.winScore + " , " +
                  state.visitCount + " )\t"+ "snake: (" + state.snake.peekFirst().getX()+", "+state.snake.peekFirst().getY() +
                ") player snake: (" + state.playerSnake.peekFirst().getX() + ", " + state.playerSnake.peekFirst().getY()+")" + "\n";
    }

    public Node getChildWithMinMaxScore() {
        if (state.isAI) {
            return Collections.max(this.childArray, Comparator.comparing(c -> {
                return c.state.visitCount;
            }));
        } else {
            return Collections.min(this.childArray, Comparator.comparing(c -> {
                return c.state.visitCount;
            }));
        }
    }
    public Direction getDirectionfromChild(Node child){
        int thisX, thisY, childX, childY;
        thisX = state.snake.peekFirst().x;
        thisY = state.snake.peekFirst().y;
        childX = child.state.snake.peekFirst().x;
        childY = child.state.snake.peekFirst().y;
        if(thisX == childX){
            if(thisY > childY)
                return Direction.North;
            else
                return Direction.South;
        }
        else{
            // The same Y
            if(thisX > childX + 1)
                return Direction.West;
            else
                return Direction.East;
        }
    }
}