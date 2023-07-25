package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;

public class VertexFormatElement {
   private final VertexFormatElement.Type type;
   private final VertexFormatElement.Usage usage;
   private final int index;
   private final int count;
   private final int byteSize;

   public VertexFormatElement(int i, VertexFormatElement.Type vertexformatelement_type, VertexFormatElement.Usage vertexformatelement_usage, int j) {
      if (this.supportsUsage(i, vertexformatelement_usage)) {
         this.usage = vertexformatelement_usage;
         this.type = vertexformatelement_type;
         this.index = i;
         this.count = j;
         this.byteSize = vertexformatelement_type.getSize() * this.count;
      } else {
         throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
      }
   }

   private boolean supportsUsage(int i, VertexFormatElement.Usage vertexformatelement_usage) {
      return i == 0 || vertexformatelement_usage == VertexFormatElement.Usage.UV;
   }

   public final VertexFormatElement.Type getType() {
      return this.type;
   }

   public final VertexFormatElement.Usage getUsage() {
      return this.usage;
   }

   public final int getCount() {
      return this.count;
   }

   public final int getIndex() {
      return this.index;
   }

   public String toString() {
      return this.count + "," + this.usage.getName() + "," + this.type.getName();
   }

   public final int getByteSize() {
      return this.byteSize;
   }

   public final boolean isPosition() {
      return this.usage == VertexFormatElement.Usage.POSITION;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         VertexFormatElement vertexformatelement = (VertexFormatElement)object;
         if (this.count != vertexformatelement.count) {
            return false;
         } else if (this.index != vertexformatelement.index) {
            return false;
         } else if (this.type != vertexformatelement.type) {
            return false;
         } else {
            return this.usage == vertexformatelement.usage;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.type.hashCode();
      i = 31 * i + this.usage.hashCode();
      i = 31 * i + this.index;
      return 31 * i + this.count;
   }

   public void setupBufferState(int i, long j, int k) {
      this.usage.setupBufferState(this.count, this.type.getGlType(), k, j, this.index, i);
   }

   public void clearBufferState(int i) {
      this.usage.clearBufferState(this.index, i);
   }

   public static enum Type {
      FLOAT(4, "Float", 5126),
      UBYTE(1, "Unsigned Byte", 5121),
      BYTE(1, "Byte", 5120),
      USHORT(2, "Unsigned Short", 5123),
      SHORT(2, "Short", 5122),
      UINT(4, "Unsigned Int", 5125),
      INT(4, "Int", 5124);

      private final int size;
      private final String name;
      private final int glType;

      private Type(int i, String s, int j) {
         this.size = i;
         this.name = s;
         this.glType = j;
      }

      public int getSize() {
         return this.size;
      }

      public String getName() {
         return this.name;
      }

      public int getGlType() {
         return this.glType;
      }
   }

   public static enum Usage {
      POSITION("Position", (i, j, k, l, i1, j1) -> {
         GlStateManager._enableVertexAttribArray(j1);
         GlStateManager._vertexAttribPointer(j1, i, j, false, k, l);
      }, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
      NORMAL("Normal", (i, j, k, l, i1, j1) -> {
         GlStateManager._enableVertexAttribArray(j1);
         GlStateManager._vertexAttribPointer(j1, i, j, true, k, l);
      }, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
      COLOR("Vertex Color", (i, j, k, l, i1, j1) -> {
         GlStateManager._enableVertexAttribArray(j1);
         GlStateManager._vertexAttribPointer(j1, i, j, true, k, l);
      }, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
      UV("UV", (i, j, k, l, i1, j1) -> {
         GlStateManager._enableVertexAttribArray(j1);
         if (j == 5126) {
            GlStateManager._vertexAttribPointer(j1, i, j, false, k, l);
         } else {
            GlStateManager._vertexAttribIPointer(j1, i, j, k, l);
         }

      }, (i, j) -> GlStateManager._disableVertexAttribArray(j)),
      PADDING("Padding", (i, j, k, l, i1, j1) -> {
      }, (i, j) -> {
      }),
      GENERIC("Generic", (i, j, k, l, i1, j1) -> {
         GlStateManager._enableVertexAttribArray(j1);
         GlStateManager._vertexAttribPointer(j1, i, j, false, k, l);
      }, (i, j) -> GlStateManager._disableVertexAttribArray(j));

      private final String name;
      private final VertexFormatElement.Usage.SetupState setupState;
      private final VertexFormatElement.Usage.ClearState clearState;

      private Usage(String s, VertexFormatElement.Usage.SetupState vertexformatelement_usage_setupstate, VertexFormatElement.Usage.ClearState vertexformatelement_usage_clearstate) {
         this.name = s;
         this.setupState = vertexformatelement_usage_setupstate;
         this.clearState = vertexformatelement_usage_clearstate;
      }

      void setupBufferState(int i, int j, int k, long l, int i1, int j1) {
         this.setupState.setupBufferState(i, j, k, l, i1, j1);
      }

      public void clearBufferState(int i, int j) {
         this.clearState.clearBufferState(i, j);
      }

      public String getName() {
         return this.name;
      }

      @FunctionalInterface
      interface ClearState {
         void clearBufferState(int i, int j);
      }

      @FunctionalInterface
      interface SetupState {
         void setupBufferState(int i, int j, int k, long l, int i1, int j1);
      }
   }
}
