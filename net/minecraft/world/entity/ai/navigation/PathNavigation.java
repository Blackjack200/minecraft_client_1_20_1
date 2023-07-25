package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation {
   private static final int MAX_TIME_RECOMPUTE = 20;
   private static final int STUCK_CHECK_INTERVAL = 100;
   private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;
   protected final Mob mob;
   protected final Level level;
   @Nullable
   protected Path path;
   protected double speedModifier;
   protected int tick;
   protected int lastStuckCheck;
   protected Vec3 lastStuckCheckPos = Vec3.ZERO;
   protected Vec3i timeoutCachedNode = Vec3i.ZERO;
   protected long timeoutTimer;
   protected long lastTimeoutCheck;
   protected double timeoutLimit;
   protected float maxDistanceToWaypoint = 0.5F;
   protected boolean hasDelayedRecomputation;
   protected long timeLastRecompute;
   protected NodeEvaluator nodeEvaluator;
   @Nullable
   private BlockPos targetPos;
   private int reachRange;
   private float maxVisitedNodesMultiplier = 1.0F;
   private final PathFinder pathFinder;
   private boolean isStuck;

   public PathNavigation(Mob mob, Level level) {
      this.mob = mob;
      this.level = level;
      int i = Mth.floor(mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
      this.pathFinder = this.createPathFinder(i);
   }

   public void resetMaxVisitedNodesMultiplier() {
      this.maxVisitedNodesMultiplier = 1.0F;
   }

   public void setMaxVisitedNodesMultiplier(float f) {
      this.maxVisitedNodesMultiplier = f;
   }

   @Nullable
   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   protected abstract PathFinder createPathFinder(int i);

   public void setSpeedModifier(double d0) {
      this.speedModifier = d0;
   }

   public void recomputePath() {
      if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
         if (this.targetPos != null) {
            this.path = null;
            this.path = this.createPath(this.targetPos, this.reachRange);
            this.timeLastRecompute = this.level.getGameTime();
            this.hasDelayedRecomputation = false;
         }
      } else {
         this.hasDelayedRecomputation = true;
      }

   }

   @Nullable
   public final Path createPath(double d0, double d1, double d2, int i) {
      return this.createPath(BlockPos.containing(d0, d1, d2), i);
   }

   @Nullable
   public Path createPath(Stream<BlockPos> stream, int i) {
      return this.createPath(stream.collect(Collectors.toSet()), 8, false, i);
   }

   @Nullable
   public Path createPath(Set<BlockPos> set, int i) {
      return this.createPath(set, 8, false, i);
   }

   @Nullable
   public Path createPath(BlockPos blockpos, int i) {
      return this.createPath(ImmutableSet.of(blockpos), 8, false, i);
   }

   @Nullable
   public Path createPath(BlockPos blockpos, int i, int j) {
      return this.createPath(ImmutableSet.of(blockpos), 8, false, i, (float)j);
   }

   @Nullable
   public Path createPath(Entity entity, int i) {
      return this.createPath(ImmutableSet.of(entity.blockPosition()), 16, true, i);
   }

   @Nullable
   protected Path createPath(Set<BlockPos> set, int i, boolean flag, int j) {
      return this.createPath(set, i, flag, j, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
   }

   @Nullable
   protected Path createPath(Set<BlockPos> set, int i, boolean flag, int j, float f) {
      if (set.isEmpty()) {
         return null;
      } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
         return null;
      } else if (!this.canUpdatePath()) {
         return null;
      } else if (this.path != null && !this.path.isDone() && set.contains(this.targetPos)) {
         return this.path;
      } else {
         this.level.getProfiler().push("pathfind");
         BlockPos blockpos = flag ? this.mob.blockPosition().above() : this.mob.blockPosition();
         int k = (int)(f + (float)i);
         PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-k, -k, -k), blockpos.offset(k, k, k));
         Path path = this.pathFinder.findPath(pathnavigationregion, this.mob, set, f, j, this.maxVisitedNodesMultiplier);
         this.level.getProfiler().pop();
         if (path != null && path.getTarget() != null) {
            this.targetPos = path.getTarget();
            this.reachRange = j;
            this.resetStuckTimeout();
         }

         return path;
      }
   }

   public boolean moveTo(double d0, double d1, double d2, double d3) {
      return this.moveTo(this.createPath(d0, d1, d2, 1), d3);
   }

   public boolean moveTo(Entity entity, double d0) {
      Path path = this.createPath(entity, 1);
      return path != null && this.moveTo(path, d0);
   }

   public boolean moveTo(@Nullable Path path, double d0) {
      if (path == null) {
         this.path = null;
         return false;
      } else {
         if (!path.sameAs(this.path)) {
            this.path = path;
         }

         if (this.isDone()) {
            return false;
         } else {
            this.trimPath();
            if (this.path.getNodeCount() <= 0) {
               return false;
            } else {
               this.speedModifier = d0;
               Vec3 vec3 = this.getTempMobPos();
               this.lastStuckCheck = this.tick;
               this.lastStuckCheckPos = vec3;
               return true;
            }
         }
      }
   }

   @Nullable
   public Path getPath() {
      return this.path;
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec3 = this.getTempMobPos();
            Vec3 vec31 = this.path.getNextEntityPos(this.mob);
            if (vec3.y > vec31.y && !this.mob.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
               this.path.advance();
            }
         }

         DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vec3 vec32 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition(vec32.x, this.getGroundY(vec32), vec32.z, this.speedModifier);
         }
      }
   }

   protected double getGroundY(Vec3 vec3) {
      BlockPos blockpos = BlockPos.containing(vec3);
      return this.level.getBlockState(blockpos.below()).isAir() ? vec3.y : WalkNodeEvaluator.getFloorLevel(this.level, blockpos);
   }

   protected void followThePath() {
      Vec3 vec3 = this.getTempMobPos();
      this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
      Vec3i vec3i = this.path.getNextNodePos();
      double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + 0.5D));
      double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
      double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + 0.5D));
      boolean flag = d0 < (double)this.maxDistanceToWaypoint && d2 < (double)this.maxDistanceToWaypoint && d1 < 1.0D;
      if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
         this.path.advance();
      }

      this.doStuckDetection(vec3);
   }

   private boolean shouldTargetNextNodeInDirection(Vec3 vec3) {
      if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
         return false;
      } else {
         Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
         if (!vec3.closerThan(vec31, 2.0D)) {
            return false;
         } else if (this.canMoveDirectly(vec3, this.path.getNextEntityPos(this.mob))) {
            return true;
         } else {
            Vec3 vec32 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
            Vec3 vec33 = vec31.subtract(vec3);
            Vec3 vec34 = vec32.subtract(vec3);
            double d0 = vec33.lengthSqr();
            double d1 = vec34.lengthSqr();
            boolean flag = d1 < d0;
            boolean flag1 = d0 < 0.5D;
            if (!flag && !flag1) {
               return false;
            } else {
               Vec3 vec35 = vec33.normalize();
               Vec3 vec36 = vec34.normalize();
               return vec36.dot(vec35) < 0.0D;
            }
         }
      }
   }

   protected void doStuckDetection(Vec3 vec3) {
      if (this.tick - this.lastStuckCheck > 100) {
         float f = this.mob.getSpeed() >= 1.0F ? this.mob.getSpeed() : this.mob.getSpeed() * this.mob.getSpeed();
         float f1 = f * 100.0F * 0.25F;
         if (vec3.distanceToSqr(this.lastStuckCheckPos) < (double)(f1 * f1)) {
            this.isStuck = true;
            this.stop();
         } else {
            this.isStuck = false;
         }

         this.lastStuckCheck = this.tick;
         this.lastStuckCheckPos = vec3;
      }

      if (this.path != null && !this.path.isDone()) {
         Vec3i vec3i = this.path.getNextNodePos();
         long i = this.level.getGameTime();
         if (vec3i.equals(this.timeoutCachedNode)) {
            this.timeoutTimer += i - this.lastTimeoutCheck;
         } else {
            this.timeoutCachedNode = vec3i;
            double d0 = vec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
            this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double)this.mob.getSpeed() * 20.0D : 0.0D;
         }

         if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
            this.timeoutPath();
         }

         this.lastTimeoutCheck = i;
      }

   }

   private void timeoutPath() {
      this.resetStuckTimeout();
      this.stop();
   }

   private void resetStuckTimeout() {
      this.timeoutCachedNode = Vec3i.ZERO;
      this.timeoutTimer = 0L;
      this.timeoutLimit = 0.0D;
      this.isStuck = false;
   }

   public boolean isDone() {
      return this.path == null || this.path.isDone();
   }

   public boolean isInProgress() {
      return !this.isDone();
   }

   public void stop() {
      this.path = null;
   }

   protected abstract Vec3 getTempMobPos();

   protected abstract boolean canUpdatePath();

   protected boolean isInLiquid() {
      return this.mob.isInWaterOrBubble() || this.mob.isInLava();
   }

   protected void trimPath() {
      if (this.path != null) {
         for(int i = 0; i < this.path.getNodeCount(); ++i) {
            Node node = this.path.getNode(i);
            Node node1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
            BlockState blockstate = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
            if (blockstate.is(BlockTags.CAULDRONS)) {
               this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
               if (node1 != null && node.y >= node1.y) {
                  this.path.replaceNode(i + 1, node.cloneAndMove(node1.x, node.y + 1, node1.z));
               }
            }
         }

      }
   }

   protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec31) {
      return false;
   }

   public boolean canCutCorner(BlockPathTypes blockpathtypes) {
      return blockpathtypes != BlockPathTypes.DANGER_FIRE && blockpathtypes != BlockPathTypes.DANGER_OTHER && blockpathtypes != BlockPathTypes.WALKABLE_DOOR;
   }

   protected static boolean isClearForMovementBetween(Mob mob, Vec3 vec3, Vec3 vec31, boolean flag) {
      Vec3 vec32 = new Vec3(vec31.x, vec31.y + (double)mob.getBbHeight() * 0.5D, vec31.z);
      return mob.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, flag ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, mob)).getType() == HitResult.Type.MISS;
   }

   public boolean isStableDestination(BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      return this.level.getBlockState(blockpos1).isSolidRender(this.level, blockpos1);
   }

   public NodeEvaluator getNodeEvaluator() {
      return this.nodeEvaluator;
   }

   public void setCanFloat(boolean flag) {
      this.nodeEvaluator.setCanFloat(flag);
   }

   public boolean canFloat() {
      return this.nodeEvaluator.canFloat();
   }

   public boolean shouldRecomputePath(BlockPos blockpos) {
      if (this.hasDelayedRecomputation) {
         return false;
      } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
         Node node = this.path.getEndNode();
         Vec3 vec3 = new Vec3(((double)node.x + this.mob.getX()) / 2.0D, ((double)node.y + this.mob.getY()) / 2.0D, ((double)node.z + this.mob.getZ()) / 2.0D);
         return blockpos.closerToCenterThan(vec3, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()));
      } else {
         return false;
      }
   }

   public float getMaxDistanceToWaypoint() {
      return this.maxDistanceToWaypoint;
   }

   public boolean isStuck() {
      return this.isStuck;
   }
}
