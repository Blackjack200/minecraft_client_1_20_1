package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
   protected static final int SLOT_INPUT = 0;
   protected static final int SLOT_FUEL = 1;
   protected static final int SLOT_RESULT = 2;
   public static final int DATA_LIT_TIME = 0;
   private static final int[] SLOTS_FOR_UP = new int[]{0};
   private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
   private static final int[] SLOTS_FOR_SIDES = new int[]{1};
   public static final int DATA_LIT_DURATION = 1;
   public static final int DATA_COOKING_PROGRESS = 2;
   public static final int DATA_COOKING_TOTAL_TIME = 3;
   public static final int NUM_DATA_VALUES = 4;
   public static final int BURN_TIME_STANDARD = 200;
   public static final int BURN_COOL_SPEED = 2;
   protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
   int litTime;
   int litDuration;
   int cookingProgress;
   int cookingTotalTime;
   protected final ContainerData dataAccess = new ContainerData() {
      public int get(int i) {
         switch (i) {
            case 0:
               return AbstractFurnaceBlockEntity.this.litTime;
            case 1:
               return AbstractFurnaceBlockEntity.this.litDuration;
            case 2:
               return AbstractFurnaceBlockEntity.this.cookingProgress;
            case 3:
               return AbstractFurnaceBlockEntity.this.cookingTotalTime;
            default:
               return 0;
         }
      }

      public void set(int i, int j) {
         switch (i) {
            case 0:
               AbstractFurnaceBlockEntity.this.litTime = j;
               break;
            case 1:
               AbstractFurnaceBlockEntity.this.litDuration = j;
               break;
            case 2:
               AbstractFurnaceBlockEntity.this.cookingProgress = j;
               break;
            case 3:
               AbstractFurnaceBlockEntity.this.cookingTotalTime = j;
         }

      }

      public int getCount() {
         return 4;
      }
   };
   private final Object2IntOpenHashMap<ResourceLocation> recipesUsed = new Object2IntOpenHashMap<>();
   private final RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheck;

   protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockentitytype, BlockPos blockpos, BlockState blockstate, RecipeType<? extends AbstractCookingRecipe> recipetype) {
      super(blockentitytype, blockpos, blockstate);
      this.quickCheck = RecipeManager.createCheck(recipetype);
   }

   public static Map<Item, Integer> getFuel() {
      Map<Item, Integer> map = Maps.newLinkedHashMap();
      add(map, Items.LAVA_BUCKET, 20000);
      add(map, Blocks.COAL_BLOCK, 16000);
      add(map, Items.BLAZE_ROD, 2400);
      add(map, Items.COAL, 1600);
      add(map, Items.CHARCOAL, 1600);
      add(map, ItemTags.LOGS, 300);
      add(map, ItemTags.BAMBOO_BLOCKS, 300);
      add(map, ItemTags.PLANKS, 300);
      add(map, Blocks.BAMBOO_MOSAIC, 300);
      add(map, ItemTags.WOODEN_STAIRS, 300);
      add(map, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
      add(map, ItemTags.WOODEN_SLABS, 150);
      add(map, Blocks.BAMBOO_MOSAIC_SLAB, 150);
      add(map, ItemTags.WOODEN_TRAPDOORS, 300);
      add(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
      add(map, ItemTags.WOODEN_FENCES, 300);
      add(map, ItemTags.FENCE_GATES, 300);
      add(map, Blocks.NOTE_BLOCK, 300);
      add(map, Blocks.BOOKSHELF, 300);
      add(map, Blocks.CHISELED_BOOKSHELF, 300);
      add(map, Blocks.LECTERN, 300);
      add(map, Blocks.JUKEBOX, 300);
      add(map, Blocks.CHEST, 300);
      add(map, Blocks.TRAPPED_CHEST, 300);
      add(map, Blocks.CRAFTING_TABLE, 300);
      add(map, Blocks.DAYLIGHT_DETECTOR, 300);
      add(map, ItemTags.BANNERS, 300);
      add(map, Items.BOW, 300);
      add(map, Items.FISHING_ROD, 300);
      add(map, Blocks.LADDER, 300);
      add(map, ItemTags.SIGNS, 200);
      add(map, ItemTags.HANGING_SIGNS, 800);
      add(map, Items.WOODEN_SHOVEL, 200);
      add(map, Items.WOODEN_SWORD, 200);
      add(map, Items.WOODEN_HOE, 200);
      add(map, Items.WOODEN_AXE, 200);
      add(map, Items.WOODEN_PICKAXE, 200);
      add(map, ItemTags.WOODEN_DOORS, 200);
      add(map, ItemTags.BOATS, 1200);
      add(map, ItemTags.WOOL, 100);
      add(map, ItemTags.WOODEN_BUTTONS, 100);
      add(map, Items.STICK, 100);
      add(map, ItemTags.SAPLINGS, 100);
      add(map, Items.BOWL, 100);
      add(map, ItemTags.WOOL_CARPETS, 67);
      add(map, Blocks.DRIED_KELP_BLOCK, 4001);
      add(map, Items.CROSSBOW, 300);
      add(map, Blocks.BAMBOO, 50);
      add(map, Blocks.DEAD_BUSH, 100);
      add(map, Blocks.SCAFFOLDING, 50);
      add(map, Blocks.LOOM, 300);
      add(map, Blocks.BARREL, 300);
      add(map, Blocks.CARTOGRAPHY_TABLE, 300);
      add(map, Blocks.FLETCHING_TABLE, 300);
      add(map, Blocks.SMITHING_TABLE, 300);
      add(map, Blocks.COMPOSTER, 300);
      add(map, Blocks.AZALEA, 100);
      add(map, Blocks.FLOWERING_AZALEA, 100);
      add(map, Blocks.MANGROVE_ROOTS, 300);
      return map;
   }

   private static boolean isNeverAFurnaceFuel(Item item) {
      return item.builtInRegistryHolder().is(ItemTags.NON_FLAMMABLE_WOOD);
   }

   private static void add(Map<Item, Integer> map, TagKey<Item> tagkey, int i) {
      for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagkey)) {
         if (!isNeverAFurnaceFuel(holder.value())) {
            map.put(holder.value(), i);
         }
      }

   }

   private static void add(Map<Item, Integer> map, ItemLike itemlike, int i) {
      Item item = itemlike.asItem();
      if (isNeverAFurnaceFuel(item)) {
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("A developer tried to explicitly make fire resistant item " + item.getName((ItemStack)null).getString() + " a furnace fuel. That will not work!"));
         }
      } else {
         map.put(item, i);
      }
   }

   private boolean isLit() {
      return this.litTime > 0;
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compoundtag, this.items);
      this.litTime = compoundtag.getShort("BurnTime");
      this.cookingProgress = compoundtag.getShort("CookTime");
      this.cookingTotalTime = compoundtag.getShort("CookTimeTotal");
      this.litDuration = this.getBurnDuration(this.items.get(1));
      CompoundTag compoundtag1 = compoundtag.getCompound("RecipesUsed");

      for(String s : compoundtag1.getAllKeys()) {
         this.recipesUsed.put(new ResourceLocation(s), compoundtag1.getInt(s));
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.putShort("BurnTime", (short)this.litTime);
      compoundtag.putShort("CookTime", (short)this.cookingProgress);
      compoundtag.putShort("CookTimeTotal", (short)this.cookingTotalTime);
      ContainerHelper.saveAllItems(compoundtag, this.items);
      CompoundTag compoundtag1 = new CompoundTag();
      this.recipesUsed.forEach((resourcelocation, integer) -> compoundtag1.putInt(resourcelocation.toString(), integer));
      compoundtag.put("RecipesUsed", compoundtag1);
   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, AbstractFurnaceBlockEntity abstractfurnaceblockentity) {
      boolean flag = abstractfurnaceblockentity.isLit();
      boolean flag1 = false;
      if (abstractfurnaceblockentity.isLit()) {
         --abstractfurnaceblockentity.litTime;
      }

      ItemStack itemstack = abstractfurnaceblockentity.items.get(1);
      boolean flag2 = !abstractfurnaceblockentity.items.get(0).isEmpty();
      boolean flag3 = !itemstack.isEmpty();
      if (abstractfurnaceblockentity.isLit() || flag3 && flag2) {
         Recipe<?> recipe;
         if (flag2) {
            recipe = abstractfurnaceblockentity.quickCheck.getRecipeFor(abstractfurnaceblockentity, level).orElse((AbstractCookingRecipe)null);
         } else {
            recipe = null;
         }

         int i = abstractfurnaceblockentity.getMaxStackSize();
         if (!abstractfurnaceblockentity.isLit() && canBurn(level.registryAccess(), recipe, abstractfurnaceblockentity.items, i)) {
            abstractfurnaceblockentity.litTime = abstractfurnaceblockentity.getBurnDuration(itemstack);
            abstractfurnaceblockentity.litDuration = abstractfurnaceblockentity.litTime;
            if (abstractfurnaceblockentity.isLit()) {
               flag1 = true;
               if (flag3) {
                  Item item = itemstack.getItem();
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     Item item1 = item.getCraftingRemainingItem();
                     abstractfurnaceblockentity.items.set(1, item1 == null ? ItemStack.EMPTY : new ItemStack(item1));
                  }
               }
            }
         }

         if (abstractfurnaceblockentity.isLit() && canBurn(level.registryAccess(), recipe, abstractfurnaceblockentity.items, i)) {
            ++abstractfurnaceblockentity.cookingProgress;
            if (abstractfurnaceblockentity.cookingProgress == abstractfurnaceblockentity.cookingTotalTime) {
               abstractfurnaceblockentity.cookingProgress = 0;
               abstractfurnaceblockentity.cookingTotalTime = getTotalCookTime(level, abstractfurnaceblockentity);
               if (burn(level.registryAccess(), recipe, abstractfurnaceblockentity.items, i)) {
                  abstractfurnaceblockentity.setRecipeUsed(recipe);
               }

               flag1 = true;
            }
         } else {
            abstractfurnaceblockentity.cookingProgress = 0;
         }
      } else if (!abstractfurnaceblockentity.isLit() && abstractfurnaceblockentity.cookingProgress > 0) {
         abstractfurnaceblockentity.cookingProgress = Mth.clamp(abstractfurnaceblockentity.cookingProgress - 2, 0, abstractfurnaceblockentity.cookingTotalTime);
      }

      if (flag != abstractfurnaceblockentity.isLit()) {
         flag1 = true;
         blockstate = blockstate.setValue(AbstractFurnaceBlock.LIT, Boolean.valueOf(abstractfurnaceblockentity.isLit()));
         level.setBlock(blockpos, blockstate, 3);
      }

      if (flag1) {
         setChanged(level, blockpos, blockstate);
      }

   }

   private static boolean canBurn(RegistryAccess registryaccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonnulllist, int i) {
      if (!nonnulllist.get(0).isEmpty() && recipe != null) {
         ItemStack itemstack = recipe.getResultItem(registryaccess);
         if (itemstack.isEmpty()) {
            return false;
         } else {
            ItemStack itemstack1 = nonnulllist.get(2);
            if (itemstack1.isEmpty()) {
               return true;
            } else if (!ItemStack.isSameItem(itemstack1, itemstack)) {
               return false;
            } else if (itemstack1.getCount() < i && itemstack1.getCount() < itemstack1.getMaxStackSize()) {
               return true;
            } else {
               return itemstack1.getCount() < itemstack.getMaxStackSize();
            }
         }
      } else {
         return false;
      }
   }

   private static boolean burn(RegistryAccess registryaccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonnulllist, int i) {
      if (recipe != null && canBurn(registryaccess, recipe, nonnulllist, i)) {
         ItemStack itemstack = nonnulllist.get(0);
         ItemStack itemstack1 = recipe.getResultItem(registryaccess);
         ItemStack itemstack2 = nonnulllist.get(2);
         if (itemstack2.isEmpty()) {
            nonnulllist.set(2, itemstack1.copy());
         } else if (itemstack2.is(itemstack1.getItem())) {
            itemstack2.grow(1);
         }

         if (itemstack.is(Blocks.WET_SPONGE.asItem()) && !nonnulllist.get(1).isEmpty() && nonnulllist.get(1).is(Items.BUCKET)) {
            nonnulllist.set(1, new ItemStack(Items.WATER_BUCKET));
         }

         itemstack.shrink(1);
         return true;
      } else {
         return false;
      }
   }

   protected int getBurnDuration(ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return 0;
      } else {
         Item item = itemstack.getItem();
         return getFuel().getOrDefault(item, 0);
      }
   }

   private static int getTotalCookTime(Level level, AbstractFurnaceBlockEntity abstractfurnaceblockentity) {
      return abstractfurnaceblockentity.quickCheck.getRecipeFor(abstractfurnaceblockentity, level).map(AbstractCookingRecipe::getCookingTime).orElse(200);
   }

   public static boolean isFuel(ItemStack itemstack) {
      return getFuel().containsKey(itemstack.getItem());
   }

   public int[] getSlotsForFace(Direction direction) {
      if (direction == Direction.DOWN) {
         return SLOTS_FOR_DOWN;
      } else {
         return direction == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
      }
   }

   public boolean canPlaceItemThroughFace(int i, ItemStack itemstack, @Nullable Direction direction) {
      return this.canPlaceItem(i, itemstack);
   }

   public boolean canTakeItemThroughFace(int i, ItemStack itemstack, Direction direction) {
      if (direction == Direction.DOWN && i == 1) {
         return itemstack.is(Items.WATER_BUCKET) || itemstack.is(Items.BUCKET);
      } else {
         return true;
      }
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int i) {
      return this.items.get(i);
   }

   public ItemStack removeItem(int i, int j) {
      return ContainerHelper.removeItem(this.items, i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return ContainerHelper.takeItem(this.items, i);
   }

   public void setItem(int i, ItemStack itemstack) {
      ItemStack itemstack1 = this.items.get(i);
      boolean flag = !itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemstack1, itemstack);
      this.items.set(i, itemstack);
      if (itemstack.getCount() > this.getMaxStackSize()) {
         itemstack.setCount(this.getMaxStackSize());
      }

      if (i == 0 && !flag) {
         this.cookingTotalTime = getTotalCookTime(this.level, this);
         this.cookingProgress = 0;
         this.setChanged();
      }

   }

   public boolean stillValid(Player player) {
      return Container.stillValidBlockEntity(this, player);
   }

   public boolean canPlaceItem(int i, ItemStack itemstack) {
      if (i == 2) {
         return false;
      } else if (i != 1) {
         return true;
      } else {
         ItemStack itemstack1 = this.items.get(1);
         return isFuel(itemstack) || itemstack.is(Items.BUCKET) && !itemstack1.is(Items.BUCKET);
      }
   }

   public void clearContent() {
      this.items.clear();
   }

   public void setRecipeUsed(@Nullable Recipe<?> recipe) {
      if (recipe != null) {
         ResourceLocation resourcelocation = recipe.getId();
         this.recipesUsed.addTo(resourcelocation, 1);
      }

   }

   @Nullable
   public Recipe<?> getRecipeUsed() {
      return null;
   }

   public void awardUsedRecipes(Player player, List<ItemStack> list) {
   }

   public void awardUsedRecipesAndPopExperience(ServerPlayer serverplayer) {
      List<Recipe<?>> list = this.getRecipesToAwardAndPopExperience(serverplayer.serverLevel(), serverplayer.position());
      serverplayer.awardRecipes(list);

      for(Recipe<?> recipe : list) {
         if (recipe != null) {
            serverplayer.triggerRecipeCrafted(recipe, this.items);
         }
      }

      this.recipesUsed.clear();
   }

   public List<Recipe<?>> getRecipesToAwardAndPopExperience(ServerLevel serverlevel, Vec3 vec3) {
      List<Recipe<?>> list = Lists.newArrayList();

      for(Object2IntMap.Entry<ResourceLocation> object2intmap_entry : this.recipesUsed.object2IntEntrySet()) {
         serverlevel.getRecipeManager().byKey(object2intmap_entry.getKey()).ifPresent((recipe) -> {
            list.add(recipe);
            createExperience(serverlevel, vec3, object2intmap_entry.getIntValue(), ((AbstractCookingRecipe)recipe).getExperience());
         });
      }

      return list;
   }

   private static void createExperience(ServerLevel serverlevel, Vec3 vec3, int i, float f) {
      int j = Mth.floor((float)i * f);
      float f1 = Mth.frac((float)i * f);
      if (f1 != 0.0F && Math.random() < (double)f1) {
         ++j;
      }

      ExperienceOrb.award(serverlevel, vec3, j);
   }

   public void fillStackedContents(StackedContents stackedcontents) {
      for(ItemStack itemstack : this.items) {
         stackedcontents.accountStack(itemstack);
      }

   }
}
