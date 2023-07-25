package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimpleJsonResourceReloadListener {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogUtils.getLogger();
   private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = ImmutableMap.of();
   private Map<ResourceLocation, Recipe<?>> byName = ImmutableMap.of();
   private boolean hasErrors;

   public RecipeManager() {
      super(GSON, "recipes");
   }

   protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      this.hasErrors = false;
      Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> map1 = Maps.newHashMap();
      ImmutableMap.Builder<ResourceLocation, Recipe<?>> immutablemap_builder = ImmutableMap.builder();

      for(Map.Entry<ResourceLocation, JsonElement> map_entry : map.entrySet()) {
         ResourceLocation resourcelocation = map_entry.getKey();

         try {
            Recipe<?> recipe = fromJson(resourcelocation, GsonHelper.convertToJsonObject(map_entry.getValue(), "top element"));
            map1.computeIfAbsent(recipe.getType(), (recipetype) -> ImmutableMap.builder()).put(resourcelocation, recipe);
            immutablemap_builder.put(resourcelocation, recipe);
         } catch (IllegalArgumentException | JsonParseException var10) {
            LOGGER.error("Parsing error loading recipe {}", resourcelocation, var10);
         }
      }

      this.recipes = map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (map_entry1) -> map_entry1.getValue().build()));
      this.byName = immutablemap_builder.build();
      LOGGER.info("Loaded {} recipes", (int)map1.size());
   }

   public boolean hadErrorsLoading() {
      return this.hasErrors;
   }

   public <C extends Container, T extends Recipe<C>> Optional<T> getRecipeFor(RecipeType<T> recipetype, C container, Level level) {
      return this.byType(recipetype).values().stream().filter((recipe) -> recipe.matches(container, level)).findFirst();
   }

   public <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> getRecipeFor(RecipeType<T> recipetype, C container, Level level, @Nullable ResourceLocation resourcelocation) {
      Map<ResourceLocation, T> map = this.byType(recipetype);
      if (resourcelocation != null) {
         T recipe = map.get(resourcelocation);
         if (recipe != null && recipe.matches(container, level)) {
            return Optional.of(Pair.of(resourcelocation, recipe));
         }
      }

      return map.entrySet().stream().filter((map_entry1) -> map_entry1.getValue().matches(container, level)).findFirst().map((map_entry) -> Pair.of(map_entry.getKey(), map_entry.getValue()));
   }

   public <C extends Container, T extends Recipe<C>> List<T> getAllRecipesFor(RecipeType<T> recipetype) {
      return List.copyOf(this.byType(recipetype).values());
   }

   public <C extends Container, T extends Recipe<C>> List<T> getRecipesFor(RecipeType<T> recipetype, C container, Level level) {
      return this.byType(recipetype).values().stream().filter((recipe1) -> recipe1.matches(container, level)).sorted(Comparator.comparing((recipe) -> recipe.getResultItem(level.registryAccess()).getDescriptionId())).collect(Collectors.toList());
   }

   private <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> byType(RecipeType<T> recipetype) {
      return this.recipes.getOrDefault(recipetype, Collections.emptyMap());
   }

   public <C extends Container, T extends Recipe<C>> NonNullList<ItemStack> getRemainingItemsFor(RecipeType<T> recipetype, C container, Level level) {
      Optional<T> optional = this.getRecipeFor(recipetype, container, level);
      if (optional.isPresent()) {
         return optional.get().getRemainingItems(container);
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

         for(int i = 0; i < nonnulllist.size(); ++i) {
            nonnulllist.set(i, container.getItem(i));
         }

         return nonnulllist;
      }
   }

   public Optional<? extends Recipe<?>> byKey(ResourceLocation resourcelocation) {
      return Optional.ofNullable(this.byName.get(resourcelocation));
   }

   public Collection<Recipe<?>> getRecipes() {
      return this.recipes.values().stream().flatMap((map) -> map.values().stream()).collect(Collectors.toSet());
   }

   public Stream<ResourceLocation> getRecipeIds() {
      return this.recipes.values().stream().flatMap((map) -> map.keySet().stream());
   }

   public static Recipe<?> fromJson(ResourceLocation resourcelocation, JsonObject jsonobject) {
      String s = GsonHelper.getAsString(jsonobject, "type");
      return BuiltInRegistries.RECIPE_SERIALIZER.getOptional(new ResourceLocation(s)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported recipe type '" + s + "'")).fromJson(resourcelocation, jsonobject);
   }

   public void replaceRecipes(Iterable<Recipe<?>> iterable) {
      this.hasErrors = false;
      Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = Maps.newHashMap();
      ImmutableMap.Builder<ResourceLocation, Recipe<?>> immutablemap_builder = ImmutableMap.builder();
      iterable.forEach((recipe) -> {
         Map<ResourceLocation, Recipe<?>> map2 = map.computeIfAbsent(recipe.getType(), (recipetype) -> Maps.newHashMap());
         ResourceLocation resourcelocation = recipe.getId();
         Recipe<?> recipe1 = map2.put(resourcelocation, recipe);
         immutablemap_builder.put(resourcelocation, recipe);
         if (recipe1 != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + resourcelocation);
         }
      });
      this.recipes = ImmutableMap.copyOf(map);
      this.byName = immutablemap_builder.build();
   }

   public static <C extends Container, T extends Recipe<C>> RecipeManager.CachedCheck<C, T> createCheck(final RecipeType<T> recipetype) {
      return new RecipeManager.CachedCheck<C, T>() {
         @Nullable
         private ResourceLocation lastRecipe;

         public Optional<T> getRecipeFor(C container, Level level) {
            RecipeManager recipemanager = level.getRecipeManager();
            Optional<Pair<ResourceLocation, T>> optional = recipemanager.getRecipeFor(recipetype, container, level, this.lastRecipe);
            if (optional.isPresent()) {
               Pair<ResourceLocation, T> pair = optional.get();
               this.lastRecipe = pair.getFirst();
               return Optional.of(pair.getSecond());
            } else {
               return Optional.empty();
            }
         }
      };
   }

   public interface CachedCheck<C extends Container, T extends Recipe<C>> {
      Optional<T> getRecipeFor(C container, Level level);
   }
}
