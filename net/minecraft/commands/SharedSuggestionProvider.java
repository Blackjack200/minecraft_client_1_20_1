package net.minecraft.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;

public interface SharedSuggestionProvider {
   Collection<String> getOnlinePlayerNames();

   default Collection<String> getCustomTabSugggestions() {
      return this.getOnlinePlayerNames();
   }

   default Collection<String> getSelectedEntities() {
      return Collections.emptyList();
   }

   Collection<String> getAllTeams();

   Stream<ResourceLocation> getAvailableSounds();

   Stream<ResourceLocation> getRecipeNames();

   CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandcontext);

   default Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
      return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
   }

   default Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
      return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
   }

   Set<ResourceKey<Level>> levels();

   RegistryAccess registryAccess();

   FeatureFlagSet enabledFeatures();

   default void suggestRegistryElements(Registry<?> registry, SharedSuggestionProvider.ElementSuggestionType sharedsuggestionprovider_elementsuggestiontype, SuggestionsBuilder suggestionsbuilder) {
      if (sharedsuggestionprovider_elementsuggestiontype.shouldSuggestTags()) {
         suggestResource(registry.getTagNames().map(TagKey::location), suggestionsbuilder, "#");
      }

      if (sharedsuggestionprovider_elementsuggestiontype.shouldSuggestElements()) {
         suggestResource(registry.keySet(), suggestionsbuilder);
      }

   }

   CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourcekey, SharedSuggestionProvider.ElementSuggestionType sharedsuggestionprovider_elementsuggestiontype, SuggestionsBuilder suggestionsbuilder, CommandContext<?> commandcontext);

   boolean hasPermission(int i);

   static <T> void filterResources(Iterable<T> iterable, String s, Function<T, ResourceLocation> function, Consumer<T> consumer) {
      boolean flag = s.indexOf(58) > -1;

      for(T object : iterable) {
         ResourceLocation resourcelocation = function.apply(object);
         if (flag) {
            String s1 = resourcelocation.toString();
            if (matchesSubStr(s, s1)) {
               consumer.accept(object);
            }
         } else if (matchesSubStr(s, resourcelocation.getNamespace()) || resourcelocation.getNamespace().equals("minecraft") && matchesSubStr(s, resourcelocation.getPath())) {
            consumer.accept(object);
         }
      }

   }

   static <T> void filterResources(Iterable<T> iterable, String s, String s1, Function<T, ResourceLocation> function, Consumer<T> consumer) {
      if (s.isEmpty()) {
         iterable.forEach(consumer);
      } else {
         String s2 = Strings.commonPrefix(s, s1);
         if (!s2.isEmpty()) {
            String s3 = s.substring(s2.length());
            filterResources(iterable, s3, function, consumer);
         }
      }

   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> iterable, SuggestionsBuilder suggestionsbuilder, String s) {
      String s1 = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(iterable, s1, s, (resourcelocation1) -> resourcelocation1, (resourcelocation) -> suggestionsbuilder.suggest(s + resourcelocation));
      return suggestionsbuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> stream, SuggestionsBuilder suggestionsbuilder, String s) {
      return suggestResource(stream::iterator, suggestionsbuilder, s);
   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> iterable, SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(iterable, s, (resourcelocation1) -> resourcelocation1, (resourcelocation) -> suggestionsbuilder.suggest(resourcelocation.toString()));
      return suggestionsbuilder.buildFuture();
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> iterable, SuggestionsBuilder suggestionsbuilder, Function<T, ResourceLocation> function, Function<T, Message> function1) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(iterable, s, function, (object) -> suggestionsbuilder.suggest(function.apply(object).toString(), function1.apply(object)));
      return suggestionsbuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> stream, SuggestionsBuilder suggestionsbuilder) {
      return suggestResource(stream::iterator, suggestionsbuilder);
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> stream, SuggestionsBuilder suggestionsbuilder, Function<T, ResourceLocation> function, Function<T, Message> function1) {
      return suggestResource(stream::iterator, suggestionsbuilder, function, function1);
   }

   static CompletableFuture<Suggestions> suggestCoordinates(String s, Collection<SharedSuggestionProvider.TextCoordinates> collection, SuggestionsBuilder suggestionsbuilder, Predicate<String> predicate) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(s)) {
         for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider_textcoordinates : collection) {
            String s1 = sharedsuggestionprovider_textcoordinates.x + " " + sharedsuggestionprovider_textcoordinates.y + " " + sharedsuggestionprovider_textcoordinates.z;
            if (predicate.test(s1)) {
               list.add(sharedsuggestionprovider_textcoordinates.x);
               list.add(sharedsuggestionprovider_textcoordinates.x + " " + sharedsuggestionprovider_textcoordinates.y);
               list.add(s1);
            }
         }
      } else {
         String[] astring = s.split(" ");
         if (astring.length == 1) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider_textcoordinates1 : collection) {
               String s2 = astring[0] + " " + sharedsuggestionprovider_textcoordinates1.y + " " + sharedsuggestionprovider_textcoordinates1.z;
               if (predicate.test(s2)) {
                  list.add(astring[0] + " " + sharedsuggestionprovider_textcoordinates1.y);
                  list.add(s2);
               }
            }
         } else if (astring.length == 2) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider_textcoordinates2 : collection) {
               String s3 = astring[0] + " " + astring[1] + " " + sharedsuggestionprovider_textcoordinates2.z;
               if (predicate.test(s3)) {
                  list.add(s3);
               }
            }
         }
      }

      return suggest(list, suggestionsbuilder);
   }

   static CompletableFuture<Suggestions> suggest2DCoordinates(String s, Collection<SharedSuggestionProvider.TextCoordinates> collection, SuggestionsBuilder suggestionsbuilder, Predicate<String> predicate) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(s)) {
         for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider_textcoordinates : collection) {
            String s1 = sharedsuggestionprovider_textcoordinates.x + " " + sharedsuggestionprovider_textcoordinates.z;
            if (predicate.test(s1)) {
               list.add(sharedsuggestionprovider_textcoordinates.x);
               list.add(s1);
            }
         }
      } else {
         String[] astring = s.split(" ");
         if (astring.length == 1) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider_textcoordinates1 : collection) {
               String s2 = astring[0] + " " + sharedsuggestionprovider_textcoordinates1.z;
               if (predicate.test(s2)) {
                  list.add(s2);
               }
            }
         }
      }

      return suggest(list, suggestionsbuilder);
   }

   static CompletableFuture<Suggestions> suggest(Iterable<String> iterable, SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : iterable) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            suggestionsbuilder.suggest(s1);
         }
      }

      return suggestionsbuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(Stream<String> stream, SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);
      stream.filter((s2) -> matchesSubStr(s, s2.toLowerCase(Locale.ROOT))).forEach(suggestionsbuilder::suggest);
      return suggestionsbuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(String[] astring, SuggestionsBuilder suggestionsbuilder) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : astring) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            suggestionsbuilder.suggest(s1);
         }
      }

      return suggestionsbuilder.buildFuture();
   }

   static <T> CompletableFuture<Suggestions> suggest(Iterable<T> iterable, SuggestionsBuilder suggestionsbuilder, Function<T, String> function, Function<T, Message> function1) {
      String s = suggestionsbuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(T object : iterable) {
         String s1 = function.apply(object);
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            suggestionsbuilder.suggest(s1, function1.apply(object));
         }
      }

      return suggestionsbuilder.buildFuture();
   }

   static boolean matchesSubStr(String s, String s1) {
      for(int i = 0; !s1.startsWith(s, i); ++i) {
         i = s1.indexOf(95, i);
         if (i < 0) {
            return false;
         }
      }

      return true;
   }

   public static enum ElementSuggestionType {
      TAGS,
      ELEMENTS,
      ALL;

      public boolean shouldSuggestTags() {
         return this == TAGS || this == ALL;
      }

      public boolean shouldSuggestElements() {
         return this == ELEMENTS || this == ALL;
      }
   }

   public static class TextCoordinates {
      public static final SharedSuggestionProvider.TextCoordinates DEFAULT_LOCAL = new SharedSuggestionProvider.TextCoordinates("^", "^", "^");
      public static final SharedSuggestionProvider.TextCoordinates DEFAULT_GLOBAL = new SharedSuggestionProvider.TextCoordinates("~", "~", "~");
      public final String x;
      public final String y;
      public final String z;

      public TextCoordinates(String s, String s1, String s2) {
         this.x = s;
         this.y = s1;
         this.z = s2;
      }
   }
}
