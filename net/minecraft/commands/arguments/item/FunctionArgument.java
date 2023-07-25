package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FunctionArgument implements ArgumentType<FunctionArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.function.tag.unknown", object));
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType((object) -> Component.translatable("arguments.function.unknown", object));

   public static FunctionArgument functions() {
      return new FunctionArgument();
   }

   public FunctionArgument.Result parse(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '#') {
         stringreader.skip();
         final ResourceLocation resourcelocation = ResourceLocation.read(stringreader);
         return new FunctionArgument.Result() {
            public Collection<CommandFunction> create(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
               return FunctionArgument.getFunctionTag(commandcontext, resourcelocation);
            }

            public Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
               return Pair.of(resourcelocation, Either.right(FunctionArgument.getFunctionTag(commandcontext, resourcelocation)));
            }
         };
      } else {
         final ResourceLocation resourcelocation1 = ResourceLocation.read(stringreader);
         return new FunctionArgument.Result() {
            public Collection<CommandFunction> create(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
               return Collections.singleton(FunctionArgument.getFunction(commandcontext, resourcelocation1));
            }

            public Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException {
               return Pair.of(resourcelocation1, Either.left(FunctionArgument.getFunction(commandcontext, resourcelocation1)));
            }
         };
      }
   }

   static CommandFunction getFunction(CommandContext<CommandSourceStack> commandcontext, ResourceLocation resourcelocation) throws CommandSyntaxException {
      return commandcontext.getSource().getServer().getFunctions().get(resourcelocation).orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create(resourcelocation.toString()));
   }

   static Collection<CommandFunction> getFunctionTag(CommandContext<CommandSourceStack> commandcontext, ResourceLocation resourcelocation) throws CommandSyntaxException {
      Collection<CommandFunction> collection = commandcontext.getSource().getServer().getFunctions().getTag(resourcelocation);
      if (collection == null) {
         throw ERROR_UNKNOWN_TAG.create(resourcelocation.toString());
      } else {
         return collection;
      }
   }

   public static Collection<CommandFunction> getFunctions(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return commandcontext.getArgument(s, FunctionArgument.Result.class).create(commandcontext);
   }

   public static Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> getFunctionOrTag(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return commandcontext.getArgument(s, FunctionArgument.Result.class).unwrap(commandcontext);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public interface Result {
      Collection<CommandFunction> create(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException;

      Pair<ResourceLocation, Either<CommandFunction, Collection<CommandFunction>>> unwrap(CommandContext<CommandSourceStack> commandcontext) throws CommandSyntaxException;
   }
}
