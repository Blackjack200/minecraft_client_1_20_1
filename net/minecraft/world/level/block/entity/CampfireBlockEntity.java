package net.minecraft.world.level.block.entity;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CampfireBlockEntity extends BlockEntity implements Clearable {
   private static final int BURN_COOL_SPEED = 2;
   private static final int NUM_SLOTS = 4;
   private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
   private final int[] cookingProgress = new int[4];
   private final int[] cookingTime = new int[4];
   private final RecipeManager.CachedCheck<Container, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

   public CampfireBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.CAMPFIRE, blockpos, blockstate);
   }

   public static void cookTick(Level level, BlockPos blockpos, BlockState blockstate, CampfireBlockEntity campfireblockentity) {
      boolean flag = false;

      for(int i = 0; i < campfireblockentity.items.size(); ++i) {
         ItemStack itemstack = campfireblockentity.items.get(i);
         if (!itemstack.isEmpty()) {
            flag = true;
            int var10002 = campfireblockentity.cookingProgress[i]++;
            if (campfireblockentity.cookingProgress[i] >= campfireblockentity.cookingTime[i]) {
               Container container = new SimpleContainer(itemstack);
               ItemStack itemstack1 = campfireblockentity.quickCheck.getRecipeFor(container, level).map((campfirecookingrecipe) -> campfirecookingrecipe.assemble(container, level.registryAccess())).orElse(itemstack);
               if (itemstack1.isItemEnabled(level.enabledFeatures())) {
                  Containers.dropItemStack(level, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), itemstack1);
                  campfireblockentity.items.set(i, ItemStack.EMPTY);
                  level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                  level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(blockstate));
               }
            }
         }
      }

      if (flag) {
         setChanged(level, blockpos, blockstate);
      }

   }

   public static void cooldownTick(Level level, BlockPos blockpos, BlockState blockstate, CampfireBlockEntity campfireblockentity) {
      boolean flag = false;

      for(int i = 0; i < campfireblockentity.items.size(); ++i) {
         if (campfireblockentity.cookingProgress[i] > 0) {
            flag = true;
            campfireblockentity.cookingProgress[i] = Mth.clamp(campfireblockentity.cookingProgress[i] - 2, 0, campfireblockentity.cookingTime[i]);
         }
      }

      if (flag) {
         setChanged(level, blockpos, blockstate);
      }

   }

   public static void particleTick(Level level, BlockPos blockpos, BlockState blockstate, CampfireBlockEntity campfireblockentity) {
      RandomSource randomsource = level.random;
      if (randomsource.nextFloat() < 0.11F) {
         for(int i = 0; i < randomsource.nextInt(2) + 2; ++i) {
            CampfireBlock.makeParticles(level, blockpos, blockstate.getValue(CampfireBlock.SIGNAL_FIRE), false);
         }
      }

      int j = blockstate.getValue(CampfireBlock.FACING).get2DDataValue();

      for(int k = 0; k < campfireblockentity.items.size(); ++k) {
         if (!campfireblockentity.items.get(k).isEmpty() && randomsource.nextFloat() < 0.2F) {
            Direction direction = Direction.from2DDataValue(Math.floorMod(k + j, 4));
            float f = 0.3125F;
            double d0 = (double)blockpos.getX() + 0.5D - (double)((float)direction.getStepX() * 0.3125F) + (double)((float)direction.getClockWise().getStepX() * 0.3125F);
            double d1 = (double)blockpos.getY() + 0.5D;
            double d2 = (double)blockpos.getZ() + 0.5D - (double)((float)direction.getStepZ() * 0.3125F) + (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

            for(int l = 0; l < 4; ++l) {
               level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 5.0E-4D, 0.0D);
            }
         }
      }

   }

   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.items.clear();
      ContainerHelper.loadAllItems(compoundtag, this.items);
      if (compoundtag.contains("CookingTimes", 11)) {
         int[] aint = compoundtag.getIntArray("CookingTimes");
         System.arraycopy(aint, 0, this.cookingProgress, 0, Math.min(this.cookingTime.length, aint.length));
      }

      if (compoundtag.contains("CookingTotalTimes", 11)) {
         int[] aint1 = compoundtag.getIntArray("CookingTotalTimes");
         System.arraycopy(aint1, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, aint1.length));
      }

   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      ContainerHelper.saveAllItems(compoundtag, this.items, true);
      compoundtag.putIntArray("CookingTimes", this.cookingProgress);
      compoundtag.putIntArray("CookingTotalTimes", this.cookingTime);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag compoundtag = new CompoundTag();
      ContainerHelper.saveAllItems(compoundtag, this.items, true);
      return compoundtag;
   }

   public Optional<CampfireCookingRecipe> getCookableRecipe(ItemStack itemstack) {
      return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SimpleContainer(itemstack), this.level);
   }

   public boolean placeFood(@Nullable Entity entity, ItemStack itemstack, int i) {
      for(int j = 0; j < this.items.size(); ++j) {
         ItemStack itemstack1 = this.items.get(j);
         if (itemstack1.isEmpty()) {
            this.cookingTime[j] = i;
            this.cookingProgress[j] = 0;
            this.items.set(j, itemstack.split(1));
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
            this.markUpdated();
            return true;
         }
      }

      return false;
   }

   private void markUpdated() {
      this.setChanged();
      this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
   }

   public void clearContent() {
      this.items.clear();
   }

   public void dowse() {
      if (this.level != null) {
         this.markUpdated();
      }

   }
}
