package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChunkToProtochunkFix extends DataFix {
   private static final int NUM_SECTIONS = 16;

   public ChunkToProtochunkFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      return this.writeFixAndRead("ChunkToProtoChunkFix", this.getInputSchema().getType(References.CHUNK), this.getOutputSchema().getType(References.CHUNK), (dynamic) -> dynamic.update("Level", ChunkToProtochunkFix::fixChunkData));
   }

   private static <T> Dynamic<T> fixChunkData(Dynamic<T> dynamic) {
      boolean flag = dynamic.get("TerrainPopulated").asBoolean(false);
      boolean flag1 = dynamic.get("LightPopulated").asNumber().result().isEmpty() || dynamic.get("LightPopulated").asBoolean(false);
      String s;
      if (flag) {
         if (flag1) {
            s = "mobs_spawned";
         } else {
            s = "decorated";
         }
      } else {
         s = "carved";
      }

      return repackTicks(repackBiomes(dynamic)).set("Status", dynamic.createString(s)).set("hasLegacyStructureData", dynamic.createBoolean(true));
   }

   private static <T> Dynamic<T> repackBiomes(Dynamic<T> dynamic) {
      return dynamic.update("Biomes", (dynamic2) -> DataFixUtils.orElse(dynamic2.asByteBufferOpt().result().map((bytebuffer) -> {
            int[] aint = new int[256];

            for(int i = 0; i < aint.length; ++i) {
               if (i < bytebuffer.capacity()) {
                  aint[i] = bytebuffer.get(i) & 255;
               }
            }

            return dynamic.createIntList(Arrays.stream(aint));
         }), dynamic2));
   }

   private static <T> Dynamic<T> repackTicks(Dynamic<T> dynamic) {
      return DataFixUtils.orElse(dynamic.get("TileTicks").asStreamOpt().result().map((stream) -> {
         List<ShortList> list = IntStream.range(0, 16).mapToObj((i1) -> new ShortArrayList()).collect(Collectors.toList());
         stream.forEach((dynamic4) -> {
            int j = dynamic4.get("x").asInt(0);
            int k = dynamic4.get("y").asInt(0);
            int l = dynamic4.get("z").asInt(0);
            short short0 = packOffsetCoordinates(j, k, l);
            list.get(k >> 4).add(short0);
         });
         return dynamic.remove("TileTicks").set("ToBeTicked", dynamic.createList(list.stream().map((shortlist) -> dynamic.createList(shortlist.intStream().mapToObj((i) -> dynamic.createShort((short)i))))));
      }), dynamic);
   }

   private static short packOffsetCoordinates(int i, int j, int k) {
      return (short)(i & 15 | (j & 15) << 4 | (k & 15) << 8);
   }
}
