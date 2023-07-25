package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

public class GameTestDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final float PADDING = 0.02F;
   private final Map<BlockPos, GameTestDebugRenderer.Marker> markers = Maps.newHashMap();

   public void addMarker(BlockPos blockpos, int i, String s, int j) {
      this.markers.put(blockpos, new GameTestDebugRenderer.Marker(i, s, Util.getMillis() + (long)j));
   }

   public void clear() {
      this.markers.clear();
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      long i = Util.getMillis();
      this.markers.entrySet().removeIf((map_entry) -> i > (map_entry.getValue()).removeAtTime);
      this.markers.forEach((blockpos, gametestdebugrenderer_marker) -> this.renderMarker(posestack, multibuffersource, blockpos, gametestdebugrenderer_marker));
   }

   private void renderMarker(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos, GameTestDebugRenderer.Marker gametestdebugrenderer_marker) {
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.02F, gametestdebugrenderer_marker.getR(), gametestdebugrenderer_marker.getG(), gametestdebugrenderer_marker.getB(), gametestdebugrenderer_marker.getA() * 0.75F);
      if (!gametestdebugrenderer_marker.text.isEmpty()) {
         double d0 = (double)blockpos.getX() + 0.5D;
         double d1 = (double)blockpos.getY() + 1.2D;
         double d2 = (double)blockpos.getZ() + 0.5D;
         DebugRenderer.renderFloatingText(posestack, multibuffersource, gametestdebugrenderer_marker.text, d0, d1, d2, -1, 0.01F, true, 0.0F, true);
      }

   }

   static class Marker {
      public int color;
      public String text;
      public long removeAtTime;

      public Marker(int i, String s, long j) {
         this.color = i;
         this.text = s;
         this.removeAtTime = j;
      }

      public float getR() {
         return (float)(this.color >> 16 & 255) / 255.0F;
      }

      public float getG() {
         return (float)(this.color >> 8 & 255) / 255.0F;
      }

      public float getB() {
         return (float)(this.color & 255) / 255.0F;
      }

      public float getA() {
         return (float)(this.color >> 24 & 255) / 255.0F;
      }
   }
}
