package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

public class DynamicGameEventListener<T extends GameEventListener> {
   private final T listener;
   @Nullable
   private SectionPos lastSection;

   public DynamicGameEventListener(T gameeventlistener) {
      this.listener = gameeventlistener;
   }

   public void add(ServerLevel serverlevel) {
      this.move(serverlevel);
   }

   public T getListener() {
      return this.listener;
   }

   public void remove(ServerLevel serverlevel) {
      ifChunkExists(serverlevel, this.lastSection, (gameeventlistenerregistry) -> gameeventlistenerregistry.unregister(this.listener));
   }

   public void move(ServerLevel serverlevel) {
      this.listener.getListenerSource().getPosition(serverlevel).map(SectionPos::of).ifPresent((sectionpos) -> {
         if (this.lastSection == null || !this.lastSection.equals(sectionpos)) {
            ifChunkExists(serverlevel, this.lastSection, (gameeventlistenerregistry1) -> gameeventlistenerregistry1.unregister(this.listener));
            this.lastSection = sectionpos;
            ifChunkExists(serverlevel, this.lastSection, (gameeventlistenerregistry) -> gameeventlistenerregistry.register(this.listener));
         }

      });
   }

   private static void ifChunkExists(LevelReader levelreader, @Nullable SectionPos sectionpos, Consumer<GameEventListenerRegistry> consumer) {
      if (sectionpos != null) {
         ChunkAccess chunkaccess = levelreader.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.FULL, false);
         if (chunkaccess != null) {
            consumer.accept(chunkaccess.getListenerRegistry(sectionpos.y()));
         }

      }
   }
}
