package net.minecraft.client.renderer.culling;

import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class Frustum {
   public static final int OFFSET_STEP = 4;
   private final FrustumIntersection intersection = new FrustumIntersection();
   private final Matrix4f matrix = new Matrix4f();
   private Vector4f viewVector;
   private double camX;
   private double camY;
   private double camZ;

   public Frustum(Matrix4f matrix4f, Matrix4f matrix4f1) {
      this.calculateFrustum(matrix4f, matrix4f1);
   }

   public Frustum(Frustum frustum) {
      this.intersection.set(frustum.matrix);
      this.matrix.set((Matrix4fc)frustum.matrix);
      this.camX = frustum.camX;
      this.camY = frustum.camY;
      this.camZ = frustum.camZ;
      this.viewVector = frustum.viewVector;
   }

   public Frustum offsetToFullyIncludeCameraCube(int i) {
      double d0 = Math.floor(this.camX / (double)i) * (double)i;
      double d1 = Math.floor(this.camY / (double)i) * (double)i;
      double d2 = Math.floor(this.camZ / (double)i) * (double)i;
      double d3 = Math.ceil(this.camX / (double)i) * (double)i;
      double d4 = Math.ceil(this.camY / (double)i) * (double)i;

      for(double d5 = Math.ceil(this.camZ / (double)i) * (double)i; this.intersection.intersectAab((float)(d0 - this.camX), (float)(d1 - this.camY), (float)(d2 - this.camZ), (float)(d3 - this.camX), (float)(d4 - this.camY), (float)(d5 - this.camZ)) != -2; this.camZ -= (double)(this.viewVector.z() * 4.0F)) {
         this.camX -= (double)(this.viewVector.x() * 4.0F);
         this.camY -= (double)(this.viewVector.y() * 4.0F);
      }

      return this;
   }

   public void prepare(double d0, double d1, double d2) {
      this.camX = d0;
      this.camY = d1;
      this.camZ = d2;
   }

   private void calculateFrustum(Matrix4f matrix4f, Matrix4f matrix4f1) {
      matrix4f1.mul(matrix4f, this.matrix);
      this.intersection.set(this.matrix);
      this.viewVector = this.matrix.transformTranspose(new Vector4f(0.0F, 0.0F, 1.0F, 0.0F));
   }

   public boolean isVisible(AABB aabb) {
      return this.cubeInFrustum(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
   }

   private boolean cubeInFrustum(double d0, double d1, double d2, double d3, double d4, double d5) {
      float f = (float)(d0 - this.camX);
      float f1 = (float)(d1 - this.camY);
      float f2 = (float)(d2 - this.camZ);
      float f3 = (float)(d3 - this.camX);
      float f4 = (float)(d4 - this.camY);
      float f5 = (float)(d5 - this.camZ);
      return this.intersection.testAab(f, f1, f2, f3, f4, f5);
   }
}
