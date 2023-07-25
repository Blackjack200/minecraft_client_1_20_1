package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
   public static final DistancePredicate ANY = new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   private final MinMaxBounds.Doubles horizontal;
   private final MinMaxBounds.Doubles absolute;

   public DistancePredicate(MinMaxBounds.Doubles minmaxbounds_doubles, MinMaxBounds.Doubles minmaxbounds_doubles1, MinMaxBounds.Doubles minmaxbounds_doubles2, MinMaxBounds.Doubles minmaxbounds_doubles3, MinMaxBounds.Doubles minmaxbounds_doubles4) {
      this.x = minmaxbounds_doubles;
      this.y = minmaxbounds_doubles1;
      this.z = minmaxbounds_doubles2;
      this.horizontal = minmaxbounds_doubles3;
      this.absolute = minmaxbounds_doubles4;
   }

   public static DistancePredicate horizontal(MinMaxBounds.Doubles minmaxbounds_doubles) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, minmaxbounds_doubles, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate vertical(MinMaxBounds.Doubles minmaxbounds_doubles) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, minmaxbounds_doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate absolute(MinMaxBounds.Doubles minmaxbounds_doubles) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, minmaxbounds_doubles);
   }

   public boolean matches(double d0, double d1, double d2, double d3, double d4, double d5) {
      float f = (float)(d0 - d3);
      float f1 = (float)(d1 - d4);
      float f2 = (float)(d2 - d5);
      if (this.x.matches((double)Mth.abs(f)) && this.y.matches((double)Mth.abs(f1)) && this.z.matches((double)Mth.abs(f2))) {
         if (!this.horizontal.matchesSqr((double)(f * f + f2 * f2))) {
            return false;
         } else {
            return this.absolute.matchesSqr((double)(f * f + f1 * f1 + f2 * f2));
         }
      } else {
         return false;
      }
   }

   public static DistancePredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "distance");
         MinMaxBounds.Doubles minmaxbounds_doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("x"));
         MinMaxBounds.Doubles minmaxbounds_doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject.get("y"));
         MinMaxBounds.Doubles minmaxbounds_doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject.get("z"));
         MinMaxBounds.Doubles minmaxbounds_doubles3 = MinMaxBounds.Doubles.fromJson(jsonobject.get("horizontal"));
         MinMaxBounds.Doubles minmaxbounds_doubles4 = MinMaxBounds.Doubles.fromJson(jsonobject.get("absolute"));
         return new DistancePredicate(minmaxbounds_doubles, minmaxbounds_doubles1, minmaxbounds_doubles2, minmaxbounds_doubles3, minmaxbounds_doubles4);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("x", this.x.serializeToJson());
         jsonobject.add("y", this.y.serializeToJson());
         jsonobject.add("z", this.z.serializeToJson());
         jsonobject.add("horizontal", this.horizontal.serializeToJson());
         jsonobject.add("absolute", this.absolute.serializeToJson());
         return jsonobject;
      }
   }
}
