package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;

public class BufferUploader {
   @Nullable
   private static VertexBuffer lastImmediateBuffer;

   public static void reset() {
      if (lastImmediateBuffer != null) {
         invalidate();
         VertexBuffer.unbind();
      }

   }

   public static void invalidate() {
      lastImmediateBuffer = null;
   }

   public static void drawWithShader(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> _drawWithShader(bufferbuilder_renderedbuffer));
      } else {
         _drawWithShader(bufferbuilder_renderedbuffer);
      }

   }

   private static void _drawWithShader(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer) {
      VertexBuffer vertexbuffer = upload(bufferbuilder_renderedbuffer);
      if (vertexbuffer != null) {
         vertexbuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
      }

   }

   public static void draw(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer) {
      VertexBuffer vertexbuffer = upload(bufferbuilder_renderedbuffer);
      if (vertexbuffer != null) {
         vertexbuffer.draw();
      }

   }

   @Nullable
   private static VertexBuffer upload(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer) {
      RenderSystem.assertOnRenderThread();
      if (bufferbuilder_renderedbuffer.isEmpty()) {
         bufferbuilder_renderedbuffer.release();
         return null;
      } else {
         VertexBuffer vertexbuffer = bindImmediateBuffer(bufferbuilder_renderedbuffer.drawState().format());
         vertexbuffer.upload(bufferbuilder_renderedbuffer);
         return vertexbuffer;
      }
   }

   private static VertexBuffer bindImmediateBuffer(VertexFormat vertexformat) {
      VertexBuffer vertexbuffer = vertexformat.getImmediateDrawVertexBuffer();
      bindImmediateBuffer(vertexbuffer);
      return vertexbuffer;
   }

   private static void bindImmediateBuffer(VertexBuffer vertexbuffer) {
      if (vertexbuffer != lastImmediateBuffer) {
         vertexbuffer.bind();
         lastImmediateBuffer = vertexbuffer;
      }

   }
}
