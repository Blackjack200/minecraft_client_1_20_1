package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CommandsReport implements DataProvider {
   private final PackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public CommandsReport(PackOutput packoutput, CompletableFuture<HolderLookup.Provider> completablefuture) {
      this.output = packoutput;
      this.registries = completablefuture;
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
      return this.registries.thenCompose((holderlookup_provider) -> {
         CommandDispatcher<CommandSourceStack> commanddispatcher = (new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext(holderlookup_provider))).getDispatcher();
         return DataProvider.saveStable(cachedoutput, ArgumentUtils.serializeNodeToJson(commanddispatcher, commanddispatcher.getRoot()), path);
      });
   }

   public final String getName() {
      return "Command Syntax";
   }
}
