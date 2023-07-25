package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class CommandSuggestions {
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
   private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
   private static final Style LITERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);
   private static final List<Style> ARGUMENT_STYLES = Stream.of(ChatFormatting.AQUA, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.LIGHT_PURPLE, ChatFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
   final Minecraft minecraft;
   private final Screen screen;
   final EditBox input;
   final Font font;
   private final boolean commandsOnly;
   private final boolean onlyShowIfCursorPastError;
   final int lineStartOffset;
   final int suggestionLineLimit;
   final boolean anchorToBottom;
   final int fillColor;
   private final List<FormattedCharSequence> commandUsage = Lists.newArrayList();
   private int commandUsagePosition;
   private int commandUsageWidth;
   @Nullable
   private ParseResults<SharedSuggestionProvider> currentParse;
   @Nullable
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Nullable
   private CommandSuggestions.SuggestionsList suggestions;
   private boolean allowSuggestions;
   boolean keepSuggestions;

   public CommandSuggestions(Minecraft minecraft, Screen screen, EditBox editbox, Font font, boolean flag, boolean flag1, int i, int j, boolean flag2, int k) {
      this.minecraft = minecraft;
      this.screen = screen;
      this.input = editbox;
      this.font = font;
      this.commandsOnly = flag;
      this.onlyShowIfCursorPastError = flag1;
      this.lineStartOffset = i;
      this.suggestionLineLimit = j;
      this.anchorToBottom = flag2;
      this.fillColor = k;
      editbox.setFormatter(this::formatChat);
   }

   public void setAllowSuggestions(boolean flag) {
      this.allowSuggestions = flag;
      if (!flag) {
         this.suggestions = null;
      }

   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.suggestions != null && this.suggestions.keyPressed(i, j, k)) {
         return true;
      } else if (this.screen.getFocused() == this.input && i == 258) {
         this.showSuggestions(true);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double d0) {
      return this.suggestions != null && this.suggestions.mouseScrolled(Mth.clamp(d0, -1.0D, 1.0D));
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      return this.suggestions != null && this.suggestions.mouseClicked((int)d0, (int)d1, i);
   }

   public void showSuggestions(boolean flag) {
      if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
         Suggestions suggestions = this.pendingSuggestions.join();
         if (!suggestions.isEmpty()) {
            int i = 0;

            for(Suggestion suggestion : suggestions.getList()) {
               i = Math.max(i, this.font.width(suggestion.getText()));
            }

            int j = Mth.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            int k = this.anchorToBottom ? this.screen.height - 12 : 72;
            this.suggestions = new CommandSuggestions.SuggestionsList(j, k, i, this.sortSuggestions(suggestions), flag);
         }
      }

   }

   public void hide() {
      this.suggestions = null;
   }

   private List<Suggestion> sortSuggestions(Suggestions suggestions) {
      String s = this.input.getValue().substring(0, this.input.getCursorPosition());
      int i = getLastWordIndex(s);
      String s1 = s.substring(i).toLowerCase(Locale.ROOT);
      List<Suggestion> list = Lists.newArrayList();
      List<Suggestion> list1 = Lists.newArrayList();

      for(Suggestion suggestion : suggestions.getList()) {
         if (!suggestion.getText().startsWith(s1) && !suggestion.getText().startsWith("minecraft:" + s1)) {
            list1.add(suggestion);
         } else {
            list.add(suggestion);
         }
      }

      list.addAll(list1);
      return list;
   }

   public void updateCommandInfo() {
      String s = this.input.getValue();
      if (this.currentParse != null && !this.currentParse.getReader().getString().equals(s)) {
         this.currentParse = null;
      }

      if (!this.keepSuggestions) {
         this.input.setSuggestion((String)null);
         this.suggestions = null;
      }

      this.commandUsage.clear();
      StringReader stringreader = new StringReader(s);
      boolean flag = stringreader.canRead() && stringreader.peek() == '/';
      if (flag) {
         stringreader.skip();
      }

      boolean flag1 = this.commandsOnly || flag;
      int i = this.input.getCursorPosition();
      if (flag1) {
         CommandDispatcher<SharedSuggestionProvider> commanddispatcher = this.minecraft.player.connection.getCommands();
         if (this.currentParse == null) {
            this.currentParse = commanddispatcher.parse(stringreader, this.minecraft.player.connection.getSuggestionsProvider());
         }

         int j = this.onlyShowIfCursorPastError ? stringreader.getCursor() : 1;
         if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = commanddispatcher.getCompletionSuggestions(this.currentParse, i);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.updateUsageInfo();
               }
            });
         }
      } else {
         String s1 = s.substring(0, i);
         int k = getLastWordIndex(s1);
         Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getCustomTabSugggestions();
         this.pendingSuggestions = SharedSuggestionProvider.suggest(collection, new SuggestionsBuilder(s1, k));
      }

   }

   private static int getLastWordIndex(String s) {
      if (Strings.isNullOrEmpty(s)) {
         return 0;
      } else {
         int i = 0;

         for(Matcher matcher = WHITESPACE_PATTERN.matcher(s); matcher.find(); i = matcher.end()) {
         }

         return i;
      }
   }

   private static FormattedCharSequence getExceptionMessage(CommandSyntaxException commandsyntaxexception) {
      Component component = ComponentUtils.fromMessage(commandsyntaxexception.getRawMessage());
      String s = commandsyntaxexception.getContext();
      return s == null ? component.getVisualOrderText() : Component.translatable("command.context.parse_error", component, commandsyntaxexception.getCursor(), s).getVisualOrderText();
   }

   private void updateUsageInfo() {
      boolean flag = false;
      if (this.input.getCursorPosition() == this.input.getValue().length()) {
         if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
            int i = 0;

            for(Map.Entry<CommandNode<SharedSuggestionProvider>, CommandSyntaxException> map_entry : this.currentParse.getExceptions().entrySet()) {
               CommandSyntaxException commandsyntaxexception = map_entry.getValue();
               if (commandsyntaxexception.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                  ++i;
               } else {
                  this.commandUsage.add(getExceptionMessage(commandsyntaxexception));
               }
            }

            if (i > 0) {
               this.commandUsage.add(getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
            }
         } else if (this.currentParse.getReader().canRead()) {
            flag = true;
         }
      }

      this.commandUsagePosition = 0;
      this.commandUsageWidth = this.screen.width;
      if (this.commandUsage.isEmpty() && !this.fillNodeUsage(ChatFormatting.GRAY) && flag) {
         this.commandUsage.add(getExceptionMessage(Commands.getParseException(this.currentParse)));
      }

      this.suggestions = null;
      if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
         this.showSuggestions(false);
      }

   }

   private boolean fillNodeUsage(ChatFormatting chatformatting) {
      CommandContextBuilder<SharedSuggestionProvider> commandcontextbuilder = this.currentParse.getContext();
      SuggestionContext<SharedSuggestionProvider> suggestioncontext = commandcontextbuilder.findSuggestionContext(this.input.getCursorPosition());
      Map<CommandNode<SharedSuggestionProvider>, String> map = this.minecraft.player.connection.getCommands().getSmartUsage(suggestioncontext.parent, this.minecraft.player.connection.getSuggestionsProvider());
      List<FormattedCharSequence> list = Lists.newArrayList();
      int i = 0;
      Style style = Style.EMPTY.withColor(chatformatting);

      for(Map.Entry<CommandNode<SharedSuggestionProvider>, String> map_entry : map.entrySet()) {
         if (!(map_entry.getKey() instanceof LiteralCommandNode)) {
            list.add(FormattedCharSequence.forward(map_entry.getValue(), style));
            i = Math.max(i, this.font.width(map_entry.getValue()));
         }
      }

      if (!list.isEmpty()) {
         this.commandUsage.addAll(list);
         this.commandUsagePosition = Mth.clamp(this.input.getScreenX(suggestioncontext.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
         this.commandUsageWidth = i;
         return true;
      } else {
         return false;
      }
   }

   private FormattedCharSequence formatChat(String s, int l) {
      return this.currentParse != null ? formatText(this.currentParse, s, l) : FormattedCharSequence.forward(s, Style.EMPTY);
   }

   @Nullable
   static String calculateSuggestionSuffix(String s, String s1) {
      return s1.startsWith(s) ? s1.substring(s.length()) : null;
   }

   private static FormattedCharSequence formatText(ParseResults<SharedSuggestionProvider> parseresults, String s, int i) {
      List<FormattedCharSequence> list = Lists.newArrayList();
      int j = 0;
      int k = -1;
      CommandContextBuilder<SharedSuggestionProvider> commandcontextbuilder = parseresults.getContext().getLastChild();

      for(ParsedArgument<SharedSuggestionProvider, ?> parsedargument : commandcontextbuilder.getArguments().values()) {
         ++k;
         if (k >= ARGUMENT_STYLES.size()) {
            k = 0;
         }

         int l = Math.max(parsedargument.getRange().getStart() - i, 0);
         if (l >= s.length()) {
            break;
         }

         int i1 = Math.min(parsedargument.getRange().getEnd() - i, s.length());
         if (i1 > 0) {
            list.add(FormattedCharSequence.forward(s.substring(j, l), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(s.substring(l, i1), ARGUMENT_STYLES.get(k)));
            j = i1;
         }
      }

      if (parseresults.getReader().canRead()) {
         int j1 = Math.max(parseresults.getReader().getCursor() - i, 0);
         if (j1 < s.length()) {
            int k1 = Math.min(j1 + parseresults.getReader().getRemainingLength(), s.length());
            list.add(FormattedCharSequence.forward(s.substring(j, j1), LITERAL_STYLE));
            list.add(FormattedCharSequence.forward(s.substring(j1, k1), UNPARSED_STYLE));
            j = k1;
         }
      }

      list.add(FormattedCharSequence.forward(s.substring(j), LITERAL_STYLE));
      return FormattedCharSequence.composite(list);
   }

   public void render(GuiGraphics guigraphics, int i, int j) {
      if (!this.renderSuggestions(guigraphics, i, j)) {
         this.renderUsage(guigraphics);
      }

   }

   public boolean renderSuggestions(GuiGraphics guigraphics, int i, int j) {
      if (this.suggestions != null) {
         this.suggestions.render(guigraphics, i, j);
         return true;
      } else {
         return false;
      }
   }

   public void renderUsage(GuiGraphics guigraphics) {
      int i = 0;

      for(FormattedCharSequence formattedcharsequence : this.commandUsage) {
         int j = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * i : 72 + 12 * i;
         guigraphics.fill(this.commandUsagePosition - 1, j, this.commandUsagePosition + this.commandUsageWidth + 1, j + 12, this.fillColor);
         guigraphics.drawString(this.font, formattedcharsequence, this.commandUsagePosition, j + 2, -1);
         ++i;
      }

   }

   public Component getNarrationMessage() {
      return (Component)(this.suggestions != null ? CommonComponents.NEW_LINE.copy().append(this.suggestions.getNarrationMessage()) : CommonComponents.EMPTY);
   }

   public class SuggestionsList {
      private final Rect2i rect;
      private final String originalContents;
      private final List<Suggestion> suggestionList;
      private int offset;
      private int current;
      private Vec2 lastMouse = Vec2.ZERO;
      private boolean tabCycles;
      private int lastNarratedEntry;

      SuggestionsList(int i, int j, int k, List<Suggestion> list, boolean flag) {
         int l = i - 1;
         int i1 = CommandSuggestions.this.anchorToBottom ? j - 3 - Math.min(list.size(), CommandSuggestions.this.suggestionLineLimit) * 12 : j;
         this.rect = new Rect2i(l, i1, k + 1, Math.min(list.size(), CommandSuggestions.this.suggestionLineLimit) * 12);
         this.originalContents = CommandSuggestions.this.input.getValue();
         this.lastNarratedEntry = flag ? -1 : 0;
         this.suggestionList = list;
         this.select(0);
      }

      public void render(GuiGraphics guigraphics, int i, int j) {
         int k = Math.min(this.suggestionList.size(), CommandSuggestions.this.suggestionLineLimit);
         int l = -5592406;
         boolean flag = this.offset > 0;
         boolean flag1 = this.suggestionList.size() > this.offset + k;
         boolean flag2 = flag || flag1;
         boolean flag3 = this.lastMouse.x != (float)i || this.lastMouse.y != (float)j;
         if (flag3) {
            this.lastMouse = new Vec2((float)i, (float)j);
         }

         if (flag2) {
            guigraphics.fill(this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestions.this.fillColor);
            guigraphics.fill(this.rect.getX(), this.rect.getY() + this.rect.getHeight(), this.rect.getX() + this.rect.getWidth(), this.rect.getY() + this.rect.getHeight() + 1, CommandSuggestions.this.fillColor);
            if (flag) {
               for(int i1 = 0; i1 < this.rect.getWidth(); ++i1) {
                  if (i1 % 2 == 0) {
                     guigraphics.fill(this.rect.getX() + i1, this.rect.getY() - 1, this.rect.getX() + i1 + 1, this.rect.getY(), -1);
                  }
               }
            }

            if (flag1) {
               for(int j1 = 0; j1 < this.rect.getWidth(); ++j1) {
                  if (j1 % 2 == 0) {
                     guigraphics.fill(this.rect.getX() + j1, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + j1 + 1, this.rect.getY() + this.rect.getHeight() + 1, -1);
                  }
               }
            }
         }

         boolean flag4 = false;

         for(int k1 = 0; k1 < k; ++k1) {
            Suggestion suggestion = this.suggestionList.get(k1 + this.offset);
            guigraphics.fill(this.rect.getX(), this.rect.getY() + 12 * k1, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * k1 + 12, CommandSuggestions.this.fillColor);
            if (i > this.rect.getX() && i < this.rect.getX() + this.rect.getWidth() && j > this.rect.getY() + 12 * k1 && j < this.rect.getY() + 12 * k1 + 12) {
               if (flag3) {
                  this.select(k1 + this.offset);
               }

               flag4 = true;
            }

            guigraphics.drawString(CommandSuggestions.this.font, suggestion.getText(), this.rect.getX() + 1, this.rect.getY() + 2 + 12 * k1, k1 + this.offset == this.current ? -256 : -5592406);
         }

         if (flag4) {
            Message message = this.suggestionList.get(this.current).getTooltip();
            if (message != null) {
               guigraphics.renderTooltip(CommandSuggestions.this.font, ComponentUtils.fromMessage(message), i, j);
            }
         }

      }

      public boolean mouseClicked(int i, int j, int k) {
         if (!this.rect.contains(i, j)) {
            return false;
         } else {
            int l = (j - this.rect.getY()) / 12 + this.offset;
            if (l >= 0 && l < this.suggestionList.size()) {
               this.select(l);
               this.useSuggestion();
            }

            return true;
         }
      }

      public boolean mouseScrolled(double d0) {
         int i = (int)(CommandSuggestions.this.minecraft.mouseHandler.xpos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledWidth() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenWidth());
         int j = (int)(CommandSuggestions.this.minecraft.mouseHandler.ypos() * (double)CommandSuggestions.this.minecraft.getWindow().getGuiScaledHeight() / (double)CommandSuggestions.this.minecraft.getWindow().getScreenHeight());
         if (this.rect.contains(i, j)) {
            this.offset = Mth.clamp((int)((double)this.offset - d0), 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
            return true;
         } else {
            return false;
         }
      }

      public boolean keyPressed(int i, int j, int k) {
         if (i == 265) {
            this.cycle(-1);
            this.tabCycles = false;
            return true;
         } else if (i == 264) {
            this.cycle(1);
            this.tabCycles = false;
            return true;
         } else if (i == 258) {
            if (this.tabCycles) {
               this.cycle(Screen.hasShiftDown() ? -1 : 1);
            }

            this.useSuggestion();
            return true;
         } else if (i == 256) {
            CommandSuggestions.this.hide();
            return true;
         } else {
            return false;
         }
      }

      public void cycle(int i) {
         this.select(this.current + i);
         int j = this.offset;
         int k = this.offset + CommandSuggestions.this.suggestionLineLimit - 1;
         if (this.current < j) {
            this.offset = Mth.clamp(this.current, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
         } else if (this.current > k) {
            this.offset = Mth.clamp(this.current + CommandSuggestions.this.lineStartOffset - CommandSuggestions.this.suggestionLineLimit, 0, Math.max(this.suggestionList.size() - CommandSuggestions.this.suggestionLineLimit, 0));
         }

      }

      public void select(int i) {
         this.current = i;
         if (this.current < 0) {
            this.current += this.suggestionList.size();
         }

         if (this.current >= this.suggestionList.size()) {
            this.current -= this.suggestionList.size();
         }

         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestions.this.input.setSuggestion(CommandSuggestions.calculateSuggestionSuffix(CommandSuggestions.this.input.getValue(), suggestion.apply(this.originalContents)));
         if (this.lastNarratedEntry != this.current) {
            CommandSuggestions.this.minecraft.getNarrator().sayNow(this.getNarrationMessage());
         }

      }

      public void useSuggestion() {
         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestions.this.keepSuggestions = true;
         CommandSuggestions.this.input.setValue(suggestion.apply(this.originalContents));
         int i = suggestion.getRange().getStart() + suggestion.getText().length();
         CommandSuggestions.this.input.setCursorPosition(i);
         CommandSuggestions.this.input.setHighlightPos(i);
         this.select(this.current);
         CommandSuggestions.this.keepSuggestions = false;
         this.tabCycles = true;
      }

      Component getNarrationMessage() {
         this.lastNarratedEntry = this.current;
         Suggestion suggestion = this.suggestionList.get(this.current);
         Message message = suggestion.getTooltip();
         return message != null ? Component.translatable("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), suggestion.getText(), message) : Component.translatable("narration.suggestion", this.current + 1, this.suggestionList.size(), suggestion.getText());
      }
   }
}
