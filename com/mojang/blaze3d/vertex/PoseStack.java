package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class PoseStack {
   private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), (arraydeque) -> {
      Matrix4f matrix4f = new Matrix4f();
      Matrix3f matrix3f = new Matrix3f();
      arraydeque.add(new PoseStack.Pose(matrix4f, matrix3f));
   });

   public void translate(double d0, double d1, double d2) {
      this.translate((float)d0, (float)d1, (float)d2);
   }

   public void translate(float f, float f1, float f2) {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      posestack_pose.pose.translate(f, f1, f2);
   }

   public void scale(float f, float f1, float f2) {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      posestack_pose.pose.scale(f, f1, f2);
      if (f == f1 && f1 == f2) {
         if (f > 0.0F) {
            return;
         }

         posestack_pose.normal.scale(-1.0F);
      }

      float f3 = 1.0F / f;
      float f4 = 1.0F / f1;
      float f5 = 1.0F / f2;
      float f6 = Mth.fastInvCubeRoot(f3 * f4 * f5);
      posestack_pose.normal.scale(f6 * f3, f6 * f4, f6 * f5);
   }

   public void mulPose(Quaternionf quaternionf) {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      posestack_pose.pose.rotate(quaternionf);
      posestack_pose.normal.rotate(quaternionf);
   }

   public void rotateAround(Quaternionf quaternionf, float f, float f1, float f2) {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      posestack_pose.pose.rotateAround(quaternionf, f, f1, f2);
      posestack_pose.normal.rotate(quaternionf);
   }

   public void pushPose() {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      this.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack_pose.pose), new Matrix3f(posestack_pose.normal)));
   }

   public void popPose() {
      this.poseStack.removeLast();
   }

   public PoseStack.Pose last() {
      return this.poseStack.getLast();
   }

   public boolean clear() {
      return this.poseStack.size() == 1;
   }

   public void setIdentity() {
      PoseStack.Pose posestack_pose = this.poseStack.getLast();
      posestack_pose.pose.identity();
      posestack_pose.normal.identity();
   }

   public void mulPoseMatrix(Matrix4f matrix4f) {
      (this.poseStack.getLast()).pose.mul(matrix4f);
   }

   public static final class Pose {
      final Matrix4f pose;
      final Matrix3f normal;

      Pose(Matrix4f matrix4f, Matrix3f matrix3f) {
         this.pose = matrix4f;
         this.normal = matrix3f;
      }

      public Matrix4f pose() {
         return this.pose;
      }

      public Matrix3f normal() {
         return this.normal;
      }
   }
}
