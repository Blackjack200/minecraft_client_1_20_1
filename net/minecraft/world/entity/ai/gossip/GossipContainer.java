package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class GossipContainer {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int DISCARD_THRESHOLD = 2;
   private final Map<UUID, GossipContainer.EntityGossips> gossips = Maps.newHashMap();

   @VisibleForDebug
   public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
      Map<UUID, Object2IntMap<GossipType>> map = Maps.newHashMap();
      this.gossips.keySet().forEach((uuid) -> {
         GossipContainer.EntityGossips gossipcontainer_entitygossips = this.gossips.get(uuid);
         map.put(uuid, gossipcontainer_entitygossips.entries);
      });
      return map;
   }

   public void decay() {
      Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

      while(iterator.hasNext()) {
         GossipContainer.EntityGossips gossipcontainer_entitygossips = iterator.next();
         gossipcontainer_entitygossips.decay();
         if (gossipcontainer_entitygossips.isEmpty()) {
            iterator.remove();
         }
      }

   }

   private Stream<GossipContainer.GossipEntry> unpack() {
      return this.gossips.entrySet().stream().flatMap((map_entry) -> map_entry.getValue().unpack(map_entry.getKey()));
   }

   private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(RandomSource randomsource, int i) {
      List<GossipContainer.GossipEntry> list = this.unpack().toList();
      if (list.isEmpty()) {
         return Collections.emptyList();
      } else {
         int[] aint = new int[list.size()];
         int j = 0;

         for(int k = 0; k < list.size(); ++k) {
            GossipContainer.GossipEntry gossipcontainer_gossipentry = list.get(k);
            j += Math.abs(gossipcontainer_gossipentry.weightedValue());
            aint[k] = j - 1;
         }

         Set<GossipContainer.GossipEntry> set = Sets.newIdentityHashSet();

         for(int l = 0; l < i; ++l) {
            int i1 = randomsource.nextInt(j);
            int j1 = Arrays.binarySearch(aint, i1);
            set.add(list.get(j1 < 0 ? -j1 - 1 : j1));
         }

         return set;
      }
   }

   private GossipContainer.EntityGossips getOrCreate(UUID uuid) {
      return this.gossips.computeIfAbsent(uuid, (uuid1) -> new GossipContainer.EntityGossips());
   }

   public void transferFrom(GossipContainer gossipcontainer, RandomSource randomsource, int i) {
      Collection<GossipContainer.GossipEntry> collection = gossipcontainer.selectGossipsForTransfer(randomsource, i);
      collection.forEach((gossipcontainer_gossipentry) -> {
         int j = gossipcontainer_gossipentry.value - gossipcontainer_gossipentry.type.decayPerTransfer;
         if (j >= 2) {
            this.getOrCreate(gossipcontainer_gossipentry.target).entries.mergeInt(gossipcontainer_gossipentry.type, j, GossipContainer::mergeValuesForTransfer);
         }

      });
   }

   public int getReputation(UUID uuid, Predicate<GossipType> predicate) {
      GossipContainer.EntityGossips gossipcontainer_entitygossips = this.gossips.get(uuid);
      return gossipcontainer_entitygossips != null ? gossipcontainer_entitygossips.weightedValue(predicate) : 0;
   }

   public long getCountForType(GossipType gossiptype, DoublePredicate doublepredicate) {
      return this.gossips.values().stream().filter((gossipcontainer_entitygossips) -> doublepredicate.test((double)(gossipcontainer_entitygossips.entries.getOrDefault(gossiptype, 0) * gossiptype.weight))).count();
   }

   public void add(UUID uuid, GossipType gossiptype, int i) {
      GossipContainer.EntityGossips gossipcontainer_entitygossips = this.getOrCreate(uuid);
      gossipcontainer_entitygossips.entries.mergeInt(gossiptype, i, (j, k) -> this.mergeValuesForAddition(gossiptype, j, k));
      gossipcontainer_entitygossips.makeSureValueIsntTooLowOrTooHigh(gossiptype);
      if (gossipcontainer_entitygossips.isEmpty()) {
         this.gossips.remove(uuid);
      }

   }

   public void remove(UUID uuid, GossipType gossiptype, int i) {
      this.add(uuid, gossiptype, -i);
   }

   public void remove(UUID uuid, GossipType gossiptype) {
      GossipContainer.EntityGossips gossipcontainer_entitygossips = this.gossips.get(uuid);
      if (gossipcontainer_entitygossips != null) {
         gossipcontainer_entitygossips.remove(gossiptype);
         if (gossipcontainer_entitygossips.isEmpty()) {
            this.gossips.remove(uuid);
         }
      }

   }

   public void remove(GossipType gossiptype) {
      Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

      while(iterator.hasNext()) {
         GossipContainer.EntityGossips gossipcontainer_entitygossips = iterator.next();
         gossipcontainer_entitygossips.remove(gossiptype);
         if (gossipcontainer_entitygossips.isEmpty()) {
            iterator.remove();
         }
      }

   }

   public <T> T store(DynamicOps<T> dynamicops) {
      return GossipContainer.GossipEntry.LIST_CODEC.encodeStart(dynamicops, this.unpack().toList()).resultOrPartial((s) -> LOGGER.warn("Failed to serialize gossips: {}", (Object)s)).orElseGet(dynamicops::emptyList);
   }

   public void update(Dynamic<?> dynamic) {
      GossipContainer.GossipEntry.LIST_CODEC.decode(dynamic).resultOrPartial((s) -> LOGGER.warn("Failed to deserialize gossips: {}", (Object)s)).stream().flatMap((pair) -> pair.getFirst().stream()).forEach((gossipcontainer_gossipentry) -> this.getOrCreate(gossipcontainer_gossipentry.target).entries.put(gossipcontainer_gossipentry.type, gossipcontainer_gossipentry.value));
   }

   private static int mergeValuesForTransfer(int i, int j) {
      return Math.max(i, j);
   }

   private int mergeValuesForAddition(GossipType gossiptype, int i, int j) {
      int k = i + j;
      return k > gossiptype.max ? Math.max(gossiptype.max, i) : k;
   }

   static class EntityGossips {
      final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

      public int weightedValue(Predicate<GossipType> predicate) {
         return this.entries.object2IntEntrySet().stream().filter((object2intmap_entry1) -> predicate.test(object2intmap_entry1.getKey())).mapToInt((object2intmap_entry) -> object2intmap_entry.getIntValue() * (object2intmap_entry.getKey()).weight).sum();
      }

      public Stream<GossipContainer.GossipEntry> unpack(UUID uuid) {
         return this.entries.object2IntEntrySet().stream().map((object2intmap_entry) -> new GossipContainer.GossipEntry(uuid, object2intmap_entry.getKey(), object2intmap_entry.getIntValue()));
      }

      public void decay() {
         ObjectIterator<Object2IntMap.Entry<GossipType>> objectiterator = this.entries.object2IntEntrySet().iterator();

         while(objectiterator.hasNext()) {
            Object2IntMap.Entry<GossipType> object2intmap_entry = objectiterator.next();
            int i = object2intmap_entry.getIntValue() - (object2intmap_entry.getKey()).decayPerDay;
            if (i < 2) {
               objectiterator.remove();
            } else {
               object2intmap_entry.setValue(i);
            }
         }

      }

      public boolean isEmpty() {
         return this.entries.isEmpty();
      }

      public void makeSureValueIsntTooLowOrTooHigh(GossipType gossiptype) {
         int i = this.entries.getInt(gossiptype);
         if (i > gossiptype.max) {
            this.entries.put(gossiptype, gossiptype.max);
         }

         if (i < 2) {
            this.remove(gossiptype);
         }

      }

      public void remove(GossipType gossiptype) {
         this.entries.removeInt(gossiptype);
      }
   }

   static record GossipEntry(UUID target, GossipType type, int value) {
      final UUID target;
      final GossipType type;
      final int value;
      public static final Codec<GossipContainer.GossipEntry> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipContainer.GossipEntry::target), GossipType.CODEC.fieldOf("Type").forGetter(GossipContainer.GossipEntry::type), ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipContainer.GossipEntry::value)).apply(recordcodecbuilder_instance, GossipContainer.GossipEntry::new));
      public static final Codec<List<GossipContainer.GossipEntry>> LIST_CODEC = CODEC.listOf();

      public int weightedValue() {
         return this.value * this.type.weight;
      }
   }
}
