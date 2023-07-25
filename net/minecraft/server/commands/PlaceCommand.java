package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PlaceCommand {
   private static final SimpleCommandExceptionType ERROR_FEATURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.feature.failed"));
   private static final SimpleCommandExceptionType ERROR_JIGSAW_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.jigsaw.failed"));
   private static final SimpleCommandExceptionType ERROR_STRUCTURE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.structure.failed"));
   private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("commands.place.template.invalid", object));
   private static final SimpleCommandExceptionType ERROR_TEMPLATE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.place.template.failed"));
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_TEMPLATES = (commandcontext, suggestionsbuilder) -> {
      StructureTemplateManager structuretemplatemanager = commandcontext.getSource().getLevel().getStructureManager();
      return SharedSuggestionProvider.suggestResource(structuretemplatemanager.listTemplates(), suggestionsbuilder);
   };

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("place").requires((commandsourcestack) -> commandsourcestack.hasPermission(2)).then(Commands.literal("feature").then(Commands.argument("feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE)).executes((commandcontext11) -> placeFeature(commandcontext11.getSource(), ResourceKeyArgument.getConfiguredFeature(commandcontext11, "feature"), BlockPos.containing(commandcontext11.getSource().getPosition()))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext10) -> placeFeature(commandcontext10.getSource(), ResourceKeyArgument.getConfiguredFeature(commandcontext10, "feature"), BlockPosArgument.getLoadedBlockPos(commandcontext10, "pos")))))).then(Commands.literal("jigsaw").then(Commands.argument("pool", ResourceKeyArgument.key(Registries.TEMPLATE_POOL)).then(Commands.argument("target", ResourceLocationArgument.id()).then(Commands.argument("max_depth", IntegerArgumentType.integer(1, 7)).executes((commandcontext9) -> placeJigsaw(commandcontext9.getSource(), ResourceKeyArgument.getStructureTemplatePool(commandcontext9, "pool"), ResourceLocationArgument.getId(commandcontext9, "target"), IntegerArgumentType.getInteger(commandcontext9, "max_depth"), BlockPos.containing(commandcontext9.getSource().getPosition()))).then(Commands.argument("position", BlockPosArgument.blockPos()).executes((commandcontext8) -> placeJigsaw(commandcontext8.getSource(), ResourceKeyArgument.getStructureTemplatePool(commandcontext8, "pool"), ResourceLocationArgument.getId(commandcontext8, "target"), IntegerArgumentType.getInteger(commandcontext8, "max_depth"), BlockPosArgument.getLoadedBlockPos(commandcontext8, "position")))))))).then(Commands.literal("structure").then(Commands.argument("structure", ResourceKeyArgument.key(Registries.STRUCTURE)).executes((commandcontext7) -> placeStructure(commandcontext7.getSource(), ResourceKeyArgument.getStructure(commandcontext7, "structure"), BlockPos.containing(commandcontext7.getSource().getPosition()))).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext6) -> placeStructure(commandcontext6.getSource(), ResourceKeyArgument.getStructure(commandcontext6, "structure"), BlockPosArgument.getLoadedBlockPos(commandcontext6, "pos")))))).then(Commands.literal("template").then(Commands.argument("template", ResourceLocationArgument.id()).suggests(SUGGEST_TEMPLATES).executes((commandcontext5) -> placeTemplate(commandcontext5.getSource(), ResourceLocationArgument.getId(commandcontext5, "template"), BlockPos.containing(commandcontext5.getSource().getPosition()), Rotation.NONE, Mirror.NONE, 1.0F, 0)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((commandcontext4) -> placeTemplate(commandcontext4.getSource(), ResourceLocationArgument.getId(commandcontext4, "template"), BlockPosArgument.getLoadedBlockPos(commandcontext4, "pos"), Rotation.NONE, Mirror.NONE, 1.0F, 0)).then(Commands.argument("rotation", TemplateRotationArgument.templateRotation()).executes((commandcontext3) -> placeTemplate(commandcontext3.getSource(), ResourceLocationArgument.getId(commandcontext3, "template"), BlockPosArgument.getLoadedBlockPos(commandcontext3, "pos"), TemplateRotationArgument.getRotation(commandcontext3, "rotation"), Mirror.NONE, 1.0F, 0)).then(Commands.argument("mirror", TemplateMirrorArgument.templateMirror()).executes((commandcontext2) -> placeTemplate(commandcontext2.getSource(), ResourceLocationArgument.getId(commandcontext2, "template"), BlockPosArgument.getLoadedBlockPos(commandcontext2, "pos"), TemplateRotationArgument.getRotation(commandcontext2, "rotation"), TemplateMirrorArgument.getMirror(commandcontext2, "mirror"), 1.0F, 0)).then(Commands.argument("integrity", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((commandcontext1) -> placeTemplate(commandcontext1.getSource(), ResourceLocationArgument.getId(commandcontext1, "template"), BlockPosArgument.getLoadedBlockPos(commandcontext1, "pos"), TemplateRotationArgument.getRotation(commandcontext1, "rotation"), TemplateMirrorArgument.getMirror(commandcontext1, "mirror"), FloatArgumentType.getFloat(commandcontext1, "integrity"), 0)).then(Commands.argument("seed", IntegerArgumentType.integer()).executes((commandcontext) -> placeTemplate(commandcontext.getSource(), ResourceLocationArgument.getId(commandcontext, "template"), BlockPosArgument.getLoadedBlockPos(commandcontext, "pos"), TemplateRotationArgument.getRotation(commandcontext, "rotation"), TemplateMirrorArgument.getMirror(commandcontext, "mirror"), FloatArgumentType.getFloat(commandcontext, "integrity"), IntegerArgumentType.getInteger(commandcontext, "seed")))))))))));
   }

   public static int placeFeature(CommandSourceStack commandsourcestack, Holder.Reference<ConfiguredFeature<?, ?>> holder_reference, BlockPos blockpos) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      ConfiguredFeature<?, ?> configuredfeature = holder_reference.value();
      ChunkPos chunkpos = new ChunkPos(blockpos);
      checkLoaded(serverlevel, new ChunkPos(chunkpos.x - 1, chunkpos.z - 1), new ChunkPos(chunkpos.x + 1, chunkpos.z + 1));
      if (!configuredfeature.place(serverlevel, serverlevel.getChunkSource().getGenerator(), serverlevel.getRandom(), blockpos)) {
         throw ERROR_FEATURE_FAILED.create();
      } else {
         String s = holder_reference.key().location().toString();
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.place.feature.success", s, blockpos.getX(), blockpos.getY(), blockpos.getZ()), true);
         return 1;
      }
   }

   public static int placeJigsaw(CommandSourceStack commandsourcestack, Holder<StructureTemplatePool> holder, ResourceLocation resourcelocation, int i, BlockPos blockpos) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      if (!JigsawPlacement.generateJigsaw(serverlevel, holder, resourcelocation, i, blockpos, false)) {
         throw ERROR_JIGSAW_FAILED.create();
      } else {
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.place.jigsaw.success", blockpos.getX(), blockpos.getY(), blockpos.getZ()), true);
         return 1;
      }
   }

   public static int placeStructure(CommandSourceStack commandsourcestack, Holder.Reference<Structure> holder_reference, BlockPos blockpos) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Structure structure = holder_reference.value();
      ChunkGenerator chunkgenerator = serverlevel.getChunkSource().getGenerator();
      StructureStart structurestart = structure.generate(commandsourcestack.registryAccess(), chunkgenerator, chunkgenerator.getBiomeSource(), serverlevel.getChunkSource().randomState(), serverlevel.getStructureManager(), serverlevel.getSeed(), new ChunkPos(blockpos), 0, serverlevel, (holder) -> true);
      if (!structurestart.isValid()) {
         throw ERROR_STRUCTURE_FAILED.create();
      } else {
         BoundingBox boundingbox = structurestart.getBoundingBox();
         ChunkPos chunkpos = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
         ChunkPos chunkpos1 = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));
         checkLoaded(serverlevel, chunkpos, chunkpos1);
         ChunkPos.rangeClosed(chunkpos, chunkpos1).forEach((chunkpos2) -> structurestart.placeInChunk(serverlevel, serverlevel.structureManager(), chunkgenerator, serverlevel.getRandom(), new BoundingBox(chunkpos2.getMinBlockX(), serverlevel.getMinBuildHeight(), chunkpos2.getMinBlockZ(), chunkpos2.getMaxBlockX(), serverlevel.getMaxBuildHeight(), chunkpos2.getMaxBlockZ()), chunkpos2));
         String s = holder_reference.key().location().toString();
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.place.structure.success", s, blockpos.getX(), blockpos.getY(), blockpos.getZ()), true);
         return 1;
      }
   }

   public static int placeTemplate(CommandSourceStack commandsourcestack, ResourceLocation resourcelocation, BlockPos blockpos, Rotation rotation, Mirror mirror, float f, int i) throws CommandSyntaxException {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

      Optional<StructureTemplate> optional;
      try {
         optional = structuretemplatemanager.get(resourcelocation);
      } catch (ResourceLocationException var13) {
         throw ERROR_TEMPLATE_INVALID.create(resourcelocation);
      }

      if (optional.isEmpty()) {
         throw ERROR_TEMPLATE_INVALID.create(resourcelocation);
      } else {
         StructureTemplate structuretemplate = optional.get();
         checkLoaded(serverlevel, new ChunkPos(blockpos), new ChunkPos(blockpos.offset(structuretemplate.getSize())));
         StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings()).setMirror(mirror).setRotation(rotation);
         if (f < 1.0F) {
            structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(f)).setRandom(StructureBlockEntity.createRandom((long)i));
         }

         boolean flag = structuretemplate.placeInWorld(serverlevel, blockpos, blockpos, structureplacesettings, StructureBlockEntity.createRandom((long)i), 2);
         if (!flag) {
            throw ERROR_TEMPLATE_FAILED.create();
         } else {
            commandsourcestack.sendSuccess(() -> Component.translatable("commands.place.template.success", resourcelocation, blockpos.getX(), blockpos.getY(), blockpos.getZ()), true);
            return 1;
         }
      }
   }

   private static void checkLoaded(ServerLevel serverlevel, ChunkPos chunkpos, ChunkPos chunkpos1) throws CommandSyntaxException {
      if (ChunkPos.rangeClosed(chunkpos, chunkpos1).filter((chunkpos2) -> !serverlevel.isLoaded(chunkpos2.getWorldPosition())).findAny().isPresent()) {
         throw BlockPosArgument.ERROR_NOT_LOADED.create();
      }
   }
}
