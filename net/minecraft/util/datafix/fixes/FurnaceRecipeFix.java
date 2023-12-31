package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class FurnaceRecipeFix extends DataFix {
   public FurnaceRecipeFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   protected TypeRewriteRule makeRule() {
      return this.cap(this.getOutputSchema().getTypeRaw(References.RECIPE));
   }

   private <R> TypeRewriteRule cap(Type<R> type) {
      Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type1 = DSL.and(DSL.optional(DSL.field("RecipesUsed", DSL.and(DSL.compoundList(type, DSL.intType()), DSL.remainderType()))), DSL.remainderType());
      OpticFinder<?> opticfinder = DSL.namedChoice("minecraft:furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace"));
      OpticFinder<?> opticfinder1 = DSL.namedChoice("minecraft:blast_furnace", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace"));
      OpticFinder<?> opticfinder2 = DSL.namedChoice("minecraft:smoker", this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker"));
      Type<?> type2 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:furnace");
      Type<?> type3 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:blast_furnace");
      Type<?> type4 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:smoker");
      Type<?> type5 = this.getInputSchema().getType(References.BLOCK_ENTITY);
      Type<?> type6 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
      return this.fixTypeEverywhereTyped("FurnaceRecipesFix", type5, type6, (typed) -> typed.updateTyped(opticfinder, type2, (typed3) -> this.updateFurnaceContents(type, type1, typed3)).updateTyped(opticfinder1, type3, (typed2) -> this.updateFurnaceContents(type, type1, typed2)).updateTyped(opticfinder2, type4, (typed1) -> this.updateFurnaceContents(type, type1, typed1)));
   }

   private <R> Typed<?> updateFurnaceContents(Type<R> type, Type<Pair<Either<Pair<List<Pair<R, Integer>>, Dynamic<?>>, Unit>, Dynamic<?>>> type1, Typed<?> typed) {
      Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
      int i = dynamic.get("RecipesUsedSize").asInt(0);
      dynamic = dynamic.remove("RecipesUsedSize");
      List<Pair<R, Integer>> list = Lists.newArrayList();

      for(int j = 0; j < i; ++j) {
         String s = "RecipeLocation" + j;
         String s1 = "RecipeAmount" + j;
         Optional<? extends Dynamic<?>> optional = dynamic.get(s).result();
         int k = dynamic.get(s1).asInt(0);
         if (k > 0) {
            optional.ifPresent((dynamic1) -> {
               Optional<? extends Pair<R, ? extends Dynamic<?>>> optional1 = type.read(dynamic1).result();
               optional1.ifPresent((pair) -> list.add(Pair.of(pair.getFirst(), k)));
            });
         }

         dynamic = dynamic.remove(s).remove(s1);
      }

      return typed.set(DSL.remainderFinder(), type1, Pair.of(Either.left(Pair.of(list, dynamic.emptyMap())), dynamic));
   }
}
