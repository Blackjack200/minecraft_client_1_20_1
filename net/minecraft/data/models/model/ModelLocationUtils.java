package net.minecraft.data.models.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelLocationUtils {
   /** @deprecated */
   @Deprecated
   public static ResourceLocation decorateBlockModelLocation(String s) {
      return new ResourceLocation("minecraft", "block/" + s);
   }

   public static ResourceLocation decorateItemModelLocation(String s) {
      return new ResourceLocation("minecraft", "item/" + s);
   }

   public static ResourceLocation getModelLocation(Block block, String s) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(block);
      return resourcelocation.withPath((s2) -> "block/" + s2 + s);
   }

   public static ResourceLocation getModelLocation(Block block) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(block);
      return resourcelocation.withPrefix("block/");
   }

   public static ResourceLocation getModelLocation(Item item) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(item);
      return resourcelocation.withPrefix("item/");
   }

   public static ResourceLocation getModelLocation(Item item, String s) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(item);
      return resourcelocation.withPath((s2) -> "item/" + s2 + s);
   }
}
