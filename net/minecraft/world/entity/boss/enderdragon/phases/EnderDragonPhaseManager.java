package net.minecraft.world.entity.boss.enderdragon.phases;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;

public class EnderDragonPhaseManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final EnderDragon dragon;
   private final DragonPhaseInstance[] phases = new DragonPhaseInstance[EnderDragonPhase.getCount()];
   @Nullable
   private DragonPhaseInstance currentPhase;

   public EnderDragonPhaseManager(EnderDragon enderdragon) {
      this.dragon = enderdragon;
      this.setPhase(EnderDragonPhase.HOVERING);
   }

   public void setPhase(EnderDragonPhase<?> enderdragonphase) {
      if (this.currentPhase == null || enderdragonphase != this.currentPhase.getPhase()) {
         if (this.currentPhase != null) {
            this.currentPhase.end();
         }

         this.currentPhase = this.getPhase(enderdragonphase);
         if (!this.dragon.level().isClientSide) {
            this.dragon.getEntityData().set(EnderDragon.DATA_PHASE, enderdragonphase.getId());
         }

         LOGGER.debug("Dragon is now in phase {} on the {}", enderdragonphase, this.dragon.level().isClientSide ? "client" : "server");
         this.currentPhase.begin();
      }
   }

   public DragonPhaseInstance getCurrentPhase() {
      return this.currentPhase;
   }

   public <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> enderdragonphase) {
      int i = enderdragonphase.getId();
      if (this.phases[i] == null) {
         this.phases[i] = enderdragonphase.createInstance(this.dragon);
      }

      return (T)this.phases[i];
   }
}
