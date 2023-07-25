package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
   public LeadItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(BlockTags.FENCES)) {
         Player player = useoncontext.getPlayer();
         if (!level.isClientSide && player != null) {
            bindPlayerMobs(player, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockpos) {
      LeashFenceKnotEntity leashfenceknotentity = null;
      boolean flag = false;
      double d0 = 7.0D;
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();

      for(Mob mob : level.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0D, (double)j - 7.0D, (double)k - 7.0D, (double)i + 7.0D, (double)j + 7.0D, (double)k + 7.0D))) {
         if (mob.getLeashHolder() == player) {
            if (leashfenceknotentity == null) {
               leashfenceknotentity = LeashFenceKnotEntity.getOrCreateKnot(level, blockpos);
               leashfenceknotentity.playPlacementSound();
            }

            mob.setLeashedTo(leashfenceknotentity, true);
            flag = true;
         }
      }

      if (flag) {
         level.gameEvent(GameEvent.BLOCK_ATTACH, blockpos, GameEvent.Context.of(player));
      }

      return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
   }
}
