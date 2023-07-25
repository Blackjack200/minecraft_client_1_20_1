package net.minecraft.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Minecart extends AbstractMinecart {
   public Minecart(EntityType<?> entitytype, Level level) {
      super(entitytype, level);
   }

   public Minecart(Level level, double d0, double d1, double d2) {
      super(EntityType.MINECART, level, d0, d1, d2);
   }

   public InteractionResult interact(Player player, InteractionHand interactionhand) {
      if (player.isSecondaryUseActive()) {
         return InteractionResult.PASS;
      } else if (this.isVehicle()) {
         return InteractionResult.PASS;
      } else if (!this.level().isClientSide) {
         return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
      } else {
         return InteractionResult.SUCCESS;
      }
   }

   protected Item getDropItem() {
      return Items.MINECART;
   }

   public void activateMinecart(int i, int j, int k, boolean flag) {
      if (flag) {
         if (this.isVehicle()) {
            this.ejectPassengers();
         }

         if (this.getHurtTime() == 0) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(50.0F);
            this.markHurt();
         }
      }

   }

   public AbstractMinecart.Type getMinecartType() {
      return AbstractMinecart.Type.RIDEABLE;
   }
}
