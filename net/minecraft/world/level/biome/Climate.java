package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class Climate {
   private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
   private static final float QUANTIZATION_FACTOR = 10000.0F;
   @VisibleForTesting
   protected static final int PARAMETER_COUNT = 7;

   public static Climate.TargetPoint target(float f, float f1, float f2, float f3, float f4, float f5) {
      return new Climate.TargetPoint(quantizeCoord(f), quantizeCoord(f1), quantizeCoord(f2), quantizeCoord(f3), quantizeCoord(f4), quantizeCoord(f5));
   }

   public static Climate.ParameterPoint parameters(float f, float f1, float f2, float f3, float f4, float f5, float f6) {
      return new Climate.ParameterPoint(Climate.Parameter.point(f), Climate.Parameter.point(f1), Climate.Parameter.point(f2), Climate.Parameter.point(f3), Climate.Parameter.point(f4), Climate.Parameter.point(f5), quantizeCoord(f6));
   }

   public static Climate.ParameterPoint parameters(Climate.Parameter climate_parameter, Climate.Parameter climate_parameter1, Climate.Parameter climate_parameter2, Climate.Parameter climate_parameter3, Climate.Parameter climate_parameter4, Climate.Parameter climate_parameter5, float f) {
      return new Climate.ParameterPoint(climate_parameter, climate_parameter1, climate_parameter2, climate_parameter3, climate_parameter4, climate_parameter5, quantizeCoord(f));
   }

   public static long quantizeCoord(float f) {
      return (long)(f * 10000.0F);
   }

   public static float unquantizeCoord(long i) {
      return (float)i / 10000.0F;
   }

   public static Climate.Sampler empty() {
      DensityFunction densityfunction = DensityFunctions.zero();
      return new Climate.Sampler(densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, densityfunction, List.of());
   }

   public static BlockPos findSpawnPosition(List<Climate.ParameterPoint> list, Climate.Sampler climate_sampler) {
      return (new Climate.SpawnFinder(list, climate_sampler)).result.location();
   }

   interface DistanceMetric<T> {
      long distance(Climate.RTree.Node<T> climate_rtree_node, long[] along);
   }

   public static record Parameter(long min, long max) {
      public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(Codec.floatRange(-2.0F, 2.0F), "min", "max", (ofloat, ofloat1) -> ofloat.compareTo(ofloat1) > 0 ? DataResult.error(() -> "Cannon construct interval, min > max (" + ofloat + " > " + ofloat1 + ")") : DataResult.success(new Climate.Parameter(Climate.quantizeCoord(ofloat), Climate.quantizeCoord(ofloat1))), (climate_parameter) -> Climate.unquantizeCoord(climate_parameter.min()), (climate_parameter) -> Climate.unquantizeCoord(climate_parameter.max()));

      public static Climate.Parameter point(float f) {
         return span(f, f);
      }

      public static Climate.Parameter span(float f, float f1) {
         if (f > f1) {
            throw new IllegalArgumentException("min > max: " + f + " " + f1);
         } else {
            return new Climate.Parameter(Climate.quantizeCoord(f), Climate.quantizeCoord(f1));
         }
      }

      public static Climate.Parameter span(Climate.Parameter climate_parameter, Climate.Parameter climate_parameter1) {
         if (climate_parameter.min() > climate_parameter1.max()) {
            throw new IllegalArgumentException("min > max: " + climate_parameter + " " + climate_parameter1);
         } else {
            return new Climate.Parameter(climate_parameter.min(), climate_parameter1.max());
         }
      }

      public String toString() {
         return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
      }

      public long distance(long i) {
         long j = i - this.max;
         long k = this.min - i;
         return j > 0L ? j : Math.max(k, 0L);
      }

      public long distance(Climate.Parameter climate_parameter) {
         long i = climate_parameter.min() - this.max;
         long j = this.min - climate_parameter.max();
         return i > 0L ? i : Math.max(j, 0L);
      }

      public Climate.Parameter span(@Nullable Climate.Parameter climate_parameter) {
         return climate_parameter == null ? this : new Climate.Parameter(Math.min(this.min, climate_parameter.min()), Math.max(this.max, climate_parameter.max()));
      }
   }

   public static class ParameterList<T> {
      private final List<Pair<Climate.ParameterPoint, T>> values;
      private final Climate.RTree<T> index;

      public static <T> Codec<Climate.ParameterList<T>> codec(MapCodec<T> mapcodec) {
         return ExtraCodecs.nonEmptyList(RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), mapcodec.forGetter(Pair::getSecond)).apply(recordcodecbuilder_instance, Pair::of)).listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values);
      }

      public ParameterList(List<Pair<Climate.ParameterPoint, T>> list) {
         this.values = list;
         this.index = Climate.RTree.create(list);
      }

      public List<Pair<Climate.ParameterPoint, T>> values() {
         return this.values;
      }

      public T findValue(Climate.TargetPoint climate_targetpoint) {
         return this.findValueIndex(climate_targetpoint);
      }

      @VisibleForTesting
      public T findValueBruteForce(Climate.TargetPoint climate_targetpoint) {
         Iterator<Pair<Climate.ParameterPoint, T>> iterator = this.values().iterator();
         Pair<Climate.ParameterPoint, T> pair = iterator.next();
         long i = pair.getFirst().fitness(climate_targetpoint);
         T object = pair.getSecond();

         while(iterator.hasNext()) {
            Pair<Climate.ParameterPoint, T> pair1 = iterator.next();
            long j = pair1.getFirst().fitness(climate_targetpoint);
            if (j < i) {
               i = j;
               object = pair1.getSecond();
            }
         }

         return object;
      }

      public T findValueIndex(Climate.TargetPoint climate_targetpoint) {
         return this.findValueIndex(climate_targetpoint, Climate.RTree.Node::distance);
      }

      protected T findValueIndex(Climate.TargetPoint climate_targetpoint, Climate.DistanceMetric<T> climate_distancemetric) {
         return this.index.search(climate_targetpoint, climate_distancemetric);
      }
   }

   public static record ParameterPoint(Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion, Climate.Parameter depth, Climate.Parameter weirdness, long offset) {
      public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Climate.Parameter.CODEC.fieldOf("temperature").forGetter((climate_parameterpoint6) -> climate_parameterpoint6.temperature), Climate.Parameter.CODEC.fieldOf("humidity").forGetter((climate_parameterpoint5) -> climate_parameterpoint5.humidity), Climate.Parameter.CODEC.fieldOf("continentalness").forGetter((climate_parameterpoint4) -> climate_parameterpoint4.continentalness), Climate.Parameter.CODEC.fieldOf("erosion").forGetter((climate_parameterpoint3) -> climate_parameterpoint3.erosion), Climate.Parameter.CODEC.fieldOf("depth").forGetter((climate_parameterpoint2) -> climate_parameterpoint2.depth), Climate.Parameter.CODEC.fieldOf("weirdness").forGetter((climate_parameterpoint1) -> climate_parameterpoint1.weirdness), Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter((climate_parameterpoint) -> climate_parameterpoint.offset)).apply(recordcodecbuilder_instance, Climate.ParameterPoint::new));

      long fitness(Climate.TargetPoint climate_targetpoint) {
         return Mth.square(this.temperature.distance(climate_targetpoint.temperature)) + Mth.square(this.humidity.distance(climate_targetpoint.humidity)) + Mth.square(this.continentalness.distance(climate_targetpoint.continentalness)) + Mth.square(this.erosion.distance(climate_targetpoint.erosion)) + Mth.square(this.depth.distance(climate_targetpoint.depth)) + Mth.square(this.weirdness.distance(climate_targetpoint.weirdness)) + Mth.square(this.offset);
      }

      protected List<Climate.Parameter> parameterSpace() {
         return ImmutableList.of(this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new Climate.Parameter(this.offset, this.offset));
      }
   }

   protected static final class RTree<T> {
      private static final int CHILDREN_PER_NODE = 6;
      private final Climate.RTree.Node<T> root;
      private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

      private RTree(Climate.RTree.Node<T> climate_rtree_node) {
         this.root = climate_rtree_node;
      }

      public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, T>> list) {
         if (list.isEmpty()) {
            throw new IllegalArgumentException("Need at least one value to build the search tree.");
         } else {
            int i = list.get(0).getFirst().parameterSpace().size();
            if (i != 7) {
               throw new IllegalStateException("Expecting parameter space to be 7, got " + i);
            } else {
               List<Climate.RTree.Leaf<T>> list1 = list.stream().map((pair) -> new Climate.RTree.Leaf(pair.getFirst(), pair.getSecond())).collect(Collectors.toCollection(ArrayList::new));
               return new Climate.RTree<>(build(i, list1));
            }
         }
      }

      private static <T> Climate.RTree.Node<T> build(int i, List<? extends Climate.RTree.Node<T>> list) {
         if (list.isEmpty()) {
            throw new IllegalStateException("Need at least one child to build a node");
         } else if (list.size() == 1) {
            return list.get(0);
         } else if (list.size() <= 6) {
            list.sort(Comparator.comparingLong((climate_rtree_node) -> {
               long l1 = 0L;

               for(int i2 = 0; i2 < i; ++i2) {
                  Climate.Parameter climate_parameter = climate_rtree_node.parameterSpace[i2];
                  l1 += Math.abs((climate_parameter.min() + climate_parameter.max()) / 2L);
               }

               return l1;
            }));
            return new Climate.RTree.SubTree<>(list);
         } else {
            long j = Long.MAX_VALUE;
            int k = -1;
            List<Climate.RTree.SubTree<T>> list1 = null;

            for(int l = 0; l < i; ++l) {
               sort(list, i, l, false);
               List<Climate.RTree.SubTree<T>> list2 = bucketize(list);
               long i1 = 0L;

               for(Climate.RTree.SubTree<T> climate_rtree_subtree : list2) {
                  i1 += cost(climate_rtree_subtree.parameterSpace);
               }

               if (j > i1) {
                  j = i1;
                  k = l;
                  list1 = list2;
               }
            }

            sort(list1, i, k, true);
            return new Climate.RTree.SubTree<>(list1.stream().map((climate_rtree_subtree1) -> build(i, Arrays.asList(climate_rtree_subtree1.children))).collect(Collectors.toList()));
         }
      }

      private static <T> void sort(List<? extends Climate.RTree.Node<T>> list, int i, int j, boolean flag) {
         Comparator<Climate.RTree.Node<T>> comparator = comparator(j, flag);

         for(int k = 1; k < i; ++k) {
            comparator = comparator.thenComparing(comparator((j + k) % i, flag));
         }

         list.sort(comparator);
      }

      private static <T> Comparator<Climate.RTree.Node<T>> comparator(int i, boolean flag) {
         return Comparator.comparingLong((climate_rtree_node) -> {
            Climate.Parameter climate_parameter = climate_rtree_node.parameterSpace[i];
            long k = (climate_parameter.min() + climate_parameter.max()) / 2L;
            return flag ? Math.abs(k) : k;
         });
      }

      private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> list) {
         List<Climate.RTree.SubTree<T>> list1 = Lists.newArrayList();
         List<Climate.RTree.Node<T>> list2 = Lists.newArrayList();
         int i = (int)Math.pow(6.0D, Math.floor(Math.log((double)list.size() - 0.01D) / Math.log(6.0D)));

         for(Climate.RTree.Node<T> climate_rtree_node : list) {
            list2.add(climate_rtree_node);
            if (list2.size() >= i) {
               list1.add(new Climate.RTree.SubTree<>(list2));
               list2 = Lists.newArrayList();
            }
         }

         if (!list2.isEmpty()) {
            list1.add(new Climate.RTree.SubTree<>(list2));
         }

         return list1;
      }

      private static long cost(Climate.Parameter[] aclimate_parameter) {
         long i = 0L;

         for(Climate.Parameter climate_parameter : aclimate_parameter) {
            i += Math.abs(climate_parameter.max() - climate_parameter.min());
         }

         return i;
      }

      static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> list) {
         if (list.isEmpty()) {
            throw new IllegalArgumentException("SubTree needs at least one child");
         } else {
            int i = 7;
            List<Climate.Parameter> list1 = Lists.newArrayList();

            for(int j = 0; j < 7; ++j) {
               list1.add((Climate.Parameter)null);
            }

            for(Climate.RTree.Node<T> climate_rtree_node : list) {
               for(int k = 0; k < 7; ++k) {
                  list1.set(k, climate_rtree_node.parameterSpace[k].span(list1.get(k)));
               }
            }

            return list1;
         }
      }

      public T search(Climate.TargetPoint climate_targetpoint, Climate.DistanceMetric<T> climate_distancemetric) {
         long[] along = climate_targetpoint.toParameterArray();
         Climate.RTree.Leaf<T> climate_rtree_leaf = this.root.search(along, this.lastResult.get(), climate_distancemetric);
         this.lastResult.set(climate_rtree_leaf);
         return climate_rtree_leaf.value;
      }

      static final class Leaf<T> extends Climate.RTree.Node<T> {
         final T value;

         Leaf(Climate.ParameterPoint climate_parameterpoint, T object) {
            super(climate_parameterpoint.parameterSpace());
            this.value = object;
         }

         protected Climate.RTree.Leaf<T> search(long[] along, @Nullable Climate.RTree.Leaf<T> climate_rtree_leaf, Climate.DistanceMetric<T> climate_distancemetric) {
            return this;
         }
      }

      abstract static class Node<T> {
         protected final Climate.Parameter[] parameterSpace;

         protected Node(List<Climate.Parameter> list) {
            this.parameterSpace = list.toArray(new Climate.Parameter[0]);
         }

         protected abstract Climate.RTree.Leaf<T> search(long[] along, @Nullable Climate.RTree.Leaf<T> climate_rtree_leaf, Climate.DistanceMetric<T> climate_distancemetric);

         protected long distance(long[] along) {
            long i = 0L;

            for(int j = 0; j < 7; ++j) {
               i += Mth.square(this.parameterSpace[j].distance(along[j]));
            }

            return i;
         }

         public String toString() {
            return Arrays.toString((Object[])this.parameterSpace);
         }
      }

      static final class SubTree<T> extends Climate.RTree.Node<T> {
         final Climate.RTree.Node<T>[] children;

         protected SubTree(List<? extends Climate.RTree.Node<T>> list) {
            this(Climate.RTree.buildParameterSpace(list), list);
         }

         protected SubTree(List<Climate.Parameter> list, List<? extends Climate.RTree.Node<T>> list1) {
            super(list);
            this.children = list1.toArray(new Climate.RTree.Node[0]);
         }

         protected Climate.RTree.Leaf<T> search(long[] along, @Nullable Climate.RTree.Leaf<T> climate_rtree_leaf, Climate.DistanceMetric<T> climate_distancemetric) {
            long i = climate_rtree_leaf == null ? Long.MAX_VALUE : climate_distancemetric.distance(climate_rtree_leaf, along);
            Climate.RTree.Leaf<T> climate_rtree_leaf1 = climate_rtree_leaf;

            for(Climate.RTree.Node<T> climate_rtree_node : this.children) {
               long j = climate_distancemetric.distance(climate_rtree_node, along);
               if (i > j) {
                  Climate.RTree.Leaf<T> climate_rtree_leaf2 = climate_rtree_node.search(along, climate_rtree_leaf1, climate_distancemetric);
                  long k = climate_rtree_node == climate_rtree_leaf2 ? j : climate_distancemetric.distance(climate_rtree_leaf2, along);
                  if (i > k) {
                     i = k;
                     climate_rtree_leaf1 = climate_rtree_leaf2;
                  }
               }
            }

            return climate_rtree_leaf1;
         }
      }
   }

   public static record Sampler(DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, List<Climate.ParameterPoint> spawnTarget) {
      public Climate.TargetPoint sample(int i, int j, int k) {
         int l = QuartPos.toBlock(i);
         int i1 = QuartPos.toBlock(j);
         int j1 = QuartPos.toBlock(k);
         DensityFunction.SinglePointContext densityfunction_singlepointcontext = new DensityFunction.SinglePointContext(l, i1, j1);
         return Climate.target((float)this.temperature.compute(densityfunction_singlepointcontext), (float)this.humidity.compute(densityfunction_singlepointcontext), (float)this.continentalness.compute(densityfunction_singlepointcontext), (float)this.erosion.compute(densityfunction_singlepointcontext), (float)this.depth.compute(densityfunction_singlepointcontext), (float)this.weirdness.compute(densityfunction_singlepointcontext));
      }

      public BlockPos findSpawnPosition() {
         return this.spawnTarget.isEmpty() ? BlockPos.ZERO : Climate.findSpawnPosition(this.spawnTarget, this);
      }
   }

   static class SpawnFinder {
      Climate.SpawnFinder.Result result;

      SpawnFinder(List<Climate.ParameterPoint> list, Climate.Sampler climate_sampler) {
         this.result = getSpawnPositionAndFitness(list, climate_sampler, 0, 0);
         this.radialSearch(list, climate_sampler, 2048.0F, 512.0F);
         this.radialSearch(list, climate_sampler, 512.0F, 32.0F);
      }

      private void radialSearch(List<Climate.ParameterPoint> list, Climate.Sampler climate_sampler, float f, float f1) {
         float f2 = 0.0F;
         float f3 = f1;
         BlockPos blockpos = this.result.location();

         while(f3 <= f) {
            int i = blockpos.getX() + (int)(Math.sin((double)f2) * (double)f3);
            int j = blockpos.getZ() + (int)(Math.cos((double)f2) * (double)f3);
            Climate.SpawnFinder.Result climate_spawnfinder_result = getSpawnPositionAndFitness(list, climate_sampler, i, j);
            if (climate_spawnfinder_result.fitness() < this.result.fitness()) {
               this.result = climate_spawnfinder_result;
            }

            f2 += f1 / f3;
            if ((double)f2 > (Math.PI * 2D)) {
               f2 = 0.0F;
               f3 += f1;
            }
         }

      }

      private static Climate.SpawnFinder.Result getSpawnPositionAndFitness(List<Climate.ParameterPoint> list, Climate.Sampler climate_sampler, int i, int j) {
         double d0 = Mth.square(2500.0D);
         int k = 2;
         long l = (long)((double)Mth.square(10000.0F) * Math.pow((double)(Mth.square((long)i) + Mth.square((long)j)) / d0, 2.0D));
         Climate.TargetPoint climate_targetpoint = climate_sampler.sample(QuartPos.fromBlock(i), 0, QuartPos.fromBlock(j));
         Climate.TargetPoint climate_targetpoint1 = new Climate.TargetPoint(climate_targetpoint.temperature(), climate_targetpoint.humidity(), climate_targetpoint.continentalness(), climate_targetpoint.erosion(), 0L, climate_targetpoint.weirdness());
         long i1 = Long.MAX_VALUE;

         for(Climate.ParameterPoint climate_parameterpoint : list) {
            i1 = Math.min(i1, climate_parameterpoint.fitness(climate_targetpoint1));
         }

         return new Climate.SpawnFinder.Result(new BlockPos(i, 0, j), l + i1);
      }

      static record Result(BlockPos location, long fitness) {
      }
   }

   public static record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {
      final long temperature;
      final long humidity;
      final long continentalness;
      final long erosion;
      final long depth;
      final long weirdness;

      @VisibleForTesting
      protected long[] toParameterArray() {
         return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
      }
   }
}
