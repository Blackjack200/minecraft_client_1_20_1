package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
   public EffectRenderingInventoryScreen(T abstractcontainermenu, Inventory inventory, Component component) {
      super(abstractcontainermenu, inventory, component);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      super.render(guigraphics, i, j, f);
      this.renderEffects(guigraphics, i, j);
   }

   public boolean canSeeEffects() {
      int i = this.leftPos + this.imageWidth + 2;
      int j = this.width - i;
      return j >= 32;
   }

   private void renderEffects(GuiGraphics guigraphics, int i, int j) {
      int k = this.leftPos + this.imageWidth + 2;
      int l = this.width - k;
      Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
      if (!collection.isEmpty() && l >= 32) {
         boolean flag = l >= 120;
         int i1 = 33;
         if (collection.size() > 5) {
            i1 = 132 / (collection.size() - 1);
         }

         Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
         this.renderBackgrounds(guigraphics, k, i1, iterable, flag);
         this.renderIcons(guigraphics, k, i1, iterable, flag);
         if (flag) {
            this.renderLabels(guigraphics, k, i1, iterable);
         } else if (i >= k && i <= k + 33) {
            int j1 = this.topPos;
            MobEffectInstance mobeffectinstance = null;

            for(MobEffectInstance mobeffectinstance1 : iterable) {
               if (j >= j1 && j <= j1 + i1) {
                  mobeffectinstance = mobeffectinstance1;
               }

               j1 += i1;
            }

            if (mobeffectinstance != null) {
               List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
               guigraphics.renderTooltip(this.font, list, Optional.empty(), i, j);
            }
         }

      }
   }

   private void renderBackgrounds(GuiGraphics guigraphics, int i, int j, Iterable<MobEffectInstance> iterable, boolean flag) {
      int k = this.topPos;

      for(MobEffectInstance mobeffectinstance : iterable) {
         if (flag) {
            guigraphics.blit(INVENTORY_LOCATION, i, k, 0, 166, 120, 32);
         } else {
            guigraphics.blit(INVENTORY_LOCATION, i, k, 0, 198, 32, 32);
         }

         k += j;
      }

   }

   private void renderIcons(GuiGraphics guigraphics, int i, int j, Iterable<MobEffectInstance> iterable, boolean flag) {
      MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
      int k = this.topPos;

      for(MobEffectInstance mobeffectinstance : iterable) {
         MobEffect mobeffect = mobeffectinstance.getEffect();
         TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
         guigraphics.blit(i + (flag ? 6 : 7), k + 7, 0, 18, 18, textureatlassprite);
         k += j;
      }

   }

   private void renderLabels(GuiGraphics guigraphics, int i, int j, Iterable<MobEffectInstance> iterable) {
      int k = this.topPos;

      for(MobEffectInstance mobeffectinstance : iterable) {
         Component component = this.getEffectName(mobeffectinstance);
         guigraphics.drawString(this.font, component, i + 10 + 18, k + 6, 16777215);
         Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
         guigraphics.drawString(this.font, component1, i + 10 + 18, k + 6 + 10, 8355711);
         k += j;
      }

   }

   private Component getEffectName(MobEffectInstance mobeffectinstance) {
      MutableComponent mutablecomponent = mobeffectinstance.getEffect().getDisplayName().copy();
      if (mobeffectinstance.getAmplifier() >= 1 && mobeffectinstance.getAmplifier() <= 9) {
         mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (mobeffectinstance.getAmplifier() + 1)));
      }

      return mutablecomponent;
   }
}
