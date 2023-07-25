package net.minecraft.network.syncher;

public class EntityDataAccessor<T> {
   private final int id;
   private final EntityDataSerializer<T> serializer;

   public EntityDataAccessor(int i, EntityDataSerializer<T> entitydataserializer) {
      this.id = i;
      this.serializer = entitydataserializer;
   }

   public int getId() {
      return this.id;
   }

   public EntityDataSerializer<T> getSerializer() {
      return this.serializer;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         EntityDataAccessor<?> entitydataaccessor = (EntityDataAccessor)object;
         return this.id == entitydataaccessor.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.id;
   }

   public String toString() {
      return "<entity data: " + this.id + ">";
   }
}
