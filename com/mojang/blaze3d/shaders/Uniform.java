package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class Uniform extends AbstractUniform implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int UT_INT1 = 0;
   public static final int UT_INT2 = 1;
   public static final int UT_INT3 = 2;
   public static final int UT_INT4 = 3;
   public static final int UT_FLOAT1 = 4;
   public static final int UT_FLOAT2 = 5;
   public static final int UT_FLOAT3 = 6;
   public static final int UT_FLOAT4 = 7;
   public static final int UT_MAT2 = 8;
   public static final int UT_MAT3 = 9;
   public static final int UT_MAT4 = 10;
   private static final boolean TRANSPOSE_MATRICIES = false;
   private int location;
   private final int count;
   private final int type;
   private final IntBuffer intValues;
   private final FloatBuffer floatValues;
   private final String name;
   private boolean dirty;
   private final Shader parent;

   public Uniform(String s, int i, int j, Shader shader) {
      this.name = s;
      this.count = j;
      this.type = i;
      this.parent = shader;
      if (i <= 3) {
         this.intValues = MemoryUtil.memAllocInt(j);
         this.floatValues = null;
      } else {
         this.intValues = null;
         this.floatValues = MemoryUtil.memAllocFloat(j);
      }

      this.location = -1;
      this.markDirty();
   }

   public static int glGetUniformLocation(int i, CharSequence charsequence) {
      return GlStateManager._glGetUniformLocation(i, charsequence);
   }

   public static void uploadInteger(int i, int j) {
      RenderSystem.glUniform1i(i, j);
   }

   public static int glGetAttribLocation(int i, CharSequence charsequence) {
      return GlStateManager._glGetAttribLocation(i, charsequence);
   }

   public static void glBindAttribLocation(int i, int j, CharSequence charsequence) {
      GlStateManager._glBindAttribLocation(i, j, charsequence);
   }

   public void close() {
      if (this.intValues != null) {
         MemoryUtil.memFree(this.intValues);
      }

      if (this.floatValues != null) {
         MemoryUtil.memFree(this.floatValues);
      }

   }

   private void markDirty() {
      this.dirty = true;
      if (this.parent != null) {
         this.parent.markDirty();
      }

   }

   public static int getTypeFromString(String s) {
      int i = -1;
      if ("int".equals(s)) {
         i = 0;
      } else if ("float".equals(s)) {
         i = 4;
      } else if (s.startsWith("matrix")) {
         if (s.endsWith("2x2")) {
            i = 8;
         } else if (s.endsWith("3x3")) {
            i = 9;
         } else if (s.endsWith("4x4")) {
            i = 10;
         }
      }

      return i;
   }

   public void setLocation(int i) {
      this.location = i;
   }

   public String getName() {
      return this.name;
   }

   public final void set(float f) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.markDirty();
   }

   public final void set(float f, float f1) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.markDirty();
   }

   public final void set(int i, float f) {
      this.floatValues.position(0);
      this.floatValues.put(i, f);
      this.markDirty();
   }

   public final void set(float f, float f1, float f2) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.markDirty();
   }

   public final void set(Vector3f vector3f) {
      this.floatValues.position(0);
      vector3f.get(this.floatValues);
      this.markDirty();
   }

   public final void set(float f, float f1, float f2, float f3) {
      this.floatValues.position(0);
      this.floatValues.put(f);
      this.floatValues.put(f1);
      this.floatValues.put(f2);
      this.floatValues.put(f3);
      this.floatValues.flip();
      this.markDirty();
   }

   public final void set(Vector4f vector4f) {
      this.floatValues.position(0);
      vector4f.get(this.floatValues);
      this.markDirty();
   }

   public final void setSafe(float f, float f1, float f2, float f3) {
      this.floatValues.position(0);
      if (this.type >= 4) {
         this.floatValues.put(0, f);
      }

      if (this.type >= 5) {
         this.floatValues.put(1, f1);
      }

      if (this.type >= 6) {
         this.floatValues.put(2, f2);
      }

      if (this.type >= 7) {
         this.floatValues.put(3, f3);
      }

      this.markDirty();
   }

   public final void setSafe(int i, int j, int k, int l) {
      this.intValues.position(0);
      if (this.type >= 0) {
         this.intValues.put(0, i);
      }

      if (this.type >= 1) {
         this.intValues.put(1, j);
      }

      if (this.type >= 2) {
         this.intValues.put(2, k);
      }

      if (this.type >= 3) {
         this.intValues.put(3, l);
      }

      this.markDirty();
   }

   public final void set(int i) {
      this.intValues.position(0);
      this.intValues.put(0, i);
      this.markDirty();
   }

   public final void set(int i, int j) {
      this.intValues.position(0);
      this.intValues.put(0, i);
      this.intValues.put(1, j);
      this.markDirty();
   }

   public final void set(int i, int j, int k) {
      this.intValues.position(0);
      this.intValues.put(0, i);
      this.intValues.put(1, j);
      this.intValues.put(2, k);
      this.markDirty();
   }

   public final void set(int i, int j, int k, int l) {
      this.intValues.position(0);
      this.intValues.put(0, i);
      this.intValues.put(1, j);
      this.intValues.put(2, k);
      this.intValues.put(3, l);
      this.markDirty();
   }

   public final void set(float[] afloat) {
      if (afloat.length < this.count) {
         LOGGER.warn("Uniform.set called with a too-small value array (expected {}, got {}). Ignoring.", this.count, afloat.length);
      } else {
         this.floatValues.position(0);
         this.floatValues.put(afloat);
         this.floatValues.position(0);
         this.markDirty();
      }
   }

   public final void setMat2x2(float f, float f1, float f2, float f3) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.markDirty();
   }

   public final void setMat2x3(float f, float f1, float f2, float f3, float f4, float f5) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.markDirty();
   }

   public final void setMat2x4(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.markDirty();
   }

   public final void setMat3x2(float f, float f1, float f2, float f3, float f4, float f5) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.markDirty();
   }

   public final void setMat3x3(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.floatValues.put(8, f8);
      this.markDirty();
   }

   public final void setMat3x4(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.floatValues.put(8, f8);
      this.floatValues.put(9, f9);
      this.floatValues.put(10, f10);
      this.floatValues.put(11, f11);
      this.markDirty();
   }

   public final void setMat4x2(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.markDirty();
   }

   public final void setMat4x3(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.floatValues.put(8, f8);
      this.floatValues.put(9, f9);
      this.floatValues.put(10, f10);
      this.floatValues.put(11, f11);
      this.markDirty();
   }

   public final void setMat4x4(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
      this.floatValues.position(0);
      this.floatValues.put(0, f);
      this.floatValues.put(1, f1);
      this.floatValues.put(2, f2);
      this.floatValues.put(3, f3);
      this.floatValues.put(4, f4);
      this.floatValues.put(5, f5);
      this.floatValues.put(6, f6);
      this.floatValues.put(7, f7);
      this.floatValues.put(8, f8);
      this.floatValues.put(9, f9);
      this.floatValues.put(10, f10);
      this.floatValues.put(11, f11);
      this.floatValues.put(12, f12);
      this.floatValues.put(13, f13);
      this.floatValues.put(14, f14);
      this.floatValues.put(15, f15);
      this.markDirty();
   }

   public final void set(Matrix4f matrix4f) {
      this.floatValues.position(0);
      matrix4f.get(this.floatValues);
      this.markDirty();
   }

   public final void set(Matrix3f matrix3f) {
      this.floatValues.position(0);
      matrix3f.get(this.floatValues);
      this.markDirty();
   }

   public void upload() {
      if (!this.dirty) {
      }

      this.dirty = false;
      if (this.type <= 3) {
         this.uploadAsInteger();
      } else if (this.type <= 7) {
         this.uploadAsFloat();
      } else {
         if (this.type > 10) {
            LOGGER.warn("Uniform.upload called, but type value ({}) is not a valid type. Ignoring.", (int)this.type);
            return;
         }

         this.uploadAsMatrix();
      }

   }

   private void uploadAsInteger() {
      this.intValues.rewind();
      switch (this.type) {
         case 0:
            RenderSystem.glUniform1(this.location, this.intValues);
            break;
         case 1:
            RenderSystem.glUniform2(this.location, this.intValues);
            break;
         case 2:
            RenderSystem.glUniform3(this.location, this.intValues);
            break;
         case 3:
            RenderSystem.glUniform4(this.location, this.intValues);
            break;
         default:
            LOGGER.warn("Uniform.upload called, but count value ({}) is  not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsFloat() {
      this.floatValues.rewind();
      switch (this.type) {
         case 4:
            RenderSystem.glUniform1(this.location, this.floatValues);
            break;
         case 5:
            RenderSystem.glUniform2(this.location, this.floatValues);
            break;
         case 6:
            RenderSystem.glUniform3(this.location, this.floatValues);
            break;
         case 7:
            RenderSystem.glUniform4(this.location, this.floatValues);
            break;
         default:
            LOGGER.warn("Uniform.upload called, but count value ({}) is not in the range of 1 to 4. Ignoring.", (int)this.count);
      }

   }

   private void uploadAsMatrix() {
      this.floatValues.clear();
      switch (this.type) {
         case 8:
            RenderSystem.glUniformMatrix2(this.location, false, this.floatValues);
            break;
         case 9:
            RenderSystem.glUniformMatrix3(this.location, false, this.floatValues);
            break;
         case 10:
            RenderSystem.glUniformMatrix4(this.location, false, this.floatValues);
      }

   }

   public int getLocation() {
      return this.location;
   }

   public int getCount() {
      return this.count;
   }

   public int getType() {
      return this.type;
   }

   public IntBuffer getIntBuffer() {
      return this.intValues;
   }

   public FloatBuffer getFloatBuffer() {
      return this.floatValues;
   }
}
