package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.WanderingTrader;

public class WanderingTraderRenderer extends MobRenderer<WanderingTrader, VillagerModel<WanderingTrader>> {
   private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/wandering_trader.png");

   public WanderingTraderRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new VillagerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new CrossedArmsItemLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(WanderingTrader wanderingtrader) {
      return VILLAGER_BASE_SKIN;
   }

   protected void scale(WanderingTrader wanderingtrader, PoseStack posestack, float f) {
      float f1 = 0.9375F;
      posestack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}
