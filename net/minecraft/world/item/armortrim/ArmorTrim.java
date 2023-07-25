package net.minecraft.world.item.armortrim;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class ArmorTrim {
   public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material), TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern)).apply(recordcodecbuilder_instance, ArmorTrim::new));
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_TRIM_ID = "Trim";
   private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("smithing_template.upgrade"))).withStyle(ChatFormatting.GRAY);
   private final Holder<TrimMaterial> material;
   private final Holder<TrimPattern> pattern;
   private final Function<ArmorMaterial, ResourceLocation> innerTexture;
   private final Function<ArmorMaterial, ResourceLocation> outerTexture;

   public ArmorTrim(Holder<TrimMaterial> holder, Holder<TrimPattern> holder1) {
      this.material = holder;
      this.pattern = holder1;
      this.innerTexture = Util.memoize((armormaterial1) -> {
         ResourceLocation resourcelocation1 = holder1.value().assetId();
         String s3 = this.getColorPaletteSuffix(armormaterial1);
         return resourcelocation1.withPath((s5) -> "trims/models/armor/" + s5 + "_leggings_" + s3);
      });
      this.outerTexture = Util.memoize((armormaterial) -> {
         ResourceLocation resourcelocation = holder1.value().assetId();
         String s = this.getColorPaletteSuffix(armormaterial);
         return resourcelocation.withPath((s2) -> "trims/models/armor/" + s2 + "_" + s);
      });
   }

   private String getColorPaletteSuffix(ArmorMaterial armormaterial) {
      Map<ArmorMaterials, String> map = this.material.value().overrideArmorMaterials();
      return armormaterial instanceof ArmorMaterials && map.containsKey(armormaterial) ? map.get(armormaterial) : this.material.value().assetName();
   }

   public boolean hasPatternAndMaterial(Holder<TrimPattern> holder, Holder<TrimMaterial> holder1) {
      return holder == this.pattern && holder1 == this.material;
   }

   public Holder<TrimPattern> pattern() {
      return this.pattern;
   }

   public Holder<TrimMaterial> material() {
      return this.material;
   }

   public ResourceLocation innerTexture(ArmorMaterial armormaterial) {
      return this.innerTexture.apply(armormaterial);
   }

   public ResourceLocation outerTexture(ArmorMaterial armormaterial) {
      return this.outerTexture.apply(armormaterial);
   }

   public boolean equals(Object object) {
      if (!(object instanceof ArmorTrim armortrim)) {
         return false;
      } else {
         return armortrim.pattern == this.pattern && armortrim.material == this.material;
      }
   }

   public static boolean setTrim(RegistryAccess registryaccess, ItemStack itemstack, ArmorTrim armortrim) {
      if (itemstack.is(ItemTags.TRIMMABLE_ARMOR)) {
         itemstack.getOrCreateTag().put("Trim", CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registryaccess), armortrim).result().orElseThrow());
         return true;
      } else {
         return false;
      }
   }

   public static Optional<ArmorTrim> getTrim(RegistryAccess registryaccess, ItemStack itemstack) {
      if (itemstack.is(ItemTags.TRIMMABLE_ARMOR) && itemstack.getTag() != null && itemstack.getTag().contains("Trim")) {
         CompoundTag compoundtag = itemstack.getTagElement("Trim");
         ArmorTrim armortrim = CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registryaccess), compoundtag).resultOrPartial(LOGGER::error).orElse((ArmorTrim)null);
         return Optional.ofNullable(armortrim);
      } else {
         return Optional.empty();
      }
   }

   public static void appendUpgradeHoverText(ItemStack itemstack, RegistryAccess registryaccess, List<Component> list) {
      Optional<ArmorTrim> optional = getTrim(registryaccess, itemstack);
      if (optional.isPresent()) {
         ArmorTrim armortrim = optional.get();
         list.add(UPGRADE_TITLE);
         list.add(CommonComponents.space().append(armortrim.pattern().value().copyWithStyle(armortrim.material())));
         list.add(CommonComponents.space().append(armortrim.material().value().description()));
      }

   }
}
