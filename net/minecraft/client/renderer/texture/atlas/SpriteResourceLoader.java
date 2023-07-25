package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class SpriteResourceLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
   private final List<SpriteSource> sources;

   private SpriteResourceLoader(List<SpriteSource> list) {
      this.sources = list;
   }

   public List<Supplier<SpriteContents>> list(ResourceManager resourcemanager) {
      final Map<ResourceLocation, SpriteSource.SpriteSupplier> map = new HashMap<>();
      SpriteSource.Output spritesource_output = new SpriteSource.Output() {
         public void add(ResourceLocation resourcelocation, SpriteSource.SpriteSupplier spritesource_spritesupplier) {
            SpriteSource.SpriteSupplier spritesource_spritesupplier1 = map.put(resourcelocation, spritesource_spritesupplier);
            if (spritesource_spritesupplier1 != null) {
               spritesource_spritesupplier1.discard();
            }

         }

         public void removeAll(Predicate<ResourceLocation> predicate) {
            Iterator<Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier>> iterator = map.entrySet().iterator();

            while(iterator.hasNext()) {
               Map.Entry<ResourceLocation, SpriteSource.SpriteSupplier> map_entry = iterator.next();
               if (predicate.test(map_entry.getKey())) {
                  map_entry.getValue().discard();
                  iterator.remove();
               }
            }

         }
      };
      this.sources.forEach((spritesource) -> spritesource.run(resourcemanager, spritesource_output));
      ImmutableList.Builder<Supplier<SpriteContents>> immutablelist_builder = ImmutableList.builder();
      immutablelist_builder.add(MissingTextureAtlasSprite::create);
      immutablelist_builder.addAll(map.values());
      return immutablelist_builder.build();
   }

   public static SpriteResourceLoader load(ResourceManager resourcemanager, ResourceLocation resourcelocation) {
      ResourceLocation resourcelocation1 = ATLAS_INFO_CONVERTER.idToFile(resourcelocation);
      List<SpriteSource> list = new ArrayList<>();

      for(Resource resource : resourcemanager.getResourceStack(resourcelocation1)) {
         try {
            BufferedReader bufferedreader = resource.openAsReader();

            try {
               Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedreader));
               list.addAll(SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow(false, LOGGER::error));
            } catch (Throwable var10) {
               if (bufferedreader != null) {
                  try {
                     bufferedreader.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (bufferedreader != null) {
               bufferedreader.close();
            }
         } catch (Exception var11) {
            LOGGER.warn("Failed to parse atlas definition {} in pack {}", resourcelocation1, resource.sourcePackId(), var11);
         }
      }

      return new SpriteResourceLoader(list);
   }
}
