package net.minecraft.data.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public abstract class ItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {
   private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
   private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

   public ItemTagsProvider(PackOutput packoutput, CompletableFuture<HolderLookup.Provider> completablefuture, CompletableFuture<TagsProvider.TagLookup<Block>> completablefuture1) {
      super(packoutput, Registries.ITEM, completablefuture, (item) -> item.builtInRegistryHolder().key());
      this.blockTags = completablefuture1;
   }

   public ItemTagsProvider(PackOutput packoutput, CompletableFuture<HolderLookup.Provider> completablefuture, CompletableFuture<TagsProvider.TagLookup<Item>> completablefuture1, CompletableFuture<TagsProvider.TagLookup<Block>> completablefuture2) {
      super(packoutput, Registries.ITEM, completablefuture, completablefuture1, (item) -> item.builtInRegistryHolder().key());
      this.blockTags = completablefuture2;
   }

   protected void copy(TagKey<Block> tagkey, TagKey<Item> tagkey1) {
      this.tagsToCopy.put(tagkey, tagkey1);
   }

   protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
      return super.createContentsProvider().thenCombineAsync(this.blockTags, (holderlookup_provider, tagsprovider_taglookup) -> {
         this.tagsToCopy.forEach((tagkey, tagkey1) -> {
            TagBuilder tagbuilder = this.getOrCreateRawBuilder(tagkey1);
            Optional<TagBuilder> optional = tagsprovider_taglookup.apply(tagkey);
            optional.orElseThrow(() -> new IllegalStateException("Missing block tag " + tagkey1.location())).build().forEach(tagbuilder::add);
         });
         return holderlookup_provider;
      });
   }
}
