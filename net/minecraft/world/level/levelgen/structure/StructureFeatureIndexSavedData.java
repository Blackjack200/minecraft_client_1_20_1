package net.minecraft.world.level.levelgen.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class StructureFeatureIndexSavedData extends SavedData {
   private static final String TAG_REMAINING_INDEXES = "Remaining";
   private static final String TAG_All_INDEXES = "All";
   private final LongSet all;
   private final LongSet remaining;

   private StructureFeatureIndexSavedData(LongSet longset, LongSet longset1) {
      this.all = longset;
      this.remaining = longset1;
   }

   public StructureFeatureIndexSavedData() {
      this(new LongOpenHashSet(), new LongOpenHashSet());
   }

   public static StructureFeatureIndexSavedData load(CompoundTag compoundtag) {
      return new StructureFeatureIndexSavedData(new LongOpenHashSet(compoundtag.getLongArray("All")), new LongOpenHashSet(compoundtag.getLongArray("Remaining")));
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putLongArray("All", this.all.toLongArray());
      compoundtag.putLongArray("Remaining", this.remaining.toLongArray());
      return compoundtag;
   }

   public void addIndex(long i) {
      this.all.add(i);
      this.remaining.add(i);
   }

   public boolean hasStartIndex(long i) {
      return this.all.contains(i);
   }

   public boolean hasUnhandledIndex(long i) {
      return this.remaining.contains(i);
   }

   public void removeIndex(long i) {
      this.remaining.remove(i);
   }

   public LongSet getAll() {
      return this.all;
   }
}
