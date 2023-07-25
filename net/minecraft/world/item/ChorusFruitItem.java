package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ChorusFruitItem extends Item {
   public ChorusFruitItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      ItemStack itemstack1 = super.finishUsingItem(itemstack, level, livingentity);
      if (!level.isClientSide) {
         double d0 = livingentity.getX();
         double d1 = livingentity.getY();
         double d2 = livingentity.getZ();

         for(int i = 0; i < 16; ++i) {
            double d3 = livingentity.getX() + (livingentity.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = Mth.clamp(livingentity.getY() + (double)(livingentity.getRandom().nextInt(16) - 8), (double)level.getMinBuildHeight(), (double)(level.getMinBuildHeight() + ((ServerLevel)level).getLogicalHeight() - 1));
            double d5 = livingentity.getZ() + (livingentity.getRandom().nextDouble() - 0.5D) * 16.0D;
            if (livingentity.isPassenger()) {
               livingentity.stopRiding();
            }

            Vec3 vec3 = livingentity.position();
            if (livingentity.randomTeleport(d3, d4, d5, true)) {
               level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(livingentity));
               SoundEvent soundevent = livingentity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
               level.playSound((Player)null, d0, d1, d2, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
               livingentity.playSound(soundevent, 1.0F, 1.0F);
               break;
            }
         }

         if (livingentity instanceof Player) {
            ((Player)livingentity).getCooldowns().addCooldown(this, 20);
         }
      }

      return itemstack1;
   }
}
