package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
   private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

   public MonsterRoomFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
      BlockPos blockpos = featureplacecontext.origin();
      RandomSource randomsource = featureplacecontext.random();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      int i = 3;
      int j = randomsource.nextInt(2) + 2;
      int k = -j - 1;
      int l = j + 1;
      int i1 = -1;
      int j1 = 4;
      int k1 = randomsource.nextInt(2) + 2;
      int l1 = -k1 - 1;
      int i2 = k1 + 1;
      int j2 = 0;

      for(int k2 = k; k2 <= l; ++k2) {
         for(int l2 = -1; l2 <= 4; ++l2) {
            for(int i3 = l1; i3 <= i2; ++i3) {
               BlockPos blockpos1 = blockpos.offset(k2, l2, i3);
               boolean flag = worldgenlevel.getBlockState(blockpos1).isSolid();
               if (l2 == -1 && !flag) {
                  return false;
               }

               if (l2 == 4 && !flag) {
                  return false;
               }

               if ((k2 == k || k2 == l || i3 == l1 || i3 == i2) && l2 == 0 && worldgenlevel.isEmptyBlock(blockpos1) && worldgenlevel.isEmptyBlock(blockpos1.above())) {
                  ++j2;
               }
            }
         }
      }

      if (j2 >= 1 && j2 <= 5) {
         for(int j3 = k; j3 <= l; ++j3) {
            for(int k3 = 3; k3 >= -1; --k3) {
               for(int l3 = l1; l3 <= i2; ++l3) {
                  BlockPos blockpos2 = blockpos.offset(j3, k3, l3);
                  BlockState blockstate = worldgenlevel.getBlockState(blockpos2);
                  if (j3 != k && k3 != -1 && l3 != l1 && j3 != l && k3 != 4 && l3 != i2) {
                     if (!blockstate.is(Blocks.CHEST) && !blockstate.is(Blocks.SPAWNER)) {
                        this.safeSetBlock(worldgenlevel, blockpos2, AIR, predicate);
                     }
                  } else if (blockpos2.getY() >= worldgenlevel.getMinBuildHeight() && !worldgenlevel.getBlockState(blockpos2.below()).isSolid()) {
                     worldgenlevel.setBlock(blockpos2, AIR, 2);
                  } else if (blockstate.isSolid() && !blockstate.is(Blocks.CHEST)) {
                     if (k3 == -1 && randomsource.nextInt(4) != 0) {
                        this.safeSetBlock(worldgenlevel, blockpos2, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), predicate);
                     } else {
                        this.safeSetBlock(worldgenlevel, blockpos2, Blocks.COBBLESTONE.defaultBlockState(), predicate);
                     }
                  }
               }
            }
         }

         for(int i4 = 0; i4 < 2; ++i4) {
            for(int j4 = 0; j4 < 3; ++j4) {
               int k4 = blockpos.getX() + randomsource.nextInt(j * 2 + 1) - j;
               int l4 = blockpos.getY();
               int i5 = blockpos.getZ() + randomsource.nextInt(k1 * 2 + 1) - k1;
               BlockPos blockpos3 = new BlockPos(k4, l4, i5);
               if (worldgenlevel.isEmptyBlock(blockpos3)) {
                  int j5 = 0;

                  for(Direction direction : Direction.Plane.HORIZONTAL) {
                     if (worldgenlevel.getBlockState(blockpos3.relative(direction)).isSolid()) {
                        ++j5;
                     }
                  }

                  if (j5 == 1) {
                     this.safeSetBlock(worldgenlevel, blockpos3, StructurePiece.reorient(worldgenlevel, blockpos3, Blocks.CHEST.defaultBlockState()), predicate);
                     RandomizableContainerBlockEntity.setLootTable(worldgenlevel, randomsource, blockpos3, BuiltInLootTables.SIMPLE_DUNGEON);
                     break;
                  }
               }
            }
         }

         this.safeSetBlock(worldgenlevel, blockpos, Blocks.SPAWNER.defaultBlockState(), predicate);
         BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos);
         if (blockentity instanceof SpawnerBlockEntity) {
            SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
            spawnerblockentity.setEntityId(this.randomEntityId(randomsource), randomsource);
         } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockpos.getX(), blockpos.getY(), blockpos.getZ());
         }

         return true;
      } else {
         return false;
      }
   }

   private EntityType<?> randomEntityId(RandomSource randomsource) {
      return Util.getRandom(MOBS, randomsource);
   }
}
