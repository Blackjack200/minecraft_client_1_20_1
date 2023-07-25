package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
   static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
   private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
   private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
   private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.newArrayList();
   @Nullable
   MobEffect primary;
   @Nullable
   MobEffect secondary;

   public BeaconScreen(final BeaconMenu beaconmenu, Inventory inventory, Component component) {
      super(beaconmenu, inventory, component);
      this.imageWidth = 230;
      this.imageHeight = 219;
      beaconmenu.addSlotListener(new ContainerListener() {
         public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
         }

         public void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j) {
            BeaconScreen.this.primary = beaconmenu.getPrimaryEffect();
            BeaconScreen.this.secondary = beaconmenu.getSecondaryEffect();
         }
      });
   }

   private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T abstractwidget) {
      this.addRenderableWidget(abstractwidget);
      this.beaconButtons.add(abstractwidget);
   }

   protected void init() {
      super.init();
      this.beaconButtons.clear();
      this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
      this.addBeaconButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));

      for(int i = 0; i <= 2; ++i) {
         int j = BeaconBlockEntity.BEACON_EFFECTS[i].length;
         int k = j * 22 + (j - 1) * 2;

         for(int l = 0; l < j; ++l) {
            MobEffect mobeffect = BeaconBlockEntity.BEACON_EFFECTS[i][l];
            BeaconScreen.BeaconPowerButton beaconscreen_beaconpowerbutton = new BeaconScreen.BeaconPowerButton(this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, mobeffect, true, i);
            beaconscreen_beaconpowerbutton.active = false;
            this.addBeaconButton(beaconscreen_beaconpowerbutton);
         }
      }

      int i1 = 3;
      int j1 = BeaconBlockEntity.BEACON_EFFECTS[3].length + 1;
      int k1 = j1 * 22 + (j1 - 1) * 2;

      for(int l1 = 0; l1 < j1 - 1; ++l1) {
         MobEffect mobeffect1 = BeaconBlockEntity.BEACON_EFFECTS[3][l1];
         BeaconScreen.BeaconPowerButton beaconscreen_beaconpowerbutton1 = new BeaconScreen.BeaconPowerButton(this.leftPos + 167 + l1 * 24 - k1 / 2, this.topPos + 47, mobeffect1, false, 3);
         beaconscreen_beaconpowerbutton1.active = false;
         this.addBeaconButton(beaconscreen_beaconpowerbutton1);
      }

      BeaconScreen.BeaconPowerButton beaconscreen_beaconpowerbutton2 = new BeaconScreen.BeaconUpgradePowerButton(this.leftPos + 167 + (j1 - 1) * 24 - k1 / 2, this.topPos + 47, BeaconBlockEntity.BEACON_EFFECTS[0][0]);
      beaconscreen_beaconpowerbutton2.visible = false;
      this.addBeaconButton(beaconscreen_beaconpowerbutton2);
   }

   public void containerTick() {
      super.containerTick();
      this.updateButtons();
   }

   void updateButtons() {
      int i = this.menu.getLevels();
      this.beaconButtons.forEach((beaconscreen_beaconbutton) -> beaconscreen_beaconbutton.updateStatus(i));
   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      guigraphics.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
      guigraphics.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(BEACON_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
      guigraphics.renderItem(new ItemStack(Items.NETHERITE_INGOT), k + 20, l + 109);
      guigraphics.renderItem(new ItemStack(Items.EMERALD), k + 41, l + 109);
      guigraphics.renderItem(new ItemStack(Items.DIAMOND), k + 41 + 22, l + 109);
      guigraphics.renderItem(new ItemStack(Items.GOLD_INGOT), k + 42 + 44, l + 109);
      guigraphics.renderItem(new ItemStack(Items.IRON_INGOT), k + 42 + 66, l + 109);
      guigraphics.pose().popPose();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   interface BeaconButton {
      void updateStatus(int i);
   }

   class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
      public BeaconCancelButton(int i, int j) {
         super(i, j, 112, 220, CommonComponents.GUI_CANCEL);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.player.closeContainer();
      }

      public void updateStatus(int i) {
      }
   }

   class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
      public BeaconConfirmButton(int i, int j) {
         super(i, j, 90, 220, CommonComponents.GUI_DONE);
      }

      public void onPress() {
         BeaconScreen.this.minecraft.getConnection().send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
         BeaconScreen.this.minecraft.player.closeContainer();
      }

      public void updateStatus(int i) {
         this.active = BeaconScreen.this.menu.hasPayment() && BeaconScreen.this.primary != null;
      }
   }

   class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
      private final boolean isPrimary;
      protected final int tier;
      private MobEffect effect;
      private TextureAtlasSprite sprite;

      public BeaconPowerButton(int i, int j, MobEffect mobeffect, boolean flag, int k) {
         super(i, j);
         this.isPrimary = flag;
         this.tier = k;
         this.setEffect(mobeffect);
      }

      protected void setEffect(MobEffect mobeffect) {
         this.effect = mobeffect;
         this.sprite = Minecraft.getInstance().getMobEffectTextures().get(mobeffect);
         this.setTooltip(Tooltip.create(this.createEffectDescription(mobeffect), (Component)null));
      }

      protected MutableComponent createEffectDescription(MobEffect mobeffect) {
         return Component.translatable(mobeffect.getDescriptionId());
      }

      public void onPress() {
         if (!this.isSelected()) {
            if (this.isPrimary) {
               BeaconScreen.this.primary = this.effect;
            } else {
               BeaconScreen.this.secondary = this.effect;
            }

            BeaconScreen.this.updateButtons();
         }
      }

      protected void renderIcon(GuiGraphics guigraphics) {
         guigraphics.blit(this.getX() + 2, this.getY() + 2, 0, 18, 18, this.sprite);
      }

      public void updateStatus(int i) {
         this.active = this.tier < i;
         this.setSelected(this.effect == (this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
      }

      protected MutableComponent createNarrationMessage() {
         return this.createEffectDescription(this.effect);
      }
   }

   abstract static class BeaconScreenButton extends AbstractButton implements BeaconScreen.BeaconButton {
      private boolean selected;

      protected BeaconScreenButton(int i, int j) {
         super(i, j, 22, 22, CommonComponents.EMPTY);
      }

      protected BeaconScreenButton(int i, int j, Component component) {
         super(i, j, 22, 22, component);
      }

      public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
         int k = 219;
         int l = 0;
         if (!this.active) {
            l += this.width * 2;
         } else if (this.selected) {
            l += this.width * 1;
         } else if (this.isHoveredOrFocused()) {
            l += this.width * 3;
         }

         guigraphics.blit(BeaconScreen.BEACON_LOCATION, this.getX(), this.getY(), l, 219, this.width, this.height);
         this.renderIcon(guigraphics);
      }

      protected abstract void renderIcon(GuiGraphics guigraphics);

      public boolean isSelected() {
         return this.selected;
      }

      public void setSelected(boolean flag) {
         this.selected = flag;
      }

      public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
         this.defaultButtonNarrationText(narrationelementoutput);
      }
   }

   abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
      private final int iconX;
      private final int iconY;

      protected BeaconSpriteScreenButton(int i, int j, int k, int l, Component component) {
         super(i, j, component);
         this.iconX = k;
         this.iconY = l;
      }

      protected void renderIcon(GuiGraphics guigraphics) {
         guigraphics.blit(BeaconScreen.BEACON_LOCATION, this.getX() + 2, this.getY() + 2, this.iconX, this.iconY, 18, 18);
      }
   }

   class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
      public BeaconUpgradePowerButton(int i, int j, MobEffect mobeffect) {
         super(i, j, mobeffect, false, 3);
      }

      protected MutableComponent createEffectDescription(MobEffect mobeffect) {
         return Component.translatable(mobeffect.getDescriptionId()).append(" II");
      }

      public void updateStatus(int i) {
         if (BeaconScreen.this.primary != null) {
            this.visible = true;
            this.setEffect(BeaconScreen.this.primary);
            super.updateStatus(i);
         } else {
            this.visible = false;
         }

      }
   }
}
