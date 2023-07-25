package net.minecraft.core.cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CauldronInteraction {
   Map<Item, CauldronInteraction> EMPTY = newInteractionMap();
   Map<Item, CauldronInteraction> WATER = newInteractionMap();
   Map<Item, CauldronInteraction> LAVA = newInteractionMap();
   Map<Item, CauldronInteraction> POWDER_SNOW = newInteractionMap();
   CauldronInteraction FILL_WATER = (blockstate, level, blockpos, player, interactionhand, itemstack) -> emptyBucket(level, blockpos, player, interactionhand, itemstack, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)), SoundEvents.BUCKET_EMPTY);
   CauldronInteraction FILL_LAVA = (blockstate, level, blockpos, player, interactionhand, itemstack) -> emptyBucket(level, blockpos, player, interactionhand, itemstack, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
   CauldronInteraction FILL_POWDER_SNOW = (blockstate, level, blockpos, player, interactionhand, itemstack) -> emptyBucket(level, blockpos, player, interactionhand, itemstack, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
   CauldronInteraction SHULKER_BOX = (blockstate, level, blockpos, player, interactionhand, itemstack) -> {
      Block block = Block.byItem(itemstack.getItem());
      if (!(block instanceof ShulkerBoxBlock)) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            ItemStack itemstack1 = new ItemStack(Blocks.SHULKER_BOX);
            if (itemstack.hasTag()) {
               itemstack1.setTag(itemstack.getTag().copy());
            }

            player.setItemInHand(interactionhand, itemstack1);
            player.awardStat(Stats.CLEAN_SHULKER_BOX);
            LayeredCauldronBlock.lowerFillLevel(blockstate, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   };
   CauldronInteraction BANNER = (blockstate, level, blockpos, player, interactionhand, itemstack) -> {
      if (BannerBlockEntity.getPatternCount(itemstack) <= 0) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            ItemStack itemstack1 = itemstack.copyWithCount(1);
            BannerBlockEntity.removeLastPattern(itemstack1);
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            if (itemstack.isEmpty()) {
               player.setItemInHand(interactionhand, itemstack1);
            } else if (player.getInventory().add(itemstack1)) {
               player.inventoryMenu.sendAllDataToRemote();
            } else {
               player.drop(itemstack1, false);
            }

            player.awardStat(Stats.CLEAN_BANNER);
            LayeredCauldronBlock.lowerFillLevel(blockstate, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   };
   CauldronInteraction DYED_ITEM = (blockstate, level, blockpos, player, interactionhand, itemstack) -> {
      Item item = itemstack.getItem();
      if (!(item instanceof DyeableLeatherItem dyeableleatheritem)) {
         return InteractionResult.PASS;
      } else if (!dyeableleatheritem.hasCustomColor(itemstack)) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            dyeableleatheritem.clearColor(itemstack);
            player.awardStat(Stats.CLEAN_ARMOR);
            LayeredCauldronBlock.lowerFillLevel(blockstate, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   };

   static Object2ObjectOpenHashMap<Item, CauldronInteraction> newInteractionMap() {
      return Util.make(new Object2ObjectOpenHashMap<>(), (object2objectopenhashmap) -> object2objectopenhashmap.defaultReturnValue((blockstate, level, blockpos, player, interactionhand, itemstack) -> InteractionResult.PASS));
   }

   InteractionResult interact(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, ItemStack itemstack);

   static void bootStrap() {
      addDefaultInteractions(EMPTY);
      EMPTY.put(Items.POTION, (blockstate8, level5, blockpos5, player5, interactionhand5, itemstack5) -> {
         if (PotionUtils.getPotion(itemstack5) != Potions.WATER) {
            return InteractionResult.PASS;
         } else {
            if (!level5.isClientSide) {
               Item item1 = itemstack5.getItem();
               player5.setItemInHand(interactionhand5, ItemUtils.createFilledResult(itemstack5, player5, new ItemStack(Items.GLASS_BOTTLE)));
               player5.awardStat(Stats.USE_CAULDRON);
               player5.awardStat(Stats.ITEM_USED.get(item1));
               level5.setBlockAndUpdate(blockpos5, Blocks.WATER_CAULDRON.defaultBlockState());
               level5.playSound((Player)null, blockpos5, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
               level5.gameEvent((Entity)null, GameEvent.FLUID_PLACE, blockpos5);
            }

            return InteractionResult.sidedSuccess(level5.isClientSide);
         }
      });
      addDefaultInteractions(WATER);
      WATER.put(Items.BUCKET, (blockstate6, level4, blockpos4, player4, interactionhand4, itemstack4) -> fillBucket(blockstate6, level4, blockpos4, player4, interactionhand4, itemstack4, new ItemStack(Items.WATER_BUCKET), (blockstate7) -> blockstate7.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
      WATER.put(Items.GLASS_BOTTLE, (blockstate5, level3, blockpos3, player3, interactionhand3, itemstack3) -> {
         if (!level3.isClientSide) {
            Item item = itemstack3.getItem();
            player3.setItemInHand(interactionhand3, ItemUtils.createFilledResult(itemstack3, player3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
            player3.awardStat(Stats.USE_CAULDRON);
            player3.awardStat(Stats.ITEM_USED.get(item));
            LayeredCauldronBlock.lowerFillLevel(blockstate5, level3, blockpos3);
            level3.playSound((Player)null, blockpos3, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level3.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos3);
         }

         return InteractionResult.sidedSuccess(level3.isClientSide);
      });
      WATER.put(Items.POTION, (blockstate4, level2, blockpos2, player2, interactionhand2, itemstack2) -> {
         if (blockstate4.getValue(LayeredCauldronBlock.LEVEL) != 3 && PotionUtils.getPotion(itemstack2) == Potions.WATER) {
            if (!level2.isClientSide) {
               player2.setItemInHand(interactionhand2, ItemUtils.createFilledResult(itemstack2, player2, new ItemStack(Items.GLASS_BOTTLE)));
               player2.awardStat(Stats.USE_CAULDRON);
               player2.awardStat(Stats.ITEM_USED.get(itemstack2.getItem()));
               level2.setBlockAndUpdate(blockpos2, blockstate4.cycle(LayeredCauldronBlock.LEVEL));
               level2.playSound((Player)null, blockpos2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
               level2.gameEvent((Entity)null, GameEvent.FLUID_PLACE, blockpos2);
            }

            return InteractionResult.sidedSuccess(level2.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      });
      WATER.put(Items.LEATHER_BOOTS, DYED_ITEM);
      WATER.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
      WATER.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
      WATER.put(Items.LEATHER_HELMET, DYED_ITEM);
      WATER.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
      WATER.put(Items.WHITE_BANNER, BANNER);
      WATER.put(Items.GRAY_BANNER, BANNER);
      WATER.put(Items.BLACK_BANNER, BANNER);
      WATER.put(Items.BLUE_BANNER, BANNER);
      WATER.put(Items.BROWN_BANNER, BANNER);
      WATER.put(Items.CYAN_BANNER, BANNER);
      WATER.put(Items.GREEN_BANNER, BANNER);
      WATER.put(Items.LIGHT_BLUE_BANNER, BANNER);
      WATER.put(Items.LIGHT_GRAY_BANNER, BANNER);
      WATER.put(Items.LIME_BANNER, BANNER);
      WATER.put(Items.MAGENTA_BANNER, BANNER);
      WATER.put(Items.ORANGE_BANNER, BANNER);
      WATER.put(Items.PINK_BANNER, BANNER);
      WATER.put(Items.PURPLE_BANNER, BANNER);
      WATER.put(Items.RED_BANNER, BANNER);
      WATER.put(Items.YELLOW_BANNER, BANNER);
      WATER.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
      WATER.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
      LAVA.put(Items.BUCKET, (blockstate2, level1, blockpos1, player1, interactionhand1, itemstack1) -> fillBucket(blockstate2, level1, blockpos1, player1, interactionhand1, itemstack1, new ItemStack(Items.LAVA_BUCKET), (blockstate3) -> true, SoundEvents.BUCKET_FILL_LAVA));
      addDefaultInteractions(LAVA);
      POWDER_SNOW.put(Items.BUCKET, (blockstate, level, blockpos, player, interactionhand, itemstack) -> fillBucket(blockstate, level, blockpos, player, interactionhand, itemstack, new ItemStack(Items.POWDER_SNOW_BUCKET), (blockstate1) -> blockstate1.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
      addDefaultInteractions(POWDER_SNOW);
   }

   static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
      map.put(Items.LAVA_BUCKET, FILL_LAVA);
      map.put(Items.WATER_BUCKET, FILL_WATER);
      map.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
   }

   static InteractionResult fillBucket(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, ItemStack itemstack, ItemStack itemstack1, Predicate<BlockState> predicate, SoundEvent soundevent) {
      if (!predicate.test(blockstate)) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            Item item = itemstack.getItem();
            player.setItemInHand(interactionhand, ItemUtils.createFilledResult(itemstack, player, itemstack1));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(blockpos, Blocks.CAULDRON.defaultBlockState());
            level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   static InteractionResult emptyBucket(Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, ItemStack itemstack, BlockState blockstate, SoundEvent soundevent) {
      if (!level.isClientSide) {
         Item item = itemstack.getItem();
         player.setItemInHand(interactionhand, ItemUtils.createFilledResult(itemstack, player, new ItemStack(Items.BUCKET)));
         player.awardStat(Stats.FILL_CAULDRON);
         player.awardStat(Stats.ITEM_USED.get(item));
         level.setBlockAndUpdate(blockpos, blockstate);
         level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
         level.gameEvent((Entity)null, GameEvent.FLUID_PLACE, blockpos);
      }

      return InteractionResult.sidedSuccess(level.isClientSide);
   }
}
