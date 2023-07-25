package net.minecraft.client.animation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class KeyframeAnimations {
   public static void animate(HierarchicalModel<?> hierarchicalmodel, AnimationDefinition animationdefinition, long i, float f, Vector3f vector3f) {
      float f1 = getElapsedSeconds(animationdefinition, i);

      for(Map.Entry<String, List<AnimationChannel>> map_entry : animationdefinition.boneAnimations().entrySet()) {
         Optional<ModelPart> optional = hierarchicalmodel.getAnyDescendantWithName(map_entry.getKey());
         List<AnimationChannel> list = map_entry.getValue();
         optional.ifPresent((modelpart) -> list.forEach((animationchannel) -> {
               Keyframe[] akeyframe = animationchannel.keyframes();
               int j = Math.max(0, Mth.binarySearch(0, akeyframe.length, (l) -> f1 <= akeyframe[l].timestamp()) - 1);
               int k = Math.min(akeyframe.length - 1, j + 1);
               Keyframe keyframe = akeyframe[j];
               Keyframe keyframe1 = akeyframe[k];
               float f6 = f1 - keyframe.timestamp();
               float f7;
               if (k != j) {
                  f7 = Mth.clamp(f6 / (keyframe1.timestamp() - keyframe.timestamp()), 0.0F, 1.0F);
               } else {
                  f7 = 0.0F;
               }

               keyframe1.interpolation().apply(vector3f, f7, akeyframe, j, k, f);
               animationchannel.target().apply(modelpart, vector3f);
            }));
      }

   }

   private static float getElapsedSeconds(AnimationDefinition animationdefinition, long i) {
      float f = (float)i / 1000.0F;
      return animationdefinition.looping() ? f % animationdefinition.lengthInSeconds() : f;
   }

   public static Vector3f posVec(float f, float f1, float f2) {
      return new Vector3f(f, -f1, f2);
   }

   public static Vector3f degreeVec(float f, float f1, float f2) {
      return new Vector3f(f * ((float)Math.PI / 180F), f1 * ((float)Math.PI / 180F), f2 * ((float)Math.PI / 180F));
   }

   public static Vector3f scaleVec(double d0, double d1, double d2) {
      return new Vector3f((float)(d0 - 1.0D), (float)(d1 - 1.0D), (float)(d2 - 1.0D));
   }
}
