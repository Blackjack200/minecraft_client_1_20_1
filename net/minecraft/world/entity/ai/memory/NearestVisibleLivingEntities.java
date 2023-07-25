package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class NearestVisibleLivingEntities {
   private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
   private final List<LivingEntity> nearbyEntities;
   private final Predicate<LivingEntity> lineOfSightTest;

   private NearestVisibleLivingEntities() {
      this.nearbyEntities = List.of();
      this.lineOfSightTest = (livingentity) -> false;
   }

   public NearestVisibleLivingEntities(LivingEntity livingentity, List<LivingEntity> list) {
      this.nearbyEntities = list;
      Object2BooleanOpenHashMap<LivingEntity> object2booleanopenhashmap = new Object2BooleanOpenHashMap<>(list.size());
      Predicate<LivingEntity> predicate = (livingentity3) -> Sensor.isEntityTargetable(livingentity, livingentity3);
      this.lineOfSightTest = (livingentity1) -> object2booleanopenhashmap.computeIfAbsent(livingentity1, predicate);
   }

   public static NearestVisibleLivingEntities empty() {
      return EMPTY;
   }

   public Optional<LivingEntity> findClosest(Predicate<LivingEntity> predicate) {
      for(LivingEntity livingentity : this.nearbyEntities) {
         if (predicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
            return Optional.of(livingentity);
         }
      }

      return Optional.empty();
   }

   public Iterable<LivingEntity> findAll(Predicate<LivingEntity> predicate) {
      return Iterables.filter(this.nearbyEntities, (livingentity) -> predicate.test(livingentity) && this.lineOfSightTest.test(livingentity));
   }

   public Stream<LivingEntity> find(Predicate<LivingEntity> predicate) {
      return this.nearbyEntities.stream().filter((livingentity) -> predicate.test(livingentity) && this.lineOfSightTest.test(livingentity));
   }

   public boolean contains(LivingEntity livingentity) {
      return this.nearbyEntities.contains(livingentity) && this.lineOfSightTest.test(livingentity);
   }

   public boolean contains(Predicate<LivingEntity> predicate) {
      for(LivingEntity livingentity : this.nearbyEntities) {
         if (predicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
            return true;
         }
      }

      return false;
   }
}
