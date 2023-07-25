package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityAnchorArgument implements ArgumentType<EntityAnchorArgument.Anchor> {
   private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
   private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType((object) -> Component.translatable("argument.anchor.invalid", object));

   public static EntityAnchorArgument.Anchor getAnchor(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, EntityAnchorArgument.Anchor.class);
   }

   public static EntityAnchorArgument anchor() {
      return new EntityAnchorArgument();
   }

   public EntityAnchorArgument.Anchor parse(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();
      String s = stringreader.readUnquotedString();
      EntityAnchorArgument.Anchor entityanchorargument_anchor = EntityAnchorArgument.Anchor.getByName(s);
      if (entityanchorargument_anchor == null) {
         stringreader.setCursor(i);
         throw ERROR_INVALID.createWithContext(stringreader, s);
      } else {
         return entityanchorargument_anchor;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggest(EntityAnchorArgument.Anchor.BY_NAME.keySet(), suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static enum Anchor {
      FEET("feet", (vec3, entity) -> vec3),
      EYES("eyes", (vec3, entity) -> new Vec3(vec3.x, vec3.y + (double)entity.getEyeHeight(), vec3.z));

      static final Map<String, EntityAnchorArgument.Anchor> BY_NAME = Util.make(Maps.newHashMap(), (hashmap) -> {
         for(EntityAnchorArgument.Anchor entityanchorargument_anchor : values()) {
            hashmap.put(entityanchorargument_anchor.name, entityanchorargument_anchor);
         }

      });
      private final String name;
      private final BiFunction<Vec3, Entity, Vec3> transform;

      private Anchor(String s, BiFunction<Vec3, Entity, Vec3> bifunction) {
         this.name = s;
         this.transform = bifunction;
      }

      @Nullable
      public static EntityAnchorArgument.Anchor getByName(String s) {
         return BY_NAME.get(s);
      }

      public Vec3 apply(Entity entity) {
         return this.transform.apply(entity.position(), entity);
      }

      public Vec3 apply(CommandSourceStack commandsourcestack) {
         Entity entity = commandsourcestack.getEntity();
         return entity == null ? commandsourcestack.getPosition() : this.transform.apply(commandsourcestack.getPosition(), entity);
      }
   }
}
