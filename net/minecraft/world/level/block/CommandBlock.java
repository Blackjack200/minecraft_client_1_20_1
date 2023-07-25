package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;

public class CommandBlock extends BaseEntityBlock implements GameMasterBlock {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final DirectionProperty FACING = DirectionalBlock.FACING;
   public static final BooleanProperty CONDITIONAL = BlockStateProperties.CONDITIONAL;
   private final boolean automatic;

   public CommandBlock(BlockBehaviour.Properties blockbehaviour_properties, boolean flag) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONDITIONAL, Boolean.valueOf(false)));
      this.automatic = flag;
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      CommandBlockEntity commandblockentity = new CommandBlockEntity(blockpos, blockstate);
      commandblockentity.setAutomatic(this.automatic);
      return commandblockentity;
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (!level.isClientSide) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof CommandBlockEntity) {
            CommandBlockEntity commandblockentity = (CommandBlockEntity)blockentity;
            boolean flag1 = level.hasNeighborSignal(blockpos);
            boolean flag2 = commandblockentity.isPowered();
            commandblockentity.setPowered(flag1);
            if (!flag2 && !commandblockentity.isAutomatic() && commandblockentity.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
               if (flag1) {
                  commandblockentity.markConditionMet();
                  level.scheduleTick(blockpos, this, 1);
               }

            }
         }
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
      if (blockentity instanceof CommandBlockEntity commandblockentity) {
         BaseCommandBlock basecommandblock = commandblockentity.getCommandBlock();
         boolean flag = !StringUtil.isNullOrEmpty(basecommandblock.getCommand());
         CommandBlockEntity.Mode commandblockentity_mode = commandblockentity.getMode();
         boolean flag1 = commandblockentity.wasConditionMet();
         if (commandblockentity_mode == CommandBlockEntity.Mode.AUTO) {
            commandblockentity.markConditionMet();
            if (flag1) {
               this.execute(blockstate, serverlevel, blockpos, basecommandblock, flag);
            } else if (commandblockentity.isConditional()) {
               basecommandblock.setSuccessCount(0);
            }

            if (commandblockentity.isPowered() || commandblockentity.isAutomatic()) {
               serverlevel.scheduleTick(blockpos, this, 1);
            }
         } else if (commandblockentity_mode == CommandBlockEntity.Mode.REDSTONE) {
            if (flag1) {
               this.execute(blockstate, serverlevel, blockpos, basecommandblock, flag);
            } else if (commandblockentity.isConditional()) {
               basecommandblock.setSuccessCount(0);
            }
         }

         serverlevel.updateNeighbourForOutputSignal(blockpos, this);
      }

   }

   private void execute(BlockState blockstate, Level level, BlockPos blockpos, BaseCommandBlock basecommandblock, boolean flag) {
      if (flag) {
         basecommandblock.performCommand(level);
      } else {
         basecommandblock.setSuccessCount(0);
      }

      executeChain(level, blockpos, blockstate.getValue(FACING));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof CommandBlockEntity && player.canUseGameMasterBlocks()) {
         player.openCommandBlock((CommandBlockEntity)blockentity);
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      return blockentity instanceof CommandBlockEntity ? ((CommandBlockEntity)blockentity).getCommandBlock().getSuccessCount() : 0;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof CommandBlockEntity commandblockentity) {
         BaseCommandBlock basecommandblock = commandblockentity.getCommandBlock();
         if (itemstack.hasCustomHoverName()) {
            basecommandblock.setName(itemstack.getHoverName());
         }

         if (!level.isClientSide) {
            if (BlockItem.getBlockEntityData(itemstack) == null) {
               basecommandblock.setTrackOutput(level.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK));
               commandblockentity.setAutomatic(this.automatic);
            }

            if (commandblockentity.getMode() == CommandBlockEntity.Mode.SEQUENCE) {
               boolean flag = level.hasNeighborSignal(blockpos);
               commandblockentity.setPowered(flag);
            }
         }

      }
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, CONDITIONAL);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getNearestLookingDirection().getOpposite());
   }

   private static void executeChain(Level level, BlockPos blockpos, Direction direction) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      GameRules gamerules = level.getGameRules();

      int i;
      BlockState blockstate;
      for(i = gamerules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH); i-- > 0; direction = blockstate.getValue(FACING)) {
         blockpos_mutableblockpos.move(direction);
         blockstate = level.getBlockState(blockpos_mutableblockpos);
         Block block = blockstate.getBlock();
         if (!blockstate.is(Blocks.CHAIN_COMMAND_BLOCK)) {
            break;
         }

         BlockEntity blockentity = level.getBlockEntity(blockpos_mutableblockpos);
         if (!(blockentity instanceof CommandBlockEntity)) {
            break;
         }

         CommandBlockEntity commandblockentity = (CommandBlockEntity)blockentity;
         if (commandblockentity.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
            break;
         }

         if (commandblockentity.isPowered() || commandblockentity.isAutomatic()) {
            BaseCommandBlock basecommandblock = commandblockentity.getCommandBlock();
            if (commandblockentity.markConditionMet()) {
               if (!basecommandblock.performCommand(level)) {
                  break;
               }

               level.updateNeighbourForOutputSignal(blockpos_mutableblockpos, block);
            } else if (commandblockentity.isConditional()) {
               basecommandblock.setSuccessCount(0);
            }
         }
      }

      if (i <= 0) {
         int j = Math.max(gamerules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH), 0);
         LOGGER.warn("Command Block chain tried to execute more than {} steps!", (int)j);
      }

   }
}
