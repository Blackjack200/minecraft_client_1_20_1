package net.minecraft.world.item.armortrim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
   public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
   public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
   public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
   public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
   public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
   public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
   public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
   public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
   public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
   public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
   public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
   public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
   public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
   public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
   public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
   public static final ResourceKey<TrimPattern> HOST = registryKey("host");

   public static void bootstrap(BootstapContext<TrimPattern> bootstapcontext) {
      register(bootstapcontext, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
      register(bootstapcontext, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
      register(bootstapcontext, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
      register(bootstapcontext, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
      register(bootstapcontext, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
      register(bootstapcontext, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
      register(bootstapcontext, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
      register(bootstapcontext, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
      register(bootstapcontext, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
      register(bootstapcontext, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
      register(bootstapcontext, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
      register(bootstapcontext, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
      register(bootstapcontext, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
      register(bootstapcontext, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
      register(bootstapcontext, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
      register(bootstapcontext, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
   }

   public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(RegistryAccess registryaccess, ItemStack itemstack) {
      return registryaccess.registryOrThrow(Registries.TRIM_PATTERN).holders().filter((holder_reference) -> itemstack.is(holder_reference.value().templateItem())).findFirst();
   }

   private static void register(BootstapContext<TrimPattern> bootstapcontext, Item item, ResourceKey<TrimPattern> resourcekey) {
      TrimPattern trimpattern = new TrimPattern(resourcekey.location(), BuiltInRegistries.ITEM.wrapAsHolder(item), Component.translatable(Util.makeDescriptionId("trim_pattern", resourcekey.location())));
      bootstapcontext.register(resourcekey, trimpattern);
   }

   private static ResourceKey<TrimPattern> registryKey(String s) {
      return ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(s));
   }
}
