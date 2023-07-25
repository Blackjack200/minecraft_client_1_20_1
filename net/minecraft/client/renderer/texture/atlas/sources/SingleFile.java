package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class SingleFile implements SpriteSource {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<SingleFile> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("resource").forGetter((singlefile1) -> singlefile1.resourceId), ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter((singlefile) -> singlefile.spriteId)).apply(recordcodecbuilder_instance, SingleFile::new));
   private final ResourceLocation resourceId;
   private final Optional<ResourceLocation> spriteId;

   public SingleFile(ResourceLocation resourcelocation, Optional<ResourceLocation> optional) {
      this.resourceId = resourcelocation;
      this.spriteId = optional;
   }

   public void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output) {
      ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
      Optional<Resource> optional = resourcemanager.getResource(resourcelocation);
      if (optional.isPresent()) {
         spritesource_output.add(this.spriteId.orElse(this.resourceId), optional.get());
      } else {
         LOGGER.warn("Missing sprite: {}", (Object)resourcelocation);
      }

   }

   public SpriteSourceType type() {
      return SpriteSources.SINGLE_FILE;
   }
}
