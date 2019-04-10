import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class State {
    static final int IN_PROGRESS = 0;
    static final int PLAYER_WIN = -1;
    static final int AI_WIN = 1;

    BoardPanel board;
    LinkedList<Point> snake, playerSnake;
    boolean isAI;
    int visitCount;
    double winScore;

    public State() {
        board = new BoardPanel();
        this.snake = new LinkedList<>();
        this.playerSnake = new LinkedList<>();
        isAI = true;
        visitCount = 0;
        winScore = Integer.MIN_VALUE;
    }

    public State(State state) {
        this.board = state.board;
        this.snake = (LinkedList<Point>) state.snake.clone();
        this.playerSnake = (LinkedList<Point>) state.playerSnake.clone();
        this.isAI = state.isAI;
        this.visitCount = state.visitCount;
        this.winScore = state.winScore;
    }

    public State(SnakeGame game) {
        this.snake = new LinkedList<>();
        this.playerSnake = new LinkedList<>();
        this.board = new BoardPanel(game);
    }

    public String toString(){
        return "State " + (isAI ? "AI" : "player") + " Score:( " + winScore + " , " + visitCount + " ) " +
                "snake: (" + snake.peekFirst().getX()+", "+snake.peekFirst().getY() +
                ") player snake: (" + playerSnake.peekFirst().getX() + ", " + playerSnake.peekFirst().getY()+")";
    }

    boolean getOpponent() { return !isAI; }

    public List<State> getAllPossibleStates() {
        List<State> possibleStates = new LinkedList<>();
        possibleStates.addAll(neighbors(isAI ? snake : playerSnake));
        return possibleStates;
    }

    private List<State> neighbors(LinkedList<Point> snake) {
        List<State> res = new LinkedList<>();
        Direction[] allDirections =
                {Direction.East, Direction.West, Direction.North, Direction.South};
        // Generate a list of neighbors of snake
        for(Direction direction: allDirections){
            res.add(generateNeighbor(this, direction));
        }
        return res;
    }

    private State generateNeighbor(State state, Direction dir){
        State neighbor = new State(state);
        Point head;
        if(neighbor.isAI){
            head = new Point(neighbor.snake.peekFirst());
        }else{
            head = new Point(neighbor.playerSnake.peekFirst());
        }
        switch (dir){
            case East:
                head.x++;
                break;
            case West:
                head.x--;
                break;
            case South:
                head.y++;
                break;
            case North:
                head.y--;
                break;
        }
        if(neighbor.isAI){
            neighbor.snake.addFirst(head);
            neighbor.snake.removeLast();
        }else{
            neighbor.playerSnake.addFirst(head);
            neighbor.playerSnake.removeLast();
        }
        return neighbor;
    }


    void addScore(double score) {
        if (this.winScore != Integer.MIN_VALUE) {
            this.winScore += score;
        } else{
            // The first time
            this.winScore = score;
        }
    }

    State randomPlay() {
//        List<Position> availablePositions = this.board.getEmptyPositions();
//        int totalPossibilities = availablePositions.size();
//        int selectRandom = (int) (Math.random() * totalPossibilities);
//        this.board.performMove(this.playerNo, availablePositions.get(selectRandom));
        LinkedList<Point> snakeToCheck = isAI ? snake : playerSnake;
        List<State>neighborList = neighbors(snakeToCheck);
        int selectRandom = (int) (Math.random() * neighborList.size());
        return neighborList.get(selectRandom);
    }

    void togglePlayer() {
        this.isAI = !this.isAI;
    }

    public int checkStatus(){
        LinkedList<Point> snakeToCheck = isAI ? snake : playerSnake;
        // System.out.println("Checking status for: isAI= " + isAI + " at: " + snakeToCheck.peekFirst());
        if (!board.inBoard(snakeToCheck.peekFirst())){
            return isAI ? PLAYER_WIN : AI_WIN;
        }else if(snakeToCheck.peekFirst().x == board.fruitX && snakeToCheck.peekFirst().y == board.fruitY){
            return isAI ? AI_WIN : PLAYER_WIN;
        } else if(board.getTile(snakeToCheck.peekFirst()) == TileType.SnakeBody) {
            return isAI ? PLAYER_WIN : AI_WIN;
        } else {
            return IN_PROGRESS;
        }
    }
    public Point getRightSnakeHead(){
        return isAI ? snake.peekFirst() : playerSnake.peekFirst();
    }
}