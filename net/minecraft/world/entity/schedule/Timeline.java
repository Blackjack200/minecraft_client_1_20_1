package net.minecraft.world.entity.schedule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.util.Collection;
import java.util.List;

public class Timeline {
   private final List<Keyframe> keyframes = Lists.newArrayList();
   private int previousIndex;

   public ImmutableList<Keyframe> getKeyframes() {
      return ImmutableList.copyOf(this.keyframes);
   }

   public Timeline addKeyframe(int i, float f) {
      this.keyframes.add(new Keyframe(i, f));
      this.sortAndDeduplicateKeyframes();
      return this;
   }

   public Timeline addKeyframes(Collection<Keyframe> collection) {
      this.keyframes.addAll(collection);
      this.sortAndDeduplicateKeyframes();
      return this;
   }

   private void sortAndDeduplicateKeyframes() {
      Int2ObjectSortedMap<Keyframe> int2objectsortedmap = new Int2ObjectAVLTreeMap<>();
      this.keyframes.forEach((keyframe) -> int2objectsortedmap.put(keyframe.getTimeStamp(), keyframe));
      this.keyframes.clear();
      this.keyframes.addAll(int2objectsortedmap.values());
      this.previousIndex = 0;
   }

   public float getValueAt(int i) {
      if (this.keyframes.size() <= 0) {
         return 0.0F;
      } else {
         Keyframe keyframe = this.keyframes.get(this.previousIndex);
         Keyframe keyframe1 = this.keyframes.get(this.keyframes.size() - 1);
         boolean flag = i < keyframe.getTimeStamp();
         int j = flag ? 0 : this.previousIndex;
         float f = flag ? keyframe1.getValue() : keyframe.getValue();

         for(int k = j; k < this.keyframes.size(); ++k) {
            Keyframe keyframe2 = this.keyframes.get(k);
            if (keyframe2.getTimeStamp() > i) {
               break;
            }

            this.previousIndex = k;
            f = keyframe2.getValue();
         }

         return f;
      }
   }
}
