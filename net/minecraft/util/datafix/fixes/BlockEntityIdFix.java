package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;

public class BlockEntityIdFix extends DataFix {
   private static final Map<String, String> ID_MAP = DataFixUtils.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put("Airportal", "minecraft:end_portal");
      hashmap.put("Banner", "minecraft:banner");
      hashmap.put("Beacon", "minecraft:beacon");
      hashmap.put("Cauldron", "minecraft:brewing_stand");
      hashmap.put("Chest", "minecraft:chest");
      hashmap.put("Comparator", "minecraft:comparator");
      hashmap.put("Control", "minecraft:command_block");
      hashmap.put("DLDetector", "minecraft:daylight_detector");
      hashmap.put("Dropper", "minecraft:dropper");
      hashmap.put("EnchantTable", "minecraft:enchanting_table");
      hashmap.put("EndGateway", "minecraft:end_gateway");
      hashmap.put("EnderChest", "minecraft:ender_chest");
      hashmap.put("FlowerPot", "minecraft:flower_pot");
      hashmap.put("Furnace", "minecraft:furnace");
      hashmap.put("Hopper", "minecraft:hopper");
      hashmap.put("MobSpawner", "minecraft:mob_spawner");
      hashmap.put("Music", "minecraft:noteblock");
      hashmap.put("Piston", "minecraft:piston");
      hashmap.put("RecordPlayer", "minecraft:jukebox");
      hashmap.put("Sign", "minecraft:sign");
      hashmap.put("Skull", "minecraft:skull");
      hashmap.put("Structure", "minecraft:structure_block");
      hashmap.put("Trap", "minecraft:dispenser");
   });

   public BlockEntityIdFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      Type<?> type1 = this.getOutputSchema().getType(References.ITEM_STACK);
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
      return TypeRewriteRule.seq(this.convertUnchecked("item stack block entity name hook converter", type, type1), this.fixTypeEverywhere("BlockEntityIdFix", taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops) -> (pair) -> pair.mapFirst((s) -> ID_MAP.getOrDefault(s, s))));
   }
}
