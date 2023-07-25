package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class PoiSection {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
   private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.newHashMap();
   private final Runnable setDirty;
   private boolean isValid;

   public static Codec<PoiSection> codec(Runnable runnable) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RecordCodecBuilder.point(runnable), Codec.BOOL.optionalFieldOf("Valid", Boolean.valueOf(false)).forGetter((poisection1) -> poisection1.isValid), PoiRecord.codec(runnable).listOf().fieldOf("Records").forGetter((poisection) -> ImmutableList.copyOf(poisection.records.values()))).apply(recordcodecbuilder_instance, PoiSection::new)).orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> new PoiSection(runnable, false, ImmutableList.of()));
   }

   public PoiSection(Runnable runnable) {
      this(runnable, true, ImmutableList.of());
   }

   private PoiSection(Runnable runnable, boolean flag, List<PoiRecord> list) {
      this.setDirty = runnable;
      this.isValid = flag;
      list.forEach(this::add);
   }

   public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> predicate, PoiManager.Occupancy poimanager_occupancy) {
      return this.byType.entrySet().stream().filter((map_entry1) -> predicate.test(map_entry1.getKey())).flatMap((map_entry) -> map_entry.getValue().stream()).filter(poimanager_occupancy.getTest());
   }

   public void add(BlockPos blockpos, Holder<PoiType> holder) {
      if (this.add(new PoiRecord(blockpos, holder, this.setDirty))) {
         LOGGER.debug("Added POI of type {} @ {}", holder.unwrapKey().map((resourcekey) -> resourcekey.location().toString()).orElse("[unregistered]"), blockpos);
         this.setDirty.run();
      }

   }

   private boolean add(PoiRecord poirecord) {
      BlockPos blockpos = poirecord.getPos();
      Holder<PoiType> holder = poirecord.getPoiType();
      short short0 = SectionPos.sectionRelativePos(blockpos);
      PoiRecord poirecord1 = this.records.get(short0);
      if (poirecord1 != null) {
         if (holder.equals(poirecord1.getPoiType())) {
            return false;
         }

         Util.logAndPauseIfInIde("POI data mismatch: already registered at " + blockpos);
      }

      this.records.put(short0, poirecord);
      this.byType.computeIfAbsent(holder, (holder1) -> Sets.newHashSet()).add(poirecord);
      return true;
   }

   public void remove(BlockPos blockpos) {
      PoiRecord poirecord = this.records.remove(SectionPos.sectionRelativePos(blockpos));
      if (poirecord == null) {
         LOGGER.error("POI data mismatch: never registered at {}", (Object)blockpos);
      } else {
         this.byType.get(poirecord.getPoiType()).remove(poirecord);
         LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(poirecord::getPoiType), LogUtils.defer(poirecord::getPos));
         this.setDirty.run();
      }
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public int getFreeTickets(BlockPos blockpos) {
      return this.getPoiRecord(blockpos).map(PoiRecord::getFreeTickets).orElse(0);
   }

   public boolean release(BlockPos blockpos) {
      PoiRecord poirecord = this.records.get(SectionPos.sectionRelativePos(blockpos));
      if (poirecord == null) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + blockpos));
      } else {
         boolean flag = poirecord.releaseTicket();
         this.setDirty.run();
         return flag;
      }
   }

   public boolean exists(BlockPos blockpos, Predicate<Holder<PoiType>> predicate) {
      return this.getType(blockpos).filter(predicate).isPresent();
   }

   public Optional<Holder<PoiType>> getType(BlockPos blockpos) {
      return this.getPoiRecord(blockpos).map(PoiRecord::getPoiType);
   }

   private Optional<PoiRecord> getPoiRecord(BlockPos blockpos) {
      return Optional.ofNullable(this.records.get(SectionPos.sectionRelativePos(blockpos)));
   }

   public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> consumer) {
      if (!this.isValid) {
         Short2ObjectMap<PoiRecord> short2objectmap = new Short2ObjectOpenHashMap<>(this.records);
         this.clear();
         consumer.accept((blockpos, holder) -> {
            short short0 = SectionPos.sectionRelativePos(blockpos);
            PoiRecord poirecord = short2objectmap.computeIfAbsent(short0, (short1) -> new PoiRecord(blockpos, holder, this.setDirty));
            this.add(poirecord);
         });
         this.isValid = true;
         this.setDirty.run();
      }

   }

   private void clear() {
      this.records.clear();
      this.byType.clear();
   }

   boolean isValid() {
      return this.isValid;
   }
}
