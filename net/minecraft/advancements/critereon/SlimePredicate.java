package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public class SlimePredicate implements EntitySubPredicate {
   private final MinMaxBounds.Ints size;

   private SlimePredicate(MinMaxBounds.Ints minmaxbounds_ints) {
      this.size = minmaxbounds_ints;
   }

   public static SlimePredicate sized(MinMaxBounds.Ints minmaxbounds_ints) {
      return new SlimePredicate(minmaxbounds_ints);
   }

   public static SlimePredicate fromJson(JsonObject jsonobject) {
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("size"));
      return new SlimePredicate(minmaxbounds_ints);
   }

   public JsonObject serializeCustomData() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("size", this.size.serializeToJson());
      return jsonobject;
   }

   public boolean matches(Entity entity, ServerLevel serverlevel, @Nullable Vec3 vec3) {
      if (entity instanceof Slime slime) {
         return this.size.matches(slime.getSize());
      } else {
         return false;
      }
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.SLIME;
   }
}
