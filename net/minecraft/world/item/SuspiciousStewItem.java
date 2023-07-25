package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item {
   public static final String EFFECTS_TAG = "Effects";
   public static final String EFFECT_ID_TAG = "EffectId";
   public static final String EFFECT_DURATION_TAG = "EffectDuration";
   public static final int DEFAULT_DURATION = 160;

   public SuspiciousStewItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public static void saveMobEffect(ItemStack itemstack, MobEffect mobeffect, int i) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      ListTag listtag = compoundtag.getList("Effects", 9);
      CompoundTag compoundtag1 = new CompoundTag();
      compoundtag1.putInt("EffectId", MobEffect.getId(mobeffect));
      compoundtag1.putInt("EffectDuration", i);
      listtag.add(compoundtag1);
      compoundtag.put("Effects", listtag);
   }

   private static void listPotionEffects(ItemStack itemstack, Consumer<MobEffectInstance> consumer) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("Effects", 9)) {
         ListTag listtag = compoundtag.getList("Effects", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            int j;
            if (compoundtag1.contains("EffectDuration", 99)) {
               j = compoundtag1.getInt("EffectDuration");
            } else {
               j = 160;
            }

            MobEffect mobeffect = MobEffect.byId(compoundtag1.getInt("EffectId"));
            if (mobeffect != null) {
               consumer.accept(new MobEffectInstance(mobeffect, j));
            }
         }
      }

   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      if (tooltipflag.isCreative()) {
         List<MobEffectInstance> list1 = new ArrayList<>();
         listPotionEffects(itemstack, list1::add);
         PotionUtils.addPotionTooltip(list1, list, 1.0F);
      }

   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      ItemStack itemstack1 = super.finishUsingItem(itemstack, level, livingentity);
      listPotionEffects(itemstack1, livingentity::addEffect);
      return livingentity instanceof Player && ((Player)livingentity).getAbilities().instabuild ? itemstack1 : new ItemStack(Items.BOWL);
   }
}
