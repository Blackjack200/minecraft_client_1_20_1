package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestRunner {
   private static final int MAX_TESTS_PER_BATCH = 100;
   public static final int PADDING_AROUND_EACH_STRUCTURE = 2;
   public static final int SPACE_BETWEEN_COLUMNS = 5;
   public static final int SPACE_BETWEEN_ROWS = 6;
   public static final int DEFAULT_TESTS_PER_ROW = 8;

   public static void runTest(GameTestInfo gametestinfo, BlockPos blockpos, GameTestTicker gametestticker) {
      gametestinfo.startExecution();
      gametestticker.add(gametestinfo);
      gametestinfo.addListener(new ReportGameListener(gametestinfo, gametestticker, blockpos));
      gametestinfo.spawnStructure(blockpos, 2);
   }

   public static Collection<GameTestInfo> runTestBatches(Collection<GameTestBatch> collection, BlockPos blockpos, Rotation rotation, ServerLevel serverlevel, GameTestTicker gametestticker, int i) {
      GameTestBatchRunner gametestbatchrunner = new GameTestBatchRunner(collection, blockpos, rotation, serverlevel, gametestticker, i);
      gametestbatchrunner.start();
      return gametestbatchrunner.getTestInfos();
   }

   public static Collection<GameTestInfo> runTests(Collection<TestFunction> collection, BlockPos blockpos, Rotation rotation, ServerLevel serverlevel, GameTestTicker gametestticker, int i) {
      return runTestBatches(groupTestsIntoBatches(collection), blockpos, rotation, serverlevel, gametestticker, i);
   }

   public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> collection) {
      Map<String, List<TestFunction>> map = collection.stream().collect(Collectors.groupingBy(TestFunction::getBatchName));
      return map.entrySet().stream().flatMap((map_entry) -> {
         String s = map_entry.getKey();
         Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(s);
         Consumer<ServerLevel> consumer1 = GameTestRegistry.getAfterBatchFunction(s);
         MutableInt mutableint = new MutableInt();
         Collection<TestFunction> collection1 = map_entry.getValue();
         return Streams.stream(Iterables.partition(collection1, 100)).map((list) -> new GameTestBatch(s + ":" + mutableint.incrementAndGet(), ImmutableList.copyOf(list), consumer, consumer1));
      }).collect(ImmutableList.toImmutableList());
   }

   public static void clearAllTests(ServerLevel serverlevel, BlockPos blockpos, GameTestTicker gametestticker, int i) {
      gametestticker.clear();
      BlockPos blockpos1 = blockpos.offset(-i, 0, -i);
      BlockPos blockpos2 = blockpos.offset(i, 0, i);
      BlockPos.betweenClosedStream(blockpos1, blockpos2).filter((blockpos5) -> serverlevel.getBlockState(blockpos5).is(Blocks.STRUCTURE_BLOCK)).forEach((blockpos3) -> {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos3);
         BlockPos blockpos4 = structureblockentity.getBlockPos();
         BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structureblockentity);
         StructureUtils.clearSpaceForStructure(boundingbox, blockpos4.getY(), serverlevel);
      });
   }

   public static void clearMarkers(ServerLevel serverlevel) {
      DebugPackets.sendGameTestClearPacket(serverlevel);
   }
}
