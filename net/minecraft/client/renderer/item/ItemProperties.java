package net.minecraft.client.renderer.item;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightBlock;

public class ItemProperties {
   private static final Map<ResourceLocation, ItemPropertyFunction> GENERIC_PROPERTIES = Maps.newHashMap();
   private static final String TAG_CUSTOM_MODEL_DATA = "CustomModelData";
   private static final ResourceLocation DAMAGED = new ResourceLocation("damaged");
   private static final ResourceLocation DAMAGE = new ResourceLocation("damage");
   private static final ClampedItemPropertyFunction PROPERTY_DAMAGED = (itemstack, clientlevel, livingentity, i) -> itemstack.isDamaged() ? 1.0F : 0.0F;
   private static final ClampedItemPropertyFunction PROPERTY_DAMAGE = (itemstack, clientlevel, livingentity, i) -> Mth.clamp((float)itemstack.getDamageValue() / (float)itemstack.getMaxDamage(), 0.0F, 1.0F);
   private static final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> PROPERTIES = Maps.newHashMap();

   private static ClampedItemPropertyFunction registerGeneric(ResourceLocation resourcelocation, ClampedItemPropertyFunction clampeditempropertyfunction) {
      GENERIC_PROPERTIES.put(resourcelocation, clampeditempropertyfunction);
      return clampeditempropertyfunction;
   }

   private static void registerCustomModelData(ItemPropertyFunction itempropertyfunction) {
      GENERIC_PROPERTIES.put(new ResourceLocation("custom_model_data"), itempropertyfunction);
   }

   private static void register(Item item, ResourceLocation resourcelocation, ClampedItemPropertyFunction clampeditempropertyfunction) {
      PROPERTIES.computeIfAbsent(item, (item1) -> Maps.newHashMap()).put(resourcelocation, clampeditempropertyfunction);
   }

   @Nullable
   public static ItemPropertyFunction getProperty(Item item, ResourceLocation resourcelocation) {
      if (item.getMaxDamage() > 0) {
         if (DAMAGE.equals(resourcelocation)) {
            return PROPERTY_DAMAGE;
         }

         if (DAMAGED.equals(resourcelocation)) {
            return PROPERTY_DAMAGED;
         }
      }

      ItemPropertyFunction itempropertyfunction = GENERIC_PROPERTIES.get(resourcelocation);
      if (itempropertyfunction != null) {
         return itempropertyfunction;
      } else {
         Map<ResourceLocation, ItemPropertyFunction> map = PROPERTIES.get(item);
         return map == null ? null : map.get(resourcelocation);
      }
   }

   static {
      registerGeneric(new ResourceLocation("lefthanded"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.getMainArm() != HumanoidArm.RIGHT ? 1.0F : 0.0F);
      registerGeneric(new ResourceLocation("cooldown"), (itemstack, clientlevel, livingentity, i) -> livingentity instanceof Player ? ((Player)livingentity).getCooldowns().getCooldownPercent(itemstack.getItem(), 0.0F) : 0.0F);
      ClampedItemPropertyFunction clampeditempropertyfunction = (itemstack, clientlevel, livingentity, i) -> {
         if (!itemstack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return Float.NEGATIVE_INFINITY;
         } else {
            return clientlevel == null ? 0.0F : ArmorTrim.getTrim(clientlevel.registryAccess(), itemstack).map(ArmorTrim::material).map(Holder::value).map(TrimMaterial::itemModelIndex).orElse(0.0F);
         }
      };
      registerGeneric(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, clampeditempropertyfunction);
      registerCustomModelData((itemstack, clientlevel, livingentity, i) -> itemstack.hasTag() ? (float)itemstack.getTag().getInt("CustomModelData") : 0.0F);
      register(Items.BOW, new ResourceLocation("pull"), (itemstack, clientlevel, livingentity, i) -> {
         if (livingentity == null) {
            return 0.0F;
         } else {
            return livingentity.getUseItem() != itemstack ? 0.0F : (float)(itemstack.getUseDuration() - livingentity.getUseItemRemainingTicks()) / 20.0F;
         }
      });
      register(Items.BRUSH, new ResourceLocation("brushing"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.getUseItem() == itemstack ? (float)(livingentity.getUseItemRemainingTicks() % 10) / 10.0F : 0.0F);
      register(Items.BOW, new ResourceLocation("pulling"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack ? 1.0F : 0.0F);
      register(Items.BUNDLE, new ResourceLocation("filled"), (itemstack, clientlevel, livingentity, i) -> BundleItem.getFullnessDisplay(itemstack));
      register(Items.CLOCK, new ResourceLocation("time"), new ClampedItemPropertyFunction() {
         private double rotation;
         private double rota;
         private long lastUpdateTick;

         public float unclampedCall(ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i) {
            Entity entity = (Entity)(livingentity != null ? livingentity : itemstack.getEntityRepresentation());
            if (entity == null) {
               return 0.0F;
            } else {
               if (clientlevel == null && entity.level() instanceof ClientLevel) {
                  clientlevel = (ClientLevel)entity.level();
               }

               if (clientlevel == null) {
                  return 0.0F;
               } else {
                  double d0;
                  if (clientlevel.dimensionType().natural()) {
                     d0 = (double)clientlevel.getTimeOfDay(1.0F);
                  } else {
                     d0 = Math.random();
                  }

                  d0 = this.wobble(clientlevel, d0);
                  return (float)d0;
               }
            }
         }

         private double wobble(Level level, double d0) {
            if (level.getGameTime() != this.lastUpdateTick) {
               this.lastUpdateTick = level.getGameTime();
               double d1 = d0 - this.rotation;
               d1 = Mth.positiveModulo(d1 + 0.5D, 1.0D) - 0.5D;
               this.rota += d1 * 0.1D;
               this.rota *= 0.9D;
               this.rotation = Mth.positiveModulo(this.rotation + this.rota, 1.0D);
            }

            return this.rotation;
         }
      });
      register(Items.COMPASS, new ResourceLocation("angle"), new CompassItemPropertyFunction((clientlevel, itemstack, entity) -> CompassItem.isLodestoneCompass(itemstack) ? CompassItem.getLodestonePosition(itemstack.getOrCreateTag()) : CompassItem.getSpawnPosition(clientlevel)));
      register(Items.RECOVERY_COMPASS, new ResourceLocation("angle"), new CompassItemPropertyFunction((clientlevel, itemstack, entity) -> {
         if (entity instanceof Player player) {
            return player.getLastDeathLocation().orElse((GlobalPos)null);
         } else {
            return null;
         }
      }));
      register(Items.CROSSBOW, new ResourceLocation("pull"), (itemstack, clientlevel, livingentity, i) -> {
         if (livingentity == null) {
            return 0.0F;
         } else {
            return CrossbowItem.isCharged(itemstack) ? 0.0F : (float)(itemstack.getUseDuration() - livingentity.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(itemstack);
         }
      });
      register(Items.CROSSBOW, new ResourceLocation("pulling"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack && !CrossbowItem.isCharged(itemstack) ? 1.0F : 0.0F);
      register(Items.CROSSBOW, new ResourceLocation("charged"), (itemstack, clientlevel, livingentity, i) -> CrossbowItem.isCharged(itemstack) ? 1.0F : 0.0F);
      register(Items.CROSSBOW, new ResourceLocation("firework"), (itemstack, clientlevel, livingentity, i) -> CrossbowItem.isCharged(itemstack) && CrossbowItem.containsChargedProjectile(itemstack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
      register(Items.ELYTRA, new ResourceLocation("broken"), (itemstack, clientlevel, livingentity, i) -> ElytraItem.isFlyEnabled(itemstack) ? 0.0F : 1.0F);
      register(Items.FISHING_ROD, new ResourceLocation("cast"), (itemstack, clientlevel, livingentity, i) -> {
         if (livingentity == null) {
            return 0.0F;
         } else {
            boolean flag = livingentity.getMainHandItem() == itemstack;
            boolean flag1 = livingentity.getOffhandItem() == itemstack;
            if (livingentity.getMainHandItem().getItem() instanceof FishingRodItem) {
               flag1 = false;
            }

            return (flag || flag1) && livingentity instanceof Player && ((Player)livingentity).fishing != null ? 1.0F : 0.0F;
         }
      });
      register(Items.SHIELD, new ResourceLocation("blocking"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack ? 1.0F : 0.0F);
      register(Items.TRIDENT, new ResourceLocation("throwing"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack ? 1.0F : 0.0F);
      register(Items.LIGHT, new ResourceLocation("level"), (itemstack, clientlevel, livingentity, i) -> {
         CompoundTag compoundtag = itemstack.getTagElement("BlockStateTag");

         try {
            if (compoundtag != null) {
               Tag tag = compoundtag.get(LightBlock.LEVEL.getName());
               if (tag != null) {
                  return (float)Integer.parseInt(tag.getAsString()) / 16.0F;
               }
            }
         } catch (NumberFormatException var6) {
         }

         return 1.0F;
      });
      register(Items.GOAT_HORN, new ResourceLocation("tooting"), (itemstack, clientlevel, livingentity, i) -> livingentity != null && livingentity.isUsingItem() && livingentity.getUseItem() == itemstack ? 1.0F : 0.0F);
   }
}
