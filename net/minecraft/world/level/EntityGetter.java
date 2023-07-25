package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
   List<Entity> getEntities(@Nullable Entity entity, AABB aabb, Predicate<? super Entity> predicate);

   <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AABB aabb, Predicate<? super T> predicate);

   default <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AABB aabb, Predicate<? super T> predicate) {
      return this.getEntities(EntityTypeTest.forClass(oclass), aabb, predicate);
   }

   List<? extends Player> players();

   default List<Entity> getEntities(@Nullable Entity entity, AABB aabb) {
      return this.getEntities(entity, aabb, EntitySelector.NO_SPECTATORS);
   }

   default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelshape) {
      if (voxelshape.isEmpty()) {
         return true;
      } else {
         for(Entity entity1 : this.getEntities(entity, voxelshape.bounds())) {
            if (!entity1.isRemoved() && entity1.blocksBuilding && (entity == null || !entity1.isPassengerOfSameVehicle(entity)) && Shapes.joinIsNotEmpty(voxelshape, Shapes.create(entity1.getBoundingBox()), BooleanOp.AND)) {
               return false;
            }
         }

         return true;
      }
   }

   default <T extends Entity> List<T> getEntitiesOfClass(Class<T> oclass, AABB aabb) {
      return this.getEntitiesOfClass(oclass, aabb, EntitySelector.NO_SPECTATORS);
   }

   default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb) {
      if (aabb.getSize() < 1.0E-7D) {
         return List.of();
      } else {
         Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
         List<Entity> list = this.getEntities(entity, aabb.inflate(1.0E-7D), predicate);
         if (list.isEmpty()) {
            return List.of();
         } else {
            ImmutableList.Builder<VoxelShape> immutablelist_builder = ImmutableList.builderWithExpectedSize(list.size());

            for(Entity entity1 : list) {
               immutablelist_builder.add(Shapes.create(entity1.getBoundingBox()));
            }

            return immutablelist_builder.build();
         }
      }
   }

   @Nullable
   default Player getNearestPlayer(double d0, double d1, double d2, double d3, @Nullable Predicate<Entity> predicate) {
      double d4 = -1.0D;
      Player player = null;

      for(Player player1 : this.players()) {
         if (predicate == null || predicate.test(player1)) {
            double d5 = player1.distanceToSqr(d0, d1, d2);
            if ((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1.0D || d5 < d4)) {
               d4 = d5;
               player = player1;
            }
         }
      }

      return player;
   }

   @Nullable
   default Player getNearestPlayer(Entity entity, double d0) {
      return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d0, false);
   }

   @Nullable
   default Player getNearestPlayer(double d0, double d1, double d2, double d3, boolean flag) {
      Predicate<Entity> predicate = flag ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
      return this.getNearestPlayer(d0, d1, d2, d3, predicate);
   }

   default boolean hasNearbyAlivePlayer(double d0, double d1, double d2, double d3) {
      for(Player player : this.players()) {
         if (EntitySelector.NO_SPECTATORS.test(player) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) {
            double d4 = player.distanceToSqr(d0, d1, d2);
            if (d3 < 0.0D || d4 < d3 * d3) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions targetingconditions, LivingEntity livingentity) {
      return this.getNearestEntity(this.players(), targetingconditions, livingentity, livingentity.getX(), livingentity.getY(), livingentity.getZ());
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions targetingconditions, LivingEntity livingentity, double d0, double d1, double d2) {
      return this.getNearestEntity(this.players(), targetingconditions, livingentity, d0, d1, d2);
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions targetingconditions, double d0, double d1, double d2) {
      return this.getNearestEntity(this.players(), targetingconditions, (LivingEntity)null, d0, d1, d2);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(Class<? extends T> oclass, TargetingConditions targetingconditions, @Nullable LivingEntity livingentity, double d0, double d1, double d2, AABB aabb) {
      return this.getNearestEntity(this.getEntitiesOfClass(oclass, aabb, (livingentity1) -> true), targetingconditions, livingentity, d0, d1, d2);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(List<? extends T> list, TargetingConditions targetingconditions, @Nullable LivingEntity livingentity, double d0, double d1, double d2) {
      double d3 = -1.0D;
      T livingentity1 = null;

      for(T livingentity2 : list) {
         if (targetingconditions.test(livingentity, livingentity2)) {
            double d4 = livingentity2.distanceToSqr(d0, d1, d2);
            if (d3 == -1.0D || d4 < d3) {
               d3 = d4;
               livingentity1 = livingentity2;
            }
         }
      }

      return livingentity1;
   }

   default List<Player> getNearbyPlayers(TargetingConditions targetingconditions, LivingEntity livingentity, AABB aabb) {
      List<Player> list = Lists.newArrayList();

      for(Player player : this.players()) {
         if (aabb.contains(player.getX(), player.getY(), player.getZ()) && targetingconditions.test(livingentity, player)) {
            list.add(player);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> oclass, TargetingConditions targetingconditions, LivingEntity livingentity, AABB aabb) {
      List<T> list = this.getEntitiesOfClass(oclass, aabb, (livingentity2) -> true);
      List<T> list1 = Lists.newArrayList();

      for(T livingentity1 : list) {
         if (targetingconditions.test(livingentity, livingentity1)) {
            list1.add(livingentity1);
         }
      }

      return list1;
   }

   @Nullable
   default Player getPlayerByUUID(UUID uuid) {
      for(int i = 0; i < this.players().size(); ++i) {
         Player player = this.players().get(i);
         if (uuid.equals(player.getUUID())) {
            return player;
         }
      }

      return null;
   }
}
