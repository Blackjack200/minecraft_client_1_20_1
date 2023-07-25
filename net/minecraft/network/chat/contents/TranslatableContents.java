package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {
   public static final Object[] NO_ARGS = new Object[0];
   private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
   private static final FormattedText TEXT_NULL = FormattedText.of("null");
   private final String key;
   @Nullable
   private final String fallback;
   private final Object[] args;
   @Nullable
   private Language decomposedWith;
   private List<FormattedText> decomposedParts = ImmutableList.of();
   private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslatableContents(String s, @Nullable String s1, Object[] aobject) {
      this.key = s;
      this.fallback = s1;
      this.args = aobject;
   }

   private void decompose() {
      Language language = Language.getInstance();
      if (language != this.decomposedWith) {
         this.decomposedWith = language;
         String s = this.fallback != null ? language.getOrDefault(this.key, this.fallback) : language.getOrDefault(this.key);

         try {
            ImmutableList.Builder<FormattedText> immutablelist_builder = ImmutableList.builder();
            this.decomposeTemplate(s, immutablelist_builder::add);
            this.decomposedParts = immutablelist_builder.build();
         } catch (TranslatableFormatException var4) {
            this.decomposedParts = ImmutableList.of(FormattedText.of(s));
         }

      }
   }

   private void decomposeTemplate(String s, Consumer<FormattedText> consumer) {
      Matcher matcher = FORMAT_PATTERN.matcher(s);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            if (k > j) {
               String s1 = s.substring(j, k);
               if (s1.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               consumer.accept(FormattedText.of(s1));
            }

            String s2 = matcher.group(2);
            String s3 = s.substring(k, l);
            if ("%".equals(s2) && "%%".equals(s3)) {
               consumer.accept(TEXT_PERCENT);
            } else {
               if (!"s".equals(s2)) {
                  throw new TranslatableFormatException(this, "Unsupported format: '" + s3 + "'");
               }

               String s4 = matcher.group(1);
               int i1 = s4 != null ? Integer.parseInt(s4) - 1 : i++;
               consumer.accept(this.getArgument(i1));
            }
         }

         if (j < s.length()) {
            String s5 = s.substring(j);
            if (s5.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            consumer.accept(FormattedText.of(s5));
         }

      } catch (IllegalArgumentException var12) {
         throw new TranslatableFormatException(this, var12);
      }
   }

   private FormattedText getArgument(int i) {
      if (i >= 0 && i < this.args.length) {
         Object object = this.args[i];
         if (object instanceof Component) {
            return (Component)object;
         } else {
            return object == null ? TEXT_NULL : FormattedText.of(object.toString());
         }
      } else {
         throw new TranslatableFormatException(this, i);
      }
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
      this.decompose();

      for(FormattedText formattedtext : this.decomposedParts) {
         Optional<T> optional = formattedtext.visit(formattedtext_styledcontentconsumer, style);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
      this.decompose();

      for(FormattedText formattedtext : this.decomposedParts) {
         Optional<T> optional = formattedtext.visit(formattedtext_contentconsumer);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public MutableComponent resolve(@Nullable CommandSourceStack commandsourcestack, @Nullable Entity entity, int i) throws CommandSyntaxException {
      Object[] aobject = new Object[this.args.length];

      for(int j = 0; j < aobject.length; ++j) {
         Object object = this.args[j];
         if (object instanceof Component) {
            aobject[j] = ComponentUtils.updateForEntity(commandsourcestack, (Component)object, entity, i);
         } else {
            aobject[j] = object;
         }
      }

      return MutableComponent.create(new TranslatableContents(this.key, this.fallback, aobject));
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof TranslatableContents) {
            TranslatableContents translatablecontents = (TranslatableContents)object;
            if (Objects.equals(this.key, translatablecontents.key) && Objects.equals(this.fallback, translatablecontents.fallback) && Arrays.equals(this.args, translatablecontents.args)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      int i = Objects.hashCode(this.key);
      i = 31 * i + Objects.hashCode(this.fallback);
      return 31 * i + Arrays.hashCode(this.args);
   }

   public String toString() {
      return "translation{key='" + this.key + "'" + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
   }

   public String getKey() {
      return this.key;
   }

   @Nullable
   public String getFallback() {
      return this.fallback;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
