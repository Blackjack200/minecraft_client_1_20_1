package net.minecraft.world.level.timers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public class TimerCallbacks<C> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final TimerCallbacks<MinecraftServer> SERVER_CALLBACKS = (new TimerCallbacks<MinecraftServer>()).register(new FunctionCallback.Serializer()).register(new FunctionTagCallback.Serializer());
   private final Map<ResourceLocation, TimerCallback.Serializer<C, ?>> idToSerializer = Maps.newHashMap();
   private final Map<Class<?>, TimerCallback.Serializer<C, ?>> classToSerializer = Maps.newHashMap();

   public TimerCallbacks<C> register(TimerCallback.Serializer<C, ?> timercallback_serializer) {
      this.idToSerializer.put(timercallback_serializer.getId(), timercallback_serializer);
      this.classToSerializer.put(timercallback_serializer.getCls(), timercallback_serializer);
      return this;
   }

   private <T extends TimerCallback<C>> TimerCallback.Serializer<C, T> getSerializer(Class<?> oclass) {
      return this.classToSerializer.get(oclass);
   }

   public <T extends TimerCallback<C>> CompoundTag serialize(T timercallback) {
      TimerCallback.Serializer<C, T> timercallback_serializer = this.getSerializer(timercallback.getClass());
      CompoundTag compoundtag = new CompoundTag();
      timercallback_serializer.serialize(compoundtag, timercallback);
      compoundtag.putString("Type", timercallback_serializer.getId().toString());
      return compoundtag;
   }

   @Nullable
   public TimerCallback<C> deserialize(CompoundTag compoundtag) {
      ResourceLocation resourcelocation = ResourceLocation.tryParse(compoundtag.getString("Type"));
      TimerCallback.Serializer<C, ?> timercallback_serializer = this.idToSerializer.get(resourcelocation);
      if (timercallback_serializer == null) {
         LOGGER.error("Failed to deserialize timer callback: {}", (Object)compoundtag);
         return null;
      } else {
         try {
            return timercallback_serializer.deserialize(compoundtag);
         } catch (Exception var5) {
            LOGGER.error("Failed to deserialize timer callback: {}", compoundtag, var5);
            return null;
         }
      }
   }
}
