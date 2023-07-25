package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WardenEmissiveLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenRenderer extends MobRenderer<Warden, WardenModel<Warden>> {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/warden/warden.png");
   private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = new ResourceLocation("textures/entity/warden/warden_bioluminescent_layer.png");
   private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("textures/entity/warden/warden_heart.png");
   private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_1.png");
   private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = new ResourceLocation("textures/entity/warden/warden_pulsating_spots_2.png");

   public WardenRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new WardenModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.WARDEN)), 0.9F);
      this.addLayer(new WardenEmissiveLayer<>(this, BIOLUMINESCENT_LAYER_TEXTURE, (warden4, f8, f9) -> 1.0F, WardenModel::getBioluminescentLayerModelParts));
      this.addLayer(new WardenEmissiveLayer<>(this, PULSATING_SPOTS_TEXTURE_1, (warden3, f6, f7) -> Math.max(0.0F, Mth.cos(f7 * 0.045F) * 0.25F), WardenModel::getPulsatingSpotsLayerModelParts));
      this.addLayer(new WardenEmissiveLayer<>(this, PULSATING_SPOTS_TEXTURE_2, (warden2, f4, f5) -> Math.max(0.0F, Mth.cos(f5 * 0.045F + (float)Math.PI) * 0.25F), WardenModel::getPulsatingSpotsLayerModelParts));
      this.addLayer(new WardenEmissiveLayer<>(this, TEXTURE, (warden1, f2, f3) -> warden1.getTendrilAnimation(f2), WardenModel::getTendrilsLayerModelParts));
      this.addLayer(new WardenEmissiveLayer<>(this, HEART_TEXTURE, (warden, f, f1) -> warden.getHeartAnimation(f), WardenModel::getHeartLayerModelParts));
   }

   public ResourceLocation getTextureLocation(Warden warden) {
      return TEXTURE;
   }
}
