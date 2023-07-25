package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter LISTER = new FileToIdConverter("functions", ".mcfunction");
   private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
   private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions");
   private volatile Map<ResourceLocation, Collection<CommandFunction>> tags = Map.of();
   private final int functionCompilationLevel;
   private final CommandDispatcher<CommandSourceStack> dispatcher;

   public Optional<CommandFunction> getFunction(ResourceLocation resourcelocation) {
      return Optional.ofNullable(this.functions.get(resourcelocation));
   }

   public Map<ResourceLocation, CommandFunction> getFunctions() {
      return this.functions;
   }

   public Collection<CommandFunction> getTag(ResourceLocation resourcelocation) {
      return this.tags.getOrDefault(resourcelocation, List.of());
   }

   public Iterable<ResourceLocation> getAvailableTags() {
      return this.tags.keySet();
   }

   public ServerFunctionLibrary(int i, CommandDispatcher<CommandSourceStack> commanddispatcher) {
      this.functionCompilationLevel = i;
      this.dispatcher = commanddispatcher;
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      CompletableFuture<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> completablefuture = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(resourcemanager), executor);
      CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> completablefuture1 = CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourcemanager), executor).thenCompose((map1) -> {
         Map<ResourceLocation, CompletableFuture<CommandFunction>> map2 = Maps.newHashMap();
         CommandSourceStack commandsourcestack = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, (ServerLevel)null, this.functionCompilationLevel, "", CommonComponents.EMPTY, (MinecraftServer)null, (Entity)null);

         for(Map.Entry<ResourceLocation, Resource> map_entry : map1.entrySet()) {
            ResourceLocation resourcelocation2 = map_entry.getKey();
            ResourceLocation resourcelocation3 = LISTER.fileToId(resourcelocation2);
            map2.put(resourcelocation3, CompletableFuture.supplyAsync(() -> {
               List<String> list = readLines(map_entry.getValue());
               return CommandFunction.fromLines(resourcelocation3, this.dispatcher, commandsourcestack, list);
            }, executor));
         }

         CompletableFuture<?>[] acompletablefuture = map2.values().toArray(new CompletableFuture[0]);
         return CompletableFuture.allOf(acompletablefuture).handle((ovoid, throwable1) -> map2);
      });
      return completablefuture.thenCombine(completablefuture1, Pair::of).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((pair) -> {
         Map<ResourceLocation, CompletableFuture<CommandFunction>> map = (Map)pair.getSecond();
         ImmutableMap.Builder<ResourceLocation, CommandFunction> immutablemap_builder = ImmutableMap.builder();
         map.forEach((resourcelocation, completablefuture2) -> completablefuture2.handle((commandfunction, throwable) -> {
               if (throwable != null) {
                  LOGGER.error("Failed to load function {}", resourcelocation, throwable);
               } else {
                  immutablemap_builder.put(resourcelocation, commandfunction);
               }

               return null;
            }).join());
         this.functions = immutablemap_builder.build();
         this.tags = this.tagsLoader.build((Map)pair.getFirst());
      }, executor1);
   }

   private static List<String> readLines(Resource resource) {
      try {
         BufferedReader bufferedreader = resource.openAsReader();

         List var2;
         try {
            var2 = bufferedreader.lines().toList();
         } catch (Throwable var5) {
            if (bufferedreader != null) {
               try {
                  bufferedreader.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (bufferedreader != null) {
            bufferedreader.close();
         }

         return var2;
      } catch (IOException var6) {
         throw new CompletionException(var6);
      }
   }
}
