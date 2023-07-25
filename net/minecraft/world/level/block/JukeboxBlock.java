package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
   public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

   protected JukeboxBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      super.setPlacedBy(level, blockpos, blockstate, livingentity, itemstack);
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null && compoundtag.contains("RecordItem")) {
         level.setBlock(blockpos, blockstate.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
      }

   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (blockstate.getValue(HAS_RECORD)) {
         BlockEntity var8 = level.getBlockEntity(blockpos);
         if (var8 instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)var8;
            jukeboxblockentity.popOutRecord();
            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity var7 = level.getBlockEntity(blockpos);
         if (var7 instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)var7;
            jukeboxblockentity.popOutRecord();
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new JukeboxBlockEntity(blockpos, blockstate);
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      BlockEntity var6 = blockgetter.getBlockEntity(blockpos);
      if (var6 instanceof JukeboxBlockEntity jukeboxblockentity) {
         if (jukeboxblockentity.isRecordPlaying()) {
            return 15;
         }
      }

      return 0;
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity var6 = level.getBlockEntity(blockpos);
      if (var6 instanceof JukeboxBlockEntity jukeboxblockentity) {
         Item var7 = jukeboxblockentity.getFirstItem().getItem();
         if (var7 instanceof RecordItem recorditem) {
            return recorditem.getAnalogOutput();
         }
      }

      return 0;
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(HAS_RECORD);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return blockstate.getValue(HAS_RECORD) ? createTickerHelper(blockentitytype, BlockEntityType.JUKEBOX, JukeboxBlockEntity::playRecordTick) : null;
   }
}
