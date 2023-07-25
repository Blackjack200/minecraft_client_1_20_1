package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractPoiSectionFix extends DataFix {
   private final String name;

   public AbstractPoiSectionFix(Schema schema, String s) {
      super(schema, false);
      this.name = s;
   }

   protected TypeRewriteRule makeRule() {
      Type<Pair<String, Dynamic<?>>> type = DSL.named(References.POI_CHUNK.typeName(), DSL.remainderType());
      if (!Objects.equals(type, this.getInputSchema().getType(References.POI_CHUNK))) {
         throw new IllegalStateException("Poi type is not what was expected.");
      } else {
         return this.fixTypeEverywhere(this.name, type, (dynamicops) -> (pair) -> pair.mapSecond(this::cap));
      }
   }

   private <T> Dynamic<T> cap(Dynamic<T> dynamic) {
      return dynamic.update("Sections", (dynamic1) -> dynamic1.updateMapValues((pair) -> pair.mapSecond(this::processSection)));
   }

   private Dynamic<?> processSection(Dynamic<?> dynamic) {
      return dynamic.update("Records", this::processSectionRecords);
   }

   private <T> Dynamic<T> processSectionRecords(Dynamic<T> dynamic1) {
      return DataFixUtils.orElse(dynamic1.asStreamOpt().result().map((stream) -> dynamic1.createList(this.processRecords(stream))), dynamic1);
   }

   protected abstract <T> Stream<Dynamic<T>> processRecords(Stream<Dynamic<T>> stream);
}
