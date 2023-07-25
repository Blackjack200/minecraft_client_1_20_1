package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class DimensionArgument implements ArgumentType<ResourceLocation> {
   private static final Collection<String> EXAMPLES = Stream.of(Level.OVERWORLD, Level.NETHER).map((resourcekey) -> resourcekey.location().toString()).collect(Collectors.toList());
   private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("argument.dimension.invalid", object));

   public ResourceLocation parse(StringReader stringreader) throws CommandSyntaxException {
      return ResourceLocation.read(stringreader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return commandcontext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)commandcontext.getSource()).levels().stream().map(ResourceKey::location), suggestionsbuilder) : Suggestions.empty();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static DimensionArgument dimension() {
      return new DimensionArgument();
   }

   public static ServerLevel getDimension(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      ResourceLocation resourcelocation = commandcontext.getArgument(s, ResourceLocation.class);
      ResourceKey<Level> resourcekey = ResourceKey.create(Registries.DIMENSION, resourcelocation);
      ServerLevel serverlevel = commandcontext.getSource().getServer().getLevel(resourcekey);
      if (serverlevel == null) {
         throw ERROR_INVALID_VALUE.create(resourcelocation);
      } else {
         return serverlevel;
      }
   }
}
