package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelParticlesPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float xDist;
   private final float yDist;
   private final float zDist;
   private final float maxSpeed;
   private final int count;
   private final boolean overrideLimiter;
   private final ParticleOptions particle;

   public <T extends ParticleOptions> ClientboundLevelParticlesPacket(T particleoptions, boolean flag, double d0, double d1, double d2, float f, float f1, float f2, float f3, int i) {
      this.particle = particleoptions;
      this.overrideLimiter = flag;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.xDist = f;
      this.yDist = f1;
      this.zDist = f2;
      this.maxSpeed = f3;
      this.count = i;
   }

   public ClientboundLevelParticlesPacket(FriendlyByteBuf friendlybytebuf) {
      ParticleType<?> particletype = friendlybytebuf.readById(BuiltInRegistries.PARTICLE_TYPE);
      this.overrideLimiter = friendlybytebuf.readBoolean();
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.xDist = friendlybytebuf.readFloat();
      this.yDist = friendlybytebuf.readFloat();
      this.zDist = friendlybytebuf.readFloat();
      this.maxSpeed = friendlybytebuf.readFloat();
      this.count = friendlybytebuf.readInt();
      this.particle = this.readParticle(friendlybytebuf, particletype);
   }

   private <T extends ParticleOptions> T readParticle(FriendlyByteBuf friendlybytebuf, ParticleType<T> particletype) {
      return particletype.getDeserializer().fromNetwork(particletype, friendlybytebuf);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeId(BuiltInRegistries.PARTICLE_TYPE, this.particle.getType());
      friendlybytebuf.writeBoolean(this.overrideLimiter);
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeFloat(this.xDist);
      friendlybytebuf.writeFloat(this.yDist);
      friendlybytebuf.writeFloat(this.zDist);
      friendlybytebuf.writeFloat(this.maxSpeed);
      friendlybytebuf.writeInt(this.count);
      this.particle.writeToNetwork(friendlybytebuf);
   }

   public boolean isOverrideLimiter() {
      return this.overrideLimiter;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public float getXDist() {
      return this.xDist;
   }

   public float getYDist() {
      return this.yDist;
   }

   public float getZDist() {
      return this.zDist;
   }

   public float getMaxSpeed() {
      return this.maxSpeed;
   }

   public int getCount() {
      return this.count;
   }

   public ParticleOptions getParticle() {
      return this.particle;
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleParticleEvent(this);
   }
}
