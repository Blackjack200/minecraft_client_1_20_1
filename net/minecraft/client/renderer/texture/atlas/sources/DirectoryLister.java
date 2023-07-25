package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class DirectoryLister implements SpriteSource {
   public static final Codec<DirectoryLister> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.fieldOf("source").forGetter((directorylister1) -> directorylister1.sourcePath), Codec.STRING.fieldOf("prefix").forGetter((directorylister) -> directorylister.idPrefix)).apply(recordcodecbuilder_instance, DirectoryLister::new));
   private final String sourcePath;
   private final String idPrefix;

   public DirectoryLister(String s, String s1) {
      this.sourcePath = s;
      this.idPrefix = s1;
   }

   public void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output) {
      FileToIdConverter filetoidconverter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
      filetoidconverter.listMatchingResources(resourcemanager).forEach((resourcelocation, resource) -> {
         ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation).withPrefix(this.idPrefix);
         spritesource_output.add(resourcelocation1, resource);
      });
   }

   public SpriteSourceType type() {
      return SpriteSources.DIRECTORY;
   }
}
