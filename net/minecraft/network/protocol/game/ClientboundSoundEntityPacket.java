package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket implements Packet<ClientGamePacketListener> {
   private final Holder<SoundEvent> sound;
   private final SoundSource source;
   private final int id;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundEntityPacket(Holder<SoundEvent> holder, SoundSource soundsource, Entity entity, float f, float f1, long i) {
      this.sound = holder;
      this.source = soundsource;
      this.id = entity.getId();
      this.volume = f;
      this.pitch = f1;
      this.seed = i;
   }

   public ClientboundSoundEntityPacket(FriendlyByteBuf friendlybytebuf) {
      this.sound = friendlybytebuf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork);
      this.source = friendlybytebuf.readEnum(SoundSource.class);
      this.id = friendlybytebuf.readVarInt();
      this.volume = friendlybytebuf.readFloat();
      this.pitch = friendlybytebuf.readFloat();
      this.seed = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (friendlybytebuf1, soundevent) -> soundevent.writeToNetwork(friendlybytebuf1));
      friendlybytebuf.writeEnum(this.source);
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeFloat(this.volume);
      friendlybytebuf.writeFloat(this.pitch);
      friendlybytebuf.writeLong(this.seed);
   }

   public Holder<SoundEvent> getSound() {
      return this.sound;
   }

   public SoundSource getSource() {
      return this.source;
   }

   public int getId() {
      return this.id;
   }

   public float getVolume() {
      return this.volume;
   }

   public float getPitch() {
      return this.pitch;
   }

   public long getSeed() {
      return this.seed;
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleSoundEntityEvent(this);
   }
}
