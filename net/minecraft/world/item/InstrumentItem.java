package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {
   private static final String TAG_INSTRUMENT = "instrument";
   private final TagKey<Instrument> instruments;

   public InstrumentItem(Item.Properties item_properties, TagKey<Instrument> tagkey) {
      super(item_properties);
      this.instruments = tagkey;
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      Optional<ResourceKey<Instrument>> optional = this.getInstrument(itemstack).flatMap(Holder::unwrapKey);
      if (optional.isPresent()) {
         MutableComponent mutablecomponent = Component.translatable(Util.makeDescriptionId("instrument", optional.get().location()));
         list.add(mutablecomponent.withStyle(ChatFormatting.GRAY));
      }

   }

   public static ItemStack create(Item item, Holder<Instrument> holder) {
      ItemStack itemstack = new ItemStack(item);
      setSoundVariantId(itemstack, holder);
      return itemstack;
   }

   public static void setRandom(ItemStack itemstack, TagKey<Instrument> tagkey, RandomSource randomsource) {
      Optional<Holder<Instrument>> optional = BuiltInRegistries.INSTRUMENT.getTag(tagkey).flatMap((holderset_named) -> holderset_named.getRandomElement(randomsource));
      optional.ifPresent((holder) -> setSoundVariantId(itemstack, holder));
   }

   private static void setSoundVariantId(ItemStack itemstack, Holder<Instrument> holder) {
      CompoundTag compoundtag = itemstack.getOrCreateTag();
      compoundtag.putString("instrument", holder.unwrapKey().orElseThrow(() -> new IllegalStateException("Invalid instrument")).location().toString());
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack);
      if (optional.isPresent()) {
         Instrument instrument = optional.get().value();
         player.startUsingItem(interactionhand);
         play(level, player, instrument);
         player.getCooldowns().addCooldown(this, instrument.useDuration());
         player.awardStat(Stats.ITEM_USED.get(this));
         return InteractionResultHolder.consume(itemstack);
      } else {
         return InteractionResultHolder.fail(itemstack);
      }
   }

   public int getUseDuration(ItemStack itemstack) {
      Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack);
      return optional.map((holder) -> holder.value().useDuration()).orElse(0);
   }

   private Optional<? extends Holder<Instrument>> getInstrument(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("instrument", 8)) {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("instrument"));
         if (resourcelocation != null) {
            return BuiltInRegistries.INSTRUMENT.getHolder(ResourceKey.create(Registries.INSTRUMENT, resourcelocation));
         }
      }

      Iterator<Holder<Instrument>> iterator = BuiltInRegistries.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();
      return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.TOOT_HORN;
   }

   private static void play(Level level, Player player, Instrument instrument) {
      SoundEvent soundevent = instrument.soundEvent().value();
      float f = instrument.range() / 16.0F;
      level.playSound(player, player, soundevent, SoundSource.RECORDS, f, 1.0F);
      level.gameEvent(GameEvent.INSTRUMENT_PLAY, player.position(), GameEvent.Context.of(player));
   }
}
