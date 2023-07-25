package net.minecraft.client.renderer.texture.atlas.sources;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import org.slf4j.Logger;

public class PalettedPermutations implements SpriteSource {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<PalettedPermutations> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.list(ResourceLocation.CODEC).fieldOf("textures").forGetter((palettedpermutations2) -> palettedpermutations2.textures), ResourceLocation.CODEC.fieldOf("palette_key").forGetter((palettedpermutations1) -> palettedpermutations1.paletteKey), Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations").forGetter((palettedpermutations) -> palettedpermutations.permutations)).apply(recordcodecbuilder_instance, PalettedPermutations::new));
   private final List<ResourceLocation> textures;
   private final Map<String, ResourceLocation> permutations;
   private final ResourceLocation paletteKey;

   private PalettedPermutations(List<ResourceLocation> list, ResourceLocation resourcelocation, Map<String, ResourceLocation> map) {
      this.textures = list;
      this.permutations = map;
      this.paletteKey = resourcelocation;
   }

   public void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output) {
      Supplier<int[]> supplier = Suppliers.memoize(() -> loadPaletteEntryFromImage(resourcemanager, this.paletteKey));
      Map<String, Supplier<IntUnaryOperator>> map = new HashMap<>();
      this.permutations.forEach((s, resourcelocation3) -> map.put(s, Suppliers.memoize(() -> createPaletteMapping(supplier.get(), loadPaletteEntryFromImage(resourcemanager, resourcelocation3)))));

      for(ResourceLocation resourcelocation : this.textures) {
         ResourceLocation resourcelocation1 = TEXTURE_ID_CONVERTER.idToFile(resourcelocation);
         Optional<Resource> optional = resourcemanager.getResource(resourcelocation1);
         if (optional.isEmpty()) {
            LOGGER.warn("Unable to find texture {}", (Object)resourcelocation1);
         } else {
            LazyLoadedImage lazyloadedimage = new LazyLoadedImage(resourcelocation1, optional.get(), map.size());

            for(Map.Entry<String, Supplier<IntUnaryOperator>> map_entry : map.entrySet()) {
               ResourceLocation resourcelocation2 = resourcelocation.withSuffix("_" + (String)map_entry.getKey());
               spritesource_output.add(resourcelocation2, new PalettedPermutations.PalettedSpriteSupplier(lazyloadedimage, map_entry.getValue(), resourcelocation2));
            }
         }
      }

   }

   private static IntUnaryOperator createPaletteMapping(int[] aint, int[] aint1) {
      if (aint1.length != aint.length) {
         LOGGER.warn("Palette mapping has different sizes: {} and {}", aint.length, aint1.length);
         throw new IllegalArgumentException();
      } else {
         Int2IntMap int2intmap = new Int2IntOpenHashMap(aint1.length);

         for(int i = 0; i < aint.length; ++i) {
            int j = aint[i];
            if (FastColor.ABGR32.alpha(j) != 0) {
               int2intmap.put(FastColor.ABGR32.transparent(j), aint1[i]);
            }
         }

         return (k) -> {
            int l = FastColor.ABGR32.alpha(k);
            if (l == 0) {
               return k;
            } else {
               int i1 = FastColor.ABGR32.transparent(k);
               int j1 = int2intmap.getOrDefault(i1, FastColor.ABGR32.opaque(i1));
               int k1 = FastColor.ABGR32.alpha(j1);
               return FastColor.ABGR32.color(l * k1 / 255, j1);
            }
         };
      }
   }

   public static int[] loadPaletteEntryFromImage(ResourceManager resourcemanager, ResourceLocation resourcelocation) {
      Optional<Resource> optional = resourcemanager.getResource(TEXTURE_ID_CONVERTER.idToFile(resourcelocation));
      if (optional.isEmpty()) {
         LOGGER.error("Failed to load palette image {}", (Object)resourcelocation);
         throw new IllegalArgumentException();
      } else {
         try {
            InputStream inputstream = optional.get().open();

            int[] var5;
            try {
               NativeImage nativeimage = NativeImage.read(inputstream);

               try {
                  var5 = nativeimage.getPixelsRGBA();
               } catch (Throwable var9) {
                  if (nativeimage != null) {
                     try {
                        nativeimage.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (nativeimage != null) {
                  nativeimage.close();
               }
            } catch (Throwable var10) {
               if (inputstream != null) {
                  try {
                     inputstream.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (inputstream != null) {
               inputstream.close();
            }

            return var5;
         } catch (Exception var11) {
            LOGGER.error("Couldn't load texture {}", resourcelocation, var11);
            throw new IllegalArgumentException();
         }
      }
   }

   public SpriteSourceType type() {
      return SpriteSources.PALETTED_PERMUTATIONS;
   }

   static record PalettedSpriteSupplier(LazyLoadedImage baseImage, Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) implements SpriteSource.SpriteSupplier {
      @Nullable
      public SpriteContents get() {
         Object var2;
         try {
            NativeImage nativeimage = this.baseImage.get().mappedCopy(this.palette.get());
            return new SpriteContents(this.permutationLocation, new FrameSize(nativeimage.getWidth(), nativeimage.getHeight()), nativeimage, AnimationMetadataSection.EMPTY);
         } catch (IllegalArgumentException | IOException var6) {
            PalettedPermutations.LOGGER.error("unable to apply palette to {}", this.permutationLocation, var6);
            var2 = null;
         } finally {
            this.baseImage.release();
         }

         return (SpriteContents)var2;
      }

      public void discard() {
         this.baseImage.release();
      }
   }
}
