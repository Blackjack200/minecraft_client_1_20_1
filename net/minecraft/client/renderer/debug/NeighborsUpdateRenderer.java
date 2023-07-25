package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

   NeighborsUpdateRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void addUpdate(long i, BlockPos blockpos) {
      Map<BlockPos, Integer> map = this.lastUpdate.computeIfAbsent(i, (olong) -> Maps.newHashMap());
      int j = map.getOrDefault(blockpos, 0);
      map.put(blockpos, j + 1);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      long i = this.minecraft.level.getGameTime();
      int j = 200;
      double d3 = 0.0025D;
      Set<BlockPos> set = Sets.newHashSet();
      Map<BlockPos, Integer> map = Maps.newHashMap();
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());
      Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> iterator = this.lastUpdate.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<Long, Map<BlockPos, Integer>> map_entry = iterator.next();
         Long olong = map_entry.getKey();
         Map<BlockPos, Integer> map1 = map_entry.getValue();
         long k = i - olong;
         if (k > 200L) {
            iterator.remove();
         } else {
            for(Map.Entry<BlockPos, Integer> map_entry1 : map1.entrySet()) {
               BlockPos blockpos = map_entry1.getKey();
               Integer integer = map_entry1.getValue();
               if (set.add(blockpos)) {
                  AABB aabb = (new AABB(BlockPos.ZERO)).inflate(0.002D).deflate(0.0025D * (double)k).move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()).move(-d0, -d1, -d2);
                  LevelRenderer.renderLineBox(posestack, vertexconsumer, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, 1.0F, 1.0F, 1.0F, 1.0F);
                  map.put(blockpos, integer);
               }
            }
         }
      }

      for(Map.Entry<BlockPos, Integer> map_entry2 : map.entrySet()) {
         BlockPos blockpos1 = map_entry2.getKey();
         Integer integer1 = map_entry2.getValue();
         DebugRenderer.renderFloatingText(posestack, multibuffersource, String.valueOf((Object)integer1), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), -1);
      }

   }
}
