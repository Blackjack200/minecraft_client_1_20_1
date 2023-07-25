package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;

public abstract class NodeEvaluator {
   protected PathNavigationRegion level;
   protected Mob mob;
   protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
   protected int entityWidth;
   protected int entityHeight;
   protected int entityDepth;
   protected boolean canPassDoors;
   protected boolean canOpenDoors;
   protected boolean canFloat;
   protected boolean canWalkOverFences;

   public void prepare(PathNavigationRegion pathnavigationregion, Mob mob) {
      this.level = pathnavigationregion;
      this.mob = mob;
      this.nodes.clear();
      this.entityWidth = Mth.floor(mob.getBbWidth() + 1.0F);
      this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0F);
      this.entityDepth = Mth.floor(mob.getBbWidth() + 1.0F);
   }

   public void done() {
      this.level = null;
      this.mob = null;
   }

   protected Node getNode(BlockPos blockpos) {
      return this.getNode(blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   protected Node getNode(int i, int j, int k) {
      return this.nodes.computeIfAbsent(Node.createHash(i, j, k), (k1) -> new Node(i, j, k));
   }

   public abstract Node getStart();

   public abstract Target getGoal(double d0, double d1, double d2);

   protected Target getTargetFromNode(Node node) {
      return new Target(node);
   }

   public abstract int getNeighbors(Node[] anode, Node node);

   public abstract BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k, Mob mob);

   public abstract BlockPathTypes getBlockPathType(BlockGetter blockgetter, int i, int j, int k);

   public void setCanPassDoors(boolean flag) {
      this.canPassDoors = flag;
   }

   public void setCanOpenDoors(boolean flag) {
      this.canOpenDoors = flag;
   }

   public void setCanFloat(boolean flag) {
      this.canFloat = flag;
   }

   public void setCanWalkOverFences(boolean flag) {
      this.canWalkOverFences = flag;
   }

   public boolean canPassDoors() {
      return this.canPassDoors;
   }

   public boolean canOpenDoors() {
      return this.canOpenDoors;
   }

   public boolean canFloat() {
      return this.canFloat;
   }

   public boolean canWalkOverFences() {
      return this.canWalkOverFences;
   }
}
