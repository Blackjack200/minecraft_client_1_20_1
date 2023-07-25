package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;

public class EvokerRenderer<T extends SpellcasterIllager> extends IllagerRenderer<T> {
   private static final ResourceLocation EVOKER_ILLAGER = new ResourceLocation("textures/entity/illager/evoker.png");

   public EvokerRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new IllagerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.EVOKER)), 0.5F);
      this.addLayer(new ItemInHandLayer<T, IllagerModel<T>>(this, entityrendererprovider_context.getItemInHandRenderer()) {
         public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T spellcasterillager, float f, float f1, float f2, float f3, float f4, float f5) {
            if (spellcasterillager.isCastingSpell()) {
               super.render(posestack, multibuffersource, i, spellcasterillager, f, f1, f2, f3, f4, f5);
            }

         }
      });
   }

   public ResourceLocation getTextureLocation(T spellcasterillager) {
      return EVOKER_ILLAGER;
   }
}
