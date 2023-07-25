package net.minecraft.network.syncher;

import java.util.Optional;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
   void write(FriendlyByteBuf friendlybytebuf, T object);

   T read(FriendlyByteBuf friendlybytebuf);

   default EntityDataAccessor<T> createAccessor(int i) {
      return new EntityDataAccessor<>(i, this);
   }

   T copy(T object);

   static <T> EntityDataSerializer<T> simple(final FriendlyByteBuf.Writer<T> friendlybytebuf_writer, final FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      return new EntityDataSerializer.ForValueType<T>() {
         public void write(FriendlyByteBuf friendlybytebuf, T object) {
            friendlybytebuf_writer.accept((T)friendlybytebuf, object);
         }

         public T read(FriendlyByteBuf friendlybytebuf) {
            return friendlybytebuf_reader.apply((T)friendlybytebuf);
         }
      };
   }

   static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> friendlybytebuf_writer, FriendlyByteBuf.Reader<T> friendlybytebuf_reader) {
      return simple(friendlybytebuf_writer.asOptional(), friendlybytebuf_reader.asOptional());
   }

   static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> oclass) {
      return simple(FriendlyByteBuf::writeEnum, (friendlybytebuf) -> friendlybytebuf.readEnum(oclass));
   }

   static <T> EntityDataSerializer<T> simpleId(IdMap<T> idmap) {
      return simple((friendlybytebuf1, object) -> friendlybytebuf1.writeId(idmap, (T)object), (friendlybytebuf) -> friendlybytebuf.<T>readById(idmap));
   }

   public interface ForValueType<T> extends EntityDataSerializer<T> {
      default T copy(T object) {
         return object;
      }
   }
}
