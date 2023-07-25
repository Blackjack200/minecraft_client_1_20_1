package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class Unstitcher implements SpriteSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<Unstitcher> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("resource").forGetter((unstitcher3) -> unstitcher3.resource), ExtraCodecs.nonEmptyList(Unstitcher.Region.CODEC.listOf()).fieldOf("regions").forGetter((unstitcher2) -> unstitcher2.regions), Codec.DOUBLE.optionalFieldOf("divisor_x", Double.valueOf(1.0D)).forGetter((unstitcher1) -> unstitcher1.xDivisor), Codec.DOUBLE.optionalFieldOf("divisor_y", Double.valueOf(1.0D)).forGetter((unstitcher) -> unstitcher.yDivisor)).apply(recordcodecbuilder_instance, Unstitcher::new));
   private final ResourceLocation resource;
   private final List<Unstitcher.Region> regions;
   private final double xDivisor;
   private final double yDivisor;

   public Unstitcher(ResourceLocation resourcelocation, List<Unstitcher.Region> list, double d0, double d1) {
      this.resource = resourcelocation;
      this.regions = list;
      this.xDivisor = d0;
      this.yDivisor = d1;
   }

   public void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output) {
      ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(this.resource);
      Optional<Resource> optional = resourcemanager.getResource(resourcelocation);
      if (optional.isPresent()) {
         LazyLoadedImage lazyloadedimage = new LazyLoadedImage(resourcelocation, optional.get(), this.regions.size());

         for(Unstitcher.Region unstitcher_region : this.regions) {
            spritesource_output.add(unstitcher_region.sprite, new Unstitcher.RegionInstance(lazyloadedimage, unstitcher_region, this.xDivisor, this.yDivisor));
         }
      } else {
         LOGGER.warn("Missing sprite: {}", (Object)resourcelocation);
      }

   }

   public SpriteSourceType type() {
      return SpriteSources.UNSTITCHER;
   }

   static record Region(ResourceLocation sprite, double x, double y, double width, double height) {
      final ResourceLocation sprite;
      final double x;
      final double y;
      final double width;
      final double height;
      public static final Codec<Unstitcher.Region> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("sprite").forGetter(Unstitcher.Region::sprite), Codec.DOUBLE.fieldOf("x").forGetter(Unstitcher.Region::x), Codec.DOUBLE.fieldOf("y").forGetter(Unstitcher.Region::y), Codec.DOUBLE.fieldOf("width").forGetter(Unstitcher.Region::width), Codec.DOUBLE.fieldOf("height").forGetter(Unstitcher.Region::height)).apply(recordcodecbuilder_instance, Unstitcher.Region::new));
   }

   static class RegionInstance implements SpriteSource.SpriteSupplier {
      private final LazyLoadedImage image;
      private final Unstitcher.Region region;
      private final double xDivisor;
      private final double yDivisor;

      RegionInstance(LazyLoadedImage lazyloadedimage, Unstitcher.Region unstitcher_region, double d0, double d1) {
         this.image = lazyloadedimage;
         this.region = unstitcher_region;
         this.xDivisor = d0;
         this.yDivisor = d1;
      }

      public SpriteContents get() {
         try {
            NativeImage nativeimage = this.image.get();
            double d0 = (double)nativeimage.getWidth() / this.xDivisor;
            double d1 = (double)nativeimage.getHeight() / this.yDivisor;
            int i = Mth.floor(this.region.x * d0);
            int j = Mth.floor(this.region.y * d1);
            int k = Mth.floor(this.region.width * d0);
            int l = Mth.floor(this.region.height * d1);
            NativeImage nativeimage1 = new NativeImage(NativeImage.Format.RGBA, k, l, false);
            nativeimage.copyRect(nativeimage1, i, j, 0, 0, k, l, false, false);
            return new SpriteContents(this.region.sprite, new FrameSize(k, l), nativeimage1, AnimationMetadataSection.EMPTY);
         } catch (Exception var15) {
            Unstitcher.LOGGER.error("Failed to unstitch region {}", this.region.sprite, var15);
         } finally {
            this.image.release();
         }

         return MissingTextureAtlasSprite.create();
      }

      public void discard() {
         this.image.release();
      }
   }
}
