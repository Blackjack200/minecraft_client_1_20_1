package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ProjectileUtil {
   public static HitResult getHitResultOnMoveVector(Entity entity, Predicate<Entity> predicate) {
      Vec3 vec3 = entity.getDeltaMovement();
      Level level = entity.level();
      Vec3 vec31 = entity.position();
      return getHitResult(vec31, entity, predicate, vec3, level);
   }

   public static HitResult getHitResultOnViewVector(Entity entity, Predicate<Entity> predicate, double d0) {
      Vec3 vec3 = entity.getViewVector(0.0F).scale(d0);
      Level level = entity.level();
      Vec3 vec31 = entity.getEyePosition();
      return getHitResult(vec31, entity, predicate, vec3, level);
   }

   private static HitResult getHitResult(Vec3 vec3, Entity entity, Predicate<Entity> predicate, Vec3 vec31, Level level) {
      Vec3 vec32 = vec3.add(vec31);
      HitResult hitresult = level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
      if (hitresult.getType() != HitResult.Type.MISS) {
         vec32 = hitresult.getLocation();
      }

      HitResult hitresult1 = getEntityHitResult(level, entity, vec3, vec32, entity.getBoundingBox().expandTowards(vec31).inflate(1.0D), predicate);
      if (hitresult1 != null) {
         hitresult = hitresult1;
      }

      return hitresult;
   }

   @Nullable
   public static EntityHitResult getEntityHitResult(Entity entity, Vec3 vec3, Vec3 vec31, AABB aabb, Predicate<Entity> predicate, double d0) {
      Level level = entity.level();
      double d1 = d0;
      Entity entity1 = null;
      Vec3 vec32 = null;

      for(Entity entity2 : level.getEntities(entity, aabb, predicate)) {
         AABB aabb1 = entity2.getBoundingBox().inflate((double)entity2.getPickRadius());
         Optional<Vec3> optional = aabb1.clip(vec3, vec31);
         if (aabb1.contains(vec3)) {
            if (d1 >= 0.0D) {
               entity1 = entity2;
               vec32 = optional.orElse(vec3);
               d1 = 0.0D;
            }
         } else if (optional.isPresent()) {
            Vec3 vec33 = optional.get();
            double d2 = vec3.distanceToSqr(vec33);
            if (d2 < d1 || d1 == 0.0D) {
               if (entity2.getRootVehicle() == entity.getRootVehicle()) {
                  if (d1 == 0.0D) {
                     entity1 = entity2;
                     vec32 = vec33;
                  }
               } else {
                  entity1 = entity2;
                  vec32 = vec33;
                  d1 = d2;
               }
            }
         }
      }

      return entity1 == null ? null : new EntityHitResult(entity1, vec32);
   }

   @Nullable
   public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec31, AABB aabb, Predicate<Entity> predicate) {
      return getEntityHitResult(level, entity, vec3, vec31, aabb, predicate, 0.3F);
   }

   @Nullable
   public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec31, AABB aabb, Predicate<Entity> predicate, float f) {
      double d0 = Double.MAX_VALUE;
      Entity entity1 = null;

      for(Entity entity2 : level.getEntities(entity, aabb, predicate)) {
         AABB aabb1 = entity2.getBoundingBox().inflate((double)f);
         Optional<Vec3> optional = aabb1.clip(vec3, vec31);
         if (optional.isPresent()) {
            double d1 = vec3.distanceToSqr(optional.get());
            if (d1 < d0) {
               entity1 = entity2;
               d0 = d1;
            }
         }
      }

      return entity1 == null ? null : new EntityHitResult(entity1);
   }

   public static void rotateTowardsMovement(Entity entity, float f) {
      Vec3 vec3 = entity.getDeltaMovement();
      if (vec3.lengthSqr() != 0.0D) {
         double d0 = vec3.horizontalDistance();
         entity.setYRot((float)(Mth.atan2(vec3.z, vec3.x) * (double)(180F / (float)Math.PI)) + 90.0F);
         entity.setXRot((float)(Mth.atan2(d0, vec3.y) * (double)(180F / (float)Math.PI)) - 90.0F);

         while(entity.getXRot() - entity.xRotO < -180.0F) {
            entity.xRotO -= 360.0F;
         }

         while(entity.getXRot() - entity.xRotO >= 180.0F) {
            entity.xRotO += 360.0F;
         }

         while(entity.getYRot() - entity.yRotO < -180.0F) {
            entity.yRotO -= 360.0F;
         }

         while(entity.getYRot() - entity.yRotO >= 180.0F) {
            entity.yRotO += 360.0F;
         }

         entity.setXRot(Mth.lerp(f, entity.xRotO, entity.getXRot()));
         entity.setYRot(Mth.lerp(f, entity.yRotO, entity.getYRot()));
      }
   }

   public static InteractionHand getWeaponHoldingHand(LivingEntity livingentity, Item item) {
      return livingentity.getMainHandItem().is(item) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
   }

   public static AbstractArrow getMobArrow(LivingEntity livingentity, ItemStack itemstack, float f) {
      ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
      AbstractArrow abstractarrow = arrowitem.createArrow(livingentity.level(), itemstack, livingentity);
      abstractarrow.setEnchantmentEffectsFromEntity(livingentity, f);
      if (itemstack.is(Items.TIPPED_ARROW) && abstractarrow instanceof Arrow) {
         ((Arrow)abstractarrow).setEffectsFromItem(itemstack);
      }

      return abstractarrow;
   }
}
