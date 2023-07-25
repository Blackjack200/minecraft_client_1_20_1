package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<>();
   private static final int MAX_ID_VALUE = 254;
   private final Entity entity;
   private final Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById = new Int2ObjectOpenHashMap<>();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private boolean isDirty;

   public SynchedEntityData(Entity entity) {
      this.entity = entity;
   }

   public static <T> EntityDataAccessor<T> defineId(Class<? extends Entity> oclass, EntityDataSerializer<T> entitydataserializer) {
      if (LOGGER.isDebugEnabled()) {
         try {
            Class<?> oclass1 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (!oclass1.equals(oclass)) {
               LOGGER.debug("defineId called for: {} from {}", oclass, oclass1, new RuntimeException());
            }
         } catch (ClassNotFoundException var5) {
         }
      }

      int i;
      if (ENTITY_ID_POOL.containsKey(oclass)) {
         i = ENTITY_ID_POOL.getInt(oclass) + 1;
      } else {
         int j = 0;
         Class<?> oclass2 = oclass;

         while(oclass2 != Entity.class) {
            oclass2 = oclass2.getSuperclass();
            if (ENTITY_ID_POOL.containsKey(oclass2)) {
               j = ENTITY_ID_POOL.getInt(oclass2) + 1;
               break;
            }
         }

         i = j;
      }

      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
      } else {
         ENTITY_ID_POOL.put(oclass, i);
         return entitydataserializer.createAccessor(i);
      }
   }

   public <T> void define(EntityDataAccessor<T> entitydataaccessor, T object) {
      int i = entitydataaccessor.getId();
      if (i > 254) {
         throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
      } else if (this.itemsById.containsKey(i)) {
         throw new IllegalArgumentException("Duplicate id value for " + i + "!");
      } else if (EntityDataSerializers.getSerializedId(entitydataaccessor.getSerializer()) < 0) {
         throw new IllegalArgumentException("Unregistered serializer " + entitydataaccessor.getSerializer() + " for " + i + "!");
      } else {
         this.createDataItem(entitydataaccessor, object);
      }
   }

   private <T> void createDataItem(EntityDataAccessor<T> entitydataaccessor, T object) {
      SynchedEntityData.DataItem<T> synchedentitydata_dataitem = new SynchedEntityData.DataItem<>(entitydataaccessor, object);
      this.lock.writeLock().lock();
      this.itemsById.put(entitydataaccessor.getId(), synchedentitydata_dataitem);
      this.lock.writeLock().unlock();
   }

   public <T> boolean hasItem(EntityDataAccessor<T> entitydataaccessor) {
      return this.itemsById.containsKey(entitydataaccessor.getId());
   }

   private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> entitydataaccessor) {
      this.lock.readLock().lock();

      SynchedEntityData.DataItem<T> synchedentitydata_dataitem;
      try {
         synchedentitydata_dataitem = this.itemsById.get(entitydataaccessor.getId());
      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.forThrowable(var9, "Getting synched entity data");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Synched entity data");
         crashreportcategory.setDetail("Data ID", entitydataaccessor);
         throw new ReportedException(crashreport);
      } finally {
         this.lock.readLock().unlock();
      }

      return synchedentitydata_dataitem;
   }

   public <T> T get(EntityDataAccessor<T> entitydataaccessor) {
      return this.getItem(entitydataaccessor).getValue();
   }

   public <T> void set(EntityDataAccessor<T> entitydataaccessor, T object) {
      this.set(entitydataaccessor, object, false);
   }

   public <T> void set(EntityDataAccessor<T> entitydataaccessor, T object, boolean flag) {
      SynchedEntityData.DataItem<T> synchedentitydata_dataitem = this.getItem(entitydataaccessor);
      if (flag || ObjectUtils.notEqual(object, synchedentitydata_dataitem.getValue())) {
         synchedentitydata_dataitem.setValue(object);
         this.entity.onSyncedDataUpdated(entitydataaccessor);
         synchedentitydata_dataitem.setDirty(true);
         this.isDirty = true;
      }

   }

   public boolean isDirty() {
      return this.isDirty;
   }

   @Nullable
   public List<SynchedEntityData.DataValue<?>> packDirty() {
      List<SynchedEntityData.DataValue<?>> list = null;
      if (this.isDirty) {
         this.lock.readLock().lock();

         for(SynchedEntityData.DataItem<?> synchedentitydata_dataitem : this.itemsById.values()) {
            if (synchedentitydata_dataitem.isDirty()) {
               synchedentitydata_dataitem.setDirty(false);
               if (list == null) {
                  list = new ArrayList<>();
               }

               list.add(synchedentitydata_dataitem.value());
            }
         }

         this.lock.readLock().unlock();
      }

      this.isDirty = false;
      return list;
   }

   @Nullable
   public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
      List<SynchedEntityData.DataValue<?>> list = null;
      this.lock.readLock().lock();

      for(SynchedEntityData.DataItem<?> synchedentitydata_dataitem : this.itemsById.values()) {
         if (!synchedentitydata_dataitem.isSetToDefault()) {
            if (list == null) {
               list = new ArrayList<>();
            }

            list.add(synchedentitydata_dataitem.value());
         }
      }

      this.lock.readLock().unlock();
      return list;
   }

   public void assignValues(List<SynchedEntityData.DataValue<?>> list) {
      this.lock.writeLock().lock();

      try {
         for(SynchedEntityData.DataValue<?> synchedentitydata_datavalue : list) {
            SynchedEntityData.DataItem<?> synchedentitydata_dataitem = this.itemsById.get(synchedentitydata_datavalue.id);
            if (synchedentitydata_dataitem != null) {
               this.assignValue(synchedentitydata_dataitem, synchedentitydata_datavalue);
               this.entity.onSyncedDataUpdated(synchedentitydata_dataitem.getAccessor());
            }
         }
      } finally {
         this.lock.writeLock().unlock();
      }

      this.entity.onSyncedDataUpdated(list);
   }

   private <T> void assignValue(SynchedEntityData.DataItem<T> synchedentitydata_dataitem, SynchedEntityData.DataValue<?> synchedentitydata_datavalue) {
      if (!Objects.equals(synchedentitydata_datavalue.serializer(), synchedentitydata_dataitem.accessor.getSerializer())) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", synchedentitydata_dataitem.accessor.getId(), this.entity, synchedentitydata_dataitem.value, synchedentitydata_dataitem.value.getClass(), synchedentitydata_datavalue.value, synchedentitydata_datavalue.value.getClass()));
      } else {
         synchedentitydata_dataitem.setValue(synchedentitydata_datavalue.value);
      }
   }

   public boolean isEmpty() {
      return this.itemsById.isEmpty();
   }

   public static class DataItem<T> {
      final EntityDataAccessor<T> accessor;
      T value;
      private final T initialValue;
      private boolean dirty;

      public DataItem(EntityDataAccessor<T> entitydataaccessor, T object) {
         this.accessor = entitydataaccessor;
         this.initialValue = object;
         this.value = object;
      }

      public EntityDataAccessor<T> getAccessor() {
         return this.accessor;
      }

      public void setValue(T object) {
         this.value = object;
      }

      public T getValue() {
         return this.value;
      }

      public boolean isDirty() {
         return this.dirty;
      }

      public void setDirty(boolean flag) {
         this.dirty = flag;
      }

      public boolean isSetToDefault() {
         return this.initialValue.equals(this.value);
      }

      public SynchedEntityData.DataValue<T> value() {
         return SynchedEntityData.DataValue.create(this.accessor, this.value);
      }
   }

   public static record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {
      final int id;
      final T value;

      public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> entitydataaccessor, T object) {
         EntityDataSerializer<T> entitydataserializer = entitydataaccessor.getSerializer();
         return new SynchedEntityData.DataValue<>(entitydataaccessor.getId(), entitydataserializer, entitydataserializer.copy(object));
      }

      public void write(FriendlyByteBuf friendlybytebuf) {
         int i = EntityDataSerializers.getSerializedId(this.serializer);
         if (i < 0) {
            throw new EncoderException("Unknown serializer type " + this.serializer);
         } else {
            friendlybytebuf.writeByte(this.id);
            friendlybytebuf.writeVarInt(i);
            this.serializer.write(friendlybytebuf, this.value);
         }
      }

      public static SynchedEntityData.DataValue<?> read(FriendlyByteBuf friendlybytebuf, int i) {
         int j = friendlybytebuf.readVarInt();
         EntityDataSerializer<?> entitydataserializer = EntityDataSerializers.getSerializer(j);
         if (entitydataserializer == null) {
            throw new DecoderException("Unknown serializer type " + j);
         } else {
            return read(friendlybytebuf, i, entitydataserializer);
         }
      }

      private static <T> SynchedEntityData.DataValue<T> read(FriendlyByteBuf friendlybytebuf, int i, EntityDataSerializer<T> entitydataserializer) {
         return new SynchedEntityData.DataValue<>(i, entitydataserializer, entitydataserializer.read(friendlybytebuf));
      }
   }
}
