package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ResourceLocationPattern;

public class SourceFilter implements SpriteSource {
   public static final Codec<SourceFilter> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocationPattern.CODEC.fieldOf("pattern").forGetter((sourcefilter) -> sourcefilter.filter)).apply(recordcodecbuilder_instance, SourceFilter::new));
   private final ResourceLocationPattern filter;

   public SourceFilter(ResourceLocationPattern resourcelocationpattern) {
      this.filter = resourcelocationpattern;
   }

   public void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output) {
      spritesource_output.removeAll(this.filter.locationPredicate());
   }

   public SpriteSourceType type() {
      return SpriteSources.FILTER;
   }
}
