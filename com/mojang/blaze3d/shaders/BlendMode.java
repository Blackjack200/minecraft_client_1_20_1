package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;
import javax.annotation.Nullable;

public class BlendMode {
   @Nullable
   private static BlendMode lastApplied;
   private final int srcColorFactor;
   private final int srcAlphaFactor;
   private final int dstColorFactor;
   private final int dstAlphaFactor;
   private final int blendFunc;
   private final boolean separateBlend;
   private final boolean opaque;

   private BlendMode(boolean flag, boolean flag1, int i, int j, int k, int l, int i1) {
      this.separateBlend = flag;
      this.srcColorFactor = i;
      this.dstColorFactor = j;
      this.srcAlphaFactor = k;
      this.dstAlphaFactor = l;
      this.opaque = flag1;
      this.blendFunc = i1;
   }

   public BlendMode() {
      this(false, true, 1, 0, 1, 0, 32774);
   }

   public BlendMode(int i, int j, int k) {
      this(false, false, i, j, i, j, k);
   }

   public BlendMode(int i, int j, int k, int l, int i1) {
      this(true, false, i, j, k, l, i1);
   }

   public void apply() {
      if (!this.equals(lastApplied)) {
         if (lastApplied == null || this.opaque != lastApplied.isOpaque()) {
            lastApplied = this;
            if (this.opaque) {
               RenderSystem.disableBlend();
               return;
            }

            RenderSystem.enableBlend();
         }

         RenderSystem.blendEquation(this.blendFunc);
         if (this.separateBlend) {
            RenderSystem.blendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
         } else {
            RenderSystem.blendFunc(this.srcColorFactor, this.dstColorFactor);
         }

      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof BlendMode)) {
         return false;
      } else {
         BlendMode blendmode = (BlendMode)object;
         if (this.blendFunc != blendmode.blendFunc) {
            return false;
         } else if (this.dstAlphaFactor != blendmode.dstAlphaFactor) {
            return false;
         } else if (this.dstColorFactor != blendmode.dstColorFactor) {
            return false;
         } else if (this.opaque != blendmode.opaque) {
            return false;
         } else if (this.separateBlend != blendmode.separateBlend) {
            return false;
         } else if (this.srcAlphaFactor != blendmode.srcAlphaFactor) {
            return false;
         } else {
            return this.srcColorFactor == blendmode.srcColorFactor;
         }
      }
   }

   public int hashCode() {
      int i = this.srcColorFactor;
      i = 31 * i + this.srcAlphaFactor;
      i = 31 * i + this.dstColorFactor;
      i = 31 * i + this.dstAlphaFactor;
      i = 31 * i + this.blendFunc;
      i = 31 * i + (this.separateBlend ? 1 : 0);
      return 31 * i + (this.opaque ? 1 : 0);
   }

   public boolean isOpaque() {
      return this.opaque;
   }

   public static int stringToBlendFunc(String s) {
      String s1 = s.trim().toLowerCase(Locale.ROOT);
      if ("add".equals(s1)) {
         return 32774;
      } else if ("subtract".equals(s1)) {
         return 32778;
      } else if ("reversesubtract".equals(s1)) {
         return 32779;
      } else if ("reverse_subtract".equals(s1)) {
         return 32779;
      } else if ("min".equals(s1)) {
         return 32775;
      } else {
         return "max".equals(s1) ? '\u8008' : '\u8006';
      }
   }

   public static int stringToBlendFactor(String s) {
      String s1 = s.trim().toLowerCase(Locale.ROOT);
      s1 = s1.replaceAll("_", "");
      s1 = s1.replaceAll("one", "1");
      s1 = s1.replaceAll("zero", "0");
      s1 = s1.replaceAll("minus", "-");
      if ("0".equals(s1)) {
         return 0;
      } else if ("1".equals(s1)) {
         return 1;
      } else if ("srccolor".equals(s1)) {
         return 768;
      } else if ("1-srccolor".equals(s1)) {
         return 769;
      } else if ("dstcolor".equals(s1)) {
         return 774;
      } else if ("1-dstcolor".equals(s1)) {
         return 775;
      } else if ("srcalpha".equals(s1)) {
         return 770;
      } else if ("1-srcalpha".equals(s1)) {
         return 771;
      } else if ("dstalpha".equals(s1)) {
         return 772;
      } else {
         return "1-dstalpha".equals(s1) ? 773 : -1;
      }
   }
}
