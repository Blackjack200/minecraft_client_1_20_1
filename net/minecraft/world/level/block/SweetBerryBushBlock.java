package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock extends BushBlock implements BonemealableBlock {
   private static final float HURT_SPEED_THRESHOLD = 0.003F;
   public static final int MAX_AGE = 3;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   private static final VoxelShape SAPLING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);
   private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public SweetBerryBushBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(Items.SWEET_BERRIES);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(AGE) == 0) {
         return SAPLING_SHAPE;
      } else {
         return blockstate.getValue(AGE) < 3 ? MID_GROWTH_SHAPE : super.getShape(blockstate, blockgetter, blockpos, collisioncontext);
      }
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(AGE) < 3;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = blockstate.getValue(AGE);
      if (i < 3 && randomsource.nextInt(5) == 0 && serverlevel.getRawBrightness(blockpos.above(), 0) >= 9) {
         BlockState blockstate1 = blockstate.setValue(AGE, Integer.valueOf(i + 1));
         serverlevel.setBlock(blockpos, blockstate1, 2);
         serverlevel.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate1));
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
         entity.makeStuckInBlock(blockstate, new Vec3((double)0.8F, 0.75D, (double)0.8F));
         if (!level.isClientSide && blockstate.getValue(AGE) > 0 && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            double d0 = Math.abs(entity.getX() - entity.xOld);
            double d1 = Math.abs(entity.getZ() - entity.zOld);
            if (d0 >= (double)0.003F || d1 >= (double)0.003F) {
               entity.hurt(level.damageSources().sweetBerryBush(), 1.0F);
            }
         }

      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      int i = blockstate.getValue(AGE);
      boolean flag = i == 3;
      if (!flag && player.getItemInHand(interactionhand).is(Items.BONE_MEAL)) {
         return InteractionResult.PASS;
      } else if (i > 1) {
         int j = 1 + level.random.nextInt(2);
         popResource(level, blockpos, new ItemStack(Items.SWEET_BERRIES, j + (flag ? 1 : 0)));
         level.playSound((Player)null, blockpos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
         BlockState blockstate1 = blockstate.setValue(AGE, Integer.valueOf(1));
         level.setBlock(blockpos, blockstate1, 2);
         level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockstate1));
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return super.use(blockstate, level, blockpos, player, interactionhand, blockhitresult);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return blockstate.getValue(AGE) < 3;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      int i = Math.min(3, blockstate.getValue(AGE) + 1);
      serverlevel.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(i)), 2);
   }
}
