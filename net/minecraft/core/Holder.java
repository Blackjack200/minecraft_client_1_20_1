package net.minecraft.core;

import com.mojang.datafixers.util.Either;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface Holder<T> {
   T value();

   boolean isBound();

   boolean is(ResourceLocation resourcelocation);

   boolean is(ResourceKey<T> resourcekey);

   boolean is(Predicate<ResourceKey<T>> predicate);

   boolean is(TagKey<T> tagkey);

   Stream<TagKey<T>> tags();

   Either<ResourceKey<T>, T> unwrap();

   Optional<ResourceKey<T>> unwrapKey();

   Holder.Kind kind();

   boolean canSerializeIn(HolderOwner<T> holderowner);

   static <T> Holder<T> direct(T object) {
      return new Holder.Direct<>(object);
   }

   public static record Direct<T>(T value) implements Holder<T> {
      public boolean isBound() {
         return true;
      }

      public boolean is(ResourceLocation resourcelocation) {
         return false;
      }

      public boolean is(ResourceKey<T> resourcekey) {
         return false;
      }

      public boolean is(TagKey<T> tagkey) {
         return false;
      }

      public boolean is(Predicate<ResourceKey<T>> predicate) {
         return false;
      }

      public Either<ResourceKey<T>, T> unwrap() {
         return Either.right(this.value);
      }

      public Optional<ResourceKey<T>> unwrapKey() {
         return Optional.empty();
      }

      public Holder.Kind kind() {
         return Holder.Kind.DIRECT;
      }

      public String toString() {
         return "Direct{" + this.value + "}";
      }

      public boolean canSerializeIn(HolderOwner<T> holderowner) {
         return true;
      }

      public Stream<TagKey<T>> tags() {
         return Stream.of();
      }
   }

   public static enum Kind {
      REFERENCE,
      DIRECT;
   }

   public static class Reference<T> implements Holder<T> {
      private final HolderOwner<T> owner;
      private Set<TagKey<T>> tags = Set.of();
      private final Holder.Reference.Type type;
      @Nullable
      private ResourceKey<T> key;
      @Nullable
      private T value;

      private Reference(Holder.Reference.Type holder_reference_type, HolderOwner<T> holderowner, @Nullable ResourceKey<T> resourcekey, @Nullable T object) {
         this.owner = holderowner;
         this.type = holder_reference_type;
         this.key = resourcekey;
         this.value = object;
      }

      public static <T> Holder.Reference<T> createStandAlone(HolderOwner<T> holderowner, ResourceKey<T> resourcekey) {
         return new Holder.Reference<>(Holder.Reference.Type.STAND_ALONE, holderowner, resourcekey, (T)null);
      }

      /** @deprecated */
      @Deprecated
      public static <T> Holder.Reference<T> createIntrusive(HolderOwner<T> holderowner, @Nullable T object) {
         return new Holder.Reference<>(Holder.Reference.Type.INTRUSIVE, holderowner, (ResourceKey<T>)null, object);
      }

      public ResourceKey<T> key() {
         if (this.key == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.value + "' from registry " + this.owner);
         } else {
            return this.key;
         }
      }

      public T value() {
         if (this.value == null) {
            throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
         } else {
            return this.value;
         }
      }

      public boolean is(ResourceLocation resourcelocation) {
         return this.key().location().equals(resourcelocation);
      }

      public boolean is(ResourceKey<T> resourcekey) {
         return this.key() == resourcekey;
      }

      public boolean is(TagKey<T> tagkey) {
         return this.tags.contains(tagkey);
      }

      public boolean is(Predicate<ResourceKey<T>> predicate) {
         return predicate.test(this.key());
      }

      public boolean canSerializeIn(HolderOwner<T> holderowner) {
         return this.owner.canSerializeIn(holderowner);
      }

      public Either<ResourceKey<T>, T> unwrap() {
         return Either.left(this.key());
      }

      public Optional<ResourceKey<T>> unwrapKey() {
         return Optional.of(this.key());
      }

      public Holder.Kind kind() {
         return Holder.Kind.REFERENCE;
      }

      public boolean isBound() {
         return this.key != null && this.value != null;
      }

      void bindKey(ResourceKey<T> resourcekey) {
         if (this.key != null && resourcekey != this.key) {
            throw new IllegalStateException("Can't change holder key: existing=" + this.key + ", new=" + resourcekey);
         } else {
            this.key = resourcekey;
         }
      }

      void bindValue(T object) {
         if (this.type == Holder.Reference.Type.INTRUSIVE && this.value != object) {
            throw new IllegalStateException("Can't change holder " + this.key + " value: existing=" + this.value + ", new=" + object);
         } else {
            this.value = object;
         }
      }

      void bindTags(Collection<TagKey<T>> collection) {
         this.tags = Set.copyOf(collection);
      }

      public Stream<TagKey<T>> tags() {
         return this.tags.stream();
      }

      public String toString() {
         return "Reference{" + this.key + "=" + this.value + "}";
      }

      static enum Type {
         STAND_ALONE,
         INTRUSIVE;
      }
   }
}
