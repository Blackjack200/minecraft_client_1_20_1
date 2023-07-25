package net.minecraft.world.entity.animal;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface Bucketable {
   boolean fromBucket();

   void setFromBucket(boolean flag);

   void saveToBucketTag(ItemStack itemstack);

   void loadFromBucketTag(CompoundTag compoundtag);

   ItemStack getBucketItemStack();

   SoundEvent getPickupSound();

   /** @deprecated */
   @Deprecated
   static void saveDefaultDataToBucketTag(Mob mob, ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      if (mob.hasCustomName()) {
         itemstack.setHoverName(mob.getCustomName());
      }

      if (mob.isNoAi()) {
         compoundtag.putBoolean("NoAI", mob.isNoAi());
      }

      if (mob.isSilent()) {
         compoundtag.putBoolean("Silent", mob.isSilent());
      }

      if (mob.isNoGravity()) {
         compoundtag.putBoolean("NoGravity", mob.isNoGravity());
      }

      if (mob.hasGlowingTag()) {
         compoundtag.putBoolean("Glowing", mob.hasGlowingTag());
      }

      if (mob.isInvulnerable()) {
         compoundtag.putBoolean("Invulnerable", mob.isInvulnerable());
      }

      compoundtag.putFloat("Health", mob.getHealth());
   }

   /** @deprecated */
   @Deprecated
   static void loadDefaultDataFromBucketTag(Mob mob, CompoundTag compoundtag) {
      if (compoundtag.contains("NoAI")) {
         mob.setNoAi(compoundtag.getBoolean("NoAI"));
      }

      if (compoundtag.contains("Silent")) {
         mob.setSilent(compoundtag.getBoolean("Silent"));
      }

      if (compoundtag.contains("NoGravity")) {
         mob.setNoGravity(compoundtag.getBoolean("NoGravity"));
      }

      if (compoundtag.contains("Glowing")) {
         mob.setGlowingTag(compoundtag.getBoolean("Glowing"));
      }

      if (compoundtag.contains("Invulnerable")) {
         mob.setInvulnerable(compoundtag.getBoolean("Invulnerable"));
      }

      if (compoundtag.contains("Health", 99)) {
         mob.setHealth(compoundtag.getFloat("Health"));
      }

   }

   static <T extends LivingEntity & Bucketable> Optional<InteractionResult> bucketMobPickup(Player player, InteractionHand interactionhand, T livingentity) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (itemstack.getItem() == Items.WATER_BUCKET && livingentity.isAlive()) {
         livingentity.playSound(livingentity.getPickupSound(), 1.0F, 1.0F);
         ItemStack itemstack1 = livingentity.getBucketItemStack();
         livingentity.saveToBucketTag(itemstack1);
         ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1, false);
         player.setItemInHand(interactionhand, itemstack2);
         Level level = livingentity.level();
         if (!level.isClientSide) {
            CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemstack1);
         }

         livingentity.discard();
         return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
      } else {
         return Optional.empty();
      }
   }
}
