package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public abstract class ProjectileWeaponItem extends Item {
   public static final Predicate<ItemStack> ARROW_ONLY = (itemstack) -> itemstack.is(ItemTags.ARROWS);
   public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or((itemstack) -> itemstack.is(Items.FIREWORK_ROCKET));

   public ProjectileWeaponItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public Predicate<ItemStack> getSupportedHeldProjectiles() {
      return this.getAllSupportedProjectiles();
   }

   public abstract Predicate<ItemStack> getAllSupportedProjectiles();

   public static ItemStack getHeldProjectile(LivingEntity livingentity, Predicate<ItemStack> predicate) {
      if (predicate.test(livingentity.getItemInHand(InteractionHand.OFF_HAND))) {
         return livingentity.getItemInHand(InteractionHand.OFF_HAND);
      } else {
         return predicate.test(livingentity.getItemInHand(InteractionHand.MAIN_HAND)) ? livingentity.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
      }
   }

   public int getEnchantmentValue() {
      return 1;
   }

   public abstract int getDefaultProjectileRange();
}
