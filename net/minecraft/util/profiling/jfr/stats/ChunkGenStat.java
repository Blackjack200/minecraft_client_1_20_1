package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public record ChunkGenStat(Duration duration, ChunkPos chunkPos, ColumnPos worldPos, ChunkStatus status, String level) implements TimedStat {
   public static ChunkGenStat from(RecordedEvent recordedevent) {
      return new ChunkGenStat(recordedevent.getDuration(), new ChunkPos(recordedevent.getInt("chunkPosX"), recordedevent.getInt("chunkPosX")), new ColumnPos(recordedevent.getInt("worldPosX"), recordedevent.getInt("worldPosZ")), ChunkStatus.byName(recordedevent.getString("status")), recordedevent.getString("level"));
   }
}
