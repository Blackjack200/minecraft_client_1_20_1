package net.minecraft.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public record AnimationChannel(AnimationChannel.Target target, Keyframe... keyframes) {
   public interface Interpolation {
      Vector3f apply(Vector3f vector3f, float f, Keyframe[] akeyframe, int i, int j, float f1);
   }

   public static class Interpolations {
      public static final AnimationChannel.Interpolation LINEAR = (vector3f, f, akeyframe, i, j, f1) -> {
         Vector3f vector3f1 = akeyframe[i].target();
         Vector3f vector3f2 = akeyframe[j].target();
         return vector3f1.lerp(vector3f2, f, vector3f).mul(f1);
      };
      public static final AnimationChannel.Interpolation CATMULLROM = (vector3f, f, akeyframe, i, j, f1) -> {
         Vector3f vector3f1 = akeyframe[Math.max(0, i - 1)].target();
         Vector3f vector3f2 = akeyframe[i].target();
         Vector3f vector3f3 = akeyframe[j].target();
         Vector3f vector3f4 = akeyframe[Math.min(akeyframe.length - 1, j + 1)].target();
         vector3f.set(Mth.catmullrom(f, vector3f1.x(), vector3f2.x(), vector3f3.x(), vector3f4.x()) * f1, Mth.catmullrom(f, vector3f1.y(), vector3f2.y(), vector3f3.y(), vector3f4.y()) * f1, Mth.catmullrom(f, vector3f1.z(), vector3f2.z(), vector3f3.z(), vector3f4.z()) * f1);
         return vector3f;
      };
   }

   public interface Target {
      void apply(ModelPart modelpart, Vector3f vector3f);
   }

   public static class Targets {
      public static final AnimationChannel.Target POSITION = ModelPart::offsetPos;
      public static final AnimationChannel.Target ROTATION = ModelPart::offsetRotation;
      public static final AnimationChannel.Target SCALE = ModelPart::offsetScale;
   }
}
