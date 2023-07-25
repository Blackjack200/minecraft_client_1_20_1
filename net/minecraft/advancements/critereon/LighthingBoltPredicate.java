package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class LighthingBoltPredicate implements EntitySubPredicate {
   private static final String BLOCKS_SET_ON_FIRE_KEY = "blocks_set_on_fire";
   private static final String ENTITY_STRUCK_KEY = "entity_struck";
   private final MinMaxBounds.Ints blocksSetOnFire;
   private final EntityPredicate entityStruck;

   private LighthingBoltPredicate(MinMaxBounds.Ints minmaxbounds_ints, EntityPredicate entitypredicate) {
      this.blocksSetOnFire = minmaxbounds_ints;
      this.entityStruck = entitypredicate;
   }

   public static LighthingBoltPredicate blockSetOnFire(MinMaxBounds.Ints minmaxbounds_ints) {
      return new LighthingBoltPredicate(minmaxbounds_ints, EntityPredicate.ANY);
   }

   public static LighthingBoltPredicate fromJson(JsonObject jsonobject) {
      return new LighthingBoltPredicate(MinMaxBounds.Ints.fromJson(jsonobject.get("blocks_set_on_fire")), EntityPredicate.fromJson(jsonobject.get("entity_struck")));
   }

   public JsonObject serializeCustomData() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("blocks_set_on_fire", this.blocksSetOnFire.serializeToJson());
      jsonobject.add("entity_struck", this.entityStruck.serializeToJson());
      return jsonobject;
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.LIGHTNING;
   }

   public boolean matches(Entity entity, ServerLevel serverlevel, @Nullable Vec3 vec3) {
      if (!(entity instanceof LightningBolt lightningbolt)) {
         return false;
      } else {
         return this.blocksSetOnFire.matches(lightningbolt.getBlocksSetOnFire()) && (this.entityStruck == EntityPredicate.ANY || lightningbolt.getHitEntities().anyMatch((entity1) -> this.entityStruck.matches(serverlevel, vec3, entity1)));
      }
   }
}
