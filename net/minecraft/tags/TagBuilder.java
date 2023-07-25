package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder {
   private final List<TagEntry> entries = new ArrayList<>();

   public static TagBuilder create() {
      return new TagBuilder();
   }

   public List<TagEntry> build() {
      return List.copyOf(this.entries);
   }

   public TagBuilder add(TagEntry tagentry) {
      this.entries.add(tagentry);
      return this;
   }

   public TagBuilder addElement(ResourceLocation resourcelocation) {
      return this.add(TagEntry.element(resourcelocation));
   }

   public TagBuilder addOptionalElement(ResourceLocation resourcelocation) {
      return this.add(TagEntry.optionalElement(resourcelocation));
   }

   public TagBuilder addTag(ResourceLocation resourcelocation) {
      return this.add(TagEntry.tag(resourcelocation));
   }

   public TagBuilder addOptionalTag(ResourceLocation resourcelocation) {
      return this.add(TagEntry.optionalTag(resourcelocation));
   }
}
