package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
   private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
   private float xMouse;
   private float yMouse;
   private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
   private boolean widthTooNarrow;
   private boolean buttonClicked;

   public InventoryScreen(Player player) {
      super(player.inventoryMenu, player.getInventory(), Component.translatable("container.crafting"));
      this.titleLabelX = 97;
   }

   public void containerTick() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
      } else {
         this.recipeBookComponent.tick();
      }
   }

   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()));
      } else {
         super.init();
         this.widthTooNarrow = this.width < 379;
         this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
         this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
         this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            button.setPosition(this.leftPos + 104, this.height / 2 - 22);
            this.buttonClicked = true;
         }));
         this.addWidget(this.recipeBookComponent);
         this.setInitialFocus(this.recipeBookComponent);
      }
   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      guigraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
         this.renderBg(guigraphics, f, i, j);
         this.recipeBookComponent.render(guigraphics, i, j, f);
      } else {
         this.recipeBookComponent.render(guigraphics, i, j, f);
         super.render(guigraphics, i, j, f);
         this.recipeBookComponent.renderGhostRecipe(guigraphics, this.leftPos, this.topPos, false, f);
      }

      this.renderTooltip(guigraphics, i, j);
      this.recipeBookComponent.renderTooltip(guigraphics, this.leftPos, this.topPos, i, j);
      this.xMouse = (float)i;
      this.yMouse = (float)j;
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = this.leftPos;
      int l = this.topPos;
      guigraphics.blit(INVENTORY_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      renderEntityInInventoryFollowsMouse(guigraphics, k + 51, l + 75, 30, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.minecraft.player);
   }

   public static void renderEntityInInventoryFollowsMouse(GuiGraphics guigraphics, int i, int j, int k, float f, float f1, LivingEntity livingentity) {
      float f2 = (float)Math.atan((double)(f / 40.0F));
      float f3 = (float)Math.atan((double)(f1 / 40.0F));
      Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
      Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f3 * 20.0F * ((float)Math.PI / 180F));
      quaternionf.mul(quaternionf1);
      float f4 = livingentity.yBodyRot;
      float f5 = livingentity.getYRot();
      float f6 = livingentity.getXRot();
      float f7 = livingentity.yHeadRotO;
      float f8 = livingentity.yHeadRot;
      livingentity.yBodyRot = 180.0F + f2 * 20.0F;
      livingentity.setYRot(180.0F + f2 * 40.0F);
      livingentity.setXRot(-f3 * 20.0F);
      livingentity.yHeadRot = livingentity.getYRot();
      livingentity.yHeadRotO = livingentity.getYRot();
      renderEntityInInventory(guigraphics, i, j, k, quaternionf, quaternionf1, livingentity);
      livingentity.yBodyRot = f4;
      livingentity.setYRot(f5);
      livingentity.setXRot(f6);
      livingentity.yHeadRotO = f7;
      livingentity.yHeadRot = f8;
   }

   public static void renderEntityInInventory(GuiGraphics guigraphics, int i, int j, int k, Quaternionf quaternionf, @Nullable Quaternionf quaternionf1, LivingEntity livingentity) {
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((double)i, (double)j, 50.0D);
      guigraphics.pose().mulPoseMatrix((new Matrix4f()).scaling((float)k, (float)k, (float)(-k)));
      guigraphics.pose().mulPose(quaternionf);
      Lighting.setupForEntityInInventory();
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      if (quaternionf1 != null) {
         quaternionf1.conjugate();
         entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
      }

      entityrenderdispatcher.setRenderShadow(false);
      RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(livingentity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, guigraphics.pose(), guigraphics.bufferSource(), 15728880));
      guigraphics.flush();
      entityrenderdispatcher.setRenderShadow(true);
      guigraphics.pose().popPose();
      Lighting.setupFor3DItems();
   }

   protected boolean isHovering(int i, int j, int k, int l, double d0, double d1) {
      return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(i, j, k, l, d0, d1);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.recipeBookComponent.mouseClicked(d0, d1, i)) {
         this.setFocused(this.recipeBookComponent);
         return true;
      } else {
         return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(d0, d1, i);
      }
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      if (this.buttonClicked) {
         this.buttonClicked = false;
         return true;
      } else {
         return super.mouseReleased(d0, d1, i);
      }
   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      boolean flag = d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
      return this.recipeBookComponent.hasClickedOutside(d0, d1, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, k) && flag;
   }

   protected void slotClicked(Slot slot, int i, int j, ClickType clicktype) {
      super.slotClicked(slot, i, j, clicktype);
      this.recipeBookComponent.slotClicked(slot);
   }

   public void recipesUpdated() {
      this.recipeBookComponent.recipesUpdated();
   }

   public RecipeBookComponent getRecipeBookComponent() {
      return this.recipeBookComponent;
   }
}
