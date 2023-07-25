package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;
import org.joml.Matrix3f;

public enum SymmetricGroup3 {
   P123(0, 1, 2),
   P213(1, 0, 2),
   P132(0, 2, 1),
   P231(1, 2, 0),
   P312(2, 0, 1),
   P321(2, 1, 0);

   private final int[] permutation;
   private final Matrix3f transformation;
   private static final int ORDER = 3;
   private static final SymmetricGroup3[][] cayleyTable = Util.make(new SymmetricGroup3[values().length][values().length], (asymmetricgroup3) -> {
      for(SymmetricGroup3 symmetricgroup3 : values()) {
         for(SymmetricGroup3 symmetricgroup31 : values()) {
            int[] aint = new int[3];

            for(int i = 0; i < 3; ++i) {
               aint[i] = symmetricgroup3.permutation[symmetricgroup31.permutation[i]];
            }

            SymmetricGroup3 symmetricgroup32 = Arrays.stream(values()).filter((symmetricgroup33) -> Arrays.equals(symmetricgroup33.permutation, aint)).findFirst().get();
            asymmetricgroup3[symmetricgroup3.ordinal()][symmetricgroup31.ordinal()] = symmetricgroup32;
         }
      }

   });

   private SymmetricGroup3(int i, int j, int k) {
      this.permutation = new int[]{i, j, k};
      this.transformation = new Matrix3f();
      this.transformation.set(this.permutation(0), 0, 1.0F);
      this.transformation.set(this.permutation(1), 1, 1.0F);
      this.transformation.set(this.permutation(2), 2, 1.0F);
   }

   public SymmetricGroup3 compose(SymmetricGroup3 symmetricgroup3) {
      return cayleyTable[this.ordinal()][symmetricgroup3.ordinal()];
   }

   public int permutation(int i) {
      return this.permutation[i];
   }

   public Matrix3f transformation() {
      return this.transformation;
   }
}
