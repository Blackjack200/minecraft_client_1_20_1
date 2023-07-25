package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

public final class EntitySelector {
   public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
   public static final Predicate<Entity> LIVING_ENTITY_STILL_ALIVE = (entity) -> entity.isAlive() && entity instanceof LivingEntity;
   public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = (entity) -> entity.isAlive() && !entity.isVehicle() && !entity.isPassenger();
   public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = (entity) -> entity instanceof Container && entity.isAlive();
   public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = (entity) -> !(entity instanceof Player) || !entity.isSpectator() && !((Player)entity).isCreative();
   public static final Predicate<Entity> NO_SPECTATORS = (entity) -> !entity.isSpectator();
   public static final Predicate<Entity> CAN_BE_COLLIDED_WITH = NO_SPECTATORS.and(Entity::canBeCollidedWith);

   private EntitySelector() {
   }

   public static Predicate<Entity> withinDistance(double d0, double d1, double d2, double d3) {
      double d4 = d3 * d3;
      return (entity) -> entity != null && entity.distanceToSqr(d0, d1, d2) <= d4;
   }

   public static Predicate<Entity> pushableBy(Entity entity) {
      Team team = entity.getTeam();
      Team.CollisionRule team_collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
      return (Predicate<Entity>)(team_collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : NO_SPECTATORS.and((entity2) -> {
         if (!entity2.isPushable()) {
            return false;
         } else if (!entity.level().isClientSide || entity2 instanceof Player && ((Player)entity2).isLocalPlayer()) {
            Team team2 = entity2.getTeam();
            Team.CollisionRule team_collisionrule2 = team2 == null ? Team.CollisionRule.ALWAYS : team2.getCollisionRule();
            if (team_collisionrule2 == Team.CollisionRule.NEVER) {
               return false;
            } else {
               boolean flag = team != null && team.isAlliedTo(team2);
               if ((team_collisionrule == Team.CollisionRule.PUSH_OWN_TEAM || team_collisionrule2 == Team.CollisionRule.PUSH_OWN_TEAM) && flag) {
                  return false;
               } else {
                  return team_collisionrule != Team.CollisionRule.PUSH_OTHER_TEAMS && team_collisionrule2 != Team.CollisionRule.PUSH_OTHER_TEAMS || flag;
               }
            }
         } else {
            return false;
         }
      }));
   }

   public static Predicate<Entity> notRiding(Entity entity) {
      return (entity2) -> {
         while(true) {
            if (entity2.isPassenger()) {
               entity2 = entity2.getVehicle();
               if (entity2 != entity) {
                  continue;
               }

               return false;
            }

            return true;
         }
      };
   }

   public static class MobCanWearArmorEntitySelector implements Predicate<Entity> {
      private final ItemStack itemStack;

      public MobCanWearArmorEntitySelector(ItemStack itemstack) {
         this.itemStack = itemstack;
      }

      public boolean test(@Nullable Entity entity) {
         if (!entity.isAlive()) {
            return false;
         } else if (!(entity instanceof LivingEntity)) {
            return false;
         } else {
            LivingEntity livingentity = (LivingEntity)entity;
            return livingentity.canTakeItem(this.itemStack);
         }
      }
   }
}
