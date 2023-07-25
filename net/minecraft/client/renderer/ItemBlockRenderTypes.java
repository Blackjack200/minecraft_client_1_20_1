package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ItemBlockRenderTypes {
   private static final Map<Block, RenderType> TYPE_BY_BLOCK = Util.make(Maps.newHashMap(), (hashmap) -> {
      RenderType rendertype = RenderType.tripwire();
      hashmap.put(Blocks.TRIPWIRE, rendertype);
      RenderType rendertype1 = RenderType.cutoutMipped();
      hashmap.put(Blocks.GRASS_BLOCK, rendertype1);
      hashmap.put(Blocks.IRON_BARS, rendertype1);
      hashmap.put(Blocks.GLASS_PANE, rendertype1);
      hashmap.put(Blocks.TRIPWIRE_HOOK, rendertype1);
      hashmap.put(Blocks.HOPPER, rendertype1);
      hashmap.put(Blocks.CHAIN, rendertype1);
      hashmap.put(Blocks.JUNGLE_LEAVES, rendertype1);
      hashmap.put(Blocks.OAK_LEAVES, rendertype1);
      hashmap.put(Blocks.SPRUCE_LEAVES, rendertype1);
      hashmap.put(Blocks.ACACIA_LEAVES, rendertype1);
      hashmap.put(Blocks.CHERRY_LEAVES, rendertype1);
      hashmap.put(Blocks.BIRCH_LEAVES, rendertype1);
      hashmap.put(Blocks.DARK_OAK_LEAVES, rendertype1);
      hashmap.put(Blocks.AZALEA_LEAVES, rendertype1);
      hashmap.put(Blocks.FLOWERING_AZALEA_LEAVES, rendertype1);
      hashmap.put(Blocks.MANGROVE_ROOTS, rendertype1);
      hashmap.put(Blocks.MANGROVE_LEAVES, rendertype1);
      RenderType rendertype2 = RenderType.cutout();
      hashmap.put(Blocks.OAK_SAPLING, rendertype2);
      hashmap.put(Blocks.SPRUCE_SAPLING, rendertype2);
      hashmap.put(Blocks.BIRCH_SAPLING, rendertype2);
      hashmap.put(Blocks.JUNGLE_SAPLING, rendertype2);
      hashmap.put(Blocks.ACACIA_SAPLING, rendertype2);
      hashmap.put(Blocks.CHERRY_SAPLING, rendertype2);
      hashmap.put(Blocks.DARK_OAK_SAPLING, rendertype2);
      hashmap.put(Blocks.GLASS, rendertype2);
      hashmap.put(Blocks.WHITE_BED, rendertype2);
      hashmap.put(Blocks.ORANGE_BED, rendertype2);
      hashmap.put(Blocks.MAGENTA_BED, rendertype2);
      hashmap.put(Blocks.LIGHT_BLUE_BED, rendertype2);
      hashmap.put(Blocks.YELLOW_BED, rendertype2);
      hashmap.put(Blocks.LIME_BED, rendertype2);
      hashmap.put(Blocks.PINK_BED, rendertype2);
      hashmap.put(Blocks.GRAY_BED, rendertype2);
      hashmap.put(Blocks.LIGHT_GRAY_BED, rendertype2);
      hashmap.put(Blocks.CYAN_BED, rendertype2);
      hashmap.put(Blocks.PURPLE_BED, rendertype2);
      hashmap.put(Blocks.BLUE_BED, rendertype2);
      hashmap.put(Blocks.BROWN_BED, rendertype2);
      hashmap.put(Blocks.GREEN_BED, rendertype2);
      hashmap.put(Blocks.RED_BED, rendertype2);
      hashmap.put(Blocks.BLACK_BED, rendertype2);
      hashmap.put(Blocks.POWERED_RAIL, rendertype2);
      hashmap.put(Blocks.DETECTOR_RAIL, rendertype2);
      hashmap.put(Blocks.COBWEB, rendertype2);
      hashmap.put(Blocks.GRASS, rendertype2);
      hashmap.put(Blocks.FERN, rendertype2);
      hashmap.put(Blocks.DEAD_BUSH, rendertype2);
      hashmap.put(Blocks.SEAGRASS, rendertype2);
      hashmap.put(Blocks.TALL_SEAGRASS, rendertype2);
      hashmap.put(Blocks.DANDELION, rendertype2);
      hashmap.put(Blocks.POPPY, rendertype2);
      hashmap.put(Blocks.BLUE_ORCHID, rendertype2);
      hashmap.put(Blocks.ALLIUM, rendertype2);
      hashmap.put(Blocks.AZURE_BLUET, rendertype2);
      hashmap.put(Blocks.RED_TULIP, rendertype2);
      hashmap.put(Blocks.ORANGE_TULIP, rendertype2);
      hashmap.put(Blocks.WHITE_TULIP, rendertype2);
      hashmap.put(Blocks.PINK_TULIP, rendertype2);
      hashmap.put(Blocks.OXEYE_DAISY, rendertype2);
      hashmap.put(Blocks.CORNFLOWER, rendertype2);
      hashmap.put(Blocks.WITHER_ROSE, rendertype2);
      hashmap.put(Blocks.LILY_OF_THE_VALLEY, rendertype2);
      hashmap.put(Blocks.BROWN_MUSHROOM, rendertype2);
      hashmap.put(Blocks.RED_MUSHROOM, rendertype2);
      hashmap.put(Blocks.TORCH, rendertype2);
      hashmap.put(Blocks.WALL_TORCH, rendertype2);
      hashmap.put(Blocks.SOUL_TORCH, rendertype2);
      hashmap.put(Blocks.SOUL_WALL_TORCH, rendertype2);
      hashmap.put(Blocks.FIRE, rendertype2);
      hashmap.put(Blocks.SOUL_FIRE, rendertype2);
      hashmap.put(Blocks.SPAWNER, rendertype2);
      hashmap.put(Blocks.REDSTONE_WIRE, rendertype2);
      hashmap.put(Blocks.WHEAT, rendertype2);
      hashmap.put(Blocks.OAK_DOOR, rendertype2);
      hashmap.put(Blocks.LADDER, rendertype2);
      hashmap.put(Blocks.RAIL, rendertype2);
      hashmap.put(Blocks.IRON_DOOR, rendertype2);
      hashmap.put(Blocks.REDSTONE_TORCH, rendertype2);
      hashmap.put(Blocks.REDSTONE_WALL_TORCH, rendertype2);
      hashmap.put(Blocks.CACTUS, rendertype2);
      hashmap.put(Blocks.SUGAR_CANE, rendertype2);
      hashmap.put(Blocks.REPEATER, rendertype2);
      hashmap.put(Blocks.OAK_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.SPRUCE_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.BIRCH_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.JUNGLE_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.ACACIA_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.CHERRY_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.DARK_OAK_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.CRIMSON_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.WARPED_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.MANGROVE_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.BAMBOO_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.ATTACHED_PUMPKIN_STEM, rendertype2);
      hashmap.put(Blocks.ATTACHED_MELON_STEM, rendertype2);
      hashmap.put(Blocks.PUMPKIN_STEM, rendertype2);
      hashmap.put(Blocks.MELON_STEM, rendertype2);
      hashmap.put(Blocks.VINE, rendertype2);
      hashmap.put(Blocks.GLOW_LICHEN, rendertype2);
      hashmap.put(Blocks.LILY_PAD, rendertype2);
      hashmap.put(Blocks.NETHER_WART, rendertype2);
      hashmap.put(Blocks.BREWING_STAND, rendertype2);
      hashmap.put(Blocks.COCOA, rendertype2);
      hashmap.put(Blocks.BEACON, rendertype2);
      hashmap.put(Blocks.FLOWER_POT, rendertype2);
      hashmap.put(Blocks.POTTED_OAK_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_SPRUCE_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_BIRCH_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_JUNGLE_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_ACACIA_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_CHERRY_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_DARK_OAK_SAPLING, rendertype2);
      hashmap.put(Blocks.POTTED_MANGROVE_PROPAGULE, rendertype2);
      hashmap.put(Blocks.POTTED_FERN, rendertype2);
      hashmap.put(Blocks.POTTED_DANDELION, rendertype2);
      hashmap.put(Blocks.POTTED_POPPY, rendertype2);
      hashmap.put(Blocks.POTTED_BLUE_ORCHID, rendertype2);
      hashmap.put(Blocks.POTTED_ALLIUM, rendertype2);
      hashmap.put(Blocks.POTTED_AZURE_BLUET, rendertype2);
      hashmap.put(Blocks.POTTED_RED_TULIP, rendertype2);
      hashmap.put(Blocks.POTTED_ORANGE_TULIP, rendertype2);
      hashmap.put(Blocks.POTTED_WHITE_TULIP, rendertype2);
      hashmap.put(Blocks.POTTED_PINK_TULIP, rendertype2);
      hashmap.put(Blocks.POTTED_OXEYE_DAISY, rendertype2);
      hashmap.put(Blocks.POTTED_CORNFLOWER, rendertype2);
      hashmap.put(Blocks.POTTED_LILY_OF_THE_VALLEY, rendertype2);
      hashmap.put(Blocks.POTTED_WITHER_ROSE, rendertype2);
      hashmap.put(Blocks.POTTED_RED_MUSHROOM, rendertype2);
      hashmap.put(Blocks.POTTED_BROWN_MUSHROOM, rendertype2);
      hashmap.put(Blocks.POTTED_DEAD_BUSH, rendertype2);
      hashmap.put(Blocks.POTTED_CACTUS, rendertype2);
      hashmap.put(Blocks.POTTED_AZALEA, rendertype2);
      hashmap.put(Blocks.POTTED_FLOWERING_AZALEA, rendertype2);
      hashmap.put(Blocks.POTTED_TORCHFLOWER, rendertype2);
      hashmap.put(Blocks.CARROTS, rendertype2);
      hashmap.put(Blocks.POTATOES, rendertype2);
      hashmap.put(Blocks.COMPARATOR, rendertype2);
      hashmap.put(Blocks.ACTIVATOR_RAIL, rendertype2);
      hashmap.put(Blocks.IRON_TRAPDOOR, rendertype2);
      hashmap.put(Blocks.SUNFLOWER, rendertype2);
      hashmap.put(Blocks.LILAC, rendertype2);
      hashmap.put(Blocks.ROSE_BUSH, rendertype2);
      hashmap.put(Blocks.PEONY, rendertype2);
      hashmap.put(Blocks.TALL_GRASS, rendertype2);
      hashmap.put(Blocks.LARGE_FERN, rendertype2);
      hashmap.put(Blocks.SPRUCE_DOOR, rendertype2);
      hashmap.put(Blocks.BIRCH_DOOR, rendertype2);
      hashmap.put(Blocks.JUNGLE_DOOR, rendertype2);
      hashmap.put(Blocks.ACACIA_DOOR, rendertype2);
      hashmap.put(Blocks.CHERRY_DOOR, rendertype2);
      hashmap.put(Blocks.DARK_OAK_DOOR, rendertype2);
      hashmap.put(Blocks.MANGROVE_DOOR, rendertype2);
      hashmap.put(Blocks.BAMBOO_DOOR, rendertype2);
      hashmap.put(Blocks.END_ROD, rendertype2);
      hashmap.put(Blocks.CHORUS_PLANT, rendertype2);
      hashmap.put(Blocks.CHORUS_FLOWER, rendertype2);
      hashmap.put(Blocks.TORCHFLOWER, rendertype2);
      hashmap.put(Blocks.TORCHFLOWER_CROP, rendertype2);
      hashmap.put(Blocks.PITCHER_PLANT, rendertype2);
      hashmap.put(Blocks.PITCHER_CROP, rendertype2);
      hashmap.put(Blocks.BEETROOTS, rendertype2);
      hashmap.put(Blocks.KELP, rendertype2);
      hashmap.put(Blocks.KELP_PLANT, rendertype2);
      hashmap.put(Blocks.TURTLE_EGG, rendertype2);
      hashmap.put(Blocks.DEAD_TUBE_CORAL, rendertype2);
      hashmap.put(Blocks.DEAD_BRAIN_CORAL, rendertype2);
      hashmap.put(Blocks.DEAD_BUBBLE_CORAL, rendertype2);
      hashmap.put(Blocks.DEAD_FIRE_CORAL, rendertype2);
      hashmap.put(Blocks.DEAD_HORN_CORAL, rendertype2);
      hashmap.put(Blocks.TUBE_CORAL, rendertype2);
      hashmap.put(Blocks.BRAIN_CORAL, rendertype2);
      hashmap.put(Blocks.BUBBLE_CORAL, rendertype2);
      hashmap.put(Blocks.FIRE_CORAL, rendertype2);
      hashmap.put(Blocks.HORN_CORAL, rendertype2);
      hashmap.put(Blocks.DEAD_TUBE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_BRAIN_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_BUBBLE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_FIRE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_HORN_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.TUBE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.BRAIN_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.BUBBLE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.FIRE_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.HORN_CORAL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.TUBE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.BRAIN_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.BUBBLE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.FIRE_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.HORN_CORAL_WALL_FAN, rendertype2);
      hashmap.put(Blocks.SEA_PICKLE, rendertype2);
      hashmap.put(Blocks.CONDUIT, rendertype2);
      hashmap.put(Blocks.BAMBOO_SAPLING, rendertype2);
      hashmap.put(Blocks.BAMBOO, rendertype2);
      hashmap.put(Blocks.POTTED_BAMBOO, rendertype2);
      hashmap.put(Blocks.SCAFFOLDING, rendertype2);
      hashmap.put(Blocks.STONECUTTER, rendertype2);
      hashmap.put(Blocks.LANTERN, rendertype2);
      hashmap.put(Blocks.SOUL_LANTERN, rendertype2);
      hashmap.put(Blocks.CAMPFIRE, rendertype2);
      hashmap.put(Blocks.SOUL_CAMPFIRE, rendertype2);
      hashmap.put(Blocks.SWEET_BERRY_BUSH, rendertype2);
      hashmap.put(Blocks.WEEPING_VINES, rendertype2);
      hashmap.put(Blocks.WEEPING_VINES_PLANT, rendertype2);
      hashmap.put(Blocks.TWISTING_VINES, rendertype2);
      hashmap.put(Blocks.TWISTING_VINES_PLANT, rendertype2);
      hashmap.put(Blocks.NETHER_SPROUTS, rendertype2);
      hashmap.put(Blocks.CRIMSON_FUNGUS, rendertype2);
      hashmap.put(Blocks.WARPED_FUNGUS, rendertype2);
      hashmap.put(Blocks.CRIMSON_ROOTS, rendertype2);
      hashmap.put(Blocks.WARPED_ROOTS, rendertype2);
      hashmap.put(Blocks.POTTED_CRIMSON_FUNGUS, rendertype2);
      hashmap.put(Blocks.POTTED_WARPED_FUNGUS, rendertype2);
      hashmap.put(Blocks.POTTED_CRIMSON_ROOTS, rendertype2);
      hashmap.put(Blocks.POTTED_WARPED_ROOTS, rendertype2);
      hashmap.put(Blocks.CRIMSON_DOOR, rendertype2);
      hashmap.put(Blocks.WARPED_DOOR, rendertype2);
      hashmap.put(Blocks.POINTED_DRIPSTONE, rendertype2);
      hashmap.put(Blocks.SMALL_AMETHYST_BUD, rendertype2);
      hashmap.put(Blocks.MEDIUM_AMETHYST_BUD, rendertype2);
      hashmap.put(Blocks.LARGE_AMETHYST_BUD, rendertype2);
      hashmap.put(Blocks.AMETHYST_CLUSTER, rendertype2);
      hashmap.put(Blocks.LIGHTNING_ROD, rendertype2);
      hashmap.put(Blocks.CAVE_VINES, rendertype2);
      hashmap.put(Blocks.CAVE_VINES_PLANT, rendertype2);
      hashmap.put(Blocks.SPORE_BLOSSOM, rendertype2);
      hashmap.put(Blocks.FLOWERING_AZALEA, rendertype2);
      hashmap.put(Blocks.AZALEA, rendertype2);
      hashmap.put(Blocks.MOSS_CARPET, rendertype2);
      hashmap.put(Blocks.PINK_PETALS, rendertype2);
      hashmap.put(Blocks.BIG_DRIPLEAF, rendertype2);
      hashmap.put(Blocks.BIG_DRIPLEAF_STEM, rendertype2);
      hashmap.put(Blocks.SMALL_DRIPLEAF, rendertype2);
      hashmap.put(Blocks.HANGING_ROOTS, rendertype2);
      hashmap.put(Blocks.SCULK_SENSOR, rendertype2);
      hashmap.put(Blocks.CALIBRATED_SCULK_SENSOR, rendertype2);
      hashmap.put(Blocks.SCULK_VEIN, rendertype2);
      hashmap.put(Blocks.SCULK_SHRIEKER, rendertype2);
      hashmap.put(Blocks.MANGROVE_PROPAGULE, rendertype2);
      hashmap.put(Blocks.MANGROVE_LOG, rendertype2);
      hashmap.put(Blocks.FROGSPAWN, rendertype2);
      RenderType rendertype3 = RenderType.translucent();
      hashmap.put(Blocks.ICE, rendertype3);
      hashmap.put(Blocks.NETHER_PORTAL, rendertype3);
      hashmap.put(Blocks.WHITE_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.ORANGE_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.MAGENTA_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.YELLOW_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.LIME_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.PINK_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.GRAY_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.CYAN_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.PURPLE_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.BLUE_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.BROWN_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.GREEN_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.RED_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.BLACK_STAINED_GLASS, rendertype3);
      hashmap.put(Blocks.WHITE_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.ORANGE_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.MAGENTA_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.YELLOW_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.LIME_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.PINK_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.GRAY_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.CYAN_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.PURPLE_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.BLUE_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.BROWN_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.GREEN_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.RED_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.BLACK_STAINED_GLASS_PANE, rendertype3);
      hashmap.put(Blocks.SLIME_BLOCK, rendertype3);
      hashmap.put(Blocks.HONEY_BLOCK, rendertype3);
      hashmap.put(Blocks.FROSTED_ICE, rendertype3);
      hashmap.put(Blocks.BUBBLE_COLUMN, rendertype3);
      hashmap.put(Blocks.TINTED_GLASS, rendertype3);
   });
   private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.newHashMap(), (hashmap) -> {
      RenderType rendertype = RenderType.translucent();
      hashmap.put(Fluids.FLOWING_WATER, rendertype);
      hashmap.put(Fluids.WATER, rendertype);
   });
   private static boolean renderCutout;

   public static RenderType getChunkRenderType(BlockState blockstate) {
      Block block = blockstate.getBlock();
      if (block instanceof LeavesBlock) {
         return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
      } else {
         RenderType rendertype = TYPE_BY_BLOCK.get(block);
         return rendertype != null ? rendertype : RenderType.solid();
      }
   }

   public static RenderType getMovingBlockRenderType(BlockState blockstate) {
      Block block = blockstate.getBlock();
      if (block instanceof LeavesBlock) {
         return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
      } else {
         RenderType rendertype = TYPE_BY_BLOCK.get(block);
         if (rendertype != null) {
            return rendertype == RenderType.translucent() ? RenderType.translucentMovingBlock() : rendertype;
         } else {
            return RenderType.solid();
         }
      }
   }

   public static RenderType getRenderType(BlockState blockstate, boolean flag) {
      RenderType rendertype = getChunkRenderType(blockstate);
      if (rendertype == RenderType.translucent()) {
         if (!Minecraft.useShaderTransparency()) {
            return Sheets.translucentCullBlockSheet();
         } else {
            return flag ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
         }
      } else {
         return Sheets.cutoutBlockSheet();
      }
   }

   public static RenderType getRenderType(ItemStack itemstack, boolean flag) {
      Item item = itemstack.getItem();
      if (item instanceof BlockItem) {
         Block block = ((BlockItem)item).getBlock();
         return getRenderType(block.defaultBlockState(), flag);
      } else {
         return flag ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
      }
   }

   public static RenderType getRenderLayer(FluidState fluidstate) {
      RenderType rendertype = TYPE_BY_FLUID.get(fluidstate.getType());
      return rendertype != null ? rendertype : RenderType.solid();
   }

   public static void setFancy(boolean flag) {
      renderCutout = flag;
   }
}
