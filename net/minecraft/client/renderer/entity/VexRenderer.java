package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

public class VexRenderer extends MobRenderer<Vex, VexModel> {
   private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
   private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

   public VexRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new VexModel(entityrendererprovider_context.bakeLayer(ModelLayers.VEX)), 0.3F);
      this.addLayer(new ItemInHandLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   protected int getBlockLightLevel(Vex vex, BlockPos blockpos) {
      return 15;
   }

   public ResourceLocation getTextureLocation(Vex vex) {
      return vex.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
   }
}
