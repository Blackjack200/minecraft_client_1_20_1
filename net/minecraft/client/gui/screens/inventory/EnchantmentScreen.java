package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
   private static final ResourceLocation ENCHANTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/enchanting_table.png");
   private static final ResourceLocation ENCHANTING_BOOK_LOCATION = new ResourceLocation("textures/entity/enchanting_table_book.png");
   private final RandomSource random = RandomSource.create();
   private BookModel bookModel;
   public int time;
   public float flip;
   public float oFlip;
   public float flipT;
   public float flipA;
   public float open;
   public float oOpen;
   private ItemStack last = ItemStack.EMPTY;

   public EnchantmentScreen(EnchantmentMenu enchantmentmenu, Inventory inventory, Component component) {
      super(enchantmentmenu, inventory, component);
   }

   protected void init() {
      super.init();
      this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
   }

   public void containerTick() {
      super.containerTick();
      this.tickBook();
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      int j = (this.width - this.imageWidth) / 2;
      int k = (this.height - this.imageHeight) / 2;

      for(int l = 0; l < 3; ++l) {
         double d2 = d0 - (double)(j + 60);
         double d3 = d1 - (double)(k + 14 + 19 * l);
         if (d2 >= 0.0D && d3 >= 0.0D && d2 < 108.0D && d3 < 19.0D && this.menu.clickMenuButton(this.minecraft.player, l)) {
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
            return true;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(ENCHANTING_TABLE_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      this.renderBook(guigraphics, k, l, f);
      EnchantmentNames.getInstance().initSeed((long)this.menu.getEnchantmentSeed());
      int i1 = this.menu.getGoldCount();

      for(int j1 = 0; j1 < 3; ++j1) {
         int k1 = k + 60;
         int l1 = k1 + 20;
         int i2 = (this.menu).costs[j1];
         if (i2 == 0) {
            guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1, l + 14 + 19 * j1, 0, 185, 108, 19);
         } else {
            String s = "" + i2;
            int j2 = 86 - this.font.width(s);
            FormattedText formattedtext = EnchantmentNames.getInstance().getRandomName(this.font, j2);
            int k2 = 6839882;
            if ((i1 < j1 + 1 || this.minecraft.player.experienceLevel < i2) && !this.minecraft.player.getAbilities().instabuild) {
               guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1, l + 14 + 19 * j1, 0, 185, 108, 19);
               guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1 + 1, l + 15 + 19 * j1, 16 * j1, 239, 16, 16);
               guigraphics.drawWordWrap(this.font, formattedtext, l1, l + 16 + 19 * j1, j2, (k2 & 16711422) >> 1);
               k2 = 4226832;
            } else {
               int l2 = i - (k + 60);
               int i3 = j - (l + 14 + 19 * j1);
               if (l2 >= 0 && i3 >= 0 && l2 < 108 && i3 < 19) {
                  guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1, l + 14 + 19 * j1, 0, 204, 108, 19);
                  k2 = 16777088;
               } else {
                  guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1, l + 14 + 19 * j1, 0, 166, 108, 19);
               }

               guigraphics.blit(ENCHANTING_TABLE_LOCATION, k1 + 1, l + 15 + 19 * j1, 16 * j1, 223, 16, 16);
               guigraphics.drawWordWrap(this.font, formattedtext, l1, l + 16 + 19 * j1, j2, k2);
               k2 = 8453920;
            }

            guigraphics.drawString(this.font, s, l1 + 86 - this.font.width(s), l + 16 + 19 * j1 + 7, k2);
         }
      }

   }

   private void renderBook(GuiGraphics guigraphics, int i, int j, float f) {
      float f1 = Mth.lerp(f, this.oOpen, this.open);
      float f2 = Mth.lerp(f, this.oFlip, this.flip);
      Lighting.setupForEntityInInventory();
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((float)i + 33.0F, (float)j + 31.0F, 100.0F);
      float f3 = 40.0F;
      guigraphics.pose().scale(-40.0F, 40.0F, 40.0F);
      guigraphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
      guigraphics.pose().translate((1.0F - f1) * 0.2F, (1.0F - f1) * 0.1F, (1.0F - f1) * 0.25F);
      float f4 = -(1.0F - f1) * 90.0F - 90.0F;
      guigraphics.pose().mulPose(Axis.YP.rotationDegrees(f4));
      guigraphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
      float f5 = Mth.clamp(Mth.frac(f2 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
      float f6 = Mth.clamp(Mth.frac(f2 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
      this.bookModel.setupAnim(0.0F, f5, f6, f1);
      VertexConsumer vertexconsumer = guigraphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
      this.bookModel.renderToBuffer(guigraphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      guigraphics.flush();
      guigraphics.pose().popPose();
      Lighting.setupFor3DItems();
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      f = this.minecraft.getFrameTime();
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
      boolean flag = this.minecraft.player.getAbilities().instabuild;
      int k = this.menu.getGoldCount();

      for(int l = 0; l < 3; ++l) {
         int i1 = (this.menu).costs[l];
         Enchantment enchantment = Enchantment.byId((this.menu).enchantClue[l]);
         int j1 = (this.menu).levelClue[l];
         int k1 = l + 1;
         if (this.isHovering(60, 14 + 19 * l, 108, 17, (double)i, (double)j) && i1 > 0 && j1 >= 0 && enchantment != null) {
            List<Component> list = Lists.newArrayList();
            list.add(Component.translatable("container.enchant.clue", enchantment.getFullname(j1)).withStyle(ChatFormatting.WHITE));
            if (!flag) {
               list.add(CommonComponents.EMPTY);
               if (this.minecraft.player.experienceLevel < i1) {
                  list.add(Component.translatable("container.enchant.level.requirement", (this.menu).costs[l]).withStyle(ChatFormatting.RED));
               } else {
                  MutableComponent mutablecomponent;
                  if (k1 == 1) {
                     mutablecomponent = Component.translatable("container.enchant.lapis.one");
                  } else {
                     mutablecomponent = Component.translatable("container.enchant.lapis.many", k1);
                  }

                  list.add(mutablecomponent.withStyle(k >= k1 ? ChatFormatting.GRAY : ChatFormatting.RED));
                  MutableComponent mutablecomponent2;
                  if (k1 == 1) {
                     mutablecomponent2 = Component.translatable("container.enchant.level.one");
                  } else {
                     mutablecomponent2 = Component.translatable("container.enchant.level.many", k1);
                  }

                  list.add(mutablecomponent2.withStyle(ChatFormatting.GRAY));
               }
            }

            guigraphics.renderComponentTooltip(this.font, list, i, j);
            break;
         }
      }

   }

   public void tickBook() {
      ItemStack itemstack = this.menu.getSlot(0).getItem();
      if (!ItemStack.matches(itemstack, this.last)) {
         this.last = itemstack;

         do {
            this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));
         } while(this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
      }

      ++this.time;
      this.oFlip = this.flip;
      this.oOpen = this.open;
      boolean flag = false;

      for(int i = 0; i < 3; ++i) {
         if ((this.menu).costs[i] != 0) {
            flag = true;
         }
      }

      if (flag) {
         this.open += 0.2F;
      } else {
         this.open -= 0.2F;
      }

      this.open = Mth.clamp(this.open, 0.0F, 1.0F);
      float f = (this.flipT - this.flip) * 0.4F;
      float f1 = 0.2F;
      f = Mth.clamp(f, -0.2F, 0.2F);
      this.flipA += (f - this.flipA) * 0.9F;
      this.flip += this.flipA;
   }
}
