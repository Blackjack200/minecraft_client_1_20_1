package com.mojang.blaze3d.vertex;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

public interface VertexConsumer {
   VertexConsumer vertex(double d0, double d1, double d2);

   VertexConsumer color(int i, int j, int k, int l);

   VertexConsumer uv(float f, float f1);

   VertexConsumer overlayCoords(int i, int j);

   VertexConsumer uv2(int i, int j);

   VertexConsumer normal(float f, float f1, float f2);

   void endVertex();

   default void vertex(float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int i, int j, float f9, float f10, float f11) {
      this.vertex((double)f, (double)f1, (double)f2);
      this.color(f3, f4, f5, f6);
      this.uv(f7, f8);
      this.overlayCoords(i);
      this.uv2(j);
      this.normal(f9, f10, f11);
      this.endVertex();
   }

   void defaultColor(int i, int j, int k, int l);

   void unsetDefaultColor();

   default VertexConsumer color(float f, float f1, float f2, float f3) {
      return this.color((int)(f * 255.0F), (int)(f1 * 255.0F), (int)(f2 * 255.0F), (int)(f3 * 255.0F));
   }

   default VertexConsumer color(int i) {
      return this.color(FastColor.ARGB32.red(i), FastColor.ARGB32.green(i), FastColor.ARGB32.blue(i), FastColor.ARGB32.alpha(i));
   }

   default VertexConsumer uv2(int i) {
      return this.uv2(i & '\uffff', i >> 16 & '\uffff');
   }

   default VertexConsumer overlayCoords(int i) {
      return this.overlayCoords(i & '\uffff', i >> 16 & '\uffff');
   }

   default void putBulkData(PoseStack.Pose posestack_pose, BakedQuad bakedquad, float f, float f1, float f2, int i, int j) {
      this.putBulkData(posestack_pose, bakedquad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, f, f1, f2, new int[]{i, i, i, i}, j, false);
   }

   default void putBulkData(PoseStack.Pose posestack_pose, BakedQuad bakedquad, float[] afloat, float f, float f1, float f2, int[] aint, int i, boolean flag) {
      float[] afloat1 = new float[]{afloat[0], afloat[1], afloat[2], afloat[3]};
      int[] aint1 = new int[]{aint[0], aint[1], aint[2], aint[3]};
      int[] aint2 = bakedquad.getVertices();
      Vec3i vec3i = bakedquad.getDirection().getNormal();
      Matrix4f matrix4f = posestack_pose.pose();
      Vector3f vector3f = posestack_pose.normal().transform(new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ()));
      int j = 8;
      int k = aint2.length / 8;
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
         IntBuffer intbuffer = bytebuffer.asIntBuffer();

         for(int l = 0; l < k; ++l) {
            intbuffer.clear();
            intbuffer.put(aint2, l * 8, 8);
            float f3 = bytebuffer.getFloat(0);
            float f4 = bytebuffer.getFloat(4);
            float f5 = bytebuffer.getFloat(8);
            float f9;
            float f10;
            float f11;
            if (flag) {
               float f6 = (float)(bytebuffer.get(12) & 255) / 255.0F;
               float f7 = (float)(bytebuffer.get(13) & 255) / 255.0F;
               float f8 = (float)(bytebuffer.get(14) & 255) / 255.0F;
               f9 = f6 * afloat1[l] * f;
               f10 = f7 * afloat1[l] * f1;
               f11 = f8 * afloat1[l] * f2;
            } else {
               f9 = afloat1[l] * f;
               f10 = afloat1[l] * f1;
               f11 = afloat1[l] * f2;
            }

            int i1 = aint1[l];
            float f15 = bytebuffer.getFloat(16);
            float f16 = bytebuffer.getFloat(20);
            Vector4f vector4f = matrix4f.transform(new Vector4f(f3, f4, f5, 1.0F));
            this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f9, f10, f11, 1.0F, f15, f16, i, i1, vector3f.x(), vector3f.y(), vector3f.z());
         }
      } catch (Throwable var33) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var32) {
               var33.addSuppressed(var32);
            }
         }

         throw var33;
      }

      if (memorystack != null) {
         memorystack.close();
      }

   }

   default VertexConsumer vertex(Matrix4f matrix4f, float f, float f1, float f2) {
      Vector4f vector4f = matrix4f.transform(new Vector4f(f, f1, f2, 1.0F));
      return this.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
   }

   default VertexConsumer normal(Matrix3f matrix3f, float f, float f1, float f2) {
      Vector3f vector3f = matrix3f.transform(new Vector3f(f, f1, f2));
      return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
   }
}
