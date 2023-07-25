package net.minecraft.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class TextureMapping {
   private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
   private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

   public TextureMapping put(TextureSlot textureslot, ResourceLocation resourcelocation) {
      this.slots.put(textureslot, resourcelocation);
      return this;
   }

   public TextureMapping putForced(TextureSlot textureslot, ResourceLocation resourcelocation) {
      this.slots.put(textureslot, resourcelocation);
      this.forcedSlots.add(textureslot);
      return this;
   }

   public Stream<TextureSlot> getForced() {
      return this.forcedSlots.stream();
   }

   public TextureMapping copySlot(TextureSlot textureslot, TextureSlot textureslot1) {
      this.slots.put(textureslot1, this.slots.get(textureslot));
      return this;
   }

   public TextureMapping copyForced(TextureSlot textureslot, TextureSlot textureslot1) {
      this.slots.put(textureslot1, this.slots.get(textureslot));
      this.forcedSlots.add(textureslot1);
      return this;
   }

   public ResourceLocation get(TextureSlot textureslot) {
      for(TextureSlot textureslot1 = textureslot; textureslot1 != null; textureslot1 = textureslot1.getParent()) {
         ResourceLocation resourcelocation = this.slots.get(textureslot1);
         if (resourcelocation != null) {
            return resourcelocation;
         }
      }

      throw new IllegalStateException("Can't find texture for slot " + textureslot);
   }

   public TextureMapping copyAndUpdate(TextureSlot textureslot, ResourceLocation resourcelocation) {
      TextureMapping texturemapping = new TextureMapping();
      texturemapping.slots.putAll(this.slots);
      texturemapping.forcedSlots.addAll(this.forcedSlots);
      texturemapping.put(textureslot, resourcelocation);
      return texturemapping;
   }

   public static TextureMapping cube(Block block) {
      ResourceLocation resourcelocation = getBlockTexture(block);
      return cube(resourcelocation);
   }

   public static TextureMapping defaultTexture(Block block) {
      ResourceLocation resourcelocation = getBlockTexture(block);
      return defaultTexture(resourcelocation);
   }

   public static TextureMapping defaultTexture(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, resourcelocation);
   }

   public static TextureMapping cube(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.ALL, resourcelocation);
   }

   public static TextureMapping cross(Block block) {
      return singleSlot(TextureSlot.CROSS, getBlockTexture(block));
   }

   public static TextureMapping cross(ResourceLocation resourcelocation) {
      return singleSlot(TextureSlot.CROSS, resourcelocation);
   }

   public static TextureMapping plant(Block block) {
      return singleSlot(TextureSlot.PLANT, getBlockTexture(block));
   }

   public static TextureMapping plant(ResourceLocation resourcelocation) {
      return singleSlot(TextureSlot.PLANT, resourcelocation);
   }

   public static TextureMapping rail(Block block) {
      return singleSlot(TextureSlot.RAIL, getBlockTexture(block));
   }

   public static TextureMapping rail(ResourceLocation resourcelocation) {
      return singleSlot(TextureSlot.RAIL, resourcelocation);
   }

   public static TextureMapping wool(Block block) {
      return singleSlot(TextureSlot.WOOL, getBlockTexture(block));
   }

   public static TextureMapping flowerbed(Block block) {
      return (new TextureMapping()).put(TextureSlot.FLOWERBED, getBlockTexture(block)).put(TextureSlot.STEM, getBlockTexture(block, "_stem"));
   }

   public static TextureMapping wool(ResourceLocation resourcelocation) {
      return singleSlot(TextureSlot.WOOL, resourcelocation);
   }

   public static TextureMapping stem(Block block) {
      return singleSlot(TextureSlot.STEM, getBlockTexture(block));
   }

   public static TextureMapping attachedStem(Block block, Block block1) {
      return (new TextureMapping()).put(TextureSlot.STEM, getBlockTexture(block)).put(TextureSlot.UPPER_STEM, getBlockTexture(block1));
   }

   public static TextureMapping pattern(Block block) {
      return singleSlot(TextureSlot.PATTERN, getBlockTexture(block));
   }

   public static TextureMapping fan(Block block) {
      return singleSlot(TextureSlot.FAN, getBlockTexture(block));
   }

   public static TextureMapping crop(ResourceLocation resourcelocation) {
      return singleSlot(TextureSlot.CROP, resourcelocation);
   }

   public static TextureMapping pane(Block block, Block block1) {
      return (new TextureMapping()).put(TextureSlot.PANE, getBlockTexture(block)).put(TextureSlot.EDGE, getBlockTexture(block1, "_top"));
   }

   public static TextureMapping singleSlot(TextureSlot textureslot, ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(textureslot, resourcelocation);
   }

   public static TextureMapping column(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.END, getBlockTexture(block, "_top"));
   }

   public static TextureMapping cubeTop(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping pottedAzalea(Block block) {
      return (new TextureMapping()).put(TextureSlot.PLANT, getBlockTexture(block, "_plant")).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping logColumn(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block)).put(TextureSlot.END, getBlockTexture(block, "_top")).put(TextureSlot.PARTICLE, getBlockTexture(block));
   }

   public static TextureMapping column(ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      return (new TextureMapping()).put(TextureSlot.SIDE, resourcelocation).put(TextureSlot.END, resourcelocation1);
   }

   public static TextureMapping fence(Block block) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, getBlockTexture(block)).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping customParticle(Block block) {
      return (new TextureMapping()).put(TextureSlot.TEXTURE, getBlockTexture(block)).put(TextureSlot.PARTICLE, getBlockTexture(block, "_particle"));
   }

   public static TextureMapping cubeBottomTop(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping cubeBottomTopWithWall(Block block) {
      ResourceLocation resourcelocation = getBlockTexture(block);
      return (new TextureMapping()).put(TextureSlot.WALL, resourcelocation).put(TextureSlot.SIDE, resourcelocation).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping columnWithWall(Block block) {
      ResourceLocation resourcelocation = getBlockTexture(block);
      return (new TextureMapping()).put(TextureSlot.TEXTURE, resourcelocation).put(TextureSlot.WALL, resourcelocation).put(TextureSlot.SIDE, resourcelocation).put(TextureSlot.END, getBlockTexture(block, "_top"));
   }

   public static TextureMapping door(ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      return (new TextureMapping()).put(TextureSlot.TOP, resourcelocation).put(TextureSlot.BOTTOM, resourcelocation1);
   }

   public static TextureMapping door(Block block) {
      return (new TextureMapping()).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping particle(Block block) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(block));
   }

   public static TextureMapping particle(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, resourcelocation);
   }

   public static TextureMapping fire0(Block block) {
      return (new TextureMapping()).put(TextureSlot.FIRE, getBlockTexture(block, "_0"));
   }

   public static TextureMapping fire1(Block block) {
      return (new TextureMapping()).put(TextureSlot.FIRE, getBlockTexture(block, "_1"));
   }

   public static TextureMapping lantern(Block block) {
      return (new TextureMapping()).put(TextureSlot.LANTERN, getBlockTexture(block));
   }

   public static TextureMapping torch(Block block) {
      return (new TextureMapping()).put(TextureSlot.TORCH, getBlockTexture(block));
   }

   public static TextureMapping torch(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.TORCH, resourcelocation);
   }

   public static TextureMapping particleFromItem(Item item) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getItemTexture(item));
   }

   public static TextureMapping commandBlock(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.BACK, getBlockTexture(block, "_back"));
   }

   public static TextureMapping orientableCube(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.TOP, getBlockTexture(block, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(block, "_bottom"));
   }

   public static TextureMapping orientableCubeOnlyTop(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping orientableCubeSameEnds(Block block) {
      return (new TextureMapping()).put(TextureSlot.SIDE, getBlockTexture(block, "_side")).put(TextureSlot.FRONT, getBlockTexture(block, "_front")).put(TextureSlot.END, getBlockTexture(block, "_end"));
   }

   public static TextureMapping top(Block block) {
      return (new TextureMapping()).put(TextureSlot.TOP, getBlockTexture(block, "_top"));
   }

   public static TextureMapping craftingTable(Block block, Block block1) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(block, "_front")).put(TextureSlot.DOWN, getBlockTexture(block1)).put(TextureSlot.UP, getBlockTexture(block, "_top")).put(TextureSlot.NORTH, getBlockTexture(block, "_front")).put(TextureSlot.EAST, getBlockTexture(block, "_side")).put(TextureSlot.SOUTH, getBlockTexture(block, "_side")).put(TextureSlot.WEST, getBlockTexture(block, "_front"));
   }

   public static TextureMapping fletchingTable(Block block, Block block1) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(block, "_front")).put(TextureSlot.DOWN, getBlockTexture(block1)).put(TextureSlot.UP, getBlockTexture(block, "_top")).put(TextureSlot.NORTH, getBlockTexture(block, "_front")).put(TextureSlot.SOUTH, getBlockTexture(block, "_front")).put(TextureSlot.EAST, getBlockTexture(block, "_side")).put(TextureSlot.WEST, getBlockTexture(block, "_side"));
   }

   public static TextureMapping snifferEgg(String s) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SNIFFER_EGG, s + "_north")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SNIFFER_EGG, s + "_bottom")).put(TextureSlot.TOP, getBlockTexture(Blocks.SNIFFER_EGG, s + "_top")).put(TextureSlot.NORTH, getBlockTexture(Blocks.SNIFFER_EGG, s + "_north")).put(TextureSlot.SOUTH, getBlockTexture(Blocks.SNIFFER_EGG, s + "_south")).put(TextureSlot.EAST, getBlockTexture(Blocks.SNIFFER_EGG, s + "_east")).put(TextureSlot.WEST, getBlockTexture(Blocks.SNIFFER_EGG, s + "_west"));
   }

   public static TextureMapping campfire(Block block) {
      return (new TextureMapping()).put(TextureSlot.LIT_LOG, getBlockTexture(block, "_log_lit")).put(TextureSlot.FIRE, getBlockTexture(block, "_fire"));
   }

   public static TextureMapping candleCake(Block block, boolean flag) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom")).put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top")).put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side")).put(TextureSlot.CANDLE, getBlockTexture(block, flag ? "_lit" : ""));
   }

   public static TextureMapping cauldron(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side")).put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom")).put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner")).put(TextureSlot.CONTENT, resourcelocation);
   }

   public static TextureMapping sculkShrieker(boolean flag) {
      String s = flag ? "_can_summon" : "";
      return (new TextureMapping()).put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom")).put(TextureSlot.SIDE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_side")).put(TextureSlot.TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, "_top")).put(TextureSlot.INNER_TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, s + "_inner_top")).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
   }

   public static TextureMapping layer0(Item item) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, getItemTexture(item));
   }

   public static TextureMapping layer0(Block block) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, getBlockTexture(block));
   }

   public static TextureMapping layer0(ResourceLocation resourcelocation) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, resourcelocation);
   }

   public static TextureMapping layered(ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, resourcelocation).put(TextureSlot.LAYER1, resourcelocation1);
   }

   public static TextureMapping layered(ResourceLocation resourcelocation, ResourceLocation resourcelocation1, ResourceLocation resourcelocation2) {
      return (new TextureMapping()).put(TextureSlot.LAYER0, resourcelocation).put(TextureSlot.LAYER1, resourcelocation1).put(TextureSlot.LAYER2, resourcelocation2);
   }

   public static ResourceLocation getBlockTexture(Block block) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(block);
      return resourcelocation.withPrefix("block/");
   }

   public static ResourceLocation getBlockTexture(Block block, String s) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(block);
      return resourcelocation.withPath((s2) -> "block/" + s2 + s);
   }

   public static ResourceLocation getItemTexture(Item item) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(item);
      return resourcelocation.withPrefix("item/");
   }

   public static ResourceLocation getItemTexture(Item item, String s) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(item);
      return resourcelocation.withPath((s2) -> "item/" + s2 + s);
   }
}
