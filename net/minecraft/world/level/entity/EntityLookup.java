package net.minecraft.world.level.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import org.slf4j.Logger;

public class EntityLookup<T extends EntityAccess> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Int2ObjectMap<T> byId = new Int2ObjectLinkedOpenHashMap<>();
   private final Map<UUID, T> byUuid = Maps.newHashMap();

   public <U extends T> void getEntities(EntityTypeTest<T, U> entitytypetest, AbortableIterationConsumer<U> abortableiterationconsumer) {
      for(T entityaccess : this.byId.values()) {
         U entityaccess1 = (U)((EntityAccess)entitytypetest.tryCast(entityaccess));
         if (entityaccess1 != null && abortableiterationconsumer.accept((T)entityaccess1).shouldAbort()) {
            return;
         }
      }

   }

   public Iterable<T> getAllEntities() {
      return Iterables.unmodifiableIterable(this.byId.values());
   }

   public void add(T entityaccess) {
      UUID uuid = entityaccess.getUUID();
      if (this.byUuid.containsKey(uuid)) {
         LOGGER.warn("Duplicate entity UUID {}: {}", uuid, entityaccess);
      } else {
         this.byUuid.put(uuid, entityaccess);
         this.byId.put(entityaccess.getId(), entityaccess);
      }
   }

   public void remove(T entityaccess) {
      this.byUuid.remove(entityaccess.getUUID());
      this.byId.remove(entityaccess.getId());
   }

   @Nullable
   public T getEntity(int i) {
      return this.byId.get(i);
   }

   @Nullable
   public T getEntity(UUID uuid) {
      return this.byUuid.get(uuid);
   }

   public int count() {
      return this.byUuid.size();
   }
}
