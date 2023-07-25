package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

public class ExperimentsScreen extends Screen {
   private static final int MAIN_CONTENT_WIDTH = 310;
   private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
   private final Screen parent;
   private final PackRepository packRepository;
   private final Consumer<PackRepository> output;
   private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<>();

   protected ExperimentsScreen(Screen screen, PackRepository packrepository, Consumer<PackRepository> consumer) {
      super(Component.translatable("experiments_screen.title"));
      this.parent = screen;
      this.packRepository = packrepository;
      this.output = consumer;

      for(Pack pack : packrepository.getAvailablePacks()) {
         if (pack.getPackSource() == PackSource.FEATURE) {
            this.packs.put(pack, packrepository.getSelectedPacks().contains(pack));
         }
      }

   }

   protected void init() {
      this.layout.addToHeader(new StringWidget(Component.translatable("selectWorld.experiments"), this.font));
      GridLayout.RowHelper gridlayout_rowhelper = this.layout.addToContents(new GridLayout()).createRowHelper(1);
      gridlayout_rowhelper.addChild((new MultiLineTextWidget(Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED), this.font)).setMaxWidth(310), gridlayout_rowhelper.newCellSettings().paddingBottom(15));
      SwitchGrid.Builder switchgrid_builder = SwitchGrid.builder(310).withInfoUnderneath(2, true).withRowSpacing(4);
      this.packs.forEach((pack, obool) -> switchgrid_builder.addSwitch(getHumanReadableTitle(pack), () -> this.packs.getBoolean(pack), (obool1) -> this.packs.put(pack, obool1.booleanValue())).withInfo(pack.getDescription()));
      switchgrid_builder.build(gridlayout_rowhelper::addChild);
      GridLayout.RowHelper gridlayout_rowhelper1 = this.layout.addToFooter((new GridLayout()).columnSpacing(10)).createRowHelper(2);
      gridlayout_rowhelper1.addChild(Button.builder(CommonComponents.GUI_DONE, (button1) -> this.onDone()).build());
      gridlayout_rowhelper1.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).build());
      this.layout.visitWidgets((guieventlistener) -> {
         AbstractWidget var10000 = this.addRenderableWidget(guieventlistener);
      });
      this.repositionElements();
   }

   private static Component getHumanReadableTitle(Pack pack) {
      String s = "dataPack." + pack.getId() + ".name";
      return (Component)(I18n.exists(s) ? Component.translatable(s) : pack.getTitle());
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   private void onDone() {
      List<Pack> list = new ArrayList<>(this.packRepository.getSelectedPacks());
      List<Pack> list1 = new ArrayList<>();
      this.packs.forEach((pack, obool) -> {
         list.remove(pack);
         if (obool) {
            list1.add(pack);
         }

      });
      list.addAll(Lists.reverse(list1));
      this.packRepository.setSelected(list.stream().map(Pack::getId).toList());
      this.output.accept(this.packRepository);
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
      int k = 32;
      guigraphics.blit(BACKGROUND_LOCATION, 0, this.layout.getHeaderHeight(), 0.0F, 0.0F, this.width, this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight(), 32, 32);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      super.render(guigraphics, i, j, f);
   }
}
