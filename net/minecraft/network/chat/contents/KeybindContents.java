package net.minecraft.network.chat.contents;

import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class KeybindContents implements ComponentContents {
   private final String name;
   @Nullable
   private Supplier<Component> nameResolver;

   public KeybindContents(String s) {
      this.name = s;
   }

   private Component getNestedComponent() {
      if (this.nameResolver == null) {
         this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
      }

      return this.nameResolver.get();
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
      return this.getNestedComponent().visit(formattedtext_contentconsumer);
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
      return this.getNestedComponent().visit(formattedtext_styledcontentconsumer, style);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof KeybindContents) {
            KeybindContents keybindcontents = (KeybindContents)object;
            if (this.name.equals(keybindcontents.name)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public String toString() {
      return "keybind{" + this.name + "}";
   }

   public String getName() {
      return this.name;
   }
}
