package net.minecraft.client.renderer.entity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface EntityRendererProvider<T extends Entity> {
   EntityRenderer<T> create(EntityRendererProvider.Context entityrendererprovider_context);

   public static class Context {
      private final EntityRenderDispatcher entityRenderDispatcher;
      private final ItemRenderer itemRenderer;
      private final BlockRenderDispatcher blockRenderDispatcher;
      private final ItemInHandRenderer itemInHandRenderer;
      private final ResourceManager resourceManager;
      private final EntityModelSet modelSet;
      private final Font font;

      public Context(EntityRenderDispatcher entityrenderdispatcher, ItemRenderer itemrenderer, BlockRenderDispatcher blockrenderdispatcher, ItemInHandRenderer iteminhandrenderer, ResourceManager resourcemanager, EntityModelSet entitymodelset, Font font) {
         this.entityRenderDispatcher = entityrenderdispatcher;
         this.itemRenderer = itemrenderer;
         this.blockRenderDispatcher = blockrenderdispatcher;
         this.itemInHandRenderer = iteminhandrenderer;
         this.resourceManager = resourcemanager;
         this.modelSet = entitymodelset;
         this.font = font;
      }

      public EntityRenderDispatcher getEntityRenderDispatcher() {
         return this.entityRenderDispatcher;
      }

      public ItemRenderer getItemRenderer() {
         return this.itemRenderer;
      }

      public BlockRenderDispatcher getBlockRenderDispatcher() {
         return this.blockRenderDispatcher;
      }

      public ItemInHandRenderer getItemInHandRenderer() {
         return this.itemInHandRenderer;
      }

      public ResourceManager getResourceManager() {
         return this.resourceManager;
      }

      public EntityModelSet getModelSet() {
         return this.modelSet;
      }

      public ModelManager getModelManager() {
         return this.blockRenderDispatcher.getBlockModelShaper().getModelManager();
      }

      public ModelPart bakeLayer(ModelLayerLocation modellayerlocation) {
         return this.modelSet.bakeLayer(modellayerlocation);
      }

      public Font getFont() {
         return this.font;
      }
   }
}
