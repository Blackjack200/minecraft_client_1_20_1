package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.function.Function;

public class ChunkBedBlockEntityInjecterFix extends DataFix {
   public ChunkBedBlockEntityInjecterFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getOutputSchema().getType(References.CHUNK);
      Type<?> type1 = type.findFieldType("Level");
      Type<?> type2 = type1.findFieldType("TileEntities");
      if (!(type2 instanceof List.ListType<?> list_listtype)) {
         throw new IllegalStateException("Tile entity type is not a list type.");
      } else {
         return this.cap(type1, list_listtype);
      }
   }

   private <TE> TypeRewriteRule cap(Type<?> type, List.ListType<TE> list_listtype) {
      Type<TE> type1 = list_listtype.getElement();
      OpticFinder<?> opticfinder = DSL.fieldFinder("Level", type);
      OpticFinder<java.util.List<TE>> opticfinder1 = DSL.fieldFinder("TileEntities", list_listtype);
      int i = 416;
      return TypeRewriteRule.seq(this.fixTypeEverywhere("InjectBedBlockEntityType", this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY), (dynamicops) -> (pair) -> pair), this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(References.CHUNK), (typed) -> {
         Typed<?> typed1 = typed.getTyped(opticfinder);
         Dynamic<?> dynamic = typed1.get(DSL.remainderFinder());
         int j = dynamic.get("xPos").asInt(0);
         int k = dynamic.get("zPos").asInt(0);
         java.util.List<TE> list = Lists.newArrayList(typed1.getOrCreate(opticfinder1));
         java.util.List<? extends Dynamic<?>> list1 = dynamic.get("Sections").asList(Function.identity());

         for(int l = 0; l < list1.size(); ++l) {
            Dynamic<?> dynamic1 = list1.get(l);
            int i1 = dynamic1.get("Y").asInt(0);
            Streams.mapWithIndex(dynamic1.get("Blocks").asIntStream(), (i2, j2) -> {
               if (416 == (i2 & 255) << 4) {
                  int k2 = (int)j2;
                  int l2 = k2 & 15;
                  int i3 = k2 >> 8 & 15;
                  int j3 = k2 >> 4 & 15;
                  Map<Dynamic<?>, Dynamic<?>> map1 = Maps.newHashMap();
                  map1.put(dynamic1.createString("id"), dynamic1.createString("minecraft:bed"));
                  map1.put(dynamic1.createString("x"), dynamic1.createInt(l2 + (j << 4)));
                  map1.put(dynamic1.createString("y"), dynamic1.createInt(i3 + (i1 << 4)));
                  map1.put(dynamic1.createString("z"), dynamic1.createInt(j3 + (k << 4)));
                  map1.put(dynamic1.createString("color"), dynamic1.createShort((short)14));
                  return map1;
               } else {
                  return null;
               }
            }).forEachOrdered((map) -> {
               if (map != null) {
                  list.add(type1.read(dynamic1.createMap(map)).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity.")).getFirst());
               }

            });
         }

         return !list.isEmpty() ? typed.set(opticfinder, typed1.set(opticfinder1, list)) : typed;
      }));
   }
}
