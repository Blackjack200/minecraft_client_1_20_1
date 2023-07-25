package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityRenderDispatcher implements ResourceManagerReloadListener {
   private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
   private static final float MAX_SHADOW_RADIUS = 32.0F;
   private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
   private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
   private Map<String, EntityRenderer<? extends Player>> playerRenderers = ImmutableMap.of();
   public final TextureManager textureManager;
   private Level level;
   public Camera camera;
   private Quaternionf cameraOrientation;
   public Entity crosshairPickEntity;
   private final ItemRenderer itemRenderer;
   private final BlockRenderDispatcher blockRenderDispatcher;
   private final ItemInHandRenderer itemInHandRenderer;
   private final Font font;
   public final Options options;
   private final EntityModelSet entityModels;
   private boolean shouldRenderShadow = true;
   private boolean renderHitBoxes;

   public <E extends Entity> int getPackedLightCoords(E entity, float f) {
      return this.getRenderer(entity).getPackedLightCoords(entity, f);
   }

   public EntityRenderDispatcher(Minecraft minecraft, TextureManager texturemanager, ItemRenderer itemrenderer, BlockRenderDispatcher blockrenderdispatcher, Font font, Options options, EntityModelSet entitymodelset) {
      this.textureManager = texturemanager;
      this.itemRenderer = itemrenderer;
      this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemrenderer);
      this.blockRenderDispatcher = blockrenderdispatcher;
      this.font = font;
      this.options = options;
      this.entityModels = entitymodelset;
   }

   public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
      if (entity instanceof AbstractClientPlayer) {
         String s = ((AbstractClientPlayer)entity).getModelName();
         EntityRenderer<? extends Player> entityrenderer = this.playerRenderers.get(s);
         return entityrenderer != null ? entityrenderer : this.playerRenderers.get("default");
      } else {
         return this.renderers.get(entity.getType());
      }
   }

   public void prepare(Level level, Camera camera, Entity entity) {
      this.level = level;
      this.camera = camera;
      this.cameraOrientation = camera.rotation();
      this.crosshairPickEntity = entity;
   }

   public void overrideCameraOrientation(Quaternionf quaternionf) {
      this.cameraOrientation = quaternionf;
   }

   public void setRenderShadow(boolean flag) {
      this.shouldRenderShadow = flag;
   }

   public void setRenderHitBoxes(boolean flag) {
      this.renderHitBoxes = flag;
   }

   public boolean shouldRenderHitBoxes() {
      return this.renderHitBoxes;
   }

   public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double d0, double d1, double d2) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(entity);
      return entityrenderer.shouldRender(entity, frustum, d0, d1, d2);
   }

   public <E extends Entity> void render(E entity, double d0, double d1, double d2, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(entity);

      try {
         Vec3 vec3 = entityrenderer.getRenderOffset(entity, f1);
         double d3 = d0 + vec3.x();
         double d4 = d1 + vec3.y();
         double d5 = d2 + vec3.z();
         posestack.pushPose();
         posestack.translate(d3, d4, d5);
         entityrenderer.render(entity, f, f1, posestack, multibuffersource, i);
         if (entity.displayFireAnimation()) {
            this.renderFlame(posestack, multibuffersource, entity);
         }

         posestack.translate(-vec3.x(), -vec3.y(), -vec3.z());
         if (this.options.entityShadows().get() && this.shouldRenderShadow && entityrenderer.shadowRadius > 0.0F && !entity.isInvisible()) {
            double d6 = this.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());
            float f2 = (float)((1.0D - d6 / 256.0D) * (double)entityrenderer.shadowStrength);
            if (f2 > 0.0F) {
               renderShadow(posestack, multibuffersource, entity, f2, f1, this.level, Math.min(entityrenderer.shadowRadius, 32.0F));
            }
         }

         if (this.renderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
            renderHitbox(posestack, multibuffersource.getBuffer(RenderType.lines()), entity, f1);
         }

         posestack.popPose();
      } catch (Throwable var24) {
         CrashReport crashreport = CrashReport.forThrowable(var24, "Rendering entity in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
         entity.fillCrashReportCategory(crashreportcategory);
         CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
         crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
         crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.level, d0, d1, d2));
         crashreportcategory1.setDetail("Rotation", f);
         crashreportcategory1.setDetail("Delta", f1);
         throw new ReportedException(crashreport);
      }
   }

   private static void renderHitbox(PoseStack posestack, VertexConsumer vertexconsumer, Entity entity, float f) {
      AABB aabb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
      LevelRenderer.renderLineBox(posestack, vertexconsumer, aabb, 1.0F, 1.0F, 1.0F, 1.0F);
      if (entity instanceof EnderDragon) {
         double d0 = -Mth.lerp((double)f, entity.xOld, entity.getX());
         double d1 = -Mth.lerp((double)f, entity.yOld, entity.getY());
         double d2 = -Mth.lerp((double)f, entity.zOld, entity.getZ());

         for(EnderDragonPart enderdragonpart : ((EnderDragon)entity).getSubEntities()) {
            posestack.pushPose();
            double d3 = d0 + Mth.lerp((double)f, enderdragonpart.xOld, enderdragonpart.getX());
            double d4 = d1 + Mth.lerp((double)f, enderdragonpart.yOld, enderdragonpart.getY());
            double d5 = d2 + Mth.lerp((double)f, enderdragonpart.zOld, enderdragonpart.getZ());
            posestack.translate(d3, d4, d5);
            LevelRenderer.renderLineBox(posestack, vertexconsumer, enderdragonpart.getBoundingBox().move(-enderdragonpart.getX(), -enderdragonpart.getY(), -enderdragonpart.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
            posestack.popPose();
         }
      }

      if (entity instanceof LivingEntity) {
         float f1 = 0.01F;
         LevelRenderer.renderLineBox(posestack, vertexconsumer, aabb.minX, (double)(entity.getEyeHeight() - 0.01F), aabb.minZ, aabb.maxX, (double)(entity.getEyeHeight() + 0.01F), aabb.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      Vec3 vec3 = entity.getViewVector(f);
      Matrix4f matrix4f = posestack.last().pose();
      Matrix3f matrix3f = posestack.last().normal();
      vertexconsumer.vertex(matrix4f, 0.0F, entity.getEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
      vertexconsumer.vertex(matrix4f, (float)(vec3.x * 2.0D), (float)((double)entity.getEyeHeight() + vec3.y * 2.0D), (float)(vec3.z * 2.0D)).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
   }

   private void renderFlame(PoseStack posestack, MultiBufferSource multibuffersource, Entity entity) {
      TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_0.sprite();
      TextureAtlasSprite textureatlassprite1 = ModelBakery.FIRE_1.sprite();
      posestack.pushPose();
      float f = entity.getBbWidth() * 1.4F;
      posestack.scale(f, f, f);
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = entity.getBbHeight() / f;
      float f4 = 0.0F;
      posestack.mulPose(Axis.YP.rotationDegrees(-this.camera.getYRot()));
      posestack.translate(0.0F, 0.0F, -0.3F + (float)((int)f3) * 0.02F);
      float f5 = 0.0F;
      int i = 0;
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(Sheets.cutoutBlockSheet());

      for(PoseStack.Pose posestack_pose = posestack.last(); f3 > 0.0F; ++i) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         float f6 = textureatlassprite2.getU0();
         float f7 = textureatlassprite2.getV0();
         float f8 = textureatlassprite2.getU1();
         float f9 = textureatlassprite2.getV1();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }

         fireVertex(posestack_pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f8, f9);
         fireVertex(posestack_pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f6, f9);
         fireVertex(posestack_pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f6, f7);
         fireVertex(posestack_pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f8, f7);
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
      }

      posestack.popPose();
   }

   private static void fireVertex(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, float f4) {
      vertexconsumer.vertex(posestack_pose.pose(), f, f1, f2).color(255, 255, 255, 255).uv(f3, f4).overlayCoords(0, 10).uv2(240).normal(posestack_pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
   }

   private static void renderShadow(PoseStack posestack, MultiBufferSource multibuffersource, Entity entity, float f, float f1, LevelReader levelreader, float f2) {
      float f3 = f2;
      if (entity instanceof Mob mob) {
         if (mob.isBaby()) {
            f3 = f2 * 0.5F;
         }
      }

      double d0 = Mth.lerp((double)f1, entity.xOld, entity.getX());
      double d1 = Mth.lerp((double)f1, entity.yOld, entity.getY());
      double d2 = Mth.lerp((double)f1, entity.zOld, entity.getZ());
      float f4 = Math.min(f / 0.5F, f3);
      int i = Mth.floor(d0 - (double)f3);
      int j = Mth.floor(d0 + (double)f3);
      int k = Mth.floor(d1 - (double)f4);
      int l = Mth.floor(d1);
      int i1 = Mth.floor(d2 - (double)f3);
      int j1 = Mth.floor(d2 + (double)f3);
      PoseStack.Pose posestack_pose = posestack.last();
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(SHADOW_RENDER_TYPE);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i1; k1 <= j1; ++k1) {
         for(int l1 = i; l1 <= j; ++l1) {
            blockpos_mutableblockpos.set(l1, 0, k1);
            ChunkAccess chunkaccess = levelreader.getChunk(blockpos_mutableblockpos);

            for(int i2 = k; i2 <= l; ++i2) {
               blockpos_mutableblockpos.setY(i2);
               float f5 = f - (float)(d1 - (double)blockpos_mutableblockpos.getY()) * 0.5F;
               renderBlockShadow(posestack_pose, vertexconsumer, chunkaccess, levelreader, blockpos_mutableblockpos, d0, d1, d2, f3, f5);
            }
         }
      }

   }

   private static void renderBlockShadow(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, ChunkAccess chunkaccess, LevelReader levelreader, BlockPos blockpos, double d0, double d1, double d2, float f, float f1) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate = chunkaccess.getBlockState(blockpos1);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE && levelreader.getMaxLocalRawBrightness(blockpos) > 3) {
         if (blockstate.isCollisionShapeFullBlock(chunkaccess, blockpos1)) {
            VoxelShape voxelshape = blockstate.getShape(chunkaccess, blockpos1);
            if (!voxelshape.isEmpty()) {
               float f2 = LightTexture.getBrightness(levelreader.dimensionType(), levelreader.getMaxLocalRawBrightness(blockpos));
               float f3 = f1 * 0.5F * f2;
               if (f3 >= 0.0F) {
                  if (f3 > 1.0F) {
                     f3 = 1.0F;
                  }

                  AABB aabb = voxelshape.bounds();
                  double d3 = (double)blockpos.getX() + aabb.minX;
                  double d4 = (double)blockpos.getX() + aabb.maxX;
                  double d5 = (double)blockpos.getY() + aabb.minY;
                  double d6 = (double)blockpos.getZ() + aabb.minZ;
                  double d7 = (double)blockpos.getZ() + aabb.maxZ;
                  float f4 = (float)(d3 - d0);
                  float f5 = (float)(d4 - d0);
                  float f6 = (float)(d5 - d1);
                  float f7 = (float)(d6 - d2);
                  float f8 = (float)(d7 - d2);
                  float f9 = -f4 / 2.0F / f + 0.5F;
                  float f10 = -f5 / 2.0F / f + 0.5F;
                  float f11 = -f7 / 2.0F / f + 0.5F;
                  float f12 = -f8 / 2.0F / f + 0.5F;
                  shadowVertex(posestack_pose, vertexconsumer, f3, f4, f6, f7, f9, f11);
                  shadowVertex(posestack_pose, vertexconsumer, f3, f4, f6, f8, f9, f12);
                  shadowVertex(posestack_pose, vertexconsumer, f3, f5, f6, f8, f10, f12);
                  shadowVertex(posestack_pose, vertexconsumer, f3, f5, f6, f7, f10, f11);
               }

            }
         }
      }
   }

   private static void shadowVertex(PoseStack.Pose posestack_pose, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, float f4, float f5) {
      Vector3f vector3f = posestack_pose.pose().transformPosition(f1, f2, f3, new Vector3f());
      vertexconsumer.vertex(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F, 1.0F, 1.0F, f, f4, f5, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
   }

   public void setLevel(@Nullable Level level) {
      this.level = level;
      if (level == null) {
         this.camera = null;
      }

   }

   public double distanceToSqr(Entity entity) {
      return this.camera.getPosition().distanceToSqr(entity.position());
   }

   public double distanceToSqr(double d0, double d1, double d2) {
      return this.camera.getPosition().distanceToSqr(d0, d1, d2);
   }

   public Quaternionf cameraOrientation() {
      return this.cameraOrientation;
   }

   public ItemInHandRenderer getItemInHandRenderer() {
      return this.itemInHandRenderer;
   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      EntityRendererProvider.Context entityrendererprovider_context = new EntityRendererProvider.Context(this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, resourcemanager, this.entityModels, this.font);
      this.renderers = EntityRenderers.createEntityRenderers(entityrendererprovider_context);
      this.playerRenderers = EntityRenderers.createPlayerRenderers(entityrendererprovider_context);
   }
}
