package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;

public interface Layout extends LayoutElement {
   void visitChildren(Consumer<LayoutElement> consumer);

   default void visitWidgets(Consumer<AbstractWidget> consumer) {
      this.visitChildren((layoutelement) -> layoutelement.visitWidgets(consumer));
   }

   default void arrangeElements() {
      this.visitChildren((layoutelement) -> {
         if (layoutelement instanceof Layout layout) {
            layout.arrangeElements();
         }

      });
   }
}
