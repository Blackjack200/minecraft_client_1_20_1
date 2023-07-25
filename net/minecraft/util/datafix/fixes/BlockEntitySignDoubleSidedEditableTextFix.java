package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BlockEntitySignDoubleSidedEditableTextFix extends NamedEntityFix {
   public BlockEntitySignDoubleSidedEditableTextFix(Schema schema, String s, String s1) {
      super(schema, false, s, References.BLOCK_ENTITY, s1);
   }

   private static Dynamic<?> fixTag(Dynamic<?> dynamic) {
      String s = "black";
      Dynamic<?> dynamic1 = dynamic.emptyMap();
      dynamic1 = dynamic1.set("messages", getTextList(dynamic, "Text"));
      dynamic1 = dynamic1.set("filtered_messages", getTextList(dynamic, "FilteredText"));
      Optional<? extends Dynamic<?>> optional = dynamic.get("Color").result();
      dynamic1 = dynamic1.set("color", optional.isPresent() ? optional.get() : dynamic1.createString("black"));
      Optional<? extends Dynamic<?>> optional1 = dynamic.get("GlowingText").result();
      dynamic1 = dynamic1.set("has_glowing_text", optional1.isPresent() ? optional1.get() : dynamic1.createBoolean(false));
      Dynamic<?> dynamic2 = dynamic.emptyMap();
      Dynamic<?> dynamic3 = getEmptyTextList(dynamic);
      dynamic2 = dynamic2.set("messages", dynamic3);
      dynamic2 = dynamic2.set("filtered_messages", dynamic3);
      dynamic2 = dynamic2.set("color", dynamic2.createString("black"));
      dynamic2 = dynamic2.set("has_glowing_text", dynamic2.createBoolean(false));
      dynamic = dynamic.set("front_text", dynamic1);
      return dynamic.set("back_text", dynamic2);
   }

   private static <T> Dynamic<T> getTextList(Dynamic<T> dynamic, String s) {
      Dynamic<T> dynamic1 = dynamic.createString(getEmptyComponent());
      return dynamic.createList(Stream.of(dynamic.get(s + "1").result().orElse(dynamic1), dynamic.get(s + "2").result().orElse(dynamic1), dynamic.get(s + "3").result().orElse(dynamic1), dynamic.get(s + "4").result().orElse(dynamic1)));
   }

   private static <T> Dynamic<T> getEmptyTextList(Dynamic<T> dynamic) {
      Dynamic<T> dynamic1 = dynamic.createString(getEmptyComponent());
      return dynamic.createList(Stream.of(dynamic1, dynamic1, dynamic1, dynamic1));
   }

   private static String getEmptyComponent() {
      return Component.Serializer.toJson(CommonComponents.EMPTY);
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), BlockEntitySignDoubleSidedEditableTextFix::fixTag);
   }
}
