package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

public class BlockAgeProcessor extends StructureProcessor {
   public static final Codec<BlockAgeProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockAgeProcessor::new, (blockageprocessor) -> blockageprocessor.mossiness).codec();
   private static final float PROBABILITY_OF_REPLACING_FULL_BLOCK = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_STAIRS = 0.5F;
   private static final float PROBABILITY_OF_REPLACING_OBSIDIAN = 0.15F;
   private static final BlockState[] NON_MOSSY_REPLACEMENTS = new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()};
   private final float mossiness;

   public BlockAgeProcessor(float f) {
      this.mossiness = f;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      RandomSource randomsource = structureplacesettings.getRandom(structuretemplate_structureblockinfo1.pos());
      BlockState blockstate = structuretemplate_structureblockinfo1.state();
      BlockPos blockpos2 = structuretemplate_structureblockinfo1.pos();
      BlockState blockstate1 = null;
      if (!blockstate.is(Blocks.STONE_BRICKS) && !blockstate.is(Blocks.STONE) && !blockstate.is(Blocks.CHISELED_STONE_BRICKS)) {
         if (blockstate.is(BlockTags.STAIRS)) {
            blockstate1 = this.maybeReplaceStairs(randomsource, structuretemplate_structureblockinfo1.state());
         } else if (blockstate.is(BlockTags.SLABS)) {
            blockstate1 = this.maybeReplaceSlab(randomsource);
         } else if (blockstate.is(BlockTags.WALLS)) {
            blockstate1 = this.maybeReplaceWall(randomsource);
         } else if (blockstate.is(Blocks.OBSIDIAN)) {
            blockstate1 = this.maybeReplaceObsidian(randomsource);
         }
      } else {
         blockstate1 = this.maybeReplaceFullStoneBlock(randomsource);
      }

      return blockstate1 != null ? new StructureTemplate.StructureBlockInfo(blockpos2, blockstate1, structuretemplate_structureblockinfo1.nbt()) : structuretemplate_structureblockinfo1;
   }

   @Nullable
   private BlockState maybeReplaceFullStoneBlock(RandomSource randomsource) {
      if (randomsource.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(randomsource, Blocks.STONE_BRICK_STAIRS)};
         BlockState[] ablockstate1 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(randomsource, Blocks.MOSSY_STONE_BRICK_STAIRS)};
         return this.getRandomBlock(randomsource, ablockstate, ablockstate1);
      }
   }

   @Nullable
   private BlockState maybeReplaceStairs(RandomSource randomsource, BlockState blockstate) {
      Direction direction = blockstate.getValue(StairBlock.FACING);
      Half half = blockstate.getValue(StairBlock.HALF);
      if (randomsource.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction).setValue(StairBlock.HALF, half), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()};
         return this.getRandomBlock(randomsource, NON_MOSSY_REPLACEMENTS, ablockstate);
      }
   }

   @Nullable
   private BlockState maybeReplaceSlab(RandomSource randomsource) {
      return randomsource.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceWall(RandomSource randomsource) {
      return randomsource.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceObsidian(RandomSource randomsource) {
      return randomsource.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
   }

   private static BlockState getRandomFacingStairs(RandomSource randomsource, Block block) {
      return block.defaultBlockState().setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomsource)).setValue(StairBlock.HALF, Util.getRandom(Half.values(), randomsource));
   }

   private BlockState getRandomBlock(RandomSource randomsource, BlockState[] ablockstate, BlockState[] ablockstate1) {
      return randomsource.nextFloat() < this.mossiness ? getRandomBlock(randomsource, ablockstate1) : getRandomBlock(randomsource, ablockstate);
   }

   private static BlockState getRandomBlock(RandomSource randomsource, BlockState[] ablockstate) {
      return ablockstate[randomsource.nextInt(ablockstate.length)];
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_AGE;
   }
}
