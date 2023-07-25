package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BrushableBlock extends BaseEntityBlock implements Fallable {
   private static final IntegerProperty DUSTED = BlockStateProperties.DUSTED;
   public static final int TICK_DELAY = 2;
   private final Block turnsInto;
   private final SoundEvent brushSound;
   private final SoundEvent brushCompletedSound;

   public BrushableBlock(Block block, BlockBehaviour.Properties blockbehaviour_properties, SoundEvent soundevent, SoundEvent soundevent1) {
      super(blockbehaviour_properties);
      this.turnsInto = block;
      this.brushSound = soundevent;
      this.brushCompletedSound = soundevent1;
      this.registerDefaultState(this.stateDefinition.any().setValue(DUSTED, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(DUSTED);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      level.scheduleTick(blockpos, this, 2);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      levelaccessor.scheduleTick(blockpos, this, 2);
      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockEntity var6 = serverlevel.getBlockEntity(blockpos);
      if (var6 instanceof BrushableBlockEntity brushableblockentity) {
         brushableblockentity.checkReset();
      }

      if (FallingBlock.isFree(serverlevel.getBlockState(blockpos.below())) && blockpos.getY() >= serverlevel.getMinBuildHeight()) {
         FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(serverlevel, blockpos, blockstate);
         fallingblockentity.disableDrop();
      }
   }

   public void onBrokenAfterFall(Level level, BlockPos blockpos, FallingBlockEntity fallingblockentity) {
      Vec3 vec3 = fallingblockentity.getBoundingBox().getCenter();
      level.levelEvent(2001, BlockPos.containing(vec3), Block.getId(fallingblockentity.getBlockState()));
      level.gameEvent(fallingblockentity, GameEvent.BLOCK_DESTROY, vec3);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(16) == 0) {
         BlockPos blockpos1 = blockpos.below();
         if (FallingBlock.isFree(level.getBlockState(blockpos1))) {
            double d0 = (double)blockpos.getX() + randomsource.nextDouble();
            double d1 = (double)blockpos.getY() - 0.05D;
            double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
            level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, blockstate), d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public @Nullable BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BrushableBlockEntity(blockpos, blockstate);
   }

   public Block getTurnsInto() {
      return this.turnsInto;
   }

   public SoundEvent getBrushSound() {
      return this.brushSound;
   }

   public SoundEvent getBrushCompletedSound() {
      return this.brushCompletedSound;
   }
}
