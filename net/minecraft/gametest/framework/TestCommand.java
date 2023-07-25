package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;

public class TestCommand {
   private static final int DEFAULT_CLEAR_RADIUS = 200;
   private static final int MAX_CLEAR_RADIUS = 1024;
   private static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
   private static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
   private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
   private static final int SHOW_POS_DURATION_MS = 10000;
   private static final int DEFAULT_X_SIZE = 5;
   private static final int DEFAULT_Y_SIZE = 5;
   private static final int DEFAULT_Z_SIZE = 5;

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("test").then(Commands.literal("runthis").executes((commandcontext23) -> runNearbyTest(commandcontext23.getSource()))).then(Commands.literal("runthese").executes((commandcontext22) -> runAllNearbyTests(commandcontext22.getSource()))).then(Commands.literal("runfailed").executes((commandcontext21) -> runLastFailedTests(commandcontext21.getSource(), false, 0, 8)).then(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()).executes((commandcontext20) -> runLastFailedTests(commandcontext20.getSource(), BoolArgumentType.getBool(commandcontext20, "onlyRequiredTests"), 0, 8)).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext19) -> runLastFailedTests(commandcontext19.getSource(), BoolArgumentType.getBool(commandcontext19, "onlyRequiredTests"), IntegerArgumentType.getInteger(commandcontext19, "rotationSteps"), 8)).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((commandcontext18) -> runLastFailedTests(commandcontext18.getSource(), BoolArgumentType.getBool(commandcontext18, "onlyRequiredTests"), IntegerArgumentType.getInteger(commandcontext18, "rotationSteps"), IntegerArgumentType.getInteger(commandcontext18, "testsPerRow"))))))).then(Commands.literal("run").then(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes((commandcontext17) -> runTest(commandcontext17.getSource(), TestFunctionArgument.getTestFunction(commandcontext17, "testName"), 0)).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext16) -> runTest(commandcontext16.getSource(), TestFunctionArgument.getTestFunction(commandcontext16, "testName"), IntegerArgumentType.getInteger(commandcontext16, "rotationSteps")))))).then(Commands.literal("runall").executes((commandcontext15) -> runAllTests(commandcontext15.getSource(), 0, 8)).then(Commands.argument("testClassName", TestClassNameArgument.testClassName()).executes((commandcontext14) -> runAllTestsInClass(commandcontext14.getSource(), TestClassNameArgument.getTestClassName(commandcontext14, "testClassName"), 0, 8)).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext13) -> runAllTestsInClass(commandcontext13.getSource(), TestClassNameArgument.getTestClassName(commandcontext13, "testClassName"), IntegerArgumentType.getInteger(commandcontext13, "rotationSteps"), 8)).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((commandcontext12) -> runAllTestsInClass(commandcontext12.getSource(), TestClassNameArgument.getTestClassName(commandcontext12, "testClassName"), IntegerArgumentType.getInteger(commandcontext12, "rotationSteps"), IntegerArgumentType.getInteger(commandcontext12, "testsPerRow")))))).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext11) -> runAllTests(commandcontext11.getSource(), IntegerArgumentType.getInteger(commandcontext11, "rotationSteps"), 8)).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((commandcontext10) -> runAllTests(commandcontext10.getSource(), IntegerArgumentType.getInteger(commandcontext10, "rotationSteps"), IntegerArgumentType.getInteger(commandcontext10, "testsPerRow")))))).then(Commands.literal("export").then(Commands.argument("testName", StringArgumentType.word()).executes((commandcontext9) -> exportTestStructure(commandcontext9.getSource(), StringArgumentType.getString(commandcontext9, "testName"))))).then(Commands.literal("exportthis").executes((commandcontext8) -> exportNearestTestStructure(commandcontext8.getSource()))).then(Commands.literal("import").then(Commands.argument("testName", StringArgumentType.word()).executes((commandcontext7) -> importTestStructure(commandcontext7.getSource(), StringArgumentType.getString(commandcontext7, "testName"))))).then(Commands.literal("pos").executes((commandcontext6) -> showPos(commandcontext6.getSource(), "pos")).then(Commands.argument("var", StringArgumentType.word()).executes((commandcontext5) -> showPos(commandcontext5.getSource(), StringArgumentType.getString(commandcontext5, "var"))))).then(Commands.literal("create").then(Commands.argument("testName", StringArgumentType.word()).executes((commandcontext4) -> createNewStructure(commandcontext4.getSource(), StringArgumentType.getString(commandcontext4, "testName"), 5, 5, 5)).then(Commands.argument("width", IntegerArgumentType.integer()).executes((commandcontext3) -> createNewStructure(commandcontext3.getSource(), StringArgumentType.getString(commandcontext3, "testName"), IntegerArgumentType.getInteger(commandcontext3, "width"), IntegerArgumentType.getInteger(commandcontext3, "width"), IntegerArgumentType.getInteger(commandcontext3, "width"))).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes((commandcontext2) -> createNewStructure(commandcontext2.getSource(), StringArgumentType.getString(commandcontext2, "testName"), IntegerArgumentType.getInteger(commandcontext2, "width"), IntegerArgumentType.getInteger(commandcontext2, "height"), IntegerArgumentType.getInteger(commandcontext2, "depth")))))))).then(Commands.literal("clearall").executes((commandcontext1) -> clearAllTests(commandcontext1.getSource(), 200)).then(Commands.argument("radius", IntegerArgumentType.integer()).executes((commandcontext) -> clearAllTests(commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "radius"))))));
   }

   private static int createNewStructure(CommandSourceStack commandsourcestack, String s, int i, int j, int k) {
      if (i <= 48 && j <= 48 && k <= 48) {
         ServerLevel serverlevel = commandsourcestack.getLevel();
         BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), commandsourcestack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
         StructureUtils.createNewEmptyStructureBlock(s.toLowerCase(), blockpos1, new Vec3i(i, j, k), Rotation.NONE, serverlevel);

         for(int l = 0; l < i; ++l) {
            for(int i1 = 0; i1 < k; ++i1) {
               BlockPos blockpos2 = new BlockPos(blockpos1.getX() + l, blockpos1.getY() + 1, blockpos1.getZ() + i1);
               Block block = Blocks.POLISHED_ANDESITE;
               BlockInput blockinput = new BlockInput(block.defaultBlockState(), Collections.emptySet(), (CompoundTag)null);
               blockinput.place(serverlevel, blockpos2, 2);
            }
         }

         StructureUtils.addCommandBlockAndButtonToStartTest(blockpos1, new BlockPos(1, 0, -1), Rotation.NONE, serverlevel);
         return 0;
      } else {
         throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
      }
   }

   private static int showPos(CommandSourceStack commandsourcestack, String s) throws CommandSyntaxException {
      BlockHitResult blockhitresult = (BlockHitResult)commandsourcestack.getPlayerOrException().pick(10.0D, 1.0F, false);
      BlockPos blockpos = blockhitresult.getBlockPos();
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockpos, 15, serverlevel);
      if (!optional.isPresent()) {
         optional = StructureUtils.findStructureBlockContainingPos(blockpos, 200, serverlevel);
      }

      if (!optional.isPresent()) {
         commandsourcestack.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockpos));
         return 0;
      } else {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(optional.get());
         BlockPos blockpos1 = blockpos.subtract(optional.get());
         String s1 = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
         String s2 = structureblockentity.getStructurePath();
         Component component = Component.literal(s1).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + s + " = new BlockPos(" + s1 + ");")));
         commandsourcestack.sendSuccess(() -> Component.literal("Position relative to " + s2 + ": ").append(component), false);
         DebugPackets.sendGameTestAddMarker(serverlevel, new BlockPos(blockpos), s1, -2147418368, 10000);
         return 1;
      }
   }

   private static int runNearbyTest(CommandSourceStack commandsourcestack) {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      ServerLevel serverlevel = commandsourcestack.getLevel();
      BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
      if (blockpos1 == null) {
         say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
         return 0;
      } else {
         GameTestRunner.clearMarkers(serverlevel);
         runTest(serverlevel, blockpos1, (MultipleTestTracker)null);
         return 1;
      }
   }

   private static int runAllNearbyTests(CommandSourceStack commandsourcestack) {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockpos, 200, serverlevel);
      if (collection.isEmpty()) {
         say(serverlevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
         return 1;
      } else {
         GameTestRunner.clearMarkers(serverlevel);
         say(commandsourcestack, "Running " + collection.size() + " tests...");
         MultipleTestTracker multipletesttracker = new MultipleTestTracker();
         collection.forEach((blockpos1) -> runTest(serverlevel, blockpos1, multipletesttracker));
         return 1;
      }
   }

   private static void runTest(ServerLevel serverlevel, BlockPos blockpos, @Nullable MultipleTestTracker multipletesttracker) {
      StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos);
      String s = structureblockentity.getStructurePath();
      TestFunction testfunction = GameTestRegistry.getTestFunction(s);
      GameTestInfo gametestinfo = new GameTestInfo(testfunction, structureblockentity.getRotation(), serverlevel);
      if (multipletesttracker != null) {
         multipletesttracker.addTestToTrack(gametestinfo);
         gametestinfo.addListener(new TestCommand.TestSummaryDisplayer(serverlevel, multipletesttracker));
      }

      runTestPreparation(testfunction, serverlevel);
      AABB aabb = StructureUtils.getStructureBounds(structureblockentity);
      BlockPos blockpos1 = BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ);
      GameTestRunner.runTest(gametestinfo, blockpos1, GameTestTicker.SINGLETON);
   }

   static void showTestSummaryIfAllDone(ServerLevel serverlevel, MultipleTestTracker multipletesttracker) {
      if (multipletesttracker.isDone()) {
         say(serverlevel, "GameTest done! " + multipletesttracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
         if (multipletesttracker.hasFailedRequired()) {
            say(serverlevel, multipletesttracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
         } else {
            say(serverlevel, "All required tests passed :)", ChatFormatting.GREEN);
         }

         if (multipletesttracker.hasFailedOptional()) {
            say(serverlevel, multipletesttracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
         }
      }

   }

   private static int clearAllTests(CommandSourceStack commandsourcestack, int i) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      GameTestRunner.clearMarkers(serverlevel);
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition().x, (double)commandsourcestack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(commandsourcestack.getPosition())).getY(), commandsourcestack.getPosition().z);
      GameTestRunner.clearAllTests(serverlevel, blockpos, GameTestTicker.SINGLETON, Mth.clamp(i, 0, 1024));
      return 1;
   }

   private static int runTest(CommandSourceStack commandsourcestack, TestFunction testfunction, int i) {
      ServerLevel serverlevel = commandsourcestack.getLevel();
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      int j = commandsourcestack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY();
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), j, blockpos.getZ() + 3);
      GameTestRunner.clearMarkers(serverlevel);
      runTestPreparation(testfunction, serverlevel);
      Rotation rotation = StructureUtils.getRotationForRotationSteps(i);
      GameTestInfo gametestinfo = new GameTestInfo(testfunction, rotation, serverlevel);
      GameTestRunner.runTest(gametestinfo, blockpos1, GameTestTicker.SINGLETON);
      return 1;
   }

   private static void runTestPreparation(TestFunction testfunction, ServerLevel serverlevel) {
      Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(testfunction.getBatchName());
      if (consumer != null) {
         consumer.accept(serverlevel);
      }

   }

   private static int runAllTests(CommandSourceStack commandsourcestack, int i, int j) {
      GameTestRunner.clearMarkers(commandsourcestack.getLevel());
      Collection<TestFunction> collection = GameTestRegistry.getAllTestFunctions();
      say(commandsourcestack, "Running all " + collection.size() + " tests...");
      GameTestRegistry.forgetFailedTests();
      runTests(commandsourcestack, collection, i, j);
      return 1;
   }

   private static int runAllTestsInClass(CommandSourceStack commandsourcestack, String s, int i, int j) {
      Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(s);
      GameTestRunner.clearMarkers(commandsourcestack.getLevel());
      say(commandsourcestack, "Running " + collection.size() + " tests from " + s + "...");
      GameTestRegistry.forgetFailedTests();
      runTests(commandsourcestack, collection, i, j);
      return 1;
   }

   private static int runLastFailedTests(CommandSourceStack commandsourcestack, boolean flag, int i, int j) {
      Collection<TestFunction> collection;
      if (flag) {
         collection = GameTestRegistry.getLastFailedTests().stream().filter(TestFunction::isRequired).collect(Collectors.toList());
      } else {
         collection = GameTestRegistry.getLastFailedTests();
      }

      if (collection.isEmpty()) {
         say(commandsourcestack, "No failed tests to rerun");
         return 0;
      } else {
         GameTestRunner.clearMarkers(commandsourcestack.getLevel());
         say(commandsourcestack, "Rerunning " + collection.size() + " failed tests (" + (flag ? "only required tests" : "including optional tests") + ")");
         runTests(commandsourcestack, collection, i, j);
         return 1;
      }
   }

   private static void runTests(CommandSourceStack commandsourcestack, Collection<TestFunction> collection, int i, int j) {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), commandsourcestack.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
      ServerLevel serverlevel = commandsourcestack.getLevel();
      Rotation rotation = StructureUtils.getRotationForRotationSteps(i);
      Collection<GameTestInfo> collection1 = GameTestRunner.runTests(collection, blockpos1, rotation, serverlevel, GameTestTicker.SINGLETON, j);
      MultipleTestTracker multipletesttracker = new MultipleTestTracker(collection1);
      multipletesttracker.addListener(new TestCommand.TestSummaryDisplayer(serverlevel, multipletesttracker));
      multipletesttracker.addFailureListener((gametestinfo) -> GameTestRegistry.rememberFailedTest(gametestinfo.getTestFunction()));
   }

   private static void say(CommandSourceStack commandsourcestack, String s) {
      commandsourcestack.sendSuccess(() -> Component.literal(s), false);
   }

   private static int exportNearestTestStructure(CommandSourceStack commandsourcestack) {
      BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
      ServerLevel serverlevel = commandsourcestack.getLevel();
      BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
      if (blockpos1 == null) {
         say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
         return 0;
      } else {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos1);
         String s = structureblockentity.getStructurePath();
         return exportTestStructure(commandsourcestack, s);
      }
   }

   private static int exportTestStructure(CommandSourceStack commandsourcestack, String s) {
      Path path = Paths.get(StructureUtils.testStructuresDir);
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);
      Path path1 = commandsourcestack.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");
      Path path2 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path1, s, path);
      if (path2 == null) {
         say(commandsourcestack, "Failed to export " + path1);
         return 1;
      } else {
         try {
            Files.createDirectories(path2.getParent());
         } catch (IOException var7) {
            say(commandsourcestack, "Could not create folder " + path2.getParent());
            var7.printStackTrace();
            return 1;
         }

         say(commandsourcestack, "Exported " + s + " to " + path2.toAbsolutePath());
         return 0;
      }
   }

   private static int importTestStructure(CommandSourceStack commandsourcestack, String s) {
      Path path = Paths.get(StructureUtils.testStructuresDir, s + ".snbt");
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", s);
      Path path1 = commandsourcestack.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");

      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path);
         String s1 = IOUtils.toString((Reader)bufferedreader);
         Files.createDirectories(path1.getParent());
         OutputStream outputstream = Files.newOutputStream(path1);

         try {
            NbtIo.writeCompressed(NbtUtils.snbtToStructure(s1), outputstream);
         } catch (Throwable var11) {
            if (outputstream != null) {
               try {
                  outputstream.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (outputstream != null) {
            outputstream.close();
         }

         say(commandsourcestack, "Imported to " + path1.toAbsolutePath());
         return 0;
      } catch (CommandSyntaxException | IOException var12) {
         System.err.println("Failed to load structure " + s);
         var12.printStackTrace();
         return 1;
      }
   }

   private static void say(ServerLevel serverlevel, String s, ChatFormatting chatformatting) {
      serverlevel.getPlayers((serverplayer1) -> true).forEach((serverplayer) -> serverplayer.sendSystemMessage(Component.literal(chatformatting + s)));
   }

   static class TestSummaryDisplayer implements GameTestListener {
      private final ServerLevel level;
      private final MultipleTestTracker tracker;

      public TestSummaryDisplayer(ServerLevel serverlevel, MultipleTestTracker multipletesttracker) {
         this.level = serverlevel;
         this.tracker = multipletesttracker;
      }

      public void testStructureLoaded(GameTestInfo gametestinfo) {
      }

      public void testPassed(GameTestInfo gametestinfo) {
         TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
      }

      public void testFailed(GameTestInfo gametestinfo) {
         TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
      }
   }
}
