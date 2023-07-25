package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class CompassItemPropertyFunction implements ClampedItemPropertyFunction {
   public static final int DEFAULT_ROTATION = 0;
   private final CompassItemPropertyFunction.CompassWobble wobble = new CompassItemPropertyFunction.CompassWobble();
   private final CompassItemPropertyFunction.CompassWobble wobbleRandom = new CompassItemPropertyFunction.CompassWobble();
   public final CompassItemPropertyFunction.CompassTarget compassTarget;

   public CompassItemPropertyFunction(CompassItemPropertyFunction.CompassTarget compassitempropertyfunction_compasstarget) {
      this.compassTarget = compassitempropertyfunction_compasstarget;
   }

   public float unclampedCall(ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i) {
      Entity entity = (Entity)(livingentity != null ? livingentity : itemstack.getEntityRepresentation());
      if (entity == null) {
         return 0.0F;
      } else {
         clientlevel = this.tryFetchLevelIfMissing(entity, clientlevel);
         return clientlevel == null ? 0.0F : this.getCompassRotation(itemstack, clientlevel, i, entity);
      }
   }

   private float getCompassRotation(ItemStack itemstack, ClientLevel clientlevel, int i, Entity entity) {
      GlobalPos globalpos = this.compassTarget.getPos(clientlevel, itemstack, entity);
      long j = clientlevel.getGameTime();
      return !this.isValidCompassTargetPos(entity, globalpos) ? this.getRandomlySpinningRotation(i, j) : this.getRotationTowardsCompassTarget(entity, j, globalpos.pos());
   }

   private float getRandomlySpinningRotation(int i, long j) {
      if (this.wobbleRandom.shouldUpdate(j)) {
         this.wobbleRandom.update(j, Math.random());
      }

      double d0 = this.wobbleRandom.rotation + (double)((float)this.hash(i) / 2.14748365E9F);
      return Mth.positiveModulo((float)d0, 1.0F);
   }

   private float getRotationTowardsCompassTarget(Entity entity, long i, BlockPos blockpos) {
      double d0 = this.getAngleFromEntityToPos(entity, blockpos);
      double d1 = this.getWrappedVisualRotationY(entity);
      if (entity instanceof Player player) {
         if (player.isLocalPlayer()) {
            if (this.wobble.shouldUpdate(i)) {
               this.wobble.update(i, 0.5D - (d1 - 0.25D));
            }

            double d2 = d0 + this.wobble.rotation;
            return Mth.positiveModulo((float)d2, 1.0F);
         }
      }

      double d3 = 0.5D - (d1 - 0.25D - d0);
      return Mth.positiveModulo((float)d3, 1.0F);
   }

   @Nullable
   private ClientLevel tryFetchLevelIfMissing(Entity entity, @Nullable ClientLevel clientlevel) {
      return clientlevel == null && entity.level() instanceof ClientLevel ? (ClientLevel)entity.level() : clientlevel;
   }

   private boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos globalpos) {
      return globalpos != null && globalpos.dimension() == entity.level().dimension() && !(globalpos.pos().distToCenterSqr(entity.position()) < (double)1.0E-5F);
   }

   private double getAngleFromEntityToPos(Entity entity, BlockPos blockpos) {
      Vec3 vec3 = Vec3.atCenterOf(blockpos);
      return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX()) / (double)((float)Math.PI * 2F);
   }

   private double getWrappedVisualRotationY(Entity entity) {
      return Mth.positiveModulo((double)(entity.getVisualRotationYInDegrees() / 360.0F), 1.0D);
   }

   private int hash(int i) {
      return i * 1327217883;
   }

   public interface CompassTarget {
      @Nullable
      GlobalPos getPos(ClientLevel clientlevel, ItemStack itemstack, Entity entity);
   }

   static class CompassWobble {
      double rotation;
      private double deltaRotation;
      private long lastUpdateTick;

      boolean shouldUpdate(long i) {
         return this.lastUpdateTick != i;
      }

      void update(long i, double d0) {
         this.lastUpdateTick = i;
         double d1 = d0 - this.rotation;
         d1 = Mth.positiveModulo(d1 + 0.5D, 1.0D) - 0.5D;
         this.deltaRotation += d1 * 0.1D;
         this.deltaRotation *= 0.8D;
         this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0D);
      }
   }
}
