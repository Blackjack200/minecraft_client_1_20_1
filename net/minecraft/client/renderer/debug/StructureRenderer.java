package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();
   private static final int MAX_RENDER_DIST = 500;

   public StructureRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      LevelAccessor levelaccessor = this.minecraft.level;
      DimensionType dimensiontype = levelaccessor.dimensionType();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.lines());
      if (this.postMainBoxes.containsKey(dimensiontype)) {
         for(BoundingBox boundingbox : this.postMainBoxes.get(dimensiontype).values()) {
            if (blockpos.closerThan(boundingbox.getCenter(), 500.0D)) {
               LevelRenderer.renderLineBox(posestack, vertexconsumer, (double)boundingbox.minX() - d0, (double)boundingbox.minY() - d1, (double)boundingbox.minZ() - d2, (double)(boundingbox.maxX() + 1) - d0, (double)(boundingbox.maxY() + 1) - d1, (double)(boundingbox.maxZ() + 1) - d2, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.postPiecesBoxes.containsKey(dimensiontype)) {
         for(Map.Entry<String, BoundingBox> map_entry : this.postPiecesBoxes.get(dimensiontype).entrySet()) {
            String s = map_entry.getKey();
            BoundingBox boundingbox1 = map_entry.getValue();
            Boolean obool = this.startPiecesMap.get(dimensiontype).get(s);
            if (blockpos.closerThan(boundingbox1.getCenter(), 500.0D)) {
               if (obool) {
                  LevelRenderer.renderLineBox(posestack, vertexconsumer, (double)boundingbox1.minX() - d0, (double)boundingbox1.minY() - d1, (double)boundingbox1.minZ() - d2, (double)(boundingbox1.maxX() + 1) - d0, (double)(boundingbox1.maxY() + 1) - d1, (double)(boundingbox1.maxZ() + 1) - d2, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  LevelRenderer.renderLineBox(posestack, vertexconsumer, (double)boundingbox1.minX() - d0, (double)boundingbox1.minY() - d1, (double)boundingbox1.minZ() - d2, (double)(boundingbox1.maxX() + 1) - d0, (double)(boundingbox1.maxY() + 1) - d1, (double)(boundingbox1.maxZ() + 1) - d2, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void addBoundingBox(BoundingBox boundingbox, List<BoundingBox> list, List<Boolean> list1, DimensionType dimensiontype) {
      if (!this.postMainBoxes.containsKey(dimensiontype)) {
         this.postMainBoxes.put(dimensiontype, Maps.newHashMap());
      }

      if (!this.postPiecesBoxes.containsKey(dimensiontype)) {
         this.postPiecesBoxes.put(dimensiontype, Maps.newHashMap());
         this.startPiecesMap.put(dimensiontype, Maps.newHashMap());
      }

      this.postMainBoxes.get(dimensiontype).put(boundingbox.toString(), boundingbox);

      for(int i = 0; i < list.size(); ++i) {
         BoundingBox boundingbox1 = list.get(i);
         Boolean obool = list1.get(i);
         this.postPiecesBoxes.get(dimensiontype).put(boundingbox1.toString(), boundingbox1);
         this.startPiecesMap.get(dimensiontype).put(boundingbox1.toString(), obool);
      }

   }

   public void clear() {
      this.postMainBoxes.clear();
      this.postPiecesBoxes.clear();
      this.startPiecesMap.clear();
   }
}
