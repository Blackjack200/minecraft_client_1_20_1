package net.minecraft.commands.arguments.selector;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelector {
   public static final int INFINITE = Integer.MAX_VALUE;
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = (vec3, list) -> {
   };
   private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>() {
      public Entity tryCast(Entity entity) {
         return entity;
      }

      public Class<? extends Entity> getBaseClass() {
         return Entity.class;
      }
   };
   private final int maxResults;
   private final boolean includesEntities;
   private final boolean worldLimited;
   private final Predicate<Entity> predicate;
   private final MinMaxBounds.Doubles range;
   private final Function<Vec3, Vec3> position;
   @Nullable
   private final AABB aabb;
   private final BiConsumer<Vec3, List<? extends Entity>> order;
   private final boolean currentEntity;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID entityUUID;
   private final EntityTypeTest<Entity, ?> type;
   private final boolean usesSelector;

   public EntitySelector(int i, boolean flag, boolean flag1, Predicate<Entity> predicate, MinMaxBounds.Doubles minmaxbounds_doubles, Function<Vec3, Vec3> function, @Nullable AABB aabb, BiConsumer<Vec3, List<? extends Entity>> biconsumer, boolean flag2, @Nullable String s, @Nullable UUID uuid, @Nullable EntityType<?> entitytype, boolean flag3) {
      this.maxResults = i;
      this.includesEntities = flag;
      this.worldLimited = flag1;
      this.predicate = predicate;
      this.range = minmaxbounds_doubles;
      this.position = function;
      this.aabb = aabb;
      this.order = biconsumer;
      this.currentEntity = flag2;
      this.playerName = s;
      this.entityUUID = uuid;
      this.type = (EntityTypeTest<Entity, ?>)(entitytype == null ? ANY_TYPE : entitytype);
      this.usesSelector = flag3;
   }

   public int getMaxResults() {
      return this.maxResults;
   }

   public boolean includesEntities() {
      return this.includesEntities;
   }

   public boolean isSelfSelector() {
      return this.currentEntity;
   }

   public boolean isWorldLimited() {
      return this.worldLimited;
   }

   public boolean usesSelector() {
      return this.usesSelector;
   }

   private void checkPermissions(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      if (this.usesSelector && !commandsourcestack.hasPermission(2)) {
         throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
      }
   }

   public Entity findSingleEntity(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      this.checkPermissions(commandsourcestack);
      List<? extends Entity> list = this.findEntities(commandsourcestack);
      if (list.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else if (list.size() > 1) {
         throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
      } else {
         return list.get(0);
      }
   }

   public List<? extends Entity> findEntities(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      return this.findEntitiesRaw(commandsourcestack).stream().filter((entity) -> entity.getType().isEnabled(commandsourcestack.enabledFeatures())).toList();
   }

   private List<? extends Entity> findEntitiesRaw(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      this.checkPermissions(commandsourcestack);
      if (!this.includesEntities) {
         return this.findPlayers(commandsourcestack);
      } else if (this.playerName != null) {
         ServerPlayer serverplayer = commandsourcestack.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<? extends Entity>)(serverplayer == null ? Collections.emptyList() : Lists.newArrayList(serverplayer));
      } else if (this.entityUUID != null) {
         for(ServerLevel serverlevel : commandsourcestack.getServer().getAllLevels()) {
            Entity entity = serverlevel.getEntity(this.entityUUID);
            if (entity != null) {
               return Lists.newArrayList(entity);
            }
         }

         return Collections.emptyList();
      } else {
         Vec3 vec3 = this.position.apply(commandsourcestack.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vec3);
         if (this.currentEntity) {
            return (List<? extends Entity>)(commandsourcestack.getEntity() != null && predicate.test(commandsourcestack.getEntity()) ? Lists.newArrayList(commandsourcestack.getEntity()) : Collections.emptyList());
         } else {
            List<Entity> list = Lists.newArrayList();
            if (this.isWorldLimited()) {
               this.addEntities(list, commandsourcestack.getLevel(), vec3, predicate);
            } else {
               for(ServerLevel serverlevel1 : commandsourcestack.getServer().getAllLevels()) {
                  this.addEntities(list, serverlevel1, vec3, predicate);
               }
            }

            return this.sortAndLimit(vec3, list);
         }
      }
   }

   private void addEntities(List<Entity> list, ServerLevel serverlevel, Vec3 vec3, Predicate<Entity> predicate) {
      int i = this.getResultLimit();
      if (list.size() < i) {
         if (this.aabb != null) {
            serverlevel.getEntities(this.type, this.aabb.move(vec3), predicate, list, i);
         } else {
            serverlevel.getEntities(this.type, predicate, list, i);
         }

      }
   }

   private int getResultLimit() {
      return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
   }

   public ServerPlayer findSinglePlayer(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      this.checkPermissions(commandsourcestack);
      List<ServerPlayer> list = this.findPlayers(commandsourcestack);
      if (list.size() != 1) {
         throw EntityArgument.NO_PLAYERS_FOUND.create();
      } else {
         return list.get(0);
      }
   }

   public List<ServerPlayer> findPlayers(CommandSourceStack commandsourcestack) throws CommandSyntaxException {
      this.checkPermissions(commandsourcestack);
      if (this.playerName != null) {
         ServerPlayer serverplayer = commandsourcestack.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<ServerPlayer>)(serverplayer == null ? Collections.emptyList() : Lists.newArrayList(serverplayer));
      } else if (this.entityUUID != null) {
         ServerPlayer serverplayer1 = commandsourcestack.getServer().getPlayerList().getPlayer(this.entityUUID);
         return (List<ServerPlayer>)(serverplayer1 == null ? Collections.emptyList() : Lists.newArrayList(serverplayer1));
      } else {
         Vec3 vec3 = this.position.apply(commandsourcestack.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vec3);
         if (this.currentEntity) {
            if (commandsourcestack.getEntity() instanceof ServerPlayer) {
               ServerPlayer serverplayer2 = (ServerPlayer)commandsourcestack.getEntity();
               if (predicate.test(serverplayer2)) {
                  return Lists.newArrayList(serverplayer2);
               }
            }

            return Collections.emptyList();
         } else {
            int i = this.getResultLimit();
            List<ServerPlayer> list;
            if (this.isWorldLimited()) {
               list = commandsourcestack.getLevel().getPlayers(predicate, i);
            } else {
               list = Lists.newArrayList();

               for(ServerPlayer serverplayer3 : commandsourcestack.getServer().getPlayerList().getPlayers()) {
                  if (predicate.test(serverplayer3)) {
                     list.add(serverplayer3);
                     if (list.size() >= i) {
                        return list;
                     }
                  }
               }
            }

            return this.sortAndLimit(vec3, list);
         }
      }
   }

   private Predicate<Entity> getPredicate(Vec3 vec3) {
      Predicate<Entity> predicate = this.predicate;
      if (this.aabb != null) {
         AABB aabb = this.aabb.move(vec3);
         predicate = predicate.and((entity1) -> aabb.intersects(entity1.getBoundingBox()));
      }

      if (!this.range.isAny()) {
         predicate = predicate.and((entity) -> this.range.matchesSqr(entity.distanceToSqr(vec3)));
      }

      return predicate;
   }

   private <T extends Entity> List<T> sortAndLimit(Vec3 vec3, List<T> list) {
      if (list.size() > 1) {
         this.order.accept(vec3, list);
      }

      return list.subList(0, Math.min(this.maxResults, list.size()));
   }

   public static Component joinNames(List<? extends Entity> list) {
      return ComponentUtils.formatList(list, Entity::getDisplayName);
   }
}
