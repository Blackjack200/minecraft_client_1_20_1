package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> extends HolderGetter<T> {
   Stream<Holder.Reference<T>> listElements();

   default Stream<ResourceKey<T>> listElementIds() {
      return this.listElements().map(Holder.Reference::key);
   }

   Stream<HolderSet.Named<T>> listTags();

   default Stream<TagKey<T>> listTagIds() {
      return this.listTags().map(HolderSet.Named::key);
   }

   default HolderLookup<T> filterElements(final Predicate<T> predicate) {
      return new HolderLookup.Delegate<T>(this) {
         public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
            return this.parent.get(resourcekey).filter((holder_reference) -> predicate.test(holder_reference.value()));
         }

         public Stream<Holder.Reference<T>> listElements() {
            return this.parent.listElements().filter((holder_reference) -> predicate.test(holder_reference.value()));
         }
      };
   }

   public static class Delegate<T> implements HolderLookup<T> {
      protected final HolderLookup<T> parent;

      public Delegate(HolderLookup<T> holderlookup) {
         this.parent = holderlookup;
      }

      public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
         return this.parent.get(resourcekey);
      }

      public Stream<Holder.Reference<T>> listElements() {
         return this.parent.listElements();
      }

      public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
         return this.parent.get(tagkey);
      }

      public Stream<HolderSet.Named<T>> listTags() {
         return this.parent.listTags();
      }
   }

   public interface Provider {
      <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey);

      default <T> HolderLookup.RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourcekey) {
         return this.<T>lookup(resourcekey).orElseThrow(() -> new IllegalStateException("Registry " + resourcekey.location() + " not found"));
      }

      default HolderGetter.Provider asGetterLookup() {
         return new HolderGetter.Provider() {
            public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
               return Provider.this.lookup(resourcekey).map((holderlookup_registrylookup) -> holderlookup_registrylookup);
            }
         };
      }

      static HolderLookup.Provider create(Stream<HolderLookup.RegistryLookup<?>> stream) {
         final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> map = stream.collect(Collectors.toUnmodifiableMap(HolderLookup.RegistryLookup::key, (holderlookup_registrylookup) -> holderlookup_registrylookup));
         return new HolderLookup.Provider() {
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourcekey) {
               return Optional.ofNullable(map.get(resourcekey));
            }
         };
      }
   }

   public interface RegistryLookup<T> extends HolderLookup<T>, HolderOwner<T> {
      ResourceKey<? extends Registry<? extends T>> key();

      Lifecycle registryLifecycle();

      default HolderLookup<T> filterFeatures(FeatureFlagSet featureflagset) {
         return (HolderLookup<T>)(FeatureElement.FILTERED_REGISTRIES.contains(this.key()) ? this.filterElements((object) -> ((FeatureElement)object).isEnabled(featureflagset)) : this);
      }

      public abstract static class Delegate<T> implements HolderLookup.RegistryLookup<T> {
         protected abstract HolderLookup.RegistryLookup<T> parent();

         public ResourceKey<? extends Registry<? extends T>> key() {
            return this.parent().key();
         }

         public Lifecycle registryLifecycle() {
            return this.parent().registryLifecycle();
         }

         public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
            return this.parent().get(resourcekey);
         }

         public Stream<Holder.Reference<T>> listElements() {
            return this.parent().listElements();
         }

         public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
            return this.parent().get(tagkey);
         }

         public Stream<HolderSet.Named<T>> listTags() {
            return this.parent().listTags();
         }
      }
   }
}
