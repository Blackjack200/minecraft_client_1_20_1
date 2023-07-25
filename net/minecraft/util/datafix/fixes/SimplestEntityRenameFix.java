package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class SimplestEntityRenameFix extends DataFix {
   private final String name;

   public SimplestEntityRenameFix(String s, Schema schema, boolean flag) {
      super(schema, flag);
      this.name = s;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(References.ENTITY);
      TaggedChoice.TaggedChoiceType<String> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(References.ENTITY);
      Type<Pair<String, String>> type = DSL.named(References.ENTITY_NAME.typeName(), NamespacedSchema.namespacedString());
      if (!Objects.equals(this.getOutputSchema().getType(References.ENTITY_NAME), type)) {
         throw new IllegalStateException("Entity name type is not what was expected.");
      } else {
         return TypeRewriteRule.seq(this.fixTypeEverywhere(this.name, taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops1) -> (pair1) -> pair1.mapFirst((s) -> {
                  String s1 = this.rename(s);
                  Type<?> type1 = taggedchoice_taggedchoicetype.types().get(s);
                  Type<?> type2 = taggedchoice_taggedchoicetype1.types().get(s1);
                  if (!type2.equals(type1, true, true)) {
                     throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", type2, type1));
                  } else {
                     return s1;
                  }
               })), this.fixTypeEverywhere(this.name + " for entity name", type, (dynamicops) -> (pair) -> pair.mapSecond(this::rename)));
      }
   }

   protected abstract String rename(String s);
}
