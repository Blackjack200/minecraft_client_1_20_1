package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagKeyArgument<T> implements ArgumentType<ResourceOrTagKeyArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagKeyArgument(ResourceKey<? extends Registry<T>> resourcekey) {
      this.registryKey = resourcekey;
   }

   public static <T> ResourceOrTagKeyArgument<T> resourceOrTagKey(ResourceKey<? extends Registry<T>> resourcekey) {
      return new ResourceOrTagKeyArgument<>(resourcekey);
   }

   public static <T> ResourceOrTagKeyArgument.Result<T> getResourceOrTagKey(CommandContext<CommandSourceStack> commandcontext, String s, ResourceKey<Registry<T>> resourcekey, DynamicCommandExceptionType dynamiccommandexceptiontype) throws CommandSyntaxException {
      ResourceOrTagKeyArgument.Result<?> resourceortagkeyargument_result = commandcontext.getArgument(s, ResourceOrTagKeyArgument.Result.class);
      Optional<ResourceOrTagKeyArgument.Result<T>> optional = resourceortagkeyargument_result.cast(resourcekey);
      return optional.orElseThrow(() -> dynamiccommandexceptiontype.create(resourceortagkeyargument_result));
   }

   public ResourceOrTagKeyArgument.Result<T> parse(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '#') {
         int i = stringreader.getCursor();

         try {
            stringreader.skip();
            ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
            return new ResourceOrTagKeyArgument.TagResult<>(TagKey.create(this.registryKey, resourcelocation));
         } catch (CommandSyntaxException var4) {
            stringreader.setCursor(i);
            throw var4;
         }
      } else {
         ResourceLocation resourcelocation1 = ResourceLocation.read(stringreader);
         return new ResourceOrTagKeyArgument.ResourceResult<>(ResourceKey.create(this.registryKey, resourcelocation1));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      Object var4 = commandcontext.getSource();
      if (var4 instanceof SharedSuggestionProvider sharedsuggestionprovider) {
         return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ALL, suggestionsbuilder, commandcontext);
      } else {
         return suggestionsbuilder.buildFuture();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ResourceOrTagKeyArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceOrTagKeyArgument.Info<T>.Template resourceortagkeyargument_info_template, FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeResourceLocation(resourceortagkeyargument_info_template.registryKey.location());
      }

      public ResourceOrTagKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
         ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
         return new ResourceOrTagKeyArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceOrTagKeyArgument.Info<T>.Template resourceortagkeyargument_info_template, JsonObject jsonobject) {
         jsonobject.addProperty("registry", resourceortagkeyargument_info_template.registryKey.location().toString());
      }

      public ResourceOrTagKeyArgument.Info<T>.Template unpack(ResourceOrTagKeyArgument<T> resourceortagkeyargument) {
         return new ResourceOrTagKeyArgument.Info.Template(resourceortagkeyargument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagKeyArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> resourcekey) {
            this.registryKey = resourcekey;
         }

         public ResourceOrTagKeyArgument<T> instantiate(CommandBuildContext commandbuildcontext) {
            return new ResourceOrTagKeyArgument<>(this.registryKey);
         }

         public ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }

   static record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.left(this.key);
      }

      public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
         return this.key.cast(resourcekey).map(ResourceOrTagKeyArgument.ResourceResult::new);
      }

      public boolean test(Holder<T> holder) {
         return holder.is(this.key);
      }

      public String asPrintable() {
         return this.key.location().toString();
      }
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<ResourceKey<T>, TagKey<T>> unwrap();

      <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey);

      String asPrintable();
   }

   static record TagResult<T>(TagKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.right(this.key);
      }

      public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
         return this.key.cast(resourcekey).map(ResourceOrTagKeyArgument.TagResult::new);
      }

      public boolean test(Holder<T> holder) {
         return holder.is(this.key);
      }

      public String asPrintable() {
         return "#" + this.key.location();
      }
   }
}
