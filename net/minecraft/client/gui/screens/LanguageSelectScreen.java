package net.minecraft.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class LanguageSelectScreen extends OptionsSubScreen {
   private static final Component WARNING_LABEL = Component.literal("(").append(Component.translatable("options.languageWarning")).append(")").withStyle(ChatFormatting.GRAY);
   private LanguageSelectScreen.LanguageSelectionList packSelectionList;
   final LanguageManager languageManager;

   public LanguageSelectScreen(Screen screen, Options options, LanguageManager languagemanager) {
      super(screen, options, Component.translatable("options.language"));
      this.languageManager = languagemanager;
   }

   protected void init() {
      this.packSelectionList = new LanguageSelectScreen.LanguageSelectionList(this.minecraft);
      this.addWidget(this.packSelectionList);
      this.addRenderableWidget(this.options.forceUnicodeFont().createButton(this.options, this.width / 2 - 155, this.height - 38, 150));
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onDone()).bounds(this.width / 2 - 155 + 160, this.height - 38, 150, 20).build());
      super.init();
   }

   void onDone() {
      LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen_languageselectionlist_entry = this.packSelectionList.getSelected();
      if (languageselectscreen_languageselectionlist_entry != null && !languageselectscreen_languageselectionlist_entry.code.equals(this.languageManager.getSelected())) {
         this.languageManager.setSelected(languageselectscreen_languageselectionlist_entry.code);
         this.options.languageCode = languageselectscreen_languageselectionlist_entry.code;
         this.minecraft.reloadResourcePacks();
         this.options.save();
      }

      this.minecraft.setScreen(this.lastScreen);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (CommonInputs.selected(i)) {
         LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen_languageselectionlist_entry = this.packSelectionList.getSelected();
         if (languageselectscreen_languageselectionlist_entry != null) {
            languageselectscreen_languageselectionlist_entry.select();
            this.onDone();
            return true;
         }
      }

      return super.keyPressed(i, j, k);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.packSelectionList.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      guigraphics.drawCenteredString(this.font, WARNING_LABEL, this.width / 2, this.height - 56, 8421504);
      super.render(guigraphics, i, j, f);
   }

   class LanguageSelectionList extends ObjectSelectionList<LanguageSelectScreen.LanguageSelectionList.Entry> {
      public LanguageSelectionList(Minecraft minecraft) {
         super(minecraft, LanguageSelectScreen.this.width, LanguageSelectScreen.this.height, 32, LanguageSelectScreen.this.height - 65 + 4, 18);
         String s = LanguageSelectScreen.this.languageManager.getSelected();
         LanguageSelectScreen.this.languageManager.getLanguages().forEach((s2, languageinfo) -> {
            LanguageSelectScreen.LanguageSelectionList.Entry languageselectscreen_languageselectionlist_entry = new LanguageSelectScreen.LanguageSelectionList.Entry(s2, languageinfo);
            this.addEntry(languageselectscreen_languageselectionlist_entry);
            if (s.equals(s2)) {
               this.setSelected(languageselectscreen_languageselectionlist_entry);
            }

         });
         if (this.getSelected() != null) {
            this.centerScrollOn(this.getSelected());
         }

      }

      protected int getScrollbarPosition() {
         return super.getScrollbarPosition() + 20;
      }

      public int getRowWidth() {
         return super.getRowWidth() + 50;
      }

      protected void renderBackground(GuiGraphics guigraphics) {
         LanguageSelectScreen.this.renderBackground(guigraphics);
      }

      public class Entry extends ObjectSelectionList.Entry<LanguageSelectScreen.LanguageSelectionList.Entry> {
         final String code;
         private final Component language;
         private long lastClickTime;

         public Entry(String s, LanguageInfo languageinfo) {
            this.code = s;
            this.language = languageinfo.toComponent();
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            guigraphics.drawCenteredString(LanguageSelectScreen.this.font, this.language, LanguageSelectionList.this.width / 2, j + 1, 16777215);
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               this.select();
               if (Util.getMillis() - this.lastClickTime < 250L) {
                  LanguageSelectScreen.this.onDone();
               }

               this.lastClickTime = Util.getMillis();
               return true;
            } else {
               this.lastClickTime = Util.getMillis();
               return false;
            }
         }

         void select() {
            LanguageSelectionList.this.setSelected(this);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.language);
         }
      }
   }
}
