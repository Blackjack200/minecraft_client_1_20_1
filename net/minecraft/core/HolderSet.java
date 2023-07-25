package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public interface HolderSet<T> extends Iterable<Holder<T>> {
   Stream<Holder<T>> stream();

   int size();

   Either<TagKey<T>, List<Holder<T>>> unwrap();

   Optional<Holder<T>> getRandomElement(RandomSource randomsource);

   Holder<T> get(int i);

   boolean contains(Holder<T> holder);

   boolean canSerializeIn(HolderOwner<T> holderowner);

   Optional<TagKey<T>> unwrapKey();

   /** @deprecated */
   @Deprecated
   @VisibleForTesting
   static <T> HolderSet.Named<T> emptyNamed(HolderOwner<T> holderowner, TagKey<T> tagkey) {
      return new HolderSet.Named<>(holderowner, tagkey);
   }

   @SafeVarargs
   static <T> HolderSet.Direct<T> direct(Holder<T>... aholder) {
      return new HolderSet.Direct<>(List.of(aholder));
   }

   static <T> HolderSet.Direct<T> direct(List<? extends Holder<T>> list) {
      return new HolderSet.Direct<>(List.copyOf(list));
   }

   @SafeVarargs
   static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> function, E... aobject) {
      return direct(Stream.of(aobject).map(function).toList());
   }

   static <E, T> HolderSet.Direct<T> direct(Function<E, Holder<T>> function, List<E> list) {
      return direct(list.stream().map(function).toList());
   }

   public static class Direct<T> extends HolderSet.ListBacked<T> {
      private final List<Holder<T>> contents;
      private @Nullable Set<Holder<T>> contentsSet;

      Direct(List<Holder<T>> list) {
         this.contents = list;
      }

      protected List<Holder<T>> contents() {
         return this.contents;
      }

      public Either<TagKey<T>, List<Holder<T>>> unwrap() {
         return Either.right(this.contents);
      }

      public Optional<TagKey<T>> unwrapKey() {
         return Optional.empty();
      }

      public boolean contains(Holder<T> holder) {
         if (this.contentsSet == null) {
            this.contentsSet = Set.copyOf(this.contents);
         }

         return this.contentsSet.contains(holder);
      }

      public String toString() {
         return "DirectSet[" + this.contents + "]";
      }
   }

   public abstract static class ListBacked<T> implements HolderSet<T> {
      protected abstract List<Holder<T>> contents();

      public int size() {
         return this.contents().size();
      }

      public Spliterator<Holder<T>> spliterator() {
         return this.contents().spliterator();
      }

      public Iterator<Holder<T>> iterator() {
         return this.contents().iterator();
      }

      public Stream<Holder<T>> stream() {
         return this.contents().stream();
      }

      public Optional<Holder<T>> getRandomElement(RandomSource randomsource) {
         return Util.getRandomSafe(this.contents(), randomsource);
      }

      public Holder<T> get(int i) {
         return this.contents().get(i);
      }

      public boolean canSerializeIn(HolderOwner<T> holderowner) {
         return true;
      }
   }

   public static class Named<T> extends HolderSet.ListBacked<T> {
      private final HolderOwner<T> owner;
      private final TagKey<T> key;
      private List<Holder<T>> contents = List.of();

      Named(HolderOwner<T> holderowner, TagKey<T> tagkey) {
         this.owner = holderowner;
         this.key = tagkey;
      }

      void bind(List<Holder<T>> list) {
         this.contents = List.copyOf(list);
      }

      public TagKey<T> key() {
         return this.key;
      }

      protected List<Holder<T>> contents() {
         return this.contents;
      }

      public Either<TagKey<T>, List<Holder<T>>> unwrap() {
         return Either.left(this.key);
      }

      public Optional<TagKey<T>> unwrapKey() {
         return Optional.of(this.key);
      }

      public boolean contains(Holder<T> holder) {
         return holder.is(this.key);
      }

      public String toString() {
         return "NamedSet(" + this.key + ")[" + this.contents + "]";
      }

      public boolean canSerializeIn(HolderOwner<T> holderowner) {
         return this.owner.canSerializeIn(holderowner);
      }
   }
}
