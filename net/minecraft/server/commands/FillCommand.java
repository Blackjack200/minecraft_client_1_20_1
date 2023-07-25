package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.fill.toobig", object, object1));
   static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), (CompoundTag)null);
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.fill.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("fill").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block(commandbuildcontext)).executes((commandcontext6) -> fillBlocks(commandcontext6.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext6, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext6, "to")), BlockStateArgument.getBlock(commandcontext6, "block"), FillCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null)).then(Commands.literal("replace").executes((commandcontext5) -> fillBlocks(commandcontext5.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext5, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext5, "to")), BlockStateArgument.getBlock(commandcontext5, "block"), FillCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null)).then(Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandbuildcontext)).executes((commandcontext4) -> fillBlocks(commandcontext4.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext4, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext4, "to")), BlockStateArgument.getBlock(commandcontext4, "block"), FillCommand.Mode.REPLACE, BlockPredicateArgument.getBlockPredicate(commandcontext4, "filter"))))).then(Commands.literal("keep").executes((commandcontext3) -> fillBlocks(commandcontext3.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext3, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext3, "to")), BlockStateArgument.getBlock(commandcontext3, "block"), FillCommand.Mode.REPLACE, (blockinworld) -> blockinworld.getLevel().isEmptyBlock(blockinworld.getPos())))).then(Commands.literal("outline").executes((commandcontext2) -> fillBlocks(commandcontext2.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext2, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext2, "to")), BlockStateArgument.getBlock(commandcontext2, "block"), FillCommand.Mode.OUTLINE, (Predicate<BlockInWorld>)null))).then(Commands.literal("hollow").executes((commandcontext1) -> fillBlocks(commandcontext1.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext1, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext1, "to")), BlockStateArgument.getBlock(commandcontext1, "block"), FillCommand.Mode.HOLLOW, (Predicate<BlockInWorld>)null))).then(Commands.literal("destroy").executes((commandcontext) -> fillBlocks(commandcontext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandcontext, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext, "to")), BlockStateArgument.getBlock(commandcontext, "block"), FillCommand.Mode.DESTROY, (Predicate<BlockInWorld>)null)))))));
   }

   private static int fillBlocks(CommandSourceStack commandsourcestack, BoundingBox boundingbox, BlockInput blockinput, FillCommand.Mode fillcommand_mode, @Nullable Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
      int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
      int j = commandsourcestack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
      if (i > j) {
         throw ERROR_AREA_TOO_LARGE.create(j, i);
      } else {
         List<BlockPos> list = Lists.newArrayList();
         ServerLevel serverlevel = commandsourcestack.getLevel();
         int k = 0;

         for(BlockPos blockpos : BlockPos.betweenClosed(boundingbox.minX(), boundingbox.minY(), boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ())) {
            if (predicate == null || predicate.test(new BlockInWorld(serverlevel, blockpos, true))) {
               BlockInput blockinput1 = fillcommand_mode.filter.filter(boundingbox, blockpos, blockinput, serverlevel);
               if (blockinput1 != null) {
                  BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
                  Clearable.tryClear(blockentity);
                  if (blockinput1.place(serverlevel, blockpos, 2)) {
                     list.add(blockpos.immutable());
                     ++k;
                  }
               }
            }
         }

         for(BlockPos blockpos1 : list) {
            Block block = serverlevel.getBlockState(blockpos1).getBlock();
            serverlevel.blockUpdated(blockpos1, block);
         }

         if (k == 0) {
            throw ERROR_FAILED.create();
         } else {
            int l = k;
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.fill.success", l), true);
            return k;
         }
      }
   }

   static enum Mode {
      REPLACE((boundingbox, blockpos, blockinput, serverlevel) -> blockinput),
      OUTLINE((boundingbox, blockpos, blockinput, serverlevel) -> blockpos.getX() != boundingbox.minX() && blockpos.getX() != boundingbox.maxX() && blockpos.getY() != boundingbox.minY() && blockpos.getY() != boundingbox.maxY() && blockpos.getZ() != boundingbox.minZ() && blockpos.getZ() != boundingbox.maxZ() ? null : blockinput),
      HOLLOW((boundingbox, blockpos, blockinput, serverlevel) -> blockpos.getX() != boundingbox.minX() && blockpos.getX() != boundingbox.maxX() && blockpos.getY() != boundingbox.minY() && blockpos.getY() != boundingbox.maxY() && blockpos.getZ() != boundingbox.minZ() && blockpos.getZ() != boundingbox.maxZ() ? FillCommand.HOLLOW_CORE : blockinput),
      DESTROY((boundingbox, blockpos, blockinput, serverlevel) -> {
         serverlevel.destroyBlock(blockpos, true);
         return blockinput;
      });

      public final SetBlockCommand.Filter filter;

      private Mode(SetBlockCommand.Filter setblockcommand_filter) {
         this.filter = setblockcommand_filter;
      }
   }
}
