package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;

public class SaddleItem extends Item {
   public SaddleItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult interactLivingEntity(ItemStack itemstack, Player player, LivingEntity livingentity, InteractionHand interactionhand) {
      if (livingentity instanceof Saddleable saddleable && livingentity.isAlive()) {
         if (!saddleable.isSaddled() && saddleable.isSaddleable()) {
            if (!player.level().isClientSide) {
               saddleable.equipSaddle(SoundSource.NEUTRAL);
               livingentity.level().gameEvent(livingentity, GameEvent.EQUIP, livingentity.position());
               itemstack.shrink(1);
            }

            return InteractionResult.sidedSuccess(player.level().isClientSide);
         }
      }

      return InteractionResult.PASS;
   }
}
