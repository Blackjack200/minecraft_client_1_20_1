package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LoomScreen extends AbstractContainerScreen<LoomMenu> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
   private static final int PATTERN_COLUMNS = 4;
   private static final int PATTERN_ROWS = 4;
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   private static final int PATTERN_IMAGE_SIZE = 14;
   private static final int SCROLLER_FULL_HEIGHT = 56;
   private static final int PATTERNS_X = 60;
   private static final int PATTERNS_Y = 13;
   private ModelPart flag;
   @Nullable
   private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
   private ItemStack bannerStack = ItemStack.EMPTY;
   private ItemStack dyeStack = ItemStack.EMPTY;
   private ItemStack patternStack = ItemStack.EMPTY;
   private boolean displayPatterns;
   private boolean hasMaxPatterns;
   private float scrollOffs;
   private boolean scrolling;
   private int startRow;

   public LoomScreen(LoomMenu loommenu, Inventory inventory, Component component) {
      super(loommenu, inventory, component);
      loommenu.registerUpdateListener(this::containerChanged);
      this.titleLabelY -= 2;
   }

   protected void init() {
      super.init();
      this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   private int totalRowCount() {
      return Mth.positiveCeilDiv(this.menu.getSelectablePatterns().size(), 4);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      this.renderBackground(guigraphics);
      int k = this.leftPos;
      int l = this.topPos;
      guigraphics.blit(BG_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      Slot slot = this.menu.getBannerSlot();
      Slot slot1 = this.menu.getDyeSlot();
      Slot slot2 = this.menu.getPatternSlot();
      Slot slot3 = this.menu.getResultSlot();
      if (!slot.hasItem()) {
         guigraphics.blit(BG_LOCATION, k + slot.x, l + slot.y, this.imageWidth, 0, 16, 16);
      }

      if (!slot1.hasItem()) {
         guigraphics.blit(BG_LOCATION, k + slot1.x, l + slot1.y, this.imageWidth + 16, 0, 16, 16);
      }

      if (!slot2.hasItem()) {
         guigraphics.blit(BG_LOCATION, k + slot2.x, l + slot2.y, this.imageWidth + 32, 0, 16, 16);
      }

      int i1 = (int)(41.0F * this.scrollOffs);
      guigraphics.blit(BG_LOCATION, k + 119, l + 13 + i1, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
      Lighting.setupForFlatItems();
      if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)(k + 139), (float)(l + 52), 0.0F);
         guigraphics.pose().scale(24.0F, -24.0F, 1.0F);
         guigraphics.pose().translate(0.5F, 0.5F, 0.5F);
         float f1 = 0.6666667F;
         guigraphics.pose().scale(0.6666667F, -0.6666667F, -0.6666667F);
         this.flag.xRot = 0.0F;
         this.flag.y = -32.0F;
         BannerRenderer.renderPatterns(guigraphics.pose(), guigraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns);
         guigraphics.pose().popPose();
         guigraphics.flush();
      } else if (this.hasMaxPatterns) {
         guigraphics.blit(BG_LOCATION, k + slot3.x - 2, l + slot3.y - 2, this.imageWidth, 17, 17, 16);
      }

      if (this.displayPatterns) {
         int j1 = k + 60;
         int k1 = l + 13;
         List<Holder<BannerPattern>> list = this.menu.getSelectablePatterns();

         label63:
         for(int l1 = 0; l1 < 4; ++l1) {
            for(int i2 = 0; i2 < 4; ++i2) {
               int j2 = l1 + this.startRow;
               int k2 = j2 * 4 + i2;
               if (k2 >= list.size()) {
                  break label63;
               }

               int l2 = j1 + i2 * 14;
               int i3 = k1 + l1 * 14;
               boolean flag = i >= l2 && j >= i3 && i < l2 + 14 && j < i3 + 14;
               int j3;
               if (k2 == this.menu.getSelectedBannerPatternIndex()) {
                  j3 = this.imageHeight + 14;
               } else if (flag) {
                  j3 = this.imageHeight + 28;
               } else {
                  j3 = this.imageHeight;
               }

               guigraphics.blit(BG_LOCATION, l2, i3, 0, j3, 14, 14);
               this.renderPattern(guigraphics, list.get(k2), l2, i3);
            }
         }
      }

      Lighting.setupFor3DItems();
   }

   private void renderPattern(GuiGraphics guigraphics, Holder<BannerPattern> holder, int i, int j) {
      CompoundTag compoundtag = new CompoundTag();
      ListTag listtag = (new BannerPattern.Builder()).addPattern(BannerPatterns.BASE, DyeColor.GRAY).addPattern(holder, DyeColor.WHITE).toListTag();
      compoundtag.put("Patterns", listtag);
      ItemStack itemstack = new ItemStack(Items.GRAY_BANNER);
      BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, compoundtag);
      PoseStack posestack = new PoseStack();
      posestack.pushPose();
      posestack.translate((float)i + 0.5F, (float)(j + 16), 0.0F);
      posestack.scale(6.0F, -6.0F, 1.0F);
      posestack.translate(0.5F, 0.5F, 0.0F);
      posestack.translate(0.5F, 0.5F, 0.5F);
      float f = 0.6666667F;
      posestack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      this.flag.xRot = 0.0F;
      this.flag.y = -32.0F;
      List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(itemstack));
      BannerRenderer.renderPatterns(posestack, guigraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, list);
      posestack.popPose();
      guigraphics.flush();
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      this.scrolling = false;
      if (this.displayPatterns) {
         int j = this.leftPos + 60;
         int k = this.topPos + 13;

         for(int l = 0; l < 4; ++l) {
            for(int i1 = 0; i1 < 4; ++i1) {
               double d2 = d0 - (double)(j + i1 * 14);
               double d3 = d1 - (double)(k + l * 14);
               int j1 = l + this.startRow;
               int k1 = j1 * 4 + i1;
               if (d2 >= 0.0D && d3 >= 0.0D && d2 < 14.0D && d3 < 14.0D && this.menu.clickMenuButton(this.minecraft.player, k1)) {
                  Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                  this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, k1);
                  return true;
               }
            }
         }

         j = this.leftPos + 119;
         k = this.topPos + 9;
         if (d0 >= (double)j && d0 < (double)(j + 12) && d1 >= (double)k && d1 < (double)(k + 56)) {
            this.scrolling = true;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      int j = this.totalRowCount() - 4;
      if (this.scrolling && this.displayPatterns && j > 0) {
         int k = this.topPos + 13;
         int l = k + 56;
         this.scrollOffs = ((float)d1 - (float)k - 7.5F) / ((float)(l - k) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.startRow = Math.max((int)((double)(this.scrollOffs * (float)j) + 0.5D), 0);
         return true;
      } else {
         return super.mouseDragged(d0, d1, i, d2, d3);
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      int i = this.totalRowCount() - 4;
      if (this.displayPatterns && i > 0) {
         float f = (float)d2 / (float)i;
         this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
         this.startRow = Math.max((int)(this.scrollOffs * (float)i + 0.5F), 0);
      }

      return true;
   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      return d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
   }

   private void containerChanged() {
      ItemStack itemstack = this.menu.getResultSlot().getItem();
      if (itemstack.isEmpty()) {
         this.resultBannerPatterns = null;
      } else {
         this.resultBannerPatterns = BannerBlockEntity.createPatterns(((BannerItem)itemstack.getItem()).getColor(), BannerBlockEntity.getItemPatterns(itemstack));
      }

      ItemStack itemstack1 = this.menu.getBannerSlot().getItem();
      ItemStack itemstack2 = this.menu.getDyeSlot().getItem();
      ItemStack itemstack3 = this.menu.getPatternSlot().getItem();
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack1);
      this.hasMaxPatterns = compoundtag != null && compoundtag.contains("Patterns", 9) && !itemstack1.isEmpty() && compoundtag.getList("Patterns", 10).size() >= 6;
      if (this.hasMaxPatterns) {
         this.resultBannerPatterns = null;
      }

      if (!ItemStack.matches(itemstack1, this.bannerStack) || !ItemStack.matches(itemstack2, this.dyeStack) || !ItemStack.matches(itemstack3, this.patternStack)) {
         this.displayPatterns = !itemstack1.isEmpty() && !itemstack2.isEmpty() && !this.hasMaxPatterns && !this.menu.getSelectablePatterns().isEmpty();
      }

      if (this.startRow >= this.totalRowCount()) {
         this.startRow = 0;
         this.scrollOffs = 0.0F;
      }

      this.bannerStack = itemstack1.copy();
      this.dyeStack = itemstack2.copy();
      this.patternStack = itemstack3.copy();
   }
}
