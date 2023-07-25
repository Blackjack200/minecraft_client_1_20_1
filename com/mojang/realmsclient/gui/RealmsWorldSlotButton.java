package com.mojang.realmsclient.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.util.RealmsTextureManager;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RealmsWorldSlotButton extends Button {
   public static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
   public static final ResourceLocation EMPTY_SLOT_LOCATION = new ResourceLocation("realms", "textures/gui/realms/empty_frame.png");
   public static final ResourceLocation CHECK_MARK_LOCATION = new ResourceLocation("minecraft", "textures/gui/checkmark.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_2.png");
   public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = new ResourceLocation("minecraft", "textures/gui/title/background/panorama_3.png");
   private static final Component SLOT_ACTIVE_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.active");
   private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
   private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
   private static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
   private final Supplier<RealmsServer> serverDataProvider;
   private final Consumer<Component> toolTipSetter;
   private final int slotIndex;
   @Nullable
   private RealmsWorldSlotButton.State state;

   public RealmsWorldSlotButton(int i, int j, int k, int l, Supplier<RealmsServer> supplier, Consumer<Component> consumer, int i1, Button.OnPress button_onpress) {
      super(i, j, k, l, CommonComponents.EMPTY, button_onpress, DEFAULT_NARRATION);
      this.serverDataProvider = supplier;
      this.slotIndex = i1;
      this.toolTipSetter = consumer;
   }

   @Nullable
   public RealmsWorldSlotButton.State getState() {
      return this.state;
   }

   public void tick() {
      RealmsServer realmsserver = this.serverDataProvider.get();
      if (realmsserver != null) {
         RealmsWorldOptions realmsworldoptions = realmsserver.slots.get(this.slotIndex);
         boolean flag = this.slotIndex == 4;
         boolean flag1;
         String s;
         long i;
         String s1;
         boolean flag2;
         if (flag) {
            flag1 = realmsserver.worldType == RealmsServer.WorldType.MINIGAME;
            s = MINIGAME.getString();
            i = (long)realmsserver.minigameId;
            s1 = realmsserver.minigameImage;
            flag2 = realmsserver.minigameId == -1;
         } else {
            flag1 = realmsserver.activeSlot == this.slotIndex && realmsserver.worldType != RealmsServer.WorldType.MINIGAME;
            s = realmsworldoptions.getSlotName(this.slotIndex);
            i = realmsworldoptions.templateId;
            s1 = realmsworldoptions.templateImage;
            flag2 = realmsworldoptions.empty;
         }

         RealmsWorldSlotButton.Action realmsworldslotbutton_action = getAction(realmsserver, flag1, flag);
         Pair<Component, Component> pair = this.getTooltipAndNarration(realmsserver, s, flag2, flag, realmsworldslotbutton_action);
         this.state = new RealmsWorldSlotButton.State(flag1, s, i, s1, flag2, flag, realmsworldslotbutton_action, pair.getFirst());
         this.setMessage(pair.getSecond());
      }
   }

   private static RealmsWorldSlotButton.Action getAction(RealmsServer realmsserver, boolean flag, boolean flag1) {
      if (flag) {
         if (!realmsserver.expired && realmsserver.state != RealmsServer.State.UNINITIALIZED) {
            return RealmsWorldSlotButton.Action.JOIN;
         }
      } else {
         if (!flag1) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }

         if (!realmsserver.expired) {
            return RealmsWorldSlotButton.Action.SWITCH_SLOT;
         }
      }

      return RealmsWorldSlotButton.Action.NOTHING;
   }

   private Pair<Component, Component> getTooltipAndNarration(RealmsServer realmsserver, String s, boolean flag, boolean flag1, RealmsWorldSlotButton.Action realmsworldslotbutton_action) {
      if (realmsworldslotbutton_action == RealmsWorldSlotButton.Action.NOTHING) {
         return Pair.of((Component)null, Component.literal(s));
      } else {
         Component component;
         if (flag1) {
            if (flag) {
               component = CommonComponents.EMPTY;
            } else {
               component = CommonComponents.space().append(s).append(CommonComponents.SPACE).append(realmsserver.minigameName);
            }
         } else {
            component = CommonComponents.space().append(s);
         }

         Component component3;
         if (realmsworldslotbutton_action == RealmsWorldSlotButton.Action.JOIN) {
            component3 = SLOT_ACTIVE_TOOLTIP;
         } else {
            component3 = flag1 ? SWITCH_TO_MINIGAME_SLOT_TOOLTIP : SWITCH_TO_WORLD_SLOT_TOOLTIP;
         }

         Component component5 = component3.copy().append(component);
         return Pair.of(component3, component5);
      }
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.state != null) {
         this.drawSlotFrame(guigraphics, this.getX(), this.getY(), i, j, this.state.isCurrentlyActiveSlot, this.state.slotName, this.slotIndex, this.state.imageId, this.state.image, this.state.empty, this.state.minigame, this.state.action, this.state.actionPrompt);
      }
   }

   private void drawSlotFrame(GuiGraphics guigraphics, int i, int j, int k, int l, boolean flag, String s, int i1, long j1, @Nullable String s1, boolean flag1, boolean flag2, RealmsWorldSlotButton.Action realmsworldslotbutton_action, @Nullable Component component) {
      boolean flag3 = this.isHoveredOrFocused();
      if (this.isMouseOver((double)k, (double)l) && component != null) {
         this.toolTipSetter.accept(component);
      }

      Minecraft minecraft = Minecraft.getInstance();
      ResourceLocation resourcelocation;
      if (flag2) {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(j1), s1);
      } else if (flag1) {
         resourcelocation = EMPTY_SLOT_LOCATION;
      } else if (s1 != null && j1 != -1L) {
         resourcelocation = RealmsTextureManager.worldTemplate(String.valueOf(j1), s1);
      } else if (i1 == 1) {
         resourcelocation = DEFAULT_WORLD_SLOT_1;
      } else if (i1 == 2) {
         resourcelocation = DEFAULT_WORLD_SLOT_2;
      } else if (i1 == 3) {
         resourcelocation = DEFAULT_WORLD_SLOT_3;
      } else {
         resourcelocation = EMPTY_SLOT_LOCATION;
      }

      if (flag) {
         guigraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      guigraphics.blit(resourcelocation, i + 3, j + 3, 0.0F, 0.0F, 74, 74, 74, 74);
      boolean flag4 = flag3 && realmsworldslotbutton_action != RealmsWorldSlotButton.Action.NOTHING;
      if (flag4) {
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      } else if (flag) {
         guigraphics.setColor(0.8F, 0.8F, 0.8F, 1.0F);
      } else {
         guigraphics.setColor(0.56F, 0.56F, 0.56F, 1.0F);
      }

      guigraphics.blit(SLOT_FRAME_LOCATION, i, j, 0.0F, 0.0F, 80, 80, 80, 80);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (flag) {
         this.renderCheckMark(guigraphics, i, j);
      }

      guigraphics.drawCenteredString(minecraft.font, s, i + 40, j + 66, 16777215);
   }

   private void renderCheckMark(GuiGraphics guigraphics, int i, int j) {
      RenderSystem.enableBlend();
      guigraphics.blit(CHECK_MARK_LOCATION, i + 67, j + 4, 0.0F, 0.0F, 9, 8, 9, 8);
      RenderSystem.disableBlend();
   }

   public static enum Action {
      NOTHING,
      SWITCH_SLOT,
      JOIN;
   }

   public static class State {
      final boolean isCurrentlyActiveSlot;
      final String slotName;
      final long imageId;
      @Nullable
      final String image;
      public final boolean empty;
      public final boolean minigame;
      public final RealmsWorldSlotButton.Action action;
      @Nullable
      final Component actionPrompt;

      State(boolean flag, String s, long i, @Nullable String s1, boolean flag1, boolean flag2, RealmsWorldSlotButton.Action realmsworldslotbutton_action, @Nullable Component component) {
         this.isCurrentlyActiveSlot = flag;
         this.slotName = s;
         this.imageId = i;
         this.image = s1;
         this.empty = flag1;
         this.minigame = flag2;
         this.action = realmsworldslotbutton_action;
         this.actionPrompt = component;
      }
   }
}
