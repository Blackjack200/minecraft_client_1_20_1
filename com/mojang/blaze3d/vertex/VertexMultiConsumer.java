package com.mojang.blaze3d.vertex;

import java.util.function.Consumer;

public class VertexMultiConsumer {
   public static VertexConsumer create() {
      throw new IllegalArgumentException();
   }

   public static VertexConsumer create(VertexConsumer vertexconsumer) {
      return vertexconsumer;
   }

   public static VertexConsumer create(VertexConsumer vertexconsumer, VertexConsumer vertexconsumer1) {
      return new VertexMultiConsumer.Double(vertexconsumer, vertexconsumer1);
   }

   public static VertexConsumer create(VertexConsumer... avertexconsumer) {
      return new VertexMultiConsumer.Multiple(avertexconsumer);
   }

   static class Double implements VertexConsumer {
      private final VertexConsumer first;
      private final VertexConsumer second;

      public Double(VertexConsumer vertexconsumer, VertexConsumer vertexconsumer1) {
         if (vertexconsumer == vertexconsumer1) {
            throw new IllegalArgumentException("Duplicate delegates");
         } else {
            this.first = vertexconsumer;
            this.second = vertexconsumer1;
         }
      }

      public VertexConsumer vertex(double d0, double d1, double d2) {
         this.first.vertex(d0, d1, d2);
         this.second.vertex(d0, d1, d2);
         return this;
      }

      public VertexConsumer color(int i, int j, int k, int l) {
         this.first.color(i, j, k, l);
         this.second.color(i, j, k, l);
         return this;
      }

      public VertexConsumer uv(float f, float f1) {
         this.first.uv(f, f1);
         this.second.uv(f, f1);
         return this;
      }

      public VertexConsumer overlayCoords(int i, int j) {
         this.first.overlayCoords(i, j);
         this.second.overlayCoords(i, j);
         return this;
      }

      public VertexConsumer uv2(int i, int j) {
         this.first.uv2(i, j);
         this.second.uv2(i, j);
         return this;
      }

      public VertexConsumer normal(float f, float f1, float f2) {
         this.first.normal(f, f1, f2);
         this.second.normal(f, f1, f2);
         return this;
      }

      public void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
         this.first.vertex(f, f1, f2, f3, f4, f5, f6, f7, f8, i, j, f9, f10, f11);
         this.second.vertex(f, f1, f2, f3, f4, f5, f6, f7, f8, i, j, f9, f10, f11);
      }

      public void endVertex() {
         this.first.endVertex();
         this.second.endVertex();
      }

      public void defaultColor(int i, int j, int k, int l) {
         this.first.defaultColor(i, j, k, l);
         this.second.defaultColor(i, j, k, l);
      }

      public void unsetDefaultColor() {
         this.first.unsetDefaultColor();
         this.second.unsetDefaultColor();
      }
   }

   static class Multiple implements VertexConsumer {
      private final VertexConsumer[] delegates;

      public Multiple(VertexConsumer[] avertexconsumer) {
         for(int i = 0; i < avertexconsumer.length; ++i) {
            for(int j = i + 1; j < avertexconsumer.length; ++j) {
               if (avertexconsumer[i] == avertexconsumer[j]) {
                  throw new IllegalArgumentException("Duplicate delegates");
               }
            }
         }

         this.delegates = avertexconsumer;
      }

      private void forEach(Consumer<VertexConsumer> consumer) {
         for(VertexConsumer vertexconsumer : this.delegates) {
            consumer.accept(vertexconsumer);
         }

      }

      public VertexConsumer vertex(double d0, double d1, double d2) {
         this.forEach((vertexconsumer) -> vertexconsumer.vertex(d0, d1, d2));
         return this;
      }

      public VertexConsumer color(int i, int j, int k, int l) {
         this.forEach((vertexconsumer) -> vertexconsumer.color(i, j, k, l));
         return this;
      }

      public VertexConsumer uv(float f, float f1) {
         this.forEach((vertexconsumer) -> vertexconsumer.uv(f, f1));
         return this;
      }

      public VertexConsumer overlayCoords(int i, int j) {
         this.forEach((vertexconsumer) -> vertexconsumer.overlayCoords(i, j));
         return this;
      }

      public VertexConsumer uv2(int i, int j) {
         this.forEach((vertexconsumer) -> vertexconsumer.uv2(i, j));
         return this;
      }

      public VertexConsumer normal(float f, float f1, float f2) {
         this.forEach((vertexconsumer) -> vertexconsumer.normal(f, f1, f2));
         return this;
      }

      public void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
         this.forEach((vertexconsumer) -> vertexconsumer.vertex(f, f1, f2, f3, f4, f5, f6, f7, f8, i, j, f9, f10, f11));
      }

      public void endVertex() {
         this.forEach(VertexConsumer::endVertex);
      }

      public void defaultColor(int i, int j, int k, int l) {
         this.forEach((vertexconsumer) -> vertexconsumer.defaultColor(i, j, k, l));
      }

      public void unsetDefaultColor() {
         this.forEach(VertexConsumer::unsetDefaultColor);
      }
   }
}
