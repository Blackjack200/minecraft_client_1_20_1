package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherBossRenderer extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {
   private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

   public WitherBossRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new WitherBossModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.WITHER)), 1.0F);
      this.addLayer(new WitherArmorLayer(this, entityrendererprovider_context.getModelSet()));
   }

   protected int getBlockLightLevel(WitherBoss witherboss, BlockPos blockpos) {
      return 15;
   }

   public ResourceLocation getTextureLocation(WitherBoss witherboss) {
      int i = witherboss.getInvulnerableTicks();
      return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
   }

   protected void scale(WitherBoss witherboss, PoseStack posestack, float f) {
      float f1 = 2.0F;
      int i = witherboss.getInvulnerableTicks();
      if (i > 0) {
         f1 -= ((float)i - f) / 220.0F * 0.5F;
      }

      posestack.scale(f1, f1, f1);
   }
}
