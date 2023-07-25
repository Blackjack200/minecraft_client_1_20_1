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
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

public class SlotArgument implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "12", "weapon");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType((object) -> Component.translatable("slot.unknown", object));
   private static final Map<String, Integer> SLOTS = Util.make(Maps.newHashMap(), (hashmap) -> {
      for(int i = 0; i < 54; ++i) {
         hashmap.put("container." + i, i);
      }

      for(int j = 0; j < 9; ++j) {
         hashmap.put("hotbar." + j, j);
      }

      for(int k = 0; k < 27; ++k) {
         hashmap.put("inventory." + k, 9 + k);
      }

      for(int l = 0; l < 27; ++l) {
         hashmap.put("enderchest." + l, 200 + l);
      }

      for(int i1 = 0; i1 < 8; ++i1) {
         hashmap.put("villager." + i1, 300 + i1);
      }

      for(int j1 = 0; j1 < 15; ++j1) {
         hashmap.put("horse." + j1, 500 + j1);
      }

      hashmap.put("weapon", EquipmentSlot.MAINHAND.getIndex(98));
      hashmap.put("weapon.mainhand", EquipmentSlot.MAINHAND.getIndex(98));
      hashmap.put("weapon.offhand", EquipmentSlot.OFFHAND.getIndex(98));
      hashmap.put("armor.head", EquipmentSlot.HEAD.getIndex(100));
      hashmap.put("armor.chest", EquipmentSlot.CHEST.getIndex(100));
      hashmap.put("armor.legs", EquipmentSlot.LEGS.getIndex(100));
      hashmap.put("armor.feet", EquipmentSlot.FEET.getIndex(100));
      hashmap.put("horse.saddle", 400);
      hashmap.put("horse.armor", 401);
      hashmap.put("horse.chest", 499);
   });

   public static SlotArgument slot() {
      return new SlotArgument();
   }

   public static int getSlot(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, Integer.class);
   }

   public Integer parse(StringReader stringreader) throws CommandSyntaxException {
      String s = stringreader.readUnquotedString();
      if (!SLOTS.containsKey(s)) {
         throw ERROR_UNKNOWN_SLOT.create(s);
      } else {
         return SLOTS.get(s);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return SharedSuggestionProvider.suggest(SLOTS.keySet(), suggestionsbuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
