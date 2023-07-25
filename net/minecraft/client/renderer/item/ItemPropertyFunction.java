package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** @deprecated */
@Deprecated
public interface ItemPropertyFunction {
   float call(ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i);
}
