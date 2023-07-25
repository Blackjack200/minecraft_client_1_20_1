package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

public class PathFinder {
   private static final float FUDGING = 1.5F;
   private final Node[] neighbors = new Node[32];
   private final int maxVisitedNodes;
   private final NodeEvaluator nodeEvaluator;
   private static final boolean DEBUG = false;
   private final BinaryHeap openSet = new BinaryHeap();

   public PathFinder(NodeEvaluator nodeevaluator, int i) {
      this.nodeEvaluator = nodeevaluator;
      this.maxVisitedNodes = i;
   }

   @Nullable
   public Path findPath(PathNavigationRegion pathnavigationregion, Mob mob, Set<BlockPos> set, float f, int i, float f1) {
      this.openSet.clear();
      this.nodeEvaluator.prepare(pathnavigationregion, mob);
      Node node = this.nodeEvaluator.getStart();
      if (node == null) {
         return null;
      } else {
         Map<Target, BlockPos> map = set.stream().collect(Collectors.toMap((blockpos) -> this.nodeEvaluator.getGoal((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()), Function.identity()));
         Path path = this.findPath(pathnavigationregion.getProfiler(), node, map, f, i, f1);
         this.nodeEvaluator.done();
         return path;
      }
   }

   @Nullable
   private Path findPath(ProfilerFiller profilerfiller, Node node, Map<Target, BlockPos> map, float f, int i, float f1) {
      profilerfiller.push("find_path");
      profilerfiller.markForCharting(MetricCategory.PATH_FINDING);
      Set<Target> set = map.keySet();
      node.g = 0.0F;
      node.h = this.getBestH(node, set);
      node.f = node.h;
      this.openSet.clear();
      this.openSet.insert(node);
      Set<Node> set1 = ImmutableSet.of();
      int j = 0;
      Set<Target> set2 = Sets.newHashSetWithExpectedSize(set.size());
      int k = (int)((float)this.maxVisitedNodes * f1);

      while(!this.openSet.isEmpty()) {
         ++j;
         if (j >= k) {
            break;
         }

         Node node1 = this.openSet.pop();
         node1.closed = true;

         for(Target target : set) {
            if (node1.distanceManhattan(target) <= (float)i) {
               target.setReached();
               set2.add(target);
            }
         }

         if (!set2.isEmpty()) {
            break;
         }

         if (!(node1.distanceTo(node) >= f)) {
            int l = this.nodeEvaluator.getNeighbors(this.neighbors, node1);

            for(int i1 = 0; i1 < l; ++i1) {
               Node node2 = this.neighbors[i1];
               float f2 = this.distance(node1, node2);
               node2.walkedDistance = node1.walkedDistance + f2;
               float f3 = node1.g + f2 + node2.costMalus;
               if (node2.walkedDistance < f && (!node2.inOpenSet() || f3 < node2.g)) {
                  node2.cameFrom = node1;
                  node2.g = f3;
                  node2.h = this.getBestH(node2, set) * 1.5F;
                  if (node2.inOpenSet()) {
                     this.openSet.changeCost(node2, node2.g + node2.h);
                  } else {
                     node2.f = node2.g + node2.h;
                     this.openSet.insert(node2);
                  }
               }
            }
         }
      }

      Optional<Path> optional = !set2.isEmpty() ? set2.stream().map((target2) -> this.reconstructPath(target2.getBestNode(), map.get(target2), true)).min(Comparator.comparingInt(Path::getNodeCount)) : set.stream().map((target1) -> this.reconstructPath(target1.getBestNode(), map.get(target1), false)).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
      profilerfiller.pop();
      return !optional.isPresent() ? null : optional.get();
   }

   protected float distance(Node node, Node node1) {
      return node.distanceTo(node1);
   }

   private float getBestH(Node node, Set<Target> set) {
      float f = Float.MAX_VALUE;

      for(Target target : set) {
         float f1 = node.distanceTo(target);
         target.updateBest(f1, node);
         f = Math.min(f1, f);
      }

      return f;
   }

   private Path reconstructPath(Node node, BlockPos blockpos, boolean flag) {
      List<Node> list = Lists.newArrayList();
      Node node1 = node;
      list.add(0, node);

      while(node1.cameFrom != null) {
         node1 = node1.cameFrom;
         list.add(0, node1);
      }

      return new Path(list, blockpos, flag);
   }
}
