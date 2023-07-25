package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.ArrayUtils;

public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
   final KeyBindsScreen keyBindsScreen;
   int maxNameWidth;

   public KeyBindsList(KeyBindsScreen keybindsscreen, Minecraft minecraft) {
      super(minecraft, keybindsscreen.width + 45, keybindsscreen.height, 20, keybindsscreen.height - 32, 20);
      this.keyBindsScreen = keybindsscreen;
      KeyMapping[] akeymapping = ArrayUtils.clone((KeyMapping[])minecraft.options.keyMappings);
      Arrays.sort((Object[])akeymapping);
      String s = null;

      for(KeyMapping keymapping : akeymapping) {
         String s1 = keymapping.getCategory();
         if (!s1.equals(s)) {
            s = s1;
            this.addEntry(new KeyBindsList.CategoryEntry(Component.translatable(s1)));
         }

         Component component = Component.translatable(keymapping.getName());
         int i = minecraft.font.width(component);
         if (i > this.maxNameWidth) {
            this.maxNameWidth = i;
         }

         this.addEntry(new KeyBindsList.KeyEntry(keymapping, component));
      }

   }

   public void resetMappingAndUpdateButtons() {
      KeyMapping.resetMapping();
      this.refreshEntries();
   }

   public void refreshEntries() {
      this.children().forEach(KeyBindsList.Entry::refreshEntry);
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 15;
   }

   public int getRowWidth() {
      return super.getRowWidth() + 32;
   }

   public class CategoryEntry extends KeyBindsList.Entry {
      final Component name;
      private final int width;

      public CategoryEntry(Component component) {
         this.name = component;
         this.width = KeyBindsList.this.minecraft.font.width(this.name);
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         guigraphics.drawString(KeyBindsList.this.minecraft.font, this.name, KeyBindsList.this.minecraft.screen.width / 2 - this.width / 2, j + i1 - 9 - 1, 16777215, false);
      }

      @Nullable
      public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
         return null;
      }

      public List<? extends GuiEventListener> children() {
         return Collections.emptyList();
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(new NarratableEntry() {
            public NarratableEntry.NarrationPriority narrationPriority() {
               return NarratableEntry.NarrationPriority.HOVERED;
            }

            public void updateNarration(NarrationElementOutput narrationelementoutput) {
               narrationelementoutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
            }
         });
      }

      protected void refreshEntry() {
      }
   }

   public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
      abstract void refreshEntry();
   }

   public class KeyEntry extends KeyBindsList.Entry {
      private final KeyMapping key;
      private final Component name;
      private final Button changeButton;
      private final Button resetButton;
      private boolean hasCollision = false;

      KeyEntry(KeyMapping keymapping, Component component) {
         this.key = keymapping;
         this.name = component;
         this.changeButton = Button.builder(component, (button1) -> {
            KeyBindsList.this.keyBindsScreen.selectedKey = keymapping;
            KeyBindsList.this.resetMappingAndUpdateButtons();
         }).bounds(0, 0, 75, 20).createNarration((supplier1) -> keymapping.isUnbound() ? Component.translatable("narrator.controls.unbound", component) : Component.translatable("narrator.controls.bound", component, supplier1.get())).build();
         this.resetButton = Button.builder(Component.translatable("controls.reset"), (button) -> {
            KeyBindsList.this.minecraft.options.setKey(keymapping, keymapping.getDefaultKey());
            KeyBindsList.this.resetMappingAndUpdateButtons();
         }).bounds(0, 0, 50, 20).createNarration((supplier) -> Component.translatable("narrator.controls.reset", component)).build();
         this.refreshEntry();
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         int var10003 = k + 90 - KeyBindsList.this.maxNameWidth;
         guigraphics.drawString(KeyBindsList.this.minecraft.font, this.name, var10003, j + i1 / 2 - 9 / 2, 16777215, false);
         this.resetButton.setX(k + 190);
         this.resetButton.setY(j);
         this.resetButton.render(guigraphics, j1, k1, f);
         this.changeButton.setX(k + 105);
         this.changeButton.setY(j);
         if (this.hasCollision) {
            int l1 = 3;
            int i2 = this.changeButton.getX() - 6;
            guigraphics.fill(i2, j + 2, i2 + 3, j + i1 + 2, ChatFormatting.RED.getColor() | -16777216);
         }

         this.changeButton.render(guigraphics, j1, k1, f);
      }

      public List<? extends GuiEventListener> children() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(this.changeButton, this.resetButton);
      }

      protected void refreshEntry() {
         this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
         this.resetButton.active = !this.key.isDefault();
         this.hasCollision = false;
         MutableComponent mutablecomponent = Component.empty();
         if (!this.key.isUnbound()) {
            for(KeyMapping keymapping : KeyBindsList.this.minecraft.options.keyMappings) {
               if (keymapping != this.key && this.key.same(keymapping)) {
                  if (this.hasCollision) {
                     mutablecomponent.append(", ");
                  }

                  this.hasCollision = true;
                  mutablecomponent.append(Component.translatable(keymapping.getName()));
               }
            }
         }

         if (this.hasCollision) {
            this.changeButton.setMessage(Component.literal("[ ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.RED));
            this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", mutablecomponent)));
         } else {
            this.changeButton.setTooltip((Tooltip)null);
         }

         if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
            this.changeButton.setMessage(Component.literal("> ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW));
         }

      }
   }
}
