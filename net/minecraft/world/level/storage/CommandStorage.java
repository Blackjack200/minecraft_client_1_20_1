package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

public class CommandStorage {
   private static final String ID_PREFIX = "command_storage_";
   private final Map<String, CommandStorage.Container> namespaces = Maps.newHashMap();
   private final DimensionDataStorage storage;

   public CommandStorage(DimensionDataStorage dimensiondatastorage) {
      this.storage = dimensiondatastorage;
   }

   private CommandStorage.Container newStorage(String s) {
      CommandStorage.Container commandstorage_container = new CommandStorage.Container();
      this.namespaces.put(s, commandstorage_container);
      return commandstorage_container;
   }

   public CompoundTag get(ResourceLocation resourcelocation) {
      String s = resourcelocation.getNamespace();
      CommandStorage.Container commandstorage_container = this.storage.get((compoundtag) -> this.newStorage(s).load(compoundtag), createId(s));
      return commandstorage_container != null ? commandstorage_container.get(resourcelocation.getPath()) : new CompoundTag();
   }

   public void set(ResourceLocation resourcelocation, CompoundTag compoundtag) {
      String s = resourcelocation.getNamespace();
      this.storage.computeIfAbsent((compoundtag1) -> this.newStorage(s).load(compoundtag1), () -> this.newStorage(s), createId(s)).put(resourcelocation.getPath(), compoundtag);
   }

   public Stream<ResourceLocation> keys() {
      return this.namespaces.entrySet().stream().flatMap((map_entry) -> map_entry.getValue().getKeys(map_entry.getKey()));
   }

   private static String createId(String s) {
      return "command_storage_" + s;
   }

   static class Container extends SavedData {
      private static final String TAG_CONTENTS = "contents";
      private final Map<String, CompoundTag> storage = Maps.newHashMap();

      CommandStorage.Container load(CompoundTag compoundtag) {
         CompoundTag compoundtag1 = compoundtag.getCompound("contents");

         for(String s : compoundtag1.getAllKeys()) {
            this.storage.put(s, compoundtag1.getCompound(s));
         }

         return this;
      }

      public CompoundTag save(CompoundTag compoundtag) {
         CompoundTag compoundtag1 = new CompoundTag();
         this.storage.forEach((s, compoundtag3) -> compoundtag1.put(s, compoundtag3.copy()));
         compoundtag.put("contents", compoundtag1);
         return compoundtag;
      }

      public CompoundTag get(String s) {
         CompoundTag compoundtag = this.storage.get(s);
         return compoundtag != null ? compoundtag : new CompoundTag();
      }

      public void put(String s, CompoundTag compoundtag) {
         if (compoundtag.isEmpty()) {
            this.storage.remove(s);
         } else {
            this.storage.put(s, compoundtag);
         }

         this.setDirty();
      }

      public Stream<ResourceLocation> getKeys(String s) {
         return this.storage.keySet().stream().map((s2) -> new ResourceLocation(s, s2));
      }
   }
}
