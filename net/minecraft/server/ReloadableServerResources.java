package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.slf4j.Logger;

public class ReloadableServerResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
   private final CommandBuildContext.Configurable commandBuildContext;
   private final Commands commands;
   private final RecipeManager recipes = new RecipeManager();
   private final TagManager tagManager;
   private final LootDataManager lootData = new LootDataManager();
   private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.lootData);
   private final ServerFunctionLibrary functionLibrary;

   public ReloadableServerResources(RegistryAccess.Frozen registryaccess_frozen, FeatureFlagSet featureflagset, Commands.CommandSelection commands_commandselection, int i) {
      this.tagManager = new TagManager(registryaccess_frozen);
      this.commandBuildContext = CommandBuildContext.configurable(registryaccess_frozen, featureflagset);
      this.commands = new Commands(commands_commandselection, this.commandBuildContext);
      this.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.CREATE_NEW);
      this.functionLibrary = new ServerFunctionLibrary(i, this.commands.getDispatcher());
   }

   public ServerFunctionLibrary getFunctionLibrary() {
      return this.functionLibrary;
   }

   public LootDataManager getLootData() {
      return this.lootData;
   }

   public RecipeManager getRecipeManager() {
      return this.recipes;
   }

   public Commands getCommands() {
      return this.commands;
   }

   public ServerAdvancementManager getAdvancements() {
      return this.advancements;
   }

   public List<PreparableReloadListener> listeners() {
      return List.of(this.tagManager, this.lootData, this.recipes, this.functionLibrary, this.advancements);
   }

   public static CompletableFuture<ReloadableServerResources> loadResources(ResourceManager resourcemanager, RegistryAccess.Frozen registryaccess_frozen, FeatureFlagSet featureflagset, Commands.CommandSelection commands_commandselection, int i, Executor executor, Executor executor1) {
      ReloadableServerResources reloadableserverresources = new ReloadableServerResources(registryaccess_frozen, featureflagset, commands_commandselection, i);
      return SimpleReloadInstance.create(resourcemanager, reloadableserverresources.listeners(), executor, executor1, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()).done().whenComplete((object1, throwable) -> reloadableserverresources.commandBuildContext.missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy.FAIL)).thenApply((object) -> reloadableserverresources);
   }

   public void updateRegistryTags(RegistryAccess registryaccess) {
      this.tagManager.getResult().forEach((tagmanager_loadresult) -> updateRegistryTags(registryaccess, tagmanager_loadresult));
      Blocks.rebuildCache();
   }

   private static <T> void updateRegistryTags(RegistryAccess registryaccess, TagManager.LoadResult<T> tagmanager_loadresult) {
      ResourceKey<? extends Registry<T>> resourcekey = tagmanager_loadresult.key();
      Map<TagKey<T>, List<Holder<T>>> map = tagmanager_loadresult.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap((map_entry1) -> TagKey.create(resourcekey, map_entry1.getKey()), (map_entry) -> List.copyOf(map_entry.getValue())));
      registryaccess.registryOrThrow(resourcekey).bindTags(map);
   }
}
