package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BeehiveDecorator extends TreeDecorator {
   public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(BeehiveDecorator::new, (beehivedecorator) -> beehivedecorator.probability).codec();
   private static final Direction WORLDGEN_FACING = Direction.SOUTH;
   private static final Direction[] SPAWN_DIRECTIONS = Direction.Plane.HORIZONTAL.stream().filter((direction) -> direction != WORLDGEN_FACING.getOpposite()).toArray((i) -> new Direction[i]);
   private final float probability;

   public BeehiveDecorator(float f) {
      this.probability = f;
   }

   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.BEEHIVE;
   }

   public void place(TreeDecorator.Context treedecorator_context) {
      RandomSource randomsource = treedecorator_context.random();
      if (!(randomsource.nextFloat() >= this.probability)) {
         List<BlockPos> list = treedecorator_context.leaves();
         List<BlockPos> list1 = treedecorator_context.logs();
         int i = !list.isEmpty() ? Math.max(list.get(0).getY() - 1, list1.get(0).getY() + 1) : Math.min(list1.get(0).getY() + 1 + randomsource.nextInt(3), list1.get(list1.size() - 1).getY());
         List<BlockPos> list2 = list1.stream().filter((blockpos2) -> blockpos2.getY() == i).flatMap((blockpos1) -> Stream.of(SPAWN_DIRECTIONS).map(blockpos1::relative)).collect(Collectors.toList());
         if (!list2.isEmpty()) {
            Collections.shuffle(list2);
            Optional<BlockPos> optional = list2.stream().filter((blockpos) -> treedecorator_context.isAir(blockpos) && treedecorator_context.isAir(blockpos.relative(WORLDGEN_FACING))).findFirst();
            if (!optional.isEmpty()) {
               treedecorator_context.setBlock(optional.get(), Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
               treedecorator_context.level().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent((beehiveblockentity) -> {
                  int j = 2 + randomsource.nextInt(2);

                  for(int k = 0; k < j; ++k) {
                     CompoundTag compoundtag = new CompoundTag();
                     compoundtag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
                     beehiveblockentity.storeBee(compoundtag, randomsource.nextInt(599), false);
                  }

               });
            }
         }
      }
   }
}
