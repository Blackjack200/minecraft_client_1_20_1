package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;

public interface ContainerEventHandler extends GuiEventListener {
   List<? extends GuiEventListener> children();

   default Optional<GuiEventListener> getChildAt(double d0, double d1) {
      for(GuiEventListener guieventlistener : this.children()) {
         if (guieventlistener.isMouseOver(d0, d1)) {
            return Optional.of(guieventlistener);
         }
      }

      return Optional.empty();
   }

   default boolean mouseClicked(double d0, double d1, int i) {
      for(GuiEventListener guieventlistener : this.children()) {
         if (guieventlistener.mouseClicked(d0, d1, i)) {
            this.setFocused(guieventlistener);
            if (i == 0) {
               this.setDragging(true);
            }

            return true;
         }
      }

      return false;
   }

   default boolean mouseReleased(double d0, double d1, int i) {
      this.setDragging(false);
      return this.getChildAt(d0, d1).filter((guieventlistener) -> guieventlistener.mouseReleased(d0, d1, i)).isPresent();
   }

   default boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      return this.getFocused() != null && this.isDragging() && i == 0 ? this.getFocused().mouseDragged(d0, d1, i, d2, d3) : false;
   }

   boolean isDragging();

   void setDragging(boolean flag);

   default boolean mouseScrolled(double d0, double d1, double d2) {
      return this.getChildAt(d0, d1).filter((guieventlistener) -> guieventlistener.mouseScrolled(d0, d1, d2)).isPresent();
   }

   default boolean keyPressed(int i, int j, int k) {
      return this.getFocused() != null && this.getFocused().keyPressed(i, j, k);
   }

   default boolean keyReleased(int i, int j, int k) {
      return this.getFocused() != null && this.getFocused().keyReleased(i, j, k);
   }

   default boolean charTyped(char c0, int i) {
      return this.getFocused() != null && this.getFocused().charTyped(c0, i);
   }

   @Nullable
   GuiEventListener getFocused();

   void setFocused(@Nullable GuiEventListener guieventlistener);

   default void setFocused(boolean flag) {
   }

   default boolean isFocused() {
      return this.getFocused() != null;
   }

   @Nullable
   default ComponentPath getCurrentFocusPath() {
      GuiEventListener guieventlistener = this.getFocused();
      return guieventlistener != null ? ComponentPath.path(this, guieventlistener.getCurrentFocusPath()) : null;
   }

   default void magicalSpecialHackyFocus(@Nullable GuiEventListener guieventlistener) {
      this.setFocused(guieventlistener);
   }

   @Nullable
   default ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      GuiEventListener guieventlistener = this.getFocused();
      if (guieventlistener != null) {
         ComponentPath componentpath = guieventlistener.nextFocusPath(focusnavigationevent);
         if (componentpath != null) {
            return ComponentPath.path(this, componentpath);
         }
      }

      if (focusnavigationevent instanceof FocusNavigationEvent.TabNavigation focusnavigationevent_tabnavigation) {
         return this.handleTabNavigation(focusnavigationevent_tabnavigation);
      } else if (focusnavigationevent instanceof FocusNavigationEvent.ArrowNavigation focusnavigationevent_arrownavigation) {
         return this.handleArrowNavigation(focusnavigationevent_arrownavigation);
      } else {
         return null;
      }
   }

   @Nullable
   private ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation focusnavigationevent_tabnavigation) {
      boolean flag = focusnavigationevent_tabnavigation.forward();
      GuiEventListener guieventlistener = this.getFocused();
      List<? extends GuiEventListener> list = new ArrayList<>(this.children());
      Collections.sort(list, Comparator.comparingInt((guieventlistener2) -> guieventlistener2.getTabOrderGroup()));
      int i = list.indexOf(guieventlistener);
      int j;
      if (guieventlistener != null && i >= 0) {
         j = i + (flag ? 1 : 0);
      } else if (flag) {
         j = 0;
      } else {
         j = list.size();
      }

      ListIterator<? extends GuiEventListener> listiterator = list.listIterator(j);
      BooleanSupplier booleansupplier = flag ? listiterator::hasNext : listiterator::hasPrevious;
      Supplier<? extends GuiEventListener> supplier = flag ? listiterator::next : listiterator::previous;

      while(booleansupplier.getAsBoolean()) {
         GuiEventListener guieventlistener1 = supplier.get();
         ComponentPath componentpath = guieventlistener1.nextFocusPath(focusnavigationevent_tabnavigation);
         if (componentpath != null) {
            return ComponentPath.path(this, componentpath);
         }
      }

      return null;
   }

   @Nullable
   private ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation focusnavigationevent_arrownavigation) {
      GuiEventListener guieventlistener = this.getFocused();
      if (guieventlistener == null) {
         ScreenDirection screendirection = focusnavigationevent_arrownavigation.direction();
         ScreenRectangle screenrectangle = this.getRectangle().getBorder(screendirection.getOpposite());
         return ComponentPath.path(this, this.nextFocusPathInDirection(screenrectangle, screendirection, (GuiEventListener)null, focusnavigationevent_arrownavigation));
      } else {
         ScreenRectangle screenrectangle1 = guieventlistener.getRectangle();
         return ComponentPath.path(this, this.nextFocusPathInDirection(screenrectangle1, focusnavigationevent_arrownavigation.direction(), guieventlistener, focusnavigationevent_arrownavigation));
      }
   }

   @Nullable
   private ComponentPath nextFocusPathInDirection(ScreenRectangle screenrectangle, ScreenDirection screendirection, @Nullable GuiEventListener guieventlistener, FocusNavigationEvent focusnavigationevent) {
      ScreenAxis screenaxis = screendirection.getAxis();
      ScreenAxis screenaxis1 = screenaxis.orthogonal();
      ScreenDirection screendirection1 = screenaxis1.getPositive();
      int i = screenrectangle.getBoundInDirection(screendirection.getOpposite());
      List<GuiEventListener> list = new ArrayList<>();

      for(GuiEventListener guieventlistener1 : this.children()) {
         if (guieventlistener1 != guieventlistener) {
            ScreenRectangle screenrectangle1 = guieventlistener1.getRectangle();
            if (screenrectangle1.overlapsInAxis(screenrectangle, screenaxis1)) {
               int j = screenrectangle1.getBoundInDirection(screendirection.getOpposite());
               if (screendirection.isAfter(j, i)) {
                  list.add(guieventlistener1);
               } else if (j == i && screendirection.isAfter(screenrectangle1.getBoundInDirection(screendirection), screenrectangle.getBoundInDirection(screendirection))) {
                  list.add(guieventlistener1);
               }
            }
         }
      }

      Comparator<GuiEventListener> comparator = Comparator.comparing((guieventlistener4) -> guieventlistener4.getRectangle().getBoundInDirection(screendirection.getOpposite()), screendirection.coordinateValueComparator());
      Comparator<GuiEventListener> comparator1 = Comparator.comparing((guieventlistener3) -> guieventlistener3.getRectangle().getBoundInDirection(screendirection1.getOpposite()), screendirection1.coordinateValueComparator());
      list.sort(comparator.thenComparing(comparator1));

      for(GuiEventListener guieventlistener2 : list) {
         ComponentPath componentpath = guieventlistener2.nextFocusPath(focusnavigationevent);
         if (componentpath != null) {
            return componentpath;
         }
      }

      return this.nextFocusPathVaguelyInDirection(screenrectangle, screendirection, guieventlistener, focusnavigationevent);
   }

   @Nullable
   private ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle screenrectangle, ScreenDirection screendirection, @Nullable GuiEventListener guieventlistener, FocusNavigationEvent focusnavigationevent) {
      ScreenAxis screenaxis = screendirection.getAxis();
      ScreenAxis screenaxis1 = screenaxis.orthogonal();
      List<Pair<GuiEventListener, Long>> list = new ArrayList<>();
      ScreenPosition screenposition = ScreenPosition.of(screenaxis, screenrectangle.getBoundInDirection(screendirection), screenrectangle.getCenterInAxis(screenaxis1));

      for(GuiEventListener guieventlistener1 : this.children()) {
         if (guieventlistener1 != guieventlistener) {
            ScreenRectangle screenrectangle1 = guieventlistener1.getRectangle();
            ScreenPosition screenposition1 = ScreenPosition.of(screenaxis, screenrectangle1.getBoundInDirection(screendirection.getOpposite()), screenrectangle1.getCenterInAxis(screenaxis1));
            if (screendirection.isAfter(screenposition1.getCoordinate(screenaxis), screenposition.getCoordinate(screenaxis))) {
               long i = Vector2i.distanceSquared(screenposition.x(), screenposition.y(), screenposition1.x(), screenposition1.y());
               list.add(Pair.of(guieventlistener1, i));
            }
         }
      }

      list.sort(Comparator.comparingDouble(Pair::getSecond));

      for(Pair<GuiEventListener, Long> pair : list) {
         ComponentPath componentpath = pair.getFirst().nextFocusPath(focusnavigationevent);
         if (componentpath != null) {
            return componentpath;
         }
      }

      return null;
   }
}
