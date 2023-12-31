package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherArmorLayer extends EnergySwirlLayer<WitherBoss, WitherBossModel<WitherBoss>> {
   private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
   private final WitherBossModel<WitherBoss> model;

   public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new WitherBossModel<>(entitymodelset.bakeLayer(ModelLayers.WITHER_ARMOR));
   }

   protected float xOffset(float f) {
      return Mth.cos(f * 0.02F) * 3.0F;
   }

   protected ResourceLocation getTextureLocation() {
      return WITHER_ARMOR_LOCATION;
   }

   protected EntityModel<WitherBoss> model() {
      return this.model;
   }
}
