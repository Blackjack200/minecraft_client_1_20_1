package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T> implements ArgumentType<Holder.Reference<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType((object) -> Component.translatable("entity.not_summonable", object));
   public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType((object, object1) -> Component.translatable("argument.resource.not_found", object, object1));
   public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType((object, object1, object2) -> Component.translatable("argument.resource.invalid_type", object, object1, object2));
   final ResourceKey<? extends Registry<T>> registryKey;
   private final HolderLookup<T> registryLookup;

   public ResourceArgument(CommandBuildContext commandbuildcontext, ResourceKey<? extends Registry<T>> resourcekey) {
      this.registryKey = resourcekey;
      this.registryLookup = commandbuildcontext.holderLookup(resourcekey);
   }

   public static <T> ResourceArgument<T> resource(CommandBuildContext commandbuildcontext, ResourceKey<? extends Registry<T>> resourcekey) {
      return new ResourceArgument<>(commandbuildcontext, resourcekey);
   }

   public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> commandcontext, String s, ResourceKey<Registry<T>> resourcekey) throws CommandSyntaxException {
      Holder.Reference<T> holder_reference = commandcontext.getArgument(s, Holder.Reference.class);
      ResourceKey<?> resourcekey1 = holder_reference.key();
      if (resourcekey1.isFor(resourcekey)) {
         return holder_reference;
      } else {
         throw ERROR_INVALID_RESOURCE_TYPE.create(resourcekey1.location(), resourcekey1.registry(), resourcekey.location());
      }
   }

   public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.ATTRIBUTE);
   }

   public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.CONFIGURED_FEATURE);
   }

   public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.STRUCTURE);
   }

   public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.ENTITY_TYPE);
   }

   public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      Holder.Reference<EntityType<?>> holder_reference = getResource(commandcontext, s, Registries.ENTITY_TYPE);
      if (!holder_reference.value().canSummon()) {
         throw ERROR_NOT_SUMMONABLE_ENTITY.create(holder_reference.key().location().toString());
      } else {
         return holder_reference;
      }
   }

   public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.MOB_EFFECT);
   }

   public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return getResource(commandcontext, s, Registries.ENCHANTMENT);
   }

   public Holder.Reference<T> parse(StringReader stringreader) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
      ResourceKey<T> resourcekey = ResourceKey.create(this.registryKey, resourcelocation);
      return this.registryLookup.get(resourcekey).orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.create(resourcelocation, this.registryKey.location()));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceArgument<T>, ResourceArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceArgument.Info<T>.Template resourceargument_info_template, FriendlyByteBuf friendlybytebuf) {
         friendlybytebuf.writeResourceLocation(resourceargument_info_template.registryKey.location());
      }

      public ResourceArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf friendlybytebuf) {
         ResourceLocation resourcelocation = friendlybytebuf.readResourceLocation();
         return new ResourceArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceArgument.Info<T>.Template resourceargument_info_template, JsonObject jsonobject) {
         jsonobject.addProperty("registry", resourceargument_info_template.registryKey.location().toString());
      }

      public ResourceArgument.Info<T>.Template unpack(ResourceArgument<T> resourceargument) {
         return new ResourceArgument.Info.Template(resourceargument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> resourcekey) {
            this.registryKey = resourcekey;
         }

         public ResourceArgument<T> instantiate(CommandBuildContext commandbuildcontext) {
            return new ResourceArgument<>(commandbuildcontext, this.registryKey);
         }

         public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
