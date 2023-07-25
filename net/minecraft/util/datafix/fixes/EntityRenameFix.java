package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;

public abstract class EntityRenameFix extends DataFix {
   protected final String name;

   public EntityRenameFix(String s, Schema schema, boolean flag) {
      super(schema, flag);
      this.name = s;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(References.ENTITY);
      return this.fixTypeEverywhere(this.name, taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops) -> (pair) -> {
            String s = pair.getFirst();
            Type<?> type = taggedchoice_taggedchoicetype.types().get(s);
            Pair<String, Typed<?>> pair1 = this.fix(s, this.getEntity(pair.getSecond(), dynamicops, type));
            Type<?> type1 = taggedchoice_taggedchoicetype1.types().get(pair1.getFirst());
            if (!type1.equals(pair1.getSecond().getType(), true, true)) {
               throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", type1, pair1.getSecond().getType()));
            } else {
               return Pair.of(pair1.getFirst(), pair1.getSecond().getValue());
            }
         });
   }

   private <A> Typed<A> getEntity(Object object, DynamicOps<?> dynamicops, Type<A> type) {
      return new Typed<>(type, dynamicops, (A)object);
   }

   protected abstract Pair<String, Typed<?>> fix(String s, Typed<?> typed);
}
