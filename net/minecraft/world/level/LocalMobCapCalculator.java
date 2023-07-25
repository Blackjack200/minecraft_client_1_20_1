package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;

public class LocalMobCapCalculator {
   private final Long2ObjectMap<List<ServerPlayer>> playersNearChunk = new Long2ObjectOpenHashMap<>();
   private final Map<ServerPlayer, LocalMobCapCalculator.MobCounts> playerMobCounts = Maps.newHashMap();
   private final ChunkMap chunkMap;

   public LocalMobCapCalculator(ChunkMap chunkmap) {
      this.chunkMap = chunkmap;
   }

   private List<ServerPlayer> getPlayersNear(ChunkPos chunkpos) {
      return this.playersNearChunk.computeIfAbsent(chunkpos.toLong(), (i) -> this.chunkMap.getPlayersCloseForSpawning(chunkpos));
   }

   public void addMob(ChunkPos chunkpos, MobCategory mobcategory) {
      for(ServerPlayer serverplayer : this.getPlayersNear(chunkpos)) {
         this.playerMobCounts.computeIfAbsent(serverplayer, (serverplayer1) -> new LocalMobCapCalculator.MobCounts()).add(mobcategory);
      }

   }

   public boolean canSpawn(MobCategory mobcategory, ChunkPos chunkpos) {
      for(ServerPlayer serverplayer : this.getPlayersNear(chunkpos)) {
         LocalMobCapCalculator.MobCounts localmobcapcalculator_mobcounts = this.playerMobCounts.get(serverplayer);
         if (localmobcapcalculator_mobcounts == null || localmobcapcalculator_mobcounts.canSpawn(mobcategory)) {
            return true;
         }
      }

      return false;
   }

   static class MobCounts {
      private final Object2IntMap<MobCategory> counts = new Object2IntOpenHashMap<>(MobCategory.values().length);

      public void add(MobCategory mobcategory) {
         this.counts.computeInt(mobcategory, (mobcategory1, integer) -> integer == null ? 1 : integer + 1);
      }

      public boolean canSpawn(MobCategory mobcategory) {
         return this.counts.getOrDefault(mobcategory, 0) < mobcategory.getMaxInstancesPerChunk();
      }
   }
}
