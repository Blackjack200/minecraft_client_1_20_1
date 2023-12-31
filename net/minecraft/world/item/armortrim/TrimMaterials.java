package net.minecraft.world.item.armortrim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimMaterials {
   public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
   public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
   public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
   public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
   public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
   public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
   public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
   public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
   public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
   public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");

   public static void bootstrap(BootstapContext<TrimMaterial> bootstapcontext) {
      register(bootstapcontext, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140), 0.1F);
      register(bootstapcontext, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), 0.2F, Map.of(ArmorMaterials.IRON, "iron_darker"));
      register(bootstapcontext, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), 0.3F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"));
      register(bootstapcontext, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575), 0.4F);
      register(bootstapcontext, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181), 0.5F);
      register(bootstapcontext, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), 0.6F, Map.of(ArmorMaterials.GOLD, "gold_darker"));
      register(bootstapcontext, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126), 0.7F);
      register(bootstapcontext, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), 0.8F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"));
      register(bootstapcontext, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151), 0.9F);
      register(bootstapcontext, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294), 1.0F);
   }

   public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(RegistryAccess registryaccess, ItemStack itemstack) {
      return registryaccess.registryOrThrow(Registries.TRIM_MATERIAL).holders().filter((holder_reference) -> itemstack.is(holder_reference.value().ingredient())).findFirst();
   }

   private static void register(BootstapContext<TrimMaterial> bootstapcontext, ResourceKey<TrimMaterial> resourcekey, Item item, Style style, float f) {
      register(bootstapcontext, resourcekey, item, style, f, Map.of());
   }

   private static void register(BootstapContext<TrimMaterial> bootstapcontext, ResourceKey<TrimMaterial> resourcekey, Item item, Style style, float f, Map<ArmorMaterials, String> map) {
      TrimMaterial trimmaterial = TrimMaterial.create(resourcekey.location().getPath(), item, f, Component.translatable(Util.makeDescriptionId("trim_material", resourcekey.location())).withStyle(style), map);
      bootstapcontext.register(resourcekey, trimmaterial);
   }

   private static ResourceKey<TrimMaterial> registryKey(String s) {
      return ResourceKey.create(Registries.TRIM_MATERIAL, new ResourceLocation(s));
   }
}
