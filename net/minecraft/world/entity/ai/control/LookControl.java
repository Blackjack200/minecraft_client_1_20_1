package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control {
   protected final Mob mob;
   protected float yMaxRotSpeed;
   protected float xMaxRotAngle;
   protected int lookAtCooldown;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   public LookControl(Mob mob) {
      this.mob = mob;
   }

   public void setLookAt(Vec3 vec3) {
      this.setLookAt(vec3.x, vec3.y, vec3.z);
   }

   public void setLookAt(Entity entity) {
      this.setLookAt(entity.getX(), getWantedY(entity), entity.getZ());
   }

   public void setLookAt(Entity entity, float f, float f1) {
      this.setLookAt(entity.getX(), getWantedY(entity), entity.getZ(), f, f1);
   }

   public void setLookAt(double d0, double d1, double d2) {
      this.setLookAt(d0, d1, d2, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
   }

   public void setLookAt(double d0, double d1, double d2, float f, float f1) {
      this.wantedX = d0;
      this.wantedY = d1;
      this.wantedZ = d2;
      this.yMaxRotSpeed = f;
      this.xMaxRotAngle = f1;
      this.lookAtCooldown = 2;
   }

   public void tick() {
      if (this.resetXRotOnTick()) {
         this.mob.setXRot(0.0F);
      }

      if (this.lookAtCooldown > 0) {
         --this.lookAtCooldown;
         this.getYRotD().ifPresent((ofloat1) -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, ofloat1, this.yMaxRotSpeed));
         this.getXRotD().ifPresent((ofloat) -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), ofloat, this.xMaxRotAngle)));
      } else {
         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
      }

      this.clampHeadRotationToBody();
   }

   protected void clampHeadRotationToBody() {
      if (!this.mob.getNavigation().isDone()) {
         this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
      }

   }

   protected boolean resetXRotOnTick() {
      return true;
   }

   public boolean isLookingAtTarget() {
      return this.lookAtCooldown > 0;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   protected Optional<Float> getXRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedY - this.mob.getEyeY();
      double d2 = this.wantedZ - this.mob.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d3) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
   }

   protected Optional<Float> getYRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedZ - this.mob.getZ();
      return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d0) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
   }

   protected float rotateTowards(float f, float f1, float f2) {
      float f3 = Mth.degreesDifference(f, f1);
      float f4 = Mth.clamp(f3, -f2, f2);
      return f + f4;
   }

   private static double getWantedY(Entity entity) {
      return entity instanceof LivingEntity ? entity.getEyeY() : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;
   }
}
