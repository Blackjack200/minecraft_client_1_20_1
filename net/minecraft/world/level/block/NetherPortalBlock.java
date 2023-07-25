package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherPortalBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   protected static final int AABB_OFFSET = 2;
   protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

   public NetherPortalBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch ((Direction.Axis)blockstate.getValue(AXIS)) {
         case Z:
            return Z_AXIS_AABB;
         case X:
         default:
            return X_AXIS_AABB;
      }
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.dimensionType().natural() && serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && randomsource.nextInt(2000) < serverlevel.getDifficulty().getId()) {
         while(serverlevel.getBlockState(blockpos).is(this)) {
            blockpos = blockpos.below();
         }

         if (serverlevel.getBlockState(blockpos).isValidSpawn(serverlevel, blockpos, EntityType.ZOMBIFIED_PIGLIN)) {
            Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(serverlevel, blockpos.above(), MobSpawnType.STRUCTURE);
            if (entity != null) {
               entity.setPortalCooldown();
            }
         }
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      Direction.Axis direction_axis = direction.getAxis();
      Direction.Axis direction_axis1 = blockstate.getValue(AXIS);
      boolean flag = direction_axis1 != direction_axis && direction_axis.isHorizontal();
      return !flag && !blockstate1.is(this) && !(new PortalShape(levelaccessor, blockpos, direction_axis1)).isComplete() ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (entity.canChangeDimensions()) {
         entity.handleInsidePortal(blockpos);
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(100) == 0) {
         level.playLocalSound((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, randomsource.nextFloat() * 0.4F + 0.8F, false);
      }

      for(int i = 0; i < 4; ++i) {
         double d0 = (double)blockpos.getX() + randomsource.nextDouble();
         double d1 = (double)blockpos.getY() + randomsource.nextDouble();
         double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
         double d3 = ((double)randomsource.nextFloat() - 0.5D) * 0.5D;
         double d4 = ((double)randomsource.nextFloat() - 0.5D) * 0.5D;
         double d5 = ((double)randomsource.nextFloat() - 0.5D) * 0.5D;
         int j = randomsource.nextInt(2) * 2 - 1;
         if (!level.getBlockState(blockpos.west()).is(this) && !level.getBlockState(blockpos.east()).is(this)) {
            d0 = (double)blockpos.getX() + 0.5D + 0.25D * (double)j;
            d3 = (double)(randomsource.nextFloat() * 2.0F * (float)j);
         } else {
            d2 = (double)blockpos.getZ() + 0.5D + 0.25D * (double)j;
            d5 = (double)(randomsource.nextFloat() * 2.0F * (float)j);
         }

         level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
      }

   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return ItemStack.EMPTY;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Direction.Axis)blockstate.getValue(AXIS)) {
               case Z:
                  return blockstate.setValue(AXIS, Direction.Axis.X);
               case X:
                  return blockstate.setValue(AXIS, Direction.Axis.Z);
               default:
                  return blockstate;
            }
         default:
            return blockstate;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AXIS);
   }
}
