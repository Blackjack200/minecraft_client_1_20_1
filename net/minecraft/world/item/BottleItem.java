package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BottleItem extends Item {
   public BottleItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      List<AreaEffectCloud> list = level.getEntitiesOfClass(AreaEffectCloud.class, player.getBoundingBox().inflate(2.0D), (areaeffectcloud1) -> areaeffectcloud1 != null && areaeffectcloud1.isAlive() && areaeffectcloud1.getOwner() instanceof EnderDragon);
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (!list.isEmpty()) {
         AreaEffectCloud areaeffectcloud = list.get(0);
         areaeffectcloud.setRadius(areaeffectcloud.getRadius() - 0.5F);
         level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
         level.gameEvent(player, GameEvent.FLUID_PICKUP, player.position());
         if (player instanceof ServerPlayer) {
            ServerPlayer serverplayer = (ServerPlayer)player;
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverplayer, itemstack, areaeffectcloud);
         }

         return InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, player, new ItemStack(Items.DRAGON_BREATH)), level.isClientSide());
      } else {
         BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
         if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
         } else {
            if (blockhitresult.getType() == HitResult.Type.BLOCK) {
               BlockPos blockpos = blockhitresult.getBlockPos();
               if (!level.mayInteract(player, blockpos)) {
                  return InteractionResultHolder.pass(itemstack);
               }

               if (level.getFluidState(blockpos).is(FluidTags.WATER)) {
                  level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                  level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
                  return InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), level.isClientSide());
               }
            }

            return InteractionResultHolder.pass(itemstack);
         }
      }
   }

   protected ItemStack turnBottleIntoItem(ItemStack itemstack, Player player, ItemStack itemstack1) {
      player.awardStat(Stats.ITEM_USED.get(this));
      return ItemUtils.createFilledResult(itemstack, player, itemstack1);
   }
}
