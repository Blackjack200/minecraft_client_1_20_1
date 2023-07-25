package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
   private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.clone.toobig", object, object1));
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
   public static final Predicate<BlockInWorld> FILTER_AIR = (blockinworld) -> !blockinworld.getState().isAir();

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("clone").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(beginEndDestinationAndModeSuffix(commandbuildcontext, (commandcontext1) -> commandcontext1.getSource().getLevel())).then(Commands.literal("from").then(Commands.argument("sourceDimension", DimensionArgument.dimension()).then(beginEndDestinationAndModeSuffix(commandbuildcontext, (commandcontext) -> DimensionArgument.getDimension(commandcontext, "sourceDimension"))))));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext commandbuildcontext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> clonecommands_commandfunction) {
      return Commands.argument("begin", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(destinationAndModeSuffix(commandbuildcontext, clonecommands_commandfunction, (commandcontext1) -> commandcontext1.getSource().getLevel())).then(Commands.literal("to").then(Commands.argument("targetDimension", DimensionArgument.dimension()).then(destinationAndModeSuffix(commandbuildcontext, clonecommands_commandfunction, (commandcontext) -> DimensionArgument.getDimension(commandcontext, "targetDimension"))))));
   }

   private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> commandcontext, ServerLevel serverlevel, String s) throws CommandSyntaxException {
      BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(commandcontext, serverlevel, s);
      return new CloneCommands.DimensionAndPosition(serverlevel, blockpos);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(CommandBuildContext commandbuildcontext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> clonecommands_commandfunction, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> clonecommands_commandfunction1) {
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction2 = (commandcontext9) -> getLoadedDimensionAndPosition(commandcontext9, clonecommands_commandfunction.apply(commandcontext9), "begin");
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction3 = (commandcontext8) -> getLoadedDimensionAndPosition(commandcontext8, clonecommands_commandfunction.apply(commandcontext8), "end");
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction4 = (commandcontext7) -> getLoadedDimensionAndPosition(commandcontext7, clonecommands_commandfunction1.apply(commandcontext7), "destination");
      return Commands.argument("destination", BlockPosArgument.blockPos()).executes((commandcontext6) -> clone(commandcontext6.getSource(), clonecommands_commandfunction2.apply(commandcontext6), clonecommands_commandfunction3.apply(commandcontext6), clonecommands_commandfunction4.apply(commandcontext6), (blockinworld2) -> true, CloneCommands.Mode.NORMAL)).then(wrapWithCloneMode(clonecommands_commandfunction2, clonecommands_commandfunction3, clonecommands_commandfunction4, (commandcontext5) -> (blockinworld1) -> true, Commands.literal("replace").executes((commandcontext4) -> clone(commandcontext4.getSource(), clonecommands_commandfunction2.apply(commandcontext4), clonecommands_commandfunction3.apply(commandcontext4), clonecommands_commandfunction4.apply(commandcontext4), (blockinworld) -> true, CloneCommands.Mode.NORMAL)))).then(wrapWithCloneMode(clonecommands_commandfunction2, clonecommands_commandfunction3, clonecommands_commandfunction4, (commandcontext3) -> FILTER_AIR, Commands.literal("masked").executes((commandcontext2) -> clone(commandcontext2.getSource(), clonecommands_commandfunction2.apply(commandcontext2), clonecommands_commandfunction3.apply(commandcontext2), clonecommands_commandfunction4.apply(commandcontext2), FILTER_AIR, CloneCommands.Mode.NORMAL)))).then(Commands.literal("filtered").then(wrapWithCloneMode(clonecommands_commandfunction2, clonecommands_commandfunction3, clonecommands_commandfunction4, (commandcontext1) -> BlockPredicateArgument.getBlockPredicate(commandcontext1, "filter"), Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandbuildcontext)).executes((commandcontext) -> clone(commandcontext.getSource(), clonecommands_commandfunction2.apply(commandcontext), clonecommands_commandfunction3.apply(commandcontext), clonecommands_commandfunction4.apply(commandcontext), BlockPredicateArgument.getBlockPredicate(commandcontext, "filter"), CloneCommands.Mode.NORMAL)))));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction1, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> clonecommands_commandfunction2, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> clonecommands_commandfunction3, ArgumentBuilder<CommandSourceStack, ?> argumentbuilder) {
      return argumentbuilder.then(Commands.literal("force").executes((commandcontext2) -> clone(commandcontext2.getSource(), clonecommands_commandfunction.apply(commandcontext2), clonecommands_commandfunction1.apply(commandcontext2), clonecommands_commandfunction2.apply(commandcontext2), clonecommands_commandfunction3.apply(commandcontext2), CloneCommands.Mode.FORCE))).then(Commands.literal("move").executes((commandcontext1) -> clone(commandcontext1.getSource(), clonecommands_commandfunction.apply(commandcontext1), clonecommands_commandfunction1.apply(commandcontext1), clonecommands_commandfunction2.apply(commandcontext1), clonecommands_commandfunction3.apply(commandcontext1), CloneCommands.Mode.MOVE))).then(Commands.literal("normal").executes((commandcontext) -> clone(commandcontext.getSource(), clonecommands_commandfunction.apply(commandcontext), clonecommands_commandfunction1.apply(commandcontext), clonecommands_commandfunction2.apply(commandcontext), clonecommands_commandfunction3.apply(commandcontext), CloneCommands.Mode.NORMAL)));
   }

   private static int clone(CommandSourceStack commandsourcestack, CloneCommands.DimensionAndPosition clonecommands_dimensionandposition, CloneCommands.DimensionAndPosition clonecommands_dimensionandposition1, CloneCommands.DimensionAndPosition clonecommands_dimensionandposition2, Predicate<BlockInWorld> predicate, CloneCommands.Mode clonecommands_mode) throws CommandSyntaxException {
      BlockPos blockpos = clonecommands_dimensionandposition.position();
      BlockPos blockpos1 = clonecommands_dimensionandposition1.position();
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
      BlockPos blockpos2 = clonecommands_dimensionandposition2.position();
      BlockPos blockpos3 = blockpos2.offset(boundingbox.getLength());
      BoundingBox boundingbox1 = BoundingBox.fromCorners(blockpos2, blockpos3);
      ServerLevel serverlevel = clonecommands_dimensionandposition.dimension();
      ServerLevel serverlevel1 = clonecommands_dimensionandposition2.dimension();
      if (!clonecommands_mode.canOverlap() && serverlevel == serverlevel1 && boundingbox1.intersects(boundingbox)) {
         throw ERROR_OVERLAP.create();
      } else {
         int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
         int j = commandsourcestack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
         if (i > j) {
            throw ERROR_AREA_TOO_LARGE.create(j, i);
         } else if (serverlevel.hasChunksAt(blockpos, blockpos1) && serverlevel1.hasChunksAt(blockpos2, blockpos3)) {
            List<CloneCommands.CloneBlockInfo> list = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> list1 = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> list2 = Lists.newArrayList();
            Deque<BlockPos> deque = Lists.newLinkedList();
            BlockPos blockpos4 = new BlockPos(boundingbox1.minX() - boundingbox.minX(), boundingbox1.minY() - boundingbox.minY(), boundingbox1.minZ() - boundingbox.minZ());

            for(int k = boundingbox.minZ(); k <= boundingbox.maxZ(); ++k) {
               for(int l = boundingbox.minY(); l <= boundingbox.maxY(); ++l) {
                  for(int i1 = boundingbox.minX(); i1 <= boundingbox.maxX(); ++i1) {
                     BlockPos blockpos5 = new BlockPos(i1, l, k);
                     BlockPos blockpos6 = blockpos5.offset(blockpos4);
                     BlockInWorld blockinworld = new BlockInWorld(serverlevel, blockpos5, false);
                     BlockState blockstate = blockinworld.getState();
                     if (predicate.test(blockinworld)) {
                        BlockEntity blockentity = serverlevel.getBlockEntity(blockpos5);
                        if (blockentity != null) {
                           CompoundTag compoundtag = blockentity.saveWithoutMetadata();
                           list1.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, compoundtag));
                           deque.addLast(blockpos5);
                        } else if (!blockstate.isSolidRender(serverlevel, blockpos5) && !blockstate.isCollisionShapeFullBlock(serverlevel, blockpos5)) {
                           list2.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, (CompoundTag)null));
                           deque.addFirst(blockpos5);
                        } else {
                           list.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, (CompoundTag)null));
                           deque.addLast(blockpos5);
                        }
                     }
                  }
               }
            }

            if (clonecommands_mode == CloneCommands.Mode.MOVE) {
               for(BlockPos blockpos7 : deque) {
                  BlockEntity blockentity1 = serverlevel.getBlockEntity(blockpos7);
                  Clearable.tryClear(blockentity1);
                  serverlevel.setBlock(blockpos7, Blocks.BARRIER.defaultBlockState(), 2);
               }

               for(BlockPos blockpos8 : deque) {
                  serverlevel.setBlock(blockpos8, Blocks.AIR.defaultBlockState(), 3);
               }
            }

            List<CloneCommands.CloneBlockInfo> list3 = Lists.newArrayList();
            list3.addAll(list);
            list3.addAll(list1);
            list3.addAll(list2);
            List<CloneCommands.CloneBlockInfo> list4 = Lists.reverse(list3);

            for(CloneCommands.CloneBlockInfo clonecommands_cloneblockinfo : list4) {
               BlockEntity blockentity2 = serverlevel1.getBlockEntity(clonecommands_cloneblockinfo.pos);
               Clearable.tryClear(blockentity2);
               serverlevel1.setBlock(clonecommands_cloneblockinfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
            }

            int j1 = 0;

            for(CloneCommands.CloneBlockInfo clonecommands_cloneblockinfo1 : list3) {
               if (serverlevel1.setBlock(clonecommands_cloneblockinfo1.pos, clonecommands_cloneblockinfo1.state, 2)) {
                  ++j1;
               }
            }

            for(CloneCommands.CloneBlockInfo clonecommands_cloneblockinfo2 : list1) {
               BlockEntity blockentity3 = serverlevel1.getBlockEntity(clonecommands_cloneblockinfo2.pos);
               if (clonecommands_cloneblockinfo2.tag != null && blockentity3 != null) {
                  blockentity3.load(clonecommands_cloneblockinfo2.tag);
                  blockentity3.setChanged();
               }

               serverlevel1.setBlock(clonecommands_cloneblockinfo2.pos, clonecommands_cloneblockinfo2.state, 2);
            }

            for(CloneCommands.CloneBlockInfo clonecommands_cloneblockinfo3 : list4) {
               serverlevel1.blockUpdated(clonecommands_cloneblockinfo3.pos, clonecommands_cloneblockinfo3.state.getBlock());
            }

            serverlevel1.getBlockTicks().copyAreaFrom(serverlevel.getBlockTicks(), boundingbox, blockpos4);
            if (j1 == 0) {
               throw ERROR_FAILED.create();
            } else {
               int k1 = j1;
               commandsourcestack.sendSuccess(() -> Component.translatable("commands.clone.success", k1), true);
               return j1;
            }
         } else {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
         }
      }
   }

   static class CloneBlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      @Nullable
      public final CompoundTag tag;

      public CloneBlockInfo(BlockPos blockpos, BlockState blockstate, @Nullable CompoundTag compoundtag) {
         this.pos = blockpos;
         this.state = blockstate;
         this.tag = compoundtag;
      }
   }

   @FunctionalInterface
   interface CommandFunction<T, R> {
      R apply(T object) throws CommandSyntaxException;
   }

   static record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
   }

   static enum Mode {
      FORCE(true),
      MOVE(true),
      NORMAL(false);

      private final boolean canOverlap;

      private Mode(boolean flag) {
         this.canOverlap = flag;
      }

      public boolean canOverlap() {
         return this.canOverlap;
      }
   }
}
