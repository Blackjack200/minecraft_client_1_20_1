package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int TOLERANCE_LEVEL_ROTATION = 1;
   private final ServerLevel level;
   private final Entity entity;
   private final int updateInterval;
   private final boolean trackDelta;
   private final Consumer<Packet<?>> broadcast;
   private final VecDeltaCodec positionCodec = new VecDeltaCodec();
   private int yRotp;
   private int xRotp;
   private int yHeadRotp;
   private Vec3 ap = Vec3.ZERO;
   private int tickCount;
   private int teleportDelay;
   private List<Entity> lastPassengers = Collections.emptyList();
   private boolean wasRiding;
   private boolean wasOnGround;
   @Nullable
   private List<SynchedEntityData.DataValue<?>> trackedDataValues;

   public ServerEntity(ServerLevel serverlevel, Entity entity, int i, boolean flag, Consumer<Packet<?>> consumer) {
      this.level = serverlevel;
      this.broadcast = consumer;
      this.entity = entity;
      this.updateInterval = i;
      this.trackDelta = flag;
      this.positionCodec.setBase(entity.trackingPosition());
      this.yRotp = Mth.floor(entity.getYRot() * 256.0F / 360.0F);
      this.xRotp = Mth.floor(entity.getXRot() * 256.0F / 360.0F);
      this.yHeadRotp = Mth.floor(entity.getYHeadRot() * 256.0F / 360.0F);
      this.wasOnGround = entity.onGround();
      this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
   }

   public void sendChanges() {
      List<Entity> list = this.entity.getPassengers();
      if (!list.equals(this.lastPassengers)) {
         this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
         removedPassengers(list, this.lastPassengers).forEach((entity) -> {
            if (entity instanceof ServerPlayer serverplayer1) {
               serverplayer1.connection.teleport(serverplayer1.getX(), serverplayer1.getY(), serverplayer1.getZ(), serverplayer1.getYRot(), serverplayer1.getXRot());
            }

         });
         this.lastPassengers = list;
      }

      Entity l = this.entity;
      if (l instanceof ItemFrame itemframe) {
         if (this.tickCount % 10 == 0) {
            ItemStack itemstack = itemframe.getItem();
            if (itemstack.getItem() instanceof MapItem) {
               Integer integer = MapItem.getMapId(itemstack);
               MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, this.level);
               if (mapitemsaveddata != null) {
                  for(ServerPlayer serverplayer : this.level.players()) {
                     mapitemsaveddata.tickCarriedBy(serverplayer, itemstack);
                     Packet<?> packet = mapitemsaveddata.getUpdatePacket(integer, serverplayer);
                     if (packet != null) {
                        serverplayer.connection.send(packet);
                     }
                  }
               }
            }

            this.sendDirtyEntityData();
         }
      }

      if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
         if (this.entity.isPassenger()) {
            int i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
            int j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
            boolean flag = Math.abs(i - this.yRotp) >= 1 || Math.abs(j - this.xRotp) >= 1;
            if (flag) {
               this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.onGround()));
               this.yRotp = i;
               this.xRotp = j;
            }

            this.positionCodec.setBase(this.entity.trackingPosition());
            this.sendDirtyEntityData();
            this.wasRiding = true;
         } else {
            ++this.teleportDelay;
            int k = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
            int l = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
            Vec3 vec3 = this.entity.trackingPosition();
            boolean flag1 = this.positionCodec.delta(vec3).lengthSqr() >= (double)7.6293945E-6F;
            Packet<?> packet1 = null;
            boolean flag2 = flag1 || this.tickCount % 60 == 0;
            boolean flag3 = Math.abs(k - this.yRotp) >= 1 || Math.abs(l - this.xRotp) >= 1;
            boolean flag4 = false;
            boolean flag5 = false;
            if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
               long i1 = this.positionCodec.encodeX(vec3);
               long j1 = this.positionCodec.encodeY(vec3);
               long k1 = this.positionCodec.encodeZ(vec3);
               boolean flag6 = i1 < -32768L || i1 > 32767L || j1 < -32768L || j1 > 32767L || k1 < -32768L || k1 > 32767L;
               if (!flag6 && this.teleportDelay <= 400 && !this.wasRiding && this.wasOnGround == this.entity.onGround()) {
                  if ((!flag2 || !flag3) && !(this.entity instanceof AbstractArrow)) {
                     if (flag2) {
                        packet1 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)i1), (short)((int)j1), (short)((int)k1), this.entity.onGround());
                        flag4 = true;
                     } else if (flag3) {
                        packet1 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)k, (byte)l, this.entity.onGround());
                        flag5 = true;
                     }
                  } else {
                     packet1 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)((int)i1), (short)((int)j1), (short)((int)k1), (byte)k, (byte)l, this.entity.onGround());
                     flag4 = true;
                     flag5 = true;
                  }
               } else {
                  this.wasOnGround = this.entity.onGround();
                  this.teleportDelay = 0;
                  packet1 = new ClientboundTeleportEntityPacket(this.entity);
                  flag4 = true;
                  flag5 = true;
               }
            }

            if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0) {
               Vec3 vec31 = this.entity.getDeltaMovement();
               double d0 = vec31.distanceToSqr(this.ap);
               if (d0 > 1.0E-7D || d0 > 0.0D && vec31.lengthSqr() == 0.0D) {
                  this.ap = vec31;
                  this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
               }
            }

            if (packet1 != null) {
               this.broadcast.accept(packet1);
            }

            this.sendDirtyEntityData();
            if (flag4) {
               this.positionCodec.setBase(vec3);
            }

            if (flag5) {
               this.yRotp = k;
               this.xRotp = l;
            }

            this.wasRiding = false;
         }

         int l1 = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
         if (Math.abs(l1 - this.yHeadRotp) >= 1) {
            this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)l1));
            this.yHeadRotp = l1;
         }

         this.entity.hasImpulse = false;
      }

      ++this.tickCount;
      if (this.entity.hurtMarked) {
         this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
         this.entity.hurtMarked = false;
      }

   }

   private static Stream<Entity> removedPassengers(List<Entity> list, List<Entity> list1) {
      return list1.stream().filter((entity) -> !list.contains(entity));
   }

   public void removePairing(ServerPlayer serverplayer) {
      this.entity.stopSeenByPlayer(serverplayer);
      serverplayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
   }

   public void addPairing(ServerPlayer serverplayer) {
      List<Packet<ClientGamePacketListener>> list = new ArrayList<>();
      this.sendPairingData(serverplayer, list::add);
      serverplayer.connection.send(new ClientboundBundlePacket(list));
      this.entity.startSeenByPlayer(serverplayer);
   }

   public void sendPairingData(ServerPlayer serverplayer, Consumer<Packet<ClientGamePacketListener>> consumer) {
      if (this.entity.isRemoved()) {
         LOGGER.warn("Fetching packet for removed entity {}", (Object)this.entity);
      }

      Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket();
      this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
      consumer.accept(packet);
      if (this.trackedDataValues != null) {
         consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
      }

      boolean flag = this.trackDelta;
      if (this.entity instanceof LivingEntity) {
         Collection<AttributeInstance> collection = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
         if (!collection.isEmpty()) {
            consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), collection));
         }

         if (((LivingEntity)this.entity).isFallFlying()) {
            flag = true;
         }
      }

      this.ap = this.entity.getDeltaMovement();
      if (flag && !(this.entity instanceof LivingEntity)) {
         consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
      }

      if (this.entity instanceof LivingEntity) {
         List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayList();

         for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = ((LivingEntity)this.entity).getItemBySlot(equipmentslot);
            if (!itemstack.isEmpty()) {
               list.add(Pair.of(equipmentslot, itemstack.copy()));
            }
         }

         if (!list.isEmpty()) {
            consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
         }
      }

      if (!this.entity.getPassengers().isEmpty()) {
         consumer.accept(new ClientboundSetPassengersPacket(this.entity));
      }

      if (this.entity.isPassenger()) {
         consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
      }

      if (this.entity instanceof Mob) {
         Mob mob = (Mob)this.entity;
         if (mob.isLeashed()) {
            consumer.accept(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
         }
      }

   }

   private void sendDirtyEntityData() {
      SynchedEntityData synchedentitydata = this.entity.getEntityData();
      List<SynchedEntityData.DataValue<?>> list = synchedentitydata.packDirty();
      if (list != null) {
         this.trackedDataValues = synchedentitydata.getNonDefaultValues();
         this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
      }

      if (this.entity instanceof LivingEntity) {
         Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getDirtyAttributes();
         if (!set.isEmpty()) {
            this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), set));
         }

         set.clear();
      }

   }

   private void broadcastAndSend(Packet<?> packet) {
      this.broadcast.accept(packet);
      if (this.entity instanceof ServerPlayer) {
         ((ServerPlayer)this.entity).connection.send(packet);
      }

   }
}
