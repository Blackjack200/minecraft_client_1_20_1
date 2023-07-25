package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

public class PoiManager extends SectionStorage<PoiSection> {
   public static final int MAX_VILLAGE_DISTANCE = 6;
   public static final int VILLAGE_SECTION_SIZE = 1;
   private final PoiManager.DistanceTracker distanceTracker;
   private final LongSet loadedChunks = new LongOpenHashSet();

   public PoiManager(Path path, DataFixer datafixer, boolean flag, RegistryAccess registryaccess, LevelHeightAccessor levelheightaccessor) {
      super(path, PoiSection::codec, PoiSection::new, datafixer, DataFixTypes.POI_CHUNK, flag, registryaccess, levelheightaccessor);
      this.distanceTracker = new PoiManager.DistanceTracker();
   }

   public void add(BlockPos blockpos, Holder<PoiType> holder) {
      this.getOrCreate(SectionPos.asLong(blockpos)).add(blockpos, holder);
   }

   public void remove(BlockPos blockpos) {
      this.getOrLoad(SectionPos.asLong(blockpos)).ifPresent((poisection) -> poisection.remove(blockpos));
   }

   public long getCountInRange(Predicate<Holder<PoiType>> predicate, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).count();
   }

   public boolean existsAtPosition(ResourceKey<PoiType> resourcekey, BlockPos blockpos) {
      return this.exists(blockpos, (holder) -> holder.is(resourcekey));
   }

   public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> predicate, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      int j = Math.floorDiv(i, 16) + 1;
      return ChunkPos.rangeClosed(new ChunkPos(blockpos), j).flatMap((chunkpos) -> this.getInChunk(predicate, chunkpos, poimanager_occupancy)).filter((poirecord) -> {
         BlockPos blockpos2 = poirecord.getPos();
         return Math.abs(blockpos2.getX() - blockpos.getX()) <= i && Math.abs(blockpos2.getZ() - blockpos.getZ()) <= i;
      });
   }

   public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> predicate, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      int j = i * i;
      return this.getInSquare(predicate, blockpos, i, poimanager_occupancy).filter((poirecord) -> poirecord.getPos().distSqr(blockpos) <= (double)j);
   }

   @VisibleForDebug
   public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> predicate, ChunkPos chunkpos, PoiManager.Occupancy poimanager_occupancy) {
      return IntStream.range(this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).boxed().map((integer) -> this.getOrLoad(SectionPos.of(chunkpos, integer).asLong())).filter(Optional::isPresent).flatMap((optional) -> optional.get().getRecords(predicate, poimanager_occupancy));
   }

   public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).map(PoiRecord::getPos).filter(predicate1);
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).filter((poirecord1) -> predicate1.test(poirecord1.getPos())).map((poirecord) -> Pair.of(poirecord.getPoiType(), poirecord.getPos()));
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.findAllWithType(predicate, predicate1, blockpos, i, poimanager_occupancy).sorted(Comparator.comparingDouble((pair) -> pair.getSecond().distSqr(blockpos)));
   }

   public Optional<BlockPos> find(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.findAll(predicate, predicate1, blockpos, i, poimanager_occupancy).findFirst();
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).map(PoiRecord::getPos).min(Comparator.comparingDouble((blockpos2) -> blockpos2.distSqr(blockpos)));
   }

   public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> predicate, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).min(Comparator.comparingDouble((poirecord1) -> poirecord1.getPos().distSqr(blockpos))).map((poirecord) -> Pair.of(poirecord.getPoiType(), poirecord.getPos()));
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, BlockPos blockpos, int i, PoiManager.Occupancy poimanager_occupancy) {
      return this.getInRange(predicate, blockpos, i, poimanager_occupancy).map(PoiRecord::getPos).filter(predicate1).min(Comparator.comparingDouble((blockpos2) -> blockpos2.distSqr(blockpos)));
   }

   public Optional<BlockPos> take(Predicate<Holder<PoiType>> predicate, BiPredicate<Holder<PoiType>, BlockPos> bipredicate, BlockPos blockpos, int i) {
      return this.getInRange(predicate, blockpos, i, PoiManager.Occupancy.HAS_SPACE).filter((poirecord1) -> bipredicate.test(poirecord1.getPoiType(), poirecord1.getPos())).findFirst().map((poirecord) -> {
         poirecord.acquireTicket();
         return poirecord.getPos();
      });
   }

   public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate1, PoiManager.Occupancy poimanager_occupancy, BlockPos blockpos, int i, RandomSource randomsource) {
      List<PoiRecord> list = Util.toShuffledList(this.getInRange(predicate, blockpos, i, poimanager_occupancy), randomsource);
      return list.stream().filter((poirecord) -> predicate1.test(poirecord.getPos())).findFirst().map(PoiRecord::getPos);
   }

   public boolean release(BlockPos blockpos) {
      return this.getOrLoad(SectionPos.asLong(blockpos)).map((poisection) -> poisection.release(blockpos)).orElseThrow(() -> Util.pauseInIde(new IllegalStateException("POI never registered at " + blockpos)));
   }

   public boolean exists(BlockPos blockpos, Predicate<Holder<PoiType>> predicate) {
      return this.getOrLoad(SectionPos.asLong(blockpos)).map((poisection) -> poisection.exists(blockpos, predicate)).orElse(false);
   }

   public Optional<Holder<PoiType>> getType(BlockPos blockpos) {
      return this.getOrLoad(SectionPos.asLong(blockpos)).flatMap((poisection) -> poisection.getType(blockpos));
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public int getFreeTickets(BlockPos blockpos) {
      return this.getOrLoad(SectionPos.asLong(blockpos)).map((poisection) -> poisection.getFreeTickets(blockpos)).orElse(0);
   }

   public int sectionsToVillage(SectionPos sectionpos) {
      this.distanceTracker.runAllUpdates();
      return this.distanceTracker.getLevel(sectionpos.asLong());
   }

   boolean isVillageCenter(long i) {
      Optional<PoiSection> optional = this.get(i);
      return optional == null ? false : optional.map((poisection) -> poisection.getRecords((holder) -> holder.is(PoiTypeTags.VILLAGE), PoiManager.Occupancy.IS_OCCUPIED).findAny().isPresent()).orElse(false);
   }

   public void tick(BooleanSupplier booleansupplier) {
      super.tick(booleansupplier);
      this.distanceTracker.runAllUpdates();
   }

   protected void setDirty(long i) {
      super.setDirty(i);
      this.distanceTracker.update(i, this.distanceTracker.getLevelFromSource(i), false);
   }

   protected void onSectionLoad(long i) {
      this.distanceTracker.update(i, this.distanceTracker.getLevelFromSource(i), false);
   }

   public void checkConsistencyWithBlocks(SectionPos sectionpos, LevelChunkSection levelchunksection) {
      Util.ifElse(this.getOrLoad(sectionpos.asLong()), (poisection1) -> poisection1.refresh((biconsumer) -> {
            if (mayHavePoi(levelchunksection)) {
               this.updateFromSection(levelchunksection, sectionpos, biconsumer);
            }

         }), () -> {
         if (mayHavePoi(levelchunksection)) {
            PoiSection poisection = this.getOrCreate(sectionpos.asLong());
            this.updateFromSection(levelchunksection, sectionpos, poisection::add);
         }

      });
   }

   private static boolean mayHavePoi(LevelChunkSection levelchunksection) {
      return levelchunksection.maybeHas(PoiTypes::hasPoi);
   }

   private void updateFromSection(LevelChunkSection levelchunksection, SectionPos sectionpos, BiConsumer<BlockPos, Holder<PoiType>> biconsumer) {
      sectionpos.blocksInside().forEach((blockpos) -> {
         BlockState blockstate = levelchunksection.getBlockState(SectionPos.sectionRelative(blockpos.getX()), SectionPos.sectionRelative(blockpos.getY()), SectionPos.sectionRelative(blockpos.getZ()));
         PoiTypes.forState(blockstate).ifPresent((holder) -> biconsumer.accept(blockpos, holder));
      });
   }

   public void ensureLoadedAndValid(LevelReader levelreader, BlockPos blockpos, int i) {
      SectionPos.aroundChunk(new ChunkPos(blockpos), Math.floorDiv(i, 16), this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).map((sectionpos) -> Pair.of(sectionpos, this.getOrLoad(sectionpos.asLong()))).filter((pair1) -> !pair1.getSecond().map(PoiSection::isValid).orElse(false)).map((pair) -> pair.getFirst().chunk()).filter((chunkpos1) -> this.loadedChunks.add(chunkpos1.toLong())).forEach((chunkpos) -> levelreader.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.EMPTY));
   }

   final class DistanceTracker extends SectionTracker {
      private final Long2ByteMap levels = new Long2ByteOpenHashMap();

      protected DistanceTracker() {
         super(7, 16, 256);
         this.levels.defaultReturnValue((byte)7);
      }

      protected int getLevelFromSource(long i) {
         return PoiManager.this.isVillageCenter(i) ? 0 : 7;
      }

      protected int getLevel(long i) {
         return this.levels.get(i);
      }

      protected void setLevel(long i, int j) {
         if (j > 6) {
            this.levels.remove(i);
         } else {
            this.levels.put(i, (byte)j);
         }

      }

      public void runAllUpdates() {
         super.runUpdates(Integer.MAX_VALUE);
      }
   }

   public static enum Occupancy {
      HAS_SPACE(PoiRecord::hasSpace),
      IS_OCCUPIED(PoiRecord::isOccupied),
      ANY((poirecord) -> true);

      private final Predicate<? super PoiRecord> test;

      private Occupancy(Predicate<? super PoiRecord> predicate) {
         this.test = predicate;
      }

      public Predicate<? super PoiRecord> getTest() {
         return this.test;
      }
   }
}
