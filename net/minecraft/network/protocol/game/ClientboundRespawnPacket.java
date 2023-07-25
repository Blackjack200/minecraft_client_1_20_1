package net.minecraft.network.protocol.game;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener> {
   public static final byte KEEP_ATTRIBUTES = 1;
   public static final byte KEEP_ENTITY_DATA = 2;
   public static final byte KEEP_ALL_DATA = 3;
   private final ResourceKey<DimensionType> dimensionType;
   private final ResourceKey<Level> dimension;
   private final long seed;
   private final GameType playerGameType;
   @Nullable
   private final GameType previousPlayerGameType;
   private final boolean isDebug;
   private final boolean isFlat;
   private final byte dataToKeep;
   private final Optional<GlobalPos> lastDeathLocation;
   private final int portalCooldown;

   public ClientboundRespawnPacket(ResourceKey<DimensionType> resourcekey, ResourceKey<Level> resourcekey1, long i, GameType gametype, @Nullable GameType gametype1, boolean flag, boolean flag1, byte b0, Optional<GlobalPos> optional, int j) {
      this.dimensionType = resourcekey;
      this.dimension = resourcekey1;
      this.seed = i;
      this.playerGameType = gametype;
      this.previousPlayerGameType = gametype1;
      this.isDebug = flag;
      this.isFlat = flag1;
      this.dataToKeep = b0;
      this.lastDeathLocation = optional;
      this.portalCooldown = j;
   }

   public ClientboundRespawnPacket(FriendlyByteBuf friendlybytebuf) {
      this.dimensionType = friendlybytebuf.readResourceKey(Registries.DIMENSION_TYPE);
      this.dimension = friendlybytebuf.readResourceKey(Registries.DIMENSION);
      this.seed = friendlybytebuf.readLong();
      this.playerGameType = GameType.byId(friendlybytebuf.readUnsignedByte());
      this.previousPlayerGameType = GameType.byNullableId(friendlybytebuf.readByte());
      this.isDebug = friendlybytebuf.readBoolean();
      this.isFlat = friendlybytebuf.readBoolean();
      this.dataToKeep = friendlybytebuf.readByte();
      this.lastDeathLocation = friendlybytebuf.readOptional(FriendlyByteBuf::readGlobalPos);
      this.portalCooldown = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeResourceKey(this.dimensionType);
      friendlybytebuf.writeResourceKey(this.dimension);
      friendlybytebuf.writeLong(this.seed);
      friendlybytebuf.writeByte(this.playerGameType.getId());
      friendlybytebuf.writeByte(GameType.getNullableId(this.previousPlayerGameType));
      friendlybytebuf.writeBoolean(this.isDebug);
      friendlybytebuf.writeBoolean(this.isFlat);
      friendlybytebuf.writeByte(this.dataToKeep);
      friendlybytebuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
      friendlybytebuf.writeVarInt(this.portalCooldown);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleRespawn(this);
   }

   public ResourceKey<DimensionType> getDimensionType() {
      return this.dimensionType;
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public long getSeed() {
      return this.seed;
   }

   public GameType getPlayerGameType() {
      return this.playerGameType;
   }

   @Nullable
   public GameType getPreviousPlayerGameType() {
      return this.previousPlayerGameType;
   }

   public boolean isDebug() {
      return this.isDebug;
   }

   public boolean isFlat() {
      return this.isFlat;
   }

   public boolean shouldKeep(byte b0) {
      return (this.dataToKeep & b0) != 0;
   }

   public Optional<GlobalPos> getLastDeathLocation() {
      return this.lastDeathLocation;
   }

   public int getPortalCooldown() {
      return this.portalCooldown;
   }
}
