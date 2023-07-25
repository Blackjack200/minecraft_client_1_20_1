package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
   private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

   public TheEndGatewayRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      super(blockentityrendererprovider_context);
   }

   public void render(TheEndGatewayBlockEntity theendgatewayblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      if (theendgatewayblockentity.isSpawning() || theendgatewayblockentity.isCoolingDown()) {
         float f1 = theendgatewayblockentity.isSpawning() ? theendgatewayblockentity.getSpawnPercent(f) : theendgatewayblockentity.getCooldownPercent(f);
         double d0 = theendgatewayblockentity.isSpawning() ? (double)theendgatewayblockentity.getLevel().getMaxBuildHeight() : 50.0D;
         f1 = Mth.sin(f1 * (float)Math.PI);
         int k = Mth.floor((double)f1 * d0);
         float[] afloat = theendgatewayblockentity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
         long l = theendgatewayblockentity.getLevel().getGameTime();
         BeaconRenderer.renderBeaconBeam(posestack, multibuffersource, BEAM_LOCATION, f, f1, l, -k, k * 2, afloat, 0.15F, 0.175F);
      }

      super.render(theendgatewayblockentity, f, posestack, multibuffersource, i, j);
   }

   protected float getOffsetUp() {
      return 1.0F;
   }

   protected float getOffsetDown() {
      return 0.0F;
   }

   protected RenderType renderType() {
      return RenderType.endGateway();
   }

   public int getViewDistance() {
      return 256;
   }
}
