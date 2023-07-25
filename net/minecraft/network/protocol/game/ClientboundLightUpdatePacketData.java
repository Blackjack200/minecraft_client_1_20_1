package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacketData {
   private final BitSet skyYMask;
   private final BitSet blockYMask;
   private final BitSet emptySkyYMask;
   private final BitSet emptyBlockYMask;
   private final List<byte[]> skyUpdates;
   private final List<byte[]> blockUpdates;

   public ClientboundLightUpdatePacketData(ChunkPos chunkpos, LevelLightEngine levellightengine, @Nullable BitSet bitset, @Nullable BitSet bitset1) {
      this.skyYMask = new BitSet();
      this.blockYMask = new BitSet();
      this.emptySkyYMask = new BitSet();
      this.emptyBlockYMask = new BitSet();
      this.skyUpdates = Lists.newArrayList();
      this.blockUpdates = Lists.newArrayList();

      for(int i = 0; i < levellightengine.getLightSectionCount(); ++i) {
         if (bitset == null || bitset.get(i)) {
            this.prepareSectionData(chunkpos, levellightengine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
         }

         if (bitset1 == null || bitset1.get(i)) {
            this.prepareSectionData(chunkpos, levellightengine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
         }
      }

   }

   public ClientboundLightUpdatePacketData(FriendlyByteBuf friendlybytebuf, int i, int j) {
      this.skyYMask = friendlybytebuf.readBitSet();
      this.blockYMask = friendlybytebuf.readBitSet();
      this.emptySkyYMask = friendlybytebuf.readBitSet();
      this.emptyBlockYMask = friendlybytebuf.readBitSet();
      this.skyUpdates = friendlybytebuf.readList((friendlybytebuf2) -> friendlybytebuf2.readByteArray(2048));
      this.blockUpdates = friendlybytebuf.readList((friendlybytebuf1) -> friendlybytebuf1.readByteArray(2048));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBitSet(this.skyYMask);
      friendlybytebuf.writeBitSet(this.blockYMask);
      friendlybytebuf.writeBitSet(this.emptySkyYMask);
      friendlybytebuf.writeBitSet(this.emptyBlockYMask);
      friendlybytebuf.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
      friendlybytebuf.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
   }

   private void prepareSectionData(ChunkPos chunkpos, LevelLightEngine levellightengine, LightLayer lightlayer, int i, BitSet bitset, BitSet bitset1, List<byte[]> list) {
      DataLayer datalayer = levellightengine.getLayerListener(lightlayer).getDataLayerData(SectionPos.of(chunkpos, levellightengine.getMinLightSection() + i));
      if (datalayer != null) {
         if (datalayer.isEmpty()) {
            bitset1.set(i);
         } else {
            bitset.set(i);
            list.add(datalayer.copy().getData());
         }
      }

   }

   public BitSet getSkyYMask() {
      return this.skyYMask;
   }

   public BitSet getEmptySkyYMask() {
      return this.emptySkyYMask;
   }

   public List<byte[]> getSkyUpdates() {
      return this.skyUpdates;
   }

   public BitSet getBlockYMask() {
      return this.blockYMask;
   }

   public BitSet getEmptyBlockYMask() {
      return this.emptyBlockYMask;
   }

   public List<byte[]> getBlockUpdates() {
      return this.blockUpdates;
   }
}
