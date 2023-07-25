package net.minecraft.world.level.block;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape VIRTUAL_FENCE_POST = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   private static final int SMOKE_DISTANCE = 5;
   private final boolean spawnParticles;
   private final int fireDamage;

   public CampfireBlock(boolean flag, int i, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.spawnParticles = flag;
      this.fireDamage = i;
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)).setValue(SIGNAL_FIRE, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof CampfireBlockEntity campfireblockentity) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         Optional<CampfireCookingRecipe> optional = campfireblockentity.getCookableRecipe(itemstack);
         if (optional.isPresent()) {
            if (!level.isClientSide && campfireblockentity.placeFood(player, player.getAbilities().instabuild ? itemstack.copy() : itemstack, optional.get().getCookingTime())) {
               player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
               return InteractionResult.SUCCESS;
            }

            return InteractionResult.CONSUME;
         }
      }

      return InteractionResult.PASS;
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (blockstate.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
         entity.hurt(level.damageSources().inFire(), (float)this.fireDamage);
      }

      super.entityInside(blockstate, level, blockpos, entity);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof CampfireBlockEntity) {
            Containers.dropContents(level, blockpos, ((CampfireBlockEntity)blockentity).getItems());
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      LevelAccessor levelaccessor = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      boolean flag = levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER;
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(flag)).setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(levelaccessor.getBlockState(blockpos.below())))).setValue(LIT, Boolean.valueOf(!flag)).setValue(FACING, blockplacecontext.getHorizontalDirection());
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return direction == Direction.DOWN ? blockstate.setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(blockstate1))) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   private boolean isSmokeSource(BlockState blockstate) {
      return blockstate.is(Blocks.HAY_BLOCK);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         if (randomsource.nextInt(10) == 0) {
            level.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + randomsource.nextFloat(), randomsource.nextFloat() * 0.7F + 0.6F, false);
         }

         if (this.spawnParticles && randomsource.nextInt(5) == 0) {
            for(int i = 0; i < randomsource.nextInt(1) + 1; ++i) {
               level.addParticle(ParticleTypes.LAVA, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, (double)(randomsource.nextFloat() / 2.0F), 5.0E-5D, (double)(randomsource.nextFloat() / 2.0F));
            }
         }

      }
   }

   public static void dowse(@Nullable Entity entity, LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      if (levelaccessor.isClientSide()) {
         for(int i = 0; i < 20; ++i) {
            makeParticles((Level)levelaccessor, blockpos, blockstate.getValue(SIGNAL_FIRE), true);
         }
      }

      BlockEntity blockentity = levelaccessor.getBlockEntity(blockpos);
      if (blockentity instanceof CampfireBlockEntity) {
         ((CampfireBlockEntity)blockentity).dowse();
      }

      levelaccessor.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockpos);
   }

   public boolean placeLiquid(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
      if (!blockstate.getValue(BlockStateProperties.WATERLOGGED) && fluidstate.getType() == Fluids.WATER) {
         boolean flag = blockstate.getValue(LIT);
         if (flag) {
            if (!levelaccessor.isClientSide()) {
               levelaccessor.playSound((Player)null, blockpos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            dowse((Entity)null, levelaccessor, blockpos, blockstate);
         }

         levelaccessor.setBlock(blockpos, blockstate.setValue(WATERLOGGED, Boolean.valueOf(true)).setValue(LIT, Boolean.valueOf(false)), 3);
         levelaccessor.scheduleTick(blockpos, fluidstate.getType(), fluidstate.getType().getTickDelay(levelaccessor));
         return true;
      } else {
         return false;
      }
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      if (!level.isClientSide && projectile.isOnFire() && projectile.mayInteract(level, blockpos) && !blockstate.getValue(LIT) && !blockstate.getValue(WATERLOGGED)) {
         level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
      }

   }

   public static void makeParticles(Level level, BlockPos blockpos, boolean flag, boolean flag1) {
      RandomSource randomsource = level.getRandom();
      SimpleParticleType simpleparticletype = flag ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
      level.addAlwaysVisibleParticle(simpleparticletype, true, (double)blockpos.getX() + 0.5D + randomsource.nextDouble() / 3.0D * (double)(randomsource.nextBoolean() ? 1 : -1), (double)blockpos.getY() + randomsource.nextDouble() + randomsource.nextDouble(), (double)blockpos.getZ() + 0.5D + randomsource.nextDouble() / 3.0D * (double)(randomsource.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
      if (flag1) {
         level.addParticle(ParticleTypes.SMOKE, (double)blockpos.getX() + 0.5D + randomsource.nextDouble() / 4.0D * (double)(randomsource.nextBoolean() ? 1 : -1), (double)blockpos.getY() + 0.4D, (double)blockpos.getZ() + 0.5D + randomsource.nextDouble() / 4.0D * (double)(randomsource.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
      }

   }

   public static boolean isSmokeyPos(Level level, BlockPos blockpos) {
      for(int i = 1; i <= 5; ++i) {
         BlockPos blockpos1 = blockpos.below(i);
         BlockState blockstate = level.getBlockState(blockpos1);
         if (isLitCampfire(blockstate)) {
            return true;
         }

         boolean flag = Shapes.joinIsNotEmpty(VIRTUAL_FENCE_POST, blockstate.getCollisionShape(level, blockpos, CollisionContext.empty()), BooleanOp.AND);
         if (flag) {
            BlockState blockstate1 = level.getBlockState(blockpos1.below());
            return isLitCampfire(blockstate1);
         }
      }

      return false;
   }

   public static boolean isLitCampfire(BlockState blockstate) {
      return blockstate.hasProperty(LIT) && blockstate.is(BlockTags.CAMPFIRES) && blockstate.getValue(LIT);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new CampfireBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      if (level.isClientSide) {
         return blockstate.getValue(LIT) ? createTickerHelper(blockentitytype, BlockEntityType.CAMPFIRE, CampfireBlockEntity::particleTick) : null;
      } else {
         return blockstate.getValue(LIT) ? createTickerHelper(blockentitytype, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cookTick) : createTickerHelper(blockentitytype, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cooldownTick);
      }
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public static boolean canLight(BlockState blockstate) {
      return blockstate.is(BlockTags.CAMPFIRES, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(WATERLOGGED) && blockbehaviour_blockstatebase.hasProperty(LIT)) && !blockstate.getValue(WATERLOGGED) && !blockstate.getValue(LIT);
   }
}
