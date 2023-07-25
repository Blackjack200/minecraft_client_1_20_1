package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface CrossbowAttackMob extends RangedAttackMob {
   void setChargingCrossbow(boolean flag);

   void shootCrossbowProjectile(LivingEntity livingentity, ItemStack itemstack, Projectile projectile, float f);

   @Nullable
   LivingEntity getTarget();

   void onCrossbowAttackPerformed();

   default void performCrossbowAttack(LivingEntity livingentity, float f) {
      InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(livingentity, Items.CROSSBOW);
      ItemStack itemstack = livingentity.getItemInHand(interactionhand);
      if (livingentity.isHolding(Items.CROSSBOW)) {
         CrossbowItem.performShooting(livingentity.level(), livingentity, interactionhand, itemstack, f, (float)(14 - livingentity.level().getDifficulty().getId() * 4));
      }

      this.onCrossbowAttackPerformed();
   }

   default void shootCrossbowProjectile(LivingEntity livingentity, LivingEntity livingentity1, Projectile projectile, float f, float f1) {
      double d0 = livingentity1.getX() - livingentity.getX();
      double d1 = livingentity1.getZ() - livingentity.getZ();
      double d2 = Math.sqrt(d0 * d0 + d1 * d1);
      double d3 = livingentity1.getY(0.3333333333333333D) - projectile.getY() + d2 * (double)0.2F;
      Vector3f vector3f = this.getProjectileShotVector(livingentity, new Vec3(d0, d3, d1), f);
      projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), f1, (float)(14 - livingentity.level().getDifficulty().getId() * 4));
      livingentity.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (livingentity.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   default Vector3f getProjectileShotVector(LivingEntity livingentity, Vec3 vec3, float f) {
      Vector3f vector3f = vec3.toVector3f().normalize();
      Vector3f vector3f1 = (new Vector3f((Vector3fc)vector3f)).cross(new Vector3f(0.0F, 1.0F, 0.0F));
      if ((double)vector3f1.lengthSquared() <= 1.0E-7D) {
         Vec3 vec31 = livingentity.getUpVector(1.0F);
         vector3f1 = (new Vector3f((Vector3fc)vector3f)).cross(vec31.toVector3f());
      }

      Vector3f vector3f2 = (new Vector3f((Vector3fc)vector3f)).rotateAxis(((float)Math.PI / 2F), vector3f1.x, vector3f1.y, vector3f1.z);
      return (new Vector3f((Vector3fc)vector3f)).rotateAxis(f * ((float)Math.PI / 180F), vector3f2.x, vector3f2.y, vector3f2.z);
   }
}
