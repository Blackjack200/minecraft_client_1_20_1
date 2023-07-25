package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
   private static final int MAX_CHUNK_LIMIT = 256;
   private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.forceload.toobig", object, object1));
   private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.forceload.query.failure", object, object1));
   private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.added.failure"));
   private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.removed.failure"));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("forceload").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("add").then(Commands.argument("from", ColumnPosArgument.columnPos()).executes((commandcontext6) -> changeForceLoad(commandcontext6.getSource(), ColumnPosArgument.getColumnPos(commandcontext6, "from"), ColumnPosArgument.getColumnPos(commandcontext6, "from"), true)).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes((commandcontext5) -> changeForceLoad(commandcontext5.getSource(), ColumnPosArgument.getColumnPos(commandcontext5, "from"), ColumnPosArgument.getColumnPos(commandcontext5, "to"), true))))).then(Commands.literal("remove").then(Commands.argument("from", ColumnPosArgument.columnPos()).executes((commandcontext4) -> changeForceLoad(commandcontext4.getSource(), ColumnPosArgument.getColumnPos(commandcontext4, "from"), ColumnPosArgument.getColumnPos(commandcontext4, "from"), false)).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes((commandcontext3) -> changeForceLoad(commandcontext3.getSource(), ColumnPosArgument.getColumnPos(commandcontext3, "from"), ColumnPosArgument.getColumnPos(commandcontext3, "to"), false)))).then(Commands.literal("all").executes((commandcontext2) -> removeAll(commandcontext2.getSource())))).then(Commands.literal("query").executes((commandcontext1) -> listForceLoad(commandcontext1.getSource())).then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes((commandcontext) -> queryForceLoad(commandcontext.getSource(), ColumnPosArgument.getColumnPos(commandcontext, "pos"))))));
   }

   private static int queryForceLoad(CommandSourceStack commandsourcestack, ColumnPos columnpos) throws CommandSyntaxException {
      ChunkPos chunkpos = columnpos.toChunkPos();
      ServerLevel serverlevel = commandsourcestack.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      boolean flag = serverlevel.getForcedChunks().contains(chunkpos.toLong());
      if (flag) {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload.query.success", chunkpos, resourcekey.location()), false);
         return 1;
      } else {
         throw ERROR_NOT_TICKING.create(chunkpos, resourcekey.location());
      }
   }

   private static int listForceLoad(CommandSourceStack commandsourcestack) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      LongSet longset = serverlevel.getForcedChunks();
      int i = longset.size();
      if (i > 0) {
         String s = Joiner.on(", ").join(longset.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
         if (i == 1) {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload.list.single", resourcekey.location(), s), false);
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload.list.multiple", i, resourcekey.location(), s), false);
         }
      } else {
         commandsourcestack.sendFailure(Component.translatable("commands.forceload.added.none", resourcekey.location()));
      }

      return i;
   }

   private static int removeAll(CommandSourceStack commandsourcestack) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      LongSet longset = serverlevel.getForcedChunks();
      longset.forEach((i) -> serverlevel.setChunkForced(ChunkPos.getX(i), ChunkPos.getZ(i), false));
      commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload.removed.all", resourcekey.location()), true);
      return 0;
   }

   private static int changeForceLoad(CommandSourceStack commandsourcestack, ColumnPos columnpos, ColumnPos columnpos1, boolean flag) throws CommandSyntaxException {
      int i = Math.min(columnpos.x(), columnpos1.x());
      int j = Math.min(columnpos.z(), columnpos1.z());
      int k = Math.max(columnpos.x(), columnpos1.x());
      int l = Math.max(columnpos.z(), columnpos1.z());
      if (i >= -30000000 && j >= -30000000 && k < 30000000 && l < 30000000) {
         int i1 = SectionPos.blockToSectionCoord(i);
         int j1 = SectionPos.blockToSectionCoord(j);
         int k1 = SectionPos.blockToSectionCoord(k);
         int l1 = SectionPos.blockToSectionCoord(l);
         long i2 = ((long)(k1 - i1) + 1L) * ((long)(l1 - j1) + 1L);
         if (i2 > 256L) {
            throw ERROR_TOO_MANY_CHUNKS.create(256, i2);
         } else {
            ServerLevel serverlevel = commandsourcestack.getLevel();
            ResourceKey<Level> resourcekey = serverlevel.dimension();
            ChunkPos chunkpos = null;
            int j2 = 0;

            for(int k2 = i1; k2 <= k1; ++k2) {
               for(int l2 = j1; l2 <= l1; ++l2) {
                  boolean flag1 = serverlevel.setChunkForced(k2, l2, flag);
                  if (flag1) {
                     ++j2;
                     if (chunkpos == null) {
                        chunkpos = new ChunkPos(k2, l2);
                     }
                  }
               }
            }

            ChunkPos chunkpos1 = chunkpos;
            if (j2 == 0) {
               throw (flag ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
            } else {
               if (j2 == 1) {
                  commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload." + (flag ? "added" : "removed") + ".single", chunkpos1, resourcekey.location()), true);
               } else {
                  ChunkPos chunkpos2 = new ChunkPos(i1, j1);
                  ChunkPos chunkpos3 = new ChunkPos(k1, l1);
                  commandsourcestack.sendSuccess(() -> Component.translatable("commands.forceload." + (flag ? "added" : "removed") + ".multiple", chunkpos1, resourcekey.location(), chunkpos2, chunkpos3), true);
               }

               return j2;
            }
         }
      } else {
         throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
      }
   }
}
