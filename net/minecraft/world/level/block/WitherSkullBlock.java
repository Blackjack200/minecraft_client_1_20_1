package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

public class WitherSkullBlock extends SkullBlock {
   @Nullable
   private static BlockPattern witherPatternFull;
   @Nullable
   private static BlockPattern witherPatternBase;

   protected WitherSkullBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(SkullBlock.Types.WITHER_SKELETON, blockbehaviour_properties);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      super.setPlacedBy(level, blockpos, blockstate, livingentity, itemstack);
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof SkullBlockEntity) {
         checkSpawn(level, blockpos, (SkullBlockEntity)blockentity);
      }

   }

   public static void checkSpawn(Level level, BlockPos blockpos, SkullBlockEntity skullblockentity) {
      if (!level.isClientSide) {
         BlockState blockstate = skullblockentity.getBlockState();
         boolean flag = blockstate.is(Blocks.WITHER_SKELETON_SKULL) || blockstate.is(Blocks.WITHER_SKELETON_WALL_SKULL);
         if (flag && blockpos.getY() >= level.getMinBuildHeight() && level.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPattern.BlockPatternMatch blockpattern_blockpatternmatch = getOrCreateWitherFull().find(level, blockpos);
            if (blockpattern_blockpatternmatch != null) {
               WitherBoss witherboss = EntityType.WITHER.create(level);
               if (witherboss != null) {
                  CarvedPumpkinBlock.clearPatternBlocks(level, blockpattern_blockpatternmatch);
                  BlockPos blockpos1 = blockpattern_blockpatternmatch.getBlock(1, 2, 0).getPos();
                  witherboss.moveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.55D, (double)blockpos1.getZ() + 0.5D, blockpattern_blockpatternmatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                  witherboss.yBodyRot = blockpattern_blockpatternmatch.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                  witherboss.makeInvulnerable();

                  for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, witherboss.getBoundingBox().inflate(50.0D))) {
                     CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayer, witherboss);
                  }

                  level.addFreshEntity(witherboss);
                  CarvedPumpkinBlock.updatePatternBlocks(level, blockpattern_blockpatternmatch);
               }

            }
         }
      }
   }

   public static boolean canSpawnMob(Level level, BlockPos blockpos, ItemStack itemstack) {
      if (itemstack.is(Items.WITHER_SKELETON_SKULL) && blockpos.getY() >= level.getMinBuildHeight() + 2 && level.getDifficulty() != Difficulty.PEACEFUL && !level.isClientSide) {
         return getOrCreateWitherBase().find(level, blockpos) != null;
      } else {
         return false;
      }
   }

   private static BlockPattern getOrCreateWitherFull() {
      if (witherPatternFull == null) {
         witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', (blockinworld1) -> blockinworld1.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStatePredicate.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', (blockinworld) -> blockinworld.getState().isAir()).build();
      }

      return witherPatternFull;
   }

   private static BlockPattern getOrCreateWitherBase() {
      if (witherPatternBase == null) {
         witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', (blockinworld1) -> blockinworld1.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)).where('~', (blockinworld) -> blockinworld.getState().isAir()).build();
      }

      return witherPatternBase;
   }
}
