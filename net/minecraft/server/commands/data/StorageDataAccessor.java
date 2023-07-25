package net.minecraft.server.commands.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.CommandStorage;

public class StorageDataAccessor implements DataAccessor {
   static final SuggestionProvider<CommandSourceStack> SUGGEST_STORAGE = (commandcontext, suggestionsbuilder) -> SharedSuggestionProvider.suggestResource(getGlobalTags(commandcontext).keys(), suggestionsbuilder);
   public static final Function<String, DataCommands.DataProvider> PROVIDER = (s) -> new DataCommands.DataProvider() {
         public DataAccessor access(CommandContext<CommandSourceStack> commandcontext) {
            return new StorageDataAccessor(StorageDataAccessor.getGlobalTags(commandcontext), ResourceLocationArgument.getId(commandcontext, s));
         }

         public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> argumentbuilder, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> function) {
            return argumentbuilder.then(Commands.literal("storage").then(function.apply(Commands.argument(s, ResourceLocationArgument.id()).suggests(StorageDataAccessor.SUGGEST_STORAGE))));
         }
      };
   private final CommandStorage storage;
   private final ResourceLocation id;

   static CommandStorage getGlobalTags(CommandContext<CommandSourceStack> commandcontext) {
      return commandcontext.getSource().getServer().getCommandStorage();
   }

   StorageDataAccessor(CommandStorage commandstorage, ResourceLocation resourcelocation) {
      this.storage = commandstorage;
      this.id = resourcelocation;
   }

   public void setData(CompoundTag compoundtag) {
      this.storage.set(this.id, compoundtag);
   }

   public CompoundTag getData() {
      return this.storage.get(this.id);
   }

   public Component getModifiedSuccess() {
      return Component.translatable("commands.data.storage.modified", this.id);
   }

   public Component getPrintSuccess(Tag tag) {
      return Component.translatable("commands.data.storage.query", this.id, NbtUtils.toPrettyComponent(tag));
   }

   public Component getPrintSuccess(NbtPathArgument.NbtPath nbtpathargument_nbtpath, double d0, int i) {
      return Component.translatable("commands.data.storage.get", nbtpathargument_nbtpath, this.id, String.format(Locale.ROOT, "%.2f", d0), i);
   }
}
