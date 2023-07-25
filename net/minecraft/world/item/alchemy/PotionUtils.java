package net.minecraft.world.item.alchemy;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class PotionUtils {
   public static final String TAG_CUSTOM_POTION_EFFECTS = "CustomPotionEffects";
   public static final String TAG_CUSTOM_POTION_COLOR = "CustomPotionColor";
   public static final String TAG_POTION = "Potion";
   private static final int EMPTY_COLOR = 16253176;
   private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);

   public static List<MobEffectInstance> getMobEffects(ItemStack itemstack) {
      return getAllEffects(itemstack.getTag());
   }

   public static List<MobEffectInstance> getAllEffects(Potion potion, Collection<MobEffectInstance> collection) {
      List<MobEffectInstance> list = Lists.newArrayList();
      list.addAll(potion.getEffects());
      list.addAll(collection);
      return list;
   }

   public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag compoundtag) {
      List<MobEffectInstance> list = Lists.newArrayList();
      list.addAll(getPotion(compoundtag).getEffects());
      getCustomEffects(compoundtag, list);
      return list;
   }

   public static List<MobEffectInstance> getCustomEffects(ItemStack itemstack) {
      return getCustomEffects(itemstack.getTag());
   }

   public static List<MobEffectInstance> getCustomEffects(@Nullable CompoundTag compoundtag) {
      List<MobEffectInstance> list = Lists.newArrayList();
      getCustomEffects(compoundtag, list);
      return list;
   }

   public static void getCustomEffects(@Nullable CompoundTag compoundtag, List<MobEffectInstance> list) {
      if (compoundtag != null && compoundtag.contains("CustomPotionEffects", 9)) {
         ListTag listtag = compoundtag.getList("CustomPotionEffects", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag1);
            if (mobeffectinstance != null) {
               list.add(mobeffectinstance);
            }
         }
      }

   }

   public static int getColor(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
         return compoundtag.getInt("CustomPotionColor");
      } else {
         return getPotion(itemstack) == Potions.EMPTY ? 16253176 : getColor(getMobEffects(itemstack));
      }
   }

   public static int getColor(Potion potion) {
      return potion == Potions.EMPTY ? 16253176 : getColor(potion.getEffects());
   }

   public static int getColor(Collection<MobEffectInstance> collection) {
      int i = 3694022;
      if (collection.isEmpty()) {
         return 3694022;
      } else {
         float f = 0.0F;
         float f1 = 0.0F;
         float f2 = 0.0F;
         int j = 0;

         for(MobEffectInstance mobeffectinstance : collection) {
            if (mobeffectinstance.isVisible()) {
               int k = mobeffectinstance.getEffect().getColor();
               int l = mobeffectinstance.getAmplifier() + 1;
               f += (float)(l * (k >> 16 & 255)) / 255.0F;
               f1 += (float)(l * (k >> 8 & 255)) / 255.0F;
               f2 += (float)(l * (k >> 0 & 255)) / 255.0F;
               j += l;
            }
         }

         if (j == 0) {
            return 0;
         } else {
            f = f / (float)j * 255.0F;
            f1 = f1 / (float)j * 255.0F;
            f2 = f2 / (float)j * 255.0F;
            return (int)f << 16 | (int)f1 << 8 | (int)f2;
         }
      }
   }

   public static Potion getPotion(ItemStack itemstack) {
      return getPotion(itemstack.getTag());
   }

   public static Potion getPotion(@Nullable CompoundTag compoundtag) {
      return compoundtag == null ? Potions.EMPTY : Potion.byName(compoundtag.getString("Potion"));
   }

   public static ItemStack setPotion(ItemStack itemstack, Potion potion) {
      ResourceLocation resourcelocation = BuiltInRegistries.POTION.getKey(potion);
      if (potion == Potions.EMPTY) {
         itemstack.removeTagKey("Potion");
      } else {
         itemstack.getOrCreateTag().putString("Potion", resourcelocation.toString());
      }

      return itemstack;
   }

   public static ItemStack setCustomEffects(ItemStack itemstack, Collection<MobEffectInstance> collection) {
      if (collection.isEmpty()) {
         return itemstack;
      } else {
         CompoundTag compoundtag = itemstack.getOrCreateTag();
         ListTag listtag = compoundtag.getList("CustomPotionEffects", 9);

         for(MobEffectInstance mobeffectinstance : collection) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         compoundtag.put("CustomPotionEffects", listtag);
         return itemstack;
      }
   }

   public static void addPotionTooltip(ItemStack itemstack, List<Component> list, float f) {
      addPotionTooltip(getMobEffects(itemstack), list, f);
   }

   public static void addPotionTooltip(List<MobEffectInstance> list, List<Component> list1, float f) {
      List<Pair<Attribute, AttributeModifier>> list2 = Lists.newArrayList();
      if (list.isEmpty()) {
         list1.add(NO_EFFECT);
      } else {
         for(MobEffectInstance mobeffectinstance : list) {
            MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
            MobEffect mobeffect = mobeffectinstance.getEffect();
            Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
            if (!map.isEmpty()) {
               for(Map.Entry<Attribute, AttributeModifier> map_entry : map.entrySet()) {
                  AttributeModifier attributemodifier = map_entry.getValue();
                  AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                  list2.add(new Pair<>(map_entry.getKey(), attributemodifier1));
               }
            }

            if (mobeffectinstance.getAmplifier() > 0) {
               mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
            }

            if (!mobeffectinstance.endsWithin(20)) {
               mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, f));
            }

            list1.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
         }
      }

      if (!list2.isEmpty()) {
         list1.add(CommonComponents.EMPTY);
         list1.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));

         for(Pair<Attribute, AttributeModifier> pair : list2) {
            AttributeModifier attributemodifier2 = pair.getSecond();
            double d0 = attributemodifier2.getAmount();
            double d2;
            if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
               d2 = attributemodifier2.getAmount();
            } else {
               d2 = attributemodifier2.getAmount() * 100.0D;
            }

            if (d0 > 0.0D) {
               list1.add(Component.translatable("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.BLUE));
            } else if (d0 < 0.0D) {
               d2 *= -1.0D;
               list1.add(Component.translatable("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(pair.getFirst().getDescriptionId())).withStyle(ChatFormatting.RED));
            }
         }
      }

   }
}
