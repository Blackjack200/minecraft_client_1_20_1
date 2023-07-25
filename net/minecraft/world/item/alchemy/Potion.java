package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public class Potion {
   @Nullable
   private final String name;
   private final ImmutableList<MobEffectInstance> effects;

   public static Potion byName(String s) {
      return BuiltInRegistries.POTION.get(ResourceLocation.tryParse(s));
   }

   public Potion(MobEffectInstance... amobeffectinstance) {
      this((String)null, amobeffectinstance);
   }

   public Potion(@Nullable String s, MobEffectInstance... amobeffectinstance) {
      this.name = s;
      this.effects = ImmutableList.copyOf(amobeffectinstance);
   }

   public String getName(String s) {
      return s + (this.name == null ? BuiltInRegistries.POTION.getKey(this).getPath() : this.name);
   }

   public List<MobEffectInstance> getEffects() {
      return this.effects;
   }

   public boolean hasInstantEffects() {
      if (!this.effects.isEmpty()) {
         for(MobEffectInstance mobeffectinstance : this.effects) {
            if (mobeffectinstance.getEffect().isInstantenous()) {
               return true;
            }
         }
      }

      return false;
   }
}
