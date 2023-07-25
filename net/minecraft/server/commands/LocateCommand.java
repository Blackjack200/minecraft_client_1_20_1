package net.minecraft.server.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocateCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.structure.not_found", object));
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.structure.invalid", object));
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.biome.not_found", object));
   private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType((object) -> Component.translatable("commands.locate.poi.not_found", object));
   private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
   private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
   private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
   private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
   private static final int POI_SEARCH_RADIUS = 256;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      commanddispatcher.register(Commands.literal("locate").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("structure").then(Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE)).executes((commandcontext2) -> locateStructure(commandcontext2.getSource(), ResourceOrTagKeyArgument.getResourceOrTagKey(commandcontext2, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID))))).then(Commands.literal("biome").then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandbuildcontext, Registries.BIOME)).executes((commandcontext1) -> locateBiome(commandcontext1.getSource(), ResourceOrTagArgument.getResourceOrTag(commandcontext1, "biome", Registries.BIOME))))).then(Commands.literal("poi").then(Commands.argument("poi", ResourceOrTagArgument.resourceOrTag(commandbuildcontext, Registries.POINT_OF_INTEREST_TYPE)).executes((commandcontext) -> locatePoi(commandcontext.getSource(), ResourceOrTagArgument.getResourceOrTag(commandcontext, "poi", Registries.POINT_OF_INTEREST_TYPE))))));
   }

   private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> resourceortagkeyargument_result, Registry<Structure> registry) {
      return resourceortagkeyargument_result.unwrap().map((resourcekey) -> registry.getHolder(resourcekey).map((holder) -> HolderSet.direct(holder)), registry::getTag);
   }

   private static int locateStructure(CommandSourceStack commandsourcestack, ResourceOrTagKeyArgument.Result<Structure> resourceortagkeyargument_result) throws CommandSyntaxException {
      Registry<Structure> registry = commandsourcestack.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
      HolderSet<Structure> holderset = getHolders(resourceortagkeyargument_result, registry).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(resourceortagkeyargument_result.asPrintable()));
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holderset, blockpos, 100, false);
      stopwatch.stop();
      if (pair == null) {
         throw ERROR_STRUCTURE_NOT_FOUND.create(resourceortagkeyargument_result.asPrintable());
      } else {
         return showLocateResult(commandsourcestack, resourceortagkeyargument_result, blockpos, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
      }
   }

   private static int locateBiome(CommandSourceStack commandsourcestack, ResourceOrTagArgument.Result<Biome> resourceortagargument_result) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Biome>> pair = commandsourcestack.getLevel().findClosestBiome3d(resourceortagargument_result, blockpos, 6400, 32, 64);
      stopwatch.stop();
      if (pair == null) {
         throw ERROR_BIOME_NOT_FOUND.create(resourceortagargument_result.asPrintable());
      } else {
         return showLocateResult(commandsourcestack, resourceortagargument_result, blockpos, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
      }
   }

   private static int locatePoi(CommandSourceStack commandsourcestack, ResourceOrTagArgument.Result<PoiType> resourceortagargument_result) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Optional<Pair<Holder<PoiType>, BlockPos>> optional = serverlevel.getPoiManager().findClosestWithType(resourceortagargument_result, blockpos, 256, PoiManager.Occupancy.ANY);
      stopwatch.stop();
      if (optional.isEmpty()) {
         throw ERROR_POI_NOT_FOUND.create(resourceortagargument_result.asPrintable());
      } else {
         return showLocateResult(commandsourcestack, resourceortagargument_result, blockpos, optional.get().swap(), "commands.locate.poi.success", false, stopwatch.elapsed());
      }
   }

   private static String getElementName(Pair<BlockPos, ? extends Holder<?>> pair) {
      return pair.getSecond().unwrapKey().map((resourcekey) -> resourcekey.location().toString()).orElse("[unregistered]");
   }

   public static int showLocateResult(CommandSourceStack commandsourcestack, ResourceOrTagArgument.Result<?> resourceortagargument_result, BlockPos blockpos, Pair<BlockPos, ? extends Holder<?>> pair, String s, boolean flag, Duration duration) {
      String s1 = resourceortagargument_result.unwrap().map((holder_reference) -> resourceortagargument_result.asPrintable(), (holderset_named) -> resourceortagargument_result.asPrintable() + " (" + getElementName(pair) + ")");
      return showLocateResult(commandsourcestack, blockpos, pair, s, flag, s1, duration);
   }

   public static int showLocateResult(CommandSourceStack commandsourcestack, ResourceOrTagKeyArgument.Result<?> resourceortagkeyargument_result, BlockPos blockpos, Pair<BlockPos, ? extends Holder<?>> pair, String s, boolean flag, Duration duration) {
      String s1 = resourceortagkeyargument_result.unwrap().map((resourcekey) -> resourcekey.location().toString(), (tagkey) -> "#" + tagkey.location() + " (" + getElementName(pair) + ")");
      return showLocateResult(commandsourcestack, blockpos, pair, s, flag, s1, duration);
   }

   private static int showLocateResult(CommandSourceStack commandsourcestack, BlockPos blockpos, Pair<BlockPos, ? extends Holder<?>> pair, String s, boolean flag, String s1, Duration duration) {
      BlockPos blockpos1 = pair.getFirst();
      int i = flag ? Mth.floor(Mth.sqrt((float)blockpos.distSqr(blockpos1))) : Mth.floor(dist(blockpos.getX(), blockpos.getZ(), blockpos1.getX(), blockpos1.getZ()));
      String s2 = flag ? String.valueOf(blockpos1.getY()) : "~";
      Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockpos1.getX(), s2, blockpos1.getZ())).withStyle((style) -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockpos1.getX() + " " + s2 + " " + blockpos1.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
      commandsourcestack.sendSuccess(() -> Component.translatable(s, s1, component, i), false);
      LOGGER.info("Locating element " + s1 + " took " + duration.toMillis() + " ms");
      return i;
   }

   private static float dist(int i, int j, int k, int l) {
      int i1 = k - i;
      int j1 = l - j;
      return Mth.sqrt((float)(i1 * i1 + j1 * j1));
   }
}
