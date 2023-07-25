package net.minecraft.client.color.block;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

public class BlockColors {
   private static final int DEFAULT = -1;
   private final IdMapper<BlockColor> blockColors = new IdMapper<>(32);
   private final Map<Block, Set<Property<?>>> coloringStates = Maps.newHashMap();

   public static BlockColors createDefault() {
      BlockColors blockcolors = new BlockColors();
      blockcolors.register((blockstate11, blockandtintgetter11, blockpos11, l3) -> blockandtintgetter11 != null && blockpos11 != null ? BiomeColors.getAverageGrassColor(blockandtintgetter11, blockstate11.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? blockpos11.below() : blockpos11) : GrassColor.getDefaultColor(), Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.addColoringState(DoublePlantBlock.HALF, Blocks.LARGE_FERN, Blocks.TALL_GRASS);
      blockcolors.register((blockstate10, blockandtintgetter10, blockpos10, k3) -> blockandtintgetter10 != null && blockpos10 != null ? BiomeColors.getAverageGrassColor(blockandtintgetter10, blockpos10) : GrassColor.getDefaultColor(), Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
      blockcolors.register((blockstate9, blockandtintgetter9, blockpos9, j3) -> {
         if (j3 != 0) {
            return blockandtintgetter9 != null && blockpos9 != null ? BiomeColors.getAverageGrassColor(blockandtintgetter9, blockpos9) : GrassColor.getDefaultColor();
         } else {
            return -1;
         }
      }, Blocks.PINK_PETALS);
      blockcolors.register((blockstate8, blockandtintgetter8, blockpos8, i3) -> FoliageColor.getEvergreenColor(), Blocks.SPRUCE_LEAVES);
      blockcolors.register((blockstate7, blockandtintgetter7, blockpos7, l2) -> FoliageColor.getBirchColor(), Blocks.BIRCH_LEAVES);
      blockcolors.register((blockstate6, blockandtintgetter6, blockpos6, k2) -> blockandtintgetter6 != null && blockpos6 != null ? BiomeColors.getAverageFoliageColor(blockandtintgetter6, blockpos6) : FoliageColor.getDefaultColor(), Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE, Blocks.MANGROVE_LEAVES);
      blockcolors.register((blockstate5, blockandtintgetter5, blockpos5, j2) -> blockandtintgetter5 != null && blockpos5 != null ? BiomeColors.getAverageWaterColor(blockandtintgetter5, blockpos5) : -1, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.WATER_CAULDRON);
      blockcolors.register((blockstate4, blockandtintgetter4, blockpos4, i2) -> RedStoneWireBlock.getColorForPower(blockstate4.getValue(RedStoneWireBlock.POWER)), Blocks.REDSTONE_WIRE);
      blockcolors.addColoringState(RedStoneWireBlock.POWER, Blocks.REDSTONE_WIRE);
      blockcolors.register((blockstate3, blockandtintgetter3, blockpos3, l1) -> blockandtintgetter3 != null && blockpos3 != null ? BiomeColors.getAverageGrassColor(blockandtintgetter3, blockpos3) : -1, Blocks.SUGAR_CANE);
      blockcolors.register((blockstate2, blockandtintgetter2, blockpos2, k1) -> 14731036, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
      blockcolors.register((blockstate1, blockandtintgetter1, blockpos1, j) -> {
         int k = blockstate1.getValue(StemBlock.AGE);
         int l = k * 32;
         int i1 = 255 - k * 8;
         int j1 = k * 4;
         return l << 16 | i1 << 8 | j1;
      }, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.addColoringState(StemBlock.AGE, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
      blockcolors.register((blockstate, blockandtintgetter, blockpos, i) -> blockandtintgetter != null && blockpos != null ? 2129968 : 7455580, Blocks.LILY_PAD);
      return blockcolors;
   }

   public int getColor(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockColor blockcolor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockstate.getBlock()));
      if (blockcolor != null) {
         return blockcolor.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, 0);
      } else {
         MapColor mapcolor = blockstate.getMapColor(level, blockpos);
         return mapcolor != null ? mapcolor.col : -1;
      }
   }

   public int getColor(BlockState blockstate, @Nullable BlockAndTintGetter blockandtintgetter, @Nullable BlockPos blockpos, int i) {
      BlockColor blockcolor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(blockstate.getBlock()));
      return blockcolor == null ? -1 : blockcolor.getColor(blockstate, blockandtintgetter, blockpos, i);
   }

   public void register(BlockColor blockcolor, Block... ablock) {
      for(Block block : ablock) {
         this.blockColors.addMapping(blockcolor, BuiltInRegistries.BLOCK.getId(block));
      }

   }

   private void addColoringStates(Set<Property<?>> set, Block... ablock) {
      for(Block block : ablock) {
         this.coloringStates.put(block, set);
      }

   }

   private void addColoringState(Property<?> property, Block... ablock) {
      this.addColoringStates(ImmutableSet.of(property), ablock);
   }

   public Set<Property<?>> getColoringProperties(Block block) {
      return this.coloringStates.getOrDefault(block, ImmutableSet.of());
   }
}
