package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
   MobEffect getSuspiciousEffect();

   int getEffectDuration();

   static List<SuspiciousEffectHolder> getAllEffectHolders() {
      return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
   }

   @Nullable
   static SuspiciousEffectHolder tryGet(ItemLike itemlike) {
      Item var3 = itemlike.asItem();
      if (var3 instanceof BlockItem blockitem) {
         Block var6 = blockitem.getBlock();
         if (var6 instanceof SuspiciousEffectHolder suspiciouseffectholder) {
            return suspiciouseffectholder;
         }
      }

      Item suspiciouseffectholder = itemlike.asItem();
      if (suspiciouseffectholder instanceof SuspiciousEffectHolder suspiciouseffectholder1) {
         return suspiciouseffectholder1;
      } else {
         return null;
      }
   }
}
