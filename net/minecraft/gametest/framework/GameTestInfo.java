package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class GameTestInfo {
   private final TestFunction testFunction;
   @Nullable
   private BlockPos structureBlockPos;
   private final ServerLevel level;
   private final Collection<GameTestListener> listeners = Lists.newArrayList();
   private final int timeoutTicks;
   private final Collection<GameTestSequence> sequences = Lists.newCopyOnWriteArrayList();
   private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<>();
   private long startTick;
   private long tickCount;
   private boolean started;
   private final Stopwatch timer = Stopwatch.createUnstarted();
   private boolean done;
   private final Rotation rotation;
   @Nullable
   private Throwable error;
   @Nullable
   private StructureBlockEntity structureBlockEntity;

   public GameTestInfo(TestFunction testfunction, Rotation rotation, ServerLevel serverlevel) {
      this.testFunction = testfunction;
      this.level = serverlevel;
      this.timeoutTicks = testfunction.getMaxTicks();
      this.rotation = testfunction.getRotation().getRotated(rotation);
   }

   void setStructureBlockPos(BlockPos blockpos) {
      this.structureBlockPos = blockpos;
   }

   void startExecution() {
      this.startTick = this.level.getGameTime() + 1L + this.testFunction.getSetupTicks();
      this.timer.start();
   }

   public void tick() {
      if (!this.isDone()) {
         this.tickInternal();
         if (this.isDone()) {
            if (this.error != null) {
               this.listeners.forEach((gametestlistener1) -> gametestlistener1.testFailed(this));
            } else {
               this.listeners.forEach((gametestlistener) -> gametestlistener.testPassed(this));
            }
         }

      }
   }

   private void tickInternal() {
      this.tickCount = this.level.getGameTime() - this.startTick;
      if (this.tickCount >= 0L) {
         if (this.tickCount == 0L) {
            this.startTest();
         }

         ObjectIterator<Object2LongMap.Entry<Runnable>> objectiterator = this.runAtTickTimeMap.object2LongEntrySet().iterator();

         while(objectiterator.hasNext()) {
            Object2LongMap.Entry<Runnable> object2longmap_entry = objectiterator.next();
            if (object2longmap_entry.getLongValue() <= this.tickCount) {
               try {
                  object2longmap_entry.getKey().run();
               } catch (Exception var4) {
                  this.fail(var4);
               }

               objectiterator.remove();
            }
         }

         if (this.tickCount > (long)this.timeoutTicks) {
            if (this.sequences.isEmpty()) {
               this.fail(new GameTestTimeoutException("Didn't succeed or fail within " + this.testFunction.getMaxTicks() + " ticks"));
            } else {
               this.sequences.forEach((gametestsequence1) -> gametestsequence1.tickAndFailIfNotComplete(this.tickCount));
               if (this.error == null) {
                  this.fail(new GameTestTimeoutException("No sequences finished"));
               }
            }
         } else {
            this.sequences.forEach((gametestsequence) -> gametestsequence.tickAndContinue(this.tickCount));
         }

      }
   }

   private void startTest() {
      if (this.started) {
         throw new IllegalStateException("Test already started");
      } else {
         this.started = true;

         try {
            this.testFunction.run(new GameTestHelper(this));
         } catch (Exception var2) {
            this.fail(var2);
         }

      }
   }

   public void setRunAtTickTime(long i, Runnable runnable) {
      this.runAtTickTimeMap.put(runnable, i);
   }

   public String getTestName() {
      return this.testFunction.getTestName();
   }

   public BlockPos getStructureBlockPos() {
      return this.structureBlockPos;
   }

   @Nullable
   public Vec3i getStructureSize() {
      StructureBlockEntity structureblockentity = this.getStructureBlockEntity();
      return structureblockentity == null ? null : structureblockentity.getStructureSize();
   }

   @Nullable
   public AABB getStructureBounds() {
      StructureBlockEntity structureblockentity = this.getStructureBlockEntity();
      return structureblockentity == null ? null : StructureUtils.getStructureBounds(structureblockentity);
   }

   @Nullable
   private StructureBlockEntity getStructureBlockEntity() {
      return (StructureBlockEntity)this.level.getBlockEntity(this.structureBlockPos);
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   public boolean hasSucceeded() {
      return this.done && this.error == null;
   }

   public boolean hasFailed() {
      return this.error != null;
   }

   public boolean hasStarted() {
      return this.started;
   }

   public boolean isDone() {
      return this.done;
   }

   public long getRunTime() {
      return this.timer.elapsed(TimeUnit.MILLISECONDS);
   }

   private void finish() {
      if (!this.done) {
         this.done = true;
         this.timer.stop();
      }

   }

   public void succeed() {
      if (this.error == null) {
         this.finish();
      }

   }

   public void fail(Throwable throwable) {
      this.error = throwable;
      this.finish();
   }

   @Nullable
   public Throwable getError() {
      return this.error;
   }

   public String toString() {
      return this.getTestName();
   }

   public void addListener(GameTestListener gametestlistener) {
      this.listeners.add(gametestlistener);
   }

   public void spawnStructure(BlockPos blockpos, int i) {
      this.structureBlockEntity = StructureUtils.spawnStructure(this.getStructureName(), blockpos, this.getRotation(), i, this.level, false);
      this.structureBlockPos = this.structureBlockEntity.getBlockPos();
      this.structureBlockEntity.setStructureName(this.getTestName());
      StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
      this.listeners.forEach((gametestlistener) -> gametestlistener.testStructureLoaded(this));
   }

   public void clearStructure() {
      if (this.structureBlockEntity == null) {
         throw new IllegalStateException("Expected structure to be initialized, but it was null");
      } else {
         BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(this.structureBlockEntity);
         StructureUtils.clearSpaceForStructure(boundingbox, this.structureBlockPos.getY(), this.level);
      }
   }

   long getTick() {
      return this.tickCount;
   }

   GameTestSequence createSequence() {
      GameTestSequence gametestsequence = new GameTestSequence(this);
      this.sequences.add(gametestsequence);
      return gametestsequence;
   }

   public boolean isRequired() {
      return this.testFunction.isRequired();
   }

   public boolean isOptional() {
      return !this.testFunction.isRequired();
   }

   public String getStructureName() {
      return this.testFunction.getStructureName();
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public TestFunction getTestFunction() {
      return this.testFunction;
   }

   public int getTimeoutTicks() {
      return this.timeoutTicks;
   }

   public boolean isFlaky() {
      return this.testFunction.isFlaky();
   }

   public int maxAttempts() {
      return this.testFunction.getMaxAttempts();
   }

   public int requiredSuccesses() {
      return this.testFunction.getRequiredSuccesses();
   }
}
