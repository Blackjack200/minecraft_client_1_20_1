package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsSelectWorldTemplateScreen extends RealmsScreen {
   static final Logger LOGGER = LogUtils.getLogger();
   static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
   static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
   static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
   static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
   private final Consumer<WorldTemplate> callback;
   RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
   int selectedTemplate = -1;
   private Button selectButton;
   private Button trailerButton;
   private Button publisherButton;
   @Nullable
   Component toolTip;
   @Nullable
   String currentLink;
   private final RealmsServer.WorldType worldType;
   int clicks;
   @Nullable
   private Component[] warning;
   private String warningURL;
   boolean displayWarning;
   private boolean hoverWarning;
   @Nullable
   List<TextRenderingUtils.Line> noTemplatesMessage;

   public RealmsSelectWorldTemplateScreen(Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType realmsserver_worldtype) {
      this(component, consumer, realmsserver_worldtype, (WorldTemplatePaginatedList)null);
   }

   public RealmsSelectWorldTemplateScreen(Component component, Consumer<WorldTemplate> consumer, RealmsServer.WorldType realmsserver_worldtype, @Nullable WorldTemplatePaginatedList worldtemplatepaginatedlist) {
      super(component);
      this.callback = consumer;
      this.worldType = realmsserver_worldtype;
      if (worldtemplatepaginatedlist == null) {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
         this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
      } else {
         this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(worldtemplatepaginatedlist.templates));
         this.fetchTemplatesAsync(worldtemplatepaginatedlist);
      }

   }

   public void setWarning(Component... acomponent) {
      this.warning = acomponent;
      this.displayWarning = true;
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.hoverWarning && this.warningURL != null) {
         Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
         return true;
      } else {
         return super.mouseClicked(d0, d1, i);
      }
   }

   public void init() {
      this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(this.worldTemplateObjectSelectionList.getTemplates());
      this.trailerButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.trailer"), (button4) -> this.onTrailer()).bounds(this.width / 2 - 206, this.height - 32, 100, 20).build());
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.select"), (button3) -> this.selectTemplate()).bounds(this.width / 2 - 100, this.height - 32, 100, 20).build());
      Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
      Button button = Button.builder(component, (button2) -> this.onClose()).bounds(this.width / 2 + 6, this.height - 32, 100, 20).build();
      this.addRenderableWidget(button);
      this.publisherButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.template.button.publisher"), (button1) -> this.onPublish()).bounds(this.width / 2 + 112, this.height - 32, 100, 20).build());
      this.selectButton.active = false;
      this.trailerButton.visible = false;
      this.publisherButton.visible = false;
      this.addWidget(this.worldTemplateObjectSelectionList);
      this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
   }

   public Component getNarrationMessage() {
      List<Component> list = Lists.newArrayListWithCapacity(2);
      if (this.title != null) {
         list.add(this.title);
      }

      if (this.warning != null) {
         list.addAll(Arrays.asList(this.warning));
      }

      return CommonComponents.joinLines(list);
   }

   void updateButtonStates() {
      this.publisherButton.visible = this.shouldPublisherBeVisible();
      this.trailerButton.visible = this.shouldTrailerBeVisible();
      this.selectButton.active = this.shouldSelectButtonBeActive();
   }

   private boolean shouldSelectButtonBeActive() {
      return this.selectedTemplate != -1;
   }

   private boolean shouldPublisherBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
   }

   private WorldTemplate getSelectedTemplate() {
      return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
   }

   private boolean shouldTrailerBeVisible() {
      return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
   }

   public void tick() {
      super.tick();
      --this.clicks;
      if (this.clicks < 0) {
         this.clicks = 0;
      }

   }

   public void onClose() {
      this.callback.accept((WorldTemplate)null);
   }

   void selectTemplate() {
      if (this.hasValidTemplate()) {
         this.callback.accept(this.getSelectedTemplate());
      }

   }

   private boolean hasValidTemplate() {
      return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
   }

   private void onTrailer() {
      if (this.hasValidTemplate()) {
         WorldTemplate worldtemplate = this.getSelectedTemplate();
         if (!"".equals(worldtemplate.trailer)) {
            Util.getPlatform().openUri(worldtemplate.trailer);
         }
      }

   }

   private void onPublish() {
      if (this.hasValidTemplate()) {
         WorldTemplate worldtemplate = this.getSelectedTemplate();
         if (!"".equals(worldtemplate.link)) {
            Util.getPlatform().openUri(worldtemplate.link);
         }
      }

   }

   private void fetchTemplatesAsync(final WorldTemplatePaginatedList worldtemplatepaginatedlist) {
      (new Thread("realms-template-fetcher") {
         public void run() {
            WorldTemplatePaginatedList worldtemplatepaginatedlist = worldtemplatepaginatedlist;

            Either<WorldTemplatePaginatedList, String> either;
            for(RealmsClient realmsclient = RealmsClient.create(); worldtemplatepaginatedlist != null; worldtemplatepaginatedlist = RealmsSelectWorldTemplateScreen.this.minecraft.submit(() -> {
               if (either.right().isPresent()) {
                  RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());
                  if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                     RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(I18n.get("mco.template.select.failure"));
                  }

                  return null;
               } else {
                  WorldTemplatePaginatedList worldtemplatepaginatedlist1 = either.left().get();

                  for(WorldTemplate worldtemplate : worldtemplatepaginatedlist1.templates) {
                     RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(worldtemplate);
                  }

                  if (worldtemplatepaginatedlist1.templates.isEmpty()) {
                     if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty()) {
                        String s = I18n.get("mco.template.select.none", "%link");
                        TextRenderingUtils.LineSegment textrenderingutils_linesegment = TextRenderingUtils.LineSegment.link(I18n.get("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator");
                        RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.decompose(s, textrenderingutils_linesegment);
                     }

                     return null;
                  } else {
                     return worldtemplatepaginatedlist1;
                  }
               }
            }).join()) {
               either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldtemplatepaginatedlist, realmsclient);
            }

         }
      }).start();
   }

   Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList worldtemplatepaginatedlist, RealmsClient realmsclient) {
      try {
         return Either.left(realmsclient.fetchWorldTemplates(worldtemplatepaginatedlist.page + 1, worldtemplatepaginatedlist.size, this.worldType));
      } catch (RealmsServiceException var4) {
         return Either.right(var4.getMessage());
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.toolTip = null;
      this.currentLink = null;
      this.hoverWarning = false;
      this.renderBackground(guigraphics);
      this.worldTemplateObjectSelectionList.render(guigraphics, i, j, f);
      if (this.noTemplatesMessage != null) {
         this.renderMultilineMessage(guigraphics, i, j, this.noTemplatesMessage);
      }

      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 13, 16777215);
      if (this.displayWarning) {
         Component[] acomponent = this.warning;

         for(int k = 0; k < acomponent.length; ++k) {
            int l = this.font.width(acomponent[k]);
            int i1 = this.width / 2 - l / 2;
            int j1 = row(-1 + k);
            if (i >= i1 && i <= i1 + l && j >= j1 && j <= j1 + 9) {
               this.hoverWarning = true;
            }
         }

         for(int k1 = 0; k1 < acomponent.length; ++k1) {
            Component component = acomponent[k1];
            int l1 = 10526880;
            if (this.warningURL != null) {
               if (this.hoverWarning) {
                  l1 = 7107012;
                  component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
               } else {
                  l1 = 3368635;
               }
            }

            guigraphics.drawCenteredString(this.font, component, this.width / 2, row(-1 + k1), l1);
         }
      }

      super.render(guigraphics, i, j, f);
      this.renderMousehoverTooltip(guigraphics, this.toolTip, i, j);
   }

   private void renderMultilineMessage(GuiGraphics guigraphics, int i, int j, List<TextRenderingUtils.Line> list) {
      for(int k = 0; k < list.size(); ++k) {
         TextRenderingUtils.Line textrenderingutils_line = list.get(k);
         int l = row(4 + k);
         int i1 = textrenderingutils_line.segments.stream().mapToInt((textrenderingutils_linesegment1) -> this.font.width(textrenderingutils_linesegment1.renderedText())).sum();
         int j1 = this.width / 2 - i1 / 2;

         for(TextRenderingUtils.LineSegment textrenderingutils_linesegment : textrenderingutils_line.segments) {
            int k1 = textrenderingutils_linesegment.isLink() ? 3368635 : 16777215;
            int l1 = guigraphics.drawString(this.font, textrenderingutils_linesegment.renderedText(), j1, l, k1);
            if (textrenderingutils_linesegment.isLink() && i > j1 && i < l1 && j > l - 3 && j < l + 8) {
               this.toolTip = Component.literal(textrenderingutils_linesegment.getLinkUrl());
               this.currentLink = textrenderingutils_linesegment.getLinkUrl();
            }

            j1 = l1;
         }
      }

   }

   protected void renderMousehoverTooltip(GuiGraphics guigraphics, @Nullable Component component, int i, int j) {
      if (component != null) {
         int k = i + 12;
         int l = j - 12;
         int i1 = this.font.width(component);
         guigraphics.fillGradient(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
         guigraphics.drawString(this.font, component, k, l, 16777215);
      }
   }

   class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry> {
      final WorldTemplate template;

      public Entry(WorldTemplate worldtemplate) {
         this.template = worldtemplate;
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         this.renderWorldTemplateItem(guigraphics, this.template, k, j, j1, k1);
      }

      private void renderWorldTemplateItem(GuiGraphics guigraphics, WorldTemplate worldtemplate, int i, int j, int k, int l) {
         int i1 = i + 45 + 20;
         guigraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, worldtemplate.name, i1, j + 2, 16777215, false);
         guigraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, worldtemplate.author, i1, j + 15, 7105644, false);
         guigraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, worldtemplate.version, i1 + 227 - RealmsSelectWorldTemplateScreen.this.font.width(worldtemplate.version), j + 1, 7105644, false);
         if (!"".equals(worldtemplate.link) || !"".equals(worldtemplate.trailer) || !"".equals(worldtemplate.recommendedPlayers)) {
            this.drawIcons(guigraphics, i1 - 1, j + 25, k, l, worldtemplate.link, worldtemplate.trailer, worldtemplate.recommendedPlayers);
         }

         this.drawImage(guigraphics, i, j + 1, k, l, worldtemplate);
      }

      private void drawImage(GuiGraphics guigraphics, int i, int j, int k, int l, WorldTemplate worldtemplate) {
         guigraphics.blit(RealmsTextureManager.worldTemplate(worldtemplate.id, worldtemplate.image), i + 1, j + 1, 0.0F, 0.0F, 38, 38, 38, 38);
         guigraphics.blit(RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION, i, j, 0.0F, 0.0F, 40, 40, 40, 40);
      }

      private void drawIcons(GuiGraphics guigraphics, int i, int j, int k, int l, String s, String s1, String s2) {
         if (!"".equals(s2)) {
            guigraphics.drawString(RealmsSelectWorldTemplateScreen.this.font, s2, i, j + 4, 5000268, false);
         }

         int i1 = "".equals(s2) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(s2) + 2;
         boolean flag = false;
         boolean flag1 = false;
         boolean flag2 = "".equals(s);
         if (k >= i + i1 && k <= i + i1 + 32 && l >= j && l <= j + 15 && l < RealmsSelectWorldTemplateScreen.this.height - 15 && l > 32) {
            if (k <= i + 15 + i1 && k > i1) {
               if (flag2) {
                  flag1 = true;
               } else {
                  flag = true;
               }
            } else if (!flag2) {
               flag1 = true;
            }
         }

         if (!flag2) {
            float f = flag ? 15.0F : 0.0F;
            guigraphics.blit(RealmsSelectWorldTemplateScreen.LINK_ICON, i + i1, j, f, 0.0F, 15, 15, 30, 15);
         }

         if (!"".equals(s1)) {
            int j1 = i + i1 + (flag2 ? 0 : 17);
            float f1 = flag1 ? 15.0F : 0.0F;
            guigraphics.blit(RealmsSelectWorldTemplateScreen.TRAILER_ICON, j1, j, f1, 0.0F, 15, 15, 30, 15);
         }

         if (flag) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.PUBLISHER_LINK_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = s;
         } else if (flag1 && !"".equals(s1)) {
            RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.TRAILER_LINK_TOOLTIP;
            RealmsSelectWorldTemplateScreen.this.currentLink = s1;
         }

      }

      public Component getNarration() {
         Component component = CommonComponents.joinLines(Component.literal(this.template.name), Component.translatable("mco.template.select.narrate.authors", this.template.author), Component.literal(this.template.recommendedPlayers), Component.translatable("mco.template.select.narrate.version", this.template.version));
         return Component.translatable("narrator.select", component);
      }
   }

   class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry> {
      public WorldTemplateObjectSelectionList() {
         this(Collections.emptyList());
      }

      public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> iterable) {
         super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
         iterable.forEach(this::addEntry);
      }

      public void addEntry(WorldTemplate worldtemplate) {
         this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(worldtemplate));
      }

      public boolean mouseClicked(double d0, double d1, int i) {
         if (i == 0 && d1 >= (double)this.y0 && d1 <= (double)this.y1) {
            int j = this.width / 2 - 150;
            if (RealmsSelectWorldTemplateScreen.this.currentLink != null) {
               Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
            }

            int k = (int)Math.floor(d1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
            int l = k / this.itemHeight;
            if (d0 >= (double)j && d0 < (double)this.getScrollbarPosition() && l >= 0 && k >= 0 && l < this.getItemCount()) {
               this.selectItem(l);
               this.itemClicked(k, l, d0, d1, this.width, i);
               if (l >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount()) {
                  return super.mouseClicked(d0, d1, i);
               }

               RealmsSelectWorldTemplateScreen.this.clicks += 7;
               if (RealmsSelectWorldTemplateScreen.this.clicks >= 10) {
                  RealmsSelectWorldTemplateScreen.this.selectTemplate();
               }

               return true;
            }
         }

         return super.mouseClicked(d0, d1, i);
      }

      public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry realmsselectworldtemplatescreen_entry) {
         super.setSelected(realmsselectworldtemplatescreen_entry);
         RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(realmsselectworldtemplatescreen_entry);
         RealmsSelectWorldTemplateScreen.this.updateButtonStates();
      }

      public int getMaxPosition() {
         return this.getItemCount() * 46;
      }

      public int getRowWidth() {
         return 300;
      }

      public void renderBackground(GuiGraphics guigraphics) {
         RealmsSelectWorldTemplateScreen.this.renderBackground(guigraphics);
      }

      public boolean isEmpty() {
         return this.getItemCount() == 0;
      }

      public WorldTemplate get(int i) {
         return (this.children().get(i)).template;
      }

      public List<WorldTemplate> getTemplates() {
         return this.children().stream().map((realmsselectworldtemplatescreen_entry) -> realmsselectworldtemplatescreen_entry.template).collect(Collectors.toList());
      }
   }
}
