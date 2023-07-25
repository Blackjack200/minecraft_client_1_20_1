package net.minecraft.client.gui.font;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class FontManager implements PreparableReloadListener, AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String FONTS_PATH = "fonts.json";
   public static final ResourceLocation MISSING_FONT = new ResourceLocation("minecraft", "missing");
   private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final FontSet missingFontSet;
   private final List<GlyphProvider> providersToClose = new ArrayList<>();
   private final Map<ResourceLocation, FontSet> fontSets = new HashMap<>();
   private final TextureManager textureManager;
   private Map<ResourceLocation, ResourceLocation> renames = ImmutableMap.of();

   public FontManager(TextureManager texturemanager) {
      this.textureManager = texturemanager;
      this.missingFontSet = Util.make(new FontSet(texturemanager, MISSING_FONT), (fontset) -> fontset.reload(Lists.newArrayList(new AllMissingGlyphProvider())));
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      profilerfiller.startTick();
      profilerfiller.endTick();
      return this.prepare(resourcemanager, executor).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((fontmanager_preparation) -> this.apply(fontmanager_preparation, profilerfiller1), executor1);
   }

   private CompletableFuture<FontManager.Preparation> prepare(ResourceManager resourcemanager, Executor executor) {
      List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> list = new ArrayList<>();

      for(Map.Entry<ResourceLocation, List<Resource>> map_entry : FONT_DEFINITIONS.listMatchingResourceStacks(resourcemanager).entrySet()) {
         ResourceLocation resourcelocation = FONT_DEFINITIONS.fileToId(map_entry.getKey());
         list.add(CompletableFuture.supplyAsync(() -> {
            List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> list9 = loadResourceStack(map_entry.getValue(), resourcelocation);
            FontManager.UnresolvedBuilderBundle fontmanager_unresolvedbuilderbundle = new FontManager.UnresolvedBuilderBundle(resourcelocation);

            for(Pair<FontManager.BuilderId, GlyphProviderDefinition> pair : list9) {
               FontManager.BuilderId fontmanager_builderid = pair.getFirst();
               pair.getSecond().unpack().ifLeft((glyphproviderdefinition_loader) -> {
                  CompletableFuture<Optional<GlyphProvider>> completablefuture = this.safeLoad(fontmanager_builderid, glyphproviderdefinition_loader, resourcemanager, executor);
                  fontmanager_unresolvedbuilderbundle.add(fontmanager_builderid, completablefuture);
               }).ifRight((glyphproviderdefinition_reference) -> fontmanager_unresolvedbuilderbundle.add(fontmanager_builderid, glyphproviderdefinition_reference));
            }

            return fontmanager_unresolvedbuilderbundle;
         }, executor));
      }

      return Util.sequence(list).thenCompose((list1) -> {
         List<CompletableFuture<Optional<GlyphProvider>>> list2 = list1.stream().flatMap(FontManager.UnresolvedBuilderBundle::listBuilders).collect(Collectors.toCollection(ArrayList::new));
         GlyphProvider glyphprovider = new AllMissingGlyphProvider();
         list2.add(CompletableFuture.completedFuture(Optional.of(glyphprovider)));
         return Util.sequence(list2).thenCompose((list4) -> {
            Map<ResourceLocation, List<GlyphProvider>> map = this.resolveProviders(list1);
            CompletableFuture<?>[] acompletablefuture = map.values().stream().map((list7) -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading(list7, glyphprovider), executor)).toArray((i) -> new CompletableFuture[i]);
            return CompletableFuture.allOf(acompletablefuture).thenApply((ovoid) -> {
               List<GlyphProvider> list6 = list4.stream().flatMap(Optional::stream).toList();
               return new FontManager.Preparation(map, list6);
            });
         });
      });
   }

   private CompletableFuture<Optional<GlyphProvider>> safeLoad(FontManager.BuilderId fontmanager_builderid, GlyphProviderDefinition.Loader glyphproviderdefinition_loader, ResourceManager resourcemanager, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            return Optional.of(glyphproviderdefinition_loader.load(resourcemanager));
         } catch (Exception var4) {
            LOGGER.warn("Failed to load builder {}, rejecting", fontmanager_builderid, var4);
            return Optional.empty();
         }
      }, executor);
   }

   private Map<ResourceLocation, List<GlyphProvider>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> list) {
      Map<ResourceLocation, List<GlyphProvider>> map = new HashMap<>();
      DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> dependencysorter = new DependencySorter<>();
      list.forEach((fontmanager_unresolvedbuilderbundle1) -> dependencysorter.addEntry(fontmanager_unresolvedbuilderbundle1.fontId, fontmanager_unresolvedbuilderbundle1));
      dependencysorter.orderByDependencies((resourcelocation, fontmanager_unresolvedbuilderbundle) -> fontmanager_unresolvedbuilderbundle.resolve(map::get).ifPresent((list1) -> map.put(resourcelocation, list1)));
      return map;
   }

   private void finalizeProviderLoading(List<GlyphProvider> list, GlyphProvider glyphprovider) {
      list.add(0, glyphprovider);
      IntSet intset = new IntOpenHashSet();

      for(GlyphProvider glyphprovider1 : list) {
         intset.addAll(glyphprovider1.getSupportedGlyphs());
      }

      intset.forEach((i) -> {
         if (i != 32) {
            for(GlyphProvider glyphprovider2 : Lists.reverse(list)) {
               if (glyphprovider2.getGlyph(i) != null) {
                  break;
               }
            }

         }
      });
   }

   private void apply(FontManager.Preparation fontmanager_preparation, ProfilerFiller profilerfiller) {
      profilerfiller.startTick();
      profilerfiller.push("closing");
      this.fontSets.values().forEach(FontSet::close);
      this.fontSets.clear();
      this.providersToClose.forEach(GlyphProvider::close);
      this.providersToClose.clear();
      profilerfiller.popPush("reloading");
      fontmanager_preparation.providers().forEach((resourcelocation, list) -> {
         FontSet fontset = new FontSet(this.textureManager, resourcelocation);
         fontset.reload(Lists.reverse(list));
         this.fontSets.put(resourcelocation, fontset);
      });
      this.providersToClose.addAll(fontmanager_preparation.allProviders);
      profilerfiller.pop();
      profilerfiller.endTick();
      if (!this.fontSets.containsKey(this.getActualId(Minecraft.DEFAULT_FONT))) {
         throw new IllegalStateException("Default font failed to load");
      }
   }

   private static List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> loadResourceStack(List<Resource> list, ResourceLocation resourcelocation) {
      List<Pair<FontManager.BuilderId, GlyphProviderDefinition>> list1 = new ArrayList<>();

      for(Resource resource : list) {
         try {
            Reader reader = resource.openAsReader();

            try {
               JsonElement jsonelement = GSON.fromJson(reader, JsonElement.class);
               FontManager.FontDefinitionFile fontmanager_fontdefinitionfile = Util.getOrThrow(FontManager.FontDefinitionFile.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new);
               List<GlyphProviderDefinition> list2 = fontmanager_fontdefinitionfile.providers;

               for(int i = list2.size() - 1; i >= 0; --i) {
                  FontManager.BuilderId fontmanager_builderid = new FontManager.BuilderId(resourcelocation, resource.sourcePackId(), i);
                  list1.add(Pair.of(fontmanager_builderid, list2.get(i)));
               }
            } catch (Throwable var12) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }
               }

               throw var12;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (Exception var13) {
            LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", resourcelocation, "fonts.json", resource.sourcePackId(), var13);
         }
      }

      return list1;
   }

   public void setRenames(Map<ResourceLocation, ResourceLocation> map) {
      this.renames = map;
   }

   private ResourceLocation getActualId(ResourceLocation resourcelocation) {
      return this.renames.getOrDefault(resourcelocation, resourcelocation);
   }

   public Font createFont() {
      return new Font((resourcelocation) -> this.fontSets.getOrDefault(this.getActualId(resourcelocation), this.missingFontSet), false);
   }

   public Font createFontFilterFishy() {
      return new Font((resourcelocation) -> this.fontSets.getOrDefault(this.getActualId(resourcelocation), this.missingFontSet), true);
   }

   public void close() {
      this.fontSets.values().forEach(FontSet::close);
      this.providersToClose.forEach(GlyphProvider::close);
      this.missingFontSet.close();
   }

   static record BuilderId(ResourceLocation fontId, String pack, int index) {
      public String toString() {
         return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
      }
   }

   static record BuilderResult(FontManager.BuilderId id, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result) {
      final Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result;

      public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> function) {
         return this.result.map((completablefuture) -> completablefuture.join().map(List::of), (resourcelocation) -> {
            List<GlyphProvider> list = function.apply(resourcelocation);
            if (list == null) {
               FontManager.LOGGER.warn("Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle", resourcelocation, this.id);
               return Optional.empty();
            } else {
               return Optional.of(list);
            }
         });
      }
   }

   static record FontDefinitionFile(List<GlyphProviderDefinition> providers) {
      final List<GlyphProviderDefinition> providers;
      public static final Codec<FontManager.FontDefinitionFile> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(GlyphProviderDefinition.CODEC.listOf().fieldOf("providers").forGetter(FontManager.FontDefinitionFile::providers)).apply(recordcodecbuilder_instance, FontManager.FontDefinitionFile::new));
   }

   static record Preparation(Map<ResourceLocation, List<GlyphProvider>> providers, List<GlyphProvider> allProviders) {
      final List<GlyphProvider> allProviders;
   }

   static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies) implements DependencySorter.Entry<ResourceLocation> {
      final ResourceLocation fontId;

      public UnresolvedBuilderBundle(ResourceLocation resourcelocation) {
         this(resourcelocation, new ArrayList<>(), new HashSet<>());
      }

      public void add(FontManager.BuilderId fontmanager_builderid, GlyphProviderDefinition.Reference glyphproviderdefinition_reference) {
         this.builders.add(new FontManager.BuilderResult(fontmanager_builderid, Either.right(glyphproviderdefinition_reference.id())));
         this.dependencies.add(glyphproviderdefinition_reference.id());
      }

      public void add(FontManager.BuilderId fontmanager_builderid, CompletableFuture<Optional<GlyphProvider>> completablefuture) {
         this.builders.add(new FontManager.BuilderResult(fontmanager_builderid, Either.left(completablefuture)));
      }

      private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
         return this.builders.stream().flatMap((fontmanager_builderresult) -> fontmanager_builderresult.result.left().stream());
      }

      public Optional<List<GlyphProvider>> resolve(Function<ResourceLocation, List<GlyphProvider>> function) {
         List<GlyphProvider> list = new ArrayList<>();

         for(FontManager.BuilderResult fontmanager_builderresult : this.builders) {
            Optional<List<GlyphProvider>> optional = fontmanager_builderresult.resolve(function);
            if (!optional.isPresent()) {
               return Optional.empty();
            }

            list.addAll(optional.get());
         }

         return Optional.of(list);
      }

      public void visitRequiredDependencies(Consumer<ResourceLocation> consumer) {
         this.dependencies.forEach(consumer);
      }

      public void visitOptionalDependencies(Consumer<ResourceLocation> consumer) {
      }
   }
}
