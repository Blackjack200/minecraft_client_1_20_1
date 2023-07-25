package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BlockRenderDispatcher implements ResourceManagerReloadListener {
   private final BlockModelShaper blockModelShaper;
   private final ModelBlockRenderer modelRenderer;
   private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
   private final LiquidBlockRenderer liquidBlockRenderer;
   private final RandomSource random = RandomSource.create();
   private final BlockColors blockColors;

   public BlockRenderDispatcher(BlockModelShaper blockmodelshaper, BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer, BlockColors blockcolors) {
      this.blockModelShaper = blockmodelshaper;
      this.blockEntityRenderer = blockentitywithoutlevelrenderer;
      this.blockColors = blockcolors;
      this.modelRenderer = new ModelBlockRenderer(this.blockColors);
      this.liquidBlockRenderer = new LiquidBlockRenderer();
   }

   public BlockModelShaper getBlockModelShaper() {
      return this.blockModelShaper;
   }

   public void renderBreakingTexture(BlockState blockstate, BlockPos blockpos, BlockAndTintGetter blockandtintgetter, PoseStack posestack, VertexConsumer vertexconsumer) {
      if (blockstate.getRenderShape() == RenderShape.MODEL) {
         BakedModel bakedmodel = this.blockModelShaper.getBlockModel(blockstate);
         long i = blockstate.getSeed(blockpos);
         this.modelRenderer.tesselateBlock(blockandtintgetter, bakedmodel, blockstate, blockpos, posestack, vertexconsumer, true, this.random, i, OverlayTexture.NO_OVERLAY);
      }
   }

   public void renderBatched(BlockState blockstate, BlockPos blockpos, BlockAndTintGetter blockandtintgetter, PoseStack posestack, VertexConsumer vertexconsumer, boolean flag, RandomSource randomsource) {
      try {
         RenderShape rendershape = blockstate.getRenderShape();
         if (rendershape == RenderShape.MODEL) {
            this.modelRenderer.tesselateBlock(blockandtintgetter, this.getBlockModel(blockstate), blockstate, blockpos, posestack, vertexconsumer, flag, randomsource, blockstate.getSeed(blockpos), OverlayTexture.NO_OVERLAY);
         }

      } catch (Throwable var11) {
         CrashReport crashreport = CrashReport.forThrowable(var11, "Tesselating block in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, blockandtintgetter, blockpos, blockstate);
         throw new ReportedException(crashreport);
      }
   }

   public void renderLiquid(BlockPos blockpos, BlockAndTintGetter blockandtintgetter, VertexConsumer vertexconsumer, BlockState blockstate, FluidState fluidstate) {
      try {
         this.liquidBlockRenderer.tesselate(blockandtintgetter, blockpos, vertexconsumer, blockstate, fluidstate);
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.forThrowable(var9, "Tesselating liquid in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
         CrashReportCategory.populateBlockDetails(crashreportcategory, blockandtintgetter, blockpos, (BlockState)null);
         throw new ReportedException(crashreport);
      }
   }

   public ModelBlockRenderer getModelRenderer() {
      return this.modelRenderer;
   }

   public BakedModel getBlockModel(BlockState blockstate) {
      return this.blockModelShaper.getBlockModel(blockstate);
   }

   public void renderSingleBlock(BlockState blockstate, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      RenderShape rendershape = blockstate.getRenderShape();
      if (rendershape != RenderShape.INVISIBLE) {
         switch (rendershape) {
            case MODEL:
               BakedModel bakedmodel = this.getBlockModel(blockstate);
               int k = this.blockColors.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, 0);
               float f = (float)(k >> 16 & 255) / 255.0F;
               float f1 = (float)(k >> 8 & 255) / 255.0F;
               float f2 = (float)(k & 255) / 255.0F;
               this.modelRenderer.renderModel(posestack.last(), multibuffersource.getBuffer(ItemBlockRenderTypes.getRenderType(blockstate, false)), blockstate, bakedmodel, f, f1, f2, i, j);
               break;
            case ENTITYBLOCK_ANIMATED:
               this.blockEntityRenderer.renderByItem(new ItemStack(blockstate.getBlock()), ItemDisplayContext.NONE, posestack, multibuffersource, i, j);
         }

      }
   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      this.liquidBlockRenderer.setupSprites();
   }
}
