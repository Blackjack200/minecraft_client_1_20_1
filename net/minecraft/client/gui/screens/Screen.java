package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

public abstract class Screen extends AbstractContainerEventHandler implements Renderable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
   private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
   public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
   protected final Component title;
   private final List<GuiEventListener> children = Lists.newArrayList();
   private final List<NarratableEntry> narratables = Lists.newArrayList();
   @Nullable
   protected Minecraft minecraft;
   private boolean initialized;
   public int width;
   public int height;
   private final List<Renderable> renderables = Lists.newArrayList();
   protected Font font;
   @Nullable
   private URI clickedLink;
   private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
   private static final long NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME;
   private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
   private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
   private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
   private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
   private long narrationSuppressTime = Long.MIN_VALUE;
   private long nextNarrationTime = Long.MAX_VALUE;
   @Nullable
   private NarratableEntry lastNarratable;
   @Nullable
   private Screen.DeferredTooltipRendering deferredTooltipRendering;
   protected final Executor screenExecutor = (runnable) -> this.minecraft.execute(() -> {
         if (this.minecraft.screen == this) {
            runnable.run();
         }

      });

   protected Screen(Component component) {
      this.title = component;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getNarrationMessage() {
      return this.getTitle();
   }

   public final void renderWithTooltip(GuiGraphics guigraphics, int i, int j, float f) {
      this.render(guigraphics, i, j, f);
      if (this.deferredTooltipRendering != null) {
         guigraphics.renderTooltip(this.font, this.deferredTooltipRendering.tooltip(), this.deferredTooltipRendering.positioner(), i, j);
         this.deferredTooltipRendering = null;
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      for(Renderable renderable : this.renderables) {
         renderable.render(guigraphics, i, j, f);
      }

   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256 && this.shouldCloseOnEsc()) {
         this.onClose();
         return true;
      } else if (super.keyPressed(i, j, k)) {
         return true;
      } else {
         Object var10000;
         switch (i) {
            case 258:
               var10000 = this.createTabEvent();
               break;
            case 259:
            case 260:
            case 261:
            default:
               var10000 = null;
               break;
            case 262:
               var10000 = this.createArrowEvent(ScreenDirection.RIGHT);
               break;
            case 263:
               var10000 = this.createArrowEvent(ScreenDirection.LEFT);
               break;
            case 264:
               var10000 = this.createArrowEvent(ScreenDirection.DOWN);
               break;
            case 265:
               var10000 = this.createArrowEvent(ScreenDirection.UP);
         }

         FocusNavigationEvent focusnavigationevent = (FocusNavigationEvent)var10000;
         if (focusnavigationevent != null) {
            ComponentPath componentpath = super.nextFocusPath(focusnavigationevent);
            if (componentpath == null && focusnavigationevent instanceof FocusNavigationEvent.TabNavigation) {
               this.clearFocus();
               componentpath = super.nextFocusPath(focusnavigationevent);
            }

            if (componentpath != null) {
               this.changeFocus(componentpath);
            }
         }

         return false;
      }
   }

   private FocusNavigationEvent.TabNavigation createTabEvent() {
      boolean flag = !hasShiftDown();
      return new FocusNavigationEvent.TabNavigation(flag);
   }

   private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection screendirection) {
      return new FocusNavigationEvent.ArrowNavigation(screendirection);
   }

   protected void setInitialFocus(GuiEventListener guieventlistener) {
      ComponentPath componentpath = ComponentPath.path(this, guieventlistener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
      if (componentpath != null) {
         this.changeFocus(componentpath);
      }

   }

   private void clearFocus() {
      ComponentPath componentpath = this.getCurrentFocusPath();
      if (componentpath != null) {
         componentpath.applyFocus(false);
      }

   }

   @VisibleForTesting
   protected void changeFocus(ComponentPath componentpath) {
      this.clearFocus();
      componentpath.applyFocus(true);
   }

   public boolean shouldCloseOnEsc() {
      return true;
   }

   public void onClose() {
      this.minecraft.setScreen((Screen)null);
   }

   protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guieventlistener) {
      this.renderables.add(guieventlistener);
      return this.addWidget(guieventlistener);
   }

   protected <T extends Renderable> T addRenderableOnly(T renderable) {
      this.renderables.add(renderable);
      return renderable;
   }

   protected <T extends GuiEventListener & NarratableEntry> T addWidget(T guieventlistener) {
      this.children.add(guieventlistener);
      this.narratables.add(guieventlistener);
      return guieventlistener;
   }

   protected void removeWidget(GuiEventListener guieventlistener) {
      if (guieventlistener instanceof Renderable) {
         this.renderables.remove((Renderable)guieventlistener);
      }

      if (guieventlistener instanceof NarratableEntry) {
         this.narratables.remove((NarratableEntry)guieventlistener);
      }

      this.children.remove(guieventlistener);
   }

   protected void clearWidgets() {
      this.renderables.clear();
      this.children.clear();
      this.narratables.clear();
   }

   public static List<Component> getTooltipFromItem(Minecraft minecraft, ItemStack itemstack) {
      return itemstack.getTooltipLines(minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
   }

   protected void insertText(String s, boolean flag) {
   }

   public boolean handleComponentClicked(@Nullable Style style) {
      if (style == null) {
         return false;
      } else {
         ClickEvent clickevent = style.getClickEvent();
         if (hasShiftDown()) {
            if (style.getInsertion() != null) {
               this.insertText(style.getInsertion(), false);
            }
         } else if (clickevent != null) {
            if (clickevent.getAction() == ClickEvent.Action.OPEN_URL) {
               if (!this.minecraft.options.chatLinks().get()) {
                  return false;
               }

               try {
                  URI uri = new URI(clickevent.getValue());
                  String s = uri.getScheme();
                  if (s == null) {
                     throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                  }

                  if (!ALLOWED_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
                     throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
                  }

                  if (this.minecraft.options.chatLinksPrompt().get()) {
                     this.clickedLink = uri;
                     this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickevent.getValue(), false));
                  } else {
                     this.openLink(uri);
                  }
               } catch (URISyntaxException var5) {
                  LOGGER.error("Can't open url for {}", clickevent, var5);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE) {
               URI uri1 = (new File(clickevent.getValue())).toURI();
               this.openLink(uri1);
            } else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
               this.insertText(SharedConstants.filterText(clickevent.getValue()), true);
            } else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               String s1 = SharedConstants.filterText(clickevent.getValue());
               if (s1.startsWith("/")) {
                  if (!this.minecraft.player.connection.sendUnsignedCommand(s1.substring(1))) {
                     LOGGER.error("Not allowed to run command with signed argument from click event: '{}'", (Object)s1);
                  }
               } else {
                  LOGGER.error("Failed to run command without '/' prefix from click event: '{}'", (Object)s1);
               }
            } else if (clickevent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD) {
               this.minecraft.keyboardHandler.setClipboard(clickevent.getValue());
            } else {
               LOGGER.error("Don't know how to handle {}", (Object)clickevent);
            }

            return true;
         }

         return false;
      }
   }

   public final void init(Minecraft minecraft, int i, int j) {
      this.minecraft = minecraft;
      this.font = minecraft.font;
      this.width = i;
      this.height = j;
      if (!this.initialized) {
         this.init();
      } else {
         this.repositionElements();
      }

      this.initialized = true;
      this.triggerImmediateNarration(false);
      this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
   }

   protected void rebuildWidgets() {
      this.clearWidgets();
      this.clearFocus();
      this.init();
   }

   public List<? extends GuiEventListener> children() {
      return this.children;
   }

   protected void init() {
   }

   public void tick() {
   }

   public void removed() {
   }

   public void added() {
   }

   public void renderBackground(GuiGraphics guigraphics) {
      if (this.minecraft.level != null) {
         guigraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
      } else {
         this.renderDirtBackground(guigraphics);
      }

   }

   public void renderDirtBackground(GuiGraphics guigraphics) {
      guigraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
      int i = 32;
      guigraphics.blit(BACKGROUND_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean isPauseScreen() {
      return true;
   }

   private void confirmLink(boolean flag) {
      if (flag) {
         this.openLink(this.clickedLink);
      }

      this.clickedLink = null;
      this.minecraft.setScreen(this);
   }

   private void openLink(URI uri) {
      Util.getPlatform().openUri(uri);
   }

   public static boolean hasControlDown() {
      if (Minecraft.ON_OSX) {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
      } else {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
      }
   }

   public static boolean hasShiftDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
   }

   public static boolean hasAltDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
   }

   public static boolean isCut(int i) {
      return i == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isPaste(int i) {
      return i == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isCopy(int i) {
      return i == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   public static boolean isSelectAll(int i) {
      return i == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
   }

   protected void repositionElements() {
      this.rebuildWidgets();
   }

   public void resize(Minecraft minecraft, int i, int j) {
      this.width = i;
      this.height = j;
      this.repositionElements();
   }

   public static void wrapScreenError(Runnable runnable, String s, String s1) {
      try {
         runnable.run();
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.forThrowable(var6, s);
         CrashReportCategory crashreportcategory = crashreport.addCategory("Affected screen");
         crashreportcategory.setDetail("Screen name", () -> s1);
         throw new ReportedException(crashreport);
      }
   }

   protected boolean isValidCharacterForName(String s, char c0, int i) {
      int j = s.indexOf(58);
      int k = s.indexOf(47);
      if (c0 == ':') {
         return (k == -1 || i <= k) && j == -1;
      } else if (c0 == '/') {
         return i > j;
      } else {
         return c0 == '_' || c0 == '-' || c0 >= 'a' && c0 <= 'z' || c0 >= '0' && c0 <= '9' || c0 == '.';
      }
   }

   public boolean isMouseOver(double d0, double d1) {
      return true;
   }

   public void onFilesDrop(List<Path> list) {
   }

   private void scheduleNarration(long i, boolean flag) {
      this.nextNarrationTime = Util.getMillis() + i;
      if (flag) {
         this.narrationSuppressTime = Long.MIN_VALUE;
      }

   }

   private void suppressNarration(long i) {
      this.narrationSuppressTime = Util.getMillis() + i;
   }

   public void afterMouseMove() {
      this.scheduleNarration(750L, false);
   }

   public void afterMouseAction() {
      this.scheduleNarration(200L, true);
   }

   public void afterKeyboardAction() {
      this.scheduleNarration(200L, true);
   }

   private boolean shouldRunNarration() {
      return this.minecraft.getNarrator().isActive();
   }

   public void handleDelayedNarration() {
      if (this.shouldRunNarration()) {
         long i = Util.getMillis();
         if (i > this.nextNarrationTime && i > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
         }
      }

   }

   public void triggerImmediateNarration(boolean flag) {
      if (this.shouldRunNarration()) {
         this.runNarration(flag);
      }

   }

   private void runNarration(boolean flag) {
      this.narrationState.update(this::updateNarrationState);
      String s = this.narrationState.collectNarrationText(!flag);
      if (!s.isEmpty()) {
         this.minecraft.getNarrator().sayNow(s);
      }

   }

   protected boolean shouldNarrateNavigation() {
      return true;
   }

   protected void updateNarrationState(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
      if (this.shouldNarrateNavigation()) {
         narrationelementoutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
      }

      this.updateNarratedWidget(narrationelementoutput);
   }

   protected void updateNarratedWidget(NarrationElementOutput narrationelementoutput) {
      List<NarratableEntry> list = this.narratables.stream().filter(NarratableEntry::isActive).collect(Collectors.toList());
      Collections.sort(list, Comparator.comparingInt(TabOrderedElement::getTabOrderGroup));
      Screen.NarratableSearchResult screen_narratablesearchresult = findNarratableWidget(list, this.lastNarratable);
      if (screen_narratablesearchresult != null) {
         if (screen_narratablesearchresult.priority.isTerminal()) {
            this.lastNarratable = screen_narratablesearchresult.entry;
         }

         if (list.size() > 1) {
            narrationelementoutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.screen", screen_narratablesearchresult.index + 1, list.size()));
            if (screen_narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
               narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
            }
         }

         screen_narratablesearchresult.entry.updateNarration(narrationelementoutput.nest());
      }

   }

   @Nullable
   public static Screen.NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableentry) {
      Screen.NarratableSearchResult screen_narratablesearchresult = null;
      Screen.NarratableSearchResult screen_narratablesearchresult1 = null;
      int i = 0;

      for(int j = list.size(); i < j; ++i) {
         NarratableEntry narratableentry1 = list.get(i);
         NarratableEntry.NarrationPriority narratableentry_narrationpriority = narratableentry1.narrationPriority();
         if (narratableentry_narrationpriority.isTerminal()) {
            if (narratableentry1 != narratableentry) {
               return new Screen.NarratableSearchResult(narratableentry1, i, narratableentry_narrationpriority);
            }

            screen_narratablesearchresult1 = new Screen.NarratableSearchResult(narratableentry1, i, narratableentry_narrationpriority);
         } else if (narratableentry_narrationpriority.compareTo(screen_narratablesearchresult != null ? screen_narratablesearchresult.priority : NarratableEntry.NarrationPriority.NONE) > 0) {
            screen_narratablesearchresult = new Screen.NarratableSearchResult(narratableentry1, i, narratableentry_narrationpriority);
         }
      }

      return screen_narratablesearchresult != null ? screen_narratablesearchresult : screen_narratablesearchresult1;
   }

   public void narrationEnabled() {
      this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> list) {
      this.setTooltipForNextRenderPass(list, DefaultTooltipPositioner.INSTANCE, true);
   }

   public void setTooltipForNextRenderPass(List<FormattedCharSequence> list, ClientTooltipPositioner clienttooltippositioner, boolean flag) {
      if (this.deferredTooltipRendering == null || flag) {
         this.deferredTooltipRendering = new Screen.DeferredTooltipRendering(list, clienttooltippositioner);
      }

   }

   protected void setTooltipForNextRenderPass(Component component) {
      this.setTooltipForNextRenderPass(Tooltip.splitTooltip(this.minecraft, component));
   }

   public void setTooltipForNextRenderPass(Tooltip tooltip, ClientTooltipPositioner clienttooltippositioner, boolean flag) {
      this.setTooltipForNextRenderPass(tooltip.toCharSequence(this.minecraft), clienttooltippositioner, flag);
   }

   protected static void hideWidgets(AbstractWidget... aabstractwidget) {
      for(AbstractWidget abstractwidget : aabstractwidget) {
         abstractwidget.visible = false;
      }

   }

   public ScreenRectangle getRectangle() {
      return new ScreenRectangle(0, 0, this.width, this.height);
   }

   @Nullable
   public Music getBackgroundMusic() {
      return null;
   }

   static record DeferredTooltipRendering(List<FormattedCharSequence> tooltip, ClientTooltipPositioner positioner) {
   }

   public static class NarratableSearchResult {
      public final NarratableEntry entry;
      public final int index;
      public final NarratableEntry.NarrationPriority priority;

      public NarratableSearchResult(NarratableEntry narratableentry, int i, NarratableEntry.NarrationPriority narratableentry_narrationpriority) {
         this.entry = narratableentry;
         this.index = i;
         this.priority = narratableentry_narrationpriority;
      }
   }
}
