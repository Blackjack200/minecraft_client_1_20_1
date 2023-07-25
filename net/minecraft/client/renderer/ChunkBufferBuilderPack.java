package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkBufferBuilderPack {
   private final Map<RenderType, BufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((rendertype1) -> rendertype1, (rendertype) -> new BufferBuilder(rendertype.bufferSize())));

   public BufferBuilder builder(RenderType rendertype) {
      return this.builders.get(rendertype);
   }

   public void clearAll() {
      this.builders.values().forEach(BufferBuilder::clear);
   }

   public void discardAll() {
      this.builders.values().forEach(BufferBuilder::discard);
   }
}
