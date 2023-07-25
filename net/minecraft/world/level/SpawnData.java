package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules) {
   public static final String ENTITY_TAG = "entity";
   public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(CompoundTag.CODEC.fieldOf("entity").forGetter((spawndata1) -> spawndata1.entityToSpawn), SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter((spawndata) -> spawndata.customSpawnRules)).apply(recordcodecbuilder_instance, SpawnData::new));
   public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

   public SpawnData() {
      this(new CompoundTag(), Optional.empty());
   }

   public SpawnData(CompoundTag compoundtag, Optional<SpawnData.CustomSpawnRules> optional) {
      if (compoundtag.contains("id")) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("id"));
         if (resourcelocation != null) {
            compoundtag.putString("id", resourcelocation.toString());
         } else {
            compoundtag.remove("id");
         }
      }

      this.entityToSpawn = compoundtag;
      this.customSpawnRules = optional;
   }

   public CompoundTag getEntityToSpawn() {
      return this.entityToSpawn;
   }

   public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
      return this.customSpawnRules;
   }

   public static record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
      private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<>(0, 15);
      public static final Codec<SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(lightLimit("block_light_limit").forGetter((spawndata_customspawnrules1) -> spawndata_customspawnrules1.blockLightLimit), lightLimit("sky_light_limit").forGetter((spawndata_customspawnrules) -> spawndata_customspawnrules.skyLightLimit)).apply(recordcodecbuilder_instance, SpawnData.CustomSpawnRules::new));

      private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> inclusiverange) {
         return !LIGHT_RANGE.contains(inclusiverange) ? DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE) : DataResult.success(inclusiverange);
      }

      private static MapCodec<InclusiveRange<Integer>> lightLimit(String s) {
         return ExtraCodecs.validate(InclusiveRange.INT.optionalFieldOf(s, LIGHT_RANGE), SpawnData.CustomSpawnRules::checkLightBoundaries);
      }
   }
}
