package net.minecraft.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootDataType<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(Deserializers.createConditionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemCondition.class, LootDataManager::createComposite), "predicates", createSimpleValidator());
   public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(Deserializers.createFunctionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemFunction.class, LootDataManager::createComposite), "item_modifiers", createSimpleValidator());
   public static final LootDataType<LootTable> TABLE = new LootDataType<>(Deserializers.createLootTableSerializer().create(), createSingleDeserialiser(LootTable.class), "loot_tables", createLootTableValidator());
   private final Gson parser;
   private final BiFunction<ResourceLocation, JsonElement, Optional<T>> topDeserializer;
   private final String directory;
   private final LootDataType.Validator<T> validator;

   private LootDataType(Gson gson, BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> bifunction, String s, LootDataType.Validator<T> lootdatatype_validator) {
      this.parser = gson;
      this.directory = s;
      this.validator = lootdatatype_validator;
      this.topDeserializer = bifunction.apply(gson, s);
   }

   public Gson parser() {
      return this.parser;
   }

   public String directory() {
      return this.directory;
   }

   public void runValidation(ValidationContext validationcontext, LootDataId<T> lootdataid, T object) {
      this.validator.run(validationcontext, lootdataid, object);
   }

   public Optional<T> deserialize(ResourceLocation resourcelocation, JsonElement jsonelement) {
      return this.topDeserializer.apply(resourcelocation, jsonelement);
   }

   public static Stream<LootDataType<?>> values() {
      return Stream.of(PREDICATE, MODIFIER, TABLE);
   }

   private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleDeserialiser(Class<T> oclass) {
      return (gson, s) -> (resourcelocation, jsonelement) -> {
            try {
               return Optional.of(gson.fromJson(jsonelement, oclass));
            } catch (Exception var6) {
               LOGGER.error("Couldn't parse element {}:{}", s, resourcelocation, var6);
               return Optional.empty();
            }
         };
   }

   private static <T> BiFunction<Gson, String, BiFunction<ResourceLocation, JsonElement, Optional<T>>> createSingleOrMultipleDeserialiser(Class<T> oclass, Function<T[], T> function) {
      Class<T[]> oclass1 = oclass.arrayType();
      return (gson, s) -> (resourcelocation, jsonelement) -> {
            try {
               if (jsonelement.isJsonArray()) {
                  T[] aobject = (T[])((Object[])gson.fromJson(jsonelement, oclass1));
                  return Optional.of(function.apply((T)aobject));
               } else {
                  return Optional.of(gson.fromJson(jsonelement, oclass));
               }
            } catch (Exception var8) {
               LOGGER.error("Couldn't parse element {}:{}", s, resourcelocation, var8);
               return Optional.empty();
            }
         };
   }

   private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
      return (validationcontext, lootdataid, lootcontextuser) -> lootcontextuser.validate(validationcontext.enterElement("{" + lootdataid.type().directory + ":" + lootdataid.location() + "}", lootdataid));
   }

   private static LootDataType.Validator<LootTable> createLootTableValidator() {
      return (validationcontext, lootdataid, loottable) -> loottable.validate(validationcontext.setParams(loottable.getParamSet()).enterElement("{" + lootdataid.type().directory + ":" + lootdataid.location() + "}", lootdataid));
   }

   @FunctionalInterface
   public interface Validator<T> {
      void run(ValidationContext validationcontext, LootDataId<T> lootdataid, T object);
   }
}
