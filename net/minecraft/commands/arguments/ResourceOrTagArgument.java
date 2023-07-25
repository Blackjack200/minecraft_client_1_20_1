package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagArgument<T> implements ArgumentType<ResourceOrTagArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   private static final Dynamic2CommandExceptionType ERROR_UNKNOWN_TAG = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("argument.resource_tag.not_found", object, object1));
   private static final Dynamic3CommandExceptionType ERROR_INVALID_TAG_TYPE = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("argument.resource_tag.invalid_type", object, object1, object2));
   private final HolderLookup<T> registryLookup;
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagArgument(CommandBuildContext commandbuildcontext, ResourceKey<? extends Registry<T>> resourcekey) {
      this.registryKey = resourcekey;
      this.registryLookup = commandbuildcontext.holderLookup(resourcekey);
   }

   public static <T> ResourceOrTagArgument<T> resourceOrTag(CommandBuildContext commandbuildcontext, ResourceKey<? extends Registry<T>> resourcekey) {
      return new ResourceOrTagArgument<>(commandbuildcontext, resourcekey);
   }

   public static <T> ResourceOrTagArgument.Result<T> getResourceOrTag(CommandContext<CommandSourceStack> commandcontext, String s, ResourceKey<Registry<T>> resourcekey) throws CommandSyntaxException {
      ResourceOrTagArgument.Result<?> resourceortagargument_result = commandcontext.getArgument(s, ResourceOrTagArgument.Result.class);
      Optional<ResourceOrTagArgument.Result<T>> optional = resourceortagargument_result.cast(resourcekey);
      return optional.orElseThrow(() -> resourceortagargument_result.unwrap().map((holder_reference) -> {
            ResourceKey<?> resourcekey4 = holder_reference.key();
            return ResourceArgument.ERROR_INVALID_RESOURCE_TYPE.create(resourcekey4.location(), resourcekey4.registry(), resourcekey.location());
         }, (holderset_named) -> {
            TagKey<?> tagkey = holderset_named.key();
            return ERROR_INVALID_TAG_TYPE.create(tagkey.location(), tagkey.registry(), resourcekey.location());
         }));
   }

   public ResourceOrTagArgument.Result<T> parse(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '#') {
         int i = stringreader.getCursor();

         try {
            stringreader.skip();
            ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
            TagKey<T> tagkey = TagKey.create(this.registryKey, resourcelocation);
            HolderSet.Named<T> holderset_named = this.registryLookup.get(tagkey).orElseThrow(() -> ERROR_UNKNOWN_TAG.create(resourcelocation, this.registryKey.location()));
            return new ResourceOrTagArgument.TagResult<>(holderset_named);
         } catch (CommandSyntaxException var6) {
            stringreader.setCursor(i);
            throw var6;
         }
      } else {
         ResourceLocation resourcelocation1 = ResourceLocation.read(stringreader);
         ResourceKey<T> resourcekey = ResourceKey.create(this.registryKey, resourcelocation1);
         Holder.Reference<T> holder_reference = this.registryLookup.get(resourcekey).orElseThrow(() -> ResourceArgument.ERROR_UNKNOWN_RESOURCE.create(resourcelocation1, this.registryKey.location()));
         return new ResourceOrTagArgument.ResourceResult<>(holder_reference);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      SharedSuggestionProvider.suggestResource(this.registryLookup.listTagIds().map(TagKey::location), suggestionsbuilder, "#");
      return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagArgument<T>, ResourceOrTagArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceOrTagArgument.Info<T>.Template resourceortagargument_info_template, FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeResourceLocation(resourceortagargument_info_template.registryKey.location());
      }

      public ResourceOrTagArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
         ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
         return new ResourceOrTagArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceOrTagArgument.Info<T>.Template resourceortagargument_info_template, JsonObject jsonobject) {
         jsonobject.addProperty("registry", resourceortagargument_info_template.registryKey.location().toString());
      }

      public ResourceOrTagArgument.Info<T>.Template unpack(ResourceOrTagArgument<T> resourceortagargument) {
         return new ResourceOrTagArgument.Info.Template(resourceortagargument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> resourcekey) {
            this.registryKey = resourcekey;
         }

         public ResourceOrTagArgument<T> instantiate(CommandBuildContext commandbuildcontext) {
            return new ResourceOrTagArgument<>(commandbuildcontext, this.registryKey);
         }

         public ArgumentTypeInfo<ResourceOrTagArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }

   static record ResourceResult<T>(Holder.Reference<T> value) implements ResourceOrTagArgument.Result<T> {
      public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
         return Either.left(this.value);
      }

      public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
         return this.value.key().isFor(resourcekey) ? Optional.of(this) : Optional.empty();
      }

      public boolean test(Holder<T> holder) {
         return holder.equals(this.value);
      }

      public String asPrintable() {
         return this.value.key().location().toString();
      }
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap();

      <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey);

      String asPrintable();
   }

   static record TagResult<T>(HolderSet.Named<T> tag) implements ResourceOrTagArgument.Result<T> {
      public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
         return Either.right(this.tag);
      }

      public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> resourcekey) {
         return this.tag.key().isFor(resourcekey) ? Optional.of(this) : Optional.empty();
      }

      public boolean test(Holder<T> holder) {
         return this.tag.contains(holder);
      }

      public String asPrintable() {
         return "#" + this.tag.key().location();
      }
   }
}
