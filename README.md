# Snake Game - AI Project

In this project, we implement a snake game in Java using the JavaFX framework.

We use algorithms in AI to let computer player play against the human player. 

Algotithms used: 

* A Star Algorithm
* Iterative Deepening A Star Algorithm
* Monte Carlo Tree Search

## Demo
![demo-video](https://github.com/SeanZheng21/Snake-AI-MCTS/blob/master/images/demo.gif)

## A Star Algorithm:

This algorithm uses an encapsulation of the GameState at a given moment: the board, the fruit, the position of the snake, the priority, and the moves.  A GameState considers its neighbors as the four duplicates of itself and moves their heads towards north, south, east, and west. The “path” is a succession of states where the parent-child relationship is kept in the child node. This algorithm chooses from a certain (valid) game state a path(out of all possible successive states) to the target by using the following relation:

$$ f(n) = g(n) + h(n) $$

where f(n) is the estimated cost, g(n) is the cost so far, and h(n) is the heuristic value. This algorithm yields good result and good efficiency with respect to other algorithms in our implementation because the calculating Hamilton heuristics is simple in this case: the sum of the difference between the coordinates of the fruit and the head of the fruit. The priority queue of the explored GameStates selects the best GameState with the smallest f(n) value.

### A Star Pseudo Code

![A star pseudo code](https://github.com/SeanZheng21/Snake-AI-MCTS/blob/master/images/pseudo_astar.png)


## Iterative Deepening A Star Algorithm

In this iterative deepening version, instead of using a queue to guide the tree exploration, an exploration depth limit is used on a Depth First Search. In function IDAStar, the limit (fValue) keeps increasing while calling the recursive function recIDAStar with different levels.

The recursive function recIDAStar is the AStar function that only explores and returns a given state if the limit (fValue) is less than the maximum limit in the parameter. The exploration boundary keeps expanding when the limit in the IDAStar increases progressively.

## Monte Carlo Tree Search (MCTS)

This algorithm uses an MCTS cycle of selection-expansion-simulation-back propagation. The game tree is initiated after each new fruit and for each non-terminal step, function MCTS descends down the tree and selects the best move within the time limit between moves. The supporting data structures are:

* Tree: statically stores the exploration result to make MCTS an “any-time” algorithm
* Node: link each state to its parent, select child using Upper Confidence Bounds (UCB)
* State: a state of the game with AI-player flag and score/visit pair

### MCTS Pseudo Code

![MCTS pseudo code](https://github.com/SeanZheng21/Snake-AI-MCTS/blob/master/images/pseudo_mcts.png)

## Selection phase:

In function selectPromisingNode, for a given node, the UCB formula

![ucb formula](https://github.com/SeanZheng21/Snake-AI-MCTS/blob/master/images/ucb.png)

helps to choose the best child node that maximizes(for AI) or minimizes (for player) the UCB value with:    
* Vi: child state i’s score/visit pair,
* N: total visit,         
* ni: child state i’s visit
* C: constant,  √2 here

## Expansion phase:

In function expandNode, the node in the parameter is the most promising node so far. It is expanded by associating its neighbors to its child array.

## Simulation phase:

In function simulateRandomPlayout, we keep choosing randomly a child and descending down the tree until it terminates. The simulation result is +1 for AI win, 0 for a tie, and -1 for player win.

## Back propagation phase:

In function backPropagation, given the start point of the simulation, the simulation result is back propagated either positively (for AI nodes) or negatively (for player nodes). The score is inverted every other level from the promising node to the root node. By adding the score and increasing the visit count, the best next move at the end is the most visited child of the root.

## Optimization in implementation

The MCTS tree is only initiated when a new fruit is generated. After each game cycle, instead of regenerate a new MCTS tree, we use the most explored part(which is the most promising part) of the tree in MCTS in the next move to reuse the result of the previous cycles. According to the unforeseeable movement of the player, the root node navigates to the node with the corresponding player position.

When the distance between the snake head and the fruit is large and the board is empty, the simulation process has to simulate a long path. With the uniform random direction choice, the simulation is extremely likely to end with the snake colliding with itself instead of reaching the fruit. We used a guiding policy to guide the simulation towards the quadrant with the fruit.

With the resource consuming MCTS algorithm, using IDAStar with the Manhattan distance can also significantly improve the simulation efficiency.