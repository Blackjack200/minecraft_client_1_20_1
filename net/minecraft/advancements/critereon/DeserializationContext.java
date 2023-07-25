package net.minecraft.advancements.critereon;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class DeserializationContext {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation id;
   private final LootDataManager lootData;
   private final Gson predicateGson = Deserializers.createConditionSerializer().create();

   public DeserializationContext(ResourceLocation resourcelocation, LootDataManager lootdatamanager) {
      this.id = resourcelocation;
      this.lootData = lootdatamanager;
   }

   public final LootItemCondition[] deserializeConditions(JsonArray jsonarray, String s, LootContextParamSet lootcontextparamset) {
      LootItemCondition[] alootitemcondition = this.predicateGson.fromJson(jsonarray, LootItemCondition[].class);
      ValidationContext validationcontext = new ValidationContext(lootcontextparamset, this.lootData);

      for(LootItemCondition lootitemcondition : alootitemcondition) {
         lootitemcondition.validate(validationcontext);
         validationcontext.getProblems().forEach((s2, s3) -> LOGGER.warn("Found validation problem in advancement trigger {}/{}: {}", s, s2, s3));
      }

      return alootitemcondition;
   }

   public ResourceLocation getAdvancementId() {
      return this.id;
   }
}
