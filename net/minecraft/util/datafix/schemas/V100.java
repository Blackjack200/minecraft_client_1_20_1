package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V100 extends Schema {
   public V100(int i, Schema schema) {
      super(i, schema);
   }

   protected static TypeTemplate equipment(Schema schema) {
      return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(schema)), "HandItems", DSL.list(References.ITEM_STACK.in(schema)));
   }

   protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> equipment(schema));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
      registerMob(schema, map, "ArmorStand");
      registerMob(schema, map, "Creeper");
      registerMob(schema, map, "Skeleton");
      registerMob(schema, map, "Spider");
      registerMob(schema, map, "Giant");
      registerMob(schema, map, "Zombie");
      registerMob(schema, map, "Slime");
      registerMob(schema, map, "Ghast");
      registerMob(schema, map, "PigZombie");
      schema.register(map, "Enderman", (s2) -> DSL.optionalFields("carried", References.BLOCK_NAME.in(schema), equipment(schema)));
      registerMob(schema, map, "CaveSpider");
      registerMob(schema, map, "Silverfish");
      registerMob(schema, map, "Blaze");
      registerMob(schema, map, "LavaSlime");
      registerMob(schema, map, "EnderDragon");
      registerMob(schema, map, "WitherBoss");
      registerMob(schema, map, "Bat");
      registerMob(schema, map, "Witch");
      registerMob(schema, map, "Endermite");
      registerMob(schema, map, "Guardian");
      registerMob(schema, map, "Pig");
      registerMob(schema, map, "Sheep");
      registerMob(schema, map, "Cow");
      registerMob(schema, map, "Chicken");
      registerMob(schema, map, "Squid");
      registerMob(schema, map, "Wolf");
      registerMob(schema, map, "MushroomCow");
      registerMob(schema, map, "SnowMan");
      registerMob(schema, map, "Ozelot");
      registerMob(schema, map, "VillagerGolem");
      schema.register(map, "EntityHorse", (s1) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), equipment(schema)));
      registerMob(schema, map, "Rabbit");
      schema.register(map, "Villager", (s) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)))), equipment(schema)));
      registerMob(schema, map, "Shulker");
      schema.registerSimple(map, "AreaEffectCloud");
      schema.registerSimple(map, "ShulkerBullet");
      return map;
   }

   public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
      super.registerTypes(schema, map, map1);
      schema.registerType(false, References.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(schema))), "palette", DSL.list(References.BLOCK_STATE.in(schema))));
      schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
   }
}
