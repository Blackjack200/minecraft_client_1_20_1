package net.minecraft.world.entity.boss;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;

public class EnderDragonPart extends Entity {
   public final EnderDragon parentMob;
   public final String name;
   private final EntityDimensions size;

   public EnderDragonPart(EnderDragon enderdragon, String s, float f, float f1) {
      super(enderdragon.getType(), enderdragon.level());
      this.size = EntityDimensions.scalable(f, f1);
      this.refreshDimensions();
      this.parentMob = enderdragon;
      this.name = s;
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
   }

   public boolean isPickable() {
      return true;
   }

   @Nullable
   public ItemStack getPickResult() {
      return this.parentMob.getPickResult();
   }

   public boolean hurt(DamageSource damagesource, float f) {
      return this.isInvulnerableTo(damagesource) ? false : this.parentMob.hurt(this, damagesource, f);
   }

   public boolean is(Entity entity) {
      return this == entity || this.parentMob == entity;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   public EntityDimensions getDimensions(Pose pose) {
      return this.size;
   }

   public boolean shouldBeSaved() {
      return false;
   }
}
