package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Divisor;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2ic;

public class GuiGraphics {
   public static final float MAX_GUI_Z = 10000.0F;
   public static final float MIN_GUI_Z = -10000.0F;
   private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
   private final Minecraft minecraft;
   private final PoseStack pose;
   private final MultiBufferSource.BufferSource bufferSource;
   private final GuiGraphics.ScissorStack scissorStack = new GuiGraphics.ScissorStack();
   private boolean managed;

   private GuiGraphics(Minecraft minecraft, PoseStack posestack, MultiBufferSource.BufferSource multibuffersource_buffersource) {
      this.minecraft = minecraft;
      this.pose = posestack;
      this.bufferSource = multibuffersource_buffersource;
   }

   public GuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource multibuffersource_buffersource) {
      this(minecraft, new PoseStack(), multibuffersource_buffersource);
   }

   /** @deprecated */
   @Deprecated
   public void drawManaged(Runnable runnable) {
      this.flush();
      this.managed = true;
      runnable.run();
      this.managed = false;
      this.flush();
   }

   /** @deprecated */
   @Deprecated
   private void flushIfUnmanaged() {
      if (!this.managed) {
         this.flush();
      }

   }

   /** @deprecated */
   @Deprecated
   private void flushIfManaged() {
      if (this.managed) {
         this.flush();
      }

   }

   public int guiWidth() {
      return this.minecraft.getWindow().getGuiScaledWidth();
   }

   public int guiHeight() {
      return this.minecraft.getWindow().getGuiScaledHeight();
   }

   public PoseStack pose() {
      return this.pose;
   }

   public MultiBufferSource.BufferSource bufferSource() {
      return this.bufferSource;
   }

   public void flush() {
      RenderSystem.disableDepthTest();
      this.bufferSource.endBatch();
      RenderSystem.enableDepthTest();
   }

   public void hLine(int i, int j, int k, int l) {
      this.hLine(RenderType.gui(), i, j, k, l);
   }

   public void hLine(RenderType rendertype, int i, int j, int k, int l) {
      if (j < i) {
         int i1 = i;
         i = j;
         j = i1;
      }

      this.fill(rendertype, i, k, j + 1, k + 1, l);
   }

   public void vLine(int i, int j, int k, int l) {
      this.vLine(RenderType.gui(), i, j, k, l);
   }

   public void vLine(RenderType rendertype, int i, int j, int k, int l) {
      if (k < j) {
         int i1 = j;
         j = k;
         k = i1;
      }

      this.fill(rendertype, i, j + 1, i + 1, k, l);
   }

   public void enableScissor(int i, int j, int k, int l) {
      this.applyScissor(this.scissorStack.push(new ScreenRectangle(i, j, k - i, l - j)));
   }

   public void disableScissor() {
      this.applyScissor(this.scissorStack.pop());
   }

   private void applyScissor(@Nullable ScreenRectangle screenrectangle) {
      this.flushIfManaged();
      if (screenrectangle != null) {
         Window window = Minecraft.getInstance().getWindow();
         int i = window.getHeight();
         double d0 = window.getGuiScale();
         double d1 = (double)screenrectangle.left() * d0;
         double d2 = (double)i - (double)screenrectangle.bottom() * d0;
         double d3 = (double)screenrectangle.width() * d0;
         double d4 = (double)screenrectangle.height() * d0;
         RenderSystem.enableScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
      } else {
         RenderSystem.disableScissor();
      }

   }

   public void setColor(float f, float f1, float f2, float f3) {
      this.flushIfManaged();
      RenderSystem.setShaderColor(f, f1, f2, f3);
   }

   public void fill(int i, int j, int k, int l, int i1) {
      this.fill(i, j, k, l, 0, i1);
   }

   public void fill(int i, int j, int k, int l, int i1, int j1) {
      this.fill(RenderType.gui(), i, j, k, l, i1, j1);
   }

   public void fill(RenderType rendertype, int i, int j, int k, int l, int i1) {
      this.fill(rendertype, i, j, k, l, 0, i1);
   }

   public void fill(RenderType rendertype, int i, int j, int k, int l, int i1, int j1) {
      Matrix4f matrix4f = this.pose.last().pose();
      if (i < k) {
         int k1 = i;
         i = k;
         k = k1;
      }

      if (j < l) {
         int l1 = j;
         j = l;
         l = l1;
      }

      float f = (float)FastColor.ARGB32.alpha(j1) / 255.0F;
      float f1 = (float)FastColor.ARGB32.red(j1) / 255.0F;
      float f2 = (float)FastColor.ARGB32.green(j1) / 255.0F;
      float f3 = (float)FastColor.ARGB32.blue(j1) / 255.0F;
      VertexConsumer vertexconsumer = this.bufferSource.getBuffer(rendertype);
      vertexconsumer.vertex(matrix4f, (float)i, (float)j, (float)i1).color(f1, f2, f3, f).endVertex();
      vertexconsumer.vertex(matrix4f, (float)i, (float)l, (float)i1).color(f1, f2, f3, f).endVertex();
      vertexconsumer.vertex(matrix4f, (float)k, (float)l, (float)i1).color(f1, f2, f3, f).endVertex();
      vertexconsumer.vertex(matrix4f, (float)k, (float)j, (float)i1).color(f1, f2, f3, f).endVertex();
      this.flushIfUnmanaged();
   }

   public void fillGradient(int i, int j, int k, int l, int i1, int j1) {
      this.fillGradient(i, j, k, l, 0, i1, j1);
   }

   public void fillGradient(int i, int j, int k, int l, int i1, int j1, int k1) {
      this.fillGradient(RenderType.gui(), i, j, k, l, j1, k1, i1);
   }

   public void fillGradient(RenderType rendertype, int i, int j, int k, int l, int i1, int j1, int k1) {
      VertexConsumer vertexconsumer = this.bufferSource.getBuffer(rendertype);
      this.fillGradient(vertexconsumer, i, j, k, l, k1, i1, j1);
      this.flushIfUnmanaged();
   }

   private void fillGradient(VertexConsumer vertexconsumer, int i, int j, int k, int l, int i1, int j1, int k1) {
      float f = (float)FastColor.ARGB32.alpha(j1) / 255.0F;
      float f1 = (float)FastColor.ARGB32.red(j1) / 255.0F;
      float f2 = (float)FastColor.ARGB32.green(j1) / 255.0F;
      float f3 = (float)FastColor.ARGB32.blue(j1) / 255.0F;
      float f4 = (float)FastColor.ARGB32.alpha(k1) / 255.0F;
      float f5 = (float)FastColor.ARGB32.red(k1) / 255.0F;
      float f6 = (float)FastColor.ARGB32.green(k1) / 255.0F;
      float f7 = (float)FastColor.ARGB32.blue(k1) / 255.0F;
      Matrix4f matrix4f = this.pose.last().pose();
      vertexconsumer.vertex(matrix4f, (float)i, (float)j, (float)i1).color(f1, f2, f3, f).endVertex();
      vertexconsumer.vertex(matrix4f, (float)i, (float)l, (float)i1).color(f5, f6, f7, f4).endVertex();
      vertexconsumer.vertex(matrix4f, (float)k, (float)l, (float)i1).color(f5, f6, f7, f4).endVertex();
      vertexconsumer.vertex(matrix4f, (float)k, (float)j, (float)i1).color(f1, f2, f3, f).endVertex();
   }

   public void drawCenteredString(Font font, String s, int i, int j, int k) {
      this.drawString(font, s, i - font.width(s) / 2, j, k);
   }

   public void drawCenteredString(Font font, Component component, int i, int j, int k) {
      FormattedCharSequence formattedcharsequence = component.getVisualOrderText();
      this.drawString(font, formattedcharsequence, i - font.width(formattedcharsequence) / 2, j, k);
   }

   public void drawCenteredString(Font font, FormattedCharSequence formattedcharsequence, int i, int j, int k) {
      this.drawString(font, formattedcharsequence, i - font.width(formattedcharsequence) / 2, j, k);
   }

   public int drawString(Font font, @Nullable String s, int i, int j, int k) {
      return this.drawString(font, s, i, j, k, true);
   }

   public int drawString(Font font, @Nullable String s, int i, int j, int k, boolean flag) {
      if (s == null) {
         return 0;
      } else {
         int l = font.drawInBatch(s, (float)i, (float)j, k, flag, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional());
         this.flushIfUnmanaged();
         return l;
      }
   }

   public int drawString(Font font, FormattedCharSequence formattedcharsequence, int i, int j, int k) {
      return this.drawString(font, formattedcharsequence, i, j, k, true);
   }

   public int drawString(Font font, FormattedCharSequence formattedcharsequence, int i, int j, int k, boolean flag) {
      int l = font.drawInBatch(formattedcharsequence, (float)i, (float)j, k, flag, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
      this.flushIfUnmanaged();
      return l;
   }

   public int drawString(Font font, Component component, int i, int j, int k) {
      return this.drawString(font, component, i, j, k, true);
   }

   public int drawString(Font font, Component component, int i, int j, int k, boolean flag) {
      return this.drawString(font, component.getVisualOrderText(), i, j, k, flag);
   }

   public void drawWordWrap(Font font, FormattedText formattedtext, int i, int j, int k, int l) {
      for(FormattedCharSequence formattedcharsequence : font.split(formattedtext, k)) {
         this.drawString(font, formattedcharsequence, i, j, l, false);
         j += 9;
      }

   }

   public void blit(int i, int j, int k, int l, int i1, TextureAtlasSprite textureatlassprite) {
      this.innerBlit(textureatlassprite.atlasLocation(), i, i + l, j, j + i1, k, textureatlassprite.getU0(), textureatlassprite.getU1(), textureatlassprite.getV0(), textureatlassprite.getV1());
   }

   public void blit(int i, int j, int k, int l, int i1, TextureAtlasSprite textureatlassprite, float f, float f1, float f2, float f3) {
      this.innerBlit(textureatlassprite.atlasLocation(), i, i + l, j, j + i1, k, textureatlassprite.getU0(), textureatlassprite.getU1(), textureatlassprite.getV0(), textureatlassprite.getV1(), f, f1, f2, f3);
   }

   public void renderOutline(int i, int j, int k, int l, int i1) {
      this.fill(i, j, i + k, j + 1, i1);
      this.fill(i, j + l - 1, i + k, j + l, i1);
      this.fill(i, j + 1, i + 1, j + l - 1, i1);
      this.fill(i + k - 1, j + 1, i + k, j + l - 1, i1);
   }

   public void blit(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1) {
      this.blit(resourcelocation, i, j, 0, (float)k, (float)l, i1, j1, 256, 256);
   }

   public void blit(ResourceLocation resourcelocation, int i, int j, int k, float f, float f1, int l, int i1, int j1, int k1) {
      this.blit(resourcelocation, i, i + l, j, j + i1, k, l, i1, f, f1, j1, k1);
   }

   public void blit(ResourceLocation resourcelocation, int i, int j, int k, int l, float f, float f1, int i1, int j1, int k1, int l1) {
      this.blit(resourcelocation, i, i + k, j, j + l, 0, i1, j1, f, f1, k1, l1);
   }

   public void blit(ResourceLocation resourcelocation, int i, int j, float f, float f1, int k, int l, int i1, int j1) {
      this.blit(resourcelocation, i, j, k, l, f, f1, k, l, i1, j1);
   }

   void blit(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, float f, float f1, int l1, int i2) {
      this.innerBlit(resourcelocation, i, j, k, l, i1, (f + 0.0F) / (float)l1, (f + (float)j1) / (float)l1, (f1 + 0.0F) / (float)i2, (f1 + (float)k1) / (float)i2);
   }

   void innerBlit(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, float f, float f1, float f2, float f3) {
      RenderSystem.setShaderTexture(0, resourcelocation);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      Matrix4f matrix4f = this.pose.last().pose();
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix4f, (float)i, (float)k, (float)i1).uv(f, f2).endVertex();
      bufferbuilder.vertex(matrix4f, (float)i, (float)l, (float)i1).uv(f, f3).endVertex();
      bufferbuilder.vertex(matrix4f, (float)j, (float)l, (float)i1).uv(f1, f3).endVertex();
      bufferbuilder.vertex(matrix4f, (float)j, (float)k, (float)i1).uv(f1, f2).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
   }

   void innerBlit(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
      RenderSystem.setShaderTexture(0, resourcelocation);
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      RenderSystem.enableBlend();
      Matrix4f matrix4f = this.pose.last().pose();
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix4f, (float)i, (float)k, (float)i1).color(f4, f5, f6, f7).uv(f, f2).endVertex();
      bufferbuilder.vertex(matrix4f, (float)i, (float)l, (float)i1).color(f4, f5, f6, f7).uv(f, f3).endVertex();
      bufferbuilder.vertex(matrix4f, (float)j, (float)l, (float)i1).color(f4, f5, f6, f7).uv(f1, f3).endVertex();
      bufferbuilder.vertex(matrix4f, (float)j, (float)k, (float)i1).color(f4, f5, f6, f7).uv(f1, f2).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
      RenderSystem.disableBlend();
   }

   public void blitNineSliced(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
      this.blitNineSliced(resourcelocation, i, j, k, l, i1, i1, i1, i1, j1, k1, l1, i2);
   }

   public void blitNineSliced(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
      this.blitNineSliced(resourcelocation, i, j, k, l, i1, j1, i1, j1, k1, l1, i2, j2);
   }

   public void blitNineSliced(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2) {
      i1 = Math.min(i1, k / 2);
      k1 = Math.min(k1, k / 2);
      j1 = Math.min(j1, l / 2);
      l1 = Math.min(l1, l / 2);
      if (k == i2 && l == j2) {
         this.blit(resourcelocation, i, j, k2, l2, k, l);
      } else if (l == j2) {
         this.blit(resourcelocation, i, j, k2, l2, i1, l);
         this.blitRepeating(resourcelocation, i + i1, j, k - k1 - i1, l, k2 + i1, l2, i2 - k1 - i1, j2);
         this.blit(resourcelocation, i + k - k1, j, k2 + i2 - k1, l2, k1, l);
      } else if (k == i2) {
         this.blit(resourcelocation, i, j, k2, l2, k, j1);
         this.blitRepeating(resourcelocation, i, j + j1, k, l - l1 - j1, k2, l2 + j1, i2, j2 - l1 - j1);
         this.blit(resourcelocation, i, j + l - l1, k2, l2 + j2 - l1, k, l1);
      } else {
         this.blit(resourcelocation, i, j, k2, l2, i1, j1);
         this.blitRepeating(resourcelocation, i + i1, j, k - k1 - i1, j1, k2 + i1, l2, i2 - k1 - i1, j1);
         this.blit(resourcelocation, i + k - k1, j, k2 + i2 - k1, l2, k1, j1);
         this.blit(resourcelocation, i, j + l - l1, k2, l2 + j2 - l1, i1, l1);
         this.blitRepeating(resourcelocation, i + i1, j + l - l1, k - k1 - i1, l1, k2 + i1, l2 + j2 - l1, i2 - k1 - i1, l1);
         this.blit(resourcelocation, i + k - k1, j + l - l1, k2 + i2 - k1, l2 + j2 - l1, k1, l1);
         this.blitRepeating(resourcelocation, i, j + j1, i1, l - l1 - j1, k2, l2 + j1, i1, j2 - l1 - j1);
         this.blitRepeating(resourcelocation, i + i1, j + j1, k - k1 - i1, l - l1 - j1, k2 + i1, l2 + j1, i2 - k1 - i1, j2 - l1 - j1);
         this.blitRepeating(resourcelocation, i + k - k1, j + j1, i1, l - l1 - j1, k2 + i2 - k1, l2 + j1, k1, j2 - l1 - j1);
      }
   }

   public void blitRepeating(ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
      int i2 = i;

      int j2;
      for(IntIterator intiterator = slices(k, k1); intiterator.hasNext(); i2 += j2) {
         j2 = intiterator.nextInt();
         int k2 = (k1 - j2) / 2;
         int l2 = j;

         int i3;
         for(IntIterator intiterator1 = slices(l, l1); intiterator1.hasNext(); l2 += i3) {
            i3 = intiterator1.nextInt();
            int j3 = (l1 - i3) / 2;
            this.blit(resourcelocation, i2, l2, i1 + k2, j1 + j3, j2, i3);
         }
      }

   }

   private static IntIterator slices(int i, int j) {
      int k = Mth.positiveCeilDiv(i, j);
      return new Divisor(i, k);
   }

   public void renderItem(ItemStack itemstack, int i, int j) {
      this.renderItem(this.minecraft.player, this.minecraft.level, itemstack, i, j, 0);
   }

   public void renderItem(ItemStack itemstack, int i, int j, int k) {
      this.renderItem(this.minecraft.player, this.minecraft.level, itemstack, i, j, k);
   }

   public void renderItem(ItemStack itemstack, int i, int j, int k, int l) {
      this.renderItem(this.minecraft.player, this.minecraft.level, itemstack, i, j, k, l);
   }

   public void renderFakeItem(ItemStack itemstack, int i, int j) {
      this.renderItem((LivingEntity)null, this.minecraft.level, itemstack, i, j, 0);
   }

   public void renderItem(LivingEntity livingentity, ItemStack itemstack, int i, int j, int k) {
      this.renderItem(livingentity, livingentity.level(), itemstack, i, j, k);
   }

   private void renderItem(@Nullable LivingEntity livingentity, @Nullable Level level, ItemStack itemstack, int i, int j, int k) {
      this.renderItem(livingentity, level, itemstack, i, j, k, 0);
   }

   private void renderItem(@Nullable LivingEntity livingentity, @Nullable Level level, ItemStack itemstack, int i, int j, int k, int l) {
      if (!itemstack.isEmpty()) {
         BakedModel bakedmodel = this.minecraft.getItemRenderer().getModel(itemstack, level, livingentity, k);
         this.pose.pushPose();
         this.pose.translate((float)(i + 8), (float)(j + 8), (float)(150 + (bakedmodel.isGui3d() ? l : 0)));

         try {
            this.pose.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
            this.pose.scale(16.0F, 16.0F, 16.0F);
            boolean flag = !bakedmodel.usesBlockLight();
            if (flag) {
               Lighting.setupForFlatItems();
            }

            this.minecraft.getItemRenderer().render(itemstack, ItemDisplayContext.GUI, false, this.pose, this.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
            this.flush();
            if (flag) {
               Lighting.setupFor3DItems();
            }
         } catch (Throwable var12) {
            CrashReport crashreport = CrashReport.forThrowable(var12, "Rendering item");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
            crashreportcategory.setDetail("Item Type", () -> String.valueOf((Object)itemstack.getItem()));
            crashreportcategory.setDetail("Item Damage", () -> String.valueOf(itemstack.getDamageValue()));
            crashreportcategory.setDetail("Item NBT", () -> String.valueOf((Object)itemstack.getTag()));
            crashreportcategory.setDetail("Item Foil", () -> String.valueOf(itemstack.hasFoil()));
            throw new ReportedException(crashreport);
         }

         this.pose.popPose();
      }
   }

   public void renderItemDecorations(Font font, ItemStack itemstack, int i, int j) {
      this.renderItemDecorations(font, itemstack, i, j, (String)null);
   }

   public void renderItemDecorations(Font font, ItemStack itemstack, int i, int j, @Nullable String s) {
      if (!itemstack.isEmpty()) {
         this.pose.pushPose();
         if (itemstack.getCount() != 1 || s != null) {
            String s1 = s == null ? String.valueOf(itemstack.getCount()) : s;
            this.pose.translate(0.0F, 0.0F, 200.0F);
            this.drawString(font, s1, i + 19 - 2 - font.width(s1), j + 6 + 3, 16777215, true);
         }

         if (itemstack.isBarVisible()) {
            int k = itemstack.getBarWidth();
            int l = itemstack.getBarColor();
            int i1 = i + 2;
            int j1 = j + 13;
            this.fill(RenderType.guiOverlay(), i1, j1, i1 + 13, j1 + 2, -16777216);
            this.fill(RenderType.guiOverlay(), i1, j1, i1 + k, j1 + 1, l | -16777216);
         }

         LocalPlayer localplayer = this.minecraft.player;
         float f = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(itemstack.getItem(), this.minecraft.getFrameTime());
         if (f > 0.0F) {
            int k1 = j + Mth.floor(16.0F * (1.0F - f));
            int l1 = k1 + Mth.ceil(16.0F * f);
            this.fill(RenderType.guiOverlay(), i, k1, i + 16, l1, Integer.MAX_VALUE);
         }

         this.pose.popPose();
      }
   }

   public void renderTooltip(Font font, ItemStack itemstack, int i, int j) {
      this.renderTooltip(font, Screen.getTooltipFromItem(this.minecraft, itemstack), itemstack.getTooltipImage(), i, j);
   }

   public void renderTooltip(Font font, List<Component> list, Optional<TooltipComponent> optional, int i, int j) {
      List<ClientTooltipComponent> list1 = list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
      optional.ifPresent((tooltipcomponent) -> list1.add(1, ClientTooltipComponent.create(tooltipcomponent)));
      this.renderTooltipInternal(font, list1, i, j, DefaultTooltipPositioner.INSTANCE);
   }

   public void renderTooltip(Font font, Component component, int i, int j) {
      this.renderTooltip(font, List.of(component.getVisualOrderText()), i, j);
   }

   public void renderComponentTooltip(Font font, List<Component> list, int i, int j) {
      this.renderTooltip(font, Lists.transform(list, Component::getVisualOrderText), i, j);
   }

   public void renderTooltip(Font font, List<? extends FormattedCharSequence> list, int i, int j) {
      this.renderTooltipInternal(font, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, DefaultTooltipPositioner.INSTANCE);
   }

   public void renderTooltip(Font font, List<FormattedCharSequence> list, ClientTooltipPositioner clienttooltippositioner, int i, int j) {
      this.renderTooltipInternal(font, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), i, j, clienttooltippositioner);
   }

   private void renderTooltipInternal(Font font, List<ClientTooltipComponent> list, int i, int j, ClientTooltipPositioner clienttooltippositioner) {
      if (!list.isEmpty()) {
         int k = 0;
         int l = list.size() == 1 ? -2 : 0;

         for(ClientTooltipComponent clienttooltipcomponent : list) {
            int i1 = clienttooltipcomponent.getWidth(font);
            if (i1 > k) {
               k = i1;
            }

            l += clienttooltipcomponent.getHeight();
         }

         int j1 = k;
         int k1 = l;
         Vector2ic vector2ic = clienttooltippositioner.positionTooltip(this.guiWidth(), this.guiHeight(), i, j, j1, k1);
         int l1 = vector2ic.x();
         int i2 = vector2ic.y();
         this.pose.pushPose();
         int j2 = 400;
         this.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(this, l1, i2, j1, k1, 400));
         this.pose.translate(0.0F, 0.0F, 400.0F);
         int k2 = i2;

         for(int l2 = 0; l2 < list.size(); ++l2) {
            ClientTooltipComponent clienttooltipcomponent1 = list.get(l2);
            clienttooltipcomponent1.renderText(font, l1, k2, this.pose.last().pose(), this.bufferSource);
            k2 += clienttooltipcomponent1.getHeight() + (l2 == 0 ? 2 : 0);
         }

         k2 = i2;

         for(int i3 = 0; i3 < list.size(); ++i3) {
            ClientTooltipComponent clienttooltipcomponent2 = list.get(i3);
            clienttooltipcomponent2.renderImage(font, l1, k2, this);
            k2 += clienttooltipcomponent2.getHeight() + (i3 == 0 ? 2 : 0);
         }

         this.pose.popPose();
      }
   }

   public void renderComponentHoverEffect(Font font, @Nullable Style style, int i, int j) {
      if (style != null && style.getHoverEvent() != null) {
         HoverEvent hoverevent = style.getHoverEvent();
         HoverEvent.ItemStackInfo hoverevent_itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
         if (hoverevent_itemstackinfo != null) {
            this.renderTooltip(font, hoverevent_itemstackinfo.getItemStack(), i, j);
         } else {
            HoverEvent.EntityTooltipInfo hoverevent_entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
            if (hoverevent_entitytooltipinfo != null) {
               if (this.minecraft.options.advancedItemTooltips) {
                  this.renderComponentTooltip(font, hoverevent_entitytooltipinfo.getTooltipLines(), i, j);
               }
            } else {
               Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
               if (component != null) {
                  this.renderTooltip(font, font.split(component, Math.max(this.guiWidth() / 2, 200)), i, j);
               }
            }
         }

      }
   }

   static class ScissorStack {
      private final Deque<ScreenRectangle> stack = new ArrayDeque<>();

      public ScreenRectangle push(ScreenRectangle screenrectangle) {
         ScreenRectangle screenrectangle1 = this.stack.peekLast();
         if (screenrectangle1 != null) {
            ScreenRectangle screenrectangle2 = Objects.requireNonNullElse(screenrectangle.intersection(screenrectangle1), ScreenRectangle.empty());
            this.stack.addLast(screenrectangle2);
            return screenrectangle2;
         } else {
            this.stack.addLast(screenrectangle);
            return screenrectangle;
         }
      }

      public @Nullable ScreenRectangle pop() {
         if (this.stack.isEmpty()) {
            throw new IllegalStateException("Scissor stack underflow");
         } else {
            this.stack.removeLast();
            return this.stack.peekLast();
         }
      }
   }
}
