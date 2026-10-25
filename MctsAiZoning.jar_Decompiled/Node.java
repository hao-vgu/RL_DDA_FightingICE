import aiinterface.CommandCenter;
import enumerate.Action;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import simulator.Simulator;
import struct.CharacterData;
import struct.FrameData;
import struct.GameData;

public class Node {
   public static final int UCT_TIME = 16500000;
   public static final int ITERATION_LIMIT = 23;
   public static final double UCB_C = 0.025;
   public static final int UCT_TREE_DEPTH = 10;
   public static final int UCT_CREATE_NODE_THRESHOULD = 10;
   public static final int SIMULATION_TIME = 60;
   private Random rnd;
   private Node parent;
   private Node[] children;
   private int depth;
   private int games;
   private double ucb;
   private double score;
   private LinkedList<Action> myActions;
   private LinkedList<Action> oppActions;
   private Simulator simulator;
   private LinkedList<Action> selectedMyActions;
   private int myOriginalHp;
   private int oppOriginalHp;
   private FrameData frameData;
   private boolean playerNumber;
   private CommandCenter commandCenter;
   private GameData gameData;
   private boolean isCreateNode;
   Deque<Action> mAction;
   Deque<Action> oppAction;
   private Map<Action, Integer> mZoningAction;
   private static int STAGE_WIDTH = 960;

   public Node(FrameData frameData, Node parent, LinkedList<Action> myActions, LinkedList<Action> oppActions, GameData gameData, boolean playerNumber, CommandCenter commandCenter, LinkedList<Action> selectedMyActions) {
      this(frameData, parent, myActions, oppActions, gameData, playerNumber, commandCenter);
      this.selectedMyActions = selectedMyActions;
   }

   public Node(FrameData frameData, Node parent, LinkedList<Action> myActions, LinkedList<Action> oppActions, GameData gameData, boolean playerNumber, CommandCenter commandCenter) {
      this.frameData = frameData;
      this.parent = parent;
      this.myActions = myActions;
      this.oppActions = oppActions;
      this.gameData = gameData;
      this.simulator = new Simulator(gameData);
      this.playerNumber = playerNumber;
      this.commandCenter = commandCenter;
      this.selectedMyActions = new LinkedList();
      this.rnd = new Random();
      this.mAction = new LinkedList();
      this.oppAction = new LinkedList();
      CharacterData myCharacter = frameData.getCharacter(playerNumber);
      CharacterData oppCharacter = frameData.getCharacter(!playerNumber);
      this.myOriginalHp = myCharacter.getHp();
      this.oppOriginalHp = oppCharacter.getHp();
      if (this.parent != null) {
         this.depth = this.parent.depth + 1;
      } else {
         this.depth = 0;
      }

      this.mZoningAction = Map.ofEntries(Map.entry(Action.STAND_D_DF_FA, 1), Map.entry(Action.STAND_D_DF_FB, 1), Map.entry(Action.THROW_A, 1), Map.entry(Action.THROW_B, 1), Map.entry(Action.BACK_STEP, 1), Map.entry(Action.BACK_JUMP, 1), Map.entry(Action.FOR_JUMP, 1), Map.entry(Action.STAND_D_DF_FC, 1));
   }

   public Action mcts() {
      long start = System.nanoTime();

      while(System.nanoTime() - start <= 16500000L) {
         this.uct();
      }

      return this.getBestVisitAction();
   }

   public double playout() {
      this.mAction.clear();
      this.oppAction.clear();

      for(int i = 0; i < this.selectedMyActions.size(); ++i) {
         this.mAction.add((Action)this.selectedMyActions.get(i));
      }

      for(int i = 0; i < 10; ++i) {
         this.mAction.add(this.getRouletteAction(this.myActions));
      }

      for(int i = 0; i < 15; ++i) {
         this.oppAction.add((Action)this.oppActions.get(this.rnd.nextInt(this.oppActions.size())));
      }

      FrameData nFrameData = this.simulator.simulate(this.frameData, this.playerNumber, this.mAction, this.oppAction, 60);
      return this.getScore(this.frameData, nFrameData, (Action)this.mAction.getLast());
   }

   public double uct() {
      Node selectedNode = null;
      double bestUcb = (double)-99999.0F;

      Node[] var7;
      for(Node child : var7 = this.children) {
         if (child.games == 0) {
            child.ucb = (double)(9999 + this.rnd.nextInt(50));
         } else {
            child.ucb = this.getUcb(child.score / (double)child.games, this.games, child.games);
         }

         if (bestUcb < child.ucb) {
            selectedNode = child;
            bestUcb = child.ucb;
         }
      }

      double score = (double)0.0F;
      if (selectedNode.games == 0) {
         score = selectedNode.playout();
      } else if (selectedNode.children == null) {
         if (selectedNode.depth < 10) {
            if (10 <= selectedNode.games) {
               selectedNode.createNode();
               selectedNode.isCreateNode = true;
               score = selectedNode.uct();
            } else {
               score = selectedNode.playout();
            }
         } else {
            score = selectedNode.playout();
         }
      } else if (selectedNode.depth < 10) {
         score = selectedNode.uct();
      } else {
         selectedNode.playout();
      }

      ++selectedNode.games;
      selectedNode.score += score;
      if (this.depth == 0) {
         ++this.games;
      }

      return score;
   }

   public void createNode() {
      this.children = new Node[this.myActions.size()];

      for(int i = 0; i < this.children.length; ++i) {
         LinkedList<Action> my = new LinkedList();

         for(Action act : this.selectedMyActions) {
            my.add(act);
         }

         my.add((Action)this.myActions.get(i));
         this.children[i] = new Node(this.frameData, this, this.myActions, this.oppActions, this.gameData, this.playerNumber, this.commandCenter, my);
      }

   }

   public Action getBestVisitAction() {
      int selected = -1;
      double bestGames = (double)-9999.0F;

      for(int i = 0; i < this.children.length; ++i) {
         if (bestGames < (double)this.children[i].games) {
            bestGames = (double)this.children[i].games;
            selected = i;
         }
      }

      return (Action)this.myActions.get(selected);
   }

   public Action getBestScoreAction() {
      int selected = -1;
      double bestScore = (double)-9999.0F;

      for(int i = 0; i < this.children.length; ++i) {
         double meanScore = this.children[i].score / (double)this.children[i].games;
         if (bestScore < meanScore) {
            bestScore = meanScore;
            selected = i;
         }
      }

      return (Action)this.myActions.get(selected);
   }

   public double getScore(FrameData before, FrameData after, Action a) {
      return this.getPersonaScore(after, a) * (double)this.getHpScore(after);
   }

   public double getPersonaScore(FrameData fd, Action a) {
      return ((double)this.checkPersonaAction(a) + this.checkPersonaDistance(fd)) / (double)3.0F;
   }

   private int getHpScore(FrameData after) {
      Node rootNode;
      for(rootNode = this; rootNode.parent != null; rootNode = rootNode.parent) {
      }

      int oppHpRoot = rootNode.oppOriginalHp;
      int oppHpRollout = after.getCharacter(!this.playerNumber).getHp();
      return oppHpRoot - oppHpRollout + 1;
   }

   private int checkPersonaAction(Action action) {
      int isZoning = (Integer)this.mZoningAction.getOrDefault(action, -1);
      return isZoning;
   }

   private double checkPersonaDistance(FrameData fd) {
      int distance = fd.getDistanceX();
      if (distance < 240) {
         return (double)-2.0F;
      } else {
         return distance >= 240 && distance < 800 ? (double)2.0F : (double)2.0F - (double)distance * (double)4.0F / (double)STAGE_WIDTH;
      }
   }

   public double getUcb(double score, int n, int ni) {
      return score + 0.025 * Math.sqrt((double)2.0F * Math.log((double)n) / (double)ni);
   }

   public void printNode(Node node) {
      System.out.println("全試行回数:" + node.games);

      for(int i = 0; i < node.children.length; ++i) {
         System.out.println(i + ",回数:" + node.children[i].games + ",深さ:" + node.children[i].depth + ",score:" + node.children[i].score / (double)node.children[i].games + ",ucb:" + node.children[i].ucb);
      }

      System.out.println("");

      for(int i = 0; i < node.children.length; ++i) {
         if (node.children[i].isCreateNode) {
            this.printNode(node.children[i]);
         }
      }

   }

   private Action getRouletteAction(LinkedList<Action> candidates) {
      double[] actFit = new double[candidates.size()];
      double totalFit = (double)0.0F;

      for(int i = 0; i < candidates.size(); ++i) {
         Action act = (Action)candidates.get(i);
         if (this.mZoningAction.containsKey(act)) {
            actFit[i] = (double)3.0F;
         } else {
            actFit[i] = (double)1.0F;
         }

         totalFit += actFit[i];
      }

      double dart = this.rnd.nextDouble();
      double wheel = (double)0.0F;
      int count = 0;

      while(dart > wheel && count < candidates.size()) {
         double normalizedFitness = actFit[count] / totalFit;
         wheel += normalizedFitness;
         if (dart > wheel) {
            ++count;
         }
      }

      if (count >= candidates.size()) {
         count = candidates.size() - 1;
      }

      return (Action)candidates.get(count);
   }
}
