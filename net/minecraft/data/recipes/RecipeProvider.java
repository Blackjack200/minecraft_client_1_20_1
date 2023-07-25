package net.minecraft.data.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class RecipeProvider implements DataProvider {
   private final PackOutput.PathProvider recipePathProvider;
   private final PackOutput.PathProvider advancementPathProvider;
   private static final Map<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>> SHAPE_BUILDERS = ImmutableMap.<BlockFamily.Variant, BiFunction<ItemLike, ItemLike, RecipeBuilder>>builder().put(BlockFamily.Variant.BUTTON, (itemlike, itemlike1) -> buttonBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.CHISELED, (itemlike, itemlike1) -> chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.CUT, (itemlike, itemlike1) -> cutBuilder(RecipeCategory.BUILDING_BLOCKS, itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.DOOR, (itemlike, itemlike1) -> doorBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.CUSTOM_FENCE, (itemlike, itemlike1) -> fenceBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.FENCE, (itemlike, itemlike1) -> fenceBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (itemlike, itemlike1) -> fenceGateBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.FENCE_GATE, (itemlike, itemlike1) -> fenceGateBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.SIGN, (itemlike, itemlike1) -> signBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.SLAB, (itemlike, itemlike1) -> slabBuilder(RecipeCategory.BUILDING_BLOCKS, itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.STAIRS, (itemlike, itemlike1) -> stairBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.PRESSURE_PLATE, (itemlike, itemlike1) -> pressurePlateBuilder(RecipeCategory.REDSTONE, itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.POLISHED, (itemlike, itemlike1) -> polishedBuilder(RecipeCategory.BUILDING_BLOCKS, itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.TRAPDOOR, (itemlike, itemlike1) -> trapdoorBuilder(itemlike, Ingredient.of(itemlike1))).put(BlockFamily.Variant.WALL, (itemlike, itemlike1) -> wallBuilder(RecipeCategory.DECORATIONS, itemlike, Ingredient.of(itemlike1))).build();

   public RecipeProvider(PackOutput packoutput) {
      this.recipePathProvider = packoutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
      this.advancementPathProvider = packoutput.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
   }

   public CompletableFuture<?> run(CachedOutput cachedoutput) {
      Set<ResourceLocation> set = Sets.newHashSet();
      List<CompletableFuture<?>> list = new ArrayList<>();
      this.buildRecipes((finishedrecipe) -> {
         if (!set.add(finishedrecipe.getId())) {
            throw new IllegalStateException("Duplicate recipe " + finishedrecipe.getId());
         } else {
            list.add(DataProvider.saveStable(cachedoutput, finishedrecipe.serializeRecipe(), this.recipePathProvider.json(finishedrecipe.getId())));
            JsonObject jsonobject = finishedrecipe.serializeAdvancement();
            if (jsonobject != null) {
               list.add(DataProvider.saveStable(cachedoutput, jsonobject, this.advancementPathProvider.json(finishedrecipe.getAdvancementId())));
            }

         }
      });
      return CompletableFuture.allOf(list.toArray((i) -> new CompletableFuture[i]));
   }

   protected CompletableFuture<?> buildAdvancement(CachedOutput cachedoutput, ResourceLocation resourcelocation, Advancement.Builder advancement_builder) {
      return DataProvider.saveStable(cachedoutput, advancement_builder.serializeToJson(), this.advancementPathProvider.json(resourcelocation));
   }

   protected abstract void buildRecipes(Consumer<FinishedRecipe> consumer);

   protected static void generateForEnabledBlockFamilies(Consumer<FinishedRecipe> consumer, FeatureFlagSet featureflagset) {
      BlockFamilies.getAllFamilies().filter((blockfamily1) -> blockfamily1.shouldGenerateRecipe(featureflagset)).forEach((blockfamily) -> generateRecipes(consumer, blockfamily));
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1, @Nullable String s) {
      oneToOneConversionRecipe(consumer, itemlike, itemlike1, s, 1);
   }

   protected static void oneToOneConversionRecipe(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1, @Nullable String s, int i) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, itemlike, i).requires(itemlike1).group(s).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer, getConversionRecipeName(itemlike, itemlike1));
   }

   protected static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipecategory, ItemLike itemlike, float f, int i, String s) {
      oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE, list, recipecategory, itemlike, f, i, s, "_from_smelting");
   }

   protected static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> list, RecipeCategory recipecategory, ItemLike itemlike, float f, int i, String s) {
      oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, list, recipecategory, itemlike, f, i, s, "_from_blasting");
   }

   private static void oreCooking(Consumer<FinishedRecipe> consumer, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer, List<ItemLike> list, RecipeCategory recipecategory, ItemLike itemlike, float f, int i, String s, String s1) {
      for(ItemLike itemlike1 : list) {
         SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike1), recipecategory, itemlike, f, i, recipeserializer).group(s).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer, getItemName(itemlike) + s1 + "_" + getItemName(itemlike1));
      }

   }

   protected static void netheriteSmithing(Consumer<FinishedRecipe> consumer, Item item, RecipeCategory recipecategory, Item item1) {
      SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(item), Ingredient.of(Items.NETHERITE_INGOT), recipecategory, item1).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT)).save(consumer, getItemName(item1) + "_smithing");
   }

   protected static void trimSmithing(Consumer<FinishedRecipe> consumer, Item item, ResourceLocation resourcelocation) {
      SmithingTrimRecipeBuilder.smithingTrim(Ingredient.of(item), Ingredient.of(ItemTags.TRIMMABLE_ARMOR), Ingredient.of(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).unlocks("has_smithing_trim_template", has(item)).save(consumer, resourcelocation);
   }

   protected static void twoByTwoPacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(recipecategory, itemlike, 1).define('#', itemlike1).pattern("##").pattern("##").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1, String s) {
      ShapelessRecipeBuilder.shapeless(recipecategory, itemlike).requires(itemlike1, 9).unlockedBy(s, has(itemlike1)).save(consumer);
   }

   protected static void threeByThreePacker(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      threeByThreePacker(consumer, recipecategory, itemlike, itemlike1, getHasName(itemlike1));
   }

   protected static void planksFromLog(Consumer<FinishedRecipe> consumer, ItemLike itemlike, TagKey<Item> tagkey, int i) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemlike, i).requires(tagkey).group("planks").unlockedBy("has_log", has(tagkey)).save(consumer);
   }

   protected static void planksFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemlike, TagKey<Item> tagkey, int i) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemlike, i).requires(tagkey).group("planks").unlockedBy("has_logs", has(tagkey)).save(consumer);
   }

   protected static void woodFromLogs(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemlike, 3).define('#', itemlike1).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(itemlike1)).save(consumer);
   }

   protected static void woodenBoat(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, itemlike).define('#', itemlike1).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", insideOf(Blocks.WATER)).save(consumer);
   }

   protected static void chestBoat(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.TRANSPORTATION, itemlike).requires(Blocks.CHEST).requires(itemlike1).group("chest_boat").unlockedBy("has_boat", has(ItemTags.BOATS)).save(consumer);
   }

   private static RecipeBuilder buttonBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, itemlike).requires(ingredient);
   }

   protected static RecipeBuilder doorBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemlike, 3).define('#', ingredient).pattern("##").pattern("##").pattern("##");
   }

   private static RecipeBuilder fenceBuilder(ItemLike itemlike, Ingredient ingredient) {
      int i = itemlike == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
      Item item = itemlike == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, i).define('W', ingredient).define('#', item).pattern("W#W").pattern("W#W");
   }

   private static RecipeBuilder fenceGateBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemlike).define('#', Items.STICK).define('W', ingredient).pattern("#W#").pattern("#W#");
   }

   protected static void pressurePlate(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      pressurePlateBuilder(RecipeCategory.REDSTONE, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   private static RecipeBuilder pressurePlateBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike).define('#', ingredient).pattern("##");
   }

   protected static void slab(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      slabBuilder(recipecategory, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static RecipeBuilder slabBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike, 6).define('#', ingredient).pattern("###");
   }

   protected static RecipeBuilder stairBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemlike, 4).define('#', ingredient).pattern("#  ").pattern("## ").pattern("###");
   }

   private static RecipeBuilder trapdoorBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, itemlike, 2).define('#', ingredient).pattern("###").pattern("###");
   }

   private static RecipeBuilder signBuilder(ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, 3).group("sign").define('#', ingredient).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
   }

   protected static void hangingSign(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, 6).group("hanging_sign").define('#', itemlike1).define('X', Items.CHAIN).pattern("X X").pattern("###").pattern("###").unlockedBy("has_stripped_logs", has(itemlike1)).save(consumer);
   }

   protected static void colorBlockWithDye(Consumer<FinishedRecipe> consumer, List<Item> list, List<Item> list1, String s) {
      for(int i = 0; i < list.size(); ++i) {
         Item item = list.get(i);
         Item item1 = list1.get(i);
         ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, item1).requires(item).requires(Ingredient.of(list1.stream().filter((item3) -> !item3.equals(item1)).map(ItemStack::new))).group(s).unlockedBy("has_needed_dye", has(item)).save(consumer, "dye_" + getItemName(item1));
      }

   }

   protected static void carpet(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, 3).define('#', itemlike1).pattern("##").group("carpet").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void bedFromPlanksAndWool(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike).define('#', itemlike1).define('X', ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void banner(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike).define('#', itemlike1).define('|', Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void stainedGlassFromGlassAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemlike, 8).define('#', Blocks.GLASS).define('X', itemlike1).pattern("###").pattern("#X#").pattern("###").group("stained_glass").unlockedBy("has_glass", has(Blocks.GLASS)).save(consumer);
   }

   protected static void stainedGlassPaneFromStainedGlass(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, 16).define('#', itemlike1).pattern("###").pattern("###").group("stained_glass_pane").unlockedBy("has_glass", has(itemlike1)).save(consumer);
   }

   protected static void stainedGlassPaneFromGlassPaneAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, itemlike, 8).define('#', Blocks.GLASS_PANE).define('$', itemlike1).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").unlockedBy("has_glass_pane", has(Blocks.GLASS_PANE)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer, getConversionRecipeName(itemlike, Blocks.GLASS_PANE));
   }

   protected static void coloredTerracottaFromTerracottaAndDye(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, itemlike, 8).define('#', Blocks.TERRACOTTA).define('X', itemlike1).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").unlockedBy("has_terracotta", has(Blocks.TERRACOTTA)).save(consumer);
   }

   protected static void concretePowder(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, itemlike, 8).requires(itemlike1).requires(Blocks.SAND, 4).requires(Blocks.GRAVEL, 4).group("concrete_powder").unlockedBy("has_sand", has(Blocks.SAND)).unlockedBy("has_gravel", has(Blocks.GRAVEL)).save(consumer);
   }

   protected static void candle(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, itemlike).requires(Blocks.CANDLE).requires(itemlike1).group("dyed_candle").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void wall(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      wallBuilder(recipecategory, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   private static RecipeBuilder wallBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike, 6).define('#', ingredient).pattern("###").pattern("###");
   }

   protected static void polished(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      polishedBuilder(recipecategory, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   private static RecipeBuilder polishedBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike, 4).define('S', ingredient).pattern("SS").pattern("SS");
   }

   protected static void cut(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      cutBuilder(recipecategory, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   private static ShapedRecipeBuilder cutBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike, 4).define('#', ingredient).pattern("##").pattern("##");
   }

   protected static void chiseled(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      chiseledBuilder(recipecategory, itemlike, Ingredient.of(itemlike1)).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void mosaicBuilder(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(recipecategory, itemlike).define('#', itemlike1).pattern("#").pattern("#").unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static ShapedRecipeBuilder chiseledBuilder(RecipeCategory recipecategory, ItemLike itemlike, Ingredient ingredient) {
      return ShapedRecipeBuilder.shaped(recipecategory, itemlike).define('#', ingredient).pattern("#").pattern("#");
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1) {
      stonecutterResultFromBase(consumer, recipecategory, itemlike, itemlike1, 1);
   }

   protected static void stonecutterResultFromBase(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, ItemLike itemlike1, int i) {
      SingleItemRecipeBuilder.stonecutting(Ingredient.of(itemlike1), recipecategory, itemlike, i).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer, getConversionRecipeName(itemlike, itemlike1) + "_stonecutting");
   }

   private static void smeltingResultFromBase(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      SimpleCookingRecipeBuilder.smelting(Ingredient.of(itemlike1), RecipeCategory.BUILDING_BLOCKS, itemlike, 0.1F, 200).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer);
   }

   protected static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, RecipeCategory recipecategory1, ItemLike itemlike1) {
      nineBlockStorageRecipes(consumer, recipecategory, itemlike, recipecategory1, itemlike1, getSimpleRecipeName(itemlike1), (String)null, getSimpleRecipeName(itemlike), (String)null);
   }

   protected static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, RecipeCategory recipecategory1, ItemLike itemlike1, String s, String s1) {
      nineBlockStorageRecipes(consumer, recipecategory, itemlike, recipecategory1, itemlike1, s, s1, getSimpleRecipeName(itemlike), (String)null);
   }

   protected static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, RecipeCategory recipecategory1, ItemLike itemlike1, String s, String s1) {
      nineBlockStorageRecipes(consumer, recipecategory, itemlike, recipecategory1, itemlike1, getSimpleRecipeName(itemlike1), (String)null, s, s1);
   }

   private static void nineBlockStorageRecipes(Consumer<FinishedRecipe> consumer, RecipeCategory recipecategory, ItemLike itemlike, RecipeCategory recipecategory1, ItemLike itemlike1, String s, @Nullable String s1, String s2, @Nullable String s3) {
      ShapelessRecipeBuilder.shapeless(recipecategory, itemlike, 9).requires(itemlike1).group(s3).unlockedBy(getHasName(itemlike1), has(itemlike1)).save(consumer, new ResourceLocation(s2));
      ShapedRecipeBuilder.shaped(recipecategory1, itemlike1).define('#', itemlike).pattern("###").pattern("###").pattern("###").group(s1).unlockedBy(getHasName(itemlike), has(itemlike)).save(consumer, new ResourceLocation(s));
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> consumer, ItemLike itemlike, TagKey<Item> tagkey) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemlike, 2).define('#', Items.DIAMOND).define('C', tagkey).define('S', itemlike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(itemlike), has(itemlike)).save(consumer);
   }

   protected static void copySmithingTemplate(Consumer<FinishedRecipe> consumer, ItemLike itemlike, ItemLike itemlike1) {
      ShapedRecipeBuilder.shaped(RecipeCategory.MISC, itemlike, 2).define('#', Items.DIAMOND).define('C', itemlike1).define('S', itemlike).pattern("#S#").pattern("#C#").pattern("###").unlockedBy(getHasName(itemlike), has(itemlike)).save(consumer);
   }

   protected static void cookRecipes(Consumer<FinishedRecipe> consumer, String s, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer, int i) {
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.BEEF, Items.COOKED_BEEF, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.COD, Items.COOKED_COD, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.KELP, Items.DRIED_KELP, 0.1F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.SALMON, Items.COOKED_SALMON, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.POTATO, Items.BAKED_POTATO, 0.35F);
      simpleCookingRecipe(consumer, s, recipeserializer, i, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
   }

   private static void simpleCookingRecipe(Consumer<FinishedRecipe> consumer, String s, RecipeSerializer<? extends AbstractCookingRecipe> recipeserializer, int i, ItemLike itemlike, ItemLike itemlike1, float f) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), RecipeCategory.FOOD, itemlike1, f, i, recipeserializer).unlockedBy(getHasName(itemlike), has(itemlike)).save(consumer, getItemName(itemlike1) + "_from_" + s);
   }

   protected static void waxRecipes(Consumer<FinishedRecipe> consumer) {
      HoneycombItem.WAXABLES.get().forEach((block, block1) -> ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block1).requires(block).requires(Items.HONEYCOMB).group(getItemName(block1)).unlockedBy(getHasName(block), has(block)).save(consumer, getConversionRecipeName(block1, Items.HONEYCOMB)));
   }

   protected static void generateRecipes(Consumer<FinishedRecipe> consumer, BlockFamily blockfamily) {
      blockfamily.getVariants().forEach((blockfamily_variant, block) -> {
         BiFunction<ItemLike, ItemLike, RecipeBuilder> bifunction = SHAPE_BUILDERS.get(blockfamily_variant);
         ItemLike itemlike = getBaseBlock(blockfamily, blockfamily_variant);
         if (bifunction != null) {
            RecipeBuilder recipebuilder = bifunction.apply(block, itemlike);
            blockfamily.getRecipeGroupPrefix().ifPresent((s) -> recipebuilder.group(s + (blockfamily_variant == BlockFamily.Variant.CUT ? "" : "_" + blockfamily_variant.getName())));
            recipebuilder.unlockedBy(blockfamily.getRecipeUnlockedBy().orElseGet(() -> getHasName(itemlike)), has(itemlike));
            recipebuilder.save(consumer);
         }

         if (blockfamily_variant == BlockFamily.Variant.CRACKED) {
            smeltingResultFromBase(consumer, block, itemlike);
         }

      });
   }

   private static Block getBaseBlock(BlockFamily blockfamily, BlockFamily.Variant blockfamily_variant) {
      if (blockfamily_variant == BlockFamily.Variant.CHISELED) {
         if (!blockfamily.getVariants().containsKey(BlockFamily.Variant.SLAB)) {
            throw new IllegalStateException("Slab is not defined for the family.");
         } else {
            return blockfamily.get(BlockFamily.Variant.SLAB);
         }
      } else {
         return blockfamily.getBaseBlock();
      }
   }

   private static EnterBlockTrigger.TriggerInstance insideOf(Block block) {
      return new EnterBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, block, StatePropertiesPredicate.ANY);
   }

   private static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints minmaxbounds_ints, ItemLike itemlike) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(itemlike).withCount(minmaxbounds_ints).build());
   }

   protected static InventoryChangeTrigger.TriggerInstance has(ItemLike itemlike) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(itemlike).build());
   }

   protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tagkey) {
      return inventoryTrigger(ItemPredicate.Builder.item().of(tagkey).build());
   }

   private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... aitempredicate) {
      return new InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, aitempredicate);
   }

   protected static String getHasName(ItemLike itemlike) {
      return "has_" + getItemName(itemlike);
   }

   protected static String getItemName(ItemLike itemlike) {
      return BuiltInRegistries.ITEM.getKey(itemlike.asItem()).getPath();
   }

   protected static String getSimpleRecipeName(ItemLike itemlike) {
      return getItemName(itemlike);
   }

   protected static String getConversionRecipeName(ItemLike itemlike, ItemLike itemlike1) {
      return getItemName(itemlike) + "_from_" + getItemName(itemlike1);
   }

   protected static String getSmeltingRecipeName(ItemLike itemlike) {
      return getItemName(itemlike) + "_from_smelting";
   }

   protected static String getBlastingRecipeName(ItemLike itemlike) {
      return getItemName(itemlike) + "_from_blasting";
   }

   public final String getName() {
      return "Recipes";
   }
}
