package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
   public OptionsList(Minecraft minecraft, int i, int j, int k, int l, int i1) {
      super(minecraft, i, j, k, l, i1);
      this.centerListVertically = false;
   }

   public int addBig(OptionInstance<?> optioninstance) {
      return this.addEntry(OptionsList.Entry.big(this.minecraft.options, this.width, optioninstance));
   }

   public void addSmall(OptionInstance<?> optioninstance, @Nullable OptionInstance<?> optioninstance1) {
      this.addEntry(OptionsList.Entry.small(this.minecraft.options, this.width, optioninstance, optioninstance1));
   }

   public void addSmall(OptionInstance<?>[] aoptioninstance) {
      for(int i = 0; i < aoptioninstance.length; i += 2) {
         this.addSmall(aoptioninstance[i], i < aoptioninstance.length - 1 ? aoptioninstance[i + 1] : null);
      }

   }

   public int getRowWidth() {
      return 400;
   }

   protected int getScrollbarPosition() {
      return super.getScrollbarPosition() + 32;
   }

   @Nullable
   public AbstractWidget findOption(OptionInstance<?> optioninstance) {
      for(OptionsList.Entry optionslist_entry : this.children()) {
         AbstractWidget abstractwidget = optionslist_entry.options.get(optioninstance);
         if (abstractwidget != null) {
            return abstractwidget;
         }
      }

      return null;
   }

   public Optional<AbstractWidget> getMouseOver(double d0, double d1) {
      for(OptionsList.Entry optionslist_entry : this.children()) {
         for(AbstractWidget abstractwidget : optionslist_entry.children) {
            if (abstractwidget.isMouseOver(d0, d1)) {
               return Optional.of(abstractwidget);
            }
         }
      }

      return Optional.empty();
   }

   protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
      final Map<OptionInstance<?>, AbstractWidget> options;
      final List<AbstractWidget> children;

      private Entry(Map<OptionInstance<?>, AbstractWidget> map) {
         this.options = map;
         this.children = ImmutableList.copyOf(map.values());
      }

      public static OptionsList.Entry big(Options options, int i, OptionInstance<?> optioninstance) {
         return new OptionsList.Entry(ImmutableMap.of(optioninstance, optioninstance.createButton(options, i / 2 - 155, 0, 310)));
      }

      public static OptionsList.Entry small(Options options, int i, OptionInstance<?> optioninstance, @Nullable OptionInstance<?> optioninstance1) {
         AbstractWidget abstractwidget = optioninstance.createButton(options, i / 2 - 155, 0, 150);
         return optioninstance1 == null ? new OptionsList.Entry(ImmutableMap.of(optioninstance, abstractwidget)) : new OptionsList.Entry(ImmutableMap.of(optioninstance, abstractwidget, optioninstance1, optioninstance1.createButton(options, i / 2 - 155 + 160, 0, 150)));
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         this.children.forEach((abstractwidget) -> {
            abstractwidget.setY(j);
            abstractwidget.render(guigraphics, j1, k1, f);
         });
      }

      public List<? extends GuiEventListener> children() {
         return this.children;
      }

      public List<? extends NarratableEntry> narratables() {
         return this.children;
      }
   }
}
