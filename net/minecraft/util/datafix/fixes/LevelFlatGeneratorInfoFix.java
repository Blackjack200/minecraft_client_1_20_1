package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix extends DataFix {
   private static final String GENERATOR_OPTIONS = "generatorOptions";
   @VisibleForTesting
   static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
   private static final Splitter SPLITTER = Splitter.on(';').limit(5);
   private static final Splitter LAYER_SPLITTER = Splitter.on(',');
   private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on('x').limit(2);
   private static final Splitter AMOUNT_SPLITTER = Splitter.on('*').limit(2);
   private static final Splitter BLOCK_SPLITTER = Splitter.on(':').limit(3);

   public LevelFlatGeneratorInfoFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped("LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), (typed) -> typed.update(DSL.remainderFinder(), this::fix));
   }

   private Dynamic<?> fix(Dynamic<?> dynamic) {
      return dynamic.get("generatorName").asString("").equalsIgnoreCase("flat") ? dynamic.update("generatorOptions", (dynamic1) -> DataFixUtils.orElse(dynamic1.asString().map(this::fixString).map(dynamic1::createString).result(), dynamic1)) : dynamic;
   }

   @VisibleForTesting
   String fixString(String s) {
      if (s.isEmpty()) {
         return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
      } else {
         Iterator<String> iterator = SPLITTER.split(s).iterator();
         String s1 = iterator.next();
         int i;
         String s2;
         if (iterator.hasNext()) {
            i = NumberUtils.toInt(s1, 0);
            s2 = iterator.next();
         } else {
            i = 0;
            s2 = s1;
         }

         if (i >= 0 && i <= 3) {
            StringBuilder stringbuilder = new StringBuilder();
            Splitter splitter = i < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
            stringbuilder.append(StreamSupport.stream(LAYER_SPLITTER.split(s2).spliterator(), false).map((s4) -> {
               List<String> list = splitter.splitToList(s4);
               int l;
               String s5;
               if (list.size() == 2) {
                  l = NumberUtils.toInt(list.get(0));
                  s5 = list.get(1);
               } else {
                  l = 1;
                  s5 = list.get(0);
               }

               List<String> list1 = BLOCK_SPLITTER.splitToList(s5);
               int j1 = list1.get(0).equals("minecraft") ? 1 : 0;
               String s7 = list1.get(j1);
               int k1 = i == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + s7) : NumberUtils.toInt(s7, 0);
               int l1 = j1 + 1;
               int i2 = list1.size() > l1 ? NumberUtils.toInt(list1.get(l1), 0) : 0;
               return (l == 1 ? "" : l + "*") + BlockStateData.getTag(k1 << 4 | i2).get("Name").asString("");
            }).collect(Collectors.joining(",")));

            while(iterator.hasNext()) {
               stringbuilder.append(';').append(iterator.next());
            }

            return stringbuilder.toString();
         } else {
            return "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
         }
      }
   }
}
