package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CrossbowItem extends ProjectileWeaponItem implements Vanishable {
   private static final String TAG_CHARGED = "Charged";
   private static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";
   private static final int MAX_CHARGE_DURATION = 25;
   public static final int DEFAULT_RANGE = 8;
   private boolean startSoundPlayed = false;
   private boolean midLoadSoundPlayed = false;
   private static final float START_SOUND_PERCENT = 0.2F;
   private static final float MID_SOUND_PERCENT = 0.5F;
   private static final float ARROW_POWER = 3.15F;
   private static final float FIREWORK_POWER = 1.6F;

   public CrossbowItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public Predicate<ItemStack> getSupportedHeldProjectiles() {
      return ARROW_OR_FIREWORK;
   }

   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (isCharged(itemstack)) {
         performShooting(level, player, interactionhand, itemstack, getShootingPower(itemstack), 1.0F);
         setCharged(itemstack, false);
         return InteractionResultHolder.consume(itemstack);
      } else if (!player.getProjectile(itemstack).isEmpty()) {
         if (!isCharged(itemstack)) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            player.startUsingItem(interactionhand);
         }

         return InteractionResultHolder.consume(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   private static float getShootingPower(ItemStack itemstack) {
      return containsChargedProjectile(itemstack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
   }

   public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int i) {
      int j = this.getUseDuration(itemstack) - i;
      float f = getPowerForTime(j, itemstack);
      if (f >= 1.0F && !isCharged(itemstack) && tryLoadProjectiles(livingentity, itemstack)) {
         setCharged(itemstack, true);
         SoundSource soundsource = livingentity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
         level.playSound((Player)null, livingentity.getX(), livingentity.getY(), livingentity.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
      }

   }

   private static boolean tryLoadProjectiles(LivingEntity livingentity, ItemStack itemstack) {
      int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemstack);
      int j = i == 0 ? 1 : 3;
      boolean flag = livingentity instanceof Player && ((Player)livingentity).getAbilities().instabuild;
      ItemStack itemstack1 = livingentity.getProjectile(itemstack);
      ItemStack itemstack2 = itemstack1.copy();

      for(int k = 0; k < j; ++k) {
         if (k > 0) {
            itemstack1 = itemstack2.copy();
         }

         if (itemstack1.isEmpty() && flag) {
            itemstack1 = new ItemStack(Items.ARROW);
            itemstack2 = itemstack1.copy();
         }

         if (!loadProjectile(livingentity, itemstack, itemstack1, k > 0, flag)) {
            return false;
         }
      }

      return true;
   }

   private static boolean loadProjectile(LivingEntity livingentity, ItemStack itemstack, ItemStack itemstack1, boolean flag, boolean flag1) {
      if (itemstack1.isEmpty()) {
         return false;
      } else {
         boolean flag2 = flag1 && itemstack1.getItem() instanceof ArrowItem;
         ItemStack itemstack2;
         if (!flag2 && !flag1 && !flag) {
            itemstack2 = itemstack1.split(1);
            if (itemstack1.isEmpty() && livingentity instanceof Player) {
               ((Player)livingentity).getInventory().removeItem(itemstack1);
            }
         } else {
            itemstack2 = itemstack1.copy();
         }

         addChargedProjectile(itemstack, itemstack2);
         return true;
      }
   }

   public static boolean isCharged(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null && compoundtag.getBoolean("Charged");
   }

   public static void setCharged(ItemStack itemstack, boolean flag) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      compoundtag.putBoolean("Charged", flag);
   }

   private static void addChargedProjectile(ItemStack itemstack, ItemStack itemstack1) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      ListTag listtag;
      if (compoundtag.contains("ChargedProjectiles", 9)) {
         listtag = compoundtag.getList("ChargedProjectiles", 10);
      } else {
         listtag = new ListTag();
      }

      CompoundTag compoundtag1 = new CompoundTag();
      itemstack1.save(compoundtag1);
      listtag.add(compoundtag1);
      compoundtag.put("ChargedProjectiles", listtag);
   }

   private static List<ItemStack> getChargedProjectiles(ItemStack itemstack) {
      List<ItemStack> list = Lists.newArrayList();
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
         ListTag listtag = compoundtag.getList("ChargedProjectiles", 10);
         if (listtag != null) {
            for(int i = 0; i < listtag.size(); ++i) {
               CompoundTag compoundtag1 = listtag.getCompound(i);
               list.add(ItemStack.of(compoundtag1));
            }
         }
      }

      return list;
   }

   private static void clearChargedProjectiles(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null) {
         ListTag listtag = compoundtag.getList("ChargedProjectiles", 9);
         listtag.clear();
         compoundtag.put("ChargedProjectiles", listtag);
      }

   }

   public static boolean containsChargedProjectile(ItemStack itemstack, Item item) {
      return getChargedProjectiles(itemstack).stream().anyMatch((itemstack1) -> itemstack1.is(item));
   }

   private static void shootProjectile(Level level, LivingEntity livingentity, InteractionHand interactionhand, ItemStack itemstack, ItemStack itemstack1, float f, boolean flag, float f1, float f2, float f3) {
      if (!level.isClientSide) {
         boolean flag1 = itemstack1.is(Items.FIREWORK_ROCKET);
         Projectile projectile;
         if (flag1) {
            projectile = new FireworkRocketEntity(level, itemstack1, livingentity, livingentity.getX(), livingentity.getEyeY() - (double)0.15F, livingentity.getZ(), true);
         } else {
            projectile = getArrow(level, livingentity, itemstack, itemstack1);
            if (flag || f3 != 0.0F) {
               ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
         }

         if (livingentity instanceof CrossbowAttackMob) {
            CrossbowAttackMob crossbowattackmob = (CrossbowAttackMob)livingentity;
            crossbowattackmob.shootCrossbowProjectile(crossbowattackmob.getTarget(), itemstack, projectile, f3);
         } else {
            Vec3 vec3 = livingentity.getUpVector(1.0F);
            Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((double)(f3 * ((float)Math.PI / 180F)), vec3.x, vec3.y, vec3.z);
            Vec3 vec31 = livingentity.getViewVector(1.0F);
            Vector3f vector3f = vec31.toVector3f().rotate(quaternionf);
            projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), f1, f2);
         }

         itemstack.hurtAndBreak(flag1 ? 3 : 1, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(interactionhand));
         level.addFreshEntity(projectile);
         level.playSound((Player)null, livingentity.getX(), livingentity.getY(), livingentity.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, f);
      }
   }

   private static AbstractArrow getArrow(Level level, LivingEntity livingentity, ItemStack itemstack, ItemStack itemstack1) {
      ArrowItem arrowitem = (ArrowItem)(itemstack1.getItem() instanceof ArrowItem ? itemstack1.getItem() : Items.ARROW);
      AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack1, livingentity);
      if (livingentity instanceof Player) {
         abstractarrow.setCritArrow(true);
      }

      abstractarrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
      abstractarrow.setShotFromCrossbow(true);
      int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, itemstack);
      if (i > 0) {
         abstractarrow.setPierceLevel((byte)i);
      }

      return abstractarrow;
   }

   public static void performShooting(Level level, LivingEntity livingentity, InteractionHand interactionhand, ItemStack itemstack, float f, float f1) {
      List<ItemStack> list = getChargedProjectiles(itemstack);
      float[] afloat = getShotPitches(livingentity.getRandom());

      for(int i = 0; i < list.size(); ++i) {
         ItemStack itemstack1 = list.get(i);
         boolean flag = livingentity instanceof Player && ((Player)livingentity).getAbilities().instabuild;
         if (!itemstack1.isEmpty()) {
            if (i == 0) {
               shootProjectile(level, livingentity, interactionhand, itemstack, itemstack1, afloat[i], flag, f, f1, 0.0F);
            } else if (i == 1) {
               shootProjectile(level, livingentity, interactionhand, itemstack, itemstack1, afloat[i], flag, f, f1, -10.0F);
            } else if (i == 2) {
               shootProjectile(level, livingentity, interactionhand, itemstack, itemstack1, afloat[i], flag, f, f1, 10.0F);
            }
         }
      }

      onCrossbowShot(level, livingentity, itemstack);
   }

   private static float[] getShotPitches(RandomSource randomsource) {
      boolean flag = randomsource.nextBoolean();
      return new float[]{1.0F, getRandomShotPitch(flag, randomsource), getRandomShotPitch(!flag, randomsource)};
   }

   private static float getRandomShotPitch(boolean flag, RandomSource randomsource) {
      float f = flag ? 0.63F : 0.43F;
      return 1.0F / (randomsource.nextFloat() * 0.5F + 1.8F) + f;
   }

   private static void onCrossbowShot(Level level, LivingEntity livingentity, ItemStack itemstack) {
      if (livingentity instanceof ServerPlayer serverplayer) {
         if (!level.isClientSide) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverplayer, itemstack);
         }

         serverplayer.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
      }

      clearChargedProjectiles(itemstack);
   }

   public void onUseTick(Level level, LivingEntity livingentity, ItemStack itemstack, int i) {
      if (!level.isClientSide) {
         int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemstack);
         SoundEvent soundevent = this.getStartSound(j);
         SoundEvent soundevent1 = j == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
         float f = (float)(itemstack.getUseDuration() - i) / (float)getChargeDuration(itemstack);
         if (f < 0.2F) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
         }

         if (f >= 0.2F && !this.startSoundPlayed) {
            this.startSoundPlayed = true;
            level.playSound((Player)null, livingentity.getX(), livingentity.getY(), livingentity.getZ(), soundevent, SoundSource.PLAYERS, 0.5F, 1.0F);
         }

         if (f >= 0.5F && soundevent1 != null && !this.midLoadSoundPlayed) {
            this.midLoadSoundPlayed = true;
            level.playSound((Player)null, livingentity.getX(), livingentity.getY(), livingentity.getZ(), soundevent1, SoundSource.PLAYERS, 0.5F, 1.0F);
         }
      }

   }

   public int getUseDuration(ItemStack itemstack) {
      return getChargeDuration(itemstack) + 3;
   }

   public static int getChargeDuration(ItemStack itemstack) {
      int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemstack);
      return i == 0 ? 25 : 25 - 5 * i;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.CROSSBOW;
   }

   private SoundEvent getStartSound(int i) {
      switch (i) {
         case 1:
            return SoundEvents.CROSSBOW_QUICK_CHARGE_1;
         case 2:
            return SoundEvents.CROSSBOW_QUICK_CHARGE_2;
         case 3:
            return SoundEvents.CROSSBOW_QUICK_CHARGE_3;
         default:
            return SoundEvents.CROSSBOW_LOADING_START;
      }
   }

   private static float getPowerForTime(int i, ItemStack itemstack) {
      float f = (float)i / (float)getChargeDuration(itemstack);
      if (f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      List<ItemStack> list1 = getChargedProjectiles(itemstack);
      if (isCharged(itemstack) && !list1.isEmpty()) {
         ItemStack itemstack1 = list1.get(0);
         list.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemstack1.getDisplayName()));
         if (tooltipflag.isAdvanced() && itemstack1.is(Items.FIREWORK_ROCKET)) {
            List<Component> list2 = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendHoverText(itemstack1, level, list2, tooltipflag);
            if (!list2.isEmpty()) {
               for(int i = 0; i < list2.size(); ++i) {
                  list2.set(i, Component.literal("  ").append(list2.get(i)).withStyle(ChatFormatting.GRAY));
               }

               list.addAll(list2);
            }
         }

      }
   }

   public boolean useOnRelease(ItemStack itemstack) {
      return itemstack.is(this);
   }

   public int getDefaultProjectileRange() {
      return 8;
   }
}
