package net.minecraft.stats;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.RecipeBookType;

public final class RecipeBookSettings {
   private static final Map<RecipeBookType, Pair<String, String>> TAG_FIELDS = ImmutableMap.of(RecipeBookType.CRAFTING, Pair.of("isGuiOpen", "isFilteringCraftable"), RecipeBookType.FURNACE, Pair.of("isFurnaceGuiOpen", "isFurnaceFilteringCraftable"), RecipeBookType.BLAST_FURNACE, Pair.of("isBlastingFurnaceGuiOpen", "isBlastingFurnaceFilteringCraftable"), RecipeBookType.SMOKER, Pair.of("isSmokerGuiOpen", "isSmokerFilteringCraftable"));
   private final Map<RecipeBookType, RecipeBookSettings.TypeSettings> states;

   private RecipeBookSettings(Map<RecipeBookType, RecipeBookSettings.TypeSettings> map) {
      this.states = map;
   }

   public RecipeBookSettings() {
      this(Util.make(Maps.newEnumMap(RecipeBookType.class), (enummap) -> {
         for(RecipeBookType recipebooktype : RecipeBookType.values()) {
            enummap.put(recipebooktype, new RecipeBookSettings.TypeSettings(false, false));
         }

      }));
   }

   public boolean isOpen(RecipeBookType recipebooktype) {
      return (this.states.get(recipebooktype)).open;
   }

   public void setOpen(RecipeBookType recipebooktype, boolean flag) {
      (this.states.get(recipebooktype)).open = flag;
   }

   public boolean isFiltering(RecipeBookType recipebooktype) {
      return (this.states.get(recipebooktype)).filtering;
   }

   public void setFiltering(RecipeBookType recipebooktype, boolean flag) {
      (this.states.get(recipebooktype)).filtering = flag;
   }

   public static RecipeBookSettings read(FriendlyByteBuf friendlybytebuf) {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         boolean flag = friendlybytebuf.readBoolean();
         boolean flag1 = friendlybytebuf.readBoolean();
         map.put(recipebooktype, new RecipeBookSettings.TypeSettings(flag, flag1));
      }

      return new RecipeBookSettings(map);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings_typesettings = this.states.get(recipebooktype);
         if (recipebooksettings_typesettings == null) {
            friendlybytebuf.writeBoolean(false);
            friendlybytebuf.writeBoolean(false);
         } else {
            friendlybytebuf.writeBoolean(recipebooksettings_typesettings.open);
            friendlybytebuf.writeBoolean(recipebooksettings_typesettings.filtering);
         }
      }

   }

   public static RecipeBookSettings read(CompoundTag compoundtag) {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);
      TAG_FIELDS.forEach((recipebooktype, pair) -> {
         boolean flag = compoundtag.getBoolean(pair.getFirst());
         boolean flag1 = compoundtag.getBoolean(pair.getSecond());
         map.put(recipebooktype, new RecipeBookSettings.TypeSettings(flag, flag1));
      });
      return new RecipeBookSettings(map);
   }

   public void write(CompoundTag compoundtag) {
      TAG_FIELDS.forEach((recipebooktype, pair) -> {
         RecipeBookSettings.TypeSettings recipebooksettings_typesettings = this.states.get(recipebooktype);
         compoundtag.putBoolean(pair.getFirst(), recipebooksettings_typesettings.open);
         compoundtag.putBoolean(pair.getSecond(), recipebooksettings_typesettings.filtering);
      });
   }

   public RecipeBookSettings copy() {
      Map<RecipeBookType, RecipeBookSettings.TypeSettings> map = Maps.newEnumMap(RecipeBookType.class);

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings_typesettings = this.states.get(recipebooktype);
         map.put(recipebooktype, recipebooksettings_typesettings.copy());
      }

      return new RecipeBookSettings(map);
   }

   public void replaceFrom(RecipeBookSettings recipebooksettings) {
      this.states.clear();

      for(RecipeBookType recipebooktype : RecipeBookType.values()) {
         RecipeBookSettings.TypeSettings recipebooksettings_typesettings = recipebooksettings.states.get(recipebooktype);
         this.states.put(recipebooktype, recipebooksettings_typesettings.copy());
      }

   }

   public boolean equals(Object object) {
      return this == object || object instanceof RecipeBookSettings && this.states.equals(((RecipeBookSettings)object).states);
   }

   public int hashCode() {
      return this.states.hashCode();
   }

   static final class TypeSettings {
      boolean open;
      boolean filtering;

      public TypeSettings(boolean flag, boolean flag1) {
         this.open = flag;
         this.filtering = flag1;
      }

      public RecipeBookSettings.TypeSettings copy() {
         return new RecipeBookSettings.TypeSettings(this.open, this.filtering);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (!(object instanceof RecipeBookSettings.TypeSettings)) {
            return false;
         } else {
            RecipeBookSettings.TypeSettings recipebooksettings_typesettings = (RecipeBookSettings.TypeSettings)object;
            return this.open == recipebooksettings_typesettings.open && this.filtering == recipebooksettings_typesettings.filtering;
         }
      }

      public int hashCode() {
         int i = this.open ? 1 : 0;
         return 31 * i + (this.filtering ? 1 : 0);
      }

      public String toString() {
         return "[open=" + this.open + ", filtering=" + this.filtering + "]";
      }
   }
}
