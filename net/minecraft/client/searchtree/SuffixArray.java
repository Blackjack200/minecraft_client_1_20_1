package net.minecraft.client.searchtree;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

public class SuffixArray<T> {
   private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
   private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int END_OF_TEXT_MARKER = -1;
   private static final int END_OF_DATA = -2;
   protected final List<T> list = Lists.newArrayList();
   private final IntList chars = new IntArrayList();
   private final IntList wordStarts = new IntArrayList();
   private IntList suffixToT = new IntArrayList();
   private IntList offsets = new IntArrayList();
   private int maxStringLength;

   public void add(T object, String s) {
      this.maxStringLength = Math.max(this.maxStringLength, s.length());
      int i = this.list.size();
      this.list.add(object);
      this.wordStarts.add(this.chars.size());

      for(int j = 0; j < s.length(); ++j) {
         this.suffixToT.add(i);
         this.offsets.add(j);
         this.chars.add(s.charAt(j));
      }

      this.suffixToT.add(i);
      this.offsets.add(s.length());
      this.chars.add(-1);
   }

   public void generate() {
      int i = this.chars.size();
      int[] aint = new int[i];
      int[] aint1 = new int[i];
      int[] aint2 = new int[i];
      int[] aint3 = new int[i];
      IntComparator intcomparator = (l2, i3) -> aint1[l2] == aint1[i3] ? Integer.compare(aint2[l2], aint2[i3]) : Integer.compare(aint1[l2], aint1[i3]);
      Swapper swapper = (i2, j2) -> {
         if (i2 != j2) {
            int k2 = aint1[i2];
            aint1[i2] = aint1[j2];
            aint1[j2] = k2;
            k2 = aint2[i2];
            aint2[i2] = aint2[j2];
            aint2[j2] = k2;
            k2 = aint3[i2];
            aint3[i2] = aint3[j2];
            aint3[j2] = k2;
         }

      };

      for(int j = 0; j < i; ++j) {
         aint[j] = this.chars.getInt(j);
      }

      int k = 1;

      for(int l = Math.min(i, this.maxStringLength); k * 2 < l; k *= 2) {
         for(int i1 = 0; i1 < i; aint3[i1] = i1++) {
            aint1[i1] = aint[i1];
            aint2[i1] = i1 + k < i ? aint[i1 + k] : -2;
         }

         Arrays.quickSort(0, i, intcomparator, swapper);

         for(int j1 = 0; j1 < i; ++j1) {
            if (j1 > 0 && aint1[j1] == aint1[j1 - 1] && aint2[j1] == aint2[j1 - 1]) {
               aint[aint3[j1]] = aint[aint3[j1 - 1]];
            } else {
               aint[aint3[j1]] = j1;
            }
         }
      }

      IntList intlist = this.suffixToT;
      IntList intlist1 = this.offsets;
      this.suffixToT = new IntArrayList(intlist.size());
      this.offsets = new IntArrayList(intlist1.size());

      for(int k1 = 0; k1 < i; ++k1) {
         int l1 = aint3[k1];
         this.suffixToT.add(intlist.getInt(l1));
         this.offsets.add(intlist1.getInt(l1));
      }

      if (DEBUG_ARRAY) {
         this.print();
      }

   }

   private void print() {
      for(int i = 0; i < this.suffixToT.size(); ++i) {
         LOGGER.debug("{} {}", i, this.getString(i));
      }

      LOGGER.debug("");
   }

   private String getString(int i) {
      int j = this.offsets.getInt(i);
      int k = this.wordStarts.getInt(this.suffixToT.getInt(i));
      StringBuilder stringbuilder = new StringBuilder();

      for(int l = 0; k + l < this.chars.size(); ++l) {
         if (l == j) {
            stringbuilder.append('^');
         }

         int i1 = this.chars.getInt(k + l);
         if (i1 == -1) {
            break;
         }

         stringbuilder.append((char)i1);
      }

      return stringbuilder.toString();
   }

   private int compare(String s, int i) {
      int j = this.wordStarts.getInt(this.suffixToT.getInt(i));
      int k = this.offsets.getInt(i);

      for(int l = 0; l < s.length(); ++l) {
         int i1 = this.chars.getInt(j + k + l);
         if (i1 == -1) {
            return 1;
         }

         char c0 = s.charAt(l);
         char c1 = (char)i1;
         if (c0 < c1) {
            return -1;
         }

         if (c0 > c1) {
            return 1;
         }
      }

      return 0;
   }

   public List<T> search(String s) {
      int i = this.suffixToT.size();
      int j = 0;
      int k = i;

      while(j < k) {
         int l = j + (k - j) / 2;
         int i1 = this.compare(s, l);
         if (DEBUG_COMPARISONS) {
            LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", s, l, this.getString(l), i1);
         }

         if (i1 > 0) {
            j = l + 1;
         } else {
            k = l;
         }
      }

      if (j >= 0 && j < i) {
         int j1 = j;
         k = i;

         while(j < k) {
            int k1 = j + (k - j) / 2;
            int l1 = this.compare(s, k1);
            if (DEBUG_COMPARISONS) {
               LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", s, k1, this.getString(k1), l1);
            }

            if (l1 >= 0) {
               j = k1 + 1;
            } else {
               k = k1;
            }
         }

         int i2 = j;
         IntSet intset = new IntOpenHashSet();

         for(int j2 = j1; j2 < i2; ++j2) {
            intset.add(this.suffixToT.getInt(j2));
         }

         int[] aint = intset.toIntArray();
         java.util.Arrays.sort(aint);
         Set<T> set = Sets.newLinkedHashSet();

         for(int k2 : aint) {
            set.add(this.list.get(k2));
         }

         return Lists.newArrayList(set);
      } else {
         return Collections.emptyList();
      }
   }
}
