package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;

public class AttributesRename extends DataFix {
   private static final Map<String, String> RENAMES = ImmutableMap.<String, String>builder().put("generic.maxHealth", "generic.max_health").put("Max Health", "generic.max_health").put("zombie.spawnReinforcements", "zombie.spawn_reinforcements").put("Spawn Reinforcements Chance", "zombie.spawn_reinforcements").put("horse.jumpStrength", "horse.jump_strength").put("Jump Strength", "horse.jump_strength").put("generic.followRange", "generic.follow_range").put("Follow Range", "generic.follow_range").put("generic.knockbackResistance", "generic.knockback_resistance").put("Knockback Resistance", "generic.knockback_resistance").put("generic.movementSpeed", "generic.movement_speed").put("Movement Speed", "generic.movement_speed").put("generic.flyingSpeed", "generic.flying_speed").put("Flying Speed", "generic.flying_speed").put("generic.attackDamage", "generic.attack_damage").put("generic.attackKnockback", "generic.attack_knockback").put("generic.attackSpeed", "generic.attack_speed").put("generic.armorToughness", "generic.armor_toughness").build();

   public AttributesRename(Schema schema) {
      super(schema, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> opticfinder = type.findField("tag");
      return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("Rename ItemStack Attributes", type, (typed1) -> typed1.updateTyped(opticfinder, AttributesRename::fixItemStackTag)), this.fixTypeEverywhereTyped("Rename Entity Attributes", this.getInputSchema().getType(References.ENTITY), AttributesRename::fixEntity), this.fixTypeEverywhereTyped("Rename Player Attributes", this.getInputSchema().getType(References.PLAYER), AttributesRename::fixEntity));
   }

   private static Dynamic<?> fixName(Dynamic<?> dynamic) {
      return DataFixUtils.orElse(dynamic.asString().result().map((s) -> RENAMES.getOrDefault(s, s)).map(dynamic::createString), dynamic);
   }

   private static Typed<?> fixItemStackTag(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("AttributeModifiers", (dynamic1) -> DataFixUtils.orElse(dynamic1.asStreamOpt().result().map((stream) -> stream.map((dynamic2) -> dynamic2.update("AttributeName", AttributesRename::fixName))).map(dynamic1::createList), dynamic1)));
   }

   private static Typed<?> fixEntity(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> dynamic.update("Attributes", (dynamic1) -> DataFixUtils.orElse(dynamic1.asStreamOpt().result().map((stream) -> stream.map((dynamic2) -> dynamic2.update("Name", AttributesRename::fixName))).map(dynamic1::createList), dynamic1)));
   }
}
