package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class VertexFormat {
   private final ImmutableList<VertexFormatElement> elements;
   private final ImmutableMap<String, VertexFormatElement> elementMapping;
   private final IntList offsets = new IntArrayList();
   private final int vertexSize;
   @Nullable
   private VertexBuffer immediateDrawVertexBuffer;

   public VertexFormat(ImmutableMap<String, VertexFormatElement> immutablemap) {
      this.elementMapping = immutablemap;
      this.elements = immutablemap.values().asList();
      int i = 0;

      for(VertexFormatElement vertexformatelement : immutablemap.values()) {
         this.offsets.add(i);
         i += vertexformatelement.getByteSize();
      }

      this.vertexSize = i;
   }

   public String toString() {
      return "format: " + this.elementMapping.size() + " elements: " + (String)this.elementMapping.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
   }

   public int getIntegerSize() {
      return this.getVertexSize() / 4;
   }

   public int getVertexSize() {
      return this.vertexSize;
   }

   public ImmutableList<VertexFormatElement> getElements() {
      return this.elements;
   }

   public ImmutableList<String> getElementAttributeNames() {
      return this.elementMapping.keySet().asList();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         VertexFormat vertexformat = (VertexFormat)object;
         return this.vertexSize != vertexformat.vertexSize ? false : this.elementMapping.equals(vertexformat.elementMapping);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.elementMapping.hashCode();
   }

   public void setupBufferState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::_setupBufferState);
      } else {
         this._setupBufferState();
      }
   }

   private void _setupBufferState() {
      int i = this.getVertexSize();
      List<VertexFormatElement> list = this.getElements();

      for(int j = 0; j < list.size(); ++j) {
         list.get(j).setupBufferState(j, (long)this.offsets.getInt(j), i);
      }

   }

   public void clearBufferState() {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(this::_clearBufferState);
      } else {
         this._clearBufferState();
      }
   }

   private void _clearBufferState() {
      ImmutableList<VertexFormatElement> immutablelist = this.getElements();

      for(int i = 0; i < immutablelist.size(); ++i) {
         VertexFormatElement vertexformatelement = immutablelist.get(i);
         vertexformatelement.clearBufferState(i);
      }

   }

   public VertexBuffer getImmediateDrawVertexBuffer() {
      VertexBuffer vertexbuffer = this.immediateDrawVertexBuffer;
      if (vertexbuffer == null) {
         this.immediateDrawVertexBuffer = vertexbuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
      }

      return vertexbuffer;
   }

   public static enum IndexType {
      SHORT(5123, 2),
      INT(5125, 4);

      public final int asGLType;
      public final int bytes;

      private IndexType(int i, int j) {
         this.asGLType = i;
         this.bytes = j;
      }

      public static VertexFormat.IndexType least(int i) {
         return (i & -65536) != 0 ? INT : SHORT;
      }
   }

   public static enum Mode {
      LINES(4, 2, 2, false),
      LINE_STRIP(5, 2, 1, true),
      DEBUG_LINES(1, 2, 2, false),
      DEBUG_LINE_STRIP(3, 2, 1, true),
      TRIANGLES(4, 3, 3, false),
      TRIANGLE_STRIP(5, 3, 1, true),
      TRIANGLE_FAN(6, 3, 1, true),
      QUADS(4, 4, 4, false);

      public final int asGLMode;
      public final int primitiveLength;
      public final int primitiveStride;
      public final boolean connectedPrimitives;

      private Mode(int i, int j, int k, boolean flag) {
         this.asGLMode = i;
         this.primitiveLength = j;
         this.primitiveStride = k;
         this.connectedPrimitives = flag;
      }

      public int indexCount(int i) {
         int j;
         switch (this) {
            case LINE_STRIP:
            case DEBUG_LINES:
            case DEBUG_LINE_STRIP:
            case TRIANGLES:
            case TRIANGLE_STRIP:
            case TRIANGLE_FAN:
               j = i;
               break;
            case LINES:
            case QUADS:
               j = i / 4 * 6;
               break;
            default:
               j = 0;
         }

         return j;
      }
   }
}
