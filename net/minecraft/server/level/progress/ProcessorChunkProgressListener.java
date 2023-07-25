package net.minecraft.server.level.progress;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class ProcessorChunkProgressListener implements ChunkProgressListener {
   private final ChunkProgressListener delegate;
   private final ProcessorMailbox<Runnable> mailbox;

   private ProcessorChunkProgressListener(ChunkProgressListener chunkprogresslistener, Executor executor) {
      this.delegate = chunkprogresslistener;
      this.mailbox = ProcessorMailbox.create(executor, "progressListener");
   }

   public static ProcessorChunkProgressListener createStarted(ChunkProgressListener chunkprogresslistener, Executor executor) {
      ProcessorChunkProgressListener processorchunkprogresslistener = new ProcessorChunkProgressListener(chunkprogresslistener, executor);
      processorchunkprogresslistener.start();
      return processorchunkprogresslistener;
   }

   public void updateSpawnPos(ChunkPos chunkpos) {
      this.mailbox.tell(() -> this.delegate.updateSpawnPos(chunkpos));
   }

   public void onStatusChange(ChunkPos chunkpos, @Nullable ChunkStatus chunkstatus) {
      this.mailbox.tell(() -> this.delegate.onStatusChange(chunkpos, chunkstatus));
   }

   public void start() {
      this.mailbox.tell(this.delegate::start);
   }

   public void stop() {
      this.mailbox.tell(this.delegate::stop);
   }
}
