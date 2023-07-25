package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockStateArgument implements ArgumentType<BlockInput> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
   private final HolderLookup<Block> blocks;

   public BlockStateArgument(CommandBuildContext commandbuildcontext) {
      this.blocks = commandbuildcontext.holderLookup(Registries.BLOCK);
   }

   public static BlockStateArgument block(CommandBuildContext commandbuildcontext) {
      return new BlockStateArgument(commandbuildcontext);
   }

   public BlockInput parse(StringReader stringreader) throws CommandSyntaxException {
      BlockStateParser.BlockResult blockstateparser_blockresult = BlockStateParser.parseForBlock(this.blocks, stringreader, true);
      return new BlockInput(blockstateparser_blockresult.blockState(), blockstateparser_blockresult.properties().keySet(), blockstateparser_blockresult.nbt());
   }

   public static BlockInput getBlock(CommandContext<CommandSourceStack> commandcontext, String s) {
      return commandcontext.getArgument(s, BlockInput.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return BlockStateParser.fillSuggestions(this.blocks, suggestionsbuilder, false, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
