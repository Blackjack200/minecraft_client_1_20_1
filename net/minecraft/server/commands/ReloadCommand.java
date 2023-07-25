package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class ReloadCommand {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void reloadPacks(Collection<String> collection, CommandSourceStack commandsourcestack) {
      commandsourcestack.getServer().reloadResources(collection).exceptionally((throwable) -> {
         LOGGER.warn("Failed to execute reload", throwable);
         commandsourcestack.sendFailure(Component.translatable("commands.reload.failure"));
         return null;
      });
   }

   private static Collection<String> discoverNewPacks(PackRepository packrepository, WorldData worlddata, Collection<String> collection) {
      packrepository.reload();
      Collection<String> collection1 = Lists.newArrayList(collection);
      Collection<String> collection2 = worlddata.getDataConfiguration().dataPacks().getDisabled();

      for(String s : packrepository.getAvailableIds()) {
         if (!collection2.contains(s) && !collection1.contains(s)) {
            collection1.add(s);
         }
      }

      return collection1;
   }

   public static void register(CommandDispatcher<CommandSourceStack> commanddispatcher) {
      commanddispatcher.register(Commands.literal("reload").requires((commandsourcestack1) -> commandsourcestack1.hasPermission(2)).executes((commandcontext) -> {
         CommandSourceStack commandsourcestack = commandcontext.getSource();
         MinecraftServer minecraftserver = commandsourcestack.getServer();
         PackRepository packrepository = minecraftserver.getPackRepository();
         WorldData worlddata = minecraftserver.getWorldData();
         Collection<String> collection = packrepository.getSelectedIds();
         Collection<String> collection1 = discoverNewPacks(packrepository, worlddata, collection);
         commandsourcestack.sendSuccess(() -> Component.translatable("commands.reload.success"), true);
         reloadPacks(collection1, commandsourcestack);
         return 0;
      }));
   }
}
