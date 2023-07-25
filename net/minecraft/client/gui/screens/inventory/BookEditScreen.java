package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class BookEditScreen extends Screen {
   private static final int TEXT_WIDTH = 114;
   private static final int TEXT_HEIGHT = 128;
   private static final int IMAGE_WIDTH = 192;
   private static final int IMAGE_HEIGHT = 192;
   private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
   private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
   private static final FormattedCharSequence BLACK_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
   private static final FormattedCharSequence GRAY_CURSOR = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
   private final Player owner;
   private final ItemStack book;
   private boolean isModified;
   private boolean isSigning;
   private int frameTick;
   private int currentPage;
   private final List<String> pages = Lists.newArrayList();
   private String title = "";
   private final TextFieldHelper pageEdit = new TextFieldHelper(this::getCurrentPageText, this::setCurrentPageText, this::getClipboard, this::setClipboard, (s4) -> s4.length() < 1024 && this.font.wordWrapHeight(s4, 114) <= 128);
   private final TextFieldHelper titleEdit = new TextFieldHelper(() -> this.title, (s3) -> this.title = s3, this::getClipboard, this::setClipboard, (s2) -> s2.length() < 16);
   private long lastClickTime;
   private int lastIndex = -1;
   private PageButton forwardButton;
   private PageButton backButton;
   private Button doneButton;
   private Button signButton;
   private Button finalizeButton;
   private Button cancelButton;
   private final InteractionHand hand;
   @Nullable
   private BookEditScreen.DisplayCache displayCache = BookEditScreen.DisplayCache.EMPTY;
   private Component pageMsg = CommonComponents.EMPTY;
   private final Component ownerText;

   public BookEditScreen(Player player, ItemStack itemstack, InteractionHand interactionhand) {
      super(GameNarrator.NO_TITLE);
      this.owner = player;
      this.book = itemstack;
      this.hand = interactionhand;
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null) {
         BookViewScreen.loadPages(compoundtag, this.pages::add);
      }

      if (this.pages.isEmpty()) {
         this.pages.add("");
      }

      this.ownerText = Component.translatable("book.byAuthor", player.getName()).withStyle(ChatFormatting.DARK_GRAY);
   }

   private void setClipboard(String s) {
      if (this.minecraft != null) {
         TextFieldHelper.setClipboardContents(this.minecraft, s);
      }

   }

   private String getClipboard() {
      return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
   }

   private int getNumPages() {
      return this.pages.size();
   }

   public void tick() {
      super.tick();
      ++this.frameTick;
   }

   protected void init() {
      this.clearDisplayCache();
      this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (button5) -> {
         this.isSigning = true;
         this.updateButtonVisibility();
      }).bounds(this.width / 2 - 100, 196, 98, 20).build());
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button4) -> {
         this.minecraft.setScreen((Screen)null);
         this.saveChanges(false);
      }).bounds(this.width / 2 + 2, 196, 98, 20).build());
      this.finalizeButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (button3) -> {
         if (this.isSigning) {
            this.saveChanges(true);
            this.minecraft.setScreen((Screen)null);
         }

      }).bounds(this.width / 2 - 100, 196, 98, 20).build());
      this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button2) -> {
         if (this.isSigning) {
            this.isSigning = false;
         }

         this.updateButtonVisibility();
      }).bounds(this.width / 2 + 2, 196, 98, 20).build());
      int i = (this.width - 192) / 2;
      int j = 2;
      this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, (button1) -> this.pageForward(), true));
      this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, (button) -> this.pageBack(), true));
      this.updateButtonVisibility();
   }

   private void pageBack() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   private void pageForward() {
      if (this.currentPage < this.getNumPages() - 1) {
         ++this.currentPage;
      } else {
         this.appendPageToBook();
         if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
         }
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   private void updateButtonVisibility() {
      this.backButton.visible = !this.isSigning && this.currentPage > 0;
      this.forwardButton.visible = !this.isSigning;
      this.doneButton.visible = !this.isSigning;
      this.signButton.visible = !this.isSigning;
      this.cancelButton.visible = this.isSigning;
      this.finalizeButton.visible = this.isSigning;
      this.finalizeButton.active = !this.title.trim().isEmpty();
   }

   private void eraseEmptyTrailingPages() {
      ListIterator<String> listiterator = this.pages.listIterator(this.pages.size());

      while(listiterator.hasPrevious() && listiterator.previous().isEmpty()) {
         listiterator.remove();
      }

   }

   private void saveChanges(boolean flag) {
      if (this.isModified) {
         this.eraseEmptyTrailingPages();
         this.updateLocalCopy(flag);
         int i = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().selected : 40;
         this.minecraft.getConnection().send(new ServerboundEditBookPacket(i, this.pages, flag ? Optional.of(this.title.trim()) : Optional.empty()));
      }
   }

   private void updateLocalCopy(boolean flag) {
      ListTag listtag = new ListTag();
      this.pages.stream().map(StringTag::valueOf).forEach(listtag::add);
      if (!this.pages.isEmpty()) {
         this.book.addTagElement("pages", listtag);
      }

      if (flag) {
         this.book.addTagElement("author", StringTag.valueOf(this.owner.getGameProfile().getName()));
         this.book.addTagElement("title", StringTag.valueOf(this.title.trim()));
      }

   }

   private void appendPageToBook() {
      if (this.getNumPages() < 100) {
         this.pages.add("");
         this.isModified = true;
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      if (super.keyPressed(i, j, k)) {
         return true;
      } else if (this.isSigning) {
         return this.titleKeyPressed(i, j, k);
      } else {
         boolean flag = this.bookKeyPressed(i, j, k);
         if (flag) {
            this.clearDisplayCache();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean charTyped(char c0, int i) {
      if (super.charTyped(c0, i)) {
         return true;
      } else if (this.isSigning) {
         boolean flag = this.titleEdit.charTyped(c0);
         if (flag) {
            this.updateButtonVisibility();
            this.isModified = true;
            return true;
         } else {
            return false;
         }
      } else if (SharedConstants.isAllowedChatCharacter(c0)) {
         this.pageEdit.insertText(Character.toString(c0));
         this.clearDisplayCache();
         return true;
      } else {
         return false;
      }
   }

   private boolean bookKeyPressed(int i, int j, int k) {
      if (Screen.isSelectAll(i)) {
         this.pageEdit.selectAll();
         return true;
      } else if (Screen.isCopy(i)) {
         this.pageEdit.copy();
         return true;
      } else if (Screen.isPaste(i)) {
         this.pageEdit.paste();
         return true;
      } else if (Screen.isCut(i)) {
         this.pageEdit.cut();
         return true;
      } else {
         TextFieldHelper.CursorStep textfieldhelper_cursorstep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
         switch (i) {
            case 257:
            case 335:
               this.pageEdit.insertText("\n");
               return true;
            case 259:
               this.pageEdit.removeFromCursor(-1, textfieldhelper_cursorstep);
               return true;
            case 261:
               this.pageEdit.removeFromCursor(1, textfieldhelper_cursorstep);
               return true;
            case 262:
               this.pageEdit.moveBy(1, Screen.hasShiftDown(), textfieldhelper_cursorstep);
               return true;
            case 263:
               this.pageEdit.moveBy(-1, Screen.hasShiftDown(), textfieldhelper_cursorstep);
               return true;
            case 264:
               this.keyDown();
               return true;
            case 265:
               this.keyUp();
               return true;
            case 266:
               this.backButton.onPress();
               return true;
            case 267:
               this.forwardButton.onPress();
               return true;
            case 268:
               this.keyHome();
               return true;
            case 269:
               this.keyEnd();
               return true;
            default:
               return false;
         }
      }
   }

   private void keyUp() {
      this.changeLine(-1);
   }

   private void keyDown() {
      this.changeLine(1);
   }

   private void changeLine(int i) {
      int j = this.pageEdit.getCursorPos();
      int k = this.getDisplayCache().changeLine(j, i);
      this.pageEdit.setCursorPos(k, Screen.hasShiftDown());
   }

   private void keyHome() {
      if (Screen.hasControlDown()) {
         this.pageEdit.setCursorToStart(Screen.hasShiftDown());
      } else {
         int i = this.pageEdit.getCursorPos();
         int j = this.getDisplayCache().findLineStart(i);
         this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
      }

   }

   private void keyEnd() {
      if (Screen.hasControlDown()) {
         this.pageEdit.setCursorToEnd(Screen.hasShiftDown());
      } else {
         BookEditScreen.DisplayCache bookeditscreen_displaycache = this.getDisplayCache();
         int i = this.pageEdit.getCursorPos();
         int j = bookeditscreen_displaycache.findLineEnd(i);
         this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
      }

   }

   private boolean titleKeyPressed(int i, int j, int k) {
      switch (i) {
         case 257:
         case 335:
            if (!this.title.isEmpty()) {
               this.saveChanges(true);
               this.minecraft.setScreen((Screen)null);
            }

            return true;
         case 259:
            this.titleEdit.removeCharsFromCursor(-1);
            this.updateButtonVisibility();
            this.isModified = true;
            return true;
         default:
            return false;
      }
   }

   private String getCurrentPageText() {
      return this.currentPage >= 0 && this.currentPage < this.pages.size() ? this.pages.get(this.currentPage) : "";
   }

   private void setCurrentPageText(String s1) {
      if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
         this.pages.set(this.currentPage, s1);
         this.isModified = true;
         this.clearDisplayCache();
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.setFocused((GuiEventListener)null);
      int k = (this.width - 192) / 2;
      int l = 2;
      guigraphics.blit(BookViewScreen.BOOK_LOCATION, k, 2, 0, 0, 192, 192);
      if (this.isSigning) {
         boolean flag = this.frameTick / 6 % 2 == 0;
         FormattedCharSequence formattedcharsequence = FormattedCharSequence.composite(FormattedCharSequence.forward(this.title, Style.EMPTY), flag ? BLACK_CURSOR : GRAY_CURSOR);
         int i1 = this.font.width(EDIT_TITLE_LABEL);
         guigraphics.drawString(this.font, EDIT_TITLE_LABEL, k + 36 + (114 - i1) / 2, 34, 0, false);
         int j1 = this.font.width(formattedcharsequence);
         guigraphics.drawString(this.font, formattedcharsequence, k + 36 + (114 - j1) / 2, 50, 0, false);
         int k1 = this.font.width(this.ownerText);
         guigraphics.drawString(this.font, this.ownerText, k + 36 + (114 - k1) / 2, 60, 0, false);
         guigraphics.drawWordWrap(this.font, FINALIZE_WARNING_LABEL, k + 36, 82, 114, 0);
      } else {
         int l1 = this.font.width(this.pageMsg);
         guigraphics.drawString(this.font, this.pageMsg, k - l1 + 192 - 44, 18, 0, false);
         BookEditScreen.DisplayCache bookeditscreen_displaycache = this.getDisplayCache();

         for(BookEditScreen.LineInfo bookeditscreen_lineinfo : bookeditscreen_displaycache.lines) {
            guigraphics.drawString(this.font, bookeditscreen_lineinfo.asComponent, bookeditscreen_lineinfo.x, bookeditscreen_lineinfo.y, -16777216, false);
         }

         this.renderHighlight(guigraphics, bookeditscreen_displaycache.selection);
         this.renderCursor(guigraphics, bookeditscreen_displaycache.cursor, bookeditscreen_displaycache.cursorAtEnd);
      }

      super.render(guigraphics, i, j, f);
   }

   private void renderCursor(GuiGraphics guigraphics, BookEditScreen.Pos2i bookeditscreen_pos2i, boolean flag) {
      if (this.frameTick / 6 % 2 == 0) {
         bookeditscreen_pos2i = this.convertLocalToScreen(bookeditscreen_pos2i);
         if (!flag) {
            guigraphics.fill(bookeditscreen_pos2i.x, bookeditscreen_pos2i.y - 1, bookeditscreen_pos2i.x + 1, bookeditscreen_pos2i.y + 9, -16777216);
         } else {
            guigraphics.drawString(this.font, "_", bookeditscreen_pos2i.x, bookeditscreen_pos2i.y, 0, false);
         }
      }

   }

   private void renderHighlight(GuiGraphics guigraphics, Rect2i[] arect2i) {
      for(Rect2i rect2i : arect2i) {
         int i = rect2i.getX();
         int j = rect2i.getY();
         int k = i + rect2i.getWidth();
         int l = j + rect2i.getHeight();
         guigraphics.fill(RenderType.guiTextHighlight(), i, j, k, l, -16776961);
      }

   }

   private BookEditScreen.Pos2i convertScreenToLocal(BookEditScreen.Pos2i bookeditscreen_pos2i) {
      return new BookEditScreen.Pos2i(bookeditscreen_pos2i.x - (this.width - 192) / 2 - 36, bookeditscreen_pos2i.y - 32);
   }

   private BookEditScreen.Pos2i convertLocalToScreen(BookEditScreen.Pos2i bookeditscreen_pos2i) {
      return new BookEditScreen.Pos2i(bookeditscreen_pos2i.x + (this.width - 192) / 2 + 36, bookeditscreen_pos2i.y + 32);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (super.mouseClicked(d0, d1, i)) {
         return true;
      } else {
         if (i == 0) {
            long j = Util.getMillis();
            BookEditScreen.DisplayCache bookeditscreen_displaycache = this.getDisplayCache();
            int k = bookeditscreen_displaycache.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)d0, (int)d1)));
            if (k >= 0) {
               if (k == this.lastIndex && j - this.lastClickTime < 250L) {
                  if (!this.pageEdit.isSelecting()) {
                     this.selectWord(k);
                  } else {
                     this.pageEdit.selectAll();
                  }
               } else {
                  this.pageEdit.setCursorPos(k, Screen.hasShiftDown());
               }

               this.clearDisplayCache();
            }

            this.lastIndex = k;
            this.lastClickTime = j;
         }

         return true;
      }
   }

   private void selectWord(int i) {
      String s = this.getCurrentPageText();
      this.pageEdit.setSelectionRange(StringSplitter.getWordPosition(s, -1, i, false), StringSplitter.getWordPosition(s, 1, i, false));
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (super.mouseDragged(d0, d1, i, d2, d3)) {
         return true;
      } else {
         if (i == 0) {
            BookEditScreen.DisplayCache bookeditscreen_displaycache = this.getDisplayCache();
            int j = bookeditscreen_displaycache.getIndexAtPosition(this.font, this.convertScreenToLocal(new BookEditScreen.Pos2i((int)d0, (int)d1)));
            this.pageEdit.setCursorPos(j, true);
            this.clearDisplayCache();
         }

         return true;
      }
   }

   private BookEditScreen.DisplayCache getDisplayCache() {
      if (this.displayCache == null) {
         this.displayCache = this.rebuildDisplayCache();
         this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, this.getNumPages());
      }

      return this.displayCache;
   }

   private void clearDisplayCache() {
      this.displayCache = null;
   }

   private void clearDisplayCacheAfterPageChange() {
      this.pageEdit.setCursorToEnd();
      this.clearDisplayCache();
   }

   private BookEditScreen.DisplayCache rebuildDisplayCache() {
      String s = this.getCurrentPageText();
      if (s.isEmpty()) {
         return BookEditScreen.DisplayCache.EMPTY;
      } else {
         int i = this.pageEdit.getCursorPos();
         int j = this.pageEdit.getSelectionPos();
         IntList intlist = new IntArrayList();
         List<BookEditScreen.LineInfo> list = Lists.newArrayList();
         MutableInt mutableint = new MutableInt();
         MutableBoolean mutableboolean = new MutableBoolean();
         StringSplitter stringsplitter = this.font.getSplitter();
         stringsplitter.splitLines(s, 114, Style.EMPTY, true, (style, k3, l3) -> {
            int i4 = mutableint.getAndIncrement();
            String s3 = s.substring(k3, l3);
            mutableboolean.setValue(s3.endsWith("\n"));
            String s4 = StringUtils.stripEnd(s3, " \n");
            int j4 = i4 * 9;
            BookEditScreen.Pos2i bookeditscreen_pos2i2 = this.convertLocalToScreen(new BookEditScreen.Pos2i(0, j4));
            intlist.add(k3);
            list.add(new BookEditScreen.LineInfo(style, s4, bookeditscreen_pos2i2.x, bookeditscreen_pos2i2.y));
         });
         int[] aint = intlist.toIntArray();
         boolean flag = i == s.length();
         BookEditScreen.Pos2i bookeditscreen_pos2i;
         if (flag && mutableboolean.isTrue()) {
            bookeditscreen_pos2i = new BookEditScreen.Pos2i(0, list.size() * 9);
         } else {
            int k = findLineFromPos(aint, i);
            int l = this.font.width(s.substring(aint[k], i));
            bookeditscreen_pos2i = new BookEditScreen.Pos2i(l, k * 9);
         }

         List<Rect2i> list1 = Lists.newArrayList();
         if (i != j) {
            int i1 = Math.min(i, j);
            int j1 = Math.max(i, j);
            int k1 = findLineFromPos(aint, i1);
            int l1 = findLineFromPos(aint, j1);
            if (k1 == l1) {
               int i2 = k1 * 9;
               int j2 = aint[k1];
               list1.add(this.createPartialLineSelection(s, stringsplitter, i1, j1, i2, j2));
            } else {
               int k2 = k1 + 1 > aint.length ? s.length() : aint[k1 + 1];
               list1.add(this.createPartialLineSelection(s, stringsplitter, i1, k2, k1 * 9, aint[k1]));

               for(int l2 = k1 + 1; l2 < l1; ++l2) {
                  int i3 = l2 * 9;
                  String s1 = s.substring(aint[l2], aint[l2 + 1]);
                  int j3 = (int)stringsplitter.stringWidth(s1);
                  list1.add(this.createSelection(new BookEditScreen.Pos2i(0, i3), new BookEditScreen.Pos2i(j3, i3 + 9)));
               }

               list1.add(this.createPartialLineSelection(s, stringsplitter, aint[l1], j1, l1 * 9, aint[l1]));
            }
         }

         return new BookEditScreen.DisplayCache(s, bookeditscreen_pos2i, flag, aint, list.toArray(new BookEditScreen.LineInfo[0]), list1.toArray(new Rect2i[0]));
      }
   }

   static int findLineFromPos(int[] aint, int i) {
      int j = Arrays.binarySearch(aint, i);
      return j < 0 ? -(j + 2) : j;
   }

   private Rect2i createPartialLineSelection(String s, StringSplitter stringsplitter, int i, int j, int k, int l) {
      String s1 = s.substring(l, i);
      String s2 = s.substring(l, j);
      BookEditScreen.Pos2i bookeditscreen_pos2i = new BookEditScreen.Pos2i((int)stringsplitter.stringWidth(s1), k);
      BookEditScreen.Pos2i bookeditscreen_pos2i1 = new BookEditScreen.Pos2i((int)stringsplitter.stringWidth(s2), k + 9);
      return this.createSelection(bookeditscreen_pos2i, bookeditscreen_pos2i1);
   }

   private Rect2i createSelection(BookEditScreen.Pos2i bookeditscreen_pos2i, BookEditScreen.Pos2i bookeditscreen_pos2i1) {
      BookEditScreen.Pos2i bookeditscreen_pos2i2 = this.convertLocalToScreen(bookeditscreen_pos2i);
      BookEditScreen.Pos2i bookeditscreen_pos2i3 = this.convertLocalToScreen(bookeditscreen_pos2i1);
      int i = Math.min(bookeditscreen_pos2i2.x, bookeditscreen_pos2i3.x);
      int j = Math.max(bookeditscreen_pos2i2.x, bookeditscreen_pos2i3.x);
      int k = Math.min(bookeditscreen_pos2i2.y, bookeditscreen_pos2i3.y);
      int l = Math.max(bookeditscreen_pos2i2.y, bookeditscreen_pos2i3.y);
      return new Rect2i(i, k, j - i, l - k);
   }

   static class DisplayCache {
      static final BookEditScreen.DisplayCache EMPTY = new BookEditScreen.DisplayCache("", new BookEditScreen.Pos2i(0, 0), true, new int[]{0}, new BookEditScreen.LineInfo[]{new BookEditScreen.LineInfo(Style.EMPTY, "", 0, 0)}, new Rect2i[0]);
      private final String fullText;
      final BookEditScreen.Pos2i cursor;
      final boolean cursorAtEnd;
      private final int[] lineStarts;
      final BookEditScreen.LineInfo[] lines;
      final Rect2i[] selection;

      public DisplayCache(String s, BookEditScreen.Pos2i bookeditscreen_pos2i, boolean flag, int[] aint, BookEditScreen.LineInfo[] abookeditscreen_lineinfo, Rect2i[] arect2i) {
         this.fullText = s;
         this.cursor = bookeditscreen_pos2i;
         this.cursorAtEnd = flag;
         this.lineStarts = aint;
         this.lines = abookeditscreen_lineinfo;
         this.selection = arect2i;
      }

      public int getIndexAtPosition(Font font, BookEditScreen.Pos2i bookeditscreen_pos2i) {
         int i = bookeditscreen_pos2i.y / 9;
         if (i < 0) {
            return 0;
         } else if (i >= this.lines.length) {
            return this.fullText.length();
         } else {
            BookEditScreen.LineInfo bookeditscreen_lineinfo = this.lines[i];
            return this.lineStarts[i] + font.getSplitter().plainIndexAtWidth(bookeditscreen_lineinfo.contents, bookeditscreen_pos2i.x, bookeditscreen_lineinfo.style);
         }
      }

      public int changeLine(int i, int j) {
         int k = BookEditScreen.findLineFromPos(this.lineStarts, i);
         int l = k + j;
         int k1;
         if (0 <= l && l < this.lineStarts.length) {
            int i1 = i - this.lineStarts[k];
            int j1 = this.lines[l].contents.length();
            k1 = this.lineStarts[l] + Math.min(i1, j1);
         } else {
            k1 = i;
         }

         return k1;
      }

      public int findLineStart(int i) {
         int j = BookEditScreen.findLineFromPos(this.lineStarts, i);
         return this.lineStarts[j];
      }

      public int findLineEnd(int i) {
         int j = BookEditScreen.findLineFromPos(this.lineStarts, i);
         return this.lineStarts[j] + this.lines[j].contents.length();
      }
   }

   static class LineInfo {
      final Style style;
      final String contents;
      final Component asComponent;
      final int x;
      final int y;

      public LineInfo(Style style, String s, int i, int j) {
         this.style = style;
         this.contents = s;
         this.x = i;
         this.y = j;
         this.asComponent = Component.literal(s).setStyle(style);
      }
   }

   static class Pos2i {
      public final int x;
      public final int y;

      Pos2i(int i, int j) {
         this.x = i;
         this.y = j;
      }
   }
}
