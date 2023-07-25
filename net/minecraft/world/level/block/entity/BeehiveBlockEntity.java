package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BeehiveBlockEntity extends BlockEntity {
   public static final String TAG_FLOWER_POS = "FlowerPos";
   public static final String MIN_OCCUPATION_TICKS = "MinOccupationTicks";
   public static final String ENTITY_DATA = "EntityData";
   public static final String TICKS_IN_HIVE = "TicksInHive";
   public static final String HAS_NECTAR = "HasNectar";
   public static final String BEES = "Bees";
   private static final List<String> IGNORED_BEE_TAGS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "HivePos", "Passengers", "Leash", "UUID");
   public static final int MAX_OCCUPANTS = 3;
   private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
   private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
   public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
   private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
   @Nullable
   private BlockPos savedFlowerPos;

   public BeehiveBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BEEHIVE, blockpos, blockstate);
   }

   public void setChanged() {
      if (this.isFireNearby()) {
         this.emptyAllLivingFromHive((Player)null, this.level.getBlockState(this.getBlockPos()), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
      }

      super.setChanged();
   }

   public boolean isFireNearby() {
      if (this.level == null) {
         return false;
      } else {
         for(BlockPos blockpos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
            if (this.level.getBlockState(blockpos).getBlock() instanceof FireBlock) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isEmpty() {
      return this.stored.isEmpty();
   }

   public boolean isFull() {
      return this.stored.size() == 3;
   }

   public void emptyAllLivingFromHive(@Nullable Player player, BlockState blockstate, BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity_beereleasestatus) {
      List<Entity> list = this.releaseAllOccupants(blockstate, beehiveblockentity_beereleasestatus);
      if (player != null) {
         for(Entity entity : list) {
            if (entity instanceof Bee) {
               Bee bee = (Bee)entity;
               if (player.position().distanceToSqr(entity.position()) <= 16.0D) {
                  if (!this.isSedated()) {
                     bee.setTarget(player);
                  } else {
                     bee.setStayOutOfHiveCountdown(400);
                  }
               }
            }
         }
      }

   }

   private List<Entity> releaseAllOccupants(BlockState blockstate, BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity_beereleasestatus) {
      List<Entity> list = Lists.newArrayList();
      this.stored.removeIf((beehiveblockentity_beedata) -> releaseOccupant(this.level, this.worldPosition, blockstate, beehiveblockentity_beedata, list, beehiveblockentity_beereleasestatus, this.savedFlowerPos));
      if (!list.isEmpty()) {
         super.setChanged();
      }

      return list;
   }

   public void addOccupant(Entity entity, boolean flag) {
      this.addOccupantWithPresetTicks(entity, flag, 0);
   }

   @VisibleForDebug
   public int getOccupantCount() {
      return this.stored.size();
   }

   public static int getHoneyLevel(BlockState blockstate) {
      return blockstate.getValue(BeehiveBlock.HONEY_LEVEL);
   }

   @VisibleForDebug
   public boolean isSedated() {
      return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
   }

   public void addOccupantWithPresetTicks(Entity entity, boolean flag, int i) {
      if (this.stored.size() < 3) {
         entity.stopRiding();
         entity.ejectPassengers();
         CompoundTag compoundtag = new CompoundTag();
         entity.save(compoundtag);
         this.storeBee(compoundtag, i, flag);
         if (this.level != null) {
            if (entity instanceof Bee) {
               Bee bee = (Bee)entity;
               if (bee.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                  this.savedFlowerPos = bee.getSavedFlowerPos();
               }
            }

            BlockPos blockpos = this.getBlockPos();
            this.level.playSound((Player)null, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, this.getBlockState()));
         }

         entity.discard();
         super.setChanged();
      }
   }

   public void storeBee(CompoundTag compoundtag, int i, boolean flag) {
      this.stored.add(new BeehiveBlockEntity.BeeData(compoundtag, i, flag ? 2400 : 600));
   }

   private static boolean releaseOccupant(Level level, BlockPos blockpos, BlockState blockstate, BeehiveBlockEntity.BeeData beehiveblockentity_beedata, @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity_beereleasestatus, @Nullable BlockPos blockpos1) {
      if ((level.isNight() || level.isRaining()) && beehiveblockentity_beereleasestatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
         return false;
      } else {
         CompoundTag compoundtag = beehiveblockentity_beedata.entityData.copy();
         removeIgnoredBeeTags(compoundtag);
         compoundtag.put("HivePos", NbtUtils.writeBlockPos(blockpos));
         compoundtag.putBoolean("NoGravity", true);
         Direction direction = blockstate.getValue(BeehiveBlock.FACING);
         BlockPos blockpos2 = blockpos.relative(direction);
         boolean flag = !level.getBlockState(blockpos2).getCollisionShape(level, blockpos2).isEmpty();
         if (flag && beehiveblockentity_beereleasestatus != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
         } else {
            Entity entity = EntityType.loadEntityRecursive(compoundtag, level, (entity1) -> entity1);
            if (entity != null) {
               if (!entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                  return false;
               } else {
                  if (entity instanceof Bee) {
                     Bee bee = (Bee)entity;
                     if (blockpos1 != null && !bee.hasSavedFlowerPos() && level.random.nextFloat() < 0.9F) {
                        bee.setSavedFlowerPos(blockpos1);
                     }

                     if (beehiveblockentity_beereleasestatus == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                        bee.dropOffNectar();
                        if (blockstate.is(BlockTags.BEEHIVES, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(BeehiveBlock.HONEY_LEVEL))) {
                           int i = getHoneyLevel(blockstate);
                           if (i < 5) {
                              int j = level.random.nextInt(100) == 0 ? 2 : 1;
                              if (i + j > 5) {
                                 --j;
                              }

                              level.setBlockAndUpdate(blockpos, blockstate.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
                           }
                        }
                     }

                     setBeeReleaseData(beehiveblockentity_beedata.ticksInHive, bee);
                     if (list != null) {
                        list.add(bee);
                     }

                     float f = entity.getBbWidth();
                     double d0 = flag ? 0.0D : 0.55D + (double)(f / 2.0F);
                     double d1 = (double)blockpos.getX() + 0.5D + d0 * (double)direction.getStepX();
                     double d2 = (double)blockpos.getY() + 0.5D - (double)(entity.getBbHeight() / 2.0F);
                     double d3 = (double)blockpos.getZ() + 0.5D + d0 * (double)direction.getStepZ();
                     entity.moveTo(d1, d2, d3, entity.getYRot(), entity.getXRot());
                  }

                  level.playSound((Player)null, blockpos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, level.getBlockState(blockpos)));
                  return level.addFreshEntity(entity);
               }
            } else {
               return false;
            }
         }
      }
   }

   static void removeIgnoredBeeTags(CompoundTag compoundtag) {
      for(String s : IGNORED_BEE_TAGS) {
         compoundtag.remove(s);
      }

   }

   private static void setBeeReleaseData(int i, Bee bee) {
      int j = bee.getAge();
      if (j < 0) {
         bee.setAge(Math.min(0, j + i));
      } else if (j > 0) {
         bee.setAge(Math.max(0, j - i));
      }

      bee.setInLoveTime(Math.max(0, bee.getInLoveTime() - i));
   }

   private boolean hasSavedFlowerPos() {
      return this.savedFlowerPos != null;
   }

   private static void tickOccupants(Level level, BlockPos blockpos, BlockState blockstate, List<BeehiveBlockEntity.BeeData> list, @Nullable BlockPos blockpos1) {
      boolean flag = false;

      BeehiveBlockEntity.BeeData beehiveblockentity_beedata;
      for(Iterator<BeehiveBlockEntity.BeeData> iterator = list.iterator(); iterator.hasNext(); ++beehiveblockentity_beedata.ticksInHive) {
         beehiveblockentity_beedata = iterator.next();
         if (beehiveblockentity_beedata.ticksInHive > beehiveblockentity_beedata.minOccupationTicks) {
            BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity_beereleasestatus = beehiveblockentity_beedata.entityData.getBoolean("HasNectar") ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
            if (releaseOccupant(level, blockpos, blockstate, beehiveblockentity_beedata, (List<Entity>)null, beehiveblockentity_beereleasestatus, blockpos1)) {
               flag = true;
               iterator.remove();
            }
         }
      }

      if (flag) {
         setChanged(level, blockpos, blockstate);
      }

   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, BeehiveBlockEntity beehiveblockentity) {
      tickOccupants(level, blockpos, blockstate, beehiveblockentity.stored, beehiveblockentity.savedFlowerPos);
      if (!beehiveblockentity.stored.isEmpty() && level.getRandom().nextDouble() < 0.005D) {
         double d0 = (double)blockpos.getX() + 0.5D;
         double d1 = (double)blockpos.getY();
         double d2 = (double)blockpos.getZ() + 0.5D;
         level.playSound((Player)null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      DebugPackets.sendHiveInfo(level, blockpos, blockstate, beehiveblockentity);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.stored.clear();
      ListTag listtag = compoundtag.getList("Bees", 10);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag1 = listtag.getCompound(i);
         BeehiveBlockEntity.BeeData beehiveblockentity_beedata = new BeehiveBlockEntity.BeeData(compoundtag1.getCompound("EntityData"), compoundtag1.getInt("TicksInHive"), compoundtag1.getInt("MinOccupationTicks"));
         this.stored.add(beehiveblockentity_beedata);
      }

      this.savedFlowerPos = null;
      if (compoundtag.contains("FlowerPos")) {
         this.savedFlowerPos = NbtUtils.readBlockPos(compoundtag.getCompound("FlowerPos"));
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.put("Bees", this.writeBees());
      if (this.hasSavedFlowerPos()) {
         compoundtag.put("FlowerPos", NbtUtils.writeBlockPos(this.savedFlowerPos));
      }

   }

   public ListTag writeBees() {
      ListTag listtag = new ListTag();

      for(BeehiveBlockEntity.BeeData beehiveblockentity_beedata : this.stored) {
         CompoundTag compoundtag = beehiveblockentity_beedata.entityData.copy();
         compoundtag.remove("UUID");
         CompoundTag compoundtag1 = new CompoundTag();
         compoundtag1.put("EntityData", compoundtag);
         compoundtag1.putInt("TicksInHive", beehiveblockentity_beedata.ticksInHive);
         compoundtag1.putInt("MinOccupationTicks", beehiveblockentity_beedata.minOccupationTicks);
         listtag.add(compoundtag1);
      }

      return listtag;
   }

   static class BeeData {
      final CompoundTag entityData;
      int ticksInHive;
      final int minOccupationTicks;

      BeeData(CompoundTag compoundtag, int i, int j) {
         BeehiveBlockEntity.removeIgnoredBeeTags(compoundtag);
         this.entityData = compoundtag;
         this.ticksInHive = i;
         this.minOccupationTicks = j;
      }
   }

   public static enum BeeReleaseStatus {
      HONEY_DELIVERED,
      BEE_RELEASED,
      EMERGENCY;
   }
}
