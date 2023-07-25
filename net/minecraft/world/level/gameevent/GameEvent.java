package net.minecraft.world.level.gameevent;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class GameEvent {
   public static final GameEvent BLOCK_ACTIVATE = register("block_activate");
   public static final GameEvent BLOCK_ATTACH = register("block_attach");
   public static final GameEvent BLOCK_CHANGE = register("block_change");
   public static final GameEvent BLOCK_CLOSE = register("block_close");
   public static final GameEvent BLOCK_DEACTIVATE = register("block_deactivate");
   public static final GameEvent BLOCK_DESTROY = register("block_destroy");
   public static final GameEvent BLOCK_DETACH = register("block_detach");
   public static final GameEvent BLOCK_OPEN = register("block_open");
   public static final GameEvent BLOCK_PLACE = register("block_place");
   public static final GameEvent CONTAINER_CLOSE = register("container_close");
   public static final GameEvent CONTAINER_OPEN = register("container_open");
   public static final GameEvent DRINK = register("drink");
   public static final GameEvent EAT = register("eat");
   public static final GameEvent ELYTRA_GLIDE = register("elytra_glide");
   public static final GameEvent ENTITY_DAMAGE = register("entity_damage");
   public static final GameEvent ENTITY_DIE = register("entity_die");
   public static final GameEvent ENTITY_DISMOUNT = register("entity_dismount");
   public static final GameEvent ENTITY_INTERACT = register("entity_interact");
   public static final GameEvent ENTITY_MOUNT = register("entity_mount");
   public static final GameEvent ENTITY_PLACE = register("entity_place");
   public static final GameEvent ENTITY_ROAR = register("entity_roar");
   public static final GameEvent ENTITY_SHAKE = register("entity_shake");
   public static final GameEvent EQUIP = register("equip");
   public static final GameEvent EXPLODE = register("explode");
   public static final GameEvent FLAP = register("flap");
   public static final GameEvent FLUID_PICKUP = register("fluid_pickup");
   public static final GameEvent FLUID_PLACE = register("fluid_place");
   public static final GameEvent HIT_GROUND = register("hit_ground");
   public static final GameEvent INSTRUMENT_PLAY = register("instrument_play");
   public static final GameEvent ITEM_INTERACT_FINISH = register("item_interact_finish");
   public static final GameEvent ITEM_INTERACT_START = register("item_interact_start");
   public static final GameEvent JUKEBOX_PLAY = register("jukebox_play", 10);
   public static final GameEvent JUKEBOX_STOP_PLAY = register("jukebox_stop_play", 10);
   public static final GameEvent LIGHTNING_STRIKE = register("lightning_strike");
   public static final GameEvent NOTE_BLOCK_PLAY = register("note_block_play");
   public static final GameEvent PRIME_FUSE = register("prime_fuse");
   public static final GameEvent PROJECTILE_LAND = register("projectile_land");
   public static final GameEvent PROJECTILE_SHOOT = register("projectile_shoot");
   public static final GameEvent SCULK_SENSOR_TENDRILS_CLICKING = register("sculk_sensor_tendrils_clicking");
   public static final GameEvent SHEAR = register("shear");
   public static final GameEvent SHRIEK = register("shriek", 32);
   public static final GameEvent SPLASH = register("splash");
   public static final GameEvent STEP = register("step");
   public static final GameEvent SWIM = register("swim");
   public static final GameEvent TELEPORT = register("teleport");
   public static final GameEvent RESONATE_1 = register("resonate_1");
   public static final GameEvent RESONATE_2 = register("resonate_2");
   public static final GameEvent RESONATE_3 = register("resonate_3");
   public static final GameEvent RESONATE_4 = register("resonate_4");
   public static final GameEvent RESONATE_5 = register("resonate_5");
   public static final GameEvent RESONATE_6 = register("resonate_6");
   public static final GameEvent RESONATE_7 = register("resonate_7");
   public static final GameEvent RESONATE_8 = register("resonate_8");
   public static final GameEvent RESONATE_9 = register("resonate_9");
   public static final GameEvent RESONATE_10 = register("resonate_10");
   public static final GameEvent RESONATE_11 = register("resonate_11");
   public static final GameEvent RESONATE_12 = register("resonate_12");
   public static final GameEvent RESONATE_13 = register("resonate_13");
   public static final GameEvent RESONATE_14 = register("resonate_14");
   public static final GameEvent RESONATE_15 = register("resonate_15");
   public static final int DEFAULT_NOTIFICATION_RADIUS = 16;
   private final String name;
   private final int notificationRadius;
   private final Holder.Reference<GameEvent> builtInRegistryHolder = BuiltInRegistries.GAME_EVENT.createIntrusiveHolder(this);

   public GameEvent(String s, int i) {
      this.name = s;
      this.notificationRadius = i;
   }

   public String getName() {
      return this.name;
   }

   public int getNotificationRadius() {
      return this.notificationRadius;
   }

   private static GameEvent register(String s) {
      return register(s, 16);
   }

   private static GameEvent register(String s, int i) {
      return Registry.register(BuiltInRegistries.GAME_EVENT, s, new GameEvent(s, i));
   }

   public String toString() {
      return "Game Event{ " + this.name + " , " + this.notificationRadius + "}";
   }

   /** @deprecated */
   @Deprecated
   public Holder.Reference<GameEvent> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public boolean is(TagKey<GameEvent> tagkey) {
      return this.builtInRegistryHolder.is(tagkey);
   }

   public static record Context(@Nullable Entity sourceEntity, @Nullable BlockState affectedState) {
      public static GameEvent.Context of(@Nullable Entity entity) {
         return new GameEvent.Context(entity, (BlockState)null);
      }

      public static GameEvent.Context of(@Nullable BlockState blockstate) {
         return new GameEvent.Context((Entity)null, blockstate);
      }

      public static GameEvent.Context of(@Nullable Entity entity, @Nullable BlockState blockstate) {
         return new GameEvent.Context(entity, blockstate);
      }
   }

   public static final class ListenerInfo implements Comparable<GameEvent.ListenerInfo> {
      private final GameEvent gameEvent;
      private final Vec3 source;
      private final GameEvent.Context context;
      private final GameEventListener recipient;
      private final double distanceToRecipient;

      public ListenerInfo(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context, GameEventListener gameeventlistener, Vec3 vec31) {
         this.gameEvent = gameevent;
         this.source = vec3;
         this.context = gameevent_context;
         this.recipient = gameeventlistener;
         this.distanceToRecipient = vec3.distanceToSqr(vec31);
      }

      public int compareTo(GameEvent.ListenerInfo gameevent_listenerinfo) {
         return Double.compare(this.distanceToRecipient, gameevent_listenerinfo.distanceToRecipient);
      }

      public GameEvent gameEvent() {
         return this.gameEvent;
      }

      public Vec3 source() {
         return this.source;
      }

      public GameEvent.Context context() {
         return this.context;
      }

      public GameEventListener recipient() {
         return this.recipient;
      }
   }
}
