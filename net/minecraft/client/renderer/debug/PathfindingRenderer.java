package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

public class PathfindingRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Map<Integer, Path> pathMap = Maps.newHashMap();
   private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
   private final Map<Integer, Long> creationMap = Maps.newHashMap();
   private static final long TIMEOUT = 5000L;
   private static final float MAX_RENDER_DIST = 80.0F;
   private static final boolean SHOW_OPEN_CLOSED = true;
   private static final boolean SHOW_OPEN_CLOSED_COST_MALUS = false;
   private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_TEXT = false;
   private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_BOX = true;
   private static final boolean SHOW_GROUND_LABELS = true;
   private static final float TEXT_SCALE = 0.02F;

   public void addPath(int i, Path path, float f) {
      this.pathMap.put(i, path);
      this.creationMap.put(i, Util.getMillis());
      this.pathMaxDist.put(i, f);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      if (!this.pathMap.isEmpty()) {
         long i = Util.getMillis();

         for(Integer integer : this.pathMap.keySet()) {
            Path path = this.pathMap.get(integer);
            float f = this.pathMaxDist.get(integer);
            renderPath(posestack, multibuffersource, path, f, true, true, d0, d1, d2);
         }

         for(Integer integer1 : this.creationMap.keySet().toArray(new Integer[0])) {
            if (i - this.creationMap.get(integer1) > 5000L) {
               this.pathMap.remove(integer1);
               this.creationMap.remove(integer1);
            }
         }

      }
   }

   public static void renderPath(PoseStack posestack, MultiBufferSource multibuffersource, Path path, float f, boolean flag, boolean flag1, double d0, double d1, double d2) {
      renderPathLine(posestack, multibuffersource.getBuffer(RenderType.debugLineStrip(6.0D)), path, d0, d1, d2);
      BlockPos blockpos = path.getTarget();
      if (distanceToCamera(blockpos, d0, d1, d2) <= 80.0F) {
         DebugRenderer.renderFilledBox(posestack, multibuffersource, (new AABB((double)((float)blockpos.getX() + 0.25F), (double)((float)blockpos.getY() + 0.25F), (double)blockpos.getZ() + 0.25D, (double)((float)blockpos.getX() + 0.75F), (double)((float)blockpos.getY() + 0.75F), (double)((float)blockpos.getZ() + 0.75F))).move(-d0, -d1, -d2), 0.0F, 1.0F, 0.0F, 0.5F);

         for(int i = 0; i < path.getNodeCount(); ++i) {
            Node node = path.getNode(i);
            if (distanceToCamera(node.asBlockPos(), d0, d1, d2) <= 80.0F) {
               float f1 = i == path.getNextNodeIndex() ? 1.0F : 0.0F;
               float f2 = i == path.getNextNodeIndex() ? 0.0F : 1.0F;
               DebugRenderer.renderFilledBox(posestack, multibuffersource, (new AABB((double)((float)node.x + 0.5F - f), (double)((float)node.y + 0.01F * (float)i), (double)((float)node.z + 0.5F - f), (double)((float)node.x + 0.5F + f), (double)((float)node.y + 0.25F + 0.01F * (float)i), (double)((float)node.z + 0.5F + f))).move(-d0, -d1, -d2), f1, 0.0F, f2, 0.5F);
            }
         }
      }

      if (flag) {
         for(Node node1 : path.getClosedSet()) {
            if (distanceToCamera(node1.asBlockPos(), d0, d1, d2) <= 80.0F) {
               DebugRenderer.renderFilledBox(posestack, multibuffersource, (new AABB((double)((float)node1.x + 0.5F - f / 2.0F), (double)((float)node1.y + 0.01F), (double)((float)node1.z + 0.5F - f / 2.0F), (double)((float)node1.x + 0.5F + f / 2.0F), (double)node1.y + 0.1D, (double)((float)node1.z + 0.5F + f / 2.0F))).move(-d0, -d1, -d2), 1.0F, 0.8F, 0.8F, 0.5F);
            }
         }

         for(Node node2 : path.getOpenSet()) {
            if (distanceToCamera(node2.asBlockPos(), d0, d1, d2) <= 80.0F) {
               DebugRenderer.renderFilledBox(posestack, multibuffersource, (new AABB((double)((float)node2.x + 0.5F - f / 2.0F), (double)((float)node2.y + 0.01F), (double)((float)node2.z + 0.5F - f / 2.0F), (double)((float)node2.x + 0.5F + f / 2.0F), (double)node2.y + 0.1D, (double)((float)node2.z + 0.5F + f / 2.0F))).move(-d0, -d1, -d2), 0.8F, 1.0F, 1.0F, 0.5F);
            }
         }
      }

      if (flag1) {
         for(int j = 0; j < path.getNodeCount(); ++j) {
            Node node3 = path.getNode(j);
            if (distanceToCamera(node3.asBlockPos(), d0, d1, d2) <= 80.0F) {
               DebugRenderer.renderFloatingText(posestack, multibuffersource, String.valueOf((Object)node3.type), (double)node3.x + 0.5D, (double)node3.y + 0.75D, (double)node3.z + 0.5D, -1, 0.02F, true, 0.0F, true);
               DebugRenderer.renderFloatingText(posestack, multibuffersource, String.format(Locale.ROOT, "%.2f", node3.costMalus), (double)node3.x + 0.5D, (double)node3.y + 0.25D, (double)node3.z + 0.5D, -1, 0.02F, true, 0.0F, true);
            }
         }
      }

   }

   public static void renderPathLine(PoseStack posestack, VertexConsumer vertexconsumer, Path path, double d0, double d1, double d2) {
      for(int i = 0; i < path.getNodeCount(); ++i) {
         Node node = path.getNode(i);
         if (!(distanceToCamera(node.asBlockPos(), d0, d1, d2) > 80.0F)) {
            float f = (float)i / (float)path.getNodeCount() * 0.33F;
            int j = i == 0 ? 0 : Mth.hsvToRgb(f, 0.9F, 0.9F);
            int k = j >> 16 & 255;
            int l = j >> 8 & 255;
            int i1 = j & 255;
            vertexconsumer.vertex(posestack.last().pose(), (float)((double)node.x - d0 + 0.5D), (float)((double)node.y - d1 + 0.5D), (float)((double)node.z - d2 + 0.5D)).color(k, l, i1, 255).endVertex();
         }
      }

   }

   private static float distanceToCamera(BlockPos blockpos, double d0, double d1, double d2) {
      return (float)(Math.abs((double)blockpos.getX() - d0) + Math.abs((double)blockpos.getY() - d1) + Math.abs((double)blockpos.getZ() - d2));
   }
}
