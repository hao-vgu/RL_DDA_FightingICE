import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import aiinterface.CommandCenter;
import enumerate.Action;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;

/**
 * Node used by MCTS.
 *
 * @author Taichi Miyazaki
 */
public class Node {

  /** UCT execution time. */
  public static final int UCT_TIME = 165 * 100000;
  
  public static final int ITERATION_LIMIT = 23;

  /** Value of the UCB1 constant C. */
  public static final double UCB_C = 3;

  /** Maximum search tree depth. */
  public static final int UCT_TREE_DEPTH = 2;

  /** Threshold for creating a node. */
  public static final int UCT_CREATE_NODE_THRESHOULD = 10;

  /** Simulation duration. */
  public static final int SIMULATION_TIME = 60;

  /** Random number generator. */
  private Random rnd;

  /** Parent node. */
  private Node parent;

  /** Child nodes. */
  private Node[] children;

  /** Node depth. */
  private int depth;

  /** Number of times the node has been visited. */
  private int games;

  /** UCB1 value. */
  private double ucb;

  /** Evaluation score. */
  private double score;

  /** All actions available to this player. */
  private LinkedList<Action> myActions;

  /** All actions available to the opponent. */
  private LinkedList<Action> oppActions;

  /** Simulator used to run simulations. */
  private Simulator simulator;

  /** Actions selected for this player during search. */
  private LinkedList<Action> selectedMyActions;

  /** This player's HP before simulation. */
  private int myOriginalHp;

  /** The opponent's HP before simulation. */
  private int oppOriginalHp;

  private FrameData frameData;
  private boolean playerNumber;
  private CommandCenter commandCenter;
  private GameData gameData;

  private boolean isCreateNode;

  Deque<Action> mAction;
  Deque<Action> oppAction;

  public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
      LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
      CommandCenter commandCenter, LinkedList<Action> selectedMyActions) {
    this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter);

    this.selectedMyActions = selectedMyActions;
  }

  public Node(FrameData frameData, Node parent, LinkedList<Action> myActions,
      LinkedList<Action> oppActions, GameData gameData, boolean playerNumber,
      CommandCenter commandCenter) {
    this.frameData = frameData;
    this.parent = parent;
    this.myActions = myActions;
    this.oppActions = oppActions;
    this.gameData = gameData;
    this.simulator = new Simulator(gameData);
    this.playerNumber = playerNumber;
    this.commandCenter = commandCenter;

    this.selectedMyActions = new LinkedList<Action>();

    this.rnd = new Random();
    this.mAction = new LinkedList<Action>();
    this.oppAction = new LinkedList<Action>();

    CharacterData myCharacter = frameData.getCharacter(playerNumber);
    CharacterData oppCharacter = frameData.getCharacter(!playerNumber);
    myOriginalHp = myCharacter.getHp();
    oppOriginalHp = oppCharacter.getHp();

    if (this.parent != null) {
      this.depth = this.parent.depth + 1;
    } else {
      this.depth = 0;
    }
  }

  /**
   * Runs MCTS.
   *
   * @return The action whose node was visited most often.
   */
  public Action mcts() {
    // Run UCT for as long as the time limit allows.
    long start = System.nanoTime();
    for (int i = 0; System.nanoTime() - start <= UCT_TIME && i < ITERATION_LIMIT; i++) {
      uct();
    }

    return getBestVisitAction();
  }

  /**
   * Runs a playout (simulation).
   *
   * @return The evaluation score of the playout.
   */
  public double playout() {

    mAction.clear();
    oppAction.clear();

    for (int i = 0; i < selectedMyActions.size(); i++) {
      mAction.add(selectedMyActions.get(i));
    }

    for (int i = 0; i < 5 - selectedMyActions.size(); i++) {
      mAction.add(myActions.get(rnd.nextInt(myActions.size())));
    }

    for (int i = 0; i < 5; i++) {
      oppAction.add(oppActions.get(rnd.nextInt(oppActions.size())));
    }

    FrameData nFrameData =
        simulator.simulate(frameData, playerNumber, mAction, oppAction, SIMULATION_TIME); // Run the simulation.

    return getScore(nFrameData);
  }

  /**
   * Runs UCT. <br>
   *
   * @return The evaluation score.
   */
  public double uct() {

    Node selectedNode = null;
    double bestUcb;

    bestUcb = -99999;

    for (Node child : this.children) {
      if (child.games == 0) {
        child.ucb = 9999 + rnd.nextInt(50);
      } else {
        child.ucb = getUcb(child.score / child.games, games, child.games);
      }


      if (bestUcb < child.ucb) {
        selectedNode = child;
        bestUcb = child.ucb;
      }

    }

    double score = 0;
    if (selectedNode.games == 0) {
      score = selectedNode.playout();
    } else {
      if (selectedNode.children == null) {
        if (selectedNode.depth < UCT_TREE_DEPTH) {
          if (UCT_CREATE_NODE_THRESHOULD <= selectedNode.games) {
            selectedNode.createNode();
            selectedNode.isCreateNode = true;
            score = selectedNode.uct();
          } else {
            score = selectedNode.playout();
          }
        } else {
          score = selectedNode.playout();
        }
      } else {
        if (selectedNode.depth < UCT_TREE_DEPTH) {
          score = selectedNode.uct();
        } else {
          selectedNode.playout();
        }
      }

    }

    selectedNode.games++;
    selectedNode.score += score;

    if (depth == 0) {
      games++;
    }

    return score;
  }

  /**
   * Creates child nodes.
   */
  public void createNode() {

    this.children = new Node[myActions.size()];

    for (int i = 0; i < children.length; i++) {

      LinkedList<Action> my = new LinkedList<Action>();
      for (Action act : selectedMyActions) {
        my.add(act);
      }

      my.add(myActions.get(i));

      children[i] =
          new Node(frameData, this, myActions, oppActions, gameData, playerNumber, commandCenter,
              my);
    }
  }

  /**
   * Returns the action of the most-visited node.
   *
   * @return The action of the most-visited node.
   */
  public Action getBestVisitAction() {

    int selected = -1;
    double bestGames = -9999;

    for (int i = 0; i < children.length; i++) {

      if (MctsAi23i.DEBUG_MODE) {
        System.out.println("評価値:" + children[i].score / children[i].games + ",試行回数:"
            + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));
      }

      if (bestGames < children[i].games) {
        bestGames = children[i].games;
        selected = i;
      }
    }

    if (MctsAi23i.DEBUG_MODE) {
      System.out.println(myActions.get(selected) + ",全試行回数:" + games);
      System.out.println("");
    }

    return this.myActions.get(selected);
  }

  /**
   * Returns the action of the highest-scoring node.
   *
   * @return The action of the highest-scoring node.
   */
  public Action getBestScoreAction() {

    int selected = -1;
    double bestScore = -9999;

    for (int i = 0; i < children.length; i++) {
    	
      if (MctsAi23i.DEBUG_MODE) {
          System.out.println("評価値:" + children[i].score / children[i].games + ",試行回数:"
              + children[i].games + ",ucb:" + children[i].ucb + ",Action:" + myActions.get(i));
      }

      double meanScore = children[i].score / children[i].games;
      if (bestScore < meanScore) {
        bestScore = meanScore;
        selected = i;
      }
    }

    if (MctsAi23i.DEBUG_MODE) {
      System.out.println(myActions.get(selected) + ",全試行回数:" + games);
      System.out.println("");
    }

    return this.myActions.get(selected);
  }

  /**
   * Returns the evaluation score.
   *
   * @param fd Frame data containing information such as HP.
   * @return The evaluation score.
   */
  public int getScore(FrameData fd) {
    return (fd.getCharacter(playerNumber).getHp() - myOriginalHp) - (fd.getCharacter(!playerNumber).getHp() - oppOriginalHp);
  }

  /**
   * Calculates the UCB1 value from the score, total playout count, and
   * playout count for the action.
   *
   * @param score Evaluation score.
   * @param n Total number of playouts.
   * @param ni Number of playouts for this action.
   * @return The UCB1 value.
   */
  public double getUcb(double score, int n, int ni) {
    return score + UCB_C * Math.sqrt((2 * Math.log(n)) / ni);
  }

  public void printNode(Node node) {
    System.out.println("全試行回数:" + node.games);
    for (int i = 0; i < node.children.length; i++) {
      System.out.println(i + ",回数:" + node.children[i].games + ",深さ:" + node.children[i].depth
          + ",score:" + node.children[i].score / node.children[i].games + ",ucb:"
          + node.children[i].ucb);
    }
    System.out.println("");
    for (int i = 0; i < node.children.length; i++) {
      if (node.children[i].isCreateNode) {
        printNode(node.children[i]);
      }
    }
  }
}