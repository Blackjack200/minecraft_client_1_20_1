package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;

public class MobSpawnSettings {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1F;
   public static final WeightedRandomList<MobSpawnSettings.SpawnerData> EMPTY_MOB_LIST = WeightedRandomList.create();
   public static final MobSpawnSettings EMPTY = (new MobSpawnSettings.Builder()).build();
   public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.floatRange(0.0F, 0.9999999F).optionalFieldOf("creature_spawn_probability", 0.1F).forGetter((mobspawnsettings2) -> mobspawnsettings2.creatureGenerationProbability), Codec.simpleMap(MobCategory.CODEC, WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).promotePartial(Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter((mobspawnsettings1) -> mobspawnsettings1.spawners), Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnSettings.MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter((mobspawnsettings) -> mobspawnsettings.mobSpawnCosts)).apply(recordcodecbuilder_instance, MobSpawnSettings::new));
   private final float creatureGenerationProbability;
   private final Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> spawners;
   private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts;

   MobSpawnSettings(float f, Map<MobCategory, WeightedRandomList<MobSpawnSettings.SpawnerData>> map, Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> map1) {
      this.creatureGenerationProbability = f;
      this.spawners = ImmutableMap.copyOf(map);
      this.mobSpawnCosts = ImmutableMap.copyOf(map1);
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobs(MobCategory mobcategory) {
      return this.spawners.getOrDefault(mobcategory, EMPTY_MOB_LIST);
   }

   @Nullable
   public MobSpawnSettings.MobSpawnCost getMobSpawnCost(EntityType<?> entitytype) {
      return this.mobSpawnCosts.get(entitytype);
   }

   public float getCreatureProbability() {
      return this.creatureGenerationProbability;
   }

   public static class Builder {
      private final Map<MobCategory, List<MobSpawnSettings.SpawnerData>> spawners = Stream.of(MobCategory.values()).collect(ImmutableMap.toImmutableMap((mobcategory1) -> mobcategory1, (mobcategory) -> Lists.newArrayList()));
      private final Map<EntityType<?>, MobSpawnSettings.MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
      private float creatureGenerationProbability = 0.1F;

      public MobSpawnSettings.Builder addSpawn(MobCategory mobcategory, MobSpawnSettings.SpawnerData mobspawnsettings_spawnerdata) {
         this.spawners.get(mobcategory).add(mobspawnsettings_spawnerdata);
         return this;
      }

      public MobSpawnSettings.Builder addMobCharge(EntityType<?> entitytype, double d0, double d1) {
         this.mobSpawnCosts.put(entitytype, new MobSpawnSettings.MobSpawnCost(d1, d0));
         return this;
      }

      public MobSpawnSettings.Builder creatureGenerationProbability(float f) {
         this.creatureGenerationProbability = f;
         return this;
      }

      public MobSpawnSettings build() {
         return new MobSpawnSettings(this.creatureGenerationProbability, this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (map_entry) -> WeightedRandomList.create(map_entry.getValue()))), ImmutableMap.copyOf(this.mobSpawnCosts));
      }
   }

   public static record MobSpawnCost(double energyBudget, double charge) {
      public static final Codec<MobSpawnSettings.MobSpawnCost> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.DOUBLE.fieldOf("energy_budget").forGetter((mobspawnsettings_mobspawncost1) -> mobspawnsettings_mobspawncost1.energyBudget), Codec.DOUBLE.fieldOf("charge").forGetter((mobspawnsettings_mobspawncost) -> mobspawnsettings_mobspawncost.charge)).apply(recordcodecbuilder_instance, MobSpawnSettings.MobSpawnCost::new));
   }

   public static class SpawnerData extends WeightedEntry.IntrusiveBase {
      public static final Codec<MobSpawnSettings.SpawnerData> CODEC = ExtraCodecs.validate(RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter((mobspawnsettings_spawnerdata2) -> mobspawnsettings_spawnerdata2.type), Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.IntrusiveBase::getWeight), ExtraCodecs.POSITIVE_INT.fieldOf("minCount").forGetter((mobspawnsettings_spawnerdata1) -> mobspawnsettings_spawnerdata1.minCount), ExtraCodecs.POSITIVE_INT.fieldOf("maxCount").forGetter((mobspawnsettings_spawnerdata) -> mobspawnsettings_spawnerdata.maxCount)).apply(recordcodecbuilder_instance, MobSpawnSettings.SpawnerData::new)), (mobspawnsettings_spawnerdata) -> mobspawnsettings_spawnerdata.minCount > mobspawnsettings_spawnerdata.maxCount ? DataResult.error(() -> "minCount needs to be smaller or equal to maxCount") : DataResult.success(mobspawnsettings_spawnerdata));
      public final EntityType<?> type;
      public final int minCount;
      public final int maxCount;

      public SpawnerData(EntityType<?> entitytype, int i, int j, int k) {
         this(entitytype, Weight.of(i), j, k);
      }

      public SpawnerData(EntityType<?> entitytype, Weight weight, int i, int j) {
         super(weight);
         this.type = entitytype.getCategory() == MobCategory.MISC ? EntityType.PIG : entitytype;
         this.minCount = i;
         this.maxCount = j;
      }

      public String toString() {
         return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.getWeight();
      }
   }
}
