package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ClampedItemPropertyFunction extends ItemPropertyFunction {
   /** @deprecated */
   @Deprecated
   default float call(ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i) {
      return Mth.clamp(this.unclampedCall(itemstack, clientlevel, livingentity, i), 0.0F, 1.0F);
   }

   float unclampedCall(ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i);
}
