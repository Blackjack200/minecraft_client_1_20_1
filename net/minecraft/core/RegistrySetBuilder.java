package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class RegistrySetBuilder {
   private final List<RegistrySetBuilder.RegistryStub<?>> entries = new ArrayList<>();

   static <T> HolderGetter<T> wrapContextLookup(final HolderLookup.RegistryLookup<T> holderlookup_registrylookup) {
      return new RegistrySetBuilder.EmptyTagLookup<T>(holderlookup_registrylookup) {
         public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
            return holderlookup_registrylookup.get(resourcekey);
         }
      };
   }

   public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourcekey, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> registrysetbuilder_registrybootstrap) {
      this.entries.add(new RegistrySetBuilder.RegistryStub<>(resourcekey, lifecycle, registrysetbuilder_registrybootstrap));
      return this;
   }

   public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourcekey, RegistrySetBuilder.RegistryBootstrap<T> registrysetbuilder_registrybootstrap) {
      return this.add(resourcekey, Lifecycle.stable(), registrysetbuilder_registrybootstrap);
   }

   private RegistrySetBuilder.BuildState createState(RegistryAccess registryaccess) {
      RegistrySetBuilder.BuildState registrysetbuilder_buildstate = RegistrySetBuilder.BuildState.create(registryaccess, this.entries.stream().map(RegistrySetBuilder.RegistryStub::key));
      this.entries.forEach((registrysetbuilder_registrystub) -> registrysetbuilder_registrystub.apply(registrysetbuilder_buildstate));
      return registrysetbuilder_buildstate;
   }

   public HolderLookup.Provider build(RegistryAccess registryaccess) {
      RegistrySetBuilder.BuildState registrysetbuilder_buildstate = this.createState(registryaccess);
      Stream<HolderLookup.RegistryLookup<?>> stream = registryaccess.registries().map((registryaccess_registryentry) -> registryaccess_registryentry.value().asLookup());
      Stream<HolderLookup.RegistryLookup<?>> stream1 = this.entries.stream().map((registrysetbuilder_registrystub) -> registrysetbuilder_registrystub.collectChanges(registrysetbuilder_buildstate).buildAsLookup());
      HolderLookup.Provider holderlookup_provider = HolderLookup.Provider.create(Stream.concat(stream, stream1.peek(registrysetbuilder_buildstate::addOwner)));
      registrysetbuilder_buildstate.reportRemainingUnreferencedValues();
      registrysetbuilder_buildstate.throwOnError();
      return holderlookup_provider;
   }

   public HolderLookup.Provider buildPatch(RegistryAccess registryaccess, HolderLookup.Provider holderlookup_provider) {
      RegistrySetBuilder.BuildState registrysetbuilder_buildstate = this.createState(registryaccess);
      Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> map = new HashMap<>();
      registrysetbuilder_buildstate.collectReferencedRegistries().forEach((registrysetbuilder_registrycontents1) -> map.put(registrysetbuilder_registrycontents1.key, registrysetbuilder_registrycontents1));
      this.entries.stream().map((registrysetbuilder_registrystub) -> registrysetbuilder_registrystub.collectChanges(registrysetbuilder_buildstate)).forEach((registrysetbuilder_registrycontents) -> map.put(registrysetbuilder_registrycontents.key, registrysetbuilder_registrycontents));
      Stream<HolderLookup.RegistryLookup<?>> stream = registryaccess.registries().map((registryaccess_registryentry) -> registryaccess_registryentry.value().asLookup());
      HolderLookup.Provider holderlookup_provider1 = HolderLookup.Provider.create(Stream.concat(stream, map.values().stream().map(RegistrySetBuilder.RegistryContents::buildAsLookup).peek(registrysetbuilder_buildstate::addOwner)));
      registrysetbuilder_buildstate.fillMissingHolders(holderlookup_provider);
      registrysetbuilder_buildstate.reportRemainingUnreferencedValues();
      registrysetbuilder_buildstate.throwOnError();
      return holderlookup_provider1;
   }

   static record BuildState(RegistrySetBuilder.CompositeOwner owner, RegistrySetBuilder.UniversalLookup lookup, Map<ResourceLocation, HolderGetter<?>> registries, Map<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> registeredValues, List<RuntimeException> errors) {
      final RegistrySetBuilder.UniversalLookup lookup;
      final Map<ResourceLocation, HolderGetter<?>> registries;
      final Map<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> registeredValues;
      final List<RuntimeException> errors;

      public static RegistrySetBuilder.BuildState create(RegistryAccess registryaccess, Stream<ResourceKey<? extends Registry<?>>> stream) {
         RegistrySetBuilder.CompositeOwner registrysetbuilder_compositeowner = new RegistrySetBuilder.CompositeOwner();
         List<RuntimeException> list = new ArrayList<>();
         RegistrySetBuilder.UniversalLookup registrysetbuilder_universallookup = new RegistrySetBuilder.UniversalLookup(registrysetbuilder_compositeowner);
         ImmutableMap.Builder<ResourceLocation, HolderGetter<?>> immutablemap_builder = ImmutableMap.builder();
         registryaccess.registries().forEach((registryaccess_registryentry) -> immutablemap_builder.put(registryaccess_registryentry.key().location(), RegistrySetBuilder.wrapContextLookup(registryaccess_registryentry.value().asLookup())));
         stream.forEach((resourcekey) -> immutablemap_builder.put(resourcekey.location(), registrysetbuilder_universallookup));
         return new RegistrySetBuilder.BuildState(registrysetbuilder_compositeowner, registrysetbuilder_universallookup, immutablemap_builder.build(), new HashMap<>(), list);
      }

      public <T> BootstapContext<T> bootstapContext() {
         return new BootstapContext<T>() {
            public Holder.Reference<T> register(ResourceKey<T> resourcekey, T object, Lifecycle lifecycle) {
               RegistrySetBuilder.RegisteredValue<?> registrysetbuilder_registeredvalue = BuildState.this.registeredValues.put(resourcekey, new RegistrySetBuilder.RegisteredValue(object, lifecycle));
               if (registrysetbuilder_registeredvalue != null) {
                  BuildState.this.errors.add(new IllegalStateException("Duplicate registration for " + resourcekey + ", new=" + object + ", old=" + registrysetbuilder_registeredvalue.value));
               }

               return BuildState.this.lookup.getOrCreate(resourcekey);
            }

            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourcekey) {
               return BuildState.this.registries.getOrDefault(resourcekey.location(), BuildState.this.lookup);
            }
         };
      }

      public void reportRemainingUnreferencedValues() {
         for(ResourceKey<Object> resourcekey : this.lookup.holders.keySet()) {
            this.errors.add(new IllegalStateException("Unreferenced key: " + resourcekey));
         }

         this.registeredValues.forEach((resourcekey1, registrysetbuilder_registeredvalue) -> this.errors.add(new IllegalStateException("Orpaned value " + registrysetbuilder_registeredvalue.value + " for key " + resourcekey1)));
      }

      public void throwOnError() {
         if (!this.errors.isEmpty()) {
            IllegalStateException illegalstateexception = new IllegalStateException("Errors during registry creation");

            for(RuntimeException runtimeexception : this.errors) {
               illegalstateexception.addSuppressed(runtimeexception);
            }

            throw illegalstateexception;
         }
      }

      public void addOwner(HolderOwner<?> holderowner) {
         this.owner.add(holderowner);
      }

      public void fillMissingHolders(HolderLookup.Provider holderlookup_provider) {
         Map<ResourceLocation, Optional<? extends HolderLookup<Object>>> map = new HashMap<>();
         Iterator<Map.Entry<ResourceKey<Object>, Holder.Reference<Object>>> iterator = this.lookup.holders.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceKey<Object>, Holder.Reference<Object>> map_entry = iterator.next();
            ResourceKey<Object> resourcekey = map_entry.getKey();
            Holder.Reference<Object> holder_reference = map_entry.getValue();
            map.computeIfAbsent(resourcekey.registry(), (resourcelocation) -> holderlookup_provider.lookup(ResourceKey.createRegistryKey(resourcelocation))).flatMap((holderlookup) -> holderlookup.get(resourcekey)).ifPresent((holder_reference2) -> {
               holder_reference.bindValue(holder_reference2.value());
               iterator.remove();
            });
         }

      }

      public Stream<RegistrySetBuilder.RegistryContents<?>> collectReferencedRegistries() {
         return this.lookup.holders.keySet().stream().map(ResourceKey::registry).distinct().map((resourcelocation) -> new RegistrySetBuilder.RegistryContents(ResourceKey.createRegistryKey(resourcelocation), Lifecycle.stable(), Map.of()));
      }
   }

   static class CompositeOwner implements HolderOwner<Object> {
      private final Set<HolderOwner<?>> owners = Sets.newIdentityHashSet();

      public boolean canSerializeIn(HolderOwner<Object> holderowner) {
         return this.owners.contains(holderowner);
      }

      public void add(HolderOwner<?> holderowner) {
         this.owners.add(holderowner);
      }
   }

   abstract static class EmptyTagLookup<T> implements HolderGetter<T> {
      protected final HolderOwner<T> owner;

      protected EmptyTagLookup(HolderOwner<T> holderowner) {
         this.owner = holderowner;
      }

      public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
         return Optional.of(HolderSet.emptyNamed(this.owner, tagkey));
      }
   }

   static record RegisteredValue<T>(T value, Lifecycle lifecycle) {
      final T value;
   }

   @FunctionalInterface
   public interface RegistryBootstrap<T> {
      void run(BootstapContext<T> bootstapcontext);
   }

   static record RegistryContents<T>(ResourceKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> values) {
      final ResourceKey<? extends Registry<? extends T>> key;
      final Lifecycle lifecycle;
      final Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> values;

      public HolderLookup.RegistryLookup<T> buildAsLookup() {
         return new HolderLookup.RegistryLookup<T>() {
            private final Map<ResourceKey<T>, Holder.Reference<T>> entries = RegistryContents.this.values.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, (map_entry) -> {
               RegistrySetBuilder.ValueAndHolder<T> registrysetbuilder_valueandholder = map_entry.getValue();
               Holder.Reference<T> holder_reference = registrysetbuilder_valueandholder.holder().orElseGet(() -> Holder.Reference.createStandAlone(this, map_entry.getKey()));
               holder_reference.bindValue(registrysetbuilder_valueandholder.value().value());
               return holder_reference;
            }));

            public ResourceKey<? extends Registry<? extends T>> key() {
               return RegistryContents.this.key;
            }

            public Lifecycle registryLifecycle() {
               return RegistryContents.this.lifecycle;
            }

            public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
               return Optional.ofNullable(this.entries.get(resourcekey));
            }

            public Stream<Holder.Reference<T>> listElements() {
               return this.entries.values().stream();
            }

            public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
               return Optional.empty();
            }

            public Stream<HolderSet.Named<T>> listTags() {
               return Stream.empty();
            }
         };
      }
   }

   static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
      void apply(RegistrySetBuilder.BuildState registrysetbuilder_buildstate) {
         this.bootstrap.run(registrysetbuilder_buildstate.bootstapContext());
      }

      public RegistrySetBuilder.RegistryContents<T> collectChanges(RegistrySetBuilder.BuildState registrysetbuilder_buildstate) {
         Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> map = new HashMap<>();
         Iterator<Map.Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>> iterator = registrysetbuilder_buildstate.registeredValues.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> map_entry = iterator.next();
            ResourceKey<?> resourcekey = map_entry.getKey();
            if (resourcekey.isFor(this.key)) {
               RegistrySetBuilder.RegisteredValue<T> registrysetbuilder_registeredvalue = map_entry.getValue();
               Holder.Reference<T> holder_reference = registrysetbuilder_buildstate.lookup.holders.remove(resourcekey);
               map.put(resourcekey, new RegistrySetBuilder.ValueAndHolder<>(registrysetbuilder_registeredvalue, Optional.ofNullable(holder_reference)));
               iterator.remove();
            }
         }

         return new RegistrySetBuilder.RegistryContents<>(this.key, this.lifecycle, map);
      }
   }

   static class UniversalLookup extends RegistrySetBuilder.EmptyTagLookup<Object> {
      final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<>();

      public UniversalLookup(HolderOwner<Object> holderowner) {
         super(holderowner);
      }

      public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourcekey) {
         return Optional.of(this.getOrCreate(resourcekey));
      }

      <T> Holder.Reference<T> getOrCreate(ResourceKey<T> resourcekey) {
         return this.holders.computeIfAbsent(resourcekey, (resourcekey1) -> Holder.Reference.createStandAlone(this.owner, resourcekey1));
      }
   }

   static record ValueAndHolder<T>(RegistrySetBuilder.RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
   }
}
