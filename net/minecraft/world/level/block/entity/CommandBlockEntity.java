package net.minecraft.world.level.block.entity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandBlockEntity extends BlockEntity {
   private boolean powered;
   private boolean auto;
   private boolean conditionMet;
   private final BaseCommandBlock commandBlock = new BaseCommandBlock() {
      public void setCommand(String s) {
         super.setCommand(s);
         CommandBlockEntity.this.setChanged();
      }

      public ServerLevel getLevel() {
         return (ServerLevel)CommandBlockEntity.this.level;
      }

      public void onUpdated() {
         BlockState blockstate = CommandBlockEntity.this.level.getBlockState(CommandBlockEntity.this.worldPosition);
         this.getLevel().sendBlockUpdated(CommandBlockEntity.this.worldPosition, blockstate, blockstate, 3);
      }

      public Vec3 getPosition() {
         return Vec3.atCenterOf(CommandBlockEntity.this.worldPosition);
      }

      public CommandSourceStack createCommandSourceStack() {
         Direction direction = CommandBlockEntity.this.getBlockState().getValue(CommandBlock.FACING);
         return new CommandSourceStack(this, Vec3.atCenterOf(CommandBlockEntity.this.worldPosition), new Vec2(0.0F, direction.toYRot()), this.getLevel(), 2, this.getName().getString(), this.getName(), this.getLevel().getServer(), (Entity)null);
      }

      public boolean isValid() {
         return !CommandBlockEntity.this.isRemoved();
      }
   };

   public CommandBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.COMMAND_BLOCK, blockpos, blockstate);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      this.commandBlock.save(compoundtag);
      compoundtag.putBoolean("powered", this.isPowered());
      compoundtag.putBoolean("conditionMet", this.wasConditionMet());
      compoundtag.putBoolean("auto", this.isAutomatic());
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.commandBlock.load(compoundtag);
      this.powered = compoundtag.getBoolean("powered");
      this.conditionMet = compoundtag.getBoolean("conditionMet");
      this.setAutomatic(compoundtag.getBoolean("auto"));
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public BaseCommandBlock getCommandBlock() {
      return this.commandBlock;
   }

   public void setPowered(boolean flag) {
      this.powered = flag;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAutomatic() {
      return this.auto;
   }

   public void setAutomatic(boolean flag) {
      boolean flag1 = this.auto;
      this.auto = flag;
      if (!flag1 && flag && !this.powered && this.level != null && this.getMode() != CommandBlockEntity.Mode.SEQUENCE) {
         this.scheduleTick();
      }

   }

   public void onModeSwitch() {
      CommandBlockEntity.Mode commandblockentity_mode = this.getMode();
      if (commandblockentity_mode == CommandBlockEntity.Mode.AUTO && (this.powered || this.auto) && this.level != null) {
         this.scheduleTick();
      }

   }

   private void scheduleTick() {
      Block block = this.getBlockState().getBlock();
      if (block instanceof CommandBlock) {
         this.markConditionMet();
         this.level.scheduleTick(this.worldPosition, block, 1);
      }

   }

   public boolean wasConditionMet() {
      return this.conditionMet;
   }

   public boolean markConditionMet() {
      this.conditionMet = true;
      if (this.isConditional()) {
         BlockPos blockpos = this.worldPosition.relative(this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING).getOpposite());
         if (this.level.getBlockState(blockpos).getBlock() instanceof CommandBlock) {
            BlockEntity blockentity = this.level.getBlockEntity(blockpos);
            this.conditionMet = blockentity instanceof CommandBlockEntity && ((CommandBlockEntity)blockentity).getCommandBlock().getSuccessCount() > 0;
         } else {
            this.conditionMet = false;
         }
      }

      return this.conditionMet;
   }

   public CommandBlockEntity.Mode getMode() {
      BlockState blockstate = this.getBlockState();
      if (blockstate.is(Blocks.COMMAND_BLOCK)) {
         return CommandBlockEntity.Mode.REDSTONE;
      } else if (blockstate.is(Blocks.REPEATING_COMMAND_BLOCK)) {
         return CommandBlockEntity.Mode.AUTO;
      } else {
         return blockstate.is(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockEntity.Mode.SEQUENCE : CommandBlockEntity.Mode.REDSTONE;
      }
   }

   public boolean isConditional() {
      BlockState blockstate = this.level.getBlockState(this.getBlockPos());
      return blockstate.getBlock() instanceof CommandBlock ? blockstate.getValue(CommandBlock.CONDITIONAL) : false;
   }

   public static enum Mode {
      SEQUENCE,
      AUTO,
      REDSTONE;
   }
}
