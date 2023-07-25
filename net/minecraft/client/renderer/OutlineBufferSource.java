package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultedVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;

public class OutlineBufferSource implements MultiBufferSource {
   private final MultiBufferSource.BufferSource bufferSource;
   private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
   private int teamR = 255;
   private int teamG = 255;
   private int teamB = 255;
   private int teamA = 255;

   public OutlineBufferSource(MultiBufferSource.BufferSource multibuffersource_buffersource) {
      this.bufferSource = multibuffersource_buffersource;
   }

   public VertexConsumer getBuffer(RenderType rendertype) {
      if (rendertype.isOutline()) {
         VertexConsumer vertexconsumer = this.outlineBufferSource.getBuffer(rendertype);
         return new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer, this.teamR, this.teamG, this.teamB, this.teamA);
      } else {
         VertexConsumer vertexconsumer1 = this.bufferSource.getBuffer(rendertype);
         Optional<RenderType> optional = rendertype.outline();
         if (optional.isPresent()) {
            VertexConsumer vertexconsumer2 = this.outlineBufferSource.getBuffer(optional.get());
            OutlineBufferSource.EntityOutlineGenerator outlinebuffersource_entityoutlinegenerator = new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
            return VertexMultiConsumer.create(outlinebuffersource_entityoutlinegenerator, vertexconsumer1);
         } else {
            return vertexconsumer1;
         }
      }
   }

   public void setColor(int i, int j, int k, int l) {
      this.teamR = i;
      this.teamG = j;
      this.teamB = k;
      this.teamA = l;
   }

   public void endOutlineBatch() {
      this.outlineBufferSource.endBatch();
   }

   static class EntityOutlineGenerator extends DefaultedVertexConsumer {
      private final VertexConsumer delegate;
      private double x;
      private double y;
      private double z;
      private float u;
      private float v;

      EntityOutlineGenerator(VertexConsumer vertexconsumer, int i, int j, int k, int l) {
         this.delegate = vertexconsumer;
         super.defaultColor(i, j, k, l);
      }

      public void defaultColor(int i, int j, int k, int l) {
      }

      public void unsetDefaultColor() {
      }

      public VertexConsumer vertex(double d0, double d1, double d2) {
         this.x = d0;
         this.y = d1;
         this.z = d2;
         return this;
      }

      public VertexConsumer color(int i, int j, int k, int l) {
         return this;
      }

      public VertexConsumer uv(float f, float f1) {
         this.u = f;
         this.v = f1;
         return this;
      }

      public VertexConsumer overlayCoords(int i, int j) {
         return this;
      }

      public VertexConsumer uv2(int i, int j) {
         return this;
      }

      public VertexConsumer normal(float f, float f1, float f2) {
         return this;
      }

      public void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
         this.delegate.vertex((double)f, (double)f1, (double)f2).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(f7, f8).endVertex();
      }

      public void endVertex() {
         this.delegate.vertex(this.x, this.y, this.z).color(this.defaultR, this.defaultG, this.defaultB, this.defaultA).uv(this.u, this.v).endVertex();
      }
   }
}
