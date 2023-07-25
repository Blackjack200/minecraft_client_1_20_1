package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ResetChunksCommand {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("resetchunks").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).executes((commandcontext2) -> resetChunks(commandcontext2.getSource(), 0, true)).then(Commands.argument("range", IntegerArgumentType.integer(0, 5)).executes((commandcontext1) -> resetChunks(commandcontext1.getSource(), IntegerArgumentType.getInteger(commandcontext1, "range"), true)).then(Commands.argument("skipOldChunks", BoolArgumentType.bool()).executes((commandcontext) -> resetChunks(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "range"), BoolArgumentType.getBool(commandcontext, "skipOldChunks"))))));
   }

   private static int resetChunks(CommandSourceStack commandsourcestack, int i, boolean flag) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      ServerChunkCache serverchunkcache = serverlevel.getChunkSource();
      serverchunkcache.chunkMap.debugReloadGenerator();
      Vec3 vec3 = commandsourcestack.getPosition();
      ChunkPos chunkpos = new ChunkPos(BlockPos.containing(vec3));
      int j = chunkpos.z - i;
      int k = chunkpos.z + i;
      int l = chunkpos.x - i;
      int i1 = chunkpos.x + i;

      for(int j1 = j; j1 <= k; ++j1) {
         for(int k1 = l; k1 <= i1; ++k1) {
            ChunkPos chunkpos1 = new ChunkPos(k1, j1);
            LevelChunk levelchunk = serverchunkcache.getChunk(k1, j1, false);
            if (levelchunk != null && (!flag || !levelchunk.isOldNoiseGeneration())) {
               for(BlockPos blockpos : BlockPos.betweenClosed(chunkpos1.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos1.getMinBlockZ(), chunkpos1.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, chunkpos1.getMaxBlockZ())) {
                  serverlevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 16);
               }
            }
         }
      }

      ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");
      long l1 = System.currentTimeMillis();
      int i2 = (i * 2 + 1) * (i * 2 + 1);

      for(ChunkStatus chunkstatus : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT)) {
         long j2 = System.currentTimeMillis();
         CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, processormailbox::tell);

         for(int k2 = chunkpos.z - i; k2 <= chunkpos.z + i; ++k2) {
            for(int l2 = chunkpos.x - i; l2 <= chunkpos.x + i; ++l2) {
               ChunkPos chunkpos2 = new ChunkPos(l2, k2);
               LevelChunk levelchunk1 = serverchunkcache.getChunk(l2, k2, false);
               if (levelchunk1 != null && (!flag || !levelchunk1.isOldNoiseGeneration())) {
                  List<ChunkAccess> list = Lists.newArrayList();
                  int i3 = Math.max(1, chunkstatus.getRange());

                  for(int j3 = chunkpos2.z - i3; j3 <= chunkpos2.z + i3; ++j3) {
                     for(int k3 = chunkpos2.x - i3; k3 <= chunkpos2.x + i3; ++k3) {
                        ChunkAccess chunkaccess = serverchunkcache.getChunk(k3, j3, chunkstatus.getParent(), true);
                        ChunkAccess chunkaccess1;
                        if (chunkaccess instanceof ImposterProtoChunk) {
                           chunkaccess1 = new ImposterProtoChunk(((ImposterProtoChunk)chunkaccess).getWrapped(), true);
                        } else if (chunkaccess instanceof LevelChunk) {
                           chunkaccess1 = new ImposterProtoChunk((LevelChunk)chunkaccess, true);
                        } else {
                           chunkaccess1 = chunkaccess;
                        }

                        list.add(chunkaccess1);
                     }
                  }

                  completablefuture = completablefuture.thenComposeAsync((unit) -> chunkstatus.generate(processormailbox::tell, serverlevel, serverchunkcache.getGenerator(), serverlevel.getStructureManager(), serverchunkcache.getLightEngine(), (chunkaccess5) -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                     }, list).thenApply((either) -> {
                        if (chunkstatus == ChunkStatus.NOISE) {
                           either.left().ifPresent((chunkaccess4) -> Heightmap.primeHeightmaps(chunkaccess4, ChunkStatus.POST_FEATURES));
                        }

                        return Unit.INSTANCE;
                     }), processormailbox::tell);
               }
            }
         }

         commandsourcestack.getServer().managedBlock(completablefuture::isDone);
         LOGGER.debug(chunkstatus + " took " + (System.currentTimeMillis() - j2) + " ms");
      }

      long l3 = System.currentTimeMillis();

      for(int i4 = chunkpos.z - i; i4 <= chunkpos.z + i; ++i4) {
         for(int j4 = chunkpos.x - i; j4 <= chunkpos.x + i; ++j4) {
            ChunkPos chunkpos3 = new ChunkPos(j4, i4);
            LevelChunk levelchunk2 = serverchunkcache.getChunk(j4, i4, false);
            if (levelchunk2 != null && (!flag || !levelchunk2.isOldNoiseGeneration())) {
               for(BlockPos blockpos1 : BlockPos.betweenClosed(chunkpos3.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos3.getMinBlockZ(), chunkpos3.getMaxBlockX(), serverlevel.getMaxBuildHeight() - 1, chunkpos3.getMaxBlockZ())) {
                  serverchunkcache.blockChanged(blockpos1);
               }
            }
         }
      }

      LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - l3) + " ms");
      long k4 = System.currentTimeMillis() - l1;
      commandsourcestack.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", i2, k4, i2, (float)k4 / (float)i2)), true);
      return 1;
   }
}
