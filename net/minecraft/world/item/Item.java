package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class Item implements FeatureElement, ItemLike {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
   protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
   protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
   public static final int MAX_STACK_SIZE = 64;
   public static final int EAT_DURATION = 32;
   public static final int MAX_BAR_WIDTH = 13;
   private final Holder.Reference<Item> builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
   private final Rarity rarity;
   private final int maxStackSize;
   private final int maxDamage;
   private final boolean isFireResistant;
   @Nullable
   private final Item craftingRemainingItem;
   @Nullable
   private String descriptionId;
   @Nullable
   private final FoodProperties foodProperties;
   private final FeatureFlagSet requiredFeatures;

   public static int getId(Item item) {
      return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
   }

   public static Item byId(int i) {
      return BuiltInRegistries.ITEM.byId(i);
   }

   /** @deprecated */
   @Deprecated
   public static Item byBlock(Block block) {
      return BY_BLOCK.getOrDefault(block, Items.AIR);
   }

   public Item(Item.Properties item_properties) {
      this.rarity = item_properties.rarity;
      this.craftingRemainingItem = item_properties.craftingRemainingItem;
      this.maxDamage = item_properties.maxDamage;
      this.maxStackSize = item_properties.maxStackSize;
      this.foodProperties = item_properties.foodProperties;
      this.isFireResistant = item_properties.isFireResistant;
      this.requiredFeatures = item_properties.requiredFeatures;
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         String s = this.getClass().getSimpleName();
         if (!s.endsWith("Item")) {
            LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)s);
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<Item> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public void onUseTick(Level level, LivingEntity livingentity, ItemStack itemstack, int i) {
   }

   public void onDestroyed(ItemEntity itementity) {
   }

   public void verifyTagAfterLoad(CompoundTag compoundtag) {
   }

   public boolean canAttackBlock(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      return true;
   }

   public Item asItem() {
      return this;
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      return InteractionResult.PASS;
   }

   public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
      return 1.0F;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      if (this.isEdible()) {
         ItemStack itemstack = player.getItemInHand(interactionhand);
         if (player.canEat(this.getFoodProperties().canAlwaysEat())) {
            player.startUsingItem(interactionhand);
            return InteractionResultHolder.consume(itemstack);
         } else {
            return InteractionResultHolder.fail(itemstack);
         }
      } else {
         return InteractionResultHolder.pass(player.getItemInHand(interactionhand));
      }
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      return this.isEdible() ? livingentity.eat(level, itemstack) : itemstack;
   }

   public final int getMaxStackSize() {
      return this.maxStackSize;
   }

   public final int getMaxDamage() {
      return this.maxDamage;
   }

   public boolean canBeDepleted() {
      return this.maxDamage > 0;
   }

   public boolean isBarVisible(ItemStack itemstack) {
      return itemstack.isDamaged();
   }

   public int getBarWidth(ItemStack itemstack) {
      return Math.round(13.0F - (float)itemstack.getDamageValue() * 13.0F / (float)this.maxDamage);
   }

   public int getBarColor(ItemStack itemstack) {
      float f = Math.max(0.0F, ((float)this.maxDamage - (float)itemstack.getDamageValue()) / (float)this.maxDamage);
      return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
   }

   public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, Player player) {
      return false;
   }

   public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, Player player, SlotAccess slotaccess) {
      return false;
   }

   public boolean hurtEnemy(ItemStack itemstack, LivingEntity livingentity, LivingEntity livingentity1) {
      return false;
   }

   public boolean mineBlock(ItemStack itemstack, Level level, BlockState blockstate, BlockPos blockpos, LivingEntity livingentity) {
      return false;
   }

   public boolean isCorrectToolForDrops(BlockState blockstate) {
      return false;
   }

   public InteractionResult interactLivingEntity(ItemStack itemstack, Player player, LivingEntity livingentity, InteractionHand interactionhand) {
      return InteractionResult.PASS;
   }

   public Component getDescription() {
      return Component.translatable(this.getDescriptionId());
   }

   public String toString() {
      return BuiltInRegistries.ITEM.getKey(this).getPath();
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
      }

      return this.descriptionId;
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   public String getDescriptionId(ItemStack itemstack) {
      return this.getDescriptionId();
   }

   public boolean shouldOverrideMultiplayerNbt() {
      return true;
   }

   @Nullable
   public final Item getCraftingRemainingItem() {
      return this.craftingRemainingItem;
   }

   public boolean hasCraftingRemainingItem() {
      return this.craftingRemainingItem != null;
   }

   public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int i, boolean flag) {
   }

   public void onCraftedBy(ItemStack itemstack, Level level, Player player) {
   }

   public boolean isComplex() {
      return false;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return itemstack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
   }

   public int getUseDuration(ItemStack itemstack) {
      if (itemstack.getItem().isEdible()) {
         return this.getFoodProperties().isFastFood() ? 16 : 32;
      } else {
         return 0;
      }
   }

   public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int i) {
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
   }

   public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
      return Optional.empty();
   }

   public Component getName(ItemStack itemstack) {
      return Component.translatable(this.getDescriptionId(itemstack));
   }

   public boolean isFoil(ItemStack itemstack) {
      return itemstack.isEnchanted();
   }

   public Rarity getRarity(ItemStack itemstack) {
      if (!itemstack.isEnchanted()) {
         return this.rarity;
      } else {
         switch (this.rarity) {
            case COMMON:
            case UNCOMMON:
               return Rarity.RARE;
            case RARE:
               return Rarity.EPIC;
            case EPIC:
            default:
               return this.rarity;
         }
      }
   }

   public boolean isEnchantable(ItemStack itemstack) {
      return this.getMaxStackSize() == 1 && this.canBeDepleted();
   }

   protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid clipcontext_fluid) {
      float f = player.getXRot();
      float f1 = player.getYRot();
      Vec3 vec3 = player.getEyePosition();
      float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
      float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
      float f6 = f3 * f4;
      float f8 = f2 * f4;
      double d0 = 5.0D;
      Vec3 vec31 = vec3.add((double)f6 * 5.0D, (double)f5 * 5.0D, (double)f8 * 5.0D);
      return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, clipcontext_fluid, player));
   }

   public int getEnchantmentValue() {
      return 0;
   }

   public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
      return false;
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentslot) {
      return ImmutableMultimap.of();
   }

   public boolean useOnRelease(ItemStack itemstack) {
      return false;
   }

   public ItemStack getDefaultInstance() {
      return new ItemStack(this);
   }

   public boolean isEdible() {
      return this.foodProperties != null;
   }

   @Nullable
   public FoodProperties getFoodProperties() {
      return this.foodProperties;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.GENERIC_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.GENERIC_EAT;
   }

   public boolean isFireResistant() {
      return this.isFireResistant;
   }

   public boolean canBeHurtBy(DamageSource damagesource) {
      return !this.isFireResistant || !damagesource.is(DamageTypeTags.IS_FIRE);
   }

   public boolean canFitInsideContainerItems() {
      return true;
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   public static class Properties {
      int maxStackSize = 64;
      int maxDamage;
      @Nullable
      Item craftingRemainingItem;
      Rarity rarity = Rarity.COMMON;
      @Nullable
      FoodProperties foodProperties;
      boolean isFireResistant;
      FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

      public Item.Properties food(FoodProperties foodproperties) {
         this.foodProperties = foodproperties;
         return this;
      }

      public Item.Properties stacksTo(int i) {
         if (this.maxDamage > 0) {
            throw new RuntimeException("Unable to have damage AND stack.");
         } else {
            this.maxStackSize = i;
            return this;
         }
      }

      public Item.Properties defaultDurability(int i) {
         return this.maxDamage == 0 ? this.durability(i) : this;
      }

      public Item.Properties durability(int i) {
         this.maxDamage = i;
         this.maxStackSize = 1;
         return this;
      }

      public Item.Properties craftRemainder(Item item) {
         this.craftingRemainingItem = item;
         return this;
      }

      public Item.Properties rarity(Rarity rarity) {
         this.rarity = rarity;
         return this;
      }

      public Item.Properties fireResistant() {
         this.isFireResistant = true;
         return this;
      }

      public Item.Properties requiredFeatures(FeatureFlag... afeatureflag) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
         return this;
      }
   }
}
