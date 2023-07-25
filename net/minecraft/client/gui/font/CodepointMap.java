package net.minecraft.client.gui.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public class CodepointMap<T> {
   private static final int BLOCK_BITS = 8;
   private static final int BLOCK_SIZE = 256;
   private static final int IN_BLOCK_MASK = 255;
   private static final int MAX_BLOCK = 4351;
   private static final int BLOCK_COUNT = 4352;
   private final T[] empty;
   private final T[][] blockMap;
   private final IntFunction<T[]> blockConstructor;

   public CodepointMap(IntFunction<T[]> intfunction, IntFunction<T[][]> intfunction1) {
      this.empty = (T[])((Object[])intfunction.apply(256));
      this.blockMap = (T[][])((Object[][])intfunction1.apply(4352));
      Arrays.fill(this.blockMap, this.empty);
      this.blockConstructor = intfunction;
   }

   public void clear() {
      Arrays.fill(this.blockMap, this.empty);
   }

   @Nullable
   public T get(int i) {
      int j = i >> 8;
      int k = i & 255;
      return this.blockMap[j][k];
   }

   @Nullable
   public T put(int i, T object) {
      int j = i >> 8;
      int k = i & 255;
      T[] aobject = this.blockMap[j];
      if (aobject == this.empty) {
         aobject = (T[])((Object[])this.blockConstructor.apply(256));
         this.blockMap[j] = aobject;
         aobject[k] = object;
         return (T)null;
      } else {
         T object1 = aobject[k];
         aobject[k] = object;
         return object1;
      }
   }

   public T computeIfAbsent(int i, IntFunction<T> intfunction) {
      int j = i >> 8;
      int k = i & 255;
      T[] aobject = this.blockMap[j];
      T object = aobject[k];
      if (object != null) {
         return object;
      } else {
         if (aobject == this.empty) {
            aobject = (T[])((Object[])this.blockConstructor.apply(256));
            this.blockMap[j] = aobject;
         }

         T object1 = intfunction.apply(i);
         aobject[k] = object1;
         return object1;
      }
   }

   @Nullable
   public T remove(int i) {
      int j = i >> 8;
      int k = i & 255;
      T[] aobject = this.blockMap[j];
      if (aobject == this.empty) {
         return (T)null;
      } else {
         T object = aobject[k];
         aobject[k] = null;
         return object;
      }
   }

   public void forEach(CodepointMap.Output<T> codepointmap_output) {
      for(int i = 0; i < this.blockMap.length; ++i) {
         T[] aobject = this.blockMap[i];
         if (aobject != this.empty) {
            for(int j = 0; j < aobject.length; ++j) {
               T object = aobject[j];
               if (object != null) {
                  int k = i << 8 | j;
                  codepointmap_output.accept(k, object);
               }
            }
         }
      }

   }

   public IntSet keySet() {
      IntOpenHashSet intopenhashset = new IntOpenHashSet();
      this.forEach((i, object) -> intopenhashset.add(i));
      return intopenhashset;
   }

   @FunctionalInterface
   public interface Output<T> {
      void accept(int i, T object);
   }
}
