package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class CreateBuffetWorldScreen extends Screen {
   private static final Component BIOME_SELECT_INFO = Component.translatable("createWorld.customize.buffet.biome");
   private final Screen parent;
   private final Consumer<Holder<Biome>> applySettings;
   final Registry<Biome> biomes;
   private CreateBuffetWorldScreen.BiomeList list;
   Holder<Biome> biome;
   private Button doneButton;

   public CreateBuffetWorldScreen(Screen screen, WorldCreationContext worldcreationcontext, Consumer<Holder<Biome>> consumer) {
      super(Component.translatable("createWorld.customize.buffet.title"));
      this.parent = screen;
      this.applySettings = consumer;
      this.biomes = worldcreationcontext.worldgenLoadContext().registryOrThrow(Registries.BIOME);
      Holder<Biome> holder = this.biomes.getHolder(Biomes.PLAINS).or(() -> this.biomes.holders().findAny()).orElseThrow();
      this.biome = worldcreationcontext.selectedDimensions().overworld().getBiomeSource().possibleBiomes().stream().findFirst().orElse(holder);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   protected void init() {
      this.list = new CreateBuffetWorldScreen.BiomeList();
      this.addWidget(this.list);
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button1) -> {
         this.applySettings.accept(this.biome);
         this.minecraft.setScreen(this.parent);
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.list.setSelected(this.list.children().stream().filter((createbuffetworldscreen_biomelist_entry) -> Objects.equals(createbuffetworldscreen_biomelist_entry.biome, this.biome)).findFirst().orElse((CreateBuffetWorldScreen.BiomeList.Entry)null));
   }

   void updateButtonValidity() {
      this.doneButton.active = this.list.getSelected() != null;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderDirtBackground(guigraphics);
      this.list.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      guigraphics.drawCenteredString(this.font, BIOME_SELECT_INFO, this.width / 2, 28, 10526880);
      super.render(guigraphics, i, j, f);
   }

   class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
      BiomeList() {
         super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
         Collator collator = Collator.getInstance(Locale.getDefault());
         CreateBuffetWorldScreen.this.biomes.holders().map((holder_reference) -> new CreateBuffetWorldScreen.BiomeList.Entry(holder_reference)).sorted(Comparator.comparing((createbuffetworldscreen_biomelist_entry) -> createbuffetworldscreen_biomelist_entry.name.getString(), collator)).forEach((abstractselectionlist_entry) -> this.addEntry(abstractselectionlist_entry));
      }

      public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry createbuffetworldscreen_biomelist_entry) {
         super.setSelected(createbuffetworldscreen_biomelist_entry);
         if (createbuffetworldscreen_biomelist_entry != null) {
            CreateBuffetWorldScreen.this.biome = createbuffetworldscreen_biomelist_entry.biome;
         }

         CreateBuffetWorldScreen.this.updateButtonValidity();
      }

      class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
         final Holder.Reference<Biome> biome;
         final Component name;

         public Entry(Holder.Reference<Biome> holder_reference) {
            this.biome = holder_reference;
            ResourceLocation resourcelocation = holder_reference.key().location();
            String s = resourcelocation.toLanguageKey("biome");
            if (Language.getInstance().has(s)) {
               this.name = Component.translatable(s);
            } else {
               this.name = Component.literal(resourcelocation.toString());
            }

         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.name);
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            guigraphics.drawString(CreateBuffetWorldScreen.this.font, this.name, k + 5, j + 2, 16777215);
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               BiomeList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}
