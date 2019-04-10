import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JFrame;

/**
 * The {@code SnakeGame} class is responsible for handling much of the game's logic.
 *
 */
public class SnakeGame extends JFrame {

	public static class GameState implements Comparable<GameState>{

		GameState parent;
		LinkedList<Point> snake,player_snake;
		BoardPanel board;
		int moves;
		int priority;
		int x, y;

		// Generate a state from a game
		GameState(SnakeGame snakeGame, int moves, int priority){
			board = snakeGame.board;
			snake = new LinkedList<>();
			player_snake = new LinkedList<>();
			snake = (LinkedList<Point>) snakeGame.snake.clone();
			player_snake = (LinkedList<Point>) snakeGame.player_snake.clone();
			this.moves = moves;
			this.parent = null;
			this.priority = priority;
			x = snake.peekFirst().x;
			y = snake.peekFirst().y;
		}
		// Generate a state from a parent state
		GameState(GameState parent, int moves, int priority){
			board = parent.board;
			// snake = new LinkedList<>(snakeGame.snake);
			snake = new LinkedList<>();
			player_snake = new LinkedList<>();
			snake = (LinkedList<Point>) parent.snake.clone();
            player_snake = (LinkedList<Point>)parent.player_snake.clone();
            this.moves = moves;
			this.parent = parent;
			this.priority = priority;
			x = snake.peekFirst().x;
			y = snake.peekFirst().y;
		}
		// Generate a state from a parent state for idAStar
		GameState(GameState parent){
			board = parent.board;
			// snake = new LinkedList<>(snakeGame.snake);
			snake = new LinkedList<>();
			snake = (LinkedList<Point>) parent.snake.clone();
			player_snake = new LinkedList<>();
            player_snake = (LinkedList<Point>) parent.player_snake.clone();
            this.moves = 0;
			this.parent = parent;
			this.priority = 0;
			x = snake.peekFirst().x;
			y = snake.peekFirst().y;
		}
		// Generate everything as null
		GameState(){
			parent = null;
			snake = null;
			board = null;
			moves = Integer.MIN_VALUE;
			priority = Integer.MIN_VALUE;
			x = 0;
			y = 0;
		}

		@Override
		public int compareTo(GameState that) {
			if (this.priority < that.priority)
				return -1;
			if (this.priority > that.priority)
				return 1;
			return 0;
		}

		public String toString(){
			return "State (" + x + ", " + y + ")";
		}
	}

	/**
	 * The number of milliseconds that should pass between each frame.
	 */
	private static final long FRAME_TIME = 1000L / 50L;
	
	/**
	 * The minimum length of the snake. This allows the snake to grow
	 * right when the game starts, so that we're not just a head moving
	 * around on the board.
	 */
	private static final int MIN_SNAKE_LENGTH = 5;
	
	/**
	 * The maximum number of directions that we can have polled in the
	 * direction list.
	 */
	private static final int MAX_DIRECTIONS = 3;
	
	/**
	 * The BoardPanel instance.
	 */
	private BoardPanel board;
	
	/**
	 * The SidePanel instance.
	 */
	private SidePanel side;
	
	/**
	 * The random number generator (used for spawning fruits).
	 */
	private Random random;
	
	/**
	 * The Clock instance for handling the game logic.
	 */
	private Clock logicTimer;
	
	/**
	 * Whether or not we're running a new game.
	 */
	private boolean isNewGame;
		
	/**
	 * Whether or not the game is over.
	 */
	private boolean isGameOver;
	
	/**	
	 * Whether or not the game is paused.
	 */
	private boolean isPaused;
	
	/**
	 * The list that contains the points for the snake.
	 */
	private LinkedList<Point> snake;
	private LinkedList<Point> player_snake;
	/**
	 * The list that contains the queued directions.
	 */
	private LinkedList<Direction> directions;
	private LinkedList<Direction> playerDirections;
	
	/**
	 * The current moves.
	 */
	private int score;
	
	/**
	 * The number of fruits that we've eaten.
	 */
	private int fruitsEaten;
	
	/**
	 * The number of points that the next fruit will award us.
	 */
	private int nextFruitScore;

	/**
	 * X position of the fruit
	 */
	private int fruitX;

	/**
	 * Y position of the fruit
	 */
	private int fruitY;

	/**
	 * A map that holds all the directions at given coordinates
	 */
	private Map<Integer, Direction> directionMap;

	/**
	 * A matrix of all the positions in the board, true if it has been visited
	 * Reinitialize at the beginning of each independent search
	 */
	boolean visitedArr[][];

    boolean have_time_left;

	/**
	 *
	 */
	public SnakeGame duplicate(){
		try {
			return (SnakeGame) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		System.err.println("Error duplicating SnakeGame");
		return null;
	}

	/**
	 * Creates a new SnakeGame instance. Creates a new window,
	 * and sets up the controller input.
	 */
	private SnakeGame(SolverMode solverMode) {
		super("Snake Remake");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
				
		/*
		 * Initialize the game's panels and add them to the window.
		 */
		this.board = new BoardPanel(this);
		this.side = new SidePanel(this);
		
		add(board, BorderLayout.CENTER);
		add(side, BorderLayout.EAST);
		
		/*
		 * Adds a new key listener to the frame to process input. 
		 */
		addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to North before adding the
				 * direction to the list.
				 */
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					goNorth();
					break;

				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to South before adding the
				 * direction to the list.
				 */	
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					goSouth();
					break;
				
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to West before adding the
				 * direction to the list.
				 */						
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					goWest();
					break;
			
				/*
				 * If the game is not paused, and the game is not over...
				 * 
				 * Ensure that the direction list is not full, and that the most
				 * recent direction is adjacent to East before adding the
				 * direction to the list.
				 */		
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					goEast();
					break;
				
				/*
				 * If the game is not over, toggle the paused flag and update
				 * the logicTimer's pause flag accordingly.
				 */
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_P:
					if(!isGameOver) {
						isPaused = !isPaused;
						logicTimer.setPaused(isPaused);
					}
					break;
				
				/*
				 * Reset the game if one is not currently in progress.
				 */
				case KeyEvent.VK_ENTER:
					if(isNewGame || isGameOver) {
						resetGame(solverMode);
					}
					break;
				}
			}
			
		});
		
		/*
		 * Resize the window to the appropriate size, center it on the
		 * screen and display it.
		 */
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void goTowardsDirection(Direction dir){
		switch (dir){
			case East:
				goEastAI();
			case West:
				goWestAI();
			case North:
				goNorthAI();
			case South:
				goSouthAI();
		}
	}

	private void goNorth(){
		if(!isPaused && !isGameOver) {
			if(playerDirections.size() < MAX_DIRECTIONS) {
				Direction last = playerDirections.peekLast();
				if(last != Direction.South && last != Direction.North) {
					playerDirections.addLast(Direction.North);
				}
			}
		}
	}

	private void goSouth(){
		if(!isPaused && !isGameOver) {
			if(playerDirections.size() < MAX_DIRECTIONS) {
				Direction last = playerDirections.peekLast();
				if(last != Direction.North && last != Direction.South) {
					playerDirections.addLast(Direction.South);
				}
			}
		}
	}

	private void goEast(){
		if(!isPaused && !isGameOver) {
			if(playerDirections.size() < MAX_DIRECTIONS) {
				Direction last = playerDirections.peekLast();
				if(last != Direction.West && last != Direction.East) {
					playerDirections.addLast(Direction.East);
				}
			}
		}
	}

	private void goWest(){
		if(!isPaused && !isGameOver) {
			if(playerDirections.size() < MAX_DIRECTIONS) {
				Direction last = playerDirections.peekLast();
				if(last != Direction.East && last != Direction.West) {
					playerDirections.addLast(Direction.West);
				}
			}
		}
	}

	private void goNorthAI(){
		if(!isPaused && !isGameOver) {
			if(directions.size() < MAX_DIRECTIONS) {
				Direction last = directions.peekLast();
				if ((last != Direction.South && last != Direction.North)&&
						(board.getTile(snake.peekFirst().x,snake.peekFirst().y - 1) != TileType.SnakeBody)) {
					directions.addLast(Direction.North);
				}
			}
		}
	}

	private void goSouthAI(){
		if(!isPaused && !isGameOver) {
			if(directions.size() < MAX_DIRECTIONS) {
				Direction last = directions.peekLast();
				if((last != Direction.North && last != Direction.South) &&
						(board.getTile(snake.peekFirst().x,snake.peekFirst().y + 1) != TileType.SnakeBody)){
					directions.addLast(Direction.South);
				}
			}
		}
	}

	private void goEastAI(){
		if(!isPaused && !isGameOver) {
			if(directions.size() < MAX_DIRECTIONS) {
				Direction last = directions.peekLast();
				if ((last != Direction.West && last != Direction.East) &&
						(board.getTile(snake.peekFirst().x + 1,snake.peekFirst().y) != TileType.SnakeBody)){
					directions.addLast(Direction.East);
				}
			}
		}
	}

	private void goWestAI(){
		if(!isPaused && !isGameOver) {
			if(directions.size() < MAX_DIRECTIONS) {
				Direction last = directions.peekLast();
				if((last != Direction.East && last != Direction.West) &&
						(board.getTile(snake.peekFirst().x,snake.peekFirst().y) != TileType.SnakeBody)) {
					directions.addLast(Direction.West);
				}
			}
		}
	}

	private void initVisitedArr(){
		visitedArr= new boolean[board.getHeight()][board.getWidth()];
		for(int i = 0; i < visitedArr.length; i++){
			for(int j = 0; j < visitedArr[0].length; j++){
				visitedArr[i][j] = false;
			}
		}
	}

	private static final float clockFrequency = 9.0f;

	/**
	 * Starts the game running in player mode with actions.
	 */
	private void startGamePlayer(SolverMode solverMode) {
		/*
		 * Initialize everything we're going to be using.
		 */
		this.random = new Random();
		this.snake = new LinkedList<>();
		this.player_snake = new LinkedList<>();
		this.directions = new LinkedList<>();
		this.logicTimer = new Clock(clockFrequency);
		this.isNewGame = true;
		this.directionMap = new HashMap<>();
		this.playerDirections = new LinkedList<>();

		//Set the timer to paused initially.
		logicTimer.setPaused(true);

		/*
		 * This is the game loop. It will update and render the game and will
		 * continue to run until the game window is closed.
		 */
		while(true) {
			//Get the current frame's start time.
			long start = System.nanoTime();

			//Update the logic timer.
			logicTimer.update();

			/*
			 * If a cycle has elapsed on the logic timer, then update the game.
			 */
			if(logicTimer.hasElapsedCycle()) {
				if (solverMode == SolverMode.MCTS)
					mcts(new GameState(this, 0, getHeuristic(snake)), false);
				checkActionList(snake.peekFirst());
				updateGame(solverMode);
			}
			// checkActionList(snake.peekFirst());
			//Repaint the board and side panel with the new content.
			board.repaint();
			side.repaint();

			/*
			 * Calculatplayer_collision == TileType.SnakeBodye the delta time between since the start of the frame
			 * and sleep for the excess time to cap the frame rate. While not
			 * incredibly accurate, it is sufficient for our purposes.
			 */
			long delta = (System.nanoTime() - start) / 1000000L;
			if(delta < FRAME_TIME) {
				try {
					Thread.sleep(FRAME_TIME - delta);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Check the list generated by AStar to add a direction
	 * according to the head coordinates
	 * @param head head of the snake to check
	 */
	private void checkActionList(Point head){
		int checkSum = head.x + BoardPanel.COL_COUNT * head.y;
		if (directionMap.containsKey(checkSum)){
			Direction dir = directionMap.get(checkSum);
			if(dir == null)
				return;
			directions.clear();
			switch (dir){
				case East:
					directions.addLast(Direction.East);
					break;
				case West:
					directions.addLast(Direction.West);
					break;
				case North:
					directions.addLast(Direction.North);
					break;
				case South:
					directions.addLast(Direction.South);
					break;
			}
			// System.out.println("I'm at: " + head.x +", "+head.y+"\tgoing: "+directions.peekLast());
			directionMap.remove(checkSum);
		}
	}

	private void generatePathFromState(GameState currentState){
		// Construct path from states
		// Generate an action list: a list of directions at (x, y)

		 //Clear the directions
		int secondLastX = 0, secondLastY = 0;
		directions.clear();
		directionMap = new HashMap<>();
		while (currentState.parent != null){
			directionMap.put(currentState.parent.x + BoardPanel.COL_COUNT * currentState.parent.y, getStateDirection(currentState));
			// System.out.println("At "+currentState.parent.x+", "+currentState.parent.y+"\tshould go "+getStateDirection(currentState));
			if(currentState.parent.parent == null){
				secondLastX = currentState.x;
				secondLastY = currentState.y;
			}
			currentState = currentState.parent;
		}
		int lastX, lastY;
		lastX = currentState.x;
		lastY = currentState.y;
		directionMap.put(currentState.x + BoardPanel.COL_COUNT * currentState.y, getInitialStateDirection(secondLastX, secondLastY, lastX, lastY));
	}

	/**
	 *  AStar generates the direction at a given game instant
	 *  This method is used each time the fruit is generated
	 */
	private void AStar() {

		PriorityQueue<GameState> queue = new PriorityQueue<>();
		queue.add(new GameState(this, 0, getHeuristic(snake)));
		GameState currentState = queue.poll();
		System.out.println("Initial position: " + currentState.x+", "+currentState.y);
		initVisitedArr();
		while (true) {
			if(visitedArr[currentState.x][currentState.y]){
				// Continues if this state has been visited
				currentState = queue.poll();
				continue;
			}
			visitedArr[currentState.x][currentState.y] = true;
			for (GameState neighborState : neighbors(currentState)){
				Point snakeHead = neighborState.snake.peekFirst();
				if(snakeHead.x < 0 || snakeHead.x >= BoardPanel.COL_COUNT || snakeHead.y < 0 || snakeHead.y >= BoardPanel.ROW_COUNT){
					// Out of board, do nothing
				}
				else if (board.getTile(snakeHead.x, snakeHead.y) == TileType.SnakeBody) {
					visitedArr[snakeHead.x][snakeHead.y] = true;
				}else if(!visitedArr[neighborState.x][neighborState.y])
					queue.add(neighborState);
			}
			if(!queue.isEmpty())
				currentState = queue.poll();
			else{
				System.err.println("Queue is empty!!!");
				break;
			}
			if(isGoal(currentState.snake)) {
				System.out.println("Goal: "+fruitX+", "+fruitY);
				break;
			}
		}

		generatePathFromState(currentState);
	}

	private void idAStar() {
		GameState currentState = new GameState(this, 0, getHeuristic(snake));
		// System.out.println("Initial: Snake: " + currentState + "\tFruit: (" + fruitX +", " + fruitY +")");
		GameState state = null;
		int fValueLimit = getHeuristic(currentState.snake);
		final int maxSteps = BoardPanel.COL_COUNT + BoardPanel.ROW_COUNT;
		final int maxLimit = BoardPanel.COL_COUNT + BoardPanel.ROW_COUNT + maxSteps;
		boolean foundCorrectly = false;

		// Iterative deepening loop
		while (fValueLimit <= maxLimit){
			// System.out.println("Executing limit: " + fValueLimit);
			initVisitedArr();
			state = idAStar(currentState, fValueLimit);
			if(state != null){
				currentState = state;
				foundCorrectly = true;
				// System.out.println("Found at limit: " + fValueLimit);
				break;
			}else {
				fValueLimit++;
				// System.out.println("Changing to limit: " + fValueLimit);
			}
		}
		if(foundCorrectly)
			generatePathFromState(currentState);
		else
			System.err.println("idAStar failed to generate a path");
	}

	/**
	 * Generates a solution state from a gamestate and a ID limit
	 * @param currentState the state to analyze
	 * @param limit  the limit of iterative deepening
	 * @return the game state which can lead to the fruit
	 *         null if current state passes the limit or no possible path under the current state
	 */
	private GameState idAStar(GameState currentState, int limit){
		// System.out.println("idAStar with depth: " + limit);
		if(isGoal(currentState.snake)){
			// System.out.println("Found goal: " + currentState);
			return currentState;
		}
		for (GameState neighbor: neighbors(currentState)){
			if(getHeuristic(neighbor.snake) >= (limit - 1) || visitedArr[neighbor.x][neighbor.y]){
				// Do not explore if the state is too far or the stated has been visited
				continue;
			}
			visitedArr[neighbor.x][neighbor.y] = true;
			if(board.getTile(neighbor.x, neighbor.y) == TileType.SnakeBody)
				continue;
			// Explore the neighbor with decreased limit
			GameState gs = idAStar(neighbor,limit - 1);
			// game state is null if we found nothing under this state
			if(gs != null){
				// Construct the result
				return gs;
			}
		}
		// System.out.println("Returning null at level: " + limit);
		return null;
	}

	private Direction getStateDirection(GameState state){
		if(state.parent == null)
			return null;
		else{
			int myX, myY, parentX, parentY;
			myX = state.x;
			myY = state.y;
			parentX = state.parent.x;
			parentY = state.parent.y;
			if(myX == parentX + 1)
				return Direction.East;
			else if(myX == parentX - 1)
				return Direction.West;
			else if(myY == parentY + 1)
				return Direction.South;
			else if(myY == parentY - 1)
				return Direction.North;
			else{
				System.err.println("Error in state!");
				return null;
			}
		}
	}

	private Direction getInitialStateDirection(int secondToLastX, int secondToLastY, int lastX, int lastY){
		int  midX, midY;
		midX = lastX;
		midY = lastY;
		if(secondToLastX == midX + 1)
			return Direction.East;
		else if(secondToLastX == midX - 1)
			return Direction.West;
		else if(secondToLastY == midY + 1)
			return Direction.South;
		else if(secondToLastY == midY - 1)
			return Direction.North;
		else{
			System.err.println("Error: wrong state to use initial direction");
			return null;
		}
	}

	public LinkedList<GameState> neighbors (GameState state) {
		LinkedList<GameState> res = new LinkedList<>();

		res.add(generateNeighbor(state, Direction.East));
		res.add(generateNeighbor(state, Direction.West));
		res.add(generateNeighbor(state, Direction.North));
		res.add(generateNeighbor(state, Direction.South));

		return res;
	}

	/**
	 * Generates a neighbor gamestate according to the indicated direction
	 * @param state The gamestate from which we generate
	 * @param dir direction of the neighbor wrt the state
	 * @return
	 */
	private GameState generateNeighbor(GameState state, Direction dir){
//		 System.out.println("Generate neighbor of: "+state.x+", "+state.y+" Direction: "+dir);
		GameState neighbor = new GameState(state, state.moves + 1, state.priority);
		Point head = new Point(neighbor.snake.peekFirst());
//		System.out.println("head: "+head.x+", "+head.y);
		if(head.x != state.x || head.y != state.y){
			System.err.println("Inconsistent value");
		}
		switch (dir){
			case East:
				head.x++;
				neighbor.x++;
				break;
			case West:
				head.x--;
				neighbor.x--;
				break;
			case South:
				head.y++;
				neighbor.y++;
				break;
			case North:
				head.y--;
				neighbor.y--;
				break;
		}
		neighbor.snake.addFirst(head);
		// Remove the old tail
		neighbor.snake.removeLast();
		// Calculate the new heuristic with the new head
		neighbor.priority = neighbor.moves + getHeuristic(neighbor.snake);
		// System.out.println("Neighbor is: "+neighbor.x+", "+neighbor.y);
		return neighbor;
	}

	/**
	 * Updates the game's logic.
	 */
	private void updateGame(SolverMode solverMode) {
		/*
		 * Gets the type of tile that the head of the snake collided with. If 
		 * the snake hit a wall, SnakeBody will be returned, as both conditions
		 * are handled identically.
		 */
		TileType collision = updateSnake(snake,directions);
//		System.out.println("====Updating player snake ====");
//		for(Direction d:playerDirections){
//			System.out.println(d);
//		}
//		System.out.println("=======================");
		TileType player_collision = updateSnake(player_snake, playerDirections);
		
		/*
		 * Here we handle the different possible collisions.
		 * 
		 * Fruit: If we collided with a fruit, we increment the number of
		 * fruits that we've eaten, update the moves, and spawn a new fruit.
		 * 
		 * SnakeBody: If we collided with our tail (or a wall), we flag that
		 * the game is over and pause the game.
		 * 
		 * If no collision occurred, we simply decrement the number of points
		 * that the next fruit will give us if it's high enough. This adds a
		 * bit of skill to the game as collecting fruits more quickly will
		 * yield a higher moves.
		 */
		if(collision == TileType.Fruit) {
			fruitsEaten++;
			score += nextFruitScore;
			spawnFruit(solverMode);
		} else if(collision == TileType.SnakeBody || player_collision == TileType.SnakeBody) {
			isGameOver = true;
			logicTimer.setPaused(true);
		} else if(nextFruitScore > 10) {
			nextFruitScore--;
		}
		if(player_collision == TileType.Fruit) {
			fruitsEaten++;
			score += nextFruitScore;
			spawnFruit(solverMode);
		}
	}

	public Direction getDirection(LinkedList<Direction> directions){
		return directions.peekFirst();
	}
	/**
	 * Updates the snake's position and size.
	 * @return Tile tile that the head moved into.
	 */
	private TileType updateSnake(LinkedList<Point> snake_update,LinkedList<Direction> list_direction) {

		/*
		 * Here we peek at the next direction rather than polling it. While
		 * not game breaking, polling the direction here causes a small bug
		 * where the snake's direction will change after a game over (though
		 * it will not move).
		 */
		if(directions.isEmpty())
			return null;

		Direction direction = getDirection(list_direction);
		//directions.peekFirst();
		/*
		 * Here we calculate the new point that the snake's head will be at
		 * after the update.
		 */		
		Point head = new Point(snake_update.peekFirst());
		switch(direction) {
		case North:
			head.y--;
			break;
			
		case South:
			head.y++;
			break;
			
		case West:
			head.x--;
			break;
			
		case East:
			head.x++;
			break;
		}
		
		/*
		 * If the snake has moved out of bounds ('hit' a wall), we can just
		 * return that it's collided with itself, as both cases are handled
		 * identically.
		 */
		if(head.x < 0 || head.x >= BoardPanel.COL_COUNT || head.y < 0 || head.y >= BoardPanel.ROW_COUNT) {
			return TileType.SnakeBody; //Pretend we collided with our body.
		}
		
		/*
		 * Here we get the tile that was located at the new head position and
		 * remove the tail from of the snake and the board if the snake is
		 * long enough, and the tile it moved onto is not a fruit.
		 * 
		 * If the tail was removed, we need to retrieve the old tile again
		 * increase the tile we hit was the tail piece that was just removed
		 * to prevent a false game over.
		 */
		TileType old = board.getTile(head.x, head.y);
		if(old != TileType.Fruit && snake.size() > MIN_SNAKE_LENGTH) {
			Point tail = snake_update.removeLast();
			board.setTile(tail, null);
			old = board.getTile(head.x, head.y);
		}
		
		/*
		 * Update the snake's position on the board if we didn't collide with
		 * our tail:
		 * 
		 * 1. Set the old head position to a body tile.
		 * 2. Add the new head to the snake.
		 * 3. Set the new head position to a head tile.
		 * 
		 * If more than one direction is in the queue, poll it to read new
		 * input.
		 */
		if(old != TileType.SnakeBody) {
			board.setTile(snake_update.peekFirst(), TileType.SnakeBody);
			snake_update.push(head);
			board.setTile(head, TileType.SnakeHead);
			if(list_direction.size() > 1) {
				list_direction.poll();
			}
		}
				
		return old;
	}

	/**
	 * Resets the game's variables to their default states and starts a new game.
	 */
	private void resetGame(SolverMode solverMode) {
		/*
		 * Reset the moves statistics. (Note that nextFruitPoints is reset in
		 * the spawnFruit function later on).
		 */
		this.score = 0;
		this.fruitsEaten = 0;
		
		/*
		 * Reset both the new game and game over flags.
		 */
		this.isNewGame = false;
		this.isGameOver = false;
		
		/*
		 * Create the head at the center of the board.
		 */
		Point head = new Point(BoardPanel.COL_COUNT / 2, BoardPanel.ROW_COUNT / 2);
		Point player_head = new Point(3,3);

		/*
		 * Clear the snake list and add the head.
		 */
		snake.clear();
		snake.add(head);

		player_snake.clear();
		player_snake.add(player_head);
		playerDirections.addLast(Direction.East);
		
		/*
		 * Clear the board and add the head.
		 */
		board.clearBoard();
		board.setTile(head, TileType.SnakeHead);
		board.setTile(player_head,TileType.SnakeHead);
		/*
		 * Reset the logic timer.
		 */
		logicTimer.reset();



		/*
		 * Spawn a new fruit.
		 */
		spawnFruit(solverMode);
	}

	/**
	 * Gets the flag that indicates whether or not we're playing a new game.
	 * @return The new game flag.
	 */
	public boolean isNewGame() {
		return isNewGame;
	}
	
	/**
	 * Gets the flag that indicates whether or not the game is over.
	 * @return The game over flag.
	 */
	public boolean isGameOver() {
		return isGameOver;
	}
	
	/**
	 * Gets the flag that indicates whether or not the game is paused.
	 * @return The paused flag.
	 */
	public boolean isPaused() {
		return isPaused;
	}

	public boolean isGoal(LinkedList<Point> snake) {
		return (snake.peekFirst().x == fruitX) && (snake.peekFirst().y == fruitY);
	}

	public int getHeuristic(LinkedList<Point> snake){
		return Math.abs(snake.peekFirst().x - fruitX) + Math.abs(snake.peekFirst().y - fruitY);
	}
	
	/**
	 * Spawns a new fruit onto the board.
	 */
	private void spawnFruit(SolverMode solverMode) {


		//Reset the moves for this fruit to 100.
		this.nextFruitScore = 100;

		/*
		 * Get a random index based on the number of free spaces left on the board.
		 */
		int index = random.nextInt(BoardPanel.COL_COUNT * BoardPanel.ROW_COUNT - snake.size());
		
		/*
		 * While we could just as easily choose a random index on the board
		 * and check it if it's free until we find an empty one, that method
		 * tends to hang if the snake becomes very large.
		 * 
		 * This method simply loops through until it finds the nth free index
		 * and selects uses that. This means that the game will be able to
		 * locate an index at a relatively constant rate regardless of the
		 * size of the snake.
		 */
		int freeFound = -1;
		for(int x = 0; x < BoardPanel.COL_COUNT; x++) {
			for(int y = 0; y < BoardPanel.ROW_COUNT; y++) {
				TileType type = board.getTile(x, y);
				if(type == null || type == TileType.Fruit) {
					if(++freeFound == index) {
						board.setTile(x, y, TileType.Fruit);
						fruitX = x;
						fruitY = y;
						board.fruitX = x;
						board.fruitY = y;
						break;
					}
				}
			}
		}


		// Use A Star to generate a path to the goal
		switch (solverMode){
			case AStar:
				AStar();
				break;
			case idAstar:
				idAStar();
				break;
			case MCTS:
				mcts(new GameState(this, 0, getHeuristic(snake)), true);
				break;
		}
	}
	
	/**
	 * Gets the current moves.
	 * @return The moves.
	 */
	public int getScore() {
		return score;
	}
	
	/**
	 * Gets the number of fruits eaten.
	 * @return The fruits eaten.
	 */
	public int getFruitsEaten() {
		return fruitsEaten;
	}
	
	/**
	 * Gets the next fruit moves.
	 * @return The next fruit moves.
	 */
	public int getNextFruitScore() {
		return nextFruitScore;
	}
	
	/**
	 * Gets the current direction of the snake.
	 * @return The current direction.
	 */
	public Direction getDirection() {
		return directions.peek();
	}
	

	public static void main(String[] args) {
		SolverMode solverMode = SolverMode.MCTS;
		SnakeGame snake = new SnakeGame(solverMode);
		snake.startGamePlayer(solverMode);
	}


	/**
	 *  -------------------------------------------------------------------------------------------
	 *  *****************************  Monte Carlo Tree Search (MCTS) *****************************
	 *  -------------------------------------------------------------------------------------------
     */

	// counter for mcts test
	private int MCTSLoopCounter;
	private static Tree tree;

	public void mcts(GameState gameState, boolean isStart) {
		// Initialize from a snake game state
		if (isStart){
			tree = new Tree();
		}
		// Initialize root here
		Node rootNode = tree.getRoot();
		rootNode.state.board = board;
		rootNode.state.snake = gameState.snake;
		rootNode.state.playerSnake = gameState.player_snake;
		rootNode.state.isAI = true;

		MCTSLoopCounter = 0;
		setNanoTime();
		while(/*haveTimeLeft() &&*/ MCTSLoopCounter < 1000){
			Node promisingNode = selectPromisingNode(rootNode);
			if( MCTSLoopCounter == 98){
				boolean bl = true;
			}
			if(!isGoal(promisingNode.state.snake))
				expandNode(promisingNode);
			Node nodeToExplore = null;
			if (promisingNode.childArray.size() > 0)
				nodeToExplore = promisingNode.getRandomChildNode();
			int playoutResult = simulateRandomPlayout(nodeToExplore);
			backPropagation(nodeToExplore, playoutResult);
			MCTSLoopCounter++;
		}
		// Return the best predictable direction so far
		Node winnerNode = rootNode.getChildWithMinMaxScore();
		System.out.println("Root at: " + tree.root.state.snake.peekFirst());
		System.out.println("Winner node at: " + winnerNode.state.snake.peekFirst());
		for(Node child: rootNode.childArray){
			System.out.println("( " + child.state.winScore + " , " + child.state.visitCount + " )");
		}
		Direction dir = tree.root.getDirectionfromChild(winnerNode);
//		System.out.println("Going: " + dir);
//		System.exit(0);
		tree.setRoot(winnerNode);
		tree.getRoot().parent = null;
		goTowardsDirection(dir);
	}

	/**
	 * Choose the best child node under the node
	 * @param rootNode The node to develop
	 * @return its best child node
	 */
	private Node selectPromisingNode(Node rootNode) {
//		System.out.println("SELECTION:");
		Node node = rootNode;
		while (node.childArray.size() != 0) {
//			System.out.print(node.state.isAI ? "AI " : "Player ");
			// descend to the best child node
			node = UCB.findBestNodeWithUCB(node, node.state.isAI);
//			System.out.println("chose: " + node);
		}
		return node;
	}

	/**
	 * Expand the promising node
	 * @param promisingNode
	 */
	private void expandNode(Node promisingNode) {
//		System.out.println("EXPANSION:");
//		System.out.println("On: " + promisingNode + "childArr: " + promisingNode.childArray.size());
		boolean bl = true;
		List<State> possibleStates = promisingNode.state.getAllPossibleStates(promisingNode);
		if(promisingNode.childArray.size() != 0)
			return;
		for (State s: possibleStates){
			Node neighborNode = new Node(s);
			neighborNode.parent = promisingNode;
			neighborNode.state.isAI = promisingNode.state.getOpponent();
			neighborNode.state.visitCount = 0;
			neighborNode.state.winScore = 0;
			promisingNode.childArray.add(neighborNode);
		}
//		System.out.println("Expanded: " + promisingNode.childArray);
	}


	private int simulateRandomPlayout(Node node) {
//		 System.out.println("SIMULATION:");
//		System.out.println("From: " + tempNode);
		State tempState = new State(node.state);
		int boardStatus = tempState.checkStatus();

		while (boardStatus == State.IN_PROGRESS) {
//			System.out.println(tempState);
//			System.out.println("BoardStatus: " + boardStatus);;
			tempState = tempState.randomPlay();
			tempState.togglePlayer();
			boardStatus = tempState.checkStatus();
		}
//		System.out.println("Simulation result for "+node.state+": " + boardStatus);
		return boardStatus;
	}

	/**
	 * Propagate the play out simulation value towards the root
	 * @param nodeToExplore
	 * @param playoutResult
	 */
	private void backPropagation(Node nodeToExplore, int playoutResult) {
//		System.out.println("\nBACK PROPAGATION:");
//		System.out.println("Propagating from " + nodeToExplore+ "\twith value: " + playoutResult);
		Node tempNode = nodeToExplore;
		boolean isAINode = tempNode.state.isAI;
		while (tempNode != null) {
			tempNode.state.visitCount++;
			if(tempNode.state.isAI == isAINode)
				tempNode.state.addScore(playoutResult);
			else
				tempNode.state.addScore(-playoutResult);
//			System.out.println("\tPropagating from " + tempNode.state+ "\twith value: " + playoutResult);
			tempNode = tempNode.parent;
		}
//		System.out.println();
	}

	private static long nanoTimeStamp;
	private void setNanoTime(){
		nanoTimeStamp = System.nanoTime();
	}

	/**
	 * Method used in the mcts loop to check if time has ran out in one interval
	 * @return if there's time left until the next update
	 */
	public boolean haveTimeLeft(){
		// TODO
		long currentTimeStamp = System.nanoTime();
		return (nanoTimeStamp + 0.5 * clockFrequency / Math.pow(1.0, 9.0)) > currentTimeStamp;
	}
}
