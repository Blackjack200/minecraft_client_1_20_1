package com.mojang.blaze3d.pipeline;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

public class MainTarget extends RenderTarget {
   public static final int DEFAULT_WIDTH = 854;
   public static final int DEFAULT_HEIGHT = 480;
   static final MainTarget.Dimension DEFAULT_DIMENSIONS = new MainTarget.Dimension(854, 480);

   public MainTarget(int i, int j) {
      super(true);
      RenderSystem.assertOnRenderThreadOrInit();
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> this.createFrameBuffer(i, j));
      } else {
         this.createFrameBuffer(i, j);
      }

   }

   private void createFrameBuffer(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      MainTarget.Dimension maintarget_dimension = this.allocateAttachments(i, j);
      this.frameBufferId = GlStateManager.glGenFramebuffers();
      GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
      GlStateManager._bindTexture(this.colorTextureId);
      GlStateManager._texParameter(3553, 10241, 9728);
      GlStateManager._texParameter(3553, 10240, 9728);
      GlStateManager._texParameter(3553, 10242, 33071);
      GlStateManager._texParameter(3553, 10243, 33071);
      GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
      GlStateManager._bindTexture(this.depthBufferId);
      GlStateManager._texParameter(3553, 34892, 0);
      GlStateManager._texParameter(3553, 10241, 9728);
      GlStateManager._texParameter(3553, 10240, 9728);
      GlStateManager._texParameter(3553, 10242, 33071);
      GlStateManager._texParameter(3553, 10243, 33071);
      GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
      GlStateManager._bindTexture(0);
      this.viewWidth = maintarget_dimension.width;
      this.viewHeight = maintarget_dimension.height;
      this.width = maintarget_dimension.width;
      this.height = maintarget_dimension.height;
      this.checkStatus();
      GlStateManager._glBindFramebuffer(36160, 0);
   }

   private MainTarget.Dimension allocateAttachments(int i, int j) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.colorTextureId = TextureUtil.generateTextureId();
      this.depthBufferId = TextureUtil.generateTextureId();
      MainTarget.AttachmentState maintarget_attachmentstate = MainTarget.AttachmentState.NONE;

      for(MainTarget.Dimension maintarget_dimension : MainTarget.Dimension.listWithFallback(i, j)) {
         maintarget_attachmentstate = MainTarget.AttachmentState.NONE;
         if (this.allocateColorAttachment(maintarget_dimension)) {
            maintarget_attachmentstate = maintarget_attachmentstate.with(MainTarget.AttachmentState.COLOR);
         }

         if (this.allocateDepthAttachment(maintarget_dimension)) {
            maintarget_attachmentstate = maintarget_attachmentstate.with(MainTarget.AttachmentState.DEPTH);
         }

         if (maintarget_attachmentstate == MainTarget.AttachmentState.COLOR_DEPTH) {
            return maintarget_dimension;
         }
      }

      throw new RuntimeException("Unrecoverable GL_OUT_OF_MEMORY (allocated attachments = " + maintarget_attachmentstate.name() + ")");
   }

   private boolean allocateColorAttachment(MainTarget.Dimension maintarget_dimension) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._getError();
      GlStateManager._bindTexture(this.colorTextureId);
      GlStateManager._texImage2D(3553, 0, 32856, maintarget_dimension.width, maintarget_dimension.height, 0, 6408, 5121, (IntBuffer)null);
      return GlStateManager._getError() != 1285;
   }

   private boolean allocateDepthAttachment(MainTarget.Dimension maintarget_dimension) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._getError();
      GlStateManager._bindTexture(this.depthBufferId);
      GlStateManager._texImage2D(3553, 0, 6402, maintarget_dimension.width, maintarget_dimension.height, 0, 6402, 5126, (IntBuffer)null);
      return GlStateManager._getError() != 1285;
   }

   static enum AttachmentState {
      NONE,
      COLOR,
      DEPTH,
      COLOR_DEPTH;

      private static final MainTarget.AttachmentState[] VALUES = values();

      MainTarget.AttachmentState with(MainTarget.AttachmentState maintarget_attachmentstate) {
         return VALUES[this.ordinal() | maintarget_attachmentstate.ordinal()];
      }
   }

   static class Dimension {
      public final int width;
      public final int height;

      Dimension(int i, int j) {
         this.width = i;
         this.height = j;
      }

      static List<MainTarget.Dimension> listWithFallback(int i, int j) {
         RenderSystem.assertOnRenderThreadOrInit();
         int k = RenderSystem.maxSupportedTextureSize();
         return i > 0 && i <= k && j > 0 && j <= k ? ImmutableList.of(new MainTarget.Dimension(i, j), MainTarget.DEFAULT_DIMENSIONS) : ImmutableList.of(MainTarget.DEFAULT_DIMENSIONS);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            MainTarget.Dimension maintarget_dimension = (MainTarget.Dimension)object;
            return this.width == maintarget_dimension.width && this.height == maintarget_dimension.height;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.width, this.height);
      }

      public String toString() {
         return this.width + "x" + this.height;
      }
   }
}
