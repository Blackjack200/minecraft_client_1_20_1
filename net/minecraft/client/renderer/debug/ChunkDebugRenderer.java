package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   final Minecraft minecraft;
   private double lastUpdateTime = Double.MIN_VALUE;
   private final int radius = 12;
   @Nullable
   private ChunkDebugRenderer.ChunkData data;

   public ChunkDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      double d3 = (double)Util.getNanos();
      if (d3 - this.lastUpdateTime > 3.0E9D) {
         this.lastUpdateTime = d3;
         IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
         if (integratedserver != null) {
            this.data = new ChunkDebugRenderer.ChunkData(integratedserver, d0, d2);
         } else {
            this.data = null;
         }
      }

      if (this.data != null) {
         Map<ChunkPos, String> map = this.data.serverData.getNow((Map<ChunkPos, String>)null);
         double d4 = this.minecraft.gameRenderer.getMainCamera().getPosition().y * 0.85D;

         for(Map.Entry<ChunkPos, String> map_entry : this.data.clientData.entrySet()) {
            ChunkPos chunkpos = map_entry.getKey();
            String s = map_entry.getValue();
            if (map != null) {
               s = s + (String)map.get(chunkpos);
            }

            String[] astring = s.split("\n");
            int i = 0;

            for(String s1 : astring) {
               DebugRenderer.renderFloatingText(posestack, multibuffersource, s1, (double)SectionPos.sectionToBlockCoord(chunkpos.x, 8), d4 + (double)i, (double)SectionPos.sectionToBlockCoord(chunkpos.z, 8), -1, 0.15F, true, 0.0F, true);
               i -= 2;
            }
         }
      }

   }

   final class ChunkData {
      final Map<ChunkPos, String> clientData;
      final CompletableFuture<Map<ChunkPos, String>> serverData;

      ChunkData(IntegratedServer integratedserver, double d0, double d1) {
         ClientLevel clientlevel = ChunkDebugRenderer.this.minecraft.level;
         ResourceKey<Level> resourcekey = clientlevel.dimension();
         int i = SectionPos.posToSectionCoord(d0);
         int j = SectionPos.posToSectionCoord(d1);
         ImmutableMap.Builder<ChunkPos, String> immutablemap_builder = ImmutableMap.builder();
         ClientChunkCache clientchunkcache = clientlevel.getChunkSource();

         for(int k = i - 12; k <= i + 12; ++k) {
            for(int l = j - 12; l <= j + 12; ++l) {
               ChunkPos chunkpos = new ChunkPos(k, l);
               String s = "";
               LevelChunk levelchunk = clientchunkcache.getChunk(k, l, false);
               s = s + "Client: ";
               if (levelchunk == null) {
                  s = s + "0n/a\n";
               } else {
                  s = s + (levelchunk.isEmpty() ? " E" : "");
                  s = s + "\n";
               }

               immutablemap_builder.put(chunkpos, s);
            }
         }

         this.clientData = immutablemap_builder.build();
         this.serverData = integratedserver.submit(() -> {
            ServerLevel serverlevel = integratedserver.getLevel(resourcekey);
            if (serverlevel == null) {
               return ImmutableMap.of();
            } else {
               ImmutableMap.Builder<ChunkPos, String> immutablemap_builder1 = ImmutableMap.builder();
               ServerChunkCache serverchunkcache = serverlevel.getChunkSource();

               for(int k1 = i - 12; k1 <= i + 12; ++k1) {
                  for(int l1 = j - 12; l1 <= j + 12; ++l1) {
                     ChunkPos chunkpos1 = new ChunkPos(k1, l1);
                     immutablemap_builder1.put(chunkpos1, "Server: " + serverchunkcache.getChunkDebugData(chunkpos1));
                  }
               }

               return immutablemap_builder1.build();
            }
         });
      }
   }
}
