package net.minecraft.world.level.pathfinder;

import net.minecraft.network.FriendlyByteBuf;

public class Target extends Node {
   private float bestHeuristic = Float.MAX_VALUE;
   private Node bestNode;
   private boolean reached;

   public Target(Node node) {
      super(node.x, node.y, node.z);
   }

   public Target(int i, int j, int k) {
      super(i, j, k);
   }

   public void updateBest(float f, Node node) {
      if (f < this.bestHeuristic) {
         this.bestHeuristic = f;
         this.bestNode = node;
      }

   }

   public Node getBestNode() {
      return this.bestNode;
   }

   public void setReached() {
      this.reached = true;
   }

   public boolean isReached() {
      return this.reached;
   }

   public static Target createFromStream(FriendlyByteBuf friendlybytebuf) {
      Target target = new Target(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
      readContents(friendlybytebuf, target);
      return target;
   }
}
