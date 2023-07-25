package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BeaconBlockEntity extends BlockEntity implements MenuProvider, Nameable {
   private static final int MAX_LEVELS = 4;
   public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, {MobEffects.DAMAGE_BOOST}, {MobEffects.REGENERATION}};
   private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
   public static final int DATA_LEVELS = 0;
   public static final int DATA_PRIMARY = 1;
   public static final int DATA_SECONDARY = 2;
   public static final int NUM_DATA_VALUES = 3;
   private static final int BLOCKS_CHECK_PER_TICK = 10;
   private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
   List<BeaconBlockEntity.BeaconBeamSection> beamSections = Lists.newArrayList();
   private List<BeaconBlockEntity.BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
   int levels;
   private int lastCheckY;
   @Nullable
   MobEffect primaryPower;
   @Nullable
   MobEffect secondaryPower;
   @Nullable
   private Component name;
   private LockCode lockKey = LockCode.NO_LOCK;
   private final ContainerData dataAccess = new ContainerData() {
      public int get(int i) {
         int var10000;
         switch (i) {
            case 0:
               var10000 = BeaconBlockEntity.this.levels;
               break;
            case 1:
               var10000 = MobEffect.getIdFromNullable(BeaconBlockEntity.this.primaryPower);
               break;
            case 2:
               var10000 = MobEffect.getIdFromNullable(BeaconBlockEntity.this.secondaryPower);
               break;
            default:
               var10000 = 0;
         }

         return var10000;
      }

      public void set(int i, int j) {
         switch (i) {
            case 0:
               BeaconBlockEntity.this.levels = j;
               break;
            case 1:
               if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                  BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
               }

               BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(j);
               break;
            case 2:
               BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(j);
         }

      }

      public int getCount() {
         return 3;
      }
   };

   public BeaconBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BEACON, blockpos, blockstate);
   }

   public static void tick(Level level, BlockPos blockpos, BlockState blockstate, BeaconBlockEntity beaconblockentity) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      BlockPos blockpos1;
      if (beaconblockentity.lastCheckY < j) {
         blockpos1 = blockpos;
         beaconblockentity.checkingBeamSections = Lists.newArrayList();
         beaconblockentity.lastCheckY = blockpos.getY() - 1;
      } else {
         blockpos1 = new BlockPos(i, beaconblockentity.lastCheckY + 1, k);
      }

      BeaconBlockEntity.BeaconBeamSection beaconblockentity_beaconbeamsection = beaconblockentity.checkingBeamSections.isEmpty() ? null : beaconblockentity.checkingBeamSections.get(beaconblockentity.checkingBeamSections.size() - 1);
      int l = level.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);

      for(int i1 = 0; i1 < 10 && blockpos1.getY() <= l; ++i1) {
         BlockState blockstate1 = level.getBlockState(blockpos1);
         Block block = blockstate1.getBlock();
         if (block instanceof BeaconBeamBlock) {
            float[] afloat = ((BeaconBeamBlock)block).getColor().getTextureDiffuseColors();
            if (beaconblockentity.checkingBeamSections.size() <= 1) {
               beaconblockentity_beaconbeamsection = new BeaconBlockEntity.BeaconBeamSection(afloat);
               beaconblockentity.checkingBeamSections.add(beaconblockentity_beaconbeamsection);
            } else if (beaconblockentity_beaconbeamsection != null) {
               if (Arrays.equals(afloat, beaconblockentity_beaconbeamsection.color)) {
                  beaconblockentity_beaconbeamsection.increaseHeight();
               } else {
                  beaconblockentity_beaconbeamsection = new BeaconBlockEntity.BeaconBeamSection(new float[]{(beaconblockentity_beaconbeamsection.color[0] + afloat[0]) / 2.0F, (beaconblockentity_beaconbeamsection.color[1] + afloat[1]) / 2.0F, (beaconblockentity_beaconbeamsection.color[2] + afloat[2]) / 2.0F});
                  beaconblockentity.checkingBeamSections.add(beaconblockentity_beaconbeamsection);
               }
            }
         } else {
            if (beaconblockentity_beaconbeamsection == null || blockstate1.getLightBlock(level, blockpos1) >= 15 && !blockstate1.is(Blocks.BEDROCK)) {
               beaconblockentity.checkingBeamSections.clear();
               beaconblockentity.lastCheckY = l;
               break;
            }

            beaconblockentity_beaconbeamsection.increaseHeight();
         }

         blockpos1 = blockpos1.above();
         ++beaconblockentity.lastCheckY;
      }

      int j1 = beaconblockentity.levels;
      if (level.getGameTime() % 80L == 0L) {
         if (!beaconblockentity.beamSections.isEmpty()) {
            beaconblockentity.levels = updateBase(level, i, j, k);
         }

         if (beaconblockentity.levels > 0 && !beaconblockentity.beamSections.isEmpty()) {
            applyEffects(level, blockpos, beaconblockentity.levels, beaconblockentity.primaryPower, beaconblockentity.secondaryPower);
            playSound(level, blockpos, SoundEvents.BEACON_AMBIENT);
         }
      }

      if (beaconblockentity.lastCheckY >= l) {
         beaconblockentity.lastCheckY = level.getMinBuildHeight() - 1;
         boolean flag = j1 > 0;
         beaconblockentity.beamSections = beaconblockentity.checkingBeamSections;
         if (!level.isClientSide) {
            boolean flag1 = beaconblockentity.levels > 0;
            if (!flag && flag1) {
               playSound(level, blockpos, SoundEvents.BEACON_ACTIVATE);

               for(ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, (new AABB((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).inflate(10.0D, 5.0D, 10.0D))) {
                  CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverplayer, beaconblockentity.levels);
               }
            } else if (flag && !flag1) {
               playSound(level, blockpos, SoundEvents.BEACON_DEACTIVATE);
            }
         }
      }

   }

   private static int updateBase(Level level, int i, int j, int k) {
      int l = 0;

      for(int i1 = 1; i1 <= 4; l = i1++) {
         int j1 = j - i1;
         if (j1 < level.getMinBuildHeight()) {
            break;
         }

         boolean flag = true;

         for(int k1 = i - i1; k1 <= i + i1 && flag; ++k1) {
            for(int l1 = k - i1; l1 <= k + i1; ++l1) {
               if (!level.getBlockState(new BlockPos(k1, j1, l1)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                  flag = false;
                  break;
               }
            }
         }

         if (!flag) {
            break;
         }
      }

      return l;
   }

   public void setRemoved() {
      playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
      super.setRemoved();
   }

   private static void applyEffects(Level level, BlockPos blockpos, int i, @Nullable MobEffect mobeffect, @Nullable MobEffect mobeffect1) {
      if (!level.isClientSide && mobeffect != null) {
         double d0 = (double)(i * 10 + 10);
         int j = 0;
         if (i >= 4 && mobeffect == mobeffect1) {
            j = 1;
         }

         int k = (9 + i * 2) * 20;
         AABB aabb = (new AABB(blockpos)).inflate(d0).expandTowards(0.0D, (double)level.getHeight(), 0.0D);
         List<Player> list = level.getEntitiesOfClass(Player.class, aabb);

         for(Player player : list) {
            player.addEffect(new MobEffectInstance(mobeffect, k, j, true, true));
         }

         if (i >= 4 && mobeffect != mobeffect1 && mobeffect1 != null) {
            for(Player player1 : list) {
               player1.addEffect(new MobEffectInstance(mobeffect1, k, 0, true, true));
            }
         }

      }
   }

   public static void playSound(Level level, BlockPos blockpos, SoundEvent soundevent) {
      level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public List<BeaconBlockEntity.BeaconBeamSection> getBeamSections() {
      return (List<BeaconBlockEntity.BeaconBeamSection>)(this.levels == 0 ? ImmutableList.of() : this.beamSections);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   @Nullable
   static MobEffect getValidEffectById(int i) {
      MobEffect mobeffect = MobEffect.byId(i);
      return VALID_EFFECTS.contains(mobeffect) ? mobeffect : null;
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.primaryPower = getValidEffectById(compoundtag.getInt("Primary"));
      this.secondaryPower = getValidEffectById(compoundtag.getInt("Secondary"));
      if (compoundtag.contains("CustomName", 8)) {
         this.name = Component.Serializer.fromJson(compoundtag.getString("CustomName"));
      }

      this.lockKey = LockCode.fromTag(compoundtag);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putInt("Primary", MobEffect.getIdFromNullable(this.primaryPower));
      compoundtag.putInt("Secondary", MobEffect.getIdFromNullable(this.secondaryPower));
      compoundtag.putInt("Levels", this.levels);
      if (this.name != null) {
         compoundtag.putString("CustomName", Component.Serializer.toJson(this.name));
      }

      this.lockKey.addToTag(compoundtag);
   }

   public void setCustomName(@Nullable Component component) {
      this.name = component;
   }

   @Nullable
   public Component getCustomName() {
      return this.name;
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      return BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName()) ? new BeaconMenu(i, inventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos())) : null;
   }

   public Component getDisplayName() {
      return this.getName();
   }

   public Component getName() {
      return this.name != null ? this.name : DEFAULT_NAME;
   }

   public void setLevel(Level level) {
      super.setLevel(level);
      this.lastCheckY = level.getMinBuildHeight() - 1;
   }

   public static class BeaconBeamSection {
      final float[] color;
      private int height;

      public BeaconBeamSection(float[] afloat) {
         this.color = afloat;
         this.height = 1;
      }

      protected void increaseHeight() {
         ++this.height;
      }

      public float[] getColor() {
         return this.color;
      }

      public int getHeight() {
         return this.height;
      }
   }
}
