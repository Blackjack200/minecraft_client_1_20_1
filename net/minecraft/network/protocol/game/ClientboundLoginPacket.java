package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public record ClientboundLoginPacket(int playerId, boolean hardcore, GameType gameType, @Nullable GameType previousGameType, Set<ResourceKey<Level>> levels, RegistryAccess.Frozen registryHolder, ResourceKey<DimensionType> dimensionType, ResourceKey<Level> dimension, long seed, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean isDebug, boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown) implements Packet<ClientGamePacketListener> {
   private static final RegistryOps<Tag> BUILTIN_CONTEXT_OPS = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

   public ClientboundLoginPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readInt(), friendlybytebuf.readBoolean(), GameType.byId(friendlybytebuf.readByte()), GameType.byNullableId(friendlybytebuf.readByte()), friendlybytebuf.readCollection(Sets::newHashSetWithExpectedSize, (friendlybytebuf1) -> friendlybytebuf1.readResourceKey(Registries.DIMENSION)), friendlybytebuf.readWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC).freeze(), friendlybytebuf.readResourceKey(Registries.DIMENSION_TYPE), friendlybytebuf.readResourceKey(Registries.DIMENSION), friendlybytebuf.readLong(), friendlybytebuf.readVarInt(), friendlybytebuf.readVarInt(), friendlybytebuf.readVarInt(), friendlybytebuf.readBoolean(), friendlybytebuf.readBoolean(), friendlybytebuf.readBoolean(), friendlybytebuf.readBoolean(), friendlybytebuf.readOptional(FriendlyByteBuf::readGlobalPos), friendlybytebuf.readVarInt());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.playerId);
      friendlybytebuf.writeBoolean(this.hardcore);
      friendlybytebuf.writeByte(this.gameType.getId());
      friendlybytebuf.writeByte(GameType.getNullableId(this.previousGameType));
      friendlybytebuf.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
      friendlybytebuf.writeWithCodec(BUILTIN_CONTEXT_OPS, RegistrySynchronization.NETWORK_CODEC, this.registryHolder);
      friendlybytebuf.writeResourceKey(this.dimensionType);
      friendlybytebuf.writeResourceKey(this.dimension);
      friendlybytebuf.writeLong(this.seed);
      friendlybytebuf.writeVarInt(this.maxPlayers);
      friendlybytebuf.writeVarInt(this.chunkRadius);
      friendlybytebuf.writeVarInt(this.simulationDistance);
      friendlybytebuf.writeBoolean(this.reducedDebugInfo);
      friendlybytebuf.writeBoolean(this.showDeathScreen);
      friendlybytebuf.writeBoolean(this.isDebug);
      friendlybytebuf.writeBoolean(this.isFlat);
      friendlybytebuf.writeOptional(this.lastDeathLocation, FriendlyByteBuf::writeGlobalPos);
      friendlybytebuf.writeVarInt(this.portalCooldown);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleLogin(this);
   }
}
