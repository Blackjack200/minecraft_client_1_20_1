package net.minecraft.world.level.chunk;

import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface PalettedContainerRO<T> {
   T get(int i, int j, int k);

   void getAll(Consumer<T> consumer);

   void write(FriendlyByteBuf friendlybytebuf);

   int getSerializedSize();

   boolean maybeHas(Predicate<T> predicate);

   void count(PalettedContainer.CountConsumer<T> palettedcontainer_countconsumer);

   PalettedContainer<T> recreate();

   PalettedContainerRO.PackedData<T> pack(IdMap<T> idmap, PalettedContainer.Strategy palettedcontainer_strategy);

   public static record PackedData<T>(List<T> paletteEntries, Optional<LongStream> storage) {
   }

   public interface Unpacker<T, C extends PalettedContainerRO<T>> {
      DataResult<C> read(IdMap<T> idmap, PalettedContainer.Strategy palettedcontainer_strategy, PalettedContainerRO.PackedData<T> palettedcontainerro_packeddata);
   }
}
