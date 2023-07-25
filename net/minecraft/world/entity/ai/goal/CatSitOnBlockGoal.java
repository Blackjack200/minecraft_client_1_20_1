package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
   private final Cat cat;

   public CatSitOnBlockGoal(Cat cat, double d0) {
      super(cat, d0, 8);
      this.cat = cat;
   }

   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && super.canUse();
   }

   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   public void stop() {
      super.stop();
      this.cat.setInSittingPose(false);
   }

   public void tick() {
      super.tick();
      this.cat.setInSittingPose(this.isReachedTarget());
   }

   protected boolean isValidTarget(LevelReader levelreader, BlockPos blockpos) {
      if (!levelreader.isEmptyBlock(blockpos.above())) {
         return false;
      } else {
         BlockState blockstate = levelreader.getBlockState(blockpos);
         if (blockstate.is(Blocks.CHEST)) {
            return ChestBlockEntity.getOpenCount(levelreader, blockpos) < 1;
         } else {
            return blockstate.is(Blocks.FURNACE) && blockstate.getValue(FurnaceBlock.LIT) ? true : blockstate.is(BlockTags.BEDS, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.<BedPart>getOptionalValue(BedBlock.PART).map((bedpart) -> bedpart != BedPart.HEAD).orElse(true));
         }
      }
   }
}
