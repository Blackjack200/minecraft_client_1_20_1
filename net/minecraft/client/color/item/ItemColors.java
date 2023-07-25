package net.minecraft.client.color.item;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemColors {
   private static final int DEFAULT = -1;
   private final IdMapper<ItemColor> itemColors = new IdMapper<>(32);

   public static ItemColors createDefault(BlockColors blockcolors) {
      ItemColors itemcolors = new ItemColors();
      itemcolors.register((itemstack8, i3) -> i3 > 0 ? -1 : ((DyeableLeatherItem)itemstack8.getItem()).getColor(itemstack8), Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS, Items.LEATHER_HORSE_ARMOR);
      itemcolors.register((itemstack7, l2) -> GrassColor.get(0.5D, 1.0D), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
      itemcolors.register((itemstack6, k1) -> {
         if (k1 != 1) {
            return -1;
         } else {
            CompoundTag compoundtag = itemstack6.getTagElement("Explosion");
            int[] aint = compoundtag != null && compoundtag.contains("Colors", 11) ? compoundtag.getIntArray("Colors") : null;
            if (aint != null && aint.length != 0) {
               if (aint.length == 1) {
                  return aint[0];
               } else {
                  int l1 = 0;
                  int i2 = 0;
                  int j2 = 0;

                  for(int k2 : aint) {
                     l1 += (k2 & 16711680) >> 16;
                     i2 += (k2 & '\uff00') >> 8;
                     j2 += (k2 & 255) >> 0;
                  }

                  l1 /= aint.length;
                  i2 /= aint.length;
                  j2 /= aint.length;
                  return l1 << 16 | i2 << 8 | j2;
               }
            } else {
               return 9079434;
            }
         }
      }, Items.FIREWORK_STAR);
      itemcolors.register((itemstack5, j1) -> j1 > 0 ? -1 : PotionUtils.getColor(itemstack5), Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

      for(SpawnEggItem spawneggitem : SpawnEggItem.eggs()) {
         itemcolors.register((itemstack4, i1) -> spawneggitem.getColor(i1), spawneggitem);
      }

      itemcolors.register((itemstack3, l) -> {
         BlockState blockstate = ((BlockItem)itemstack3.getItem()).getBlock().defaultBlockState();
         return blockcolors.getColor(blockstate, (BlockAndTintGetter)null, (BlockPos)null, l);
      }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.FERN, Blocks.VINE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.LILY_PAD);
      itemcolors.register((itemstack2, k) -> FoliageColor.getMangroveColor(), Blocks.MANGROVE_LEAVES);
      itemcolors.register((itemstack1, j) -> j == 0 ? PotionUtils.getColor(itemstack1) : -1, Items.TIPPED_ARROW);
      itemcolors.register((itemstack, i) -> i == 0 ? -1 : MapItem.getColor(itemstack), Items.FILLED_MAP);
      return itemcolors;
   }

   public int getColor(ItemStack itemstack, int i) {
      ItemColor itemcolor = this.itemColors.byId(BuiltInRegistries.ITEM.getId(itemstack.getItem()));
      return itemcolor == null ? -1 : itemcolor.getColor(itemstack, i);
   }

   public void register(ItemColor itemcolor, ItemLike... aitemlike) {
      for(ItemLike itemlike : aitemlike) {
         this.itemColors.addMapping(itemcolor, Item.getId(itemlike.asItem()));
      }

   }
}
