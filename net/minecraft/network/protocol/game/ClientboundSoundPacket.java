package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class ClientboundSoundPacket implements Packet<ClientGamePacketListener> {
   public static final float LOCATION_ACCURACY = 8.0F;
   private final Holder<SoundEvent> sound;
   private final SoundSource source;
   private final int x;
   private final int y;
   private final int z;
   private final float volume;
   private final float pitch;
   private final long seed;

   public ClientboundSoundPacket(Holder<SoundEvent> holder, SoundSource soundsource, double d0, double d1, double d2, float f, float f1, long i) {
      this.sound = holder;
      this.source = soundsource;
      this.x = (int)(d0 * 8.0D);
      this.y = (int)(d1 * 8.0D);
      this.z = (int)(d2 * 8.0D);
      this.volume = f;
      this.pitch = f1;
      this.seed = i;
   }

   public ClientboundSoundPacket(FriendlyByteBuf friendlybytebuf) {
      this.sound = friendlybytebuf.readById(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), SoundEvent::readFromNetwork);
      this.source = friendlybytebuf.readEnum(SoundSource.class);
      this.x = friendlybytebuf.readInt();
      this.y = friendlybytebuf.readInt();
      this.z = friendlybytebuf.readInt();
      this.volume = friendlybytebuf.readFloat();
      this.pitch = friendlybytebuf.readFloat();
      this.seed = friendlybytebuf.readLong();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeId(BuiltInRegistries.SOUND_EVENT.asHolderIdMap(), this.sound, (friendlybytebuf1, soundevent) -> soundevent.writeToNetwork(friendlybytebuf1));
      friendlybytebuf.writeEnum(this.source);
      friendlybytebuf.writeInt(this.x);
      friendlybytebuf.writeInt(this.y);
      friendlybytebuf.writeInt(this.z);
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

   public double getX() {
      return (double)((float)this.x / 8.0F);
   }

   public double getY() {
      return (double)((float)this.y / 8.0F);
   }

   public double getZ() {
      return (double)((float)this.z / 8.0F);
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
      clientgamepacketlistener.handleSoundEvent(this);
   }
}
