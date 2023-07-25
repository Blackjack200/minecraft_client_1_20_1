package net.minecraft.client.model.geom;

public class PartPose {
   public static final PartPose ZERO = offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   public final float x;
   public final float y;
   public final float z;
   public final float xRot;
   public final float yRot;
   public final float zRot;

   private PartPose(float f, float f1, float f2, float f3, float f4, float f5) {
      this.x = f;
      this.y = f1;
      this.z = f2;
      this.xRot = f3;
      this.yRot = f4;
      this.zRot = f5;
   }

   public static PartPose offset(float f, float f1, float f2) {
      return offsetAndRotation(f, f1, f2, 0.0F, 0.0F, 0.0F);
   }

   public static PartPose rotation(float f, float f1, float f2) {
      return offsetAndRotation(0.0F, 0.0F, 0.0F, f, f1, f2);
   }

   public static PartPose offsetAndRotation(float f, float f1, float f2, float f3, float f4, float f5) {
      return new PartPose(f, f1, f2, f3, f4, f5);
   }
}
