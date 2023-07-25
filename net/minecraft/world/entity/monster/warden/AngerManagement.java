package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AngerManagement {
   @VisibleForTesting
   protected static final int CONVERSION_DELAY = 2;
   @VisibleForTesting
   protected static final int MAX_ANGER = 150;
   private static final int DEFAULT_ANGER_DECREASE = 1;
   private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
   int highestAnger;
   private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(UUIDUtil.CODEC.fieldOf("uuid").forGetter(Pair::getFirst), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)).apply(recordcodecbuilder_instance, Pair::of));
   private final Predicate<Entity> filter;
   @VisibleForTesting
   protected final ArrayList<Entity> suspects;
   private final AngerManagement.Sorter suspectSorter;
   @VisibleForTesting
   protected final Object2IntMap<Entity> angerBySuspect;
   @VisibleForTesting
   protected final Object2IntMap<UUID> angerByUuid;

   public static Codec<AngerManagement> codec(Predicate<Entity> predicate) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs)).apply(recordcodecbuilder_instance, (list) -> new AngerManagement(predicate, list)));
   }

   public AngerManagement(Predicate<Entity> predicate, List<Pair<UUID, Integer>> list) {
      this.filter = predicate;
      this.suspects = new ArrayList<>();
      this.suspectSorter = new AngerManagement.Sorter(this);
      this.angerBySuspect = new Object2IntOpenHashMap<>();
      this.angerByUuid = new Object2IntOpenHashMap<>(list.size());
      list.forEach((pair) -> this.angerByUuid.put(pair.getFirst(), pair.getSecond()));
   }

   private List<Pair<UUID, Integer>> createUuidAngerPairs() {
      return Streams.<Pair<UUID, Integer>>concat(this.suspects.stream().map((entity) -> Pair.of(entity.getUUID(), this.angerBySuspect.getInt(entity))), this.angerByUuid.object2IntEntrySet().stream().map((object2intmap_entry) -> Pair.of(object2intmap_entry.getKey(), object2intmap_entry.getIntValue()))).collect(Collectors.toList());
   }

   public void tick(ServerLevel serverlevel, Predicate<Entity> predicate) {
      --this.conversionDelay;
      if (this.conversionDelay <= 0) {
         this.convertFromUuids(serverlevel);
         this.conversionDelay = 2;
      }

      ObjectIterator<Object2IntMap.Entry<UUID>> objectiterator = this.angerByUuid.object2IntEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Object2IntMap.Entry<UUID> object2intmap_entry = objectiterator.next();
         int i = object2intmap_entry.getIntValue();
         if (i <= 1) {
            objectiterator.remove();
         } else {
            object2intmap_entry.setValue(i - 1);
         }
      }

      ObjectIterator<Object2IntMap.Entry<Entity>> objectiterator1 = this.angerBySuspect.object2IntEntrySet().iterator();

      while(objectiterator1.hasNext()) {
         Object2IntMap.Entry<Entity> object2intmap_entry1 = objectiterator1.next();
         int j = object2intmap_entry1.getIntValue();
         Entity entity = object2intmap_entry1.getKey();
         Entity.RemovalReason entity_removalreason = entity.getRemovalReason();
         if (j > 1 && predicate.test(entity) && entity_removalreason == null) {
            object2intmap_entry1.setValue(j - 1);
         } else {
            this.suspects.remove(entity);
            objectiterator1.remove();
            if (j > 1 && entity_removalreason != null) {
               switch (entity_removalreason) {
                  case CHANGED_DIMENSION:
                  case UNLOADED_TO_CHUNK:
                  case UNLOADED_WITH_PLAYER:
                     this.angerByUuid.put(entity.getUUID(), j - 1);
               }
            }
         }
      }

      this.sortAndUpdateHighestAnger();
   }

   private void sortAndUpdateHighestAnger() {
      this.highestAnger = 0;
      this.suspects.sort(this.suspectSorter);
      if (this.suspects.size() == 1) {
         this.highestAnger = this.angerBySuspect.getInt(this.suspects.get(0));
      }

   }

   private void convertFromUuids(ServerLevel serverlevel) {
      ObjectIterator<Object2IntMap.Entry<UUID>> objectiterator = this.angerByUuid.object2IntEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Object2IntMap.Entry<UUID> object2intmap_entry = objectiterator.next();
         int i = object2intmap_entry.getIntValue();
         Entity entity = serverlevel.getEntity(object2intmap_entry.getKey());
         if (entity != null) {
            this.angerBySuspect.put(entity, i);
            this.suspects.add(entity);
            objectiterator.remove();
         }
      }

   }

   public int increaseAnger(Entity entity, int i) {
      boolean flag = !this.angerBySuspect.containsKey(entity);
      int j = this.angerBySuspect.computeInt(entity, (entity1, integer) -> Math.min(150, (integer == null ? 0 : integer) + i));
      if (flag) {
         int k = this.angerByUuid.removeInt(entity.getUUID());
         j += k;
         this.angerBySuspect.put(entity, j);
         this.suspects.add(entity);
      }

      this.sortAndUpdateHighestAnger();
      return j;
   }

   public void clearAnger(Entity entity) {
      this.angerBySuspect.removeInt(entity);
      this.suspects.remove(entity);
      this.sortAndUpdateHighestAnger();
   }

   @Nullable
   private Entity getTopSuspect() {
      return this.suspects.stream().filter(this.filter).findFirst().orElse((Entity)null);
   }

   public int getActiveAnger(@Nullable Entity entity) {
      return entity == null ? this.highestAnger : this.angerBySuspect.getInt(entity);
   }

   public Optional<LivingEntity> getActiveEntity() {
      return Optional.ofNullable(this.getTopSuspect()).filter((entity1) -> entity1 instanceof LivingEntity).map((entity) -> (LivingEntity)entity);
   }

   @VisibleForTesting
   protected static record Sorter(AngerManagement angerManagement) implements Comparator<Entity> {
      public int compare(Entity entity, Entity entity1) {
         if (entity.equals(entity1)) {
            return 0;
         } else {
            int i = this.angerManagement.angerBySuspect.getOrDefault(entity, 0);
            int j = this.angerManagement.angerBySuspect.getOrDefault(entity1, 0);
            this.angerManagement.highestAnger = Math.max(this.angerManagement.highestAnger, Math.max(i, j));
            boolean flag = AngerLevel.byAnger(i).isAngry();
            boolean flag1 = AngerLevel.byAnger(j).isAngry();
            if (flag != flag1) {
               return flag ? -1 : 1;
            } else {
               boolean flag2 = entity instanceof Player;
               boolean flag3 = entity1 instanceof Player;
               if (flag2 != flag3) {
                  return flag2 ? -1 : 1;
               } else {
                  return Integer.compare(j, i);
               }
            }
         }
      }
   }
}
