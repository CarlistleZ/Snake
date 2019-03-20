import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class State {
    public static final int IN_PROGRESS = 0;
    public static final int PLAYER_WIN = -1;
    public static final int AI_WIN = 1;

    public BoardPanel board;
    LinkedList<Point> snake, playerSnake;
    public boolean isAI;
    public int visitCount;
    public double winScore;

    public State() {
        board = new BoardPanel();
        this.snake = new LinkedList<>();
    }

    public State(State state) {
        this.board = new BoardPanel(state.board);
        this.snake = (LinkedList<Point>) state.snake.clone();
        this.playerSnake = (LinkedList<Point>) state.playerSnake.clone();
        this.isAI = state.isAI;
        this.visitCount = state.visitCount;
        this.winScore = state.winScore;
    }

    public State(SnakeGame game) {
        this.snake = new LinkedList<>();
        this.board = new BoardPanel(game);
    }

    boolean getOpponent() { return !isAI; }

    public List<State> getAllPossibleStates() {
        List<State> possibleStates = new LinkedList<>();
        possibleStates.addAll(neighbors(isAI ? snake : playerSnake));
        return possibleStates;
    }

    private List<State> neighbors(LinkedList<Point> snake) {
        List<State> res = new LinkedList<>();
        // TODO
        // Generate a list of neighbors of snake
        return res;
    }

    void addScore(double score) {
        if (this.winScore != Integer.MIN_VALUE)
            this.winScore += score;
    }

    void randomPlay() {
//        List<Position> availablePositions = this.board.getEmptyPositions();
//        int totalPossibilities = availablePositions.size();
//        int selectRandom = (int) (Math.random() * totalPossibilities);
//        this.board.performMove(this.playerNo, availablePositions.get(selectRandom));
        // TODO
    }

    void togglePlayer() {
        this.isAI = !this.isAI;
    }

    public int checkStatus(){
        if(snake.peekFirst().x == board.fruitX && snake.peekFirst().y == board.fruitY){
            if(isAI){
                return AI_WIN;
            }else{
                return PLAYER_WIN;
            }
        }else{
            return IN_PROGRESS;
        }
    }
}