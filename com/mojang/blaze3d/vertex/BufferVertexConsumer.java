package com.mojang.blaze3d.vertex;

import net.minecraft.util.Mth;

public interface BufferVertexConsumer extends VertexConsumer {
   VertexFormatElement currentElement();

   void nextElement();

   void putByte(int i, byte b0);

   void putShort(int i, short short0);

   void putFloat(int i, float f);

   default VertexConsumer vertex(double d0, double d1, double d2) {
      if (this.currentElement().getUsage() != VertexFormatElement.Usage.POSITION) {
         return this;
      } else if (this.currentElement().getType() == VertexFormatElement.Type.FLOAT && this.currentElement().getCount() == 3) {
         this.putFloat(0, (float)d0);
         this.putFloat(4, (float)d1);
         this.putFloat(8, (float)d2);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer color(int i, int j, int k, int l) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.COLOR) {
         return this;
      } else if (vertexformatelement.getType() == VertexFormatElement.Type.UBYTE && vertexformatelement.getCount() == 4) {
         this.putByte(0, (byte)i);
         this.putByte(1, (byte)j);
         this.putByte(2, (byte)k);
         this.putByte(3, (byte)l);
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   default VertexConsumer uv(float f, float f1) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == 0) {
         if (vertexformatelement.getType() == VertexFormatElement.Type.FLOAT && vertexformatelement.getCount() == 2) {
            this.putFloat(0, f);
            this.putFloat(4, f1);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer overlayCoords(int i, int j) {
      return this.uvShort((short)i, (short)j, 1);
   }

   default VertexConsumer uv2(int i, int j) {
      return this.uvShort((short)i, (short)j, 2);
   }

   default VertexConsumer uvShort(short short0, short short1, int i) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.UV && vertexformatelement.getIndex() == i) {
         if (vertexformatelement.getType() == VertexFormatElement.Type.SHORT && vertexformatelement.getCount() == 2) {
            this.putShort(0, short0);
            this.putShort(2, short1);
            this.nextElement();
            return this;
         } else {
            throw new IllegalStateException();
         }
      } else {
         return this;
      }
   }

   default VertexConsumer normal(float f, float f1, float f2) {
      VertexFormatElement vertexformatelement = this.currentElement();
      if (vertexformatelement.getUsage() != VertexFormatElement.Usage.NORMAL) {
         return this;
      } else if (vertexformatelement.getType() == VertexFormatElement.Type.BYTE && vertexformatelement.getCount() == 3) {
         this.putByte(0, normalIntValue(f));
         this.putByte(1, normalIntValue(f1));
         this.putByte(2, normalIntValue(f2));
         this.nextElement();
         return this;
      } else {
         throw new IllegalStateException();
      }
   }

   static byte normalIntValue(float f) {
      return (byte)((int)(Mth.clamp(f, -1.0F, 1.0F) * 127.0F) & 255);
   }
}
