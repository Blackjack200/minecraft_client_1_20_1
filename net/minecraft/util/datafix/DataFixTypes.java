package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
   LEVEL(References.LEVEL),
   PLAYER(References.PLAYER),
   CHUNK(References.CHUNK),
   HOTBAR(References.HOTBAR),
   OPTIONS(References.OPTIONS),
   STRUCTURE(References.STRUCTURE),
   STATS(References.STATS),
   SAVED_DATA(References.SAVED_DATA),
   ADVANCEMENTS(References.ADVANCEMENTS),
   POI_CHUNK(References.POI_CHUNK),
   WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
   ENTITY_CHUNK(References.ENTITY_CHUNK);

   public static final Set<DSL.TypeReference> TYPES_FOR_LEVEL_LIST;
   private final DSL.TypeReference type;

   private DataFixTypes(DSL.TypeReference dsl_typereference) {
      this.type = dsl_typereference;
   }

   private static int currentVersion() {
      return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
   }

   public <T> Dynamic<T> update(DataFixer datafixer, Dynamic<T> dynamic, int i, int j) {
      return datafixer.update(this.type, dynamic, i, j);
   }

   public <T> Dynamic<T> updateToCurrentVersion(DataFixer datafixer, Dynamic<T> dynamic, int i) {
      return this.update(datafixer, dynamic, i, currentVersion());
   }

   public CompoundTag update(DataFixer datafixer, CompoundTag compoundtag, int i, int j) {
      return this.update(datafixer, new Dynamic<>(NbtOps.INSTANCE, compoundtag), i, j).getValue();
   }

   public CompoundTag updateToCurrentVersion(DataFixer datafixer, CompoundTag compoundtag, int i) {
      return this.update(datafixer, compoundtag, i, currentVersion());
   }

   static {
      TYPES_FOR_LEVEL_LIST = Set.of(LEVEL.type);
   }
}
