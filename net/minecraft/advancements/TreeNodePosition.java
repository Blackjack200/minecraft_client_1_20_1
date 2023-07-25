package net.minecraft.advancements;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;

public class TreeNodePosition {
   private final Advancement advancement;
   @Nullable
   private final TreeNodePosition parent;
   @Nullable
   private final TreeNodePosition previousSibling;
   private final int childIndex;
   private final List<TreeNodePosition> children = Lists.newArrayList();
   private TreeNodePosition ancestor;
   @Nullable
   private TreeNodePosition thread;
   private int x;
   private float y;
   private float mod;
   private float change;
   private float shift;

   public TreeNodePosition(Advancement advancement, @Nullable TreeNodePosition treenodeposition, @Nullable TreeNodePosition treenodeposition1, int i, int j) {
      if (advancement.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position an invisible advancement!");
      } else {
         this.advancement = advancement;
         this.parent = treenodeposition;
         this.previousSibling = treenodeposition1;
         this.childIndex = i;
         this.ancestor = this;
         this.x = j;
         this.y = -1.0F;
         TreeNodePosition treenodeposition2 = null;

         for(Advancement advancement1 : advancement.getChildren()) {
            treenodeposition2 = this.addChild(advancement1, treenodeposition2);
         }

      }
   }

   @Nullable
   private TreeNodePosition addChild(Advancement advancement, @Nullable TreeNodePosition treenodeposition) {
      if (advancement.getDisplay() != null) {
         treenodeposition = new TreeNodePosition(advancement, this, treenodeposition, this.children.size() + 1, this.x + 1);
         this.children.add(treenodeposition);
      } else {
         for(Advancement advancement1 : advancement.getChildren()) {
            treenodeposition = this.addChild(advancement1, treenodeposition);
         }
      }

      return treenodeposition;
   }

   private void firstWalk() {
      if (this.children.isEmpty()) {
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
         } else {
            this.y = 0.0F;
         }

      } else {
         TreeNodePosition treenodeposition = null;

         for(TreeNodePosition treenodeposition1 : this.children) {
            treenodeposition1.firstWalk();
            treenodeposition = treenodeposition1.apportion(treenodeposition == null ? treenodeposition1 : treenodeposition);
         }

         this.executeShifts();
         float f = ((this.children.get(0)).y + (this.children.get(this.children.size() - 1)).y) / 2.0F;
         if (this.previousSibling != null) {
            this.y = this.previousSibling.y + 1.0F;
            this.mod = this.y - f;
         } else {
            this.y = f;
         }

      }
   }

   private float secondWalk(float f, int i, float f1) {
      this.y += f;
      this.x = i;
      if (this.y < f1) {
         f1 = this.y;
      }

      for(TreeNodePosition treenodeposition : this.children) {
         f1 = treenodeposition.secondWalk(f + this.mod, i + 1, f1);
      }

      return f1;
   }

   private void thirdWalk(float f) {
      this.y += f;

      for(TreeNodePosition treenodeposition : this.children) {
         treenodeposition.thirdWalk(f);
      }

   }

   private void executeShifts() {
      float f = 0.0F;
      float f1 = 0.0F;

      for(int i = this.children.size() - 1; i >= 0; --i) {
         TreeNodePosition treenodeposition = this.children.get(i);
         treenodeposition.y += f;
         treenodeposition.mod += f;
         f1 += treenodeposition.change;
         f += treenodeposition.shift + f1;
      }

   }

   @Nullable
   private TreeNodePosition previousOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(0) : null;
      }
   }

   @Nullable
   private TreeNodePosition nextOrThread() {
      if (this.thread != null) {
         return this.thread;
      } else {
         return !this.children.isEmpty() ? this.children.get(this.children.size() - 1) : null;
      }
   }

   private TreeNodePosition apportion(TreeNodePosition treenodeposition) {
      if (this.previousSibling == null) {
         return treenodeposition;
      } else {
         TreeNodePosition treenodeposition1 = this;
         TreeNodePosition treenodeposition2 = this;
         TreeNodePosition treenodeposition3 = this.previousSibling;
         TreeNodePosition treenodeposition4 = this.parent.children.get(0);
         float f = this.mod;
         float f1 = this.mod;
         float f2 = treenodeposition3.mod;

         float f3;
         for(f3 = treenodeposition4.mod; treenodeposition3.nextOrThread() != null && treenodeposition1.previousOrThread() != null; f1 += treenodeposition2.mod) {
            treenodeposition3 = treenodeposition3.nextOrThread();
            treenodeposition1 = treenodeposition1.previousOrThread();
            treenodeposition4 = treenodeposition4.previousOrThread();
            treenodeposition2 = treenodeposition2.nextOrThread();
            treenodeposition2.ancestor = this;
            float f4 = treenodeposition3.y + f2 - (treenodeposition1.y + f) + 1.0F;
            if (f4 > 0.0F) {
               treenodeposition3.getAncestor(this, treenodeposition).moveSubtree(this, f4);
               f += f4;
               f1 += f4;
            }

            f2 += treenodeposition3.mod;
            f += treenodeposition1.mod;
            f3 += treenodeposition4.mod;
         }

         if (treenodeposition3.nextOrThread() != null && treenodeposition2.nextOrThread() == null) {
            treenodeposition2.thread = treenodeposition3.nextOrThread();
            treenodeposition2.mod += f2 - f1;
         } else {
            if (treenodeposition1.previousOrThread() != null && treenodeposition4.previousOrThread() == null) {
               treenodeposition4.thread = treenodeposition1.previousOrThread();
               treenodeposition4.mod += f - f3;
            }

            treenodeposition = this;
         }

         return treenodeposition;
      }
   }

   private void moveSubtree(TreeNodePosition treenodeposition, float f) {
      float f1 = (float)(treenodeposition.childIndex - this.childIndex);
      if (f1 != 0.0F) {
         treenodeposition.change -= f / f1;
         this.change += f / f1;
      }

      treenodeposition.shift += f;
      treenodeposition.y += f;
      treenodeposition.mod += f;
   }

   private TreeNodePosition getAncestor(TreeNodePosition treenodeposition, TreeNodePosition treenodeposition1) {
      return this.ancestor != null && treenodeposition.parent.children.contains(this.ancestor) ? this.ancestor : treenodeposition1;
   }

   private void finalizePosition() {
      if (this.advancement.getDisplay() != null) {
         this.advancement.getDisplay().setLocation((float)this.x, this.y);
      }

      if (!this.children.isEmpty()) {
         for(TreeNodePosition treenodeposition : this.children) {
            treenodeposition.finalizePosition();
         }
      }

   }

   public static void run(Advancement advancement) {
      if (advancement.getDisplay() == null) {
         throw new IllegalArgumentException("Can't position children of an invisible root!");
      } else {
         TreeNodePosition treenodeposition = new TreeNodePosition(advancement, (TreeNodePosition)null, (TreeNodePosition)null, 1, 0);
         treenodeposition.firstWalk();
         float f = treenodeposition.secondWalk(0.0F, 0, treenodeposition.y);
         if (f < 0.0F) {
            treenodeposition.thirdWalk(-f);
         }

         treenodeposition.finalizePosition();
      }
   }
}
