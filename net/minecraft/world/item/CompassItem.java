package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

public class CompassItem extends Item implements Vanishable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_LODESTONE_POS = "LodestonePos";
   public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
   public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

   public CompassItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public static boolean isLodestoneCompass(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null && (compoundtag.contains("LodestoneDimension") || compoundtag.contains("LodestonePos"));
   }

   private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag compoundtag) {
      return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundtag.get("LodestoneDimension")).result();
   }

   @Nullable
   public static GlobalPos getLodestonePosition(CompoundTag compoundtag) {
      boolean flag = compoundtag.contains("LodestonePos");
      boolean flag1 = compoundtag.contains("LodestoneDimension");
      if (flag && flag1) {
         Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundtag);
         if (optional.isPresent()) {
            BlockPos blockpos = NbtUtils.readBlockPos(compoundtag.getCompound("LodestonePos"));
            return GlobalPos.of(optional.get(), blockpos);
         }
      }

      return null;
   }

   @Nullable
   public static GlobalPos getSpawnPosition(Level level) {
      return level.dimensionType().natural() ? GlobalPos.of(level.dimension(), level.getSharedSpawnPos()) : null;
   }

   public boolean isFoil(ItemStack itemstack) {
      return isLodestoneCompass(itemstack) || super.isFoil(itemstack);
   }

   public void inventoryTick(ItemStack itemstack, Level level, Entity entity, int i, boolean flag) {
      if (!level.isClientSide) {
         if (isLodestoneCompass(itemstack)) {
            CompoundTag compoundtag = itemstack.getOrCreateTag();
            if (compoundtag.contains("LodestoneTracked") && !compoundtag.getBoolean("LodestoneTracked")) {
               return;
            }

            Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundtag);
            if (optional.isPresent() && optional.get() == level.dimension() && compoundtag.contains("LodestonePos")) {
               BlockPos blockpos = NbtUtils.readBlockPos(compoundtag.getCompound("LodestonePos"));
               if (!level.isInWorldBounds(blockpos) || !((ServerLevel)level).getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockpos)) {
                  compoundtag.remove("LodestonePos");
               }
            }
         }

      }
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      BlockPos blockpos = useoncontext.getClickedPos();
      Level level = useoncontext.getLevel();
      if (!level.getBlockState(blockpos).is(Blocks.LODESTONE)) {
         return super.useOn(useoncontext);
      } else {
         level.playSound((Player)null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
         Player player = useoncontext.getPlayer();
         ItemStack itemstack = useoncontext.getItemInHand();
         boolean flag = !player.getAbilities().instabuild && itemstack.getCount() == 1;
         if (flag) {
            this.addLodestoneTags(level.dimension(), blockpos, itemstack.getOrCreateTag());
         } else {
            ItemStack itemstack1 = new ItemStack(Items.COMPASS, 1);
            CompoundTag compoundtag = itemstack.hasTag() ? itemstack.getTag().copy() : new CompoundTag();
            itemstack1.setTag(compoundtag);
            if (!player.getAbilities().instabuild) {
               itemstack.shrink(1);
            }

            this.addLodestoneTags(level.dimension(), blockpos, compoundtag);
            if (!player.getInventory().add(itemstack1)) {
               player.drop(itemstack1, false);
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   private void addLodestoneTags(ResourceKey<Level> resourcekey, BlockPos blockpos, CompoundTag compoundtag) {
      compoundtag.put("LodestonePos", NbtUtils.writeBlockPos(blockpos));
      Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, resourcekey).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("LodestoneDimension", tag));
      compoundtag.putBoolean("LodestoneTracked", true);
   }

   public String getDescriptionId(ItemStack itemstack) {
      return isLodestoneCompass(itemstack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemstack);
   }
}
