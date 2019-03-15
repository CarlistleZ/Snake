public class MCTS {

	private static class GameState implements Comparable<GameState>{
		GameState parent;
		LinkedList<Point>snake;
		BoardPanel board;
		int moves;
		int x, y;
		int explored;			// Number of explored states under this node
		int score;				// +1 if won, -1 if lose, 0 if even
		int depth;				// Depth of the state in the game tree

		public void updateFromChild(int childScore){
			explored++;
			score += childScore;
		}
	}

	private final int depthLimit = 15;

	/**
	* Main MCTS method
	* Perform a Monte Carlo Tree Search on a game state
	* returns the value for evaluation
	*/
	public void MCTS(GameState currentState){
		// perform random walk if it passes the limit
		if(currentState.depth > depthLimit){
			int stateScore = randomWalk(currentState);
			currentState.parent.updateFromChild(stateScore);
			return;
		}
		// Within the limit range, develop the game tree by exploring the children
		for(GameState neighbor: currentState.neighbors()){
			
		}

	}


	/**
	* Choose a child randomly until a leaf and return the leaf value
	*/
	private int randomWalk(GameState state){

	}

}