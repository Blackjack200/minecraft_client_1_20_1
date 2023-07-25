package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Arrays;
import java.util.function.Function;

public class EntityProjectileOwnerFix extends DataFix {
   public EntityProjectileOwnerFix(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      return this.fixTypeEverywhereTyped("EntityProjectileOwner", schema.getType(References.ENTITY), this::updateProjectiles);
   }

   private Typed<?> updateProjectiles(Typed<?> typed) {
      typed = this.updateEntity(typed, "minecraft:egg", this::updateOwnerThrowable);
      typed = this.updateEntity(typed, "minecraft:ender_pearl", this::updateOwnerThrowable);
      typed = this.updateEntity(typed, "minecraft:experience_bottle", this::updateOwnerThrowable);
      typed = this.updateEntity(typed, "minecraft:snowball", this::updateOwnerThrowable);
      typed = this.updateEntity(typed, "minecraft:potion", this::updateOwnerThrowable);
      typed = this.updateEntity(typed, "minecraft:potion", this::updateItemPotion);
      typed = this.updateEntity(typed, "minecraft:llama_spit", this::updateOwnerLlamaSpit);
      typed = this.updateEntity(typed, "minecraft:arrow", this::updateOwnerArrow);
      typed = this.updateEntity(typed, "minecraft:spectral_arrow", this::updateOwnerArrow);
      return this.updateEntity(typed, "minecraft:trident", this::updateOwnerArrow);
   }

   private Dynamic<?> updateOwnerArrow(Dynamic<?> dynamic) {
      long i = dynamic.get("OwnerUUIDMost").asLong(0L);
      long j = dynamic.get("OwnerUUIDLeast").asLong(0L);
      return this.setUUID(dynamic, i, j).remove("OwnerUUIDMost").remove("OwnerUUIDLeast");
   }

   private Dynamic<?> updateOwnerLlamaSpit(Dynamic<?> dynamic1) {
      OptionalDynamic<?> optionaldynamic = dynamic1.get("Owner");
      long k = optionaldynamic.get("OwnerUUIDMost").asLong(0L);
      long l = optionaldynamic.get("OwnerUUIDLeast").asLong(0L);
      return this.setUUID(dynamic1, k, l).remove("Owner");
   }

   private Dynamic<?> updateItemPotion(Dynamic<?> dynamic2) {
      OptionalDynamic<?> optionaldynamic1 = dynamic2.get("Potion");
      return dynamic2.set("Item", optionaldynamic1.orElseEmptyMap()).remove("Potion");
   }

   private Dynamic<?> updateOwnerThrowable(Dynamic<?> dynamic3) {
      String s = "owner";
      OptionalDynamic<?> optionaldynamic2 = dynamic3.get("owner");
      long i1 = optionaldynamic2.get("M").asLong(0L);
      long j1 = optionaldynamic2.get("L").asLong(0L);
      return this.setUUID(dynamic3, i1, j1).remove("owner");
   }

   private Dynamic<?> setUUID(Dynamic<?> dynamic, long i, long j) {
      String s = "OwnerUUID";
      return i != 0L && j != 0L ? dynamic.set("OwnerUUID", dynamic.createIntList(Arrays.stream(createUUIDArray(i, j)))) : dynamic;
   }

   private static int[] createUUIDArray(long i, long j) {
      return new int[]{(int)(i >> 32), (int)i, (int)(j >> 32), (int)j};
   }

   private Typed<?> updateEntity(Typed<?> typed, String s, Function<Dynamic<?>, Dynamic<?>> function) {
      Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, s);
      Type<?> type1 = this.getOutputSchema().getChoiceType(References.ENTITY, s);
      return typed.updateTyped(DSL.namedChoice(s, type), type1, (typed1) -> typed1.update(DSL.remainderFinder(), function));
   }
}
