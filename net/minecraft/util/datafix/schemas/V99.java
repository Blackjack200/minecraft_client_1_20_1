package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class V99 extends Schema {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("minecraft:furnace", "Furnace");
      hashmap.put("minecraft:lit_furnace", "Furnace");
      hashmap.put("minecraft:chest", "Chest");
      hashmap.put("minecraft:trapped_chest", "Chest");
      hashmap.put("minecraft:ender_chest", "EnderChest");
      hashmap.put("minecraft:jukebox", "RecordPlayer");
      hashmap.put("minecraft:dispenser", "Trap");
      hashmap.put("minecraft:dropper", "Dropper");
      hashmap.put("minecraft:sign", "Sign");
      hashmap.put("minecraft:mob_spawner", "MobSpawner");
      hashmap.put("minecraft:noteblock", "Music");
      hashmap.put("minecraft:brewing_stand", "Cauldron");
      hashmap.put("minecraft:enhanting_table", "EnchantTable");
      hashmap.put("minecraft:command_block", "CommandBlock");
      hashmap.put("minecraft:beacon", "Beacon");
      hashmap.put("minecraft:skull", "Skull");
      hashmap.put("minecraft:daylight_detector", "DLDetector");
      hashmap.put("minecraft:hopper", "Hopper");
      hashmap.put("minecraft:banner", "Banner");
      hashmap.put("minecraft:flower_pot", "FlowerPot");
      hashmap.put("minecraft:repeating_command_block", "CommandBlock");
      hashmap.put("minecraft:chain_command_block", "CommandBlock");
      hashmap.put("minecraft:standing_sign", "Sign");
      hashmap.put("minecraft:wall_sign", "Sign");
      hashmap.put("minecraft:piston_head", "Piston");
      hashmap.put("minecraft:daylight_detector_inverted", "DLDetector");
      hashmap.put("minecraft:unpowered_comparator", "Comparator");
      hashmap.put("minecraft:powered_comparator", "Comparator");
      hashmap.put("minecraft:wall_banner", "Banner");
      hashmap.put("minecraft:standing_banner", "Banner");
      hashmap.put("minecraft:structure_block", "Structure");
      hashmap.put("minecraft:end_portal", "Airportal");
      hashmap.put("minecraft:end_gateway", "EndGateway");
      hashmap.put("minecraft:shield", "Banner");
   });
   protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction() {
      public <T> T apply(DynamicOps<T> dynamicops, T object) {
         return V99.addNames(new Dynamic<>(dynamicops, object), V99.ITEM_TO_BLOCKENTITY, "ArmorStand");
      }
   };

   public V99(int i, Schema schema) {
      super(i, schema);
   }

   protected static TypeTemplate equipment(Schema schema) {
      return DSL.optionalFields("Equipment", DSL.list(References.ITEM_STACK.in(schema)));
   }

   protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> equipment(schema));
   }

   protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
   }

   protected static void registerMinecart(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
   }

   protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String s) {
      schema.register(map, s, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
      schema.register(map, "Item", (s12) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
      schema.registerSimple(map, "XPOrb");
      registerThrowableProjectile(schema, map, "ThrownEgg");
      schema.registerSimple(map, "LeashKnot");
      schema.registerSimple(map, "Painting");
      schema.register(map, "Arrow", (s11) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
      schema.register(map, "TippedArrow", (s10) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
      schema.register(map, "SpectralArrow", (s9) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
      registerThrowableProjectile(schema, map, "Snowball");
      registerThrowableProjectile(schema, map, "Fireball");
      registerThrowableProjectile(schema, map, "SmallFireball");
      registerThrowableProjectile(schema, map, "ThrownEnderpearl");
      schema.registerSimple(map, "EyeOfEnderSignal");
      schema.register(map, "ThrownPotion", (s8) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema), "Potion", References.ITEM_STACK.in(schema)));
      registerThrowableProjectile(schema, map, "ThrownExpBottle");
      schema.register(map, "ItemFrame", (s7) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
      registerThrowableProjectile(schema, map, "WitherSkull");
      schema.registerSimple(map, "PrimedTnt");
      schema.register(map, "FallingSand", (s6) -> DSL.optionalFields("Block", References.BLOCK_NAME.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)));
      schema.register(map, "FireworksRocketEntity", (s5) -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)));
      schema.registerSimple(map, "Boat");
      schema.register(map, "Minecart", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
      registerMinecart(schema, map, "MinecartRideable");
      schema.register(map, "MinecartChest", (s4) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
      registerMinecart(schema, map, "MinecartFurnace");
      registerMinecart(schema, map, "MinecartTNT");
      schema.register(map, "MinecartSpawner", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), References.UNTAGGED_SPAWNER.in(schema)));
      schema.register(map, "MinecartHopper", (s3) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
      registerMinecart(schema, map, "MinecartCommandBlock");
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
      schema.registerSimple(map, "EnderCrystal");
      schema.registerSimple(map, "AreaEffectCloud");
      schema.registerSimple(map, "ShulkerBullet");
      registerMob(schema, map, "Shulker");
      return map;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
      Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
      registerInventory(schema, map, "Furnace");
      registerInventory(schema, map, "Chest");
      schema.registerSimple(map, "EnderChest");
      schema.register(map, "RecordPlayer", (s2) -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema)));
      registerInventory(schema, map, "Trap");
      registerInventory(schema, map, "Dropper");
      schema.registerSimple(map, "Sign");
      schema.register(map, "MobSpawner", (s1) -> References.UNTAGGED_SPAWNER.in(schema));
      schema.registerSimple(map, "Music");
      schema.registerSimple(map, "Piston");
      registerInventory(schema, map, "Cauldron");
      schema.registerSimple(map, "EnchantTable");
      schema.registerSimple(map, "Airportal");
      schema.registerSimple(map, "Control");
      schema.registerSimple(map, "Beacon");
      schema.registerSimple(map, "Skull");
      schema.registerSimple(map, "DLDetector");
      registerInventory(schema, map, "Hopper");
      schema.registerSimple(map, "Comparator");
      schema.register(map, "FlowerPot", (s) -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema))));
      schema.registerSimple(map, "Banner");
      schema.registerSimple(map, "Structure");
      schema.registerSimple(map, "EndGateway");
      return map;
   }

   public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map1) {
      schema.registerType(false, References.LEVEL, DSL::remainder);
      schema.registerType(false, References.PLAYER, () -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "EnderItems", DSL.list(References.ITEM_STACK.in(schema))));
      schema.registerType(false, References.CHUNK, () -> DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(schema))))));
      schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), map1));
      schema.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Riding", References.ENTITY_TREE.in(schema), References.ENTITY.in(schema)));
      schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
      schema.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), map));
      schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema)), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(schema), "BlockEntityTag", References.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema)), "Items", DSL.list(References.ITEM_STACK.in(schema)))), ADD_NAMES, HookFunction.IDENTITY));
      schema.registerType(false, References.OPTIONS, DSL::remainder);
      schema.registerType(false, References.BLOCK_NAME, () -> DSL.or(DSL.constType(DSL.intType()), DSL.constType(NamespacedSchema.namespacedString())));
      schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
      schema.registerType(false, References.STATS, DSL::remainder);
      schema.registerType(false, References.SAVED_DATA, () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(schema)), "Objectives", DSL.list(References.OBJECTIVE.in(schema)), "Teams", DSL.list(References.TEAM.in(schema)))));
      schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
      schema.registerType(false, References.OBJECTIVE, DSL::remainder);
      schema.registerType(false, References.TEAM, DSL::remainder);
      schema.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
      schema.registerType(false, References.POI_CHUNK, DSL::remainder);
      schema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
      schema.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema))));
   }

   protected static <T> T addNames(Dynamic<T> dynamic, Map<String, String> map, String s) {
      return dynamic.update("tag", (dynamic2) -> dynamic2.update("BlockEntityTag", (dynamic6) -> {
            String s4 = dynamic.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
            if (!"minecraft:air".equals(s4)) {
               String s5 = map.get(s4);
               if (s5 != null) {
                  return dynamic6.set("id", dynamic.createString(s5));
               }

               LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)s4);
            }

            return dynamic6;
         }).update("EntityTag", (dynamic4) -> {
            String s3 = dynamic.get("id").asString("");
            return "minecraft:armor_stand".equals(NamespacedSchema.ensureNamespaced(s3)) ? dynamic4.set("id", dynamic.createString(s)) : dynamic4;
         })).getValue();
   }
}
