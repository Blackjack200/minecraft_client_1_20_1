package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.place.feature.invalid", object));
   private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType((object) -> Component.translatable("commands.place.structure.invalid", object));
   private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType((object) -> Component.translatable("commands.place.jigsaw.invalid", object));
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceKeyArgument(ResourceKey<? extends Registry<T>> resourcekey) {
      this.registryKey = resourcekey;
   }

   public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> resourcekey) {
      return new ResourceKeyArgument<>(resourcekey);
   }

   private static <T> ResourceKey<T> getRegistryKey(CommandContext<CommandSourceStack> commandcontext, String s, ResourceKey<Registry<T>> resourcekey, DynamicCommandExceptionType dynamiccommandexceptiontype) throws CommandSyntaxException {
      ResourceKey<?> resourcekey1 = commandcontext.getArgument(s, ResourceKey.class);
      Optional<ResourceKey<T>> optional = resourcekey1.cast(resourcekey);
      return optional.orElseThrow(() -> dynamiccommandexceptiontype.create(resourcekey1));
   }

   private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandcontext, ResourceKey<? extends Registry<T>> resourcekey) {
      return commandcontext.getSource().getServer().registryAccess().registryOrThrow(resourcekey);
   }

   private static <T> Holder.Reference<T> resolveKey(CommandContext<CommandSourceStack> commandcontext, String s, ResourceKey<Registry<T>> resourcekey, DynamicCommandExceptionType dynamiccommandexceptiontype) throws CommandSyntaxException {
      ResourceKey<T> resourcekey1 = getRegistryKey(commandcontext, s, resourcekey, dynamiccommandexceptiontype);
      return getRegistry(commandcontext, resourcekey).getHolder(resourcekey1).orElseThrow(() -> dynamiccommandexceptiontype.create(resourcekey1.location()));
   }

   public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return resolveKey(commandcontext, s, Registries.CONFIGURED_FEATURE, ERROR_INVALID_FEATURE);
   }

   public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return resolveKey(commandcontext, s, Registries.STRUCTURE, ERROR_INVALID_STRUCTURE);
   }

   public static Holder.Reference<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return resolveKey(commandcontext, s, Registries.TEMPLATE_POOL, ERROR_INVALID_TEMPLATE_POOL);
   }

   public ResourceKey<T> parse(StringReader stringreader) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
      return ResourceKey.create(this.registryKey, resourcelocation);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      Object var4 = commandcontext.getSource();
      if (var4 instanceof SharedSuggestionProvider sharedsuggestionprovider) {
         return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, suggestionsbuilder, commandcontext);
      } else {
         return suggestionsbuilder.buildFuture();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceKeyArgument<T>, ResourceKeyArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceKeyArgument.Info<T>.Template resourcekeyargument_info_template, FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeResourceLocation(resourcekeyargument_info_template.registryKey.location());
      }

      public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
         ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
         return new ResourceKeyArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceKeyArgument.Info<T>.Template resourcekeyargument_info_template, JsonObject jsonobject) {
         jsonobject.addProperty("registry", resourcekeyargument_info_template.registryKey.location().toString());
      }

      public ResourceKeyArgument.Info<T>.Template unpack(ResourceKeyArgument<T> resourcekeyargument) {
         return new ResourceKeyArgument.Info.Template(resourcekeyargument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> resourcekey) {
            this.registryKey = resourcekey;
         }

         public ResourceKeyArgument<T> instantiate(CommandBuildContext commandbuildcontext) {
            return new ResourceKeyArgument<>(this.registryKey);
         }

         public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
