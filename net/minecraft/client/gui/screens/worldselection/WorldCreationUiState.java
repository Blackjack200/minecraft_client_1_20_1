package net.minecraft.client.gui.screens.worldselection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldCreationUiState {
   private static final Component DEFAULT_WORLD_NAME = Component.translatable("selectWorld.newWorld");
   private final List<Consumer<WorldCreationUiState>> listeners = new ArrayList<>();
   private String name = DEFAULT_WORLD_NAME.getString();
   private WorldCreationUiState.SelectedGameMode gameMode = WorldCreationUiState.SelectedGameMode.SURVIVAL;
   private Difficulty difficulty = Difficulty.NORMAL;
   @Nullable
   private Boolean allowCheats;
   private String seed;
   private boolean generateStructures;
   private boolean bonusChest;
   private final Path savesFolder;
   private String targetFolder;
   private WorldCreationContext settings;
   private WorldCreationUiState.WorldTypeEntry worldType;
   private final List<WorldCreationUiState.WorldTypeEntry> normalPresetList = new ArrayList<>();
   private final List<WorldCreationUiState.WorldTypeEntry> altPresetList = new ArrayList<>();
   private GameRules gameRules = new GameRules();

   public WorldCreationUiState(Path path, WorldCreationContext worldcreationcontext, Optional<ResourceKey<WorldPreset>> optional, OptionalLong optionallong) {
      this.savesFolder = path;
      this.settings = worldcreationcontext;
      this.worldType = new WorldCreationUiState.WorldTypeEntry(findPreset(worldcreationcontext, optional).orElse((Holder<WorldPreset>)null));
      this.updatePresetLists();
      this.seed = optionallong.isPresent() ? Long.toString(optionallong.getAsLong()) : "";
      this.generateStructures = worldcreationcontext.options().generateStructures();
      this.bonusChest = worldcreationcontext.options().generateBonusChest();
      this.targetFolder = this.findResultFolder(this.name);
   }

   public void addListener(Consumer<WorldCreationUiState> consumer) {
      this.listeners.add(consumer);
   }

   public void onChanged() {
      boolean flag = this.isBonusChest();
      if (flag != this.settings.options().generateBonusChest()) {
         this.settings = this.settings.withOptions((worldoptions1) -> worldoptions1.withBonusChest(flag));
      }

      boolean flag1 = this.isGenerateStructures();
      if (flag1 != this.settings.options().generateStructures()) {
         this.settings = this.settings.withOptions((worldoptions) -> worldoptions.withStructures(flag1));
      }

      for(Consumer<WorldCreationUiState> consumer : this.listeners) {
         consumer.accept(this);
      }

   }

   public void setName(String s) {
      this.name = s;
      this.targetFolder = this.findResultFolder(s);
      this.onChanged();
   }

   private String findResultFolder(String s) {
      String s1 = s.trim();

      try {
         return FileUtil.findAvailableName(this.savesFolder, !s1.isEmpty() ? s1 : DEFAULT_WORLD_NAME.getString(), "");
      } catch (Exception var5) {
         try {
            return FileUtil.findAvailableName(this.savesFolder, "World", "");
         } catch (IOException var4) {
            throw new RuntimeException("Could not create save folder", var4);
         }
      }
   }

   public String getName() {
      return this.name;
   }

   public String getTargetFolder() {
      return this.targetFolder;
   }

   public void setGameMode(WorldCreationUiState.SelectedGameMode worldcreationuistate_selectedgamemode) {
      this.gameMode = worldcreationuistate_selectedgamemode;
      this.onChanged();
   }

   public WorldCreationUiState.SelectedGameMode getGameMode() {
      return this.isDebug() ? WorldCreationUiState.SelectedGameMode.DEBUG : this.gameMode;
   }

   public void setDifficulty(Difficulty difficulty) {
      this.difficulty = difficulty;
      this.onChanged();
   }

   public Difficulty getDifficulty() {
      return this.isHardcore() ? Difficulty.HARD : this.difficulty;
   }

   public boolean isHardcore() {
      return this.getGameMode() == WorldCreationUiState.SelectedGameMode.HARDCORE;
   }

   public void setAllowCheats(boolean flag) {
      this.allowCheats = flag;
      this.onChanged();
   }

   public boolean isAllowCheats() {
      if (this.isDebug()) {
         return true;
      } else if (this.isHardcore()) {
         return false;
      } else if (this.allowCheats == null) {
         return this.getGameMode() == WorldCreationUiState.SelectedGameMode.CREATIVE;
      } else {
         return this.allowCheats;
      }
   }

   public void setSeed(String s) {
      this.seed = s;
      this.settings = this.settings.withOptions((worldoptions) -> worldoptions.withSeed(WorldOptions.parseSeed(this.getSeed())));
      this.onChanged();
   }

   public String getSeed() {
      return this.seed;
   }

   public void setGenerateStructures(boolean flag) {
      this.generateStructures = flag;
      this.onChanged();
   }

   public boolean isGenerateStructures() {
      return this.isDebug() ? false : this.generateStructures;
   }

   public void setBonusChest(boolean flag) {
      this.bonusChest = flag;
      this.onChanged();
   }

   public boolean isBonusChest() {
      return !this.isDebug() && !this.isHardcore() ? this.bonusChest : false;
   }

   public void setSettings(WorldCreationContext worldcreationcontext) {
      this.settings = worldcreationcontext;
      this.updatePresetLists();
      this.onChanged();
   }

   public WorldCreationContext getSettings() {
      return this.settings;
   }

   public void updateDimensions(WorldCreationContext.DimensionsUpdater worldcreationcontext_dimensionsupdater) {
      this.settings = this.settings.withDimensions(worldcreationcontext_dimensionsupdater);
      this.onChanged();
   }

   protected boolean tryUpdateDataConfiguration(WorldDataConfiguration worlddataconfiguration) {
      WorldDataConfiguration worlddataconfiguration1 = this.settings.dataConfiguration();
      if (worlddataconfiguration1.dataPacks().getEnabled().equals(worlddataconfiguration.dataPacks().getEnabled()) && worlddataconfiguration1.enabledFeatures().equals(worlddataconfiguration.enabledFeatures())) {
         this.settings = new WorldCreationContext(this.settings.options(), this.settings.datapackDimensions(), this.settings.selectedDimensions(), this.settings.worldgenRegistries(), this.settings.dataPackResources(), worlddataconfiguration);
         return true;
      } else {
         return false;
      }
   }

   public boolean isDebug() {
      return this.settings.selectedDimensions().isDebug();
   }

   public void setWorldType(WorldCreationUiState.WorldTypeEntry worldcreationuistate_worldtypeentry) {
      this.worldType = worldcreationuistate_worldtypeentry;
      Holder<WorldPreset> holder = worldcreationuistate_worldtypeentry.preset();
      if (holder != null) {
         this.updateDimensions((registryaccess_frozen, worlddimensions) -> holder.value().createWorldDimensions());
      }

   }

   public WorldCreationUiState.WorldTypeEntry getWorldType() {
      return this.worldType;
   }

   @Nullable
   public PresetEditor getPresetEditor() {
      Holder<WorldPreset> holder = this.getWorldType().preset();
      return holder != null ? PresetEditor.EDITORS.get(holder.unwrapKey()) : null;
   }

   public List<WorldCreationUiState.WorldTypeEntry> getNormalPresetList() {
      return this.normalPresetList;
   }

   public List<WorldCreationUiState.WorldTypeEntry> getAltPresetList() {
      return this.altPresetList;
   }

   private void updatePresetLists() {
      Registry<WorldPreset> registry = this.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET);
      this.normalPresetList.clear();
      this.normalPresetList.addAll(getNonEmptyList(registry, WorldPresetTags.NORMAL).orElseGet(() -> registry.holders().map(WorldCreationUiState.WorldTypeEntry::new).toList()));
      this.altPresetList.clear();
      this.altPresetList.addAll(getNonEmptyList(registry, WorldPresetTags.EXTENDED).orElse(this.normalPresetList));
      Holder<WorldPreset> holder = this.worldType.preset();
      if (holder != null) {
         this.worldType = findPreset(this.getSettings(), holder.unwrapKey()).map(WorldCreationUiState.WorldTypeEntry::new).orElse(this.normalPresetList.get(0));
      }

   }

   private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext worldcreationcontext, Optional<ResourceKey<WorldPreset>> optional) {
      return optional.flatMap((resourcekey) -> worldcreationcontext.worldgenLoadContext().<WorldPreset>registryOrThrow(Registries.WORLD_PRESET).getHolder(resourcekey));
   }

   private static Optional<List<WorldCreationUiState.WorldTypeEntry>> getNonEmptyList(Registry<WorldPreset> registry, TagKey<WorldPreset> tagkey) {
      return registry.getTag(tagkey).map((holderset_named) -> holderset_named.stream().map(WorldCreationUiState.WorldTypeEntry::new).toList()).filter((list) -> !list.isEmpty());
   }

   public void setGameRules(GameRules gamerules) {
      this.gameRules = gamerules;
      this.onChanged();
   }

   public GameRules getGameRules() {
      return this.gameRules;
   }

   public static enum SelectedGameMode {
      SURVIVAL("survival", GameType.SURVIVAL),
      HARDCORE("hardcore", GameType.SURVIVAL),
      CREATIVE("creative", GameType.CREATIVE),
      DEBUG("spectator", GameType.SPECTATOR);

      public final GameType gameType;
      public final Component displayName;
      private final Component info;

      private SelectedGameMode(String s, GameType gametype) {
         this.gameType = gametype;
         this.displayName = Component.translatable("selectWorld.gameMode." + s);
         this.info = Component.translatable("selectWorld.gameMode." + s + ".info");
      }

      public Component getInfo() {
         return this.info;
      }
   }

   public static record WorldTypeEntry(@Nullable Holder<WorldPreset> preset) {
      private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");

      public Component describePreset() {
         return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).map((resourcekey) -> Component.translatable(resourcekey.location().toLanguageKey("generator"))).orElse(CUSTOM_WORLD_DESCRIPTION);
      }

      public boolean isAmplified() {
         return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).filter((resourcekey) -> resourcekey.equals(WorldPresets.AMPLIFIED)).isPresent();
      }
   }
}
