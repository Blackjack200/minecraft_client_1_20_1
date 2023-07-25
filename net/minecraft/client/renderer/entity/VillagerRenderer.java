package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class VillagerRenderer extends MobRenderer<Villager, VillagerModel<Villager>> {
   private static final ResourceLocation VILLAGER_BASE_SKIN = new ResourceLocation("textures/entity/villager/villager.png");

   public VillagerRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new VillagerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new VillagerProfessionLayer<>(this, entityrendererprovider_context.getResourceManager(), "villager"));
      this.addLayer(new CrossedArmsItemLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(Villager villager) {
      return VILLAGER_BASE_SKIN;
   }

   protected void scale(Villager villager, PoseStack posestack, float f) {
      float f1 = 0.9375F;
      if (villager.isBaby()) {
         f1 *= 0.5F;
         this.shadowRadius = 0.25F;
      } else {
         this.shadowRadius = 0.5F;
      }

      posestack.scale(f1, f1, f1);
   }
}
