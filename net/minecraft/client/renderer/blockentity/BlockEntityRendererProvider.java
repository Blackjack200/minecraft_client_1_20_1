package net.minecraft.client.renderer.blockentity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface BlockEntityRendererProvider<T extends BlockEntity> {
   BlockEntityRenderer<T> create(BlockEntityRendererProvider.Context blockentityrendererprovider_context);

   public static class Context {
      private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
      private final BlockRenderDispatcher blockRenderDispatcher;
      private final ItemRenderer itemRenderer;
      private final EntityRenderDispatcher entityRenderer;
      private final EntityModelSet modelSet;
      private final Font font;

      public Context(BlockEntityRenderDispatcher blockentityrenderdispatcher, BlockRenderDispatcher blockrenderdispatcher, ItemRenderer itemrenderer, EntityRenderDispatcher entityrenderdispatcher, EntityModelSet entitymodelset, Font font) {
         this.blockEntityRenderDispatcher = blockentityrenderdispatcher;
         this.blockRenderDispatcher = blockrenderdispatcher;
         this.itemRenderer = itemrenderer;
         this.entityRenderer = entityrenderdispatcher;
         this.modelSet = entitymodelset;
         this.font = font;
      }

      public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
         return this.blockEntityRenderDispatcher;
      }

      public BlockRenderDispatcher getBlockRenderDispatcher() {
         return this.blockRenderDispatcher;
      }

      public EntityRenderDispatcher getEntityRenderer() {
         return this.entityRenderer;
      }

      public ItemRenderer getItemRenderer() {
         return this.itemRenderer;
      }

      public EntityModelSet getModelSet() {
         return this.modelSet;
      }

      public ModelPart bakeLayer(ModelLayerLocation modellayerlocation) {
         return this.modelSet.bakeLayer(modellayerlocation);
      }

      public Font getFont() {
         return this.font;
      }
   }
}
