package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
   private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
   private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
   public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(DEFAULT_NAME, (commandcontext, suggestionsbuilder) -> commandcontext.getSource().customSuggestion(commandcontext));
   public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = register(new ResourceLocation("all_recipes"), (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggestResource(commandcontext.getSource().getRecipeNames(), suggestionsbuilder));
   public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(new ResourceLocation("available_sounds"), (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggestResource(commandcontext.getSource().getAvailableSounds(), suggestionsbuilder));
   public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(new ResourceLocation("summonable_entities"), (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter((entitytype1) -> entitytype1.isEnabled(commandcontext.getSource().enabledFeatures()) && entitytype1.canSummon()), suggestionsbuilder, EntityType::getKey, (entitytype) -> Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(entitytype)))));

   public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(ResourceLocation resourcelocation, SuggestionProvider<SharedSuggestionProvider> suggestionprovider) {
      if (PROVIDERS_BY_NAME.containsKey(resourcelocation)) {
         throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + resourcelocation);
      } else {
         PROVIDERS_BY_NAME.put(resourcelocation, suggestionprovider);
         return new SuggestionProviders.Wrapper(resourcelocation, suggestionprovider);
      }
   }

   public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation resourcelocation) {
      return PROVIDERS_BY_NAME.getOrDefault(resourcelocation, ASK_SERVER);
   }

   public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> suggestionprovider) {
      return suggestionprovider instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)suggestionprovider).name : DEFAULT_NAME;
   }

   public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> suggestionprovider) {
      return suggestionprovider instanceof SuggestionProviders.Wrapper ? suggestionprovider : ASK_SERVER;
   }

   protected static class Wrapper implements SuggestionProvider<SharedSuggestionProvider> {
      private final SuggestionProvider<SharedSuggestionProvider> delegate;
      final ResourceLocation name;

      public Wrapper(ResourceLocation resourcelocation, SuggestionProvider<SharedSuggestionProvider> suggestionprovider) {
         this.delegate = suggestionprovider;
         this.name = resourcelocation;
      }

      public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> commandcontext, SuggestionsBuilder suggestionsbuilder) throws CommandSyntaxException {
         return this.delegate.getSuggestions(commandcontext, suggestionsbuilder);
      }
   }
}
