package net.minecraft.server.bossevents;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CustomBossEvents {
   private final Map<ResourceLocation, CustomBossEvent> events = Maps.newHashMap();

   @Nullable
   public CustomBossEvent get(ResourceLocation resourcelocation) {
      return this.events.get(resourcelocation);
   }

   public CustomBossEvent create(ResourceLocation resourcelocation, Component component) {
      CustomBossEvent custombossevent = new CustomBossEvent(resourcelocation, component);
      this.events.put(resourcelocation, custombossevent);
      return custombossevent;
   }

   public void remove(CustomBossEvent custombossevent) {
      this.events.remove(custombossevent.getTextId());
   }

   public Collection<ResourceLocation> getIds() {
      return this.events.keySet();
   }

   public Collection<CustomBossEvent> getEvents() {
      return this.events.values();
   }

   public CompoundTag save() {
      CompoundTag compoundtag = new CompoundTag();

      for(CustomBossEvent custombossevent : this.events.values()) {
         compoundtag.put(custombossevent.getTextId().toString(), custombossevent.save());
      }

      return compoundtag;
   }

   public void load(CompoundTag compoundtag) {
      for(String s : compoundtag.getAllKeys()) {
         ResourceLocation resourcelocation = new ResourceLocation(s);
         this.events.put(resourcelocation, CustomBossEvent.load(compoundtag.getCompound(s), resourcelocation));
      }

   }

   public void onPlayerConnect(ServerPlayer serverplayer) {
      for(CustomBossEvent custombossevent : this.events.values()) {
         custombossevent.onPlayerConnect(serverplayer);
      }

   }

   public void onPlayerDisconnect(ServerPlayer serverplayer) {
      for(CustomBossEvent custombossevent : this.events.values()) {
         custombossevent.onPlayerDisconnect(serverplayer);
      }

   }
}
