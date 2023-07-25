package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;

public class RenderBuffers {
   private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
   private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (object2objectlinkedopenhashmap) -> {
      object2objectlinkedopenhashmap.put(Sheets.solidBlockSheet(), this.fixedBufferPack.builder(RenderType.solid()));
      object2objectlinkedopenhashmap.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.builder(RenderType.cutout()));
      object2objectlinkedopenhashmap.put(Sheets.bannerSheet(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
      object2objectlinkedopenhashmap.put(Sheets.translucentCullBlockSheet(), this.fixedBufferPack.builder(RenderType.translucent()));
      put(object2objectlinkedopenhashmap, Sheets.shieldSheet());
      put(object2objectlinkedopenhashmap, Sheets.bedSheet());
      put(object2objectlinkedopenhashmap, Sheets.shulkerBoxSheet());
      put(object2objectlinkedopenhashmap, Sheets.signSheet());
      put(object2objectlinkedopenhashmap, Sheets.hangingSignSheet());
      put(object2objectlinkedopenhashmap, Sheets.chestSheet());
      put(object2objectlinkedopenhashmap, RenderType.translucentNoCrumbling());
      put(object2objectlinkedopenhashmap, RenderType.armorGlint());
      put(object2objectlinkedopenhashmap, RenderType.armorEntityGlint());
      put(object2objectlinkedopenhashmap, RenderType.glint());
      put(object2objectlinkedopenhashmap, RenderType.glintDirect());
      put(object2objectlinkedopenhashmap, RenderType.glintTranslucent());
      put(object2objectlinkedopenhashmap, RenderType.entityGlint());
      put(object2objectlinkedopenhashmap, RenderType.entityGlintDirect());
      put(object2objectlinkedopenhashmap, RenderType.waterMask());
      ModelBakery.DESTROY_TYPES.forEach((rendertype) -> put(object2objectlinkedopenhashmap, rendertype));
   });
   private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
   private final MultiBufferSource.BufferSource crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
   private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

   private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> object2objectlinkedopenhashmap, RenderType rendertype) {
      object2objectlinkedopenhashmap.put(rendertype, new BufferBuilder(rendertype.bufferSize()));
   }

   public ChunkBufferBuilderPack fixedBufferPack() {
      return this.fixedBufferPack;
   }

   public MultiBufferSource.BufferSource bufferSource() {
      return this.bufferSource;
   }

   public MultiBufferSource.BufferSource crumblingBufferSource() {
      return this.crumblingBufferSource;
   }

   public OutlineBufferSource outlineBufferSource() {
      return this.outlineBufferSource;
   }
}
