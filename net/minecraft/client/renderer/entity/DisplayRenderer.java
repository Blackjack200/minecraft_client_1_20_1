package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public abstract class DisplayRenderer<T extends Display, S> extends EntityRenderer<T> {
   private final EntityRenderDispatcher entityRenderDispatcher;

   protected DisplayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.entityRenderDispatcher = entityrendererprovider_context.getEntityRenderDispatcher();
   }

   public ResourceLocation getTextureLocation(T display) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   public void render(T display, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      Display.RenderState display_renderstate = display.renderState();
      if (display_renderstate != null) {
         S object = this.getSubState(display);
         if (object != null) {
            float f2 = display.calculateInterpolationProgress(f1);
            this.shadowRadius = display_renderstate.shadowRadius().get(f2);
            this.shadowStrength = display_renderstate.shadowStrength().get(f2);
            int j = display_renderstate.brightnessOverride();
            int k = j != -1 ? j : i;
            super.render(display, f, f1, posestack, multibuffersource, k);
            posestack.pushPose();
            posestack.mulPose(this.calculateOrientation(display_renderstate, display));
            Transformation transformation = display_renderstate.transformation().get(f2);
            posestack.mulPoseMatrix(transformation.getMatrix());
            posestack.last().normal().rotate(transformation.getLeftRotation()).rotate(transformation.getRightRotation());
            this.renderInner(display, object, posestack, multibuffersource, k, f2);
            posestack.popPose();
         }
      }
   }

   private Quaternionf calculateOrientation(Display.RenderState display_renderstate, T display) {
      Camera camera = this.entityRenderDispatcher.camera;
      Quaternionf var10000;
      switch (display_renderstate.billboardConstraints()) {
         case FIXED:
            var10000 = display.orientation();
            break;
         case HORIZONTAL:
            var10000 = (new Quaternionf()).rotationYXZ(-0.017453292F * display.getYRot(), -0.017453292F * camera.getXRot(), 0.0F);
            break;
         case VERTICAL:
            var10000 = (new Quaternionf()).rotationYXZ((float)Math.PI - ((float)Math.PI / 180F) * camera.getYRot(), ((float)Math.PI / 180F) * display.getXRot(), 0.0F);
            break;
         case CENTER:
            var10000 = (new Quaternionf()).rotationYXZ((float)Math.PI - ((float)Math.PI / 180F) * camera.getYRot(), -0.017453292F * camera.getXRot(), 0.0F);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   @Nullable
   protected abstract S getSubState(T display);

   protected abstract void renderInner(T display, S object, PoseStack posestack, MultiBufferSource multibuffersource, int i, float f);

   public static class BlockDisplayRenderer extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState> {
      private final BlockRenderDispatcher blockRenderer;

      protected BlockDisplayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
         super(entityrendererprovider_context);
         this.blockRenderer = entityrendererprovider_context.getBlockRenderDispatcher();
      }

      @Nullable
      protected Display.BlockDisplay.BlockRenderState getSubState(Display.BlockDisplay display_blockdisplay) {
         return display_blockdisplay.blockRenderState();
      }

      public void renderInner(Display.BlockDisplay display_blockdisplay, Display.BlockDisplay.BlockRenderState display_blockdisplay_blockrenderstate, PoseStack posestack, MultiBufferSource multibuffersource, int i, float f) {
         this.blockRenderer.renderSingleBlock(display_blockdisplay_blockrenderstate.blockState(), posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY);
      }
   }

   public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState> {
      private final ItemRenderer itemRenderer;

      protected ItemDisplayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
         super(entityrendererprovider_context);
         this.itemRenderer = entityrendererprovider_context.getItemRenderer();
      }

      @Nullable
      protected Display.ItemDisplay.ItemRenderState getSubState(Display.ItemDisplay display_itemdisplay) {
         return display_itemdisplay.itemRenderState();
      }

      public void renderInner(Display.ItemDisplay display_itemdisplay, Display.ItemDisplay.ItemRenderState display_itemdisplay_itemrenderstate, PoseStack posestack, MultiBufferSource multibuffersource, int i, float f) {
         posestack.mulPose(Axis.YP.rotation((float)Math.PI));
         this.itemRenderer.renderStatic(display_itemdisplay_itemrenderstate.itemStack(), display_itemdisplay_itemrenderstate.itemTransform(), i, OverlayTexture.NO_OVERLAY, posestack, multibuffersource, display_itemdisplay.level(), display_itemdisplay.getId());
      }
   }

   public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState> {
      private final Font font;

      protected TextDisplayRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
         super(entityrendererprovider_context);
         this.font = entityrendererprovider_context.getFont();
      }

      private Display.TextDisplay.CachedInfo splitLines(Component component, int i) {
         List<FormattedCharSequence> list = this.font.split(component, i);
         List<Display.TextDisplay.CachedLine> list1 = new ArrayList<>(list.size());
         int j = 0;

         for(FormattedCharSequence formattedcharsequence : list) {
            int k = this.font.width(formattedcharsequence);
            j = Math.max(j, k);
            list1.add(new Display.TextDisplay.CachedLine(formattedcharsequence, k));
         }

         return new Display.TextDisplay.CachedInfo(list1, j);
      }

      @Nullable
      protected Display.TextDisplay.TextRenderState getSubState(Display.TextDisplay display_textdisplay) {
         return display_textdisplay.textRenderState();
      }

      public void renderInner(Display.TextDisplay display_textdisplay, Display.TextDisplay.TextRenderState display_textdisplay_textrenderstate, PoseStack posestack, MultiBufferSource multibuffersource, int i, float f) {
         byte b0 = display_textdisplay_textrenderstate.flags();
         boolean flag = (b0 & 2) != 0;
         boolean flag1 = (b0 & 4) != 0;
         boolean flag2 = (b0 & 1) != 0;
         Display.TextDisplay.Align display_textdisplay_align = Display.TextDisplay.getAlign(b0);
         byte b1 = (byte)display_textdisplay_textrenderstate.textOpacity().get(f);
         int j;
         if (flag1) {
            float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            j = (int)(f1 * 255.0F) << 24;
         } else {
            j = display_textdisplay_textrenderstate.backgroundColor().get(f);
         }

         float f2 = 0.0F;
         Matrix4f matrix4f = posestack.last().pose();
         matrix4f.rotate((float)Math.PI, 0.0F, 1.0F, 0.0F);
         matrix4f.scale(-0.025F, -0.025F, -0.025F);
         Display.TextDisplay.CachedInfo display_textdisplay_cachedinfo = display_textdisplay.cacheDisplay(this::splitLines);
         int l = 9 + 1;
         int i1 = display_textdisplay_cachedinfo.width();
         int j1 = display_textdisplay_cachedinfo.lines().size() * l;
         matrix4f.translate(1.0F - (float)i1 / 2.0F, (float)(-j1), 0.0F);
         if (j != 0) {
            VertexConsumer vertexconsumer = multibuffersource.getBuffer(flag ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
            vertexconsumer.vertex(matrix4f, -1.0F, -1.0F, 0.0F).color(j).uv2(i).endVertex();
            vertexconsumer.vertex(matrix4f, -1.0F, (float)j1, 0.0F).color(j).uv2(i).endVertex();
            vertexconsumer.vertex(matrix4f, (float)i1, (float)j1, 0.0F).color(j).uv2(i).endVertex();
            vertexconsumer.vertex(matrix4f, (float)i1, -1.0F, 0.0F).color(j).uv2(i).endVertex();
         }

         for(Display.TextDisplay.CachedLine display_textdisplay_cachedline : display_textdisplay_cachedinfo.lines()) {
            float var10000;
            switch (display_textdisplay_align) {
               case LEFT:
                  var10000 = 0.0F;
                  break;
               case RIGHT:
                  var10000 = (float)(i1 - display_textdisplay_cachedline.width());
                  break;
               case CENTER:
                  var10000 = (float)i1 / 2.0F - (float)display_textdisplay_cachedline.width() / 2.0F;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            float f3 = var10000;
            this.font.drawInBatch(display_textdisplay_cachedline.contents(), f3, f2, b1 << 24 | 16777215, flag2, matrix4f, multibuffersource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, 0, i);
            f2 += (float)l;
         }

      }
   }
}
