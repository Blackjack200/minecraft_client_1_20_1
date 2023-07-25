package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;

public final class MissingTextureAtlasSprite {
   private static final int MISSING_IMAGE_WIDTH = 16;
   private static final int MISSING_IMAGE_HEIGHT = 16;
   private static final String MISSING_TEXTURE_NAME = "missingno";
   private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
   private static final AnimationMetadataSection EMPTY_ANIMATION_META = new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false);
   @Nullable
   private static DynamicTexture missingTexture;

   private static NativeImage generateMissingImage(int i, int j) {
      NativeImage nativeimage = new NativeImage(i, j, false);
      int k = -16777216;
      int l = -524040;

      for(int i1 = 0; i1 < j; ++i1) {
         for(int j1 = 0; j1 < i; ++j1) {
            if (i1 < j / 2 ^ j1 < i / 2) {
               nativeimage.setPixelRGBA(j1, i1, -524040);
            } else {
               nativeimage.setPixelRGBA(j1, i1, -16777216);
            }
         }
      }

      return nativeimage;
   }

   public static SpriteContents create() {
      NativeImage nativeimage = generateMissingImage(16, 16);
      return new SpriteContents(MISSING_TEXTURE_LOCATION, new FrameSize(16, 16), nativeimage, EMPTY_ANIMATION_META);
   }

   public static ResourceLocation getLocation() {
      return MISSING_TEXTURE_LOCATION;
   }

   public static DynamicTexture getTexture() {
      if (missingTexture == null) {
         NativeImage nativeimage = generateMissingImage(16, 16);
         nativeimage.untrack();
         missingTexture = new DynamicTexture(nativeimage);
         Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
      }

      return missingTexture;
   }
}
