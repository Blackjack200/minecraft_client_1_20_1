package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Stitcher<T extends Stitcher.Entry> {
   private static final Comparator<Stitcher.Holder<?>> HOLDER_COMPARATOR = Comparator.comparing((stitcher_holder) -> -stitcher_holder.height).thenComparing((stitcher_holder) -> -stitcher_holder.width).thenComparing((stitcher_holder) -> stitcher_holder.entry.name());
   private final int mipLevel;
   private final List<Stitcher.Holder<T>> texturesToBeStitched = new ArrayList<>();
   private final List<Stitcher.Region<T>> storage = new ArrayList<>();
   private int storageX;
   private int storageY;
   private final int maxWidth;
   private final int maxHeight;

   public Stitcher(int i, int j, int k) {
      this.mipLevel = k;
      this.maxWidth = i;
      this.maxHeight = j;
   }

   public int getWidth() {
      return this.storageX;
   }

   public int getHeight() {
      return this.storageY;
   }

   public void registerSprite(T stitcher_entry) {
      Stitcher.Holder<T> stitcher_holder = new Stitcher.Holder<>(stitcher_entry, this.mipLevel);
      this.texturesToBeStitched.add(stitcher_holder);
   }

   public void stitch() {
      List<Stitcher.Holder<T>> list = new ArrayList<>(this.texturesToBeStitched);
      list.sort(HOLDER_COMPARATOR);

      for(Stitcher.Holder<T> stitcher_holder : list) {
         if (!this.addToStorage(stitcher_holder)) {
            throw new StitcherException(stitcher_holder.entry, list.stream().map((stitcher_holder1) -> stitcher_holder1.entry).collect(ImmutableList.toImmutableList()));
         }
      }

   }

   public void gatherSprites(Stitcher.SpriteLoader<T> stitcher_spriteloader) {
      for(Stitcher.Region<T> stitcher_region : this.storage) {
         stitcher_region.walk(stitcher_spriteloader);
      }

   }

   static int smallestFittingMinTexel(int i, int j) {
      return (i >> j) + ((i & (1 << j) - 1) == 0 ? 0 : 1) << j;
   }

   private boolean addToStorage(Stitcher.Holder<T> stitcher_holder) {
      for(Stitcher.Region<T> stitcher_region : this.storage) {
         if (stitcher_region.add(stitcher_holder)) {
            return true;
         }
      }

      return this.expand(stitcher_holder);
   }

   private boolean expand(Stitcher.Holder<T> stitcher_holder) {
      int i = Mth.smallestEncompassingPowerOfTwo(this.storageX);
      int j = Mth.smallestEncompassingPowerOfTwo(this.storageY);
      int k = Mth.smallestEncompassingPowerOfTwo(this.storageX + stitcher_holder.width);
      int l = Mth.smallestEncompassingPowerOfTwo(this.storageY + stitcher_holder.height);
      boolean flag = k <= this.maxWidth;
      boolean flag1 = l <= this.maxHeight;
      if (!flag && !flag1) {
         return false;
      } else {
         boolean flag2 = flag && i != k;
         boolean flag3 = flag1 && j != l;
         boolean flag4;
         if (flag2 ^ flag3) {
            flag4 = flag2;
         } else {
            flag4 = flag && i <= j;
         }

         Stitcher.Region<T> stitcher_region;
         if (flag4) {
            if (this.storageY == 0) {
               this.storageY = l;
            }

            stitcher_region = new Stitcher.Region<>(this.storageX, 0, k - this.storageX, this.storageY);
            this.storageX = k;
         } else {
            stitcher_region = new Stitcher.Region<>(0, this.storageY, this.storageX, l - this.storageY);
            this.storageY = l;
         }

         stitcher_region.add(stitcher_holder);
         this.storage.add(stitcher_region);
         return true;
      }
   }

   public interface Entry {
      int width();

      int height();

      ResourceLocation name();
   }

   static record Holder<T extends Stitcher.Entry>(T entry, int width, int height) {
      final T entry;
      final int width;
      final int height;

      public Holder(T stitcher_entry, int i) {
         this(stitcher_entry, Stitcher.smallestFittingMinTexel(stitcher_entry.width(), i), Stitcher.smallestFittingMinTexel(stitcher_entry.height(), i));
      }
   }

   public static class Region<T extends Stitcher.Entry> {
      private final int originX;
      private final int originY;
      private final int width;
      private final int height;
      @Nullable
      private List<Stitcher.Region<T>> subSlots;
      @Nullable
      private Stitcher.Holder<T> holder;

      public Region(int i, int j, int k, int l) {
         this.originX = i;
         this.originY = j;
         this.width = k;
         this.height = l;
      }

      public int getX() {
         return this.originX;
      }

      public int getY() {
         return this.originY;
      }

      public boolean add(Stitcher.Holder<T> stitcher_holder) {
         if (this.holder != null) {
            return false;
         } else {
            int i = stitcher_holder.width;
            int j = stitcher_holder.height;
            if (i <= this.width && j <= this.height) {
               if (i == this.width && j == this.height) {
                  this.holder = stitcher_holder;
                  return true;
               } else {
                  if (this.subSlots == null) {
                     this.subSlots = new ArrayList<>(1);
                     this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY, i, j));
                     int k = this.width - i;
                     int l = this.height - j;
                     if (l > 0 && k > 0) {
                        int i1 = Math.max(this.height, k);
                        int j1 = Math.max(this.width, l);
                        if (i1 >= j1) {
                           this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, i, l));
                           this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, this.height));
                        } else {
                           this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, j));
                           this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, this.width, l));
                        }
                     } else if (k == 0) {
                        this.subSlots.add(new Stitcher.Region<>(this.originX, this.originY + j, i, l));
                     } else if (l == 0) {
                        this.subSlots.add(new Stitcher.Region<>(this.originX + i, this.originY, k, j));
                     }
                  }

                  for(Stitcher.Region<T> stitcher_region : this.subSlots) {
                     if (stitcher_region.add(stitcher_holder)) {
                        return true;
                     }
                  }

                  return false;
               }
            } else {
               return false;
            }
         }
      }

      public void walk(Stitcher.SpriteLoader<T> stitcher_spriteloader) {
         if (this.holder != null) {
            stitcher_spriteloader.load(this.holder.entry, this.getX(), this.getY());
         } else if (this.subSlots != null) {
            for(Stitcher.Region<T> stitcher_region : this.subSlots) {
               stitcher_region.walk(stitcher_spriteloader);
            }
         }

      }

      public String toString() {
         return "Slot{originX=" + this.originX + ", originY=" + this.originY + ", width=" + this.width + ", height=" + this.height + ", texture=" + this.holder + ", subSlots=" + this.subSlots + "}";
      }
   }

   public interface SpriteLoader<T extends Stitcher.Entry> {
      void load(T stitcher_entry, int i, int j);
   }
}
