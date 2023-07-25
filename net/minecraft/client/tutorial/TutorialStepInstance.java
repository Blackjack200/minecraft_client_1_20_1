package net.minecraft.client.tutorial;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface TutorialStepInstance {
   default void clear() {
   }

   default void tick() {
   }

   default void onInput(Input input) {
   }

   default void onMouse(double d0, double d1) {
   }

   default void onLookAt(ClientLevel clientlevel, HitResult hitresult) {
   }

   default void onDestroyBlock(ClientLevel clientlevel, BlockPos blockpos, BlockState blockstate, float f) {
   }

   default void onOpenInventory() {
   }

   default void onGetItem(ItemStack itemstack) {
   }
}
