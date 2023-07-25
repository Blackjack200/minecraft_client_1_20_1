package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public abstract class EntityLootSubProvider implements LootTableSubProvider {
   protected static final EntityPredicate.Builder ENTITY_ON_FIRE = EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true).build());
   private static final Set<EntityType<?>> SPECIAL_LOOT_TABLE_TYPES = ImmutableSet.of(EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER);
   private final FeatureFlagSet allowed;
   private final FeatureFlagSet required;
   private final Map<EntityType<?>, Map<ResourceLocation, LootTable.Builder>> map = Maps.newHashMap();

   protected EntityLootSubProvider(FeatureFlagSet featureflagset) {
      this(featureflagset, featureflagset);
   }

   protected EntityLootSubProvider(FeatureFlagSet featureflagset, FeatureFlagSet featureflagset1) {
      this.allowed = featureflagset;
      this.required = featureflagset1;
   }

   protected static LootTable.Builder createSheepTable(ItemLike itemlike) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(itemlike))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootTableReference.lootTableReference(EntityType.SHEEP.getDefaultLootTable())));
   }

   public abstract void generate();

   public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biconsumer) {
      this.generate();
      Set<ResourceLocation> set = Sets.newHashSet();
      BuiltInRegistries.ENTITY_TYPE.holders().forEach((holder_reference) -> {
         EntityType<?> entitytype = holder_reference.value();
         if (entitytype.isEnabled(this.allowed)) {
            if (canHaveLootTable(entitytype)) {
               Map<ResourceLocation, LootTable.Builder> map = this.map.remove(entitytype);
               ResourceLocation resourcelocation = entitytype.getDefaultLootTable();
               if (!resourcelocation.equals(BuiltInLootTables.EMPTY) && entitytype.isEnabled(this.required) && (map == null || !map.containsKey(resourcelocation))) {
                  throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourcelocation, holder_reference.key().location()));
               }

               if (map != null) {
                  map.forEach((resourcelocation1, loottable_builder) -> {
                     if (!set.add(resourcelocation1)) {
                        throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", resourcelocation1, holder_reference.key().location()));
                     } else {
                        biconsumer.accept(resourcelocation1, loottable_builder);
                     }
                  });
               }
            } else {
               Map<ResourceLocation, LootTable.Builder> map1 = this.map.remove(entitytype);
               if (map1 != null) {
                  throw new IllegalStateException(String.format(Locale.ROOT, "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot", map1.keySet().stream().map(ResourceLocation::toString).collect(Collectors.joining(",")), holder_reference.key().location()));
               }
            }

         }
      });
      if (!this.map.isEmpty()) {
         throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + this.map.keySet());
      }
   }

   private static boolean canHaveLootTable(EntityType<?> entitytype) {
      return SPECIAL_LOOT_TABLE_TYPES.contains(entitytype) || entitytype.getCategory() != MobCategory.MISC;
   }

   protected LootItemCondition.Builder killedByFrog() {
      return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(EntityType.FROG)));
   }

   protected LootItemCondition.Builder killedByFrogVariant(FrogVariant frogvariant) {
      return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(EntityType.FROG).subPredicate(EntitySubPredicate.variant(frogvariant))));
   }

   protected void add(EntityType<?> entitytype, LootTable.Builder loottable_builder) {
      this.add(entitytype, entitytype.getDefaultLootTable(), loottable_builder);
   }

   protected void add(EntityType<?> entitytype, ResourceLocation resourcelocation, LootTable.Builder loottable_builder) {
      this.map.computeIfAbsent(entitytype, (entitytype1) -> new HashMap()).put(resourcelocation, loottable_builder);
   }
}
