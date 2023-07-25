package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;

public class MutableComponent implements Component {
   private final ComponentContents contents;
   private final List<Component> siblings;
   private Style style;
   private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
   @Nullable
   private Language decomposedWith;

   MutableComponent(ComponentContents componentcontents, List<Component> list, Style style) {
      this.contents = componentcontents;
      this.siblings = list;
      this.style = style;
   }

   public static MutableComponent create(ComponentContents componentcontents) {
      return new MutableComponent(componentcontents, Lists.newArrayList(), Style.EMPTY);
   }

   public ComponentContents getContents() {
      return this.contents;
   }

   public List<Component> getSiblings() {
      return this.siblings;
   }

   public MutableComponent setStyle(Style style) {
      this.style = style;
      return this;
   }

   public Style getStyle() {
      return this.style;
   }

   public MutableComponent append(String s) {
      return this.append(Component.literal(s));
   }

   public MutableComponent append(Component component) {
      this.siblings.add(component);
      return this;
   }

   public MutableComponent withStyle(UnaryOperator<Style> unaryoperator) {
      this.setStyle(unaryoperator.apply(this.getStyle()));
      return this;
   }

   public MutableComponent withStyle(Style style) {
      this.setStyle(style.applyTo(this.getStyle()));
      return this;
   }

   public MutableComponent withStyle(ChatFormatting... achatformatting) {
      this.setStyle(this.getStyle().applyFormats(achatformatting));
      return this;
   }

   public MutableComponent withStyle(ChatFormatting chatformatting) {
      this.setStyle(this.getStyle().applyFormat(chatformatting));
      return this;
   }

   public FormattedCharSequence getVisualOrderText() {
      Language language = Language.getInstance();
      if (this.decomposedWith != language) {
         this.visualOrderText = language.getVisualOrder(this);
         this.decomposedWith = language;
      }

      return this.visualOrderText;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof MutableComponent)) {
         return false;
      } else {
         MutableComponent mutablecomponent = (MutableComponent)object;
         return this.contents.equals(mutablecomponent.contents) && this.style.equals(mutablecomponent.style) && this.siblings.equals(mutablecomponent.siblings);
      }
   }

   public int hashCode() {
      return Objects.hash(this.contents, this.style, this.siblings);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder(this.contents.toString());
      boolean flag = !this.style.isEmpty();
      boolean flag1 = !this.siblings.isEmpty();
      if (flag || flag1) {
         stringbuilder.append('[');
         if (flag) {
            stringbuilder.append("style=");
            stringbuilder.append((Object)this.style);
         }

         if (flag && flag1) {
            stringbuilder.append(", ");
         }

         if (flag1) {
            stringbuilder.append("siblings=");
            stringbuilder.append((Object)this.siblings);
         }

         stringbuilder.append(']');
      }

      return stringbuilder.toString();
   }
}
