package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData extends SavedData {
   public static final String FILE_ID = "chunks";
   private static final String TAG_FORCED = "Forced";
   private final LongSet chunks;

   private ForcedChunksSavedData(LongSet longset) {
      this.chunks = longset;
   }

   public ForcedChunksSavedData() {
      this(new LongOpenHashSet());
   }

   public static ForcedChunksSavedData load(CompoundTag compoundtag) {
      return new ForcedChunksSavedData(new LongOpenHashSet(compoundtag.getLongArray("Forced")));
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putLongArray("Forced", this.chunks.toLongArray());
      return compoundtag;
   }

   public LongSet getChunks() {
      return this.chunks;
   }
}
