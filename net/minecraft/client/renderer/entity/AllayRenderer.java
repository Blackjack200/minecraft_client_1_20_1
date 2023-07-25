package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;

public class AllayRenderer extends MobRenderer<Allay, AllayModel> {
   private static final ResourceLocation ALLAY_TEXTURE = new ResourceLocation("textures/entity/allay/allay.png");

   public AllayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new AllayModel(entityrendererprovider_context.bakeLayer(ModelLayers.ALLAY)), 0.4F);
      this.addLayer(new ItemInHandLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Allay allay) {
      return ALLAY_TEXTURE;
   }

   protected int getBlockLightLevel(Allay allay, BlockPos blockpos) {
      return 15;
   }
}
