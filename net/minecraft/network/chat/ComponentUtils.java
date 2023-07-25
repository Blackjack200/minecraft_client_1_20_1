package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
   public static final String DEFAULT_SEPARATOR_TEXT = ", ";
   public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
   public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

   public static MutableComponent mergeStyles(MutableComponent mutablecomponent, Style style) {
      if (style.isEmpty()) {
         return mutablecomponent;
      } else {
         Style style1 = mutablecomponent.getStyle();
         if (style1.isEmpty()) {
            return mutablecomponent.setStyle(style);
         } else {
            return style1.equals(style) ? mutablecomponent : mutablecomponent.setStyle(style1.applyTo(style));
         }
      }
   }

   public static Optional<MutableComponent> updateForEntity(@Nullable CommandSourceStack commandsourcestack, Optional<Component> optional, @Nullable Entity entity, int i) throws CommandSyntaxException {
      return optional.isPresent() ? Optional.of(updateForEntity(commandsourcestack, optional.get(), entity, i)) : Optional.empty();
   }

   public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandsourcestack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
      if (i > 100) {
         return component.copy();
      } else {
         MutableComponent mutablecomponent = component.getContents().resolve(commandsourcestack, entity, i + 1);

         for(Component component1 : component.getSiblings()) {
            mutablecomponent.append(updateForEntity(commandsourcestack, component1, entity, i + 1));
         }

         return mutablecomponent.withStyle(resolveStyle(commandsourcestack, component.getStyle(), entity, i));
      }
   }

   private static Style resolveStyle(@Nullable CommandSourceStack commandsourcestack, Style style, @Nullable Entity entity, int i) throws CommandSyntaxException {
      HoverEvent hoverevent = style.getHoverEvent();
      if (hoverevent != null) {
         Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
         if (component != null) {
            HoverEvent hoverevent1 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(commandsourcestack, component, entity, i + 1));
            return style.withHoverEvent(hoverevent1);
         }
      }

      return style;
   }

   public static Component getDisplayName(GameProfile gameprofile) {
      if (gameprofile.getName() != null) {
         return Component.literal(gameprofile.getName());
      } else {
         return gameprofile.getId() != null ? Component.literal(gameprofile.getId().toString()) : Component.literal("(unknown)");
      }
   }

   public static Component formatList(Collection<String> collection) {
      return formatAndSortList(collection, (s) -> Component.literal(s).withStyle(ChatFormatting.GREEN));
   }

   public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
      if (collection.isEmpty()) {
         return CommonComponents.EMPTY;
      } else if (collection.size() == 1) {
         return function.apply(collection.iterator().next());
      } else {
         List<T> list = Lists.newArrayList(collection);
         list.sort(Comparable::compareTo);
         return formatList(list, function);
      }
   }

   public static <T> Component formatList(Collection<? extends T> collection, Function<T, Component> function) {
      return formatList(collection, DEFAULT_SEPARATOR, function);
   }

   public static <T> MutableComponent formatList(Collection<? extends T> collection, Optional<? extends Component> optional, Function<T, Component> function) {
      return formatList(collection, DataFixUtils.orElse(optional, DEFAULT_SEPARATOR), function);
   }

   public static Component formatList(Collection<? extends Component> collection, Component component) {
      return formatList(collection, component, Function.identity());
   }

   public static <T> MutableComponent formatList(Collection<? extends T> collection, Component component, Function<T, Component> function) {
      if (collection.isEmpty()) {
         return Component.empty();
      } else if (collection.size() == 1) {
         return function.apply(collection.iterator().next()).copy();
      } else {
         MutableComponent mutablecomponent = Component.empty();
         boolean flag = true;

         for(T object : collection) {
            if (!flag) {
               mutablecomponent.append(component);
            }

            mutablecomponent.append(function.apply(object));
            flag = false;
         }

         return mutablecomponent;
      }
   }

   public static MutableComponent wrapInSquareBrackets(Component component) {
      return Component.translatable("chat.square_brackets", component);
   }

   public static Component fromMessage(Message message) {
      return (Component)(message instanceof Component ? (Component)message : Component.literal(message.getString()));
   }

   public static boolean isTranslationResolvable(@Nullable Component component) {
      if (component != null) {
         ComponentContents s = component.getContents();
         if (s instanceof TranslatableContents) {
            TranslatableContents translatablecontents = (TranslatableContents)s;
            String s = translatablecontents.getKey();
            String s1 = translatablecontents.getFallback();
            return s1 != null || Language.getInstance().has(s);
         }
      }

      return true;
   }

   public static MutableComponent copyOnClickText(String s) {
      return wrapInSquareBrackets(Component.literal(s).withStyle((style) -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, s)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))).withInsertion(s)));
   }
}
