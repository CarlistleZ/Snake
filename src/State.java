import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class State {
    public BoardPanel board;
    LinkedList<Point> snake;
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
//        List<Position> availablePositions = this.board.getEmptyPositions();
//        availablePositions.forEach(p -> {
//            State newState = new State(this.board);
//            newState.setPlayerNo(3 - this.playerNo);
//            newState.getBoard().performMove(newState.getPlayerNo(), p);
//            possibleStates.add(newState);
//        });
        // TODO
        return possibleStates;
    }

    void incrementVisit() {
        this.visitCount++;
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
}