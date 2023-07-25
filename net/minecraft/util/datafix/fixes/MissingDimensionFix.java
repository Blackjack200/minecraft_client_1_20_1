package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix extends DataFix {
   public MissingDimensionFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected static <A> Type<Pair<A, Dynamic<?>>> fields(String s, Type<A> type) {
      return DSL.and(DSL.field(s, type), DSL.remainderType());
   }

   protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> optionalFields(String s, Type<A> type) {
      return DSL.and(DSL.optional(DSL.field(s, type)), DSL.remainderType());
   }

   protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> optionalFields(String s, Type<A1> type, String s1, Type<A2> type1) {
      return DSL.and(DSL.optional(DSL.field(s, type)), DSL.optional(DSL.field(s1, type1)), DSL.remainderType());
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      Type<?> type = DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL.remainderType(), "minecraft:flat", flatType(schema), "minecraft:noise", optionalFields("biome_source", DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:fixed", fields("biome", schema.getType(References.BIOME)), "minecraft:multi_noise", DSL.list(fields("biome", schema.getType(References.BIOME))), "minecraft:checkerboard", fields("biomes", DSL.list(schema.getType(References.BIOME))), "minecraft:vanilla_layered", DSL.remainderType(), "minecraft:the_end", DSL.remainderType())), "settings", DSL.or(DSL.string(), optionalFields("default_block", schema.getType(References.BLOCK_NAME), "default_fluid", schema.getType(References.BLOCK_NAME))))));
      CompoundList.CompoundListType<String, ?> compoundlist_compoundlisttype = DSL.compoundList(NamespacedSchema.namespacedString(), fields("generator", type));
      Type<?> type1 = DSL.and(compoundlist_compoundlisttype, DSL.remainderType());
      Type<?> type2 = schema.getType(References.WORLD_GEN_SETTINGS);
      FieldFinder<?> fieldfinder = new FieldFinder<>("dimensions", type1);
      if (!type2.findFieldType("dimensions").equals(type1)) {
         throw new IllegalStateException();
      } else {
         OpticFinder<? extends List<? extends Pair<String, ?>>> opticfinder = compoundlist_compoundlisttype.finder();
         return this.fixTypeEverywhereTyped("MissingDimensionFix", type2, (typed) -> typed.updateTyped(fieldfinder, (typed2) -> typed2.updateTyped(opticfinder, (typed4) -> {
                  if (!(typed4.getValue() instanceof List)) {
                     throw new IllegalStateException("List exptected");
                  } else if (((List)typed4.getValue()).isEmpty()) {
                     Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
                     Dynamic<?> dynamic1 = this.recreateSettings(dynamic);
                     return DataFixUtils.orElse(compoundlist_compoundlisttype.readTyped(dynamic1).result().map(Pair::getFirst), typed4);
                  } else {
                     return typed4;
                  }
               })));
      }
   }

   protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatType(Schema schema) {
      return optionalFields("settings", optionalFields("biome", schema.getType(References.BIOME), "layers", DSL.list(optionalFields("block", schema.getType(References.BLOCK_NAME)))));
   }

   private <T> Dynamic<T> recreateSettings(Dynamic<T> dynamic) {
      long i = dynamic.get("seed").asLong(0L);
      return new Dynamic<>(dynamic.getOps(), WorldGenSettingsFix.vanillaLevels(dynamic, i, WorldGenSettingsFix.defaultOverworld(dynamic, i), false));
   }
}
