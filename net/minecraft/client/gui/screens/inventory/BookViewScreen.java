package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

public class BookViewScreen extends Screen {
   public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
   public static final int PAGE_TEXT_X_OFFSET = 36;
   public static final int PAGE_TEXT_Y_OFFSET = 30;
   public static final BookViewScreen.BookAccess EMPTY_ACCESS = new BookViewScreen.BookAccess() {
      public int getPageCount() {
         return 0;
      }

      public FormattedText getPageRaw(int i) {
         return FormattedText.EMPTY;
      }
   };
   public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
   protected static final int TEXT_WIDTH = 114;
   protected static final int TEXT_HEIGHT = 128;
   protected static final int IMAGE_WIDTH = 192;
   protected static final int IMAGE_HEIGHT = 192;
   private BookViewScreen.BookAccess bookAccess;
   private int currentPage;
   private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
   private int cachedPage = -1;
   private Component pageMsg = CommonComponents.EMPTY;
   private PageButton forwardButton;
   private PageButton backButton;
   private final boolean playTurnSound;

   public BookViewScreen(BookViewScreen.BookAccess bookviewscreen_bookaccess) {
      this(bookviewscreen_bookaccess, true);
   }

   public BookViewScreen() {
      this(EMPTY_ACCESS, false);
   }

   private BookViewScreen(BookViewScreen.BookAccess bookviewscreen_bookaccess, boolean flag) {
      super(GameNarrator.NO_TITLE);
      this.bookAccess = bookviewscreen_bookaccess;
      this.playTurnSound = flag;
   }

   public void setBookAccess(BookViewScreen.BookAccess bookviewscreen_bookaccess) {
      this.bookAccess = bookviewscreen_bookaccess;
      this.currentPage = Mth.clamp(this.currentPage, 0, bookviewscreen_bookaccess.getPageCount());
      this.updateButtonVisibility();
      this.cachedPage = -1;
   }

   public boolean setPage(int i) {
      int j = Mth.clamp(i, 0, this.bookAccess.getPageCount() - 1);
      if (j != this.currentPage) {
         this.currentPage = j;
         this.updateButtonVisibility();
         this.cachedPage = -1;
         return true;
      } else {
         return false;
      }
   }

   protected boolean forcePage(int i) {
      return this.setPage(i);
   }

   protected void init() {
      this.createMenuControls();
      this.createPageControlButtons();
   }

   protected void createMenuControls() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).bounds(this.width / 2 - 100, 196, 200, 20).build());
   }

   protected void createPageControlButtons() {
      int i = (this.width - 192) / 2;
      int j = 2;
      this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, (button1) -> this.pageForward(), this.playTurnSound));
      this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, (button) -> this.pageBack(), this.playTurnSound));
      this.updateButtonVisibility();
   }

   private int getNumPages() {
      return this.bookAccess.getPageCount();
   }

   protected void pageBack() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtonVisibility();
   }

   protected void pageForward() {
      if (this.currentPage < this.getNumPages() - 1) {
         ++this.currentPage;
      }

      this.updateButtonVisibility();
   }

   private void updateButtonVisibility() {
      this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
      this.backButton.visible = this.currentPage > 0;
   }

   public boolean keyPressed(int i, int j, int k) {
      if (super.keyPressed(i, j, k)) {
         return true;
      } else {
         switch (i) {
            case 266:
               this.backButton.onPress();
               return true;
            case 267:
               this.forwardButton.onPress();
               return true;
            default:
               return false;
         }
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      int k = (this.width - 192) / 2;
      int l = 2;
      guigraphics.blit(BOOK_LOCATION, k, 2, 0, 0, 192, 192);
      if (this.cachedPage != this.currentPage) {
         FormattedText formattedtext = this.bookAccess.getPage(this.currentPage);
         this.cachedPageComponents = this.font.split(formattedtext, 114);
         this.pageMsg = Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
      }

      this.cachedPage = this.currentPage;
      int i1 = this.font.width(this.pageMsg);
      guigraphics.drawString(this.font, this.pageMsg, k - i1 + 192 - 44, 18, 0, false);
      int j1 = Math.min(128 / 9, this.cachedPageComponents.size());

      for(int k1 = 0; k1 < j1; ++k1) {
         FormattedCharSequence formattedcharsequence = this.cachedPageComponents.get(k1);
         guigraphics.drawString(this.font, formattedcharsequence, k + 36, 32 + k1 * 9, 0, false);
      }

      Style style = this.getClickedComponentStyleAt((double)i, (double)j);
      if (style != null) {
         guigraphics.renderComponentHoverEffect(this.font, style, i, j);
      }

      super.render(guigraphics, i, j, f);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (i == 0) {
         Style style = this.getClickedComponentStyleAt(d0, d1);
         if (style != null && this.handleComponentClicked(style)) {
            return true;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean handleComponentClicked(Style style) {
      ClickEvent clickevent = style.getClickEvent();
      if (clickevent == null) {
         return false;
      } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
         String s = clickevent.getValue();

         try {
            int i = Integer.parseInt(s) - 1;
            return this.forcePage(i);
         } catch (Exception var5) {
            return false;
         }
      } else {
         boolean flag = super.handleComponentClicked(style);
         if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.closeScreen();
         }

         return flag;
      }
   }

   protected void closeScreen() {
      this.minecraft.setScreen((Screen)null);
   }

   @Nullable
   public Style getClickedComponentStyleAt(double d0, double d1) {
      if (this.cachedPageComponents.isEmpty()) {
         return null;
      } else {
         int i = Mth.floor(d0 - (double)((this.width - 192) / 2) - 36.0D);
         int j = Mth.floor(d1 - 2.0D - 30.0D);
         if (i >= 0 && j >= 0) {
            int k = Math.min(128 / 9, this.cachedPageComponents.size());
            if (i <= 114 && j < 9 * k + k) {
               int l = j / 9;
               if (l >= 0 && l < this.cachedPageComponents.size()) {
                  FormattedCharSequence formattedcharsequence = this.cachedPageComponents.get(l);
                  return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedcharsequence, i);
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   static List<String> loadPages(CompoundTag compoundtag) {
      ImmutableList.Builder<String> immutablelist_builder = ImmutableList.builder();
      loadPages(compoundtag, immutablelist_builder::add);
      return immutablelist_builder.build();
   }

   public static void loadPages(CompoundTag compoundtag, Consumer<String> consumer) {
      ListTag listtag = compoundtag.getList("pages", 8).copy();
      IntFunction<String> intfunction;
      if (Minecraft.getInstance().isTextFilteringEnabled() && compoundtag.contains("filtered_pages", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("filtered_pages");
         intfunction = (j) -> {
            String s = String.valueOf(j);
            return compoundtag1.contains(s) ? compoundtag1.getString(s) : listtag.getString(j);
         };
      } else {
         intfunction = listtag::getString;
      }

      for(int i = 0; i < listtag.size(); ++i) {
         consumer.accept(intfunction.apply(i));
      }

   }

   public interface BookAccess {
      int getPageCount();

      FormattedText getPageRaw(int i);

      default FormattedText getPage(int i) {
         return i >= 0 && i < this.getPageCount() ? this.getPageRaw(i) : FormattedText.EMPTY;
      }

      static BookViewScreen.BookAccess fromItem(ItemStack itemstack) {
         if (itemstack.is(Items.WRITTEN_BOOK)) {
            return new BookViewScreen.WrittenBookAccess(itemstack);
         } else {
            return (BookViewScreen.BookAccess)(itemstack.is(Items.WRITABLE_BOOK) ? new BookViewScreen.WritableBookAccess(itemstack) : BookViewScreen.EMPTY_ACCESS);
         }
      }
   }

   public static class WritableBookAccess implements BookViewScreen.BookAccess {
      private final List<String> pages;

      public WritableBookAccess(ItemStack itemstack) {
         this.pages = readPages(itemstack);
      }

      private static List<String> readPages(ItemStack itemstack) {
         CompoundTag compoundtag = itemstack.getTag();
         return (List<String>)(compoundtag != null ? BookViewScreen.loadPages(compoundtag) : ImmutableList.of());
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public FormattedText getPageRaw(int i) {
         return FormattedText.of(this.pages.get(i));
      }
   }

   public static class WrittenBookAccess implements BookViewScreen.BookAccess {
      private final List<String> pages;

      public WrittenBookAccess(ItemStack itemstack) {
         this.pages = readPages(itemstack);
      }

      private static List<String> readPages(ItemStack itemstack) {
         CompoundTag compoundtag = itemstack.getTag();
         return (List<String>)(compoundtag != null && WrittenBookItem.makeSureTagIsValid(compoundtag) ? BookViewScreen.loadPages(compoundtag) : ImmutableList.of(Component.Serializer.toJson(Component.translatable("book.invalid.tag").withStyle(ChatFormatting.DARK_RED))));
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public FormattedText getPageRaw(int i) {
         String s = this.pages.get(i);

         try {
            FormattedText formattedtext = Component.Serializer.fromJson(s);
            if (formattedtext != null) {
               return formattedtext;
            }
         } catch (Exception var4) {
         }

         return FormattedText.of(s);
      }
   }
}
