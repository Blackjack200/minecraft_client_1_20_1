package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.setblock.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("setblock").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block(commandbuildcontext)).executes((commandcontext3) -> setBlock(commandcontext3.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext3, "pos"), BlockStateArgument.getBlock(commandcontext3, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null)).then(Commands.literal("destroy").executes((commandcontext2) -> setBlock(commandcontext2.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext2, "pos"), BlockStateArgument.getBlock(commandcontext2, "block"), SetBlockCommand.Mode.DESTROY, (Predicate<BlockInWorld>)null))).then(Commands.literal("keep").executes((commandcontext1) -> setBlock(commandcontext1.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext1, "pos"), BlockStateArgument.getBlock(commandcontext1, "block"), SetBlockCommand.Mode.REPLACE, (blockinworld) -> blockinworld.getLevel().isEmptyBlock(blockinworld.getPos())))).then(Commands.literal("replace").executes((commandcontext) -> setBlock(commandcontext.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext, "pos"), BlockStateArgument.getBlock(commandcontext, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null))))));
   }

   private static int setBlock(CommandSourceStack commandsourcestack, BlockPos blockpos, BlockInput blockinput, SetBlockCommand.Mode setblockcommand_mode, @Nullable Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      if (predicate != null && !predicate.test(new BlockInWorld(serverlevel, blockpos, true))) {
         throw ERROR_FAILED.create();
      } else {
         boolean flag;
         if (setblockcommand_mode == SetBlockCommand.Mode.DESTROY) {
            serverlevel.destroyBlock(blockpos, true);
            flag = !blockinput.getState().isAir() || !serverlevel.getBlockState(blockpos).isAir();
         } else {
            BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
            Clearable.tryClear(blockentity);
            flag = true;
         }

         if (flag && !blockinput.place(serverlevel, blockpos, 2)) {
            throw ERROR_FAILED.create();
         } else {
            serverlevel.blockUpdated(blockpos, blockinput.getState().getBlock());
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.setblock.success", blockpos.getX(), blockpos.getY(), blockpos.getZ()), true);
            return 1;
         }
      }
   }

   public interface Filter {
      @Nullable
      BlockInput filter(BoundingBox boundingbox, BlockPos blockpos, BlockInput blockinput, ServerLevel serverlevel);
   }

   public static enum Mode {
      REPLACE,
      DESTROY;
   }
}
