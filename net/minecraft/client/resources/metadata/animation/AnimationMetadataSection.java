package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.Lists;
import java.util.List;

public class AnimationMetadataSection {
   public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
   public static final String SECTION_NAME = "animation";
   public static final int DEFAULT_FRAME_TIME = 1;
   public static final int UNKNOWN_SIZE = -1;
   public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection(Lists.newArrayList(), -1, -1, 1, false) {
      public FrameSize calculateFrameSize(int i, int j) {
         return new FrameSize(i, j);
      }
   };
   private final List<AnimationFrame> frames;
   private final int frameWidth;
   private final int frameHeight;
   private final int defaultFrameTime;
   private final boolean interpolatedFrames;

   public AnimationMetadataSection(List<AnimationFrame> list, int i, int j, int k, boolean flag) {
      this.frames = list;
      this.frameWidth = i;
      this.frameHeight = j;
      this.defaultFrameTime = k;
      this.interpolatedFrames = flag;
   }

   public FrameSize calculateFrameSize(int i, int j) {
      if (this.frameWidth != -1) {
         return this.frameHeight != -1 ? new FrameSize(this.frameWidth, this.frameHeight) : new FrameSize(this.frameWidth, j);
      } else if (this.frameHeight != -1) {
         return new FrameSize(i, this.frameHeight);
      } else {
         int k = Math.min(i, j);
         return new FrameSize(k, k);
      }
   }

   public int getDefaultFrameTime() {
      return this.defaultFrameTime;
   }

   public boolean isInterpolatedFrames() {
      return this.interpolatedFrames;
   }

   public void forEachFrame(AnimationMetadataSection.FrameOutput animationmetadatasection_frameoutput) {
      for(AnimationFrame animationframe : this.frames) {
         animationmetadatasection_frameoutput.accept(animationframe.getIndex(), animationframe.getTime(this.defaultFrameTime));
      }

   }

   @FunctionalInterface
   public interface FrameOutput {
      void accept(int i, int j);
   }
}
