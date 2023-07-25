package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class FireChargeItem extends Item {
   public FireChargeItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      boolean flag = false;
      if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
         blockpos = blockpos.relative(useoncontext.getClickedFace());
         if (BaseFireBlock.canBePlacedAt(level, blockpos, useoncontext.getHorizontalDirection())) {
            this.playSound(level, blockpos);
            level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
            level.gameEvent(useoncontext.getPlayer(), GameEvent.BLOCK_PLACE, blockpos);
            flag = true;
         }
      } else {
         this.playSound(level, blockpos);
         level.setBlockAndUpdate(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
         level.gameEvent(useoncontext.getPlayer(), GameEvent.BLOCK_CHANGE, blockpos);
         flag = true;
      }

      if (flag) {
         useoncontext.getItemInHand().shrink(1);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.FAIL;
      }
   }

   private void playSound(Level level, BlockPos blockpos) {
      RandomSource randomsource = level.getRandom();
      level.playSound((Player)null, blockpos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 1.0F);
   }
}
