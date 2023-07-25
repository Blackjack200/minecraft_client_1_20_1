package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface MultiBufferSource {
   static MultiBufferSource.BufferSource immediate(BufferBuilder bufferbuilder) {
      return immediateWithBuffers(ImmutableMap.of(), bufferbuilder);
   }

   static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> map, BufferBuilder bufferbuilder) {
      return new MultiBufferSource.BufferSource(bufferbuilder, map);
   }

   VertexConsumer getBuffer(RenderType rendertype);

   public static class BufferSource implements MultiBufferSource {
      protected final BufferBuilder builder;
      protected final Map<RenderType, BufferBuilder> fixedBuffers;
      protected Optional<RenderType> lastState = Optional.empty();
      protected final Set<BufferBuilder> startedBuffers = Sets.newHashSet();

      protected BufferSource(BufferBuilder bufferbuilder, Map<RenderType, BufferBuilder> map) {
         this.builder = bufferbuilder;
         this.fixedBuffers = map;
      }

      public VertexConsumer getBuffer(RenderType rendertype) {
         Optional<RenderType> optional = rendertype.asOptional();
         BufferBuilder bufferbuilder = this.getBuilderRaw(rendertype);
         if (!Objects.equals(this.lastState, optional) || !rendertype.canConsolidateConsecutiveGeometry()) {
            if (this.lastState.isPresent()) {
               RenderType rendertype1 = this.lastState.get();
               if (!this.fixedBuffers.containsKey(rendertype1)) {
                  this.endBatch(rendertype1);
               }
            }

            if (this.startedBuffers.add(bufferbuilder)) {
               bufferbuilder.begin(rendertype.mode(), rendertype.format());
            }

            this.lastState = optional;
         }

         return bufferbuilder;
      }

      private BufferBuilder getBuilderRaw(RenderType rendertype) {
         return this.fixedBuffers.getOrDefault(rendertype, this.builder);
      }

      public void endLastBatch() {
         if (this.lastState.isPresent()) {
            RenderType rendertype = this.lastState.get();
            if (!this.fixedBuffers.containsKey(rendertype)) {
               this.endBatch(rendertype);
            }

            this.lastState = Optional.empty();
         }

      }

      public void endBatch() {
         this.lastState.ifPresent((rendertype1) -> {
            VertexConsumer vertexconsumer = this.getBuffer(rendertype1);
            if (vertexconsumer == this.builder) {
               this.endBatch(rendertype1);
            }

         });

         for(RenderType rendertype : this.fixedBuffers.keySet()) {
            this.endBatch(rendertype);
         }

      }

      public void endBatch(RenderType rendertype) {
         BufferBuilder bufferbuilder = this.getBuilderRaw(rendertype);
         boolean flag = Objects.equals(this.lastState, rendertype.asOptional());
         if (flag || bufferbuilder != this.builder) {
            if (this.startedBuffers.remove(bufferbuilder)) {
               rendertype.end(bufferbuilder, RenderSystem.getVertexSorting());
               if (flag) {
                  this.lastState = Optional.empty();
               }

            }
         }
      }
   }
}
