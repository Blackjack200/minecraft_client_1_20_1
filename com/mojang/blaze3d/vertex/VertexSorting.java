package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import org.joml.Vector3f;

public interface VertexSorting {
   VertexSorting DISTANCE_TO_ORIGIN = byDistance(0.0F, 0.0F, 0.0F);
   VertexSorting ORTHOGRAPHIC_Z = byDistance((vector3f) -> -vector3f.z());

   static VertexSorting byDistance(float f, float f1, float f2) {
      return byDistance(new Vector3f(f, f1, f2));
   }

   static VertexSorting byDistance(Vector3f vector3f) {
      return byDistance(vector3f::distanceSquared);
   }

   static VertexSorting byDistance(VertexSorting.DistanceFunction vertexsorting_distancefunction) {
      return (avector3f) -> {
         float[] afloat = new float[avector3f.length];
         int[] aint = new int[avector3f.length];

         for(int i = 0; i < avector3f.length; aint[i] = i++) {
            afloat[i] = vertexsorting_distancefunction.apply(avector3f[i]);
         }

         IntArrays.mergeSort(aint, (j, k) -> Floats.compare(afloat[k], afloat[j]));
         return aint;
      };
   }

   int[] sort(Vector3f[] avector3f);

   public interface DistanceFunction {
      float apply(Vector3f vector3f);
   }
}
