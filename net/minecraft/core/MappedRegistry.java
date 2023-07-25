package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> implements WritableRegistry<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   final ResourceKey<? extends Registry<T>> key;
   private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
   private final Object2IntMap<T> toId = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), (object2intopencustomhashmap) -> object2intopencustomhashmap.defaultReturnValue(-1));
   private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap<>();
   private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap<>();
   private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap<>();
   private final Map<T, Lifecycle> lifecycles = new IdentityHashMap<>();
   private Lifecycle registryLifecycle;
   private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap<>();
   private boolean frozen;
   @Nullable
   private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
   @Nullable
   private List<Holder.Reference<T>> holdersInOrder;
   private int nextId;
   private final HolderLookup.RegistryLookup<T> lookup = new HolderLookup.RegistryLookup<T>() {
      public ResourceKey<? extends Registry<? extends T>> key() {
         return MappedRegistry.this.key;
      }

      public Lifecycle registryLifecycle() {
         return MappedRegistry.this.registryLifecycle();
      }

      public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
         return MappedRegistry.this.getHolder(resourcekey);
      }

      public Stream<Holder.Reference<T>> listElements() {
         return MappedRegistry.this.holders();
      }

      public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
         return MappedRegistry.this.getTag(tagkey);
      }

      public Stream<HolderSet.Named<T>> listTags() {
         return MappedRegistry.this.getTags().map(Pair::getSecond);
      }
   };

   public MappedRegistry(ResourceKey<? extends Registry<T>> resourcekey, Lifecycle lifecycle) {
      this(resourcekey, lifecycle, false);
   }

   public MappedRegistry(ResourceKey<? extends Registry<T>> resourcekey, Lifecycle lifecycle, boolean flag) {
      Bootstrap.checkBootstrapCalled(() -> "registry " + resourcekey);
      this.key = resourcekey;
      this.registryLifecycle = lifecycle;
      if (flag) {
         this.unregisteredIntrusiveHolders = new IdentityHashMap<>();
      }

   }

   public ResourceKey<? extends Registry<T>> key() {
      return this.key;
   }

   public String toString() {
      return "Registry[" + this.key + " (" + this.registryLifecycle + ")]";
   }

   private List<Holder.Reference<T>> holdersInOrder() {
      if (this.holdersInOrder == null) {
         this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
      }

      return this.holdersInOrder;
   }

   private void validateWrite() {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen");
      }
   }

   private void validateWrite(ResourceKey<T> resourcekey) {
      if (this.frozen) {
         throw new IllegalStateException("Registry is already frozen (trying to add key " + resourcekey + ")");
      }
   }

   public Holder.Reference<T> registerMapping(int i, ResourceKey<T> resourcekey, T object, Lifecycle lifecycle) {
      this.validateWrite(resourcekey);
      Validate.notNull(resourcekey);
      Validate.notNull(object);
      if (this.byLocation.containsKey(resourcekey.location())) {
         Util.pauseInIde(new IllegalStateException("Adding duplicate key '" + resourcekey + "' to registry"));
      }

      if (this.byValue.containsKey(object)) {
         Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + object + "' to registry"));
      }

      Holder.Reference<T> holder_reference;
      if (this.unregisteredIntrusiveHolders != null) {
         holder_reference = this.unregisteredIntrusiveHolders.remove(object);
         if (holder_reference == null) {
            throw new AssertionError("Missing intrusive holder for " + resourcekey + ":" + object);
         }

         holder_reference.bindKey(resourcekey);
      } else {
         holder_reference = this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> Holder.Reference.createStandAlone(this.holderOwner(), resourcekey1));
      }

      this.byKey.put(resourcekey, holder_reference);
      this.byLocation.put(resourcekey.location(), holder_reference);
      this.byValue.put(object, holder_reference);
      this.byId.size(Math.max(this.byId.size(), i + 1));
      this.byId.set(i, holder_reference);
      this.toId.put(object, i);
      if (this.nextId <= i) {
         this.nextId = i + 1;
      }

      this.lifecycles.put(object, lifecycle);
      this.registryLifecycle = this.registryLifecycle.add(lifecycle);
      this.holdersInOrder = null;
      return holder_reference;
   }

   public Holder.Reference<T> register(ResourceKey<T> resourcekey, T object, Lifecycle lifecycle) {
      return this.registerMapping(this.nextId, resourcekey, object, lifecycle);
   }

   @Nullable
   public ResourceLocation getKey(T object) {
      Holder.Reference<T> holder_reference = this.byValue.get(object);
      return holder_reference != null ? holder_reference.key().location() : null;
   }

   public Optional<ResourceKey<T>> getResourceKey(T object) {
      return Optional.ofNullable(this.byValue.get(object)).map(Holder.Reference::key);
   }

   public int getId(@Nullable T object) {
      return this.toId.getInt(object);
   }

   @Nullable
   public T get(@Nullable ResourceKey<T> resourcekey) {
      return getValueFromNullable(this.byKey.get(resourcekey));
   }

   @Nullable
   public T byId(int i) {
      return (T)(i >= 0 && i < this.byId.size() ? getValueFromNullable(this.byId.get(i)) : null);
   }

   public Optional<Holder.Reference<T>> getHolder(int i) {
      return i >= 0 && i < this.byId.size() ? Optional.ofNullable(this.byId.get(i)) : Optional.empty();
   }

   public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourcekey) {
      return Optional.ofNullable(this.byKey.get(resourcekey));
   }

   public Holder<T> wrapAsHolder(T object) {
      Holder.Reference<T> holder_reference = this.byValue.get(object);
      return (Holder<T>)(holder_reference != null ? holder_reference : Holder.direct(object));
   }

   Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> resourcekey) {
      return this.byKey.computeIfAbsent(resourcekey, (resourcekey1) -> {
         if (this.unregisteredIntrusiveHolders != null) {
            throw new IllegalStateException("This registry can't create new holders without value");
         } else {
            this.validateWrite(resourcekey1);
            return Holder.Reference.createStandAlone(this.holderOwner(), resourcekey1);
         }
      });
   }

   public int size() {
      return this.byKey.size();
   }

   public Lifecycle lifecycle(T object) {
      return this.lifecycles.get(object);
   }

   public Lifecycle registryLifecycle() {
      return this.registryLifecycle;
   }

   public Iterator<T> iterator() {
      return Iterators.transform(this.holdersInOrder().iterator(), Holder::value);
   }

   @Nullable
   public T get(@Nullable ResourceLocation resourcelocation) {
      Holder.Reference<T> holder_reference = this.byLocation.get(resourcelocation);
      return getValueFromNullable(holder_reference);
   }

   @Nullable
   private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> holder_reference) {
      return (T)(holder_reference != null ? holder_reference.value() : null);
   }

   public Set<ResourceLocation> keySet() {
      return Collections.unmodifiableSet(this.byLocation.keySet());
   }

   public Set<ResourceKey<T>> registryKeySet() {
      return Collections.unmodifiableSet(this.byKey.keySet());
   }

   public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
      return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
   }

   public Stream<Holder.Reference<T>> holders() {
      return this.holdersInOrder().stream();
   }

   public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
      return this.tags.entrySet().stream().map((map_entry) -> Pair.of(map_entry.getKey(), map_entry.getValue()));
   }

   public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagkey) {
      HolderSet.Named<T> holderset_named = this.tags.get(tagkey);
      if (holderset_named == null) {
         holderset_named = this.createTag(tagkey);
         Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap<>(this.tags);
         map.put(tagkey, holderset_named);
         this.tags = map;
      }

      return holderset_named;
   }

   private HolderSet.Named<T> createTag(TagKey<T> tagkey) {
      return new HolderSet.Named<>(this.holderOwner(), tagkey);
   }

   public Stream<TagKey<T>> getTagNames() {
      return this.tags.keySet().stream();
   }

   public boolean isEmpty() {
      return this.byKey.isEmpty();
   }

   public Optional<Holder.Reference<T>> getRandom(RandomSource randomsource) {
      return Util.getRandomSafe(this.holdersInOrder(), randomsource);
   }

   public boolean containsKey(ResourceLocation resourcelocation) {
      return this.byLocation.containsKey(resourcelocation);
   }

   public boolean containsKey(ResourceKey<T> resourcekey) {
      return this.byKey.containsKey(resourcekey);
   }

   public Registry<T> freeze() {
      if (this.frozen) {
         return this;
      } else {
         this.frozen = true;
         this.byValue.forEach((object, holder_reference) -> holder_reference.bindValue(object));
         List<ResourceLocation> list = this.byKey.entrySet().stream().filter((map_entry1) -> !map_entry1.getValue().isBound()).map((map_entry) -> map_entry.getKey().location()).sorted().toList();
         if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + list);
         } else {
            if (this.unregisteredIntrusiveHolders != null) {
               if (!this.unregisteredIntrusiveHolders.isEmpty()) {
                  throw new IllegalStateException("Some intrusive holders were not registered: " + this.unregisteredIntrusiveHolders.values());
               }

               this.unregisteredIntrusiveHolders = null;
            }

            return this;
         }
      }
   }

   public Holder.Reference<T> createIntrusiveHolder(T object) {
      if (this.unregisteredIntrusiveHolders == null) {
         throw new IllegalStateException("This registry can't create intrusive holders");
      } else {
         this.validateWrite();
         return this.unregisteredIntrusiveHolders.computeIfAbsent(object, (object1) -> Holder.Reference.createIntrusive(this.asLookup(), object1));
      }
   }

   public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagkey) {
      return Optional.ofNullable(this.tags.get(tagkey));
   }

   public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
      Map<Holder.Reference<T>, List<TagKey<T>>> map1 = new IdentityHashMap<>();
      this.byKey.values().forEach((holder_reference1) -> map1.put(holder_reference1, new ArrayList<>()));
      map.forEach((tagkey2, list1) -> {
         for(Holder<T> holder : list1) {
            if (!holder.canSerializeIn(this.asLookup())) {
               throw new IllegalStateException("Can't create named set " + tagkey2 + " containing value " + holder + " from outside registry " + this);
            }

            if (!(holder instanceof Holder.Reference)) {
               throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagkey2);
            }

            Holder.Reference<T> holder_reference = (Holder.Reference)holder;
            map1.get(holder_reference).add(tagkey2);
         }

      });
      Set<TagKey<T>> set = Sets.difference(this.tags.keySet(), map.keySet());
      if (!set.isEmpty()) {
         LOGGER.warn("Not all defined tags for registry {} are present in data pack: {}", this.key(), set.stream().map((tagkey1) -> tagkey1.location().toString()).sorted().collect(Collectors.joining(", ")));
      }

      Map<TagKey<T>, HolderSet.Named<T>> map2 = new IdentityHashMap<>(this.tags);
      map.forEach((tagkey, list) -> map2.computeIfAbsent(tagkey, this::createTag).bind(list));
      map1.forEach(Holder.Reference::bindTags);
      this.tags = map2;
   }

   public void resetTags() {
      this.tags.values().forEach((holderset_named) -> holderset_named.bind(List.of()));
      this.byKey.values().forEach((holder_reference) -> holder_reference.bindTags(Set.of()));
   }

   public HolderGetter<T> createRegistrationLookup() {
      this.validateWrite();
      return new HolderGetter<T>() {
         public Optional<Holder.Reference<T>> get(ResourceKey<T> resourcekey) {
            return Optional.of(this.getOrThrow(resourcekey));
         }

         public Holder.Reference<T> getOrThrow(ResourceKey<T> resourcekey) {
            return MappedRegistry.this.getOrCreateHolderOrThrow(resourcekey);
         }

         public Optional<HolderSet.Named<T>> get(TagKey<T> tagkey) {
            return Optional.of(this.getOrThrow(tagkey));
         }

         public HolderSet.Named<T> getOrThrow(TagKey<T> tagkey) {
            return MappedRegistry.this.getOrCreateTag(tagkey);
         }
      };
   }

   public HolderOwner<T> holderOwner() {
      return this.lookup;
   }

   public HolderLookup.RegistryLookup<T> asLookup() {
      return this.lookup;
   }
}
