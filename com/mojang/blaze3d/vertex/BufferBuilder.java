package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
   private static final int GROWTH_SIZE = 2097152;
   private static final Logger LOGGER = LogUtils.getLogger();
   private ByteBuffer buffer;
   private int renderedBufferCount;
   private int renderedBufferPointer;
   private int nextElementByte;
   private int vertices;
   @Nullable
   private VertexFormatElement currentElement;
   private int elementIndex;
   private VertexFormat format;
   private VertexFormat.Mode mode;
   private boolean fastFormat;
   private boolean fullFormat;
   private boolean building;
   @Nullable
   private Vector3f[] sortingPoints;
   @Nullable
   private VertexSorting sorting;
   private boolean indexOnly;

   public BufferBuilder(int i) {
      this.buffer = MemoryTracker.create(i * 6);
   }

   private void ensureVertexCapacity() {
      this.ensureCapacity(this.format.getVertexSize());
   }

   private void ensureCapacity(int i) {
      if (this.nextElementByte + i > this.buffer.capacity()) {
         int j = this.buffer.capacity();
         int k = j + roundUp(i);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", j, k);
         ByteBuffer bytebuffer = MemoryTracker.resize(this.buffer, k);
         bytebuffer.rewind();
         this.buffer = bytebuffer;
      }
   }

   private static int roundUp(int i) {
      int j = 2097152;
      if (i == 0) {
         return j;
      } else {
         if (i < 0) {
            j *= -1;
         }

         int k = i % j;
         return k == 0 ? i : i + j - k;
      }
   }

   public void setQuadSorting(VertexSorting vertexsorting) {
      if (this.mode == VertexFormat.Mode.QUADS) {
         this.sorting = vertexsorting;
         if (this.sortingPoints == null) {
            this.sortingPoints = this.makeQuadSortingPoints();
         }

      }
   }

   public BufferBuilder.SortState getSortState() {
      return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sorting);
   }

   public void restoreSortState(BufferBuilder.SortState bufferbuilder_sortstate) {
      this.buffer.rewind();
      this.mode = bufferbuilder_sortstate.mode;
      this.vertices = bufferbuilder_sortstate.vertices;
      this.nextElementByte = this.renderedBufferPointer;
      this.sortingPoints = bufferbuilder_sortstate.sortingPoints;
      this.sorting = bufferbuilder_sortstate.sorting;
      this.indexOnly = true;
   }

   public void begin(VertexFormat.Mode vertexformat_mode, VertexFormat vertexformat) {
      if (this.building) {
         throw new IllegalStateException("Already building!");
      } else {
         this.building = true;
         this.mode = vertexformat_mode;
         this.switchFormat(vertexformat);
         this.currentElement = vertexformat.getElements().get(0);
         this.elementIndex = 0;
         this.buffer.rewind();
      }
   }

   private void switchFormat(VertexFormat vertexformat) {
      if (this.format != vertexformat) {
         this.format = vertexformat;
         boolean flag = vertexformat == DefaultVertexFormat.NEW_ENTITY;
         boolean flag1 = vertexformat == DefaultVertexFormat.BLOCK;
         this.fastFormat = flag || flag1;
         this.fullFormat = flag;
      }
   }

   private IntConsumer intConsumer(int i, VertexFormat.IndexType vertexformat_indextype) {
      MutableInt mutableint = new MutableInt(i);
      IntConsumer var10000;
      switch (vertexformat_indextype) {
         case SHORT:
            var10000 = (k) -> this.buffer.putShort(mutableint.getAndAdd(2), (short)k);
            break;
         case INT:
            var10000 = (j) -> this.buffer.putInt(mutableint.getAndAdd(4), j);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   private Vector3f[] makeQuadSortingPoints() {
      FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
      int i = this.renderedBufferPointer / 4;
      int j = this.format.getIntegerSize();
      int k = j * this.mode.primitiveStride;
      int l = this.vertices / this.mode.primitiveStride;
      Vector3f[] avector3f = new Vector3f[l];

      for(int i1 = 0; i1 < l; ++i1) {
         float f = floatbuffer.get(i + i1 * k + 0);
         float f1 = floatbuffer.get(i + i1 * k + 1);
         float f2 = floatbuffer.get(i + i1 * k + 2);
         float f3 = floatbuffer.get(i + i1 * k + j * 2 + 0);
         float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
         float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
         float f6 = (f + f3) / 2.0F;
         float f7 = (f1 + f4) / 2.0F;
         float f8 = (f2 + f5) / 2.0F;
         avector3f[i1] = new Vector3f(f6, f7, f8);
      }

      return avector3f;
   }

   private void putSortedQuadIndices(VertexFormat.IndexType vertexformat_indextype) {
      if (this.sortingPoints != null && this.sorting != null) {
         int[] aint = this.sorting.sort(this.sortingPoints);
         IntConsumer intconsumer = this.intConsumer(this.nextElementByte, vertexformat_indextype);

         for(int i : aint) {
            intconsumer.accept(i * this.mode.primitiveStride + 0);
            intconsumer.accept(i * this.mode.primitiveStride + 1);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 2);
            intconsumer.accept(i * this.mode.primitiveStride + 3);
            intconsumer.accept(i * this.mode.primitiveStride + 0);
         }

      } else {
         throw new IllegalStateException("Sorting state uninitialized");
      }
   }

   public boolean isCurrentBatchEmpty() {
      return this.vertices == 0;
   }

   @Nullable
   public BufferBuilder.RenderedBuffer endOrDiscardIfEmpty() {
      this.ensureDrawing();
      if (this.isCurrentBatchEmpty()) {
         this.reset();
         return null;
      } else {
         BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = this.storeRenderedBuffer();
         this.reset();
         return bufferbuilder_renderedbuffer;
      }
   }

   public BufferBuilder.RenderedBuffer end() {
      this.ensureDrawing();
      BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = this.storeRenderedBuffer();
      this.reset();
      return bufferbuilder_renderedbuffer;
   }

   private void ensureDrawing() {
      if (!this.building) {
         throw new IllegalStateException("Not building!");
      }
   }

   private BufferBuilder.RenderedBuffer storeRenderedBuffer() {
      int i = this.mode.indexCount(this.vertices);
      int j = !this.indexOnly ? this.vertices * this.format.getVertexSize() : 0;
      VertexFormat.IndexType vertexformat_indextype = VertexFormat.IndexType.least(i);
      boolean flag;
      int l;
      if (this.sortingPoints != null) {
         int k = Mth.roundToward(i * vertexformat_indextype.bytes, 4);
         this.ensureCapacity(k);
         this.putSortedQuadIndices(vertexformat_indextype);
         flag = false;
         this.nextElementByte += k;
         l = j + k;
      } else {
         flag = true;
         l = j;
      }

      int j1 = this.renderedBufferPointer;
      this.renderedBufferPointer += l;
      ++this.renderedBufferCount;
      BufferBuilder.DrawState bufferbuilder_drawstate = new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, vertexformat_indextype, this.indexOnly, flag);
      return new BufferBuilder.RenderedBuffer(j1, bufferbuilder_drawstate);
   }

   private void reset() {
      this.building = false;
      this.vertices = 0;
      this.currentElement = null;
      this.elementIndex = 0;
      this.sortingPoints = null;
      this.sorting = null;
      this.indexOnly = false;
   }

   public void putByte(int i, byte b0) {
      this.buffer.put(this.nextElementByte + i, b0);
   }

   public void putShort(int i, short short0) {
      this.buffer.putShort(this.nextElementByte + i, short0);
   }

   public void putFloat(int i, float f) {
      this.buffer.putFloat(this.nextElementByte + i, f);
   }

   public void endVertex() {
      if (this.elementIndex != 0) {
         throw new IllegalStateException("Not filled all elements of the vertex");
      } else {
         ++this.vertices;
         this.ensureVertexCapacity();
         if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int i = this.format.getVertexSize();
            this.buffer.put(this.nextElementByte, this.buffer, this.nextElementByte - i, i);
            this.nextElementByte += i;
            ++this.vertices;
            this.ensureVertexCapacity();
         }

      }
   }

   public void nextElement() {
      ImmutableList<VertexFormatElement> immutablelist = this.format.getElements();
      this.elementIndex = (this.elementIndex + 1) % immutablelist.size();
      this.nextElementByte += this.currentElement.getByteSize();
      VertexFormatElement vertexformatelement = immutablelist.get(this.elementIndex);
      this.currentElement = vertexformatelement;
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
         this.nextElement();
      }

      if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
         BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
      }

   }

   public VertexConsumer color(int i, int j, int k, int l) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else {
         return BufferVertexConsumer.super.color(i, j, k, l);
      }
   }

   public void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else if (this.fastFormat) {
         this.putFloat(0, f);
         this.putFloat(4, f1);
         this.putFloat(8, f2);
         this.putByte(12, (byte)((int)(f3 * 255.0F)));
         this.putByte(13, (byte)((int)(f4 * 255.0F)));
         this.putByte(14, (byte)((int)(f5 * 255.0F)));
         this.putByte(15, (byte)((int)(f6 * 255.0F)));
         this.putFloat(16, f7);
         this.putFloat(20, f8);
         int k;
         if (this.fullFormat) {
            this.putShort(24, (short)(i & '\uffff'));
            this.putShort(26, (short)(i >> 16 & '\uffff'));
            k = 28;
         } else {
            k = 24;
         }

         this.putShort(k + 0, (short)(j & '\uffff'));
         this.putShort(k + 2, (short)(j >> 16 & '\uffff'));
         this.putByte(k + 4, BufferVertexConsumer.normalIntValue(f9));
         this.putByte(k + 5, BufferVertexConsumer.normalIntValue(f10));
         this.putByte(k + 6, BufferVertexConsumer.normalIntValue(f11));
         this.nextElementByte += k + 8;
         this.endVertex();
      } else {
         super.vertex(f, f1, f2, f3, f4, f5, f6, f7, f8, i, j, f9, f10, f11);
      }
   }

   void releaseRenderedBuffer() {
      if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
         this.clear();
      }

   }

   public void clear() {
      if (this.renderedBufferCount > 0) {
         LOGGER.warn("Clearing BufferBuilder with unused batches");
      }

      this.discard();
   }

   public void discard() {
      this.renderedBufferCount = 0;
      this.renderedBufferPointer = 0;
      this.nextElementByte = 0;
   }

   public VertexFormatElement currentElement() {
      if (this.currentElement == null) {
         throw new IllegalStateException("BufferBuilder not started");
      } else {
         return this.currentElement;
      }
   }

   public boolean building() {
      return this.building;
   }

   ByteBuffer bufferSlice(int i, int j) {
      return MemoryUtil.memSlice(this.buffer, i, j - i);
   }

   public static record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType, boolean indexOnly, boolean sequentialIndex) {
      final int vertexCount;

      public int vertexBufferSize() {
         return this.vertexCount * this.format.getVertexSize();
      }

      public int vertexBufferStart() {
         return 0;
      }

      public int vertexBufferEnd() {
         return this.vertexBufferSize();
      }

      public int indexBufferStart() {
         return this.indexOnly ? 0 : this.vertexBufferEnd();
      }

      public int indexBufferEnd() {
         return this.indexBufferStart() + this.indexBufferSize();
      }

      private int indexBufferSize() {
         return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
      }

      public int bufferSize() {
         return this.indexBufferEnd();
      }
   }

   public class RenderedBuffer {
      private final int pointer;
      private final BufferBuilder.DrawState drawState;
      private boolean released;

      RenderedBuffer(int i, BufferBuilder.DrawState bufferbuilder_drawstate) {
         this.pointer = i;
         this.drawState = bufferbuilder_drawstate;
      }

      public ByteBuffer vertexBuffer() {
         int i = this.pointer + this.drawState.vertexBufferStart();
         int j = this.pointer + this.drawState.vertexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public ByteBuffer indexBuffer() {
         int i = this.pointer + this.drawState.indexBufferStart();
         int j = this.pointer + this.drawState.indexBufferEnd();
         return BufferBuilder.this.bufferSlice(i, j);
      }

      public BufferBuilder.DrawState drawState() {
         return this.drawState;
      }

      public boolean isEmpty() {
         return this.drawState.vertexCount == 0;
      }

      public void release() {
         if (this.released) {
            throw new IllegalStateException("Buffer has already been released!");
         } else {
            BufferBuilder.this.releaseRenderedBuffer();
            this.released = true;
         }
      }
   }

   public static class SortState {
      final VertexFormat.Mode mode;
      final int vertices;
      @Nullable
      final Vector3f[] sortingPoints;
      @Nullable
      final VertexSorting sorting;

      SortState(VertexFormat.Mode vertexformat_mode, int i, @Nullable Vector3f[] avector3f, @Nullable VertexSorting vertexsorting) {
         this.mode = vertexformat_mode;
         this.vertices = i;
         this.sortingPoints = avector3f;
         this.sorting = vertexsorting;
      }
   }
}
