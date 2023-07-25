package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public class RemotePlayer extends AbstractClientPlayer {
   private Vec3 lerpDeltaMovement = Vec3.ZERO;
   private int lerpDeltaMovementSteps;

   public RemotePlayer(ClientLevel clientlevel, GameProfile gameprofile) {
      super(clientlevel, gameprofile);
      this.setMaxUpStep(1.0F);
      this.noPhysics = true;
   }

   public boolean shouldRenderAtSqrDistance(double d0) {
      double d1 = this.getBoundingBox().getSize() * 10.0D;
      if (Double.isNaN(d1)) {
         d1 = 1.0D;
      }

      d1 *= 64.0D * getViewScale();
      return d0 < d1 * d1;
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return true;
   }

   public void tick() {
      super.tick();
      this.calculateEntityAnimation(false);
   }

   public void aiStep() {
      if (this.lerpSteps > 0) {
         double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
         double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
         double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
         this.setYRot(this.getYRot() + (float)Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot()) / (float)this.lerpSteps);
         this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
         --this.lerpSteps;
         this.setPos(d0, d1, d2);
         this.setRot(this.getYRot(), this.getXRot());
      }

      if (this.lerpHeadSteps > 0) {
         this.yHeadRot += (float)(Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
         --this.lerpHeadSteps;
      }

      if (this.lerpDeltaMovementSteps > 0) {
         this.addDeltaMovement(new Vec3((this.lerpDeltaMovement.x - this.getDeltaMovement().x) / (double)this.lerpDeltaMovementSteps, (this.lerpDeltaMovement.y - this.getDeltaMovement().y) / (double)this.lerpDeltaMovementSteps, (this.lerpDeltaMovement.z - this.getDeltaMovement().z) / (double)this.lerpDeltaMovementSteps));
         --this.lerpDeltaMovementSteps;
      }

      this.oBob = this.bob;
      this.updateSwingTime();
      float f1;
      if (this.onGround() && !this.isDeadOrDying()) {
         f1 = (float)Math.min(0.1D, this.getDeltaMovement().horizontalDistance());
      } else {
         f1 = 0.0F;
      }

      this.bob += (f1 - this.bob) * 0.4F;
      this.level().getProfiler().push("push");
      this.pushEntities();
      this.level().getProfiler().pop();
   }

   public void lerpMotion(double d0, double d1, double d2) {
      this.lerpDeltaMovement = new Vec3(d0, d1, d2);
      this.lerpDeltaMovementSteps = this.getType().updateInterval() + 1;
   }

   protected void updatePlayerPose() {
   }

   public void sendSystemMessage(Component component) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.gui.getChat().addMessage(component);
   }
}
