package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem extends BucketItem {
   private final EntityType<?> type;
   private final SoundEvent emptySound;

   public MobBucketItem(EntityType<?> entitytype, Fluid fluid, SoundEvent soundevent, Item.Properties item_properties) {
      super(fluid, item_properties);
      this.type = entitytype;
      this.emptySound = soundevent;
   }

   public void checkExtraContent(@Nullable Player player, Level level, ItemStack itemstack, BlockPos blockpos) {
      if (level instanceof ServerLevel) {
         this.spawn((ServerLevel)level, itemstack, blockpos);
         level.gameEvent(player, GameEvent.ENTITY_PLACE, blockpos);
      }

   }

   protected void playEmptySound(@Nullable Player player, LevelAccessor levelaccessor, BlockPos blockpos) {
      levelaccessor.playSound(player, blockpos, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
   }

   private void spawn(ServerLevel serverlevel, ItemStack itemstack, BlockPos blockpos) {
      Entity entity = this.type.spawn(serverlevel, itemstack, (Player)null, blockpos, MobSpawnType.BUCKET, true, false);
      if (entity instanceof Bucketable bucketable) {
         bucketable.loadFromBucketTag(itemstack.getOrCreateTag());
         bucketable.setFromBucket(true);
      }

   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      if (this.type == EntityType.TROPICAL_FISH) {
         CompoundTag compoundtag = itemstack.getTag();
         if (compoundtag != null && compoundtag.contains("BucketVariantTag", 3)) {
            int i = compoundtag.getInt("BucketVariantTag");
            ChatFormatting[] achatformatting = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
            String s = "color.minecraft." + TropicalFish.getBaseColor(i);
            String s1 = "color.minecraft." + TropicalFish.getPatternColor(i);

            for(int j = 0; j < TropicalFish.COMMON_VARIANTS.size(); ++j) {
               if (i == TropicalFish.COMMON_VARIANTS.get(j).getPackedId()) {
                  list.add(Component.translatable(TropicalFish.getPredefinedName(j)).withStyle(achatformatting));
                  return;
               }
            }

            list.add(TropicalFish.getPattern(i).displayName().plainCopy().withStyle(achatformatting));
            MutableComponent mutablecomponent = Component.translatable(s);
            if (!s.equals(s1)) {
               mutablecomponent.append(", ").append(Component.translatable(s1));
            }

            mutablecomponent.withStyle(achatformatting);
            list.add(mutablecomponent);
         }
      }

   }
}
