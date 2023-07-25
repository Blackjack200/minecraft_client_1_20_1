package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect {
   public InstantenousMobEffect(MobEffectCategory mobeffectcategory, int i) {
      super(mobeffectcategory, i);
   }

   public boolean isInstantenous() {
      return true;
   }

   public boolean isDurationEffectTick(int i, int j) {
      return i >= 1;
   }
}
