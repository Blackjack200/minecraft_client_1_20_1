package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Path {
   private final List<Node> nodes;
   private Node[] openSet = new Node[0];
   private Node[] closedSet = new Node[0];
   @Nullable
   private Set<Target> targetNodes;
   private int nextNodeIndex;
   private final BlockPos target;
   private final float distToTarget;
   private final boolean reached;

   public Path(List<Node> list, BlockPos blockpos, boolean flag) {
      this.nodes = list;
      this.target = blockpos;
      this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).distanceManhattan(this.target);
      this.reached = flag;
   }

   public void advance() {
      ++this.nextNodeIndex;
   }

   public boolean notStarted() {
      return this.nextNodeIndex <= 0;
   }

   public boolean isDone() {
      return this.nextNodeIndex >= this.nodes.size();
   }

   @Nullable
   public Node getEndNode() {
      return !this.nodes.isEmpty() ? this.nodes.get(this.nodes.size() - 1) : null;
   }

   public Node getNode(int i) {
      return this.nodes.get(i);
   }

   public void truncateNodes(int i) {
      if (this.nodes.size() > i) {
         this.nodes.subList(i, this.nodes.size()).clear();
      }

   }

   public void replaceNode(int i, Node node) {
      this.nodes.set(i, node);
   }

   public int getNodeCount() {
      return this.nodes.size();
   }

   public int getNextNodeIndex() {
      return this.nextNodeIndex;
   }

   public void setNextNodeIndex(int i) {
      this.nextNodeIndex = i;
   }

   public Vec3 getEntityPosAtNode(Entity entity, int i) {
      Node node = this.nodes.get(i);
      double d0 = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5D;
      double d1 = (double)node.y;
      double d2 = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5D;
      return new Vec3(d0, d1, d2);
   }

   public BlockPos getNodePos(int i) {
      return this.nodes.get(i).asBlockPos();
   }

   public Vec3 getNextEntityPos(Entity entity) {
      return this.getEntityPosAtNode(entity, this.nextNodeIndex);
   }

   public BlockPos getNextNodePos() {
      return this.nodes.get(this.nextNodeIndex).asBlockPos();
   }

   public Node getNextNode() {
      return this.nodes.get(this.nextNodeIndex);
   }

   @Nullable
   public Node getPreviousNode() {
      return this.nextNodeIndex > 0 ? this.nodes.get(this.nextNodeIndex - 1) : null;
   }

   public boolean sameAs(@Nullable Path path) {
      if (path == null) {
         return false;
      } else if (path.nodes.size() != this.nodes.size()) {
         return false;
      } else {
         for(int i = 0; i < this.nodes.size(); ++i) {
            Node node = this.nodes.get(i);
            Node node1 = path.nodes.get(i);
            if (node.x != node1.x || node.y != node1.y || node.z != node1.z) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean canReach() {
      return this.reached;
   }

   @VisibleForDebug
   void setDebug(Node[] anode, Node[] anode1, Set<Target> set) {
      this.openSet = anode;
      this.closedSet = anode1;
      this.targetNodes = set;
   }

   @VisibleForDebug
   public Node[] getOpenSet() {
      return this.openSet;
   }

   @VisibleForDebug
   public Node[] getClosedSet() {
      return this.closedSet;
   }

   public void writeToStream(FriendlyByteBuf friendlybytebuf) {
      if (this.targetNodes != null && !this.targetNodes.isEmpty()) {
         friendlybytebuf.writeBoolean(this.reached);
         friendlybytebuf.writeInt(this.nextNodeIndex);
         friendlybytebuf.writeInt(this.targetNodes.size());
         this.targetNodes.forEach((target) -> target.writeToStream(friendlybytebuf));
         friendlybytebuf.writeInt(this.target.getX());
         friendlybytebuf.writeInt(this.target.getY());
         friendlybytebuf.writeInt(this.target.getZ());
         friendlybytebuf.writeInt(this.nodes.size());

         for(Node node : this.nodes) {
            node.writeToStream(friendlybytebuf);
         }

         friendlybytebuf.writeInt(this.openSet.length);

         for(Node node1 : this.openSet) {
            node1.writeToStream(friendlybytebuf);
         }

         friendlybytebuf.writeInt(this.closedSet.length);

         for(Node node2 : this.closedSet) {
            node2.writeToStream(friendlybytebuf);
         }

      }
   }

   public static Path createFromStream(FriendlyByteBuf friendlybytebuf) {
      boolean flag = friendlybytebuf.readBoolean();
      int i = friendlybytebuf.readInt();
      int j = friendlybytebuf.readInt();
      Set<Target> set = Sets.newHashSet();

      for(int k = 0; k < j; ++k) {
         set.add(Target.createFromStream(friendlybytebuf));
      }

      BlockPos blockpos = new BlockPos(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
      List<Node> list = Lists.newArrayList();
      int l = friendlybytebuf.readInt();

      for(int i1 = 0; i1 < l; ++i1) {
         list.add(Node.createFromStream(friendlybytebuf));
      }

      Node[] anode = new Node[friendlybytebuf.readInt()];

      for(int j1 = 0; j1 < anode.length; ++j1) {
         anode[j1] = Node.createFromStream(friendlybytebuf);
      }

      Node[] anode1 = new Node[friendlybytebuf.readInt()];

      for(int k1 = 0; k1 < anode1.length; ++k1) {
         anode1[k1] = Node.createFromStream(friendlybytebuf);
      }

      Path path = new Path(list, blockpos, flag);
      path.openSet = anode;
      path.closedSet = anode1;
      path.targetNodes = set;
      path.nextNodeIndex = i;
      return path;
   }

   public String toString() {
      return "Path(length=" + this.nodes.size() + ")";
   }

   public BlockPos getTarget() {
      return this.target;
   }

   public float getDistToTarget() {
      return this.distToTarget;
   }
}
