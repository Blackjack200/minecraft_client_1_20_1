package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel> {
   public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

   public ArmorStandRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new ArmorStandModel(entityrendererprovider_context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
      this.addLayer(new HumanoidArmorLayer<>(this, new ArmorStandArmorModel(entityrendererprovider_context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)), new ArmorStandArmorModel(entityrendererprovider_context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)), entityrendererprovider_context.getModelManager()));
      this.addLayer(new ItemInHandLayer<>(this, entityrendererprovider_context.getItemInHandRenderer()));
      this.addLayer(new ElytraLayer<>(this, entityrendererprovider_context.getModelSet()));
      this.addLayer(new CustomHeadLayer<>(this, entityrendererprovider_context.getModelSet(), entityrendererprovider_context.getItemInHandRenderer()));
   }

   public ResourceLocation getTextureLocation(ArmorStand armorstand) {
      return DEFAULT_SKIN_LOCATION;
   }

   protected void setupRotations(ArmorStand armorstand, PoseStack posestack, float f, float f1, float f2) {
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f1));
      float f3 = (float)(armorstand.level().getGameTime() - armorstand.lastHit) + f2;
      if (f3 < 5.0F) {
         posestack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f3 / 1.5F * (float)Math.PI) * 3.0F));
      }

   }

   protected boolean shouldShowName(ArmorStand armorstand) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(armorstand);
      float f = armorstand.isCrouching() ? 32.0F : 64.0F;
      return d0 >= (double)(f * f) ? false : armorstand.isCustomNameVisible();
   }

   @Nullable
   protected RenderType getRenderType(ArmorStand armorstand, boolean flag, boolean flag1, boolean flag2) {
      if (!armorstand.isMarker()) {
         return super.getRenderType(armorstand, flag, flag1, flag2);
      } else {
         ResourceLocation resourcelocation = this.getTextureLocation(armorstand);
         if (flag1) {
            return RenderType.entityTranslucent(resourcelocation, false);
         } else {
            return flag ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
         }
      }
   }
}
