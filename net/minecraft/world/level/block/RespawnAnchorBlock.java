package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RespawnAnchorBlock extends Block {
   public static final int MIN_CHARGES = 0;
   public static final int MAX_CHARGES = 4;
   public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
   private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
   private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = (new ImmutableList.Builder<Vec3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator()).add(new Vec3i(0, 1, 0)).build();

   public RespawnAnchorBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (interactionhand == InteractionHand.MAIN_HAND && !isRespawnFuel(itemstack) && isRespawnFuel(player.getItemInHand(InteractionHand.OFF_HAND))) {
         return InteractionResult.PASS;
      } else if (isRespawnFuel(itemstack) && canBeCharged(blockstate)) {
         charge(player, level, blockpos, blockstate);
         if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else if (blockstate.getValue(CHARGE) == 0) {
         return InteractionResult.PASS;
      } else if (!canSetSpawn(level)) {
         if (!level.isClientSide) {
            this.explode(blockstate, level, blockpos);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         if (!level.isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            if (serverplayer.getRespawnDimension() != level.dimension() || !blockpos.equals(serverplayer.getRespawnPosition())) {
               serverplayer.setRespawnPosition(level.dimension(), blockpos, 0.0F, false, true);
               level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.CONSUME;
      }
   }

   private static boolean isRespawnFuel(ItemStack itemstack) {
      return itemstack.is(Items.GLOWSTONE);
   }

   private static boolean canBeCharged(BlockState blockstate) {
      return blockstate.getValue(CHARGE) < 4;
   }

   private static boolean isWaterThatWouldFlow(BlockPos blockpos, Level level) {
      FluidState fluidstate = level.getFluidState(blockpos);
      if (!fluidstate.is(FluidTags.WATER)) {
         return false;
      } else if (fluidstate.isSource()) {
         return true;
      } else {
         float f = (float)fluidstate.getAmount();
         if (f < 2.0F) {
            return false;
         } else {
            FluidState fluidstate1 = level.getFluidState(blockpos.below());
            return !fluidstate1.is(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState blockstate, Level level, final BlockPos blockpos) {
      level.removeBlock(blockpos, false);
      boolean flag = Direction.Plane.HORIZONTAL.stream().map(blockpos::relative).anyMatch((blockpos1) -> isWaterThatWouldFlow(blockpos1, level));
      final boolean flag1 = flag || level.getFluidState(blockpos.above()).is(FluidTags.WATER);
      ExplosionDamageCalculator explosiondamagecalculator = new ExplosionDamageCalculator() {
         public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockgetter, BlockPos blockposx, BlockState blockstate, FluidState fluidstate) {
            return blockpos.equals(blockpos) && flag1 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(explosion, blockgetter, blockpos, blockstate, fluidstate);
         }
      };
      Vec3 vec3 = blockpos.getCenter();
      level.explode((Entity)null, level.damageSources().badRespawnPointExplosion(vec3), explosiondamagecalculator, vec3, 5.0F, true, Level.ExplosionInteraction.BLOCK);
   }

   public static boolean canSetSpawn(Level level) {
      return level.dimensionType().respawnAnchorWorks();
   }

   public static void charge(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate) {
      BlockState blockstate1 = blockstate.setValue(CHARGE, Integer.valueOf(blockstate.getValue(CHARGE) + 1));
      level.setBlock(blockpos, blockstate1, 3);
      level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(entity, blockstate1));
      level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(CHARGE) != 0) {
         if (randomsource.nextInt(100) == 0) {
            level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
         }

         double d0 = (double)blockpos.getX() + 0.5D + (0.5D - randomsource.nextDouble());
         double d1 = (double)blockpos.getY() + 1.0D;
         double d2 = (double)blockpos.getZ() + 0.5D + (0.5D - randomsource.nextDouble());
         double d3 = (double)randomsource.nextFloat() * 0.04D;
         level.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(CHARGE);
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public static int getScaledChargeLevel(BlockState blockstate, int i) {
      return Mth.floor((float)(blockstate.getValue(CHARGE) - 0) / 4.0F * (float)i);
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return getScaledChargeLevel(blockstate, 15);
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos) {
      Optional<Vec3> optional = findStandUpPosition(entitytype, collisiongetter, blockpos, true);
      return optional.isPresent() ? optional : findStandUpPosition(entitytype, collisiongetter, blockpos, false);
   }

   private static Optional<Vec3> findStandUpPosition(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos, boolean flag) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Vec3i vec3i : RESPAWN_OFFSETS) {
         blockpos_mutableblockpos.set(blockpos).move(vec3i);
         Vec3 vec3 = DismountHelper.findSafeDismountLocation(entitytype, collisiongetter, blockpos_mutableblockpos, flag);
         if (vec3 != null) {
            return Optional.of(vec3);
         }
      }

      return Optional.empty();
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
