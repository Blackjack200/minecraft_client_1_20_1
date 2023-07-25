package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;

public class EntityMinecartIdentifiersFix extends DataFix {
   private static final List<String> MINECART_BY_ID = Lists.newArrayList("MinecartRideable", "MinecartChest", "MinecartFurnace");

   public EntityMinecartIdentifiersFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(References.ENTITY);
      return this.fixTypeEverywhere("EntityMinecartIdentifiersFix", taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops) -> (pair) -> {
            if (!Objects.equals(pair.getFirst(), "Minecart")) {
               return pair;
            } else {
               Typed<? extends Pair<String, ?>> typed = taggedchoice_taggedchoicetype.point(dynamicops, "Minecart", pair.getSecond()).orElseThrow(IllegalStateException::new);
               Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
               int i = dynamic.get("Type").asInt(0);
               String s;
               if (i > 0 && i < MINECART_BY_ID.size()) {
                  s = MINECART_BY_ID.get(i);
               } else {
                  s = "MinecartRideable";
               }

               return Pair.of(s, typed.write().map((dynamic1) -> taggedchoice_taggedchoicetype1.types().get(s).read(dynamic1)).result().orElseThrow(() -> new IllegalStateException("Could not read the new minecart.")));
            }
         });
   }
}
