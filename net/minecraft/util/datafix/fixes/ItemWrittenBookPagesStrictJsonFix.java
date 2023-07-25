package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class ItemWrittenBookPagesStrictJsonFix extends DataFix {
   public ItemWrittenBookPagesStrictJsonFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public Dynamic<?> fixTag(Dynamic<?> dynamic) {
      return dynamic.update("pages", (dynamic2) -> DataFixUtils.orElse(dynamic2.asStreamOpt().map((stream) -> stream.map((dynamic3) -> {
               if (!dynamic3.asString().result().isPresent()) {
                  return dynamic3;
               } else {
                  String s = dynamic3.asString("");
                  Component component = null;
                  if (!"null".equals(s) && !StringUtils.isEmpty(s)) {
                     if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"' || s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}') {
                        try {
                           component = GsonHelper.fromNullableJson(BlockEntitySignTextStrictJsonFix.GSON, s, Component.class, true);
                           if (component == null) {
                              component = CommonComponents.EMPTY;
                           }
                        } catch (Exception var6) {
                        }

                        if (component == null) {
                           try {
                              component = Component.Serializer.fromJson(s);
                           } catch (Exception var5) {
                           }
                        }

                        if (component == null) {
                           try {
                              component = Component.Serializer.fromJsonLenient(s);
                           } catch (Exception var4) {
                           }
                        }

                        if (component == null) {
                           component = Component.literal(s);
                        }
                     } else {
                        component = Component.literal(s);
                     }
                  } else {
                     component = CommonComponents.EMPTY;
                  }

                  return dynamic3.createString(Component.Serializer.toJson(component));
               }
            })).map(dynamic::createList).result(), dynamic.emptyList()));
   }

   public TypeRewriteRule makeRule() {
      Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> opticfinder = type.findField("tag");
      return this.fixTypeEverywhereTyped("ItemWrittenBookPagesStrictJsonFix", type, (typed) -> typed.updateTyped(opticfinder, (typed1) -> typed1.update(DSL.remainderFinder(), this::fixTag)));
   }
}
