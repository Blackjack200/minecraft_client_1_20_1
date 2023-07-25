package com.mojang.math;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
   private static final float G = 3.0F + 2.0F * Math.sqrt(2.0F);
   private static final GivensParameters PI_4 = GivensParameters.fromPositiveAngle(((float)java.lang.Math.PI / 4F));

   private MatrixUtil() {
   }

   public static Matrix4f mulComponentWise(Matrix4f matrix4f, float f) {
      return matrix4f.set(matrix4f.m00() * f, matrix4f.m01() * f, matrix4f.m02() * f, matrix4f.m03() * f, matrix4f.m10() * f, matrix4f.m11() * f, matrix4f.m12() * f, matrix4f.m13() * f, matrix4f.m20() * f, matrix4f.m21() * f, matrix4f.m22() * f, matrix4f.m23() * f, matrix4f.m30() * f, matrix4f.m31() * f, matrix4f.m32() * f, matrix4f.m33() * f);
   }

   private static GivensParameters approxGivensQuat(float f, float f1, float f2) {
      float f3 = 2.0F * (f - f2);
      return G * f1 * f1 < f3 * f3 ? GivensParameters.fromUnnormalized(f1, f3) : PI_4;
   }

   private static GivensParameters qrGivensQuat(float f, float f1) {
      float f2 = (float)java.lang.Math.hypot((double)f, (double)f1);
      float f3 = f2 > 1.0E-6F ? f1 : 0.0F;
      float f4 = Math.abs(f) + Math.max(f2, 1.0E-6F);
      if (f < 0.0F) {
         float f5 = f3;
         f3 = f4;
         f4 = f5;
      }

      return GivensParameters.fromUnnormalized(f3, f4);
   }

   private static void similarityTransform(Matrix3f matrix3f, Matrix3f matrix3f1) {
      matrix3f.mul(matrix3f1);
      matrix3f1.transpose();
      matrix3f1.mul(matrix3f);
      matrix3f.set((Matrix3fc)matrix3f1);
   }

   private static void stepJacobi(Matrix3f matrix3f, Matrix3f matrix3f1, Quaternionf quaternionf, Quaternionf quaternionf1) {
      if (matrix3f.m01 * matrix3f.m01 + matrix3f.m10 * matrix3f.m10 > 1.0E-6F) {
         GivensParameters givensparameters = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m01 + matrix3f.m10), matrix3f.m11);
         Quaternionf quaternionf2 = givensparameters.aroundZ(quaternionf);
         quaternionf1.mul(quaternionf2);
         givensparameters.aroundZ(matrix3f1);
         similarityTransform(matrix3f, matrix3f1);
      }

      if (matrix3f.m02 * matrix3f.m02 + matrix3f.m20 * matrix3f.m20 > 1.0E-6F) {
         GivensParameters givensparameters1 = approxGivensQuat(matrix3f.m00, 0.5F * (matrix3f.m02 + matrix3f.m20), matrix3f.m22).inverse();
         Quaternionf quaternionf3 = givensparameters1.aroundY(quaternionf);
         quaternionf1.mul(quaternionf3);
         givensparameters1.aroundY(matrix3f1);
         similarityTransform(matrix3f, matrix3f1);
      }

      if (matrix3f.m12 * matrix3f.m12 + matrix3f.m21 * matrix3f.m21 > 1.0E-6F) {
         GivensParameters givensparameters2 = approxGivensQuat(matrix3f.m11, 0.5F * (matrix3f.m12 + matrix3f.m21), matrix3f.m22);
         Quaternionf quaternionf4 = givensparameters2.aroundX(quaternionf);
         quaternionf1.mul(quaternionf4);
         givensparameters2.aroundX(matrix3f1);
         similarityTransform(matrix3f, matrix3f1);
      }

   }

   public static Quaternionf eigenvalueJacobi(Matrix3f matrix3f, int i) {
      Quaternionf quaternionf = new Quaternionf();
      Matrix3f matrix3f1 = new Matrix3f();
      Quaternionf quaternionf1 = new Quaternionf();

      for(int j = 0; j < i; ++j) {
         stepJacobi(matrix3f, matrix3f1, quaternionf1, quaternionf);
      }

      quaternionf.normalize();
      return quaternionf;
   }

   public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f matrix3f) {
      Matrix3f matrix3f1 = new Matrix3f(matrix3f);
      matrix3f1.transpose();
      matrix3f1.mul(matrix3f);
      Quaternionf quaternionf = eigenvalueJacobi(matrix3f1, 5);
      float f = matrix3f1.m00;
      float f1 = matrix3f1.m11;
      boolean flag = (double)f < 1.0E-6D;
      boolean flag1 = (double)f1 < 1.0E-6D;
      Matrix3f matrix3f3 = matrix3f.rotate(quaternionf);
      Quaternionf quaternionf1 = new Quaternionf();
      Quaternionf quaternionf2 = new Quaternionf();
      GivensParameters givensparameters;
      if (flag) {
         givensparameters = qrGivensQuat(matrix3f3.m11, -matrix3f3.m10);
      } else {
         givensparameters = qrGivensQuat(matrix3f3.m00, matrix3f3.m01);
      }

      Quaternionf quaternionf3 = givensparameters.aroundZ(quaternionf2);
      Matrix3f matrix3f4 = givensparameters.aroundZ(matrix3f1);
      quaternionf1.mul(quaternionf3);
      matrix3f4.transpose().mul(matrix3f3);
      if (flag) {
         givensparameters = qrGivensQuat(matrix3f4.m22, -matrix3f4.m20);
      } else {
         givensparameters = qrGivensQuat(matrix3f4.m00, matrix3f4.m02);
      }

      givensparameters = givensparameters.inverse();
      Quaternionf quaternionf4 = givensparameters.aroundY(quaternionf2);
      Matrix3f matrix3f5 = givensparameters.aroundY(matrix3f3);
      quaternionf1.mul(quaternionf4);
      matrix3f5.transpose().mul(matrix3f4);
      if (flag1) {
         givensparameters = qrGivensQuat(matrix3f5.m22, -matrix3f5.m21);
      } else {
         givensparameters = qrGivensQuat(matrix3f5.m11, matrix3f5.m12);
      }

      Quaternionf quaternionf5 = givensparameters.aroundX(quaternionf2);
      Matrix3f matrix3f6 = givensparameters.aroundX(matrix3f4);
      quaternionf1.mul(quaternionf5);
      matrix3f6.transpose().mul(matrix3f5);
      Vector3f vector3f = new Vector3f(matrix3f6.m00, matrix3f6.m11, matrix3f6.m22);
      return Triple.of(quaternionf1, vector3f, quaternionf.conjugate());
   }
}
