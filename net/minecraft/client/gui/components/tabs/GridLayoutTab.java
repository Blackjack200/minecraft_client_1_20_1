package net.minecraft.client.gui.components.tabs;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public class GridLayoutTab implements Tab {
   private final Component title;
   protected final GridLayout layout = new GridLayout();

   public GridLayoutTab(Component component) {
      this.title = component;
   }

   public Component getTabTitle() {
      return this.title;
   }

   public void visitChildren(Consumer<AbstractWidget> consumer) {
      this.layout.visitWidgets(consumer);
   }

   public void doLayout(ScreenRectangle screenrectangle) {
      this.layout.arrangeElements();
      FrameLayout.alignInRectangle(this.layout, screenrectangle, 0.5F, 0.16666667F);
   }
}
