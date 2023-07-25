package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem extends ComplexItem {
   public static final int IMAGE_WIDTH = 128;
   public static final int IMAGE_HEIGHT = 128;
   private static final int DEFAULT_MAP_COLOR = -12173266;
   private static final String TAG_MAP = "map";
   public static final String MAP_SCALE_TAG = "map_scale_direction";
   public static final String MAP_LOCK_TAG = "map_to_lock";

   public MapItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public static ItemStack create(Level level, int i, int j, byte b0, boolean flag, boolean flag1) {
      ItemStack itemstack = new ItemStack(Items.FILLED_MAP);
      createAndStoreSavedData(itemstack, level, i, j, b0, flag, flag1, level.dimension());
      return itemstack;
   }

   @Nullable
   public static MapItemSavedData getSavedData(@Nullable Integer integer, Level level) {
      return integer == null ? null : level.getMapData(makeKey(integer));
   }

   @Nullable
   public static MapItemSavedData getSavedData(ItemStack itemstack, Level level) {
      Integer integer = getMapId(itemstack);
      return getSavedData(integer, level);
   }

   @Nullable
   public static Integer getMapId(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null && compoundtag.contains("map", 99) ? compoundtag.getInt("map") : null;
   }

   private static int createNewSavedData(Level level, int i, int j, int k, boolean flag, boolean flag1, ResourceKey<Level> resourcekey) {
      MapItemSavedData mapitemsaveddata = MapItemSavedData.createFresh((double)i, (double)j, (byte)k, flag, flag1, resourcekey);
      int l = level.getFreeMapId();
      level.setMapData(makeKey(l), mapitemsaveddata);
      return l;
   }

   private static void storeMapData(ItemStack itemstack, int i) {
      itemstack.getOrCreateTag().putInt("map", i);
   }

   private static void createAndStoreSavedData(ItemStack itemstack, Level level, int i, int j, int k, boolean flag, boolean flag1, ResourceKey<Level> resourcekey) {
      int l = createNewSavedData(level, i, j, k, flag, flag1, resourcekey);
      storeMapData(itemstack, l);
   }

   public static String makeKey(int i) {
      return "map_" + i;
   }

   public void update(Level level, Entity entity, MapItemSavedData mapitemsaveddata) {
      if (level.dimension() == mapitemsaveddata.dimension && entity instanceof Player) {
         int i = 1 << mapitemsaveddata.scale;
         int j = mapitemsaveddata.centerX;
         int k = mapitemsaveddata.centerZ;
         int l = Mth.floor(entity.getX() - (double)j) / i + 64;
         int i1 = Mth.floor(entity.getZ() - (double)k) / i + 64;
         int j1 = 128 / i;
         if (level.dimensionType().hasCeiling()) {
            j1 /= 2;
         }

         MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer = mapitemsaveddata.getHoldingPlayer((Player)entity);
         ++mapitemsaveddata_holdingplayer.step;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();
         boolean flag = false;

         for(int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
            if ((k1 & 15) == (mapitemsaveddata_holdingplayer.step & 15) || flag) {
               flag = false;
               double d0 = 0.0D;

               for(int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                  if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                     int i2 = Mth.square(k1 - l) + Mth.square(l1 - i1);
                     boolean flag1 = i2 > (j1 - 2) * (j1 - 2);
                     int j2 = (j / i + k1 - 64) * i;
                     int k2 = (k / i + l1 - 64) * i;
                     Multiset<MapColor> multiset = LinkedHashMultiset.create();
                     LevelChunk levelchunk = level.getChunk(SectionPos.blockToSectionCoord(j2), SectionPos.blockToSectionCoord(k2));
                     if (!levelchunk.isEmpty()) {
                        int l2 = 0;
                        double d1 = 0.0D;
                        if (level.dimensionType().hasCeiling()) {
                           int i3 = j2 + k2 * 231871;
                           i3 = i3 * i3 * 31287121 + i3 * 11;
                           if ((i3 >> 20 & 1) == 0) {
                              multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                           } else {
                              multiset.add(Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                           }

                           d1 = 100.0D;
                        } else {
                           for(int j3 = 0; j3 < i; ++j3) {
                              for(int k3 = 0; k3 < i; ++k3) {
                                 blockpos_mutableblockpos.set(j2 + j3, 0, k2 + k3);
                                 int l3 = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos_mutableblockpos.getX(), blockpos_mutableblockpos.getZ()) + 1;
                                 BlockState blockstate2;
                                 if (l3 <= level.getMinBuildHeight() + 1) {
                                    blockstate2 = Blocks.BEDROCK.defaultBlockState();
                                 } else {
                                    do {
                                       --l3;
                                       blockpos_mutableblockpos.setY(l3);
                                       blockstate2 = levelchunk.getBlockState(blockpos_mutableblockpos);
                                    } while(blockstate2.getMapColor(level, blockpos_mutableblockpos) == MapColor.NONE && l3 > level.getMinBuildHeight());

                                    if (l3 > level.getMinBuildHeight() && !blockstate2.getFluidState().isEmpty()) {
                                       int i4 = l3 - 1;
                                       blockpos_mutableblockpos1.set(blockpos_mutableblockpos);

                                       BlockState blockstate1;
                                       do {
                                          blockpos_mutableblockpos1.setY(i4--);
                                          blockstate1 = levelchunk.getBlockState(blockpos_mutableblockpos1);
                                          ++l2;
                                       } while(i4 > level.getMinBuildHeight() && !blockstate1.getFluidState().isEmpty());

                                       blockstate2 = this.getCorrectStateForFluidBlock(level, blockstate2, blockpos_mutableblockpos);
                                    }
                                 }

                                 mapitemsaveddata.checkBanners(level, blockpos_mutableblockpos.getX(), blockpos_mutableblockpos.getZ());
                                 d1 += (double)l3 / (double)(i * i);
                                 multiset.add(blockstate2.getMapColor(level, blockpos_mutableblockpos));
                              }
                           }
                        }

                        l2 /= i * i;
                        MapColor mapcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.NONE);
                        MapColor.Brightness mapcolor_brightness;
                        if (mapcolor == MapColor.WATER) {
                           double d2 = (double)l2 * 0.1D + (double)(k1 + l1 & 1) * 0.2D;
                           if (d2 < 0.5D) {
                              mapcolor_brightness = MapColor.Brightness.HIGH;
                           } else if (d2 > 0.9D) {
                              mapcolor_brightness = MapColor.Brightness.LOW;
                           } else {
                              mapcolor_brightness = MapColor.Brightness.NORMAL;
                           }
                        } else {
                           double d3 = (d1 - d0) * 4.0D / (double)(i + 4) + ((double)(k1 + l1 & 1) - 0.5D) * 0.4D;
                           if (d3 > 0.6D) {
                              mapcolor_brightness = MapColor.Brightness.HIGH;
                           } else if (d3 < -0.6D) {
                              mapcolor_brightness = MapColor.Brightness.LOW;
                           } else {
                              mapcolor_brightness = MapColor.Brightness.NORMAL;
                           }
                        }

                        d0 = d1;
                        if (l1 >= 0 && i2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
                           flag |= mapitemsaveddata.updateColor(k1, l1, mapcolor.getPackedId(mapcolor_brightness));
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockstate, BlockPos blockpos) {
      FluidState fluidstate = blockstate.getFluidState();
      return !fluidstate.isEmpty() && !blockstate.isFaceSturdy(level, blockpos, Direction.UP) ? fluidstate.createLegacyBlock() : blockstate;
   }

   private static boolean isBiomeWatery(boolean[] aboolean, int i, int j) {
      return aboolean[j * 128 + i];
   }

   public static void renderBiomePreviewMap(ServerLevel serverlevel, ItemStack itemstack) {
      MapItemSavedData mapitemsaveddata = getSavedData(itemstack, serverlevel);
      if (mapitemsaveddata != null) {
         if (serverlevel.dimension() == mapitemsaveddata.dimension) {
            int i = 1 << mapitemsaveddata.scale;
            int j = mapitemsaveddata.centerX;
            int k = mapitemsaveddata.centerZ;
            boolean[] aboolean = new boolean[16384];
            int l = j / i - 64;
            int i1 = k / i - 64;
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

            for(int j1 = 0; j1 < 128; ++j1) {
               for(int k1 = 0; k1 < 128; ++k1) {
                  Holder<Biome> holder = serverlevel.getBiome(blockpos_mutableblockpos.set((l + k1) * i, 0, (i1 + j1) * i));
                  aboolean[j1 * 128 + k1] = holder.is(BiomeTags.WATER_ON_MAP_OUTLINES);
               }
            }

            for(int l1 = 1; l1 < 127; ++l1) {
               for(int i2 = 1; i2 < 127; ++i2) {
                  int j2 = 0;

                  for(int k2 = -1; k2 < 2; ++k2) {
                     for(int l2 = -1; l2 < 2; ++l2) {
                        if ((k2 != 0 || l2 != 0) && isBiomeWatery(aboolean, l1 + k2, i2 + l2)) {
                           ++j2;
                        }
                     }
                  }

                  MapColor.Brightness mapcolor_brightness = MapColor.Brightness.LOWEST;
                  MapColor mapcolor = MapColor.NONE;
                  if (isBiomeWatery(aboolean, l1, i2)) {
                     mapcolor = MapColor.COLOR_ORANGE;
                     if (j2 > 7 && i2 % 2 == 0) {
                        switch ((l1 + (int)(Mth.sin((float)i2 + 0.0F) * 7.0F)) / 8 % 5) {
                           case 0:
                           case 4:
                              mapcolor_brightness = MapColor.Brightness.LOW;
                              break;
                           case 1:
                           case 3:
                              mapcolor_brightness = MapColor.Brightness.NORMAL;
                              break;
                           case 2:
                              mapcolor_brightness = MapColor.Brightness.HIGH;
                        }
                     } else if (j2 > 7) {
                        mapcolor = MapColor.NONE;
                     } else if (j2 > 5) {
                        mapcolor_brightness = MapColor.Brightness.NORMAL;
                     } else if (j2 > 3) {
                        mapcolor_brightness = MapColor.Brightness.LOW;
                     } else if (j2 > 1) {
                        mapcolor_brightness = MapColor.Brightness.LOW;
                     }
                  } else if (j2 > 0) {
                     mapcolor = MapColor.COLOR_BROWN;
                     if (j2 > 3) {
                        mapcolor_brightness = MapColor.Brightness.NORMAL;
                     } else {
                        mapcolor_brightness = MapColor.Brightness.LOWEST;
                     }
                  }

                  if (mapcolor != MapColor.NONE) {
                     mapitemsaveddata.setColor(l1, i2, mapcolor.getPackedId(mapcolor_brightness));
                  }
               }
            }

         }
      }
   }

   public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int i, boolean flag) {
      if (!level.isClientSide) {
         MapItemSavedData mapitemsaveddata = getSavedData(itemstack, level);
         if (mapitemsaveddata != null) {
            if (entity instanceof Player) {
               Player player = (Player)entity;
               mapitemsaveddata.tickCarriedBy(player, itemstack);
            }

            if (!mapitemsaveddata.locked && (flag || entity instanceof Player && ((Player)entity).getOffhandItem() == itemstack)) {
               this.update(level, entity, mapitemsaveddata);
            }

         }
      }
   }

   @Nullable
   public Packet<?> getUpdatePacket(ItemStack itemstack, Level level, Player player) {
      Integer integer = getMapId(itemstack);
      MapItemSavedData mapitemsaveddata = getSavedData(integer, level);
      return mapitemsaveddata != null ? mapitemsaveddata.getUpdatePacket(integer, player) : null;
   }

   public void onCraftedBy(ItemStack itemstack, Level level, Player player) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("map_scale_direction", 99)) {
         scaleMap(itemstack, level, compoundtag.getInt("map_scale_direction"));
         compoundtag.remove("map_scale_direction");
      } else if (compoundtag != null && compoundtag.contains("map_to_lock", 1) && compoundtag.getBoolean("map_to_lock")) {
         lockMap(level, itemstack);
         compoundtag.remove("map_to_lock");
      }

   }

   private static void scaleMap(ItemStack itemstack, Level level, int i) {
      MapItemSavedData mapitemsaveddata = getSavedData(itemstack, level);
      if (mapitemsaveddata != null) {
         int j = level.getFreeMapId();
         level.setMapData(makeKey(j), mapitemsaveddata.scaled(i));
         storeMapData(itemstack, j);
      }

   }

   public static void lockMap(Level level, ItemStack itemstack) {
      MapItemSavedData mapitemsaveddata = getSavedData(itemstack, level);
      if (mapitemsaveddata != null) {
         int i = level.getFreeMapId();
         String s = makeKey(i);
         MapItemSavedData mapitemsaveddata1 = mapitemsaveddata.locked();
         level.setMapData(s, mapitemsaveddata1);
         storeMapData(itemstack, i);
      }

   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      Integer integer = getMapId(itemstack);
      MapItemSavedData mapitemsaveddata = level == null ? null : getSavedData(integer, level);
      CompoundTag compoundtag = itemstack.getTag();
      boolean flag;
      byte b0;
      if (compoundtag != null) {
         flag = compoundtag.getBoolean("map_to_lock");
         b0 = compoundtag.getByte("map_scale_direction");
      } else {
         flag = false;
         b0 = 0;
      }

      if (mapitemsaveddata != null && (mapitemsaveddata.locked || flag)) {
         list.add(Component.translatable("filled_map.locked", integer).withStyle(ChatFormatting.GRAY));
      }

      if (tooltipflag.isAdvanced()) {
         if (mapitemsaveddata != null) {
            if (!flag && b0 == 0) {
               list.add(Component.translatable("filled_map.id", integer).withStyle(ChatFormatting.GRAY));
            }

            int i = Math.min(mapitemsaveddata.scale + b0, 4);
            list.add(Component.translatable("filled_map.scale", 1 << i).withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("filled_map.level", i, 4).withStyle(ChatFormatting.GRAY));
         } else {
            list.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
         }
      }

   }

   public static int getColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTagElement("display");
      if (compoundtag != null && compoundtag.contains("MapColor", 99)) {
         int i = compoundtag.getInt("MapColor");
         return -16777216 | i & 16777215;
      } else {
         return -12173266;
      }
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      BlockState blockstate = useoncontext.getLevel().getBlockState(useoncontext.getClickedPos());
      if (blockstate.is(BlockTags.BANNERS)) {
         if (!useoncontext.getLevel().isClientSide) {
            MapItemSavedData mapitemsaveddata = getSavedData(useoncontext.getItemInHand(), useoncontext.getLevel());
            if (mapitemsaveddata != null && !mapitemsaveddata.toggleBanner(useoncontext.getLevel(), useoncontext.getClickedPos())) {
               return InteractionResult.FAIL;
            }
         }

         return InteractionResult.sidedSuccess(useoncontext.getLevel().isClientSide);
      } else {
         return super.useOn(useoncontext);
      }
   }
}
