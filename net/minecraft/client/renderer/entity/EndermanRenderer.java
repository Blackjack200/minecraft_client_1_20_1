package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EndermanRenderer extends MobRenderer<EnderMan, EndermanModel<EnderMan>> {
   private static final ResourceLocation ENDERMAN_LOCATION = new ResourceLocation("textures/entity/enderman/enderman.png");
   private final RandomSource random = RandomSource.create();

   public EndermanRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new EndermanModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
      this.addLayer(new EnderEyesLayer<>(this));
      this.addLayer(new CarriedBlockLayer(this, entityrendererprovider_context.getBlockRenderDispatcher()));
   }

   public void render(EnderMan enderman, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      BlockState blockstate = enderman.getCarriedBlock();
      EndermanModel<EnderMan> endermanmodel = this.getModel();
      endermanmodel.carrying = blockstate != null;
      endermanmodel.creepy = enderman.isCreepy();
      super.render(enderman, f, f1, posestack, multibuffersource, i);
   }

   public Vec3 getRenderOffset(EnderMan enderman, float f) {
      if (enderman.isCreepy()) {
         double d0 = 0.02D;
         return new Vec3(this.random.nextGaussian() * 0.02D, 0.0D, this.random.nextGaussian() * 0.02D);
      } else {
         return super.getRenderOffset(enderman, f);
      }
   }

   public ResourceLocation getTextureLocation(EnderMan enderman) {
      return ENDERMAN_LOCATION;
   }
}
