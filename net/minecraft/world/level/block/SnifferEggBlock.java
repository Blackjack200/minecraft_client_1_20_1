package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnifferEggBlock extends Block {
   public static final int MAX_HATCH_LEVEL = 2;
   public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
   private static final int REGULAR_HATCH_TIME_TICKS = 24000;
   private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
   private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
   private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D);

   public SnifferEggBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HATCH);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public int getHatchLevel(BlockState blockstate) {
      return blockstate.getValue(HATCH);
   }

   private boolean isReadyToHatch(BlockState blockstate) {
      return this.getHatchLevel(blockstate) == 2;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!this.isReadyToHatch(blockstate)) {
         serverlevel.playSound((Player)null, blockpos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
         serverlevel.setBlock(blockpos, blockstate.setValue(HATCH, Integer.valueOf(this.getHatchLevel(blockstate) + 1)), 2);
      } else {
         serverlevel.playSound((Player)null, blockpos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + randomsource.nextFloat() * 0.2F);
         serverlevel.destroyBlock(blockpos, false);
         Sniffer sniffer = EntityType.SNIFFER.create(serverlevel);
         if (sniffer != null) {
            Vec3 vec3 = blockpos.getCenter();
            sniffer.setBaby(true);
            sniffer.moveTo(vec3.x(), vec3.y(), vec3.z(), Mth.wrapDegrees(serverlevel.random.nextFloat() * 360.0F), 0.0F);
            serverlevel.addFreshEntity(sniffer);
         }

      }
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      boolean flag1 = hatchBoost(level, blockpos);
      if (!level.isClientSide() && flag1) {
         level.levelEvent(3009, blockpos, 0);
      }

      int i = flag1 ? 12000 : 24000;
      int j = i / 3;
      level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(blockstate));
      level.scheduleTick(blockpos, this, j + level.random.nextInt(300));
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public static boolean hatchBoost(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos.below()).is(BlockTags.SNIFFER_EGG_HATCH_BOOST);
   }
}
