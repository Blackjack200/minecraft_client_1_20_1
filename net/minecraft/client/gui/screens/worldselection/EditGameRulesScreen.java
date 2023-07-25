package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameRules;

public class EditGameRulesScreen extends Screen {
   private final Consumer<Optional<GameRules>> exitCallback;
   private EditGameRulesScreen.RuleList rules;
   private final Set<EditGameRulesScreen.RuleEntry> invalidEntries = Sets.newHashSet();
   private Button doneButton;
   @Nullable
   private List<FormattedCharSequence> tooltip;
   private final GameRules gameRules;

   public EditGameRulesScreen(GameRules gamerules, Consumer<Optional<GameRules>> consumer) {
      super(Component.translatable("editGamerule.title"));
      this.gameRules = gamerules;
      this.exitCallback = consumer;
   }

   protected void init() {
      this.rules = new EditGameRulesScreen.RuleList(this.gameRules);
      this.addWidget(this.rules);
      GridLayout.RowHelper gridlayout_rowhelper = (new GridLayout()).columnSpacing(10).createRowHelper(2);
      this.doneButton = gridlayout_rowhelper.addChild(Button.builder(CommonComponents.GUI_DONE, (button1) -> this.exitCallback.accept(Optional.of(this.gameRules))).build());
      gridlayout_rowhelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.exitCallback.accept(Optional.empty())).build());
      gridlayout_rowhelper.getGrid().visitWidgets((guieventlistener) -> {
         AbstractWidget var10000 = this.addRenderableWidget(guieventlistener);
      });
      gridlayout_rowhelper.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
      gridlayout_rowhelper.getGrid().arrangeElements();
   }

   public void onClose() {
      this.exitCallback.accept(Optional.empty());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.tooltip = null;
      this.rules.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(guigraphics, i, j, f);
   }

   private void updateDoneButton() {
      this.doneButton.active = this.invalidEntries.isEmpty();
   }

   void markInvalid(EditGameRulesScreen.RuleEntry editgamerulesscreen_ruleentry) {
      this.invalidEntries.add(editgamerulesscreen_ruleentry);
      this.updateDoneButton();
   }

   void clearInvalid(EditGameRulesScreen.RuleEntry editgamerulesscreen_ruleentry) {
      this.invalidEntries.remove(editgamerulesscreen_ruleentry);
      this.updateDoneButton();
   }

   public class BooleanRuleEntry extends EditGameRulesScreen.GameRuleEntry {
      private final CycleButton<Boolean> checkbox;

      public BooleanRuleEntry(Component component, List<FormattedCharSequence> list, String s, GameRules.BooleanValue gamerules_booleanvalue) {
         super(list, component);
         this.checkbox = CycleButton.onOffBuilder(gamerules_booleanvalue.get()).displayOnlyValue().withCustomNarration((cyclebutton1) -> cyclebutton1.createDefaultNarrationMessage().append("\n").append(s)).create(10, 5, 44, 20, component, (cyclebutton, obool) -> gamerules_booleanvalue.set(obool, (MinecraftServer)null));
         this.children.add(this.checkbox);
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         this.renderLabel(guigraphics, j, k);
         this.checkbox.setX(k + l - 45);
         this.checkbox.setY(j);
         this.checkbox.render(guigraphics, j1, k1, f);
      }
   }

   public class CategoryRuleEntry extends EditGameRulesScreen.RuleEntry {
      final Component label;

      public CategoryRuleEntry(Component component) {
         super((List<FormattedCharSequence>)null);
         this.label = component;
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         guigraphics.drawCenteredString(EditGameRulesScreen.this.minecraft.font, this.label, k + l / 2, j + 5, 16777215);
      }

      public List<? extends GuiEventListener> children() {
         return ImmutableList.of();
      }

      public List<? extends NarratableEntry> narratables() {
         return ImmutableList.of(new NarratableEntry() {
            public NarratableEntry.NarrationPriority narrationPriority() {
               return NarratableEntry.NarrationPriority.HOVERED;
            }

            public void updateNarration(NarrationElementOutput narrationelementoutput) {
               narrationelementoutput.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
            }
         });
      }
   }

   @FunctionalInterface
   interface EntryFactory<T extends GameRules.Value<T>> {
      EditGameRulesScreen.RuleEntry create(Component component, List<FormattedCharSequence> list, String s, T gamerules_value);
   }

   public abstract class GameRuleEntry extends EditGameRulesScreen.RuleEntry {
      private final List<FormattedCharSequence> label;
      protected final List<AbstractWidget> children = Lists.newArrayList();

      public GameRuleEntry(@Nullable List<FormattedCharSequence> list, Component component) {
         super(list);
         this.label = EditGameRulesScreen.this.minecraft.font.split(component, 175);
      }

      public List<? extends GuiEventListener> children() {
         return this.children;
      }

      public List<? extends NarratableEntry> narratables() {
         return this.children;
      }

      protected void renderLabel(GuiGraphics guigraphics, int i, int j) {
         if (this.label.size() == 1) {
            guigraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), j, i + 5, 16777215, false);
         } else if (this.label.size() >= 2) {
            guigraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(0), j, i, 16777215, false);
            guigraphics.drawString(EditGameRulesScreen.this.minecraft.font, this.label.get(1), j, i + 10, 16777215, false);
         }

      }
   }

   public class IntegerRuleEntry extends EditGameRulesScreen.GameRuleEntry {
      private final EditBox input;

      public IntegerRuleEntry(Component component, List<FormattedCharSequence> list, String s, GameRules.IntegerValue gamerules_integervalue) {
         super(list, component);
         this.input = new EditBox(EditGameRulesScreen.this.minecraft.font, 10, 5, 42, 20, component.copy().append("\n").append(s).append("\n"));
         this.input.setValue(Integer.toString(gamerules_integervalue.get()));
         this.input.setResponder((s1) -> {
            if (gamerules_integervalue.tryDeserialize(s1)) {
               this.input.setTextColor(14737632);
               EditGameRulesScreen.this.clearInvalid(this);
            } else {
               this.input.setTextColor(16711680);
               EditGameRulesScreen.this.markInvalid(this);
            }

         });
         this.children.add(this.input);
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         this.renderLabel(guigraphics, j, k);
         this.input.setX(k + l - 44);
         this.input.setY(j);
         this.input.render(guigraphics, j1, k1, f);
      }
   }

   public abstract static class RuleEntry extends ContainerObjectSelectionList.Entry<EditGameRulesScreen.RuleEntry> {
      @Nullable
      final List<FormattedCharSequence> tooltip;

      public RuleEntry(@Nullable List<FormattedCharSequence> list) {
         this.tooltip = list;
      }
   }

   public class RuleList extends ContainerObjectSelectionList<EditGameRulesScreen.RuleEntry> {
      public RuleList(final GameRules gamerules) {
         super(EditGameRulesScreen.this.minecraft, EditGameRulesScreen.this.width, EditGameRulesScreen.this.height, 43, EditGameRulesScreen.this.height - 32, 24);
         final Map<GameRules.Category, Map<GameRules.Key<?>, EditGameRulesScreen.RuleEntry>> map = Maps.newHashMap();
         GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            public void visitBoolean(GameRules.Key<GameRules.BooleanValue> gamerules_key, GameRules.Type<GameRules.BooleanValue> gamerules_type) {
               this.addEntry(gamerules_key, (component, list, s, gamerules_booleanvalue) -> EditGameRulesScreen.this.new BooleanRuleEntry(component, list, s, gamerules_booleanvalue));
            }

            public void visitInteger(GameRules.Key<GameRules.IntegerValue> gamerules_key, GameRules.Type<GameRules.IntegerValue> gamerules_type) {
               this.addEntry(gamerules_key, (component, list, s, gamerules_integervalue) -> EditGameRulesScreen.this.new IntegerRuleEntry(component, list, s, gamerules_integervalue));
            }

            private <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> gamerules_key, EditGameRulesScreen.EntryFactory<T> editgamerulesscreen_entryfactory) {
               Component component = Component.translatable(gamerules_key.getDescriptionId());
               Component component1 = Component.literal(gamerules_key.getId()).withStyle(ChatFormatting.YELLOW);
               T gamerules_value = gamerules.getRule(gamerules_key);
               String s = gamerules_value.serialize();
               Component component2 = Component.translatable("editGamerule.default", Component.literal(s)).withStyle(ChatFormatting.GRAY);
               String s1 = gamerules_key.getDescriptionId() + ".description";
               List<FormattedCharSequence> list;
               String s2;
               if (I18n.exists(s1)) {
                  ImmutableList.Builder<FormattedCharSequence> immutablelist_builder = ImmutableList.<FormattedCharSequence>builder().add(component1.getVisualOrderText());
                  Component component3 = Component.translatable(s1);
                  EditGameRulesScreen.this.font.split(component3, 150).forEach(immutablelist_builder::add);
                  list = immutablelist_builder.add(component2.getVisualOrderText()).build();
                  s2 = component3.getString() + "\n" + component2.getString();
               } else {
                  list = ImmutableList.of(component1.getVisualOrderText(), component2.getVisualOrderText());
                  s2 = component2.getString();
               }

               map.computeIfAbsent(gamerules_key.getCategory(), (gamerules_category) -> Maps.newHashMap()).put(gamerules_key, editgamerulesscreen_entryfactory.create(component, list, s2, gamerules_value));
            }
         });
         map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((map_entry) -> {
            this.addEntry(EditGameRulesScreen.this.new CategoryRuleEntry(Component.translatable(map_entry.getKey().getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
            map_entry.getValue().entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRules.Key::getId))).forEach((map_entry1) -> this.addEntry(map_entry1.getValue()));
         });
      }

      public void render(GuiGraphics guigraphics, int i, int j, float f) {
         super.render(guigraphics, i, j, f);
         EditGameRulesScreen.RuleEntry editgamerulesscreen_ruleentry = this.getHovered();
         if (editgamerulesscreen_ruleentry != null && editgamerulesscreen_ruleentry.tooltip != null) {
            EditGameRulesScreen.this.setTooltipForNextRenderPass(editgamerulesscreen_ruleentry.tooltip);
         }

      }
   }
}
