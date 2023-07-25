package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;

public class ItemCooldowns {
   private final Map<Item, ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
   private int tickCount;

   public boolean isOnCooldown(Item item) {
      return this.getCooldownPercent(item, 0.0F) > 0.0F;
   }

   public float getCooldownPercent(Item item, float f) {
      ItemCooldowns.CooldownInstance itemcooldowns_cooldowninstance = this.cooldowns.get(item);
      if (itemcooldowns_cooldowninstance != null) {
         float f1 = (float)(itemcooldowns_cooldowninstance.endTime - itemcooldowns_cooldowninstance.startTime);
         float f2 = (float)itemcooldowns_cooldowninstance.endTime - ((float)this.tickCount + f);
         return Mth.clamp(f2 / f1, 0.0F, 1.0F);
      } else {
         return 0.0F;
      }
   }

   public void tick() {
      ++this.tickCount;
      if (!this.cooldowns.isEmpty()) {
         Iterator<Map.Entry<Item, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<Item, ItemCooldowns.CooldownInstance> map_entry = iterator.next();
            if ((map_entry.getValue()).endTime <= this.tickCount) {
               iterator.remove();
               this.onCooldownEnded(map_entry.getKey());
            }
         }
      }

   }

   public void addCooldown(Item item, int i) {
      this.cooldowns.put(item, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + i));
      this.onCooldownStarted(item, i);
   }

   public void removeCooldown(Item item) {
      this.cooldowns.remove(item);
      this.onCooldownEnded(item);
   }

   protected void onCooldownStarted(Item item, int i) {
   }

   protected void onCooldownEnded(Item item) {
   }

   static class CooldownInstance {
      final int startTime;
      final int endTime;

      CooldownInstance(int i, int j) {
         this.startTime = i;
         this.endTime = j;
      }
   }
}
