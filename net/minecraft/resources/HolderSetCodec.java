package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<Holder<E>> elementCodec;
   private final Codec<List<Holder<E>>> homogenousListCodec;
   private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

   private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> codec, boolean flag) {
      Codec<List<Holder<E>>> codec1 = ExtraCodecs.validate(codec.listOf(), ExtraCodecs.ensureHomogenous(Holder::kind));
      return flag ? codec1 : Codec.either(codec1, codec).xmap((either) -> either.map((list1) -> list1, List::of), (list) -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list));
   }

   public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> resourcekey, Codec<Holder<E>> codec, boolean flag) {
      return new HolderSetCodec<>(resourcekey, codec, flag);
   }

   private HolderSetCodec(ResourceKey<? extends Registry<E>> resourcekey, Codec<Holder<E>> codec, boolean flag) {
      this.registryKey = resourcekey;
      this.elementCodec = codec;
      this.homogenousListCodec = homogenousList(codec, flag);
      this.registryAwareCodec = Codec.either(TagKey.hashedCodec(resourcekey), this.homogenousListCodec);
   }

   public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<T> registryops) {
         Optional<HolderGetter<E>> optional = registryops.getter(this.registryKey);
         if (optional.isPresent()) {
            HolderGetter<E> holdergetter = optional.get();
            return this.registryAwareCodec.decode(dynamicops, object).map((pair) -> pair.mapFirst((either) -> either.map(holdergetter::getOrThrow, HolderSet::direct)));
         }
      }

      return this.decodeWithoutRegistry(dynamicops, object);
   }

   public <T> DataResult<T> encode(HolderSet<E> holderset, DynamicOps<T> dynamicops, T object) {
      if (dynamicops instanceof RegistryOps<T> registryops) {
         Optional<HolderOwner<E>> optional = registryops.owner(this.registryKey);
         if (optional.isPresent()) {
            if (!holderset.canSerializeIn(optional.get())) {
               return DataResult.error(() -> "HolderSet " + holderset + " is not valid in current registry set");
            }

            return this.registryAwareCodec.encode(holderset.unwrap().mapRight(List::copyOf), dynamicops, object);
         }
      }

      return this.encodeWithoutRegistry(holderset, dynamicops, object);
   }

   private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> dynamicops, T object) {
      return this.elementCodec.listOf().decode(dynamicops, object).flatMap((pair) -> {
         List<Holder.Direct<E>> list = new ArrayList<>();

         for(Holder<E> holder : pair.getFirst()) {
            if (!(holder instanceof Holder.Direct)) {
               return DataResult.error(() -> "Can't decode element " + holder + " without registry");
            }

            Holder.Direct<E> holder_direct = (Holder.Direct)holder;
            list.add(holder_direct);
         }

         return DataResult.success(new Pair<>(HolderSet.direct(list), pair.getSecond()));
      });
   }

   private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> holderset, DynamicOps<T> dynamicops, T object) {
      return this.homogenousListCodec.encode(holderset.stream().toList(), dynamicops, object);
   }
}
