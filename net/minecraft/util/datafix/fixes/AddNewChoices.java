package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;

public class AddNewChoices extends DataFix {
   private final String name;
   private final DSL.TypeReference type;

   public AddNewChoices(Schema schema, String s, DSL.TypeReference dsl_typereference) {
      super(schema, true);
      this.name = s;
      this.type = dsl_typereference;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype = this.getInputSchema().findChoiceType(this.type);
      TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype1 = this.getOutputSchema().findChoiceType(this.type);
      return this.cap(this.name, taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1);
   }

   protected final <K> TypeRewriteRule cap(String s, TaggedChoice.TaggedChoiceType<K> taggedchoice_taggedchoicetype, TaggedChoice.TaggedChoiceType<?> taggedchoice_taggedchoicetype1) {
      if (taggedchoice_taggedchoicetype.getKeyType() != taggedchoice_taggedchoicetype1.getKeyType()) {
         throw new IllegalStateException("Could not inject: key type is not the same");
      } else {
         return this.fixTypeEverywhere(s, taggedchoice_taggedchoicetype, taggedchoice_taggedchoicetype1, (dynamicops) -> (pair) -> {
               if (!taggedchoice_taggedchoicetype1.hasType(pair.getFirst())) {
                  throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown type %s in %s ", pair.getFirst(), this.type));
               } else {
                  return pair;
               }
            });
      }
   }
}
