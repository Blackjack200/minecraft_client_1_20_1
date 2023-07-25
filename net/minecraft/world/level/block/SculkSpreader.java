package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SculkSpreader {
   public static final int MAX_GROWTH_RATE_RADIUS = 24;
   public static final int MAX_CHARGE = 1000;
   public static final float MAX_DECAY_FACTOR = 0.5F;
   private static final int MAX_CURSORS = 32;
   public static final int SHRIEKER_PLACEMENT_RATE = 11;
   final boolean isWorldGeneration;
   private final TagKey<Block> replaceableBlocks;
   private final int growthSpawnCost;
   private final int noGrowthRadius;
   private final int chargeDecayRate;
   private final int additionalDecayRate;
   private List<SculkSpreader.ChargeCursor> cursors = new ArrayList<>();
   private static final Logger LOGGER = LogUtils.getLogger();

   public SculkSpreader(boolean flag, TagKey<Block> tagkey, int i, int j, int k, int l) {
      this.isWorldGeneration = flag;
      this.replaceableBlocks = tagkey;
      this.growthSpawnCost = i;
      this.noGrowthRadius = j;
      this.chargeDecayRate = k;
      this.additionalDecayRate = l;
   }

   public static SculkSpreader createLevelSpreader() {
      return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
   }

   public static SculkSpreader createWorldGenSpreader() {
      return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
   }

   public TagKey<Block> replaceableBlocks() {
      return this.replaceableBlocks;
   }

   public int growthSpawnCost() {
      return this.growthSpawnCost;
   }

   public int noGrowthRadius() {
      return this.noGrowthRadius;
   }

   public int chargeDecayRate() {
      return this.chargeDecayRate;
   }

   public int additionalDecayRate() {
      return this.additionalDecayRate;
   }

   public boolean isWorldGeneration() {
      return this.isWorldGeneration;
   }

   @VisibleForTesting
   public List<SculkSpreader.ChargeCursor> getCursors() {
      return this.cursors;
   }

   public void clear() {
      this.cursors.clear();
   }

   public void load(CompoundTag compoundtag) {
      if (compoundtag.contains("cursors", 9)) {
         this.cursors.clear();
         List<SculkSpreader.ChargeCursor> list = SculkSpreader.ChargeCursor.CODEC.listOf().parse(new Dynamic<>(NbtOps.INSTANCE, compoundtag.getList("cursors", 10))).resultOrPartial(LOGGER::error).orElseGet(ArrayList::new);
         int i = Math.min(list.size(), 32);

         for(int j = 0; j < i; ++j) {
            this.addCursor(list.get(j));
         }
      }

   }

   public void save(CompoundTag compoundtag) {
      SculkSpreader.ChargeCursor.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.cursors).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("cursors", tag));
   }

   public void addCursors(BlockPos blockpos, int i) {
      while(i > 0) {
         int j = Math.min(i, 1000);
         this.addCursor(new SculkSpreader.ChargeCursor(blockpos, j));
         i -= j;
      }

   }

   private void addCursor(SculkSpreader.ChargeCursor sculkspreader_chargecursor) {
      if (this.cursors.size() < 32) {
         this.cursors.add(sculkspreader_chargecursor);
      }
   }

   public void updateCursors(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, boolean flag) {
      if (!this.cursors.isEmpty()) {
         List<SculkSpreader.ChargeCursor> list = new ArrayList<>();
         Map<BlockPos, SculkSpreader.ChargeCursor> map = new HashMap<>();
         Object2IntMap<BlockPos> object2intmap = new Object2IntOpenHashMap<>();

         for(SculkSpreader.ChargeCursor sculkspreader_chargecursor : this.cursors) {
            sculkspreader_chargecursor.update(levelaccessor, blockpos, randomsource, this, flag);
            if (sculkspreader_chargecursor.charge <= 0) {
               levelaccessor.levelEvent(3006, sculkspreader_chargecursor.getPos(), 0);
            } else {
               BlockPos blockpos1 = sculkspreader_chargecursor.getPos();
               object2intmap.computeInt(blockpos1, (blockpos3, integer) -> (integer == null ? 0 : integer) + sculkspreader_chargecursor.charge);
               SculkSpreader.ChargeCursor sculkspreader_chargecursor1 = map.get(blockpos1);
               if (sculkspreader_chargecursor1 == null) {
                  map.put(blockpos1, sculkspreader_chargecursor);
                  list.add(sculkspreader_chargecursor);
               } else if (!this.isWorldGeneration() && sculkspreader_chargecursor.charge + sculkspreader_chargecursor1.charge <= 1000) {
                  sculkspreader_chargecursor1.mergeWith(sculkspreader_chargecursor);
               } else {
                  list.add(sculkspreader_chargecursor);
                  if (sculkspreader_chargecursor.charge < sculkspreader_chargecursor1.charge) {
                     map.put(blockpos1, sculkspreader_chargecursor);
                  }
               }
            }
         }

         for(Object2IntMap.Entry<BlockPos> object2intmap_entry : object2intmap.object2IntEntrySet()) {
            BlockPos blockpos2 = object2intmap_entry.getKey();
            int i = object2intmap_entry.getIntValue();
            SculkSpreader.ChargeCursor sculkspreader_chargecursor2 = map.get(blockpos2);
            Collection<Direction> collection = sculkspreader_chargecursor2 == null ? null : sculkspreader_chargecursor2.getFacingData();
            if (i > 0 && collection != null) {
               int j = (int)(Math.log1p((double)i) / (double)2.3F) + 1;
               int k = (j << 6) + MultifaceBlock.pack(collection);
               levelaccessor.levelEvent(3006, blockpos2, k);
            }
         }

         this.cursors = list;
      }
   }

   public static class ChargeCursor {
      private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(new ObjectArrayList<>(18), (objectarraylist) -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter((blockpos) -> (blockpos.getX() == 0 || blockpos.getY() == 0 || blockpos.getZ() == 0) && !blockpos.equals(BlockPos.ZERO)).map(BlockPos::immutable).forEach(objectarraylist::add));
      public static final int MAX_CURSOR_DECAY_DELAY = 1;
      private BlockPos pos;
      int charge;
      private int updateDelay;
      private int decayDelay;
      @Nullable
      private Set<Direction> facings;
      private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC.listOf().xmap((list) -> Sets.newEnumSet(list, Direction.class), Lists::newArrayList);
      public static final Codec<SculkSpreader.ChargeCursor> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(SculkSpreader.ChargeCursor::getPos), Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(SculkSpreader.ChargeCursor::getCharge), Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(SculkSpreader.ChargeCursor::getDecayDelay), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter((sculkspreader_chargecursor1) -> sculkspreader_chargecursor1.updateDelay), DIRECTION_SET.optionalFieldOf("facings").forGetter((sculkspreader_chargecursor) -> Optional.ofNullable(sculkspreader_chargecursor.getFacingData()))).apply(recordcodecbuilder_instance, SculkSpreader.ChargeCursor::new));

      private ChargeCursor(BlockPos blockpos, int i, int j, int k, Optional<Set<Direction>> optional) {
         this.pos = blockpos;
         this.charge = i;
         this.decayDelay = j;
         this.updateDelay = k;
         this.facings = optional.orElse((Set<Direction>)null);
      }

      public ChargeCursor(BlockPos blockpos, int i) {
         this(blockpos, i, 1, 0, Optional.empty());
      }

      public BlockPos getPos() {
         return this.pos;
      }

      public int getCharge() {
         return this.charge;
      }

      public int getDecayDelay() {
         return this.decayDelay;
      }

      @Nullable
      public Set<Direction> getFacingData() {
         return this.facings;
      }

      private boolean shouldUpdate(LevelAccessor levelaccessor, BlockPos blockpos, boolean flag) {
         if (this.charge <= 0) {
            return false;
         } else if (flag) {
            return true;
         } else if (levelaccessor instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)levelaccessor;
            return serverlevel.shouldTickBlocksAt(blockpos);
         } else {
            return false;
         }
      }

      public void update(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, SculkSpreader sculkspreader, boolean flag) {
         if (this.shouldUpdate(levelaccessor, blockpos, sculkspreader.isWorldGeneration)) {
            if (this.updateDelay > 0) {
               --this.updateDelay;
            } else {
               BlockState blockstate = levelaccessor.getBlockState(this.pos);
               SculkBehaviour sculkbehaviour = getBlockBehaviour(blockstate);
               if (flag && sculkbehaviour.attemptSpreadVein(levelaccessor, this.pos, blockstate, this.facings, sculkspreader.isWorldGeneration())) {
                  if (sculkbehaviour.canChangeBlockStateOnSpread()) {
                     blockstate = levelaccessor.getBlockState(this.pos);
                     sculkbehaviour = getBlockBehaviour(blockstate);
                  }

                  levelaccessor.playSound((Player)null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
               }

               this.charge = sculkbehaviour.attemptUseCharge(this, levelaccessor, blockpos, randomsource, sculkspreader, flag);
               if (this.charge <= 0) {
                  sculkbehaviour.onDischarged(levelaccessor, blockstate, this.pos, randomsource);
               } else {
                  BlockPos blockpos1 = getValidMovementPos(levelaccessor, this.pos, randomsource);
                  if (blockpos1 != null) {
                     sculkbehaviour.onDischarged(levelaccessor, blockstate, this.pos, randomsource);
                     this.pos = blockpos1.immutable();
                     if (sculkspreader.isWorldGeneration() && !this.pos.closerThan(new Vec3i(blockpos.getX(), this.pos.getY(), blockpos.getZ()), 15.0D)) {
                        this.charge = 0;
                        return;
                     }

                     blockstate = levelaccessor.getBlockState(blockpos1);
                  }

                  if (blockstate.getBlock() instanceof SculkBehaviour) {
                     this.facings = MultifaceBlock.availableFaces(blockstate);
                  }

                  this.decayDelay = sculkbehaviour.updateDecayDelay(this.decayDelay);
                  this.updateDelay = sculkbehaviour.getSculkSpreadDelay();
               }
            }
         }
      }

      void mergeWith(SculkSpreader.ChargeCursor sculkspreader_chargecursor) {
         this.charge += sculkspreader_chargecursor.charge;
         sculkspreader_chargecursor.charge = 0;
         this.updateDelay = Math.min(this.updateDelay, sculkspreader_chargecursor.updateDelay);
      }

      private static SculkBehaviour getBlockBehaviour(BlockState blockstate) {
         Block var2 = blockstate.getBlock();
         SculkBehaviour var10000;
         if (var2 instanceof SculkBehaviour sculkbehaviour) {
            var10000 = sculkbehaviour;
         } else {
            var10000 = SculkBehaviour.DEFAULT;
         }

         return var10000;
      }

      private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource randomsource) {
         return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, randomsource);
      }

      @Nullable
      private static BlockPos getValidMovementPos(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
         BlockPos.MutableBlockPos blockpos_mutableblockpos1 = blockpos.mutable();

         for(Vec3i vec3i : getRandomizedNonCornerNeighbourOffsets(randomsource)) {
            blockpos_mutableblockpos1.setWithOffset(blockpos, vec3i);
            BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos1);
            if (blockstate.getBlock() instanceof SculkBehaviour && isMovementUnobstructed(levelaccessor, blockpos, blockpos_mutableblockpos1)) {
               blockpos_mutableblockpos.set(blockpos_mutableblockpos1);
               if (SculkVeinBlock.hasSubstrateAccess(levelaccessor, blockstate, blockpos_mutableblockpos1)) {
                  break;
               }
            }
         }

         return blockpos_mutableblockpos.equals(blockpos) ? null : blockpos_mutableblockpos;
      }

      private static boolean isMovementUnobstructed(LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
         if (blockpos.distManhattan(blockpos1) == 1) {
            return true;
         } else {
            BlockPos blockpos2 = blockpos1.subtract(blockpos);
            Direction direction = Direction.fromAxisAndDirection(Direction.Axis.X, blockpos2.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction1 = Direction.fromAxisAndDirection(Direction.Axis.Y, blockpos2.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction2 = Direction.fromAxisAndDirection(Direction.Axis.Z, blockpos2.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            if (blockpos2.getX() == 0) {
               return isUnobstructed(levelaccessor, blockpos, direction1) || isUnobstructed(levelaccessor, blockpos, direction2);
            } else if (blockpos2.getY() == 0) {
               return isUnobstructed(levelaccessor, blockpos, direction) || isUnobstructed(levelaccessor, blockpos, direction2);
            } else {
               return isUnobstructed(levelaccessor, blockpos, direction) || isUnobstructed(levelaccessor, blockpos, direction1);
            }
         }
      }

      private static boolean isUnobstructed(LevelAccessor levelaccessor, BlockPos blockpos, Direction direction) {
         BlockPos blockpos1 = blockpos.relative(direction);
         return !levelaccessor.getBlockState(blockpos1).isFaceSturdy(levelaccessor, blockpos1, direction.getOpposite());
      }
   }
}
