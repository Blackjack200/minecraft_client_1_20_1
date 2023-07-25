package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LightningRodBlock extends RodBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int ACTIVATION_TICKS = 8;
   public static final int RANGE = 128;
   private static final int SPARK_CYCLE = 200;

   public LightningRodBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      boolean flag = fluidstate.getType() == Fluids.WATER;
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getClickedFace()).setValue(WATERLOGGED, Boolean.valueOf(flag));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) && blockstate.getValue(FACING) == direction ? 15 : 0;
   }

   public void onLightningStrike(BlockState blockstate, Level level, BlockPos blockpos) {
      level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)), 3);
      this.updateNeighbours(blockstate, level, blockpos);
      level.scheduleTick(blockpos, this, 8);
      level.levelEvent(3002, blockpos, blockstate.getValue(FACING).getAxis().ordinal());
   }

   private void updateNeighbours(BlockState blockstate, Level level, BlockPos blockpos) {
      level.updateNeighborsAt(blockpos.relative(blockstate.getValue(FACING).getOpposite()), this);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      serverlevel.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)), 3);
      this.updateNeighbours(blockstate, serverlevel, blockpos);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (level.isThundering() && (long)level.random.nextInt(200) <= level.getGameTime() % 200L && blockpos.getY() == level.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos.getX(), blockpos.getZ()) - 1) {
         ParticleUtils.spawnParticlesAlongAxis(blockstate.getValue(FACING).getAxis(), level, blockpos, 0.125D, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(1, 2));
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(POWERED)) {
            this.updateNeighbours(blockstate, level, blockpos);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(blockpos, this)) {
            level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(false)), 18);
         }

      }
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      if (level.isThundering() && projectile instanceof ThrownTrident && ((ThrownTrident)projectile).isChanneling()) {
         BlockPos blockpos = blockhitresult.getBlockPos();
         if (level.canSeeSky(blockpos)) {
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
            if (lightningbolt != null) {
               lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos.above()));
               Entity entity = projectile.getOwner();
               lightningbolt.setCause(entity instanceof ServerPlayer ? (ServerPlayer)entity : null);
               level.addFreshEntity(lightningbolt);
            }

            level.playSound((Player)null, blockpos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, POWERED, WATERLOGGED);
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }
}
