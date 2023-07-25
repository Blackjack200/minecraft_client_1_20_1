package net.minecraft.tags;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class TagEntry {
   private static final Codec<TagEntry> FULL_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ExtraCodecs.TAG_OR_ELEMENT_ID.fieldOf("id").forGetter(TagEntry::elementOrTag), Codec.BOOL.optionalFieldOf("required", Boolean.valueOf(true)).forGetter((tagentry) -> tagentry.required)).apply(recordcodecbuilder_instance, TagEntry::new));
   public static final Codec<TagEntry> CODEC = Codec.either(ExtraCodecs.TAG_OR_ELEMENT_ID, FULL_CODEC).xmap((either) -> either.map((extracodecs_tagorelementlocation) -> new TagEntry(extracodecs_tagorelementlocation, true), (tagentry) -> tagentry), (tagentry) -> tagentry.required ? Either.left(tagentry.elementOrTag()) : Either.right(tagentry));
   private final ResourceLocation id;
   private final boolean tag;
   private final boolean required;

   private TagEntry(ResourceLocation resourcelocation, boolean flag, boolean flag1) {
      this.id = resourcelocation;
      this.tag = flag;
      this.required = flag1;
   }

   private TagEntry(ExtraCodecs.TagOrElementLocation extracodecs_tagorelementlocation, boolean flag) {
      this.id = extracodecs_tagorelementlocation.id();
      this.tag = extracodecs_tagorelementlocation.tag();
      this.required = flag;
   }

   private ExtraCodecs.TagOrElementLocation elementOrTag() {
      return new ExtraCodecs.TagOrElementLocation(this.id, this.tag);
   }

   public static TagEntry element(ResourceLocation resourcelocation) {
      return new TagEntry(resourcelocation, false, true);
   }

   public static TagEntry optionalElement(ResourceLocation resourcelocation) {
      return new TagEntry(resourcelocation, false, false);
   }

   public static TagEntry tag(ResourceLocation resourcelocation) {
      return new TagEntry(resourcelocation, true, true);
   }

   public static TagEntry optionalTag(ResourceLocation resourcelocation) {
      return new TagEntry(resourcelocation, true, false);
   }

   public <T> boolean build(TagEntry.Lookup<T> tagentry_lookup, Consumer<T> consumer) {
      if (this.tag) {
         Collection<T> collection = tagentry_lookup.tag(this.id);
         if (collection == null) {
            return !this.required;
         }

         collection.forEach(consumer);
      } else {
         T object = tagentry_lookup.element(this.id);
         if (object == null) {
            return !this.required;
         }

         consumer.accept(object);
      }

      return true;
   }

   public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
      if (this.tag && this.required) {
         consumer.accept(this.id);
      }

   }

   public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
      if (this.tag && !this.required) {
         consumer.accept(this.id);
      }

   }

   public boolean verifyIfPresent(Predicate<ResourceLocation> predicate, Predicate<ResourceLocation> predicate1) {
      return !this.required || (this.tag ? predicate1 : predicate).test(this.id);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      if (this.tag) {
         stringbuilder.append('#');
      }

      stringbuilder.append((Object)this.id);
      if (!this.required) {
         stringbuilder.append('?');
      }

      return stringbuilder.toString();
   }

   public interface Lookup<T> {
      @Nullable
      T element(ResourceLocation resourcelocation);

      @Nullable
      Collection<T> tag(ResourceLocation resourcelocation);
   }
}
