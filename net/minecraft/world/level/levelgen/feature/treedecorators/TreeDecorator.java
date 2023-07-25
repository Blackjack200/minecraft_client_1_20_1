package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class TreeDecorator {
   public static final Codec<TreeDecorator> CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

   protected abstract TreeDecoratorType<?> type();

   public abstract void place(TreeDecorator.Context treedecorator_context);

   public static final class Context {
      private final LevelSimulatedReader level;
      private final BiConsumer<BlockPos, BlockState> decorationSetter;
      private final RandomSource random;
      private final ObjectArrayList<BlockPos> logs;
      private final ObjectArrayList<BlockPos> leaves;
      private final ObjectArrayList<BlockPos> roots;

      public Context(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, Set<BlockPos> set, Set<BlockPos> set1, Set<BlockPos> set2) {
         this.level = levelsimulatedreader;
         this.decorationSetter = biconsumer;
         this.random = randomsource;
         this.roots = new ObjectArrayList<>(set2);
         this.logs = new ObjectArrayList<>(set);
         this.leaves = new ObjectArrayList<>(set1);
         this.logs.sort(Comparator.comparingInt(Vec3i::getY));
         this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
         this.roots.sort(Comparator.comparingInt(Vec3i::getY));
      }

      public void placeVine(BlockPos blockpos, BooleanProperty booleanproperty) {
         this.setBlock(blockpos, Blocks.VINE.defaultBlockState().setValue(booleanproperty, Boolean.valueOf(true)));
      }

      public void setBlock(BlockPos blockpos, BlockState blockstate) {
         this.decorationSetter.accept(blockpos, blockstate);
      }

      public boolean isAir(BlockPos blockpos) {
         return this.level.isStateAtPosition(blockpos, BlockBehaviour.BlockStateBase::isAir);
      }

      public LevelSimulatedReader level() {
         return this.level;
      }

      public RandomSource random() {
         return this.random;
      }

      public ObjectArrayList<BlockPos> logs() {
         return this.logs;
      }

      public ObjectArrayList<BlockPos> leaves() {
         return this.leaves;
      }

      public ObjectArrayList<BlockPos> roots() {
         return this.roots;
      }
   }
}
