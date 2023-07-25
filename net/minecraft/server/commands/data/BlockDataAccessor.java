package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDataAccessor implements DataAccessor {
   static final SimpleCommandExceptionType ERROR_NOT_A_BLOCK_ENTITY = new SimpleCommandExceptionType(Component.translatable("commands.data.block.invalid"));
   public static final Function<String, DataCommands.DataProvider> PROVIDER = (s) -> new DataCommands.DataProvider() {
         public DataAccessor access(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
            BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(commandcontext, s + "Pos");
            BlockEntity blockentity = commandcontext.getSource().getLevel().getBlockEntity(blockpos);
            if (blockentity == null) {
               throw BlockDataAccessor.ERROR_NOT_A_BLOCK_ENTITY.create();
            } else {
               return new BlockDataAccessor(blockentity, blockpos);
            }
         }

         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
            return argumentbuilder.then(Commands.literal("block").then(function.apply(Commands.argument(s + "Pos", BlockPosArgument.blockPos()))));
         }
      };
   private final BlockEntity entity;
   private final BlockPos pos;

   public BlockDataAccessor(BlockEntity blockentity, BlockPos blockpos) {
      this.entity = blockentity;
      this.pos = blockpos;
   }

   public void setData(CompoundTag compoundtag) {
      BlockState blockstate = this.entity.getLevel().getBlockState(this.pos);
      this.entity.load(compoundtag);
      this.entity.setChanged();
      this.entity.getLevel().sendBlockUpdated(this.pos, blockstate, blockstate, 3);
   }

   public CompoundTag getData() {
      return this.entity.saveWithFullMetadata();
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
   }

   public Component getPrintSuccess(Tag tag) {
      return Component.translatable("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtUtils.toPrettyComponent(tag));
   }

   public Component getPrintSuccess(NbtPathArgument.NbtPath nbtpathargument_nbtpath, double d0, int i) {
      return Component.translatable("commands.data.block.get", nbtpathargument_nbtpath, this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", d0), i);
   }
}
