package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;

public class BlockEntityRenderDispatcher implements ResourceManagerReloadListener {
   private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
   private final Font font;
   private final EntityModelSet entityModelSet;
   public Level level;
   public Camera camera;
   public HitResult cameraHitResult;
   private final Supplier<BlockRenderDispatcher> blockRenderDispatcher;
   private final Supplier<ItemRenderer> itemRenderer;
   private final Supplier<EntityRenderDispatcher> entityRenderer;

   public BlockEntityRenderDispatcher(Font font, EntityModelSet entitymodelset, Supplier<BlockRenderDispatcher> supplier, Supplier<ItemRenderer> supplier1, Supplier<EntityRenderDispatcher> supplier2) {
      this.itemRenderer = supplier1;
      this.entityRenderer = supplier2;
      this.font = font;
      this.entityModelSet = entitymodelset;
      this.blockRenderDispatcher = supplier;
   }

   @Nullable
   public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockentity) {
      return this.renderers.get(blockentity.getType());
   }

   public void prepare(Level level, Camera camera, HitResult hitresult) {
      if (this.level != level) {
         this.setLevel(level);
      }

      this.camera = camera;
      this.cameraHitResult = hitresult;
   }

   public <E extends BlockEntity> void render(E blockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource) {
      BlockEntityRenderer<E> blockentityrenderer = this.getRenderer(blockentity);
      if (blockentityrenderer != null) {
         if (blockentity.hasLevel() && blockentity.getType().isValid(blockentity.getBlockState())) {
            if (blockentityrenderer.shouldRender(blockentity, this.camera.getPosition())) {
               tryRender(blockentity, () -> setupAndRender(blockentityrenderer, blockentity, f, posestack, multibuffersource));
            }
         }
      }
   }

   private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> blockentityrenderer, T blockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource) {
      Level level = blockentity.getLevel();
      int i;
      if (level != null) {
         i = LevelRenderer.getLightColor(level, blockentity.getBlockPos());
      } else {
         i = 15728880;
      }

      blockentityrenderer.render(blockentity, f, posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY);
   }

   public <E extends BlockEntity> boolean renderItem(E blockentity, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      BlockEntityRenderer<E> blockentityrenderer = this.getRenderer(blockentity);
      if (blockentityrenderer == null) {
         return true;
      } else {
         tryRender(blockentity, () -> blockentityrenderer.render(blockentity, 0.0F, posestack, multibuffersource, i, j));
         return false;
      }
   }

   private static void tryRender(BlockEntity blockentity, Runnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var5) {
         CrashReport crashreport = CrashReport.forThrowable(var5, "Rendering Block Entity");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
         blockentity.fillCrashReportCategory(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public void setLevel(@Nullable Level level) {
      this.level = level;
      if (level == null) {
         this.camera = null;
      }

   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      BlockEntityRendererProvider.Context blockentityrendererprovider_context = new BlockEntityRendererProvider.Context(this, this.blockRenderDispatcher.get(), this.itemRenderer.get(), this.entityRenderer.get(), this.entityModelSet, this.font);
      this.renderers = BlockEntityRenderers.createEntityRenderers(blockentityrendererprovider_context);
   }
}
