package net.minecraft.world.entity.player;

import net.minecraft.nbt.CompoundTag;

public class Abilities {
   public boolean invulnerable;
   public boolean flying;
   public boolean mayfly;
   public boolean instabuild;
   public boolean mayBuild = true;
   private float flyingSpeed = 0.05F;
   private float walkingSpeed = 0.1F;

   public void addSaveData(CompoundTag compoundtag) {
      CompoundTag compoundtag1 = new CompoundTag();
      compoundtag1.putBoolean("invulnerable", this.invulnerable);
      compoundtag1.putBoolean("flying", this.flying);
      compoundtag1.putBoolean("mayfly", this.mayfly);
      compoundtag1.putBoolean("instabuild", this.instabuild);
      compoundtag1.putBoolean("mayBuild", this.mayBuild);
      compoundtag1.putFloat("flySpeed", this.flyingSpeed);
      compoundtag1.putFloat("walkSpeed", this.walkingSpeed);
      compoundtag.put("abilities", compoundtag1);
   }

   public void loadSaveData(CompoundTag compoundtag) {
      if (compoundtag.contains("abilities", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("abilities");
         this.invulnerable = compoundtag1.getBoolean("invulnerable");
         this.flying = compoundtag1.getBoolean("flying");
         this.mayfly = compoundtag1.getBoolean("mayfly");
         this.instabuild = compoundtag1.getBoolean("instabuild");
         if (compoundtag1.contains("flySpeed", 99)) {
            this.flyingSpeed = compoundtag1.getFloat("flySpeed");
            this.walkingSpeed = compoundtag1.getFloat("walkSpeed");
         }

         if (compoundtag1.contains("mayBuild", 1)) {
            this.mayBuild = compoundtag1.getBoolean("mayBuild");
         }
      }

   }

   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   public void setFlyingSpeed(float f) {
      this.flyingSpeed = f;
   }

   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }

   public void setWalkingSpeed(float f) {
      this.walkingSpeed = f;
   }
}
