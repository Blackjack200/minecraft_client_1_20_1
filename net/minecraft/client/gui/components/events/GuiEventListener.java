package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface GuiEventListener extends TabOrderedElement {
   long DOUBLE_CLICK_THRESHOLD_MS = 250L;

   default void mouseMoved(double d0, double d1) {
   }

   default boolean mouseClicked(double d0, double d1, int i) {
      return false;
   }

   default boolean mouseReleased(double d0, double d1, int i) {
      return false;
   }

   default boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      return false;
   }

   default boolean mouseScrolled(double d0, double d1, double d2) {
      return false;
   }

   default boolean keyPressed(int i, int j, int k) {
      return false;
   }

   default boolean keyReleased(int i, int j, int k) {
      return false;
   }

   default boolean charTyped(char c0, int i) {
      return false;
   }

   @Nullable
   default ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      return null;
   }

   default boolean isMouseOver(double d0, double d1) {
      return false;
   }

   void setFocused(boolean flag);

   boolean isFocused();

   @Nullable
   default ComponentPath getCurrentFocusPath() {
      return this.isFocused() ? ComponentPath.leaf(this) : null;
   }

   default ScreenRectangle getRectangle() {
      return ScreenRectangle.empty();
   }
}
