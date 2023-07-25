package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ExecuteCommand {
   private static final int MAX_TEST_AREA = 32768;
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("commands.execute.blocks.toobig", object, object1));
   private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));
   private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType((object) -> Component.translatable("commands.execute.conditional.fail_count", object));
   private static final BinaryOperator<ResultConsumer<CommandSourceStack>> CALLBACK_CHAINER = (resultconsumer, resultconsumer1) -> (commandcontext, flag, i) -> {
         resultconsumer.onCommandComplete(commandcontext, flag, i);
         resultconsumer1.onCommandComplete(commandcontext, flag, i);
      };
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_PREDICATE = (commandcontext, suggestionsbuilder) -> {
      LootDataManager lootdatamanager = commandcontext.getSource().getServer().getLootData();
      return SharedSuggestionProvider.suggestResource(lootdatamanager.getKeys(LootDataType.PREDICATE), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher, CommandBuildContext commandbuildcontext) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = commanddispatcher.register(Commands.literal("execute").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(2)));
      commanddispatcher.register(Commands.literal("execute").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("run").redirect(commanddispatcher.getRoot())).then(addConditionals(literalcommandnode, Commands.literal("if"), true, commandbuildcontext)).then(addConditionals(literalcommandnode, Commands.literal("unless"), false, commandbuildcontext)).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (commandcontext12) -> {
         List<CommandSourceStack> list4 = Lists.newArrayList();

         for(Entity entity4 : EntityArgument.getOptionalEntities(commandcontext12, "targets")) {
            list4.add(commandcontext12.getSource().withEntity(entity4));
         }

         return list4;
      }))).then(Commands.literal("at").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (commandcontext11) -> {
         List<CommandSourceStack> list3 = Lists.newArrayList();

         for(Entity entity3 : EntityArgument.getOptionalEntities(commandcontext11, "targets")) {
            list3.add(commandcontext11.getSource().withLevel((ServerLevel)entity3.level()).withPosition(entity3.position()).withRotation(entity3.getRotationVector()));
         }

         return list3;
      }))).then(Commands.literal("store").then(wrapStores(literalcommandnode, Commands.literal("result"), true)).then(wrapStores(literalcommandnode, Commands.literal("success"), false))).then(Commands.literal("positioned").then(Commands.argument("pos", Vec3Argument.vec3()).redirect(literalcommandnode, (commandcontext10) -> commandcontext10.getSource().withPosition(Vec3Argument.getVec3(commandcontext10, "pos")).withAnchor(EntityAnchorArgument.Anchor.FEET))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (commandcontext9) -> {
         List<CommandSourceStack> list2 = Lists.newArrayList();

         for(Entity entity2 : EntityArgument.getOptionalEntities(commandcontext9, "targets")) {
            list2.add(commandcontext9.getSource().withPosition(entity2.position()));
         }

         return list2;
      }))).then(Commands.literal("over").then(Commands.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect(literalcommandnode, (commandcontext8) -> {
         Vec3 vec3 = commandcontext8.getSource().getPosition();
         ServerLevel serverlevel = commandcontext8.getSource().getLevel();
         double d0 = vec3.x();
         double d1 = vec3.z();
         if (!serverlevel.hasChunk(SectionPos.blockToSectionCoord(d0), SectionPos.blockToSectionCoord(d1))) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
         } else {
            int i = serverlevel.getHeight(HeightmapTypeArgument.getHeightmap(commandcontext8, "heightmap"), Mth.floor(d0), Mth.floor(d1));
            return commandcontext8.getSource().withPosition(new Vec3(d0, (double)i, d1));
         }
      })))).then(Commands.literal("rotated").then(Commands.argument("rot", RotationArgument.rotation()).redirect(literalcommandnode, (commandcontext7) -> commandcontext7.getSource().withRotation(RotationArgument.getRotation(commandcontext7, "rot").getRotation(commandcontext7.getSource())))).then(Commands.literal("as").then(Commands.argument("targets", EntityArgument.entities()).fork(literalcommandnode, (commandcontext6) -> {
         List<CommandSourceStack> list1 = Lists.newArrayList();

         for(Entity entity1 : EntityArgument.getOptionalEntities(commandcontext6, "targets")) {
            list1.add(commandcontext6.getSource().withRotation(entity1.getRotationVector()));
         }

         return list1;
      })))).then(Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("anchor", EntityAnchorArgument.anchor()).fork(literalcommandnode, (commandcontext5) -> {
         List<CommandSourceStack> list = Lists.newArrayList();
         EntityAnchorArgument.Anchor entityanchorargument_anchor = EntityAnchorArgument.getAnchor(commandcontext5, "anchor");

         for(Entity entity : EntityArgument.getOptionalEntities(commandcontext5, "targets")) {
            list.add(commandcontext5.getSource().facing(entity, entityanchorargument_anchor));
         }

         return list;
      })))).then(Commands.argument("pos", Vec3Argument.vec3()).redirect(literalcommandnode, (commandcontext4) -> commandcontext4.getSource().facing(Vec3Argument.getVec3(commandcontext4, "pos"))))).then(Commands.literal("align").then(Commands.argument("axes", SwizzleArgument.swizzle()).redirect(literalcommandnode, (commandcontext3) -> commandcontext3.getSource().withPosition(commandcontext3.getSource().getPosition().align(SwizzleArgument.getSwizzle(commandcontext3, "axes")))))).then(Commands.literal("anchored").then(Commands.argument("anchor", EntityAnchorArgument.anchor()).redirect(literalcommandnode, (commandcontext2) -> commandcontext2.getSource().withAnchor(EntityAnchorArgument.getAnchor(commandcontext2, "anchor"))))).then(Commands.literal("in").then(Commands.argument("dimension", DimensionArgument.dimension()).redirect(literalcommandnode, (commandcontext1) -> commandcontext1.getSource().withLevel(DimensionArgument.getDimension(commandcontext1, "dimension"))))).then(Commands.literal("summon").then(Commands.argument("entity", ResourceArgument.resource(commandbuildcontext, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).redirect(literalcommandnode, (commandcontext) -> spawnEntityAndRedirect(commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"))))).then(createRelationOperations(literalcommandnode, Commands.literal("on"))));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapStores(LiteralCommandNode<CommandSourceStack> literalcommandnode, LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder, boolean flag) {
      literalargumentbuilder.then(Commands.literal("score").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).redirect(literalcommandnode, (commandcontext14) -> storeValue(commandcontext14.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandcontext14, "targets"), ObjectiveArgument.getObjective(commandcontext14, "objective"), flag)))));
      literalargumentbuilder.then(Commands.literal("bossbar").then(Commands.argument("id", ResourceLocationArgument.id()).suggests(BossBarCommands.SUGGEST_BOSS_BAR).then(Commands.literal("value").redirect(literalcommandnode, (commandcontext13) -> storeValue(commandcontext13.getSource(), BossBarCommands.getBossBar(commandcontext13), true, flag))).then(Commands.literal("max").redirect(literalcommandnode, (commandcontext12) -> storeValue(commandcontext12.getSource(), BossBarCommands.getBossBar(commandcontext12), false, flag)))));

      for(DataCommands.DataProvider datacommands_dataprovider : DataCommands.TARGET_PROVIDERS) {
         datacommands_dataprovider.wrap(literalargumentbuilder, (argumentbuilder) -> argumentbuilder.then(Commands.argument("path", NbtPathArgument.nbtPath()).then(Commands.literal("int").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext10) -> storeData(commandcontext10.getSource(), datacommands_dataprovider.access(commandcontext10), NbtPathArgument.getPath(commandcontext10, "path"), (j1) -> IntTag.valueOf((int)((double)j1 * DoubleArgumentType.getDouble(commandcontext10, "scale"))), flag)))).then(Commands.literal("float").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext8) -> storeData(commandcontext8.getSource(), datacommands_dataprovider.access(commandcontext8), NbtPathArgument.getPath(commandcontext8, "path"), (i1) -> FloatTag.valueOf((float)((double)i1 * DoubleArgumentType.getDouble(commandcontext8, "scale"))), flag)))).then(Commands.literal("short").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext6) -> storeData(commandcontext6.getSource(), datacommands_dataprovider.access(commandcontext6), NbtPathArgument.getPath(commandcontext6, "path"), (l) -> ShortTag.valueOf((short)((int)((double)l * DoubleArgumentType.getDouble(commandcontext6, "scale")))), flag)))).then(Commands.literal("long").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext4) -> storeData(commandcontext4.getSource(), datacommands_dataprovider.access(commandcontext4), NbtPathArgument.getPath(commandcontext4, "path"), (k) -> LongTag.valueOf((long)((double)k * DoubleArgumentType.getDouble(commandcontext4, "scale"))), flag)))).then(Commands.literal("double").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext2) -> storeData(commandcontext2.getSource(), datacommands_dataprovider.access(commandcontext2), NbtPathArgument.getPath(commandcontext2, "path"), (j) -> DoubleTag.valueOf((double)j * DoubleArgumentType.getDouble(commandcontext2, "scale")), flag)))).then(Commands.literal("byte").then(Commands.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> storeData(commandcontext.getSource(), datacommands_dataprovider.access(commandcontext), NbtPathArgument.getPath(commandcontext, "path"), (i) -> ByteTag.valueOf((byte)((int)((double)i * DoubleArgumentType.getDouble(commandcontext, "scale")))), flag))))));
      }

      return literalargumentbuilder;
   }

   private static CommandSourceStack storeValue(CommandSourceStack commandsourcestack, Collection<String> collection, Objective objective, boolean flag) {
      Scoreboard scoreboard = commandsourcestack.getServer().getScoreboard();
      return commandsourcestack.withCallback((commandcontext, flag2, i) -> {
         for(String s : collection) {
            Score score = scoreboard.getOrCreatePlayerScore(s, objective);
            int j = flag ? i : (flag2 ? 1 : 0);
            score.setScore(j);
         }

      }, CALLBACK_CHAINER);
   }

   private static CommandSourceStack storeValue(CommandSourceStack commandsourcestack, CustomBossEvent custombossevent, boolean flag, boolean flag1) {
      return commandsourcestack.withCallback((commandcontext, flag4, i) -> {
         int j = flag1 ? i : (flag4 ? 1 : 0);
         if (flag) {
            custombossevent.setValue(j);
         } else {
            custombossevent.setMax(j);
         }

      }, CALLBACK_CHAINER);
   }

   private static CommandSourceStack storeData(CommandSourceStack commandsourcestack, DataAccessor dataaccessor, NbtPathArgument.NbtPath nbtpathargument_nbtpath, IntFunction<Tag> intfunction, boolean flag) {
      return commandsourcestack.withCallback((commandcontext, flag2, i) -> {
         try {
            CompoundTag compoundtag = dataaccessor.getData();
            int j = flag ? i : (flag2 ? 1 : 0);
            nbtpathargument_nbtpath.set(compoundtag, intfunction.apply(j));
            dataaccessor.setData(compoundtag);
         } catch (CommandSyntaxException var9) {
         }

      }, CALLBACK_CHAINER);
   }

   private static boolean isChunkLoaded(ServerLevel serverlevel, BlockPos blockpos) {
      ChunkPos chunkpos = new ChunkPos(blockpos);
      LevelChunk levelchunk = serverlevel.getChunkSource().getChunkNow(chunkpos.x, chunkpos.z);
      if (levelchunk == null) {
         return false;
      } else {
         return levelchunk.getFullStatus() == FullChunkStatus.ENTITY_TICKING && serverlevel.areEntitiesLoaded(chunkpos.toLong());
      }
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addConditionals(CommandNode<CommandSourceStack> commandnode, LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder, boolean flag, CommandBuildContext commandbuildcontext) {
      literalargumentbuilder.then(Commands.literal("block").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(addConditional(commandnode, Commands.argument("block", BlockPredicateArgument.blockPredicate(commandbuildcontext)), flag, (commandcontext14) -> BlockPredicateArgument.getBlockPredicate(commandcontext14, "block").test(new BlockInWorld(commandcontext14.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(commandcontext14, "pos"), true)))))).then(Commands.literal("biome").then(Commands.argument("pos", BlockPosArgument.blockPos()).then(addConditional(commandnode, Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(commandbuildcontext, Registries.BIOME)), flag, (commandcontext13) -> ResourceOrTagArgument.getResourceOrTag(commandcontext13, "biome", Registries.BIOME).test(commandcontext13.getSource().getLevel().getBiome(BlockPosArgument.getLoadedBlockPos(commandcontext13, "pos"))))))).then(Commands.literal("loaded").then(addConditional(commandnode, Commands.argument("pos", BlockPosArgument.blockPos()), flag, (commandcontext12) -> isChunkLoaded(commandcontext12.getSource().getLevel(), BlockPosArgument.getBlockPos(commandcontext12, "pos"))))).then(Commands.literal("dimension").then(addConditional(commandnode, Commands.argument("dimension", DimensionArgument.dimension()), flag, (commandcontext11) -> DimensionArgument.getDimension(commandcontext11, "dimension") == commandcontext11.getSource().getLevel()))).then(Commands.literal("score").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.literal("=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), flag, (commandcontext10) -> checkScore(commandcontext10, Integer::equals))))).then(Commands.literal("<").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), flag, (commandcontext9) -> checkScore(commandcontext9, (integer6, integer7) -> integer6 < integer7))))).then(Commands.literal("<=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), flag, (commandcontext8) -> checkScore(commandcontext8, (integer4, integer5) -> integer4 <= integer5))))).then(Commands.literal(">").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), flag, (commandcontext7) -> checkScore(commandcontext7, (integer2, integer3) -> integer2 > integer3))))).then(Commands.literal(">=").then(Commands.argument("source", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, Commands.argument("sourceObjective", ObjectiveArgument.objective()), flag, (commandcontext6) -> checkScore(commandcontext6, (integer, integer1) -> integer >= integer1))))).then(Commands.literal("matches").then(addConditional(commandnode, Commands.argument("range", RangeArgument.intRange()), flag, (commandcontext5) -> checkScore(commandcontext5, RangeArgument.Ints.getRange(commandcontext5, "range")))))))).then(Commands.literal("blocks").then(Commands.argument("start", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(Commands.argument("destination", BlockPosArgument.blockPos()).then(addIfBlocksConditional(commandnode, Commands.literal("all"), flag, false)).then(addIfBlocksConditional(commandnode, Commands.literal("masked"), flag, true)))))).then(Commands.literal("entity").then(Commands.argument("entities", EntityArgument.entities()).fork(commandnode, (commandcontext4) -> expect(commandcontext4, flag, !EntityArgument.getOptionalEntities(commandcontext4, "entities").isEmpty())).executes(createNumericConditionalHandler(flag, (commandcontext3) -> EntityArgument.getOptionalEntities(commandcontext3, "entities").size())))).then(Commands.literal("predicate").then(addConditional(commandnode, Commands.argument("predicate", ResourceLocationArgument.id()).suggests(SUGGEST_PREDICATE), flag, (commandcontext2) -> checkCustomPredicate(commandcontext2.getSource(), ResourceLocationArgument.getPredicate(commandcontext2, "predicate")))));

      for(DataCommands.DataProvider datacommands_dataprovider : DataCommands.SOURCE_PROVIDERS) {
         literalargumentbuilder.then(datacommands_dataprovider.wrap(Commands.literal("data"), (argumentbuilder) -> argumentbuilder.then(Commands.argument("path", NbtPathArgument.nbtPath()).fork(commandnode, (commandcontext1) -> expect(commandcontext1, flag, checkMatchingData(datacommands_dataprovider.access(commandcontext1), NbtPathArgument.getPath(commandcontext1, "path")) > 0)).executes(createNumericConditionalHandler(flag, (commandcontext) -> checkMatchingData(datacommands_dataprovider.access(commandcontext), NbtPathArgument.getPath(commandcontext, "path")))))));
      }

      return literalargumentbuilder;
   }

   private static Command<CommandSourceStack> createNumericConditionalHandler(boolean flag, ExecuteCommand.CommandNumericPredicate executecommand_commandnumericpredicate) {
      return flag ? (commandcontext1) -> {
         int j = executecommand_commandnumericpredicate.test(commandcontext1);
         if (j > 0) {
            commandcontext1.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", j), false);
            return j;
         } else {
            throw ERROR_CONDITIONAL_FAILED.create();
         }
      } : (commandcontext) -> {
         int i = executecommand_commandnumericpredicate.test(commandcontext);
         if (i == 0) {
            commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw ERROR_CONDITIONAL_FAILED_COUNT.create(i);
         }
      };
   }

   private static int checkMatchingData(DataAccessor dataaccessor, NbtPathArgument.NbtPath nbtpathargument_nbtpath) throws CommandSyntaxException {
      return nbtpathargument_nbtpath.countMatching(dataaccessor.getData());
   }

   private static boolean checkScore(CommandContext<CommandSourceStack> commandcontext, BiPredicate<Integer, Integer> bipredicate) throws CommandSyntaxException {
      String s = ScoreHolderArgument.getName(commandcontext, "target");
      Objective objective = ObjectiveArgument.getObjective(commandcontext, "targetObjective");
      String s1 = ScoreHolderArgument.getName(commandcontext, "source");
      Objective objective1 = ObjectiveArgument.getObjective(commandcontext, "sourceObjective");
      Scoreboard scoreboard = commandcontext.getSource().getServer().getScoreboard();
      if (scoreboard.hasPlayerScore(s, objective) && scoreboard.hasPlayerScore(s1, objective1)) {
         Score score = scoreboard.getOrCreatePlayerScore(s, objective);
         Score score1 = scoreboard.getOrCreatePlayerScore(s1, objective1);
         return bipredicate.test(score.getScore(), score1.getScore());
      } else {
         return false;
      }
   }

   private static boolean checkScore(CommandContext<CommandSourceStack> commandcontext, MinMaxBounds.Ints minmaxbounds_ints) throws CommandSyntaxException {
      String s = ScoreHolderArgument.getName(commandcontext, "target");
      Objective objective = ObjectiveArgument.getObjective(commandcontext, "targetObjective");
      Scoreboard scoreboard = commandcontext.getSource().getServer().getScoreboard();
      return !scoreboard.hasPlayerScore(s, objective) ? false : minmaxbounds_ints.matches(scoreboard.getOrCreatePlayerScore(s, objective).getScore());
   }

   private static boolean checkCustomPredicate(CommandSourceStack commandsourcestack, LootItemCondition lootitemcondition) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, commandsourcestack.getPosition()).withOptionalParameter(LootContextParams.THIS_ENTITY, commandsourcestack.getEntity()).create(LootContextParamSets.COMMAND);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      lootcontext.pushVisitedElement(LootContext.createVisitedEntry(lootitemcondition));
      return lootitemcondition.test(lootcontext);
   }

   private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> commandcontext, boolean flag, boolean flag1) {
      return (Collection<CommandSourceStack>)(flag1 == flag ? Collections.singleton(commandcontext.getSource()) : Collections.emptyList());
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addConditional(CommandNode<CommandSourceStack> commandnode, ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, boolean flag, ExecuteCommand.CommandPredicate executecommand_commandpredicate) {
      return argumentbuilder.fork(commandnode, (commandcontext1) -> expect(commandcontext1, flag, executecommand_commandpredicate.test(commandcontext1))).executes((commandcontext) -> {
         if (flag == executecommand_commandpredicate.test(commandcontext)) {
            commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
            return 1;
         } else {
            throw ERROR_CONDITIONAL_FAILED.create();
         }
      });
   }

   private static ArgumentBuilder<CommandSourceStack, ?> addIfBlocksConditional(CommandNode<CommandSourceStack> commandnode, ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, boolean flag, boolean flag1) {
      return argumentbuilder.fork(commandnode, (commandcontext2) -> expect(commandcontext2, flag, checkRegions(commandcontext2, flag1).isPresent())).executes(flag ? (commandcontext1) -> checkIfRegions(commandcontext1, flag1) : (commandcontext) -> checkUnlessRegions(commandcontext, flag1));
   }

   private static int checkIfRegions(CommandContext<CommandSourceStack> commandcontext, boolean flag) throws CommandSyntaxException {
      OptionalInt optionalint = checkRegions(commandcontext, flag);
      if (optionalint.isPresent()) {
         commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", optionalint.getAsInt()), false);
         return optionalint.getAsInt();
      } else {
         throw ERROR_CONDITIONAL_FAILED.create();
      }
   }

   private static int checkUnlessRegions(CommandContext<CommandSourceStack> commandcontext, boolean flag) throws CommandSyntaxException {
      OptionalInt optionalint = checkRegions(commandcontext, flag);
      if (optionalint.isPresent()) {
         throw ERROR_CONDITIONAL_FAILED_COUNT.create(optionalint.getAsInt());
      } else {
         commandcontext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
         return 1;
      }
   }

   private static OptionalInt checkRegions(CommandContext<CommandSourceStack> commandcontext, boolean flag) throws CommandSyntaxException {
      return checkRegions(commandcontext.getSource().getLevel(), BlockPosArgument.getLoadedBlockPos(commandcontext, "start"), BlockPosArgument.getLoadedBlockPos(commandcontext, "end"), BlockPosArgument.getLoadedBlockPos(commandcontext, "destination"), flag);
   }

   private static OptionalInt checkRegions(ServerLevel serverlevel, BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, boolean flag) throws CommandSyntaxException {
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
      BoundingBox boundingbox1 = BoundingBox.fromCorners(blockpos2, blockpos2.offset(boundingbox.getLength()));
      BlockPos blockpos3 = new BlockPos(boundingbox1.minX() - boundingbox.minX(), boundingbox1.minY() - boundingbox.minY(), boundingbox1.minZ() - boundingbox.minZ());
      int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
      if (i > 32768) {
         throw ERROR_AREA_TOO_LARGE.create(32768, i);
      } else {
         int j = 0;

         for(int k = boundingbox.minZ(); k <= boundingbox.maxZ(); ++k) {
            for(int l = boundingbox.minY(); l <= boundingbox.maxY(); ++l) {
               for(int i1 = boundingbox.minX(); i1 <= boundingbox.maxX(); ++i1) {
                  BlockPos blockpos4 = new BlockPos(i1, l, k);
                  BlockPos blockpos5 = blockpos4.offset(blockpos3);
                  BlockState blockstate = serverlevel.getBlockState(blockpos4);
                  if (!flag || !blockstate.is(Blocks.AIR)) {
                     if (blockstate != serverlevel.getBlockState(blockpos5)) {
                        return OptionalInt.empty();
                     }

                     BlockEntity blockentity = serverlevel.getBlockEntity(blockpos4);
                     BlockEntity blockentity1 = serverlevel.getBlockEntity(blockpos5);
                     if (blockentity != null) {
                        if (blockentity1 == null) {
                           return OptionalInt.empty();
                        }

                        if (blockentity1.getType() != blockentity.getType()) {
                           return OptionalInt.empty();
                        }

                        CompoundTag compoundtag = blockentity.saveWithoutMetadata();
                        CompoundTag compoundtag1 = blockentity1.saveWithoutMetadata();
                        if (!compoundtag.equals(compoundtag1)) {
                           return OptionalInt.empty();
                        }
                     }

                     ++j;
                  }
               }
            }
         }

         return OptionalInt.of(j);
      }
   }

   private static RedirectModifier<CommandSourceStack> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> function) {
      return (commandcontext) -> {
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         Entity entity = commandsourcestack.getEntity();
         return (Collection<CommandSourceStack>)(entity == null ? List.of() : function.apply(entity).filter((entity2) -> !entity2.isRemoved()).map((entity1) -> List.of(commandsourcestack.withEntity(entity1))).orElse(List.of()));
      };
   }

   private static RedirectModifier<CommandSourceStack> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> function) {
      return (commandcontext) -> {
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         Entity entity = commandsourcestack.getEntity();
         return entity == null ? List.of() : function.apply(entity).filter((entity1) -> !entity1.isRemoved()).map(commandsourcestack::withEntity).toList();
      };
   }

   private static LiteralArgumentBuilder<CommandSourceStack> createRelationOperations(CommandNode<CommandSourceStack> commandnode, LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder) {
      return literalargumentbuilder.then(Commands.literal("owner").fork(commandnode, expandOneToOneEntityRelation((entity7) -> {
         Optional var10000;
         if (entity7 instanceof OwnableEntity ownableentity) {
            var10000 = Optional.ofNullable(ownableentity.getOwner());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      }))).then(Commands.literal("leasher").fork(commandnode, expandOneToOneEntityRelation((entity6) -> {
         Optional var10000;
         if (entity6 instanceof Mob mob) {
            var10000 = Optional.ofNullable(mob.getLeashHolder());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      }))).then(Commands.literal("target").fork(commandnode, expandOneToOneEntityRelation((entity5) -> {
         Optional var10000;
         if (entity5 instanceof Targeting targeting) {
            var10000 = Optional.ofNullable(targeting.getTarget());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      }))).then(Commands.literal("attacker").fork(commandnode, expandOneToOneEntityRelation((entity4) -> {
         Optional var10000;
         if (entity4 instanceof Attackable attackable) {
            var10000 = Optional.ofNullable(attackable.getLastAttacker());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      }))).then(Commands.literal("vehicle").fork(commandnode, expandOneToOneEntityRelation((entity3) -> Optional.ofNullable(entity3.getVehicle())))).then(Commands.literal("controller").fork(commandnode, expandOneToOneEntityRelation((entity2) -> Optional.ofNullable(entity2.getControllingPassenger())))).then(Commands.literal("origin").fork(commandnode, expandOneToOneEntityRelation((entity1) -> {
         Optional var10000;
         if (entity1 instanceof TraceableEntity traceableentity) {
            var10000 = Optional.ofNullable(traceableentity.getOwner());
         } else {
            var10000 = Optional.empty();
         }

         return var10000;
      }))).then(Commands.literal("passengers").fork(commandnode, expandOneToManyEntityRelation((entity) -> entity.getPassengers().stream())));
   }

   private static CommandSourceStack spawnEntityAndRedirect(CommandSourceStack commandsourcestack, Holder.Reference<EntityType<?>> holder_reference) throws CommandSyntaxException {
      Entity entity = SummonCommand.createEntity(commandsourcestack, holder_reference, commandsourcestack.getPosition(), new CompoundTag(), true);
      return commandsourcestack.withEntity(entity);
   }

   @FunctionalInterface
   interface CommandNumericPredicate {
      int test(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException;
   }

   @FunctionalInterface
   interface CommandPredicate {
      boolean test(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException;
   }
}
