package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

public class Marker extends Entity {
   private static final String DATA_TAG = "data";
   private CompoundTag data = new CompoundTag();

   public Marker(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
      this.noPhysics = true;
   }

   public void tick() {
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag compoundtag) {
      this.data = compoundtag.getCompound("data");
   }

   protected void addAdditionalSaveData(CompoundTag compoundtag) {
      compoundtag.put("data", this.data.copy());
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      throw new IllegalStateException("Markers should never be sent");
   }

   protected boolean canAddPassenger(Entity entity) {
      return false;
   }

   protected boolean couldAcceptPassenger() {
      return false;
   }

   protected void addPassenger(Entity entity) {
      throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public boolean isIgnoringBlockTriggers() {
      return true;
   }
}
