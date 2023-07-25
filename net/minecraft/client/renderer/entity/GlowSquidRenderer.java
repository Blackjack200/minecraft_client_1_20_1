package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SquidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;

public class GlowSquidRenderer extends SquidRenderer<GlowSquid> {
   private static final ResourceLocation GLOW_SQUID_LOCATION = new ResourceLocation("textures/entity/squid/glow_squid.png");

   public GlowSquidRenderer(EntityRendererProvider.Context entityrendererprovider_context, SquidModel<GlowSquid> squidmodel) {
      super(entityrendererprovider_context, squidmodel);
   }

   public ResourceLocation getTextureLocation(GlowSquid glowsquid) {
      return GLOW_SQUID_LOCATION;
   }

   protected int getBlockLightLevel(GlowSquid glowsquid, BlockPos blockpos) {
      int i = (int)Mth.clampedLerp(0.0F, 15.0F, 1.0F - (float)glowsquid.getDarkTicksRemaining() / 10.0F);
      return i == 15 ? 15 : Math.max(i, super.getBlockLightLevel(glowsquid, blockpos));
   }
}
