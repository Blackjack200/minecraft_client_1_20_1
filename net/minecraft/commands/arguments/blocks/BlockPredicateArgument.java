package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private final HolderLookup<Block> blocks;

   public BlockPredicateArgument(CommandBuildContext commandbuildcontext) {
      this.blocks = commandbuildcontext.holderLookup(Registries.BLOCK);
   }

   public static BlockPredicateArgument blockPredicate(CommandBuildContext commandbuildcontext) {
      return new BlockPredicateArgument(commandbuildcontext);
   }

   public BlockPredicateArgument.Result parse(StringReader stringreader) throws CommandSyntaxException {
      return parse(this.blocks, stringreader);
   }

   public static BlockPredicateArgument.Result parse(HolderLookup<Block> holderlookup, StringReader stringreader) throws CommandSyntaxException {
      return BlockStateParser.parseForTesting(holderlookup, stringreader, true).map((blockstateparser_blockresult) -> new BlockPredicateArgument.BlockPredicate(blockstateparser_blockresult.blockState(), blockstateparser_blockresult.properties().keySet(), blockstateparser_blockresult.nbt()), (blockstateparser_tagresult) -> new BlockPredicateArgument.TagPredicate(blockstateparser_tagresult.tag(), blockstateparser_tagresult.vagueProperties(), blockstateparser_tagresult.nbt()));
   }

   public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> commandcontext, String s) throws CommandSyntaxException {
      return commandcontext.getArgument(s, BlockPredicateArgument.Result.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandcontext, SuggestionsBuilder suggestionsbuilder) {
      return BlockStateParser.fillSuggestions(this.blocks, suggestionsbuilder, true, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static class BlockPredicate implements BlockPredicateArgument.Result {
      private final BlockState state;
      private final Set<Property<?>> properties;
      @Nullable
      private final CompoundTag nbt;

      public BlockPredicate(BlockState blockstate, Set<Property<?>> set, @Nullable CompoundTag compoundtag) {
         this.state = blockstate;
         this.properties = set;
         this.nbt = compoundtag;
      }

      public boolean test(BlockInWorld blockinworld) {
         BlockState blockstate = blockinworld.getState();
         if (!blockstate.is(this.state.getBlock())) {
            return false;
         } else {
            for(Property<?> property : this.properties) {
               if (blockstate.getValue(property) != this.state.getValue(property)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockentity = blockinworld.getEntity();
               return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.saveWithFullMetadata(), true);
            }
         }
      }

      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }

   public interface Result extends Predicate<BlockInWorld> {
      boolean requiresNbt();
   }

   static class TagPredicate implements BlockPredicateArgument.Result {
      private final HolderSet<Block> tag;
      @Nullable
      private final CompoundTag nbt;
      private final Map<String, String> vagueProperties;

      TagPredicate(HolderSet<Block> holderset, Map<String, String> map, @Nullable CompoundTag compoundtag) {
         this.tag = holderset;
         this.vagueProperties = map;
         this.nbt = compoundtag;
      }

      public boolean test(BlockInWorld blockinworld) {
         BlockState blockstate = blockinworld.getState();
         if (!blockstate.is(this.tag)) {
            return false;
         } else {
            for(Map.Entry<String, String> map_entry : this.vagueProperties.entrySet()) {
               Property<?> property = blockstate.getBlock().getStateDefinition().getProperty(map_entry.getKey());
               if (property == null) {
                  return false;
               }

               Comparable<?> comparable = (Comparable)property.getValue(map_entry.getValue()).orElse((Object)null);
               if (comparable == null) {
                  return false;
               }

               if (blockstate.getValue(property) != comparable) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockentity = blockinworld.getEntity();
               return blockentity != null && NbtUtils.compareNbt(this.nbt, blockentity.saveWithFullMetadata(), true);
            }
         }
      }

      public boolean requiresNbt() {
         return this.nbt != null;
      }
   }
}
