package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class CyclingSlotBackground {
   private static final int ICON_CHANGE_TICK_RATE = 30;
   private static final int ICON_SIZE = 16;
   private static final int ICON_TRANSITION_TICK_DURATION = 4;
   private final int slotIndex;
   private List<ResourceLocation> icons = List.of();
   private int tick;
   private int iconIndex;

   public CyclingSlotBackground(int i) {
      this.slotIndex = i;
   }

   public void tick(List<ResourceLocation> list) {
      if (!this.icons.equals(list)) {
         this.icons = list;
         this.iconIndex = 0;
      }

      if (!this.icons.isEmpty() && ++this.tick % 30 == 0) {
         this.iconIndex = (this.iconIndex + 1) % this.icons.size();
      }

   }

   public void render(AbstractContainerMenu abstractcontainermenu, GuiGraphics guigraphics, float f, int i, int j) {
      Slot slot = abstractcontainermenu.getSlot(this.slotIndex);
      if (!this.icons.isEmpty() && !slot.hasItem()) {
         boolean flag = this.icons.size() > 1 && this.tick >= 30;
         float f1 = flag ? this.getIconTransitionTransparency(f) : 1.0F;
         if (f1 < 1.0F) {
            int k = Math.floorMod(this.iconIndex - 1, this.icons.size());
            this.renderIcon(slot, this.icons.get(k), 1.0F - f1, guigraphics, i, j);
         }

         this.renderIcon(slot, this.icons.get(this.iconIndex), f1, guigraphics, i, j);
      }
   }

   private void renderIcon(Slot slot, ResourceLocation resourcelocation, float f, GuiGraphics guigraphics, int i, int j) {
      TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(resourcelocation);
      guigraphics.blit(i + slot.x, j + slot.y, 0, 16, 16, textureatlassprite, 1.0F, 1.0F, 1.0F, f);
   }

   private float getIconTransitionTransparency(float f) {
      float f1 = (float)(this.tick % 30) + f;
      return Math.min(f1, 4.0F) / 4.0F;
   }
}
