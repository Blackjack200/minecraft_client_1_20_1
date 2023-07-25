package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
   private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

   public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Wolf wolf, float f, float f1, float f2, float f3, float f4, float f5) {
      if (wolf.isTame() && !wolf.isInvisible()) {
         float[] afloat = wolf.getCollarColor().getTextureDiffuseColors();
         renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, posestack, multibuffersource, i, wolf, afloat[0], afloat[1], afloat[2]);
      }
   }
}
