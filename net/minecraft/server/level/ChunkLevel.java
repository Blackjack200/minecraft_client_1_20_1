package net.minecraft.server.level;

import net.minecraft.world.level.chunk.ChunkStatus;

public class ChunkLevel {
   private static final int FULL_CHUNK_LEVEL = 33;
   private static final int BLOCK_TICKING_LEVEL = 32;
   private static final int ENTITY_TICKING_LEVEL = 31;
   public static final int MAX_LEVEL = 33 + ChunkStatus.maxDistance();

   public static ChunkStatus generationStatus(int i) {
      return i < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(i - 33);
   }

   public static int byStatus(ChunkStatus chunkstatus) {
      return 33 + ChunkStatus.getDistance(chunkstatus);
   }

   public static FullChunkStatus fullStatus(int i) {
      if (i <= 31) {
         return FullChunkStatus.ENTITY_TICKING;
      } else if (i <= 32) {
         return FullChunkStatus.BLOCK_TICKING;
      } else {
         return i <= 33 ? FullChunkStatus.FULL : FullChunkStatus.INACCESSIBLE;
      }
   }

   public static int byStatus(FullChunkStatus fullchunkstatus) {
      int var10000;
      switch (fullchunkstatus) {
         case INACCESSIBLE:
            var10000 = MAX_LEVEL;
            break;
         case FULL:
            var10000 = 33;
            break;
         case BLOCK_TICKING:
            var10000 = 32;
            break;
         case ENTITY_TICKING:
            var10000 = 31;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static boolean isEntityTicking(int i) {
      return i <= 31;
   }

   public static boolean isBlockTicking(int i) {
      return i <= 32;
   }

   public static boolean isLoaded(int i) {
      return i <= MAX_LEVEL;
   }
}
