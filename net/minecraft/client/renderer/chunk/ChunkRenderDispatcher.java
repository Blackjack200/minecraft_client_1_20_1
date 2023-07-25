package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ChunkRenderDispatcher {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_WORKERS_32_BIT = 4;
   private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
   private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
   private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
   private final Queue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
   private int highPriorityQuota = 2;
   private final Queue<ChunkBufferBuilderPack> freeBuffers;
   private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
   private volatile int toBatchCount;
   private volatile int freeBufferCount;
   final ChunkBufferBuilderPack fixedBuffers;
   private final ProcessorMailbox<Runnable> mailbox;
   private final Executor executor;
   ClientLevel level;
   final LevelRenderer renderer;
   private Vec3 camera = Vec3.ZERO;

   public ChunkRenderDispatcher(ClientLevel clientlevel, LevelRenderer levelrenderer, Executor executor, boolean flag, ChunkBufferBuilderPack chunkbufferbuilderpack) {
      this.level = clientlevel;
      this.renderer = levelrenderer;
      int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
      int j = Runtime.getRuntime().availableProcessors();
      int k = flag ? j : Math.min(j, 4);
      int l = Math.max(1, Math.min(k, i));
      this.fixedBuffers = chunkbufferbuilderpack;
      List<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);

      try {
         for(int i1 = 0; i1 < l; ++i1) {
            list.add(new ChunkBufferBuilderPack());
         }
      } catch (OutOfMemoryError var14) {
         LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
         int j1 = Math.min(list.size() * 2 / 3, list.size() - 1);

         for(int k1 = 0; k1 < j1; ++k1) {
            list.remove(list.size() - 1);
         }

         System.gc();
      }

      this.freeBuffers = Queues.newArrayDeque(list);
      this.freeBufferCount = this.freeBuffers.size();
      this.executor = executor;
      this.mailbox = ProcessorMailbox.create(executor, "Chunk Renderer");
      this.mailbox.tell(this::runTask);
   }

   public void setLevel(ClientLevel clientlevel) {
      this.level = clientlevel;
   }

   private void runTask() {
      if (!this.freeBuffers.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask = this.pollTask();
         if (chunkrenderdispatcher_renderchunk_chunkcompiletask != null) {
            ChunkBufferBuilderPack chunkbufferbuilderpack1 = this.freeBuffers.poll();
            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.freeBufferCount = this.freeBuffers.size();
            CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(chunkrenderdispatcher_renderchunk_chunkcompiletask.name(), () -> chunkrenderdispatcher_renderchunk_chunkcompiletask.doTask(chunkbufferbuilderpack1)), this.executor).thenCompose((completablefuture) -> completablefuture).whenComplete((chunkrenderdispatcher_chunktaskresult, throwable) -> {
               if (throwable != null) {
                  Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Batching chunks"));
               } else {
                  this.mailbox.tell(() -> {
                     if (chunkrenderdispatcher_chunktaskresult == ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL) {
                        chunkbufferbuilderpack1.clearAll();
                     } else {
                        chunkbufferbuilderpack1.discardAll();
                     }

                     this.freeBuffers.add(chunkbufferbuilderpack1);
                     this.freeBufferCount = this.freeBuffers.size();
                     this.runTask();
                  });
               }
            });
         }
      }
   }

   @Nullable
   private ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pollTask() {
      if (this.highPriorityQuota <= 0) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask = this.toBatchLowPriority.poll();
         if (chunkrenderdispatcher_renderchunk_chunkcompiletask != null) {
            this.highPriorityQuota = 2;
            return chunkrenderdispatcher_renderchunk_chunkcompiletask;
         }
      }

      ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask1 = this.toBatchHighPriority.poll();
      if (chunkrenderdispatcher_renderchunk_chunkcompiletask1 != null) {
         --this.highPriorityQuota;
         return chunkrenderdispatcher_renderchunk_chunkcompiletask1;
      } else {
         this.highPriorityQuota = 2;
         return this.toBatchLowPriority.poll();
      }
   }

   public String getStats() {
      return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
   }

   public int getToBatchCount() {
      return this.toBatchCount;
   }

   public int getToUpload() {
      return this.toUpload.size();
   }

   public int getFreeBufferCount() {
      return this.freeBufferCount;
   }

   public void setCamera(Vec3 vec3) {
      this.camera = vec3;
   }

   public Vec3 getCameraPosition() {
      return this.camera;
   }

   public void uploadAllPendingUploads() {
      Runnable runnable;
      while((runnable = this.toUpload.poll()) != null) {
         runnable.run();
      }

   }

   public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher_renderchunk, RenderRegionCache renderregioncache) {
      chunkrenderdispatcher_renderchunk.compileSync(renderregioncache);
   }

   public void blockUntilClear() {
      this.clearBatchQueue();
   }

   public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask) {
      this.mailbox.tell(() -> {
         if (chunkrenderdispatcher_renderchunk_chunkcompiletask.isHighPriority) {
            this.toBatchHighPriority.offer(chunkrenderdispatcher_renderchunk_chunkcompiletask);
         } else {
            this.toBatchLowPriority.offer(chunkrenderdispatcher_renderchunk_chunkcompiletask);
         }

         this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
         this.runTask();
      });
   }

   public CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer, VertexBuffer vertexbuffer) {
      return CompletableFuture.runAsync(() -> {
         if (!vertexbuffer.isInvalid()) {
            vertexbuffer.bind();
            vertexbuffer.upload(bufferbuilder_renderedbuffer);
            VertexBuffer.unbind();
         }
      }, this.toUpload::add);
   }

   private void clearBatchQueue() {
      while(!this.toBatchHighPriority.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask = this.toBatchHighPriority.poll();
         if (chunkrenderdispatcher_renderchunk_chunkcompiletask != null) {
            chunkrenderdispatcher_renderchunk_chunkcompiletask.cancel();
         }
      }

      while(!this.toBatchLowPriority.isEmpty()) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask1 = this.toBatchLowPriority.poll();
         if (chunkrenderdispatcher_renderchunk_chunkcompiletask1 != null) {
            chunkrenderdispatcher_renderchunk_chunkcompiletask1.cancel();
         }
      }

      this.toBatchCount = 0;
   }

   public boolean isQueueEmpty() {
      return this.toBatchCount == 0 && this.toUpload.isEmpty();
   }

   public void dispose() {
      this.clearBatchQueue();
      this.mailbox.close();
      this.freeBuffers.clear();
   }

   static enum ChunkTaskResult {
      SUCCESSFUL,
      CANCELLED;
   }

   public static class CompiledChunk {
      public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk() {
         public boolean facesCanSeeEachother(Direction direction, Direction direction1) {
            return false;
         }
      };
      final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
      final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
      VisibilitySet visibilitySet = new VisibilitySet();
      @Nullable
      BufferBuilder.SortState transparencyState;

      public boolean hasNoRenderableLayers() {
         return this.hasBlocks.isEmpty();
      }

      public boolean isEmpty(RenderType rendertype) {
         return !this.hasBlocks.contains(rendertype);
      }

      public List<BlockEntity> getRenderableBlockEntities() {
         return this.renderableBlockEntities;
      }

      public boolean facesCanSeeEachother(Direction direction, Direction direction1) {
         return this.visibilitySet.visibilityBetween(direction, direction1);
      }
   }

   public class RenderChunk {
      public static final int SIZE = 16;
      public final int index;
      public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
      final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
      @Nullable
      private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
      private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
      private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((rendertype1) -> rendertype1, (rendertype) -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
      private AABB bb;
      private boolean dirty = true;
      final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
      private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], (ablockpos_mutableblockpos) -> {
         for(int i1 = 0; i1 < ablockpos_mutableblockpos.length; ++i1) {
            ablockpos_mutableblockpos[i1] = new BlockPos.MutableBlockPos();
         }

      });
      private boolean playerChanged;

      public RenderChunk(int i, int j, int k, int l) {
         this.index = i;
         this.setOrigin(j, k, l);
      }

      private boolean doesChunkExistAt(BlockPos blockpos) {
         return ChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()), ChunkStatus.FULL, false) != null;
      }

      public boolean hasAllNeighbors() {
         int i = 24;
         if (!(this.getDistToPlayerSqr() > 576.0D)) {
            return true;
         } else {
            return this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
         }
      }

      public AABB getBoundingBox() {
         return this.bb;
      }

      public VertexBuffer getBuffer(RenderType rendertype) {
         return this.buffers.get(rendertype);
      }

      public void setOrigin(int i, int j, int k) {
         this.reset();
         this.origin.set(i, j, k);
         this.bb = new AABB((double)i, (double)j, (double)k, (double)(i + 16), (double)(j + 16), (double)(k + 16));

         for(Direction direction : Direction.values()) {
            this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
         }

      }

      protected double getDistToPlayerSqr() {
         Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         double d0 = this.bb.minX + 8.0D - camera.getPosition().x;
         double d1 = this.bb.minY + 8.0D - camera.getPosition().y;
         double d2 = this.bb.minZ + 8.0D - camera.getPosition().z;
         return d0 * d0 + d1 * d1 + d2 * d2;
      }

      void beginLayer(BufferBuilder bufferbuilder) {
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
      }

      public ChunkRenderDispatcher.CompiledChunk getCompiledChunk() {
         return this.compiled.get();
      }

      private void reset() {
         this.cancelTasks();
         this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
         this.dirty = true;
      }

      public void releaseBuffers() {
         this.reset();
         this.buffers.values().forEach(VertexBuffer::close);
      }

      public BlockPos getOrigin() {
         return this.origin;
      }

      public void setDirty(boolean flag) {
         boolean flag1 = this.dirty;
         this.dirty = true;
         this.playerChanged = flag | (flag1 && this.playerChanged);
      }

      public void setNotDirty() {
         this.dirty = false;
         this.playerChanged = false;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public boolean isDirtyFromPlayer() {
         return this.dirty && this.playerChanged;
      }

      public BlockPos getRelativeOrigin(Direction direction) {
         return this.relativeOrigins[direction.ordinal()];
      }

      public boolean resortTransparency(RenderType rendertype, ChunkRenderDispatcher chunkrenderdispatcher) {
         ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher_compiledchunk = this.getCompiledChunk();
         if (this.lastResortTransparencyTask != null) {
            this.lastResortTransparencyTask.cancel();
         }

         if (!chunkrenderdispatcher_compiledchunk.hasBlocks.contains(rendertype)) {
            return false;
         } else {
            this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), chunkrenderdispatcher_compiledchunk);
            chunkrenderdispatcher.schedule(this.lastResortTransparencyTask);
            return true;
         }
      }

      protected boolean cancelTasks() {
         boolean flag = false;
         if (this.lastRebuildTask != null) {
            this.lastRebuildTask.cancel();
            this.lastRebuildTask = null;
            flag = true;
         }

         if (this.lastResortTransparencyTask != null) {
            this.lastResortTransparencyTask.cancel();
            this.lastResortTransparencyTask = null;
         }

         return flag;
      }

      public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask(RenderRegionCache renderregioncache) {
         boolean flag = this.cancelTasks();
         BlockPos blockpos = this.origin.immutable();
         int i = 1;
         RenderChunkRegion renderchunkregion = renderregioncache.createRegion(ChunkRenderDispatcher.this.level, blockpos.offset(-1, -1, -1), blockpos.offset(16, 16, 16), 1);
         boolean flag1 = this.compiled.get() == ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
         if (flag1 && flag) {
            this.initialCompilationCancelCount.incrementAndGet();
         }

         this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(this.getDistToPlayerSqr(), renderchunkregion, !flag1 || this.initialCompilationCancelCount.get() > 2);
         return this.lastRebuildTask;
      }

      public void rebuildChunkAsync(ChunkRenderDispatcher chunkrenderdispatcher, RenderRegionCache renderregioncache) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask = this.createCompileTask(renderregioncache);
         chunkrenderdispatcher.schedule(chunkrenderdispatcher_renderchunk_chunkcompiletask);
      }

      void updateGlobalBlockEntities(Collection<BlockEntity> collection) {
         Set<BlockEntity> set = Sets.newHashSet(collection);
         Set<BlockEntity> set1;
         synchronized(this.globalBlockEntities) {
            set1 = Sets.newHashSet(this.globalBlockEntities);
            set.removeAll(this.globalBlockEntities);
            set1.removeAll(collection);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(collection);
         }

         ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(set1, set);
      }

      public void compileSync(RenderRegionCache renderregioncache) {
         ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask = this.createCompileTask(renderregioncache);
         chunkrenderdispatcher_renderchunk_chunkcompiletask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
      }

      abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
         protected final double distAtCreation;
         protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
         protected final boolean isHighPriority;

         public ChunkCompileTask(double d0, boolean flag) {
            this.distAtCreation = d0;
            this.isHighPriority = flag;
         }

         public abstract CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkbufferbuilderpack);

         public abstract void cancel();

         protected abstract String name();

         public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher_renderchunk_chunkcompiletask) {
            return Doubles.compare(this.distAtCreation, chunkrenderdispatcher_renderchunk_chunkcompiletask.distAtCreation);
         }
      }

      class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         @Nullable
         protected RenderChunkRegion region;

         public RebuildTask(double d0, @Nullable RenderChunkRegion renderchunkregion, boolean flag) {
            super(d0, flag);
            this.region = renderchunkregion;
         }

         protected String name() {
            return "rend_chk_rebuild";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkbufferbuilderpack) {
            if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.hasAllNeighbors()) {
               this.region = null;
               RenderChunk.this.setDirty(false);
               this.isCancelled.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher_renderchunk_rebuildtask_compileresults = this.compile(f, f1, f2, chunkbufferbuilderpack);
               RenderChunk.this.updateGlobalBlockEntities(chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.globalBlockEntities);
               if (this.isCancelled.get()) {
                  chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               } else {
                  ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher_compiledchunk = new ChunkRenderDispatcher.CompiledChunk();
                  chunkrenderdispatcher_compiledchunk.visibilitySet = chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.visibilitySet;
                  chunkrenderdispatcher_compiledchunk.renderableBlockEntities.addAll(chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.blockEntities);
                  chunkrenderdispatcher_compiledchunk.transparencyState = chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.transparencyState;
                  List<CompletableFuture<Void>> list = Lists.newArrayList();
                  chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.renderedLayers.forEach((rendertype, bufferbuilder_renderedbuffer) -> {
                     list.add(ChunkRenderDispatcher.this.uploadChunkLayer(bufferbuilder_renderedbuffer, RenderChunk.this.getBuffer(rendertype)));
                     chunkrenderdispatcher_compiledchunk.hasBlocks.add(rendertype);
                  });
                  return Util.sequenceFailFast(list).handle((list1, throwable) -> {
                     if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                     }

                     if (this.isCancelled.get()) {
                        return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                     } else {
                        RenderChunk.this.compiled.set(chunkrenderdispatcher_compiledchunk);
                        RenderChunk.this.initialCompilationCancelCount.set(0);
                        ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                        return ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     }
                  });
               }
            }
         }

         private ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults compile(float f, float f1, float f2, ChunkBufferBuilderPack chunkbufferbuilderpack) {
            ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher_renderchunk_rebuildtask_compileresults = new ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults();
            int i = 1;
            BlockPos blockpos = RenderChunk.this.origin.immutable();
            BlockPos blockpos1 = blockpos.offset(15, 15, 15);
            VisGraph visgraph = new VisGraph();
            RenderChunkRegion renderchunkregion = this.region;
            this.region = null;
            PoseStack posestack = new PoseStack();
            if (renderchunkregion != null) {
               ModelBlockRenderer.enableCaching();
               Set<RenderType> set = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
               RandomSource randomsource = RandomSource.create();
               BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();

               for(BlockPos blockpos2 : BlockPos.betweenClosed(blockpos, blockpos1)) {
                  BlockState blockstate = renderchunkregion.getBlockState(blockpos2);
                  if (blockstate.isSolidRender(renderchunkregion, blockpos2)) {
                     visgraph.setOpaque(blockpos2);
                  }

                  if (blockstate.hasBlockEntity()) {
                     BlockEntity blockentity = renderchunkregion.getBlockEntity(blockpos2);
                     if (blockentity != null) {
                        this.handleBlockEntity(chunkrenderdispatcher_renderchunk_rebuildtask_compileresults, blockentity);
                     }
                  }

                  BlockState blockstate1 = renderchunkregion.getBlockState(blockpos2);
                  FluidState fluidstate = blockstate1.getFluidState();
                  if (!fluidstate.isEmpty()) {
                     RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                     BufferBuilder bufferbuilder = chunkbufferbuilderpack.builder(rendertype);
                     if (set.add(rendertype)) {
                        RenderChunk.this.beginLayer(bufferbuilder);
                     }

                     blockrenderdispatcher.renderLiquid(blockpos2, renderchunkregion, bufferbuilder, blockstate1, fluidstate);
                  }

                  if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                     RenderType rendertype1 = ItemBlockRenderTypes.getChunkRenderType(blockstate);
                     BufferBuilder bufferbuilder1 = chunkbufferbuilderpack.builder(rendertype1);
                     if (set.add(rendertype1)) {
                        RenderChunk.this.beginLayer(bufferbuilder1);
                     }

                     posestack.pushPose();
                     posestack.translate((float)(blockpos2.getX() & 15), (float)(blockpos2.getY() & 15), (float)(blockpos2.getZ() & 15));
                     blockrenderdispatcher.renderBatched(blockstate, blockpos2, renderchunkregion, posestack, bufferbuilder1, true, randomsource);
                     posestack.popPose();
                  }
               }

               if (set.contains(RenderType.translucent())) {
                  BufferBuilder bufferbuilder2 = chunkbufferbuilderpack.builder(RenderType.translucent());
                  if (!bufferbuilder2.isCurrentBatchEmpty()) {
                     bufferbuilder2.setQuadSorting(VertexSorting.byDistance(f - (float)blockpos.getX(), f1 - (float)blockpos.getY(), f2 - (float)blockpos.getZ()));
                     chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.transparencyState = bufferbuilder2.getSortState();
                  }
               }

               for(RenderType rendertype2 : set) {
                  BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = chunkbufferbuilderpack.builder(rendertype2).endOrDiscardIfEmpty();
                  if (bufferbuilder_renderedbuffer != null) {
                     chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.renderedLayers.put(rendertype2, bufferbuilder_renderedbuffer);
                  }
               }

               ModelBlockRenderer.clearCache();
            }

            chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.visibilitySet = visgraph.resolve();
            return chunkrenderdispatcher_renderchunk_rebuildtask_compileresults;
         }

         private <E extends BlockEntity> void handleBlockEntity(ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults chunkrenderdispatcher_renderchunk_rebuildtask_compileresults, E blockentity) {
            BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockentity);
            if (blockentityrenderer != null) {
               chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.blockEntities.add(blockentity);
               if (blockentityrenderer.shouldRenderOffScreen(blockentity)) {
                  chunkrenderdispatcher_renderchunk_rebuildtask_compileresults.globalBlockEntities.add(blockentity);
               }
            }

         }

         public void cancel() {
            this.region = null;
            if (this.isCancelled.compareAndSet(false, true)) {
               RenderChunk.this.setDirty(false);
            }

         }

         static final class CompileResults {
            public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
            public final List<BlockEntity> blockEntities = new ArrayList<>();
            public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
            public VisibilitySet visibilitySet = new VisibilitySet();
            @Nullable
            public BufferBuilder.SortState transparencyState;
         }
      }

      class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
         private final ChunkRenderDispatcher.CompiledChunk compiledChunk;

         public ResortTransparencyTask(double d0, ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher_compiledchunk) {
            super(d0, true);
            this.compiledChunk = chunkrenderdispatcher_compiledchunk;
         }

         protected String name() {
            return "rend_chk_sort";
         }

         public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack chunkbufferbuilderpack) {
            if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (!RenderChunk.this.hasAllNeighbors()) {
               this.isCancelled.set(true);
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else if (this.isCancelled.get()) {
               return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
            } else {
               Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
               float f = (float)vec3.x;
               float f1 = (float)vec3.y;
               float f2 = (float)vec3.z;
               BufferBuilder.SortState bufferbuilder_sortstate = this.compiledChunk.transparencyState;
               if (bufferbuilder_sortstate != null && !this.compiledChunk.isEmpty(RenderType.translucent())) {
                  BufferBuilder bufferbuilder = chunkbufferbuilderpack.builder(RenderType.translucent());
                  RenderChunk.this.beginLayer(bufferbuilder);
                  bufferbuilder.restoreSortState(bufferbuilder_sortstate);
                  bufferbuilder.setQuadSorting(VertexSorting.byDistance(f - (float)RenderChunk.this.origin.getX(), f1 - (float)RenderChunk.this.origin.getY(), f2 - (float)RenderChunk.this.origin.getZ()));
                  this.compiledChunk.transparencyState = bufferbuilder.getSortState();
                  BufferBuilder.RenderedBuffer bufferbuilder_renderedbuffer = bufferbuilder.end();
                  if (this.isCancelled.get()) {
                     bufferbuilder_renderedbuffer.release();
                     return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                  } else {
                     CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> completablefuture = ChunkRenderDispatcher.this.uploadChunkLayer(bufferbuilder_renderedbuffer, RenderChunk.this.getBuffer(RenderType.translucent())).thenApply((ovoid) -> ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                     return completablefuture.handle((chunkrenderdispatcher_chunktaskresult, throwable) -> {
                        if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
                           Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwable, "Rendering chunk"));
                        }

                        return this.isCancelled.get() ? ChunkRenderDispatcher.ChunkTaskResult.CANCELLED : ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                     });
                  }
               } else {
                  return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
               }
            }
         }

         public void cancel() {
            this.isCancelled.set(true);
         }
      }
   }
}
