package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class NoteBlock extends Block {
   public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final IntegerProperty NOTE = BlockStateProperties.NOTE;
   public static final int NOTE_VOLUME = 3;

   public NoteBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP).setValue(NOTE, Integer.valueOf(0)).setValue(POWERED, Boolean.valueOf(false)));
   }

   private BlockState setInstrument(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      NoteBlockInstrument noteblockinstrument = levelaccessor.getBlockState(blockpos.above()).instrument();
      if (noteblockinstrument.worksAboveNoteBlock()) {
         return blockstate.setValue(INSTRUMENT, noteblockinstrument);
      } else {
         NoteBlockInstrument noteblockinstrument1 = levelaccessor.getBlockState(blockpos.below()).instrument();
         NoteBlockInstrument noteblockinstrument2 = noteblockinstrument1.worksAboveNoteBlock() ? NoteBlockInstrument.HARP : noteblockinstrument1;
         return blockstate.setValue(INSTRUMENT, noteblockinstrument2);
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.setInstrument(blockplacecontext.getLevel(), blockplacecontext.getClickedPos(), this.defaultBlockState());
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      boolean flag = direction.getAxis() == Direction.Axis.Y;
      return flag ? this.setInstrument(levelaccessor, blockpos, blockstate) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      boolean flag1 = level.hasNeighborSignal(blockpos);
      if (flag1 != blockstate.getValue(POWERED)) {
         if (flag1) {
            this.playNote((Entity)null, blockstate, level, blockpos);
         }

         level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag1)), 3);
      }

   }

   private void playNote(@Nullable Entity entity, BlockState blockstate, Level level, BlockPos blockpos) {
      if (blockstate.getValue(INSTRUMENT).worksAboveNoteBlock() || level.getBlockState(blockpos.above()).isAir()) {
         level.blockEvent(blockpos, this, 0, 0);
         level.gameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, blockpos);
      }

   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (itemstack.is(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS) && blockhitresult.getDirection() == Direction.UP) {
         return InteractionResult.PASS;
      } else if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         blockstate = blockstate.cycle(NOTE);
         level.setBlock(blockpos, blockstate, 3);
         this.playNote(player, blockstate, level, blockpos);
         player.awardStat(Stats.TUNE_NOTEBLOCK);
         return InteractionResult.CONSUME;
      }
   }

   public void attack(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      if (!level.isClientSide) {
         this.playNote(player, blockstate, level, blockpos);
         player.awardStat(Stats.PLAY_NOTEBLOCK);
      }
   }

   public static float getPitchFromNote(int i) {
      return (float)Math.pow(2.0D, (double)(i - 12) / 12.0D);
   }

   public boolean triggerEvent(BlockState blockstate, Level level, BlockPos blockpos, int i, int j) {
      NoteBlockInstrument noteblockinstrument = blockstate.getValue(INSTRUMENT);
      float f;
      if (noteblockinstrument.isTunable()) {
         int k = blockstate.getValue(NOTE);
         f = getPitchFromNote(k);
         level.addParticle(ParticleTypes.NOTE, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 1.2D, (double)blockpos.getZ() + 0.5D, (double)k / 24.0D, 0.0D, 0.0D);
      } else {
         f = 1.0F;
      }

      Holder<SoundEvent> holder;
      if (noteblockinstrument.hasCustomSound()) {
         ResourceLocation resourcelocation = this.getCustomSoundId(level, blockpos);
         if (resourcelocation == null) {
            return false;
         }

         holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourcelocation));
      } else {
         holder = noteblockinstrument.getSoundEvent();
      }

      level.playSeededSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, holder, SoundSource.RECORDS, 3.0F, f, level.random.nextLong());
      return true;
   }

   @Nullable
   private ResourceLocation getCustomSoundId(Level level, BlockPos blockpos) {
      BlockEntity var4 = level.getBlockEntity(blockpos.above());
      if (var4 instanceof SkullBlockEntity skullblockentity) {
         return skullblockentity.getNoteBlockSound();
      } else {
         return null;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(INSTRUMENT, POWERED, NOTE);
   }
}
