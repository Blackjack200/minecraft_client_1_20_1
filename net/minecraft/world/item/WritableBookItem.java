package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class WritableBookItem extends Item {
   public WritableBookItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(Blocks.LECTERN)) {
         return LecternBlock.tryPlaceBook(useoncontext.getPlayer(), level, blockpos, blockstate, useoncontext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      player.openItemGui(itemstack, interactionhand);
      player.awardStat(Stats.ITEM_USED.get(this));
      return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
   }

   public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundtag) {
      if (compoundtag == null) {
         return false;
      } else if (!compoundtag.contains("pages", 9)) {
         return false;
      } else {
         ListTag listtag = compoundtag.getList("pages", 8);

         for(int i = 0; i < listtag.size(); ++i) {
            String s = listtag.getString(i);
            if (s.length() > 32767) {
               return false;
            }
         }

         return true;
      }
   }
}
