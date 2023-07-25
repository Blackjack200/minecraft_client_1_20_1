package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class GlowInkSacItem extends Item implements SignApplicator {
   public GlowInkSacItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean tryApplyToSign(Level level, SignBlockEntity signblockentity, boolean flag, Player player) {
      if (signblockentity.updateText((signtext) -> signtext.setHasGlowingText(true), flag)) {
         level.playSound((Player)null, signblockentity.getBlockPos(), SoundEvents.GLOW_INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
