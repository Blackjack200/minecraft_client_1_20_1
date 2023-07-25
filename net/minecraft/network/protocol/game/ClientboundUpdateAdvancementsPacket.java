package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundUpdateAdvancementsPacket implements Packet<ClientGamePacketListener> {
   private final boolean reset;
   private final Map<ResourceLocation, Advancement.Builder> added;
   private final Set<ResourceLocation> removed;
   private final Map<ResourceLocation, AdvancementProgress> progress;

   public ClientboundUpdateAdvancementsPacket(boolean flag, Collection<Advancement> collection, Set<ResourceLocation> set, Map<ResourceLocation, AdvancementProgress> map) {
      this.reset = flag;
      ImmutableMap.Builder<ResourceLocation, Advancement.Builder> immutablemap_builder = ImmutableMap.builder();

      for(Advancement advancement : collection) {
         immutablemap_builder.put(advancement.getId(), advancement.deconstruct());
      }

      this.added = immutablemap_builder.build();
      this.removed = ImmutableSet.copyOf(set);
      this.progress = ImmutableMap.copyOf(map);
   }

   public ClientboundUpdateAdvancementsPacket(FriendlyByteBuf friendlybytebuf) {
      this.reset = friendlybytebuf.readBoolean();
      this.added = friendlybytebuf.readMap(FriendlyByteBuf::readResourceLocation, Advancement.Builder::fromNetwork);
      this.removed = friendlybytebuf.readCollection(Sets::newLinkedHashSetWithExpectedSize, FriendlyByteBuf::readResourceLocation);
      this.progress = friendlybytebuf.readMap(FriendlyByteBuf::readResourceLocation, AdvancementProgress::fromNetwork);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBoolean(this.reset);
      friendlybytebuf.writeMap(this.added, FriendlyByteBuf::writeResourceLocation, (friendlybytebuf2, advancement_builder) -> advancement_builder.serializeToNetwork(friendlybytebuf2));
      friendlybytebuf.writeCollection(this.removed, FriendlyByteBuf::writeResourceLocation);
      friendlybytebuf.writeMap(this.progress, FriendlyByteBuf::writeResourceLocation, (friendlybytebuf1, advancementprogress) -> advancementprogress.serializeToNetwork(friendlybytebuf1));
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleUpdateAdvancementsPacket(this);
   }

   public Map<ResourceLocation, Advancement.Builder> getAdded() {
      return this.added;
   }

   public Set<ResourceLocation> getRemoved() {
      return this.removed;
   }

   public Map<ResourceLocation, AdvancementProgress> getProgress() {
      return this.progress;
   }

   public boolean shouldReset() {
      return this.reset;
   }
}
