package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEmissiveLayer<T extends Warden, M extends WardenModel<T>> extends RenderLayer<T, M> {
   private final ResourceLocation texture;
   private final WardenEmissiveLayer.AlphaFunction<T> alphaFunction;
   private final WardenEmissiveLayer.DrawSelector<T, M> drawSelector;

   public WardenEmissiveLayer(RenderLayerParent<T, M> renderlayerparent, ResourceLocation resourcelocation, WardenEmissiveLayer.AlphaFunction<T> wardenemissivelayer_alphafunction, WardenEmissiveLayer.DrawSelector<T, M> wardenemissivelayer_drawselector) {
      super(renderlayerparent);
      this.texture = resourcelocation;
      this.alphaFunction = wardenemissivelayer_alphafunction;
      this.drawSelector = wardenemissivelayer_drawselector;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T warden, float f, float f1, float f2, float f3, float f4, float f5) {
      if (!warden.isInvisible()) {
         this.onlyDrawSelectedParts();
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityTranslucentEmissive(this.texture));
         this.getParentModel().renderToBuffer(posestack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(warden, 0.0F), 1.0F, 1.0F, 1.0F, this.alphaFunction.apply(warden, f2, f3));
         this.resetDrawForAllParts();
      }
   }

   private void onlyDrawSelectedParts() {
      List<ModelPart> list = this.drawSelector.getPartsToDraw(this.getParentModel());
      this.getParentModel().root().getAllParts().forEach((modelpart1) -> modelpart1.skipDraw = true);
      list.forEach((modelpart) -> modelpart.skipDraw = false);
   }

   private void resetDrawForAllParts() {
      this.getParentModel().root().getAllParts().forEach((modelpart) -> modelpart.skipDraw = false);
   }

   public interface AlphaFunction<T extends Warden> {
      float apply(T warden, float f, float f1);
   }

   public interface DrawSelector<T extends Warden, M extends EntityModel<T>> {
      List<ModelPart> getPartsToDraw(M entitymodel);
   }
}
