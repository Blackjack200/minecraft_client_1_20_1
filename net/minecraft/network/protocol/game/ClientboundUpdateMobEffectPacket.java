package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_AMBIENT = 1;
   private static final int FLAG_VISIBLE = 2;
   private static final int FLAG_SHOW_ICON = 4;
   private final int entityId;
   private final MobEffect effect;
   private final byte effectAmplifier;
   private final int effectDurationTicks;
   private final byte flags;
   @Nullable
   private final MobEffectInstance.FactorData factorData;

   public ClientboundUpdateMobEffectPacket(int i, MobEffectInstance mobeffectinstance) {
      this.entityId = i;
      this.effect = mobeffectinstance.getEffect();
      this.effectAmplifier = (byte)(mobeffectinstance.getAmplifier() & 255);
      this.effectDurationTicks = mobeffectinstance.getDuration();
      byte b0 = 0;
      if (mobeffectinstance.isAmbient()) {
         b0 = (byte)(b0 | 1);
      }

      if (mobeffectinstance.isVisible()) {
         b0 = (byte)(b0 | 2);
      }

      if (mobeffectinstance.showIcon()) {
         b0 = (byte)(b0 | 4);
      }

      this.flags = b0;
      this.factorData = mobeffectinstance.getFactorData().orElse((MobEffectInstance.FactorData)null);
   }

   public ClientboundUpdateMobEffectPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readVarInt();
      this.effect = friendlybytebuf.readById(BuiltInRegistries.MOB_EFFECT);
      this.effectAmplifier = friendlybytebuf.readByte();
      this.effectDurationTicks = friendlybytebuf.readVarInt();
      this.flags = friendlybytebuf.readByte();
      this.factorData = friendlybytebuf.readNullable((friendlybytebuf1) -> friendlybytebuf1.readWithCodec(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
      friendlybytebuf.writeByte(this.effectAmplifier);
      friendlybytebuf.writeVarInt(this.effectDurationTicks);
      friendlybytebuf.writeByte(this.flags);
      friendlybytebuf.writeNullable(this.factorData, (friendlybytebuf1, mobeffectinstance_factordata) -> friendlybytebuf1.writeWithCodec(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC, mobeffectinstance_factordata));
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleUpdateMobEffect(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public MobEffect getEffect() {
      return this.effect;
   }

   public byte getEffectAmplifier() {
      return this.effectAmplifier;
   }

   public int getEffectDurationTicks() {
      return this.effectDurationTicks;
   }

   public boolean isEffectVisible() {
      return (this.flags & 2) == 2;
   }

   public boolean isEffectAmbient() {
      return (this.flags & 1) == 1;
   }

   public boolean effectShowsIcon() {
      return (this.flags & 4) == 4;
   }

   @Nullable
   public MobEffectInstance.FactorData getFactorData() {
      return this.factorData;
   }
}
