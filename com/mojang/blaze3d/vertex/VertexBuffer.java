package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public class VertexBuffer implements AutoCloseable {
   private final VertexBuffer.Usage usage;
   private int vertexBufferId;
   private int indexBufferId;
   private int arrayObjectId;
   @Nullable
   private VertexFormat format;
   @Nullable
   private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
   private VertexFormat.IndexType indexType;
   private int indexCount;
   private VertexFormat.Mode mode;

   public VertexBuffer(VertexBuffer.Usage vertexbuffer_usage) {
      this.usage = vertexbuffer_usage;
      RenderSystem.assertOnRenderThread();
      this.vertexBufferId = GlStateManager._glGenBuffers();
      this.indexBufferId = GlStateManager._glGenBuffers();
      this.arrayObjectId = GlStateManager._glGenVertexArrays();
   }

   public void upload(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer) {
      if (!this.isInvalid()) {
         RenderSystem.assertOnRenderThread();

         try {
            BufferBuilder.DrawState bufferbuilder_drawstate = bufferbuilder_renderedbuffer.drawState();
            this.format = this.uploadVertexBuffer(bufferbuilder_drawstate, bufferbuilder_renderedbuffer.vertexBuffer());
            this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder_drawstate, bufferbuilder_renderedbuffer.indexBuffer());
            this.indexCount = bufferbuilder_drawstate.indexCount();
            this.indexType = bufferbuilder_drawstate.indexType();
            this.mode = bufferbuilder_drawstate.mode();
         } finally {
            bufferbuilder_renderedbuffer.release();
         }

      }
   }

   private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState bufferbuilder_drawstate, ByteBuffer bytebuffer) {
      boolean flag = false;
      if (!bufferbuilder_drawstate.format().equals(this.format)) {
         if (this.format != null) {
            this.format.clearBufferState();
         }

         GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         bufferbuilder_drawstate.format().setupBufferState();
         flag = true;
      }

      if (!bufferbuilder_drawstate.indexOnly()) {
         if (!flag) {
            GlStateManager._glBindBuffer(34962, this.vertexBufferId);
         }

         RenderSystem.glBufferData(34962, bytebuffer, this.usage.id);
      }

      return bufferbuilder_drawstate.format();
   }

   @Nullable
   private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState bufferbuilder_drawstate, ByteBuffer bytebuffer) {
      if (!bufferbuilder_drawstate.sequentialIndex()) {
         GlStateManager._glBindBuffer(34963, this.indexBufferId);
         RenderSystem.glBufferData(34963, bytebuffer, this.usage.id);
         return null;
      } else {
         RenderSystem.AutoStorageIndexBuffer rendersystem_autostorageindexbuffer = RenderSystem.getSequentialBuffer(bufferbuilder_drawstate.mode());
         if (rendersystem_autostorageindexbuffer != this.sequentialIndices || !rendersystem_autostorageindexbuffer.hasStorage(bufferbuilder_drawstate.indexCount())) {
            rendersystem_autostorageindexbuffer.bind(bufferbuilder_drawstate.indexCount());
         }

         return rendersystem_autostorageindexbuffer;
      }
   }

   public void bind() {
      BufferUploader.invalidate();
      GlStateManager._glBindVertexArray(this.arrayObjectId);
   }

   public static void unbind() {
      BufferUploader.invalidate();
      GlStateManager._glBindVertexArray(0);
   }

   public void draw() {
      RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
   }

   private VertexFormat.IndexType getIndexType() {
      RenderSystem.AutoStorageIndexBuffer rendersystem_autostorageindexbuffer = this.sequentialIndices;
      return rendersystem_autostorageindexbuffer != null ? rendersystem_autostorageindexbuffer.type() : this.indexType;
   }

   public void drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f1, ShaderInstance shaderinstance) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> this._drawWithShader(new Matrix4f(matrix4f), new Matrix4f(matrix4f1), shaderinstance));
      } else {
         this._drawWithShader(matrix4f, matrix4f1, shaderinstance);
      }

   }

   private void _drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f1, ShaderInstance shaderinstance) {
      for(int i = 0; i < 12; ++i) {
         int j = RenderSystem.getShaderTexture(i);
         shaderinstance.setSampler("Sampler" + i, j);
      }

      if (shaderinstance.MODEL_VIEW_MATRIX != null) {
         shaderinstance.MODEL_VIEW_MATRIX.set(matrix4f);
      }

      if (shaderinstance.PROJECTION_MATRIX != null) {
         shaderinstance.PROJECTION_MATRIX.set(matrix4f1);
      }

      if (shaderinstance.INVERSE_VIEW_ROTATION_MATRIX != null) {
         shaderinstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
      }

      if (shaderinstance.COLOR_MODULATOR != null) {
         shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
      }

      if (shaderinstance.GLINT_ALPHA != null) {
         shaderinstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
      }

      if (shaderinstance.FOG_START != null) {
         shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
      }

      if (shaderinstance.FOG_END != null) {
         shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
      }

      if (shaderinstance.FOG_COLOR != null) {
         shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
      }

      if (shaderinstance.FOG_SHAPE != null) {
         shaderinstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
      }

      if (shaderinstance.TEXTURE_MATRIX != null) {
         shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
      }

      if (shaderinstance.GAME_TIME != null) {
         shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
      }

      if (shaderinstance.SCREEN_SIZE != null) {
         Window window = Minecraft.getInstance().getWindow();
         shaderinstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
      }

      if (shaderinstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
         shaderinstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
      }

      RenderSystem.setupShaderLights(shaderinstance);
      shaderinstance.apply();
      this.draw();
      shaderinstance.clear();
   }

   public void close() {
      if (this.vertexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.vertexBufferId);
         this.vertexBufferId = -1;
      }

      if (this.indexBufferId >= 0) {
         RenderSystem.glDeleteBuffers(this.indexBufferId);
         this.indexBufferId = -1;
      }

      if (this.arrayObjectId >= 0) {
         RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
         this.arrayObjectId = -1;
      }

   }

   public VertexFormat getFormat() {
      return this.format;
   }

   public boolean isInvalid() {
      return this.arrayObjectId == -1;
   }

   public static enum Usage {
      STATIC(35044),
      DYNAMIC(35048);

      final int id;

      private Usage(int i) {
         this.id = i;
      }
   }
}
