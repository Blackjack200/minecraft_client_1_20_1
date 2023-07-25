package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class PresetFlatWorldScreen extends Screen {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int SLOT_TEX_SIZE = 128;
   private static final int SLOT_BG_SIZE = 18;
   private static final int SLOT_STAT_HEIGHT = 20;
   private static final int SLOT_BG_X = 1;
   private static final int SLOT_BG_Y = 1;
   private static final int SLOT_FG_X = 2;
   private static final int SLOT_FG_Y = 2;
   private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
   public static final Component UNKNOWN_PRESET = Component.translatable("flat_world_preset.unknown");
   private final CreateFlatWorldScreen parent;
   private Component shareText;
   private Component listText;
   private PresetFlatWorldScreen.PresetsList list;
   private Button selectButton;
   EditBox export;
   FlatLevelGeneratorSettings settings;

   public PresetFlatWorldScreen(CreateFlatWorldScreen createflatworldscreen) {
      super(Component.translatable("createWorld.customize.presets.title"));
      this.parent = createflatworldscreen;
   }

   @Nullable
   private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> holdergetter, String s, int i) {
      List<String> list = Splitter.on('*').limit(2).splitToList(s);
      int j;
      String s1;
      if (list.size() == 2) {
         s1 = list.get(1);

         try {
            j = Math.max(Integer.parseInt(list.get(0)), 0);
         } catch (NumberFormatException var11) {
            LOGGER.error("Error while parsing flat world string", (Throwable)var11);
            return null;
         }
      } else {
         s1 = list.get(0);
         j = 1;
      }

      int l = Math.min(i + j, DimensionType.Y_SIZE);
      int i1 = l - i;

      Optional<Holder.Reference<Block>> optional;
      try {
         optional = holdergetter.get(ResourceKey.create(Registries.BLOCK, new ResourceLocation(s1)));
      } catch (Exception var10) {
         LOGGER.error("Error while parsing flat world string", (Throwable)var10);
         return null;
      }

      if (optional.isEmpty()) {
         LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)s1);
         return null;
      } else {
         return new FlatLayerInfo(i1, optional.get().value());
      }
   }

   private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> holdergetter, String s) {
      List<FlatLayerInfo> list = Lists.newArrayList();
      String[] astring = s.split(",");
      int i = 0;

      for(String s1 : astring) {
         FlatLayerInfo flatlayerinfo = getLayerInfoFromString(holdergetter, s1, i);
         if (flatlayerinfo == null) {
            return Collections.emptyList();
         }

         list.add(flatlayerinfo);
         i += flatlayerinfo.getHeight();
      }

      return list;
   }

   public static FlatLevelGeneratorSettings fromString(HolderGetter<Block> holdergetter, HolderGetter<Biome> holdergetter1, HolderGetter<StructureSet> holdergetter2, HolderGetter<PlacedFeature> holdergetter3, String s, FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      Iterator<String> iterator = Splitter.on(';').split(s).iterator();
      if (!iterator.hasNext()) {
         return FlatLevelGeneratorSettings.getDefault(holdergetter1, holdergetter2, holdergetter3);
      } else {
         List<FlatLayerInfo> list = getLayersInfoFromString(holdergetter, iterator.next());
         if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault(holdergetter1, holdergetter2, holdergetter3);
         } else {
            Holder.Reference<Biome> holder_reference = holdergetter1.getOrThrow(DEFAULT_BIOME);
            Holder<Biome> holder = holder_reference;
            if (iterator.hasNext()) {
               String s1 = iterator.next();
               holder = Optional.ofNullable(ResourceLocation.tryParse(s1)).map((resourcelocation) -> ResourceKey.create(Registries.BIOME, resourcelocation)).flatMap(holdergetter1::get).orElseGet(() -> {
                  LOGGER.warn("Invalid biome: {}", (Object)s1);
                  return holder_reference;
               });
            }

            return flatlevelgeneratorsettings.withBiomeAndLayers(list, flatlevelgeneratorsettings.structureOverrides(), holder);
         }
      }
   }

   static String save(FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < flatlevelgeneratorsettings.getLayersInfo().size(); ++i) {
         if (i > 0) {
            stringbuilder.append(",");
         }

         stringbuilder.append(flatlevelgeneratorsettings.getLayersInfo().get(i));
      }

      stringbuilder.append(";");
      stringbuilder.append(flatlevelgeneratorsettings.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> new IllegalStateException("Biome not registered")));
      return stringbuilder.toString();
   }

   protected void init() {
      this.shareText = Component.translatable("createWorld.customize.presets.share");
      this.listText = Component.translatable("createWorld.customize.presets.list");
      this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
      this.export.setMaxLength(1230);
      WorldCreationContext worldcreationcontext = this.parent.parent.getUiState().getSettings();
      RegistryAccess registryaccess = worldcreationcontext.worldgenLoadContext();
      FeatureFlagSet featureflagset = worldcreationcontext.dataConfiguration().enabledFeatures();
      HolderGetter<Biome> holdergetter = registryaccess.lookupOrThrow(Registries.BIOME);
      HolderGetter<StructureSet> holdergetter1 = registryaccess.lookupOrThrow(Registries.STRUCTURE_SET);
      HolderGetter<PlacedFeature> holdergetter2 = registryaccess.lookupOrThrow(Registries.PLACED_FEATURE);
      HolderGetter<Block> holdergetter3 = registryaccess.lookupOrThrow(Registries.BLOCK).filterFeatures(featureflagset);
      this.export.setValue(save(this.parent.settings()));
      this.settings = this.parent.settings();
      this.addWidget(this.export);
      this.list = new PresetFlatWorldScreen.PresetsList(registryaccess, featureflagset);
      this.addWidget(this.list);
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), (button1) -> {
         FlatLevelGeneratorSettings flatlevelgeneratorsettings = fromString(holdergetter3, holdergetter, holdergetter1, holdergetter2, this.export.getValue(), this.settings);
         this.parent.setConfig(flatlevelgeneratorsettings);
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.updateButtonValidity(this.list.getSelected() != null);
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      return this.list.mouseScrolled(d0, d1, d2);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.export.getValue();
      this.init(minecraft, i, j);
      this.export.setValue(s);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.list.render(guigraphics, i, j, f);
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 400.0F);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      guigraphics.drawString(this.font, this.shareText, 50, 30, 10526880);
      guigraphics.drawString(this.font, this.listText, 50, 70, 10526880);
      guigraphics.pose().popPose();
      this.export.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }

   public void tick() {
      this.export.tick();
      super.tick();
   }

   public void updateButtonValidity(boolean flag) {
      this.selectButton.active = flag || this.export.getValue().length() > 1;
   }

   class PresetsList extends ObjectSelectionList<PresetFlatWorldScreen.PresetsList.Entry> {
      public PresetsList(RegistryAccess registryaccess, FeatureFlagSet featureflagset) {
         super(PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height, 80, PresetFlatWorldScreen.this.height - 37, 24);

         for(Holder<FlatLevelGeneratorPreset> holder : registryaccess.registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
            Set<Block> set = holder.value().settings().getLayersInfo().stream().map((flatlayerinfo) -> flatlayerinfo.getBlockState().getBlock()).filter((block) -> !block.isEnabled(featureflagset)).collect(Collectors.toSet());
            if (!set.isEmpty()) {
               PresetFlatWorldScreen.LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", holder.unwrapKey().map((resourcekey) -> resourcekey.location().toString()).orElse("<unknown>"), set);
            } else {
               this.addEntry(new PresetFlatWorldScreen.PresetsList.Entry(holder));
            }
         }

      }

      public void setSelected(@Nullable PresetFlatWorldScreen.PresetsList.Entry presetflatworldscreen_presetslist_entry) {
         super.setSelected(presetflatworldscreen_presetslist_entry);
         PresetFlatWorldScreen.this.updateButtonValidity(presetflatworldscreen_presetslist_entry != null);
      }

      public boolean keyPressed(int i, int j, int k) {
         if (super.keyPressed(i, j, k)) {
            return true;
         } else {
            if (CommonInputs.selected(i) && this.getSelected() != null) {
               this.getSelected().select();
            }

            return false;
         }
      }

      public class Entry extends ObjectSelectionList.Entry<PresetFlatWorldScreen.PresetsList.Entry> {
         private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
         private final FlatLevelGeneratorPreset preset;
         private final Component name;

         public Entry(Holder<FlatLevelGeneratorPreset> holder) {
            this.preset = holder.value();
            this.name = holder.unwrapKey().map((resourcekey) -> Component.translatable(resourcekey.location().toLanguageKey("flat_world_preset"))).orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            this.blitSlot(guigraphics, k, j, this.preset.displayItem().value());
            guigraphics.drawString(PresetFlatWorldScreen.this.font, this.name, k + 18 + 5, j + 6, 16777215, false);
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               this.select();
            }

            return false;
         }

         void select() {
            PresetsList.this.setSelected(this);
            PresetFlatWorldScreen.this.settings = this.preset.settings();
            PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
            PresetFlatWorldScreen.this.export.moveCursorToStart();
         }

         private void blitSlot(GuiGraphics guigraphics, int i, int j, Item item) {
            this.blitSlotBg(guigraphics, i + 1, j + 1);
            guigraphics.renderFakeItem(new ItemStack(item), i + 2, j + 2);
         }

         private void blitSlotBg(GuiGraphics guigraphics, int i, int j) {
            guigraphics.blit(STATS_ICON_LOCATION, i, j, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.name);
         }
      }
   }
}
