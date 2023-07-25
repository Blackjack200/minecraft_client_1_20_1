package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument implements ArgumentType<ParticleOptions> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle with options");
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType((object) -> Component.translatable("particle.notFound", object));
   private final HolderLookup<ParticleType<?>> particles;

   public ParticleArgument(CommandBuildContext commandbuildcontext) {
      this.particles = commandbuildcontext.holderLookup(Registries.PARTICLE_TYPE);
   }

   public static ParticleArgument particle(CommandBuildContext commandbuildcontext) {
      return new ParticleArgument(commandbuildcontext);
   }

   public static ParticleOptions getParticle(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, ParticleOptions.class);
   }

   public ParticleOptions parse(StringReader stringreader) throws CommandSyntaxException {
      return readParticle(stringreader, this.particles);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static ParticleOptions readParticle(StringReader stringreader, HolderLookup<ParticleType<?>> holderlookup) throws CommandSyntaxException {
      ParticleType<?> particletype = readParticleType(stringreader, holderlookup);
      return readParticle(stringreader, particletype);
   }

   private static ParticleType<?> readParticleType(StringReader stringreader, HolderLookup<ParticleType<?>> holderlookup) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
      ResourceKey<ParticleType<?>> resourcekey = ResourceKey.create(Registries.PARTICLE_TYPE, resourcelocation);
      return holderlookup.get(resourcekey).orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.create(resourcelocation)).value();
   }

   private static <T extends ParticleOptions> T readParticle(StringReader stringreader, ParticleType<T> particletype) throws CommandSyntaxException {
      return particletype.getDeserializer().fromCommand(particletype, stringreader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggestResource(this.particles.listElementIds().map(ResourceKey::location), suggestionsbuilder);
   }
}
