package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.slf4j.Logger;

public final class ItemStack {
   public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStack::getItem), Codec.INT.fieldOf("Count").forGetter(ItemStack::getCount), CompoundTag.CODEC.optionalFieldOf("tag").forGetter((itemstack) -> Optional.ofNullable(itemstack.getTag()))).apply(recordcodecbuilder_instance, ItemStack::new));
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ItemStack EMPTY = new ItemStack((Void)null);
   public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (decimalformat) -> decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
   public static final String TAG_ENCH = "Enchantments";
   public static final String TAG_DISPLAY = "display";
   public static final String TAG_DISPLAY_NAME = "Name";
   public static final String TAG_LORE = "Lore";
   public static final String TAG_DAMAGE = "Damage";
   public static final String TAG_COLOR = "color";
   private static final String TAG_UNBREAKABLE = "Unbreakable";
   private static final String TAG_REPAIR_COST = "RepairCost";
   private static final String TAG_CAN_DESTROY_BLOCK_LIST = "CanDestroy";
   private static final String TAG_CAN_PLACE_ON_BLOCK_LIST = "CanPlaceOn";
   private static final String TAG_HIDE_FLAGS = "HideFlags";
   private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
   private static final int DONT_HIDE_TOOLTIP = 0;
   private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
   private int count;
   private int popTime;
   /** @deprecated */
   @Deprecated
   @Nullable
   private final Item item;
   @Nullable
   private CompoundTag tag;
   @Nullable
   private Entity entityRepresentation;
   @Nullable
   private AdventureModeCheck adventureBreakCheck;
   @Nullable
   private AdventureModeCheck adventurePlaceCheck;

   public Optional<TooltipComponent> getTooltipImage() {
      return this.getItem().getTooltipImage(this);
   }

   public ItemStack(ItemLike itemlike) {
      this(itemlike, 1);
   }

   public ItemStack(Holder<Item> holder) {
      this(holder.value(), 1);
   }

   private ItemStack(ItemLike itemlike, int i, Optional<CompoundTag> optional) {
      this(itemlike, i);
      optional.ifPresent(this::setTag);
   }

   public ItemStack(Holder<Item> holder, int i) {
      this(holder.value(), i);
   }

   public ItemStack(ItemLike itemlike, int i) {
      this.item = itemlike.asItem();
      this.count = i;
      if (this.item.canBeDepleted()) {
         this.setDamageValue(this.getDamageValue());
      }

   }

   private ItemStack(@Nullable Void ovoid) {
      this.item = null;
   }

   private ItemStack(CompoundTag compoundtag) {
      this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(compoundtag.getString("id")));
      this.count = compoundtag.getByte("Count");
      if (compoundtag.contains("tag", 10)) {
         this.tag = compoundtag.getCompound("tag");
         this.getItem().verifyTagAfterLoad(this.tag);
      }

      if (this.getItem().canBeDepleted()) {
         this.setDamageValue(this.getDamageValue());
      }

   }

   public static ItemStack of(CompoundTag compoundtag) {
      try {
         return new ItemStack(compoundtag);
      } catch (RuntimeException var2) {
         LOGGER.debug("Tried to load invalid item: {}", compoundtag, var2);
         return EMPTY;
      }
   }

   public boolean isEmpty() {
      return this == EMPTY || this.item == Items.AIR || this.count <= 0;
   }

   public boolean isItemEnabled(FeatureFlagSet featureflagset) {
      return this.isEmpty() || this.getItem().isEnabled(featureflagset);
   }

   public ItemStack split(int i) {
      int j = Math.min(i, this.getCount());
      ItemStack itemstack = this.copyWithCount(j);
      this.shrink(j);
      return itemstack;
   }

   public ItemStack copyAndClear() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack itemstack = this.copy();
         this.setCount(0);
         return itemstack;
      }
   }

   public Item getItem() {
      return this.isEmpty() ? Items.AIR : this.item;
   }

   public Holder<Item> getItemHolder() {
      return this.getItem().builtInRegistryHolder();
   }

   public boolean is(TagKey<Item> tagkey) {
      return this.getItem().builtInRegistryHolder().is(tagkey);
   }

   public boolean is(Item item) {
      return this.getItem() == item;
   }

   public boolean is(Predicate<Holder<Item>> predicate) {
      return predicate.test(this.getItem().builtInRegistryHolder());
   }

   public boolean is(Holder<Item> holder) {
      return this.getItem().builtInRegistryHolder() == holder;
   }

   public Stream<TagKey<Item>> getTags() {
      return this.getItem().builtInRegistryHolder().tags();
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Player player = useoncontext.getPlayer();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockInWorld blockinworld = new BlockInWorld(useoncontext.getLevel(), blockpos, false);
      if (player != null && !player.getAbilities().mayBuild && !this.hasAdventureModePlaceTagForBlock(useoncontext.getLevel().registryAccess().registryOrThrow(Registries.BLOCK), blockinworld)) {
         return InteractionResult.PASS;
      } else {
         Item item = this.getItem();
         InteractionResult interactionresult = item.useOn(useoncontext);
         if (player != null && interactionresult.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
         }

         return interactionresult;
      }
   }

   public float getDestroySpeed(BlockState blockstate) {
      return this.getItem().getDestroySpeed(this, blockstate);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      return this.getItem().use(level, player, interactionhand);
   }

   public ItemStack finishUsingItem(Level level, LivingEntity livingentity) {
      return this.getItem().finishUsingItem(this, level, livingentity);
   }

   public CompoundTag save(CompoundTag compoundtag) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(this.getItem());
      compoundtag.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
      compoundtag.putByte("Count", (byte)this.count);
      if (this.tag != null) {
         compoundtag.put("tag", this.tag.copy());
      }

      return compoundtag;
   }

   public int getMaxStackSize() {
      return this.getItem().getMaxStackSize();
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
   }

   public boolean isDamageableItem() {
      if (!this.isEmpty() && this.getItem().getMaxDamage() > 0) {
         CompoundTag compoundtag = this.getTag();
         return compoundtag == null || !compoundtag.getBoolean("Unbreakable");
      } else {
         return false;
      }
   }

   public boolean isDamaged() {
      return this.isDamageableItem() && this.getDamageValue() > 0;
   }

   public int getDamageValue() {
      return this.tag == null ? 0 : this.tag.getInt("Damage");
   }

   public void setDamageValue(int i) {
      this.getOrCreateTag().putInt("Damage", Math.max(0, i));
   }

   public int getMaxDamage() {
      return this.getItem().getMaxDamage();
   }

   public boolean hurt(int i, RandomSource randomsource, @Nullable ServerPlayer serverplayer) {
      if (!this.isDamageableItem()) {
         return false;
      } else {
         if (i > 0) {
            int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
            int k = 0;

            for(int l = 0; j > 0 && l < i; ++l) {
               if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, j, randomsource)) {
                  ++k;
               }
            }

            i -= k;
            if (i <= 0) {
               return false;
            }
         }

         if (serverplayer != null && i != 0) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverplayer, this, this.getDamageValue() + i);
         }

         int i1 = this.getDamageValue() + i;
         this.setDamageValue(i1);
         return i1 >= this.getMaxDamage();
      }
   }

   public <T extends LivingEntity> void hurtAndBreak(int i, T livingentity, Consumer<T> consumer) {
      if (!livingentity.level().isClientSide && (!(livingentity instanceof Player) || !((Player)livingentity).getAbilities().instabuild)) {
         if (this.isDamageableItem()) {
            if (this.hurt(i, livingentity.getRandom(), livingentity instanceof ServerPlayer ? (ServerPlayer)livingentity : null)) {
               consumer.accept(livingentity);
               Item item = this.getItem();
               this.shrink(1);
               if (livingentity instanceof Player) {
                  ((Player)livingentity).awardStat(Stats.ITEM_BROKEN.get(item));
               }

               this.setDamageValue(0);
            }

         }
      }
   }

   public boolean isBarVisible() {
      return this.getItem().isBarVisible(this);
   }

   public int getBarWidth() {
      return this.getItem().getBarWidth(this);
   }

   public int getBarColor() {
      return this.getItem().getBarColor(this);
   }

   public boolean overrideStackedOnOther(Slot slot, ClickAction clickaction, Player player) {
      return this.getItem().overrideStackedOnOther(this, slot, clickaction, player);
   }

   public boolean overrideOtherStackedOnMe(ItemStack itemstack, Slot slot, ClickAction clickaction, Player player, SlotAccess slotaccess) {
      return this.getItem().overrideOtherStackedOnMe(this, itemstack, slot, clickaction, player, slotaccess);
   }

   public void hurtEnemy(LivingEntity livingentity, Player player) {
      Item item = this.getItem();
      if (item.hurtEnemy(this, livingentity, player)) {
         player.awardStat(Stats.ITEM_USED.get(item));
      }

   }

   public void mineBlock(Level level, BlockState blockstate, BlockPos blockpos, Player player) {
      Item item = this.getItem();
      if (item.mineBlock(this, level, blockstate, blockpos, player)) {
         player.awardStat(Stats.ITEM_USED.get(item));
      }

   }

   public boolean isCorrectToolForDrops(BlockState blockstate) {
      return this.getItem().isCorrectToolForDrops(blockstate);
   }

   public InteractionResult interactLivingEntity(Player player, LivingEntity livingentity, InteractionHand interactionhand) {
      return this.getItem().interactLivingEntity(this, player, livingentity, interactionhand);
   }

   public ItemStack copy() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack itemstack = new ItemStack(this.getItem(), this.count);
         itemstack.setPopTime(this.getPopTime());
         if (this.tag != null) {
            itemstack.tag = this.tag.copy();
         }

         return itemstack;
      }
   }

   public ItemStack copyWithCount(int i) {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack itemstack = this.copy();
         itemstack.setCount(i);
         return itemstack;
      }
   }

   public static boolean matches(ItemStack itemstack, ItemStack itemstack1) {
      if (itemstack == itemstack1) {
         return true;
      } else {
         return itemstack.getCount() != itemstack1.getCount() ? false : isSameItemSameTags(itemstack, itemstack1);
      }
   }

   public static boolean isSameItem(ItemStack itemstack, ItemStack itemstack1) {
      return itemstack.is(itemstack1.getItem());
   }

   public static boolean isSameItemSameTags(ItemStack itemstack, ItemStack itemstack1) {
      if (!itemstack.is(itemstack1.getItem())) {
         return false;
      } else {
         return itemstack.isEmpty() && itemstack1.isEmpty() ? true : Objects.equals(itemstack.tag, itemstack1.tag);
      }
   }

   public String getDescriptionId() {
      return this.getItem().getDescriptionId(this);
   }

   public String toString() {
      return this.getCount() + " " + this.getItem();
   }

   public void inventoryTick(Level level, Entity entity, int i, boolean flag) {
      if (this.popTime > 0) {
         --this.popTime;
      }

      if (this.getItem() != null) {
         this.getItem().inventoryTick(this, level, entity, i, flag);
      }

   }

   public void onCraftedBy(Level level, Player player, int i) {
      player.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), i);
      this.getItem().onCraftedBy(this, level, player);
   }

   public int getUseDuration() {
      return this.getItem().getUseDuration(this);
   }

   public UseAnim getUseAnimation() {
      return this.getItem().getUseAnimation(this);
   }

   public void releaseUsing(Level level, LivingEntity livingentity, int i) {
      this.getItem().releaseUsing(this, level, livingentity, i);
   }

   public boolean useOnRelease() {
      return this.getItem().useOnRelease(this);
   }

   public boolean hasTag() {
      return !this.isEmpty() && this.tag != null && !this.tag.isEmpty();
   }

   @Nullable
   public CompoundTag getTag() {
      return this.tag;
   }

   public CompoundTag getOrCreateTag() {
      if (this.tag == null) {
         this.setTag(new CompoundTag());
      }

      return this.tag;
   }

   public CompoundTag getOrCreateTagElement(String s) {
      if (this.tag != null && this.tag.contains(s, 10)) {
         return this.tag.getCompound(s);
      } else {
         CompoundTag compoundtag = new CompoundTag();
         this.addTagElement(s, compoundtag);
         return compoundtag;
      }
   }

   @Nullable
   public CompoundTag getTagElement(String s) {
      return this.tag != null && this.tag.contains(s, 10) ? this.tag.getCompound(s) : null;
   }

   public void removeTagKey(String s) {
      if (this.tag != null && this.tag.contains(s)) {
         this.tag.remove(s);
         if (this.tag.isEmpty()) {
            this.tag = null;
         }
      }

   }

   public ListTag getEnchantmentTags() {
      return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListTag();
   }

   public void setTag(@Nullable CompoundTag compoundtag) {
      this.tag = compoundtag;
      if (this.getItem().canBeDepleted()) {
         this.setDamageValue(this.getDamageValue());
      }

      if (compoundtag != null) {
         this.getItem().verifyTagAfterLoad(compoundtag);
      }

   }

   public Component getHoverName() {
      CompoundTag compoundtag = this.getTagElement("display");
      if (compoundtag != null && compoundtag.contains("Name", 8)) {
         try {
            Component component = Component.Serializer.fromJson(compoundtag.getString("Name"));
            if (component != null) {
               return component;
            }

            compoundtag.remove("Name");
         } catch (Exception var3) {
            compoundtag.remove("Name");
         }
      }

      return this.getItem().getName(this);
   }

   public ItemStack setHoverName(@Nullable Component component) {
      CompoundTag compoundtag = this.getOrCreateTagElement("display");
      if (component != null) {
         compoundtag.putString("Name", Component.Serializer.toJson(component));
      } else {
         compoundtag.remove("Name");
      }

      return this;
   }

   public void resetHoverName() {
      CompoundTag compoundtag = this.getTagElement("display");
      if (compoundtag != null) {
         compoundtag.remove("Name");
         if (compoundtag.isEmpty()) {
            this.removeTagKey("display");
         }
      }

      if (this.tag != null && this.tag.isEmpty()) {
         this.tag = null;
      }

   }

   public boolean hasCustomHoverName() {
      CompoundTag compoundtag = this.getTagElement("display");
      return compoundtag != null && compoundtag.contains("Name", 8);
   }

   public List<Component> getTooltipLines(@Nullable Player player, TooltipFlag tooltipflag) {
      List<Component> list = Lists.newArrayList();
      MutableComponent mutablecomponent = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color);
      if (this.hasCustomHoverName()) {
         mutablecomponent.withStyle(ChatFormatting.ITALIC);
      }

      list.add(mutablecomponent);
      if (!tooltipflag.isAdvanced() && !this.hasCustomHoverName() && this.is(Items.FILLED_MAP)) {
         Integer integer = MapItem.getMapId(this);
         if (integer != null) {
            list.add(Component.literal("#" + integer).withStyle(ChatFormatting.GRAY));
         }
      }

      int i = this.getHideFlags();
      if (shouldShowInTooltip(i, ItemStack.TooltipPart.ADDITIONAL)) {
         this.getItem().appendHoverText(this, player == null ? null : player.level(), list, tooltipflag);
      }

      if (this.hasTag()) {
         if (shouldShowInTooltip(i, ItemStack.TooltipPart.UPGRADES) && player != null) {
            ArmorTrim.appendUpgradeHoverText(this, player.level().registryAccess(), list);
         }

         if (shouldShowInTooltip(i, ItemStack.TooltipPart.ENCHANTMENTS)) {
            appendEnchantmentNames(list, this.getEnchantmentTags());
         }

         if (this.tag.contains("display", 10)) {
            CompoundTag compoundtag = this.tag.getCompound("display");
            if (shouldShowInTooltip(i, ItemStack.TooltipPart.DYE) && compoundtag.contains("color", 99)) {
               if (tooltipflag.isAdvanced()) {
                  list.add(Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", compoundtag.getInt("color"))).withStyle(ChatFormatting.GRAY));
               } else {
                  list.add(Component.translatable("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
               }
            }

            if (compoundtag.getTagType("Lore") == 9) {
               ListTag listtag = compoundtag.getList("Lore", 8);

               for(int j = 0; j < listtag.size(); ++j) {
                  String s = listtag.getString(j);

                  try {
                     MutableComponent mutablecomponent1 = Component.Serializer.fromJson(s);
                     if (mutablecomponent1 != null) {
                        list.add(ComponentUtils.mergeStyles(mutablecomponent1, LORE_STYLE));
                     }
                  } catch (Exception var19) {
                     compoundtag.remove("Lore");
                  }
               }
            }
         }
      }

      if (shouldShowInTooltip(i, ItemStack.TooltipPart.MODIFIERS)) {
         for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(equipmentslot);
            if (!multimap.isEmpty()) {
               list.add(CommonComponents.EMPTY);
               list.add(Component.translatable("item.modifiers." + equipmentslot.getName()).withStyle(ChatFormatting.GRAY));

               for(Map.Entry<Attribute, AttributeModifier> map_entry : multimap.entries()) {
                  AttributeModifier attributemodifier = map_entry.getValue();
                  double d0 = attributemodifier.getAmount();
                  boolean flag = false;
                  if (player != null) {
                     if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                        d0 += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        d0 += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                        flag = true;
                     } else if (attributemodifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                        d0 += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                        flag = true;
                     }
                  }

                  double d2;
                  if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                     if (map_entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                        d2 = d0 * 10.0D;
                     } else {
                        d2 = d0;
                     }
                  } else {
                     d2 = d0 * 100.0D;
                  }

                  if (flag) {
                     list.add(CommonComponents.space().append(Component.translatable("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(map_entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                  } else if (d0 > 0.0D) {
                     list.add(Component.translatable("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(map_entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                  } else if (d0 < 0.0D) {
                     d2 *= -1.0D;
                     list.add(Component.translatable("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2), Component.translatable(map_entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED));
                  }
               }
            }
         }
      }

      if (this.hasTag()) {
         if (shouldShowInTooltip(i, ItemStack.TooltipPart.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
            list.add(Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE));
         }

         if (shouldShowInTooltip(i, ItemStack.TooltipPart.CAN_DESTROY) && this.tag.contains("CanDestroy", 9)) {
            ListTag listtag1 = this.tag.getList("CanDestroy", 8);
            if (!listtag1.isEmpty()) {
               list.add(CommonComponents.EMPTY);
               list.add(Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY));

               for(int k = 0; k < listtag1.size(); ++k) {
                  list.addAll(expandBlockState(listtag1.getString(k)));
               }
            }
         }

         if (shouldShowInTooltip(i, ItemStack.TooltipPart.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9)) {
            ListTag listtag2 = this.tag.getList("CanPlaceOn", 8);
            if (!listtag2.isEmpty()) {
               list.add(CommonComponents.EMPTY);
               list.add(Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY));

               for(int l = 0; l < listtag2.size(); ++l) {
                  list.addAll(expandBlockState(listtag2.getString(l)));
               }
            }
         }
      }

      if (tooltipflag.isAdvanced()) {
         if (this.isDamaged()) {
            list.add(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
         }

         list.add(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
         if (this.hasTag()) {
            list.add(Component.translatable("item.nbt_tags", this.tag.getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
         }
      }

      if (player != null && !this.getItem().isEnabled(player.level().enabledFeatures())) {
         list.add(DISABLED_ITEM_TOOLTIP);
      }

      return list;
   }

   private static boolean shouldShowInTooltip(int i, ItemStack.TooltipPart itemstack_tooltippart) {
      return (i & itemstack_tooltippart.getMask()) == 0;
   }

   private int getHideFlags() {
      return this.hasTag() && this.tag.contains("HideFlags", 99) ? this.tag.getInt("HideFlags") : 0;
   }

   public void hideTooltipPart(ItemStack.TooltipPart itemstack_tooltippart) {
      CompoundTag compoundtag = this.getOrCreateTag();
      compoundtag.putInt("HideFlags", compoundtag.getInt("HideFlags") | itemstack_tooltippart.getMask());
   }

   public static void appendEnchantmentNames(List<Component> list, ListTag listtag) {
      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         BuiltInRegistries.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(compoundtag)).ifPresent((enchantment) -> list.add(enchantment.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundtag))));
      }

   }

   private static Collection<Component> expandBlockState(String s) {
      try {
         return BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), s, true).map((blockstateparser_blockresult) -> Lists.newArrayList(blockstateparser_blockresult.blockState().getBlock().getName().withStyle(ChatFormatting.DARK_GRAY)), (blockstateparser_tagresult) -> blockstateparser_tagresult.tag().stream().map((holder) -> holder.value().getName().withStyle(ChatFormatting.DARK_GRAY)).collect(Collectors.toList()));
      } catch (CommandSyntaxException var2) {
         return Lists.newArrayList(Component.literal("missingno").withStyle(ChatFormatting.DARK_GRAY));
      }
   }

   public boolean hasFoil() {
      return this.getItem().isFoil(this);
   }

   public Rarity getRarity() {
      return this.getItem().getRarity(this);
   }

   public boolean isEnchantable() {
      if (!this.getItem().isEnchantable(this)) {
         return false;
      } else {
         return !this.isEnchanted();
      }
   }

   public void enchant(Enchantment enchantment, int i) {
      this.getOrCreateTag();
      if (!this.tag.contains("Enchantments", 9)) {
         this.tag.put("Enchantments", new ListTag());
      }

      ListTag listtag = this.tag.getList("Enchantments", 10);
      listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), (byte)i));
   }

   public boolean isEnchanted() {
      if (this.tag != null && this.tag.contains("Enchantments", 9)) {
         return !this.tag.getList("Enchantments", 10).isEmpty();
      } else {
         return false;
      }
   }

   public void addTagElement(String s, Tag tag) {
      this.getOrCreateTag().put(s, tag);
   }

   public boolean isFramed() {
      return this.entityRepresentation instanceof ItemFrame;
   }

   public void setEntityRepresentation(@Nullable Entity entity) {
      this.entityRepresentation = entity;
   }

   @Nullable
   public ItemFrame getFrame() {
      return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
   }

   @Nullable
   public Entity getEntityRepresentation() {
      return !this.isEmpty() ? this.entityRepresentation : null;
   }

   public int getBaseRepairCost() {
      return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
   }

   public void setRepairCost(int i) {
      this.getOrCreateTag().putInt("RepairCost", i);
   }

   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentslot) {
      Multimap<Attribute, AttributeModifier> multimap;
      if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
         multimap = HashMultimap.create();
         ListTag listtag = this.tag.getList("AttributeModifiers", 10);

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            if (!compoundtag.contains("Slot", 8) || compoundtag.getString("Slot").equals(equipmentslot.getName())) {
               Optional<Attribute> optional = BuiltInRegistries.ATTRIBUTE.getOptional(ResourceLocation.tryParse(compoundtag.getString("AttributeName")));
               if (optional.isPresent()) {
                  AttributeModifier attributemodifier = AttributeModifier.load(compoundtag);
                  if (attributemodifier != null && attributemodifier.getId().getLeastSignificantBits() != 0L && attributemodifier.getId().getMostSignificantBits() != 0L) {
                     multimap.put(optional.get(), attributemodifier);
                  }
               }
            }
         }
      } else {
         multimap = this.getItem().getDefaultAttributeModifiers(equipmentslot);
      }

      return multimap;
   }

   public void addAttributeModifier(Attribute attribute, AttributeModifier attributemodifier, @Nullable EquipmentSlot equipmentslot) {
      this.getOrCreateTag();
      if (!this.tag.contains("AttributeModifiers", 9)) {
         this.tag.put("AttributeModifiers", new ListTag());
      }

      ListTag listtag = this.tag.getList("AttributeModifiers", 10);
      CompoundTag compoundtag = attributemodifier.save();
      compoundtag.putString("AttributeName", BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString());
      if (equipmentslot != null) {
         compoundtag.putString("Slot", equipmentslot.getName());
      }

      listtag.add(compoundtag);
   }

   public Component getDisplayName() {
      MutableComponent mutablecomponent = Component.empty().append(this.getHoverName());
      if (this.hasCustomHoverName()) {
         mutablecomponent.withStyle(ChatFormatting.ITALIC);
      }

      MutableComponent mutablecomponent1 = ComponentUtils.wrapInSquareBrackets(mutablecomponent);
      if (!this.isEmpty()) {
         mutablecomponent1.withStyle(this.getRarity().color).withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
      }

      return mutablecomponent1;
   }

   public boolean hasAdventureModePlaceTagForBlock(Registry<Block> registry, BlockInWorld blockinworld) {
      if (this.adventurePlaceCheck == null) {
         this.adventurePlaceCheck = new AdventureModeCheck("CanPlaceOn");
      }

      return this.adventurePlaceCheck.test(this, registry, blockinworld);
   }

   public boolean hasAdventureModeBreakTagForBlock(Registry<Block> registry, BlockInWorld blockinworld) {
      if (this.adventureBreakCheck == null) {
         this.adventureBreakCheck = new AdventureModeCheck("CanDestroy");
      }

      return this.adventureBreakCheck.test(this, registry, blockinworld);
   }

   public int getPopTime() {
      return this.popTime;
   }

   public void setPopTime(int i) {
      this.popTime = i;
   }

   public int getCount() {
      return this.isEmpty() ? 0 : this.count;
   }

   public void setCount(int i) {
      this.count = i;
   }

   public void grow(int i) {
      this.setCount(this.getCount() + i);
   }

   public void shrink(int i) {
      this.grow(-i);
   }

   public void onUseTick(Level level, LivingEntity livingentity, int i) {
      this.getItem().onUseTick(level, livingentity, this, i);
   }

   public void onDestroyed(ItemEntity itementity) {
      this.getItem().onDestroyed(itementity);
   }

   public boolean isEdible() {
      return this.getItem().isEdible();
   }

   public SoundEvent getDrinkingSound() {
      return this.getItem().getDrinkingSound();
   }

   public SoundEvent getEatingSound() {
      return this.getItem().getEatingSound();
   }

   public static enum TooltipPart {
      ENCHANTMENTS,
      MODIFIERS,
      UNBREAKABLE,
      CAN_DESTROY,
      CAN_PLACE,
      ADDITIONAL,
      DYE,
      UPGRADES;

      private final int mask = 1 << this.ordinal();

      public int getMask() {
         return this.mask;
      }
   }
}
