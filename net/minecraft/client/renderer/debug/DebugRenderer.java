package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DebugRenderer {
   public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
   public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
   public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
   public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
   public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
   public final DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
   public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
   public final StructureRenderer structureRenderer;
   public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
   public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
   public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
   public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
   public final BrainDebugRenderer brainDebugRenderer;
   public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
   public final BeeDebugRenderer beeDebugRenderer;
   public final RaidDebugRenderer raidDebugRenderer;
   public final GoalSelectorDebugRenderer goalSelectorRenderer;
   public final GameTestDebugRenderer gameTestDebugRenderer;
   public final GameEventListenerRenderer gameEventListenerRenderer;
   public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
   private boolean renderChunkborder;

   public DebugRenderer(Minecraft minecraft) {
      this.waterDebugRenderer = new WaterDebugRenderer(minecraft);
      this.chunkBorderRenderer = new ChunkBorderRenderer(minecraft);
      this.heightMapRenderer = new HeightMapRenderer(minecraft);
      this.collisionBoxRenderer = new CollisionBoxRenderer(minecraft);
      this.supportBlockRenderer = new SupportBlockRenderer(minecraft);
      this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(minecraft);
      this.structureRenderer = new StructureRenderer(minecraft);
      this.lightDebugRenderer = new LightDebugRenderer(minecraft);
      this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
      this.solidFaceRenderer = new SolidFaceRenderer(minecraft);
      this.chunkRenderer = new ChunkDebugRenderer(minecraft);
      this.brainDebugRenderer = new BrainDebugRenderer(minecraft);
      this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
      this.beeDebugRenderer = new BeeDebugRenderer(minecraft);
      this.raidDebugRenderer = new RaidDebugRenderer(minecraft);
      this.goalSelectorRenderer = new GoalSelectorDebugRenderer(minecraft);
      this.gameTestDebugRenderer = new GameTestDebugRenderer();
      this.gameEventListenerRenderer = new GameEventListenerRenderer(minecraft);
      this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(minecraft, LightLayer.SKY);
   }

   public void clear() {
      this.pathfindingRenderer.clear();
      this.waterDebugRenderer.clear();
      this.chunkBorderRenderer.clear();
      this.heightMapRenderer.clear();
      this.collisionBoxRenderer.clear();
      this.supportBlockRenderer.clear();
      this.neighborsUpdateRenderer.clear();
      this.structureRenderer.clear();
      this.lightDebugRenderer.clear();
      this.worldGenAttemptRenderer.clear();
      this.solidFaceRenderer.clear();
      this.chunkRenderer.clear();
      this.brainDebugRenderer.clear();
      this.villageSectionsDebugRenderer.clear();
      this.beeDebugRenderer.clear();
      this.raidDebugRenderer.clear();
      this.goalSelectorRenderer.clear();
      this.gameTestDebugRenderer.clear();
      this.gameEventListenerRenderer.clear();
      this.skyLightSectionDebugRenderer.clear();
   }

   public boolean switchRenderChunkborder() {
      this.renderChunkborder = !this.renderChunkborder;
      return this.renderChunkborder;
   }

   public void render(PoseStack posestack, MultiBufferSource.BufferSource multibuffersource_buffersource, double d0, double d1, double d2) {
      if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
         this.chunkBorderRenderer.render(posestack, multibuffersource_buffersource, d0, d1, d2);
      }

      this.gameTestDebugRenderer.render(posestack, multibuffersource_buffersource, d0, d1, d2);
   }

   public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int i) {
      if (entity == null) {
         return Optional.empty();
      } else {
         Vec3 vec3 = entity.getEyePosition();
         Vec3 vec31 = entity.getViewVector(1.0F).scale((double)i);
         Vec3 vec32 = vec3.add(vec31);
         AABB aabb = entity.getBoundingBox().expandTowards(vec31).inflate(1.0D);
         int j = i * i;
         Predicate<Entity> predicate = (entity1) -> !entity1.isSpectator() && entity1.isPickable();
         EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(entity, vec3, vec32, aabb, predicate, (double)j);
         if (entityhitresult == null) {
            return Optional.empty();
         } else {
            return vec3.distanceToSqr(entityhitresult.getLocation()) > (double)j ? Optional.empty() : Optional.of(entityhitresult.getEntity());
         }
      }
   }

   public static void renderFilledBox(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos, BlockPos blockpos1, float f, float f1, float f2, float f3) {
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition().reverse();
         AABB aabb = (new AABB(blockpos, blockpos1)).move(vec3);
         renderFilledBox(posestack, multibuffersource, aabb, f, f1, f2, f3);
      }
   }

   public static void renderFilledBox(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos, float f, float f1, float f2, float f3, float f4) {
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition().reverse();
         AABB aabb = (new AABB(blockpos)).move(vec3).inflate((double)f);
         renderFilledBox(posestack, multibuffersource, aabb, f1, f2, f3, f4);
      }
   }

   public static void renderFilledBox(PoseStack posestack, MultiBufferSource multibuffersource, AABB aabb, float f, float f1, float f2, float f3) {
      renderFilledBox(posestack, multibuffersource, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, f, f1, f2, f3);
   }

   public static void renderFilledBox(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2, double d3, double d4, double d5, float f, float f1, float f2, float f3) {
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.debugFilledBox());
      LevelRenderer.addChainedFilledBoxVertices(posestack, vertexconsumer, d0, d1, d2, d3, d4, d5, f, f1, f2, f3);
   }

   public static void renderFloatingText(PoseStack posestack, MultiBufferSource multibuffersource, String s, int i, int j, int k, int l) {
      renderFloatingText(posestack, multibuffersource, s, (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, l);
   }

   public static void renderFloatingText(PoseStack posestack, MultiBufferSource multibuffersource, String s, double d0, double d1, double d2, int i) {
      renderFloatingText(posestack, multibuffersource, s, d0, d1, d2, i, 0.02F);
   }

   public static void renderFloatingText(PoseStack posestack, MultiBufferSource multibuffersource, String s, double d0, double d1, double d2, int i, float f) {
      renderFloatingText(posestack, multibuffersource, s, d0, d1, d2, i, f, true, 0.0F, false);
   }

   public static void renderFloatingText(PoseStack posestack, MultiBufferSource multibuffersource, String s, double d0, double d1, double d2, int i, float f, boolean flag, float f1, boolean flag1) {
      Minecraft minecraft = Minecraft.getInstance();
      Camera camera = minecraft.gameRenderer.getMainCamera();
      if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
         Font font = minecraft.font;
         double d3 = camera.getPosition().x;
         double d4 = camera.getPosition().y;
         double d5 = camera.getPosition().z;
         posestack.pushPose();
         posestack.translate((float)(d0 - d3), (float)(d1 - d4) + 0.07F, (float)(d2 - d5));
         posestack.mulPoseMatrix((new Matrix4f()).rotation(camera.rotation()));
         posestack.scale(-f, -f, f);
         float f2 = flag ? (float)(-font.width(s)) / 2.0F : 0.0F;
         f2 -= f1 / f;
         font.drawInBatch(s, f2, 0.0F, i, false, posestack.last().pose(), multibuffersource, flag1 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880);
         posestack.popPose();
      }
   }

   public interface SimpleDebugRenderer {
      void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2);

      default void clear() {
      }
   }
}
