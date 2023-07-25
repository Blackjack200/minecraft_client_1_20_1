package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
   public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
   private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.fillbiome.toobig", object, object1));

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("fillbiome").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(Commands.argument("biome", ResourceArgument.resource(commandbuildcontext, Registries.BIOME)).executes((commandcontext1) -> fill(commandcontext1.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext1, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext1, "to"), ResourceArgument.getResource(commandcontext1, "biome", Registries.BIOME), (holder) -> true)).then(Commands.literal("replace").then(Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(commandbuildcontext, Registries.BIOME)).executes((commandcontext) -> fill(commandcontext.getSource(), BlockPosArgument.getLoadedBlockPos(commandcontext, "from"), BlockPosArgument.getLoadedBlockPos(commandcontext, "to"), ResourceArgument.getResource(commandcontext, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag(commandcontext, "filter", Registries.BIOME)::test))))))));
   }

   private static int quantize(int i) {
      return QuartPos.toBlock(QuartPos.fromBlock(i));
   }

   private static BlockPos quantize(BlockPos blockpos) {
      return new BlockPos(quantize(blockpos.getX()), quantize(blockpos.getY()), quantize(blockpos.getZ()));
   }

   private static BiomeResolver makeResolver(MutableInt mutableint, ChunkAccess chunkaccess, BoundingBox boundingbox, Holder<Biome> holder, Predicate<Holder<Biome>> predicate) {
      return (i, j, k, climate_sampler) -> {
         int l = QuartPos.toBlock(i);
         int i1 = QuartPos.toBlock(j);
         int j1 = QuartPos.toBlock(k);
         Holder<Biome> holder2 = chunkaccess.getNoiseBiome(i, j, k);
         if (boundingbox.isInside(l, i1, j1) && predicate.test(holder2)) {
            mutableint.increment();
            return holder;
         } else {
            return holder2;
         }
      };
   }

   private static int fill(CommandSourceStack commandsourcestack, BlockPos blockpos, BlockPos blockpos1, Holder.Reference<Biome> holder_reference, Predicate<Holder<Biome>> predicate) throws CommandSyntaxException {
      BlockPos blockpos2 = quantize(blockpos);
      BlockPos blockpos3 = quantize(blockpos1);
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos2, blockpos3);
      int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
      int j = commandsourcestack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
      if (i > j) {
         throw ERROR_VOLUME_TOO_LARGE.create(j, i);
      } else {
         ServerLevel serverlevel = commandsourcestack.getLevel();
         List<ChunkAccess> list = new ArrayList<>();

         for(int k = SectionPos.blockToSectionCoord(boundingbox.minZ()); k <= SectionPos.blockToSectionCoord(boundingbox.maxZ()); ++k) {
            for(int l = SectionPos.blockToSectionCoord(boundingbox.minX()); l <= SectionPos.blockToSectionCoord(boundingbox.maxX()); ++l) {
               ChunkAccess chunkaccess = serverlevel.getChunk(l, k, ChunkStatus.FULL, false);
               if (chunkaccess == null) {
                  throw ERROR_NOT_LOADED.create();
               }

               list.add(chunkaccess);
            }
         }

         MutableInt mutableint = new MutableInt(0);

         for(ChunkAccess chunkaccess1 : list) {
            chunkaccess1.fillBiomesFromNoise(makeResolver(mutableint, chunkaccess1, boundingbox, holder_reference, predicate), serverlevel.getChunkSource().randomState().sampler());
            chunkaccess1.setUnsaved(true);
         }

         serverlevel.getChunkSource().chunkMap.resendBiomesForChunks(list);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.fillbiome.success.count", mutableint.getValue(), boundingbox.minX(), boundingbox.minY(), boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ()), true);
         return mutableint.getValue();
      }
   }
}
