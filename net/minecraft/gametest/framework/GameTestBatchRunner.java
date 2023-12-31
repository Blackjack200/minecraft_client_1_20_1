package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class GameTestBatchRunner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockPos firstTestNorthWestCorner;
   final ServerLevel level;
   private final GameTestTicker testTicker;
   private final int testsPerRow;
   private final List<GameTestInfo> allTestInfos;
   private final List<Pair<GameTestBatch, Collection<GameTestInfo>>> batches;
   private final BlockPos.MutableBlockPos nextTestNorthWestCorner;

   public GameTestBatchRunner(Collection<GameTestBatch> collection, BlockPos blockpos, Rotation rotation, ServerLevel serverlevel, GameTestTicker gametestticker, int i) {
      this.nextTestNorthWestCorner = blockpos.mutable();
      this.firstTestNorthWestCorner = blockpos;
      this.level = serverlevel;
      this.testTicker = gametestticker;
      this.testsPerRow = i;
      this.batches = collection.stream().map((gametestbatch) -> {
         Collection<GameTestInfo> collection1 = gametestbatch.getTestFunctions().stream().map((testfunction) -> new GameTestInfo(testfunction, rotation, serverlevel)).collect(ImmutableList.toImmutableList());
         return Pair.of(gametestbatch, collection1);
      }).collect(ImmutableList.toImmutableList());
      this.allTestInfos = this.batches.stream().flatMap((pair) -> pair.getSecond().stream()).collect(ImmutableList.toImmutableList());
   }

   public List<GameTestInfo> getTestInfos() {
      return this.allTestInfos;
   }

   public void start() {
      this.runBatch(0);
   }

   void runBatch(final int i) {
      if (i < this.batches.size()) {
         Pair<GameTestBatch, Collection<GameTestInfo>> pair = this.batches.get(i);
         final GameTestBatch gametestbatch = pair.getFirst();
         Collection<GameTestInfo> collection = pair.getSecond();
         Map<GameTestInfo, BlockPos> map = this.createStructuresForBatch(collection);
         String s = gametestbatch.getName();
         LOGGER.info("Running test batch '{}' ({} tests)...", s, collection.size());
         gametestbatch.runBeforeBatchFunction(this.level);
         final MultipleTestTracker multipletesttracker = new MultipleTestTracker();
         collection.forEach(multipletesttracker::addTestToTrack);
         multipletesttracker.addListener(new GameTestListener() {
            private void testCompleted() {
               if (multipletesttracker.isDone()) {
                  gametestbatch.runAfterBatchFunction(GameTestBatchRunner.this.level);
                  GameTestBatchRunner.this.runBatch(i + 1);
               }

            }

            public void testStructureLoaded(GameTestInfo gametestinfo) {
            }

            public void testPassed(GameTestInfo gametestinfo) {
               this.testCompleted();
            }

            public void testFailed(GameTestInfo gametestinfo) {
               this.testCompleted();
            }
         });
         collection.forEach((gametestinfo) -> {
            BlockPos blockpos = map.get(gametestinfo);
            GameTestRunner.runTest(gametestinfo, blockpos, this.testTicker);
         });
      }
   }

   private Map<GameTestInfo, BlockPos> createStructuresForBatch(Collection<GameTestInfo> collection) {
      Map<GameTestInfo, BlockPos> map = Maps.newHashMap();
      int i = 0;
      AABB aabb = new AABB(this.nextTestNorthWestCorner);

      for(GameTestInfo gametestinfo : collection) {
         BlockPos blockpos = new BlockPos(this.nextTestNorthWestCorner);
         StructureBlockEntity structureblockentity = StructureUtils.spawnStructure(gametestinfo.getStructureName(), blockpos, gametestinfo.getRotation(), 2, this.level, true);
         AABB aabb1 = StructureUtils.getStructureBounds(structureblockentity);
         gametestinfo.setStructureBlockPos(structureblockentity.getBlockPos());
         map.put(gametestinfo, new BlockPos(this.nextTestNorthWestCorner));
         aabb = aabb.minmax(aabb1);
         this.nextTestNorthWestCorner.move((int)aabb1.getXsize() + 5, 0, 0);
         if (i++ % this.testsPerRow == this.testsPerRow - 1) {
            this.nextTestNorthWestCorner.move(0, 0, (int)aabb.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            aabb = new AABB(this.nextTestNorthWestCorner);
         }
      }

      return map;
   }
}
