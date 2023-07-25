package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinRenderer extends HumanoidMobRenderer<Mob, PiglinModel<Mob>> {
   private static final Map<EntityType<?>, ResourceLocation> TEXTURES = ImmutableMap.of(EntityType.PIGLIN, new ResourceLocation("textures/entity/piglin/piglin.png"), EntityType.ZOMBIFIED_PIGLIN, new ResourceLocation("textures/entity/piglin/zombified_piglin.png"), EntityType.PIGLIN_BRUTE, new ResourceLocation("textures/entity/piglin/piglin_brute.png"));
   private static final float PIGLIN_CUSTOM_HEAD_SCALE = 1.0019531F;

   public PiglinRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation, ModelLayerLocation modellayerlocation1, ModelLayerLocation modellayerlocation2, boolean flag) {
      super(entityrendererprovider_context, createModel(entityrendererprovider_context.getModelSet(), modellayerlocation, flag), 0.5F, 1.0019531F, 1.0F, 1.0019531F);
      this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel(entityrendererprovider_context.bakeLayer(modellayerlocation1)), new HumanoidArmorModel(entityrendererprovider_context.bakeLayer(modellayerlocation2)), entityrendererprovider_context.getModelManager()));
   }

   private static PiglinModel<Mob> createModel(EntityModelSet entitymodelset, ModelLayerLocation modellayerlocation, boolean flag) {
      PiglinModel<Mob> piglinmodel = new PiglinModel<>(entitymodelset.bakeLayer(modellayerlocation));
      if (flag) {
         piglinmodel.rightEar.visible = false;
      }

      return piglinmodel;
   }

   public ResourceLocation getTextureLocation(Mob mob) {
      ResourceLocation resourcelocation = TEXTURES.get(mob.getType());
      if (resourcelocation == null) {
         throw new IllegalArgumentException("I don't know what texture to use for " + mob.getType());
      } else {
         return resourcelocation;
      }
   }

   protected boolean isShaking(Mob mob) {
      return super.isShaking(mob) || mob instanceof AbstractPiglin && ((AbstractPiglin)mob).isConverting();
   }
}
