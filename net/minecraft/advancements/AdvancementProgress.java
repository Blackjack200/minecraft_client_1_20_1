package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public class AdvancementProgress implements Comparable<AdvancementProgress> {
   final Map<String, CriterionProgress> criteria;
   private String[][] requirements = new String[0][];

   private AdvancementProgress(Map<String, CriterionProgress> map) {
      this.criteria = map;
   }

   public AdvancementProgress() {
      this.criteria = Maps.newHashMap();
   }

   public void update(Map<String, Criterion> map, String[][] astring) {
      Set<String> set = map.keySet();
      this.criteria.entrySet().removeIf((map_entry) -> !set.contains(map_entry.getKey()));

      for(String s : set) {
         if (!this.criteria.containsKey(s)) {
            this.criteria.put(s, new CriterionProgress());
         }
      }

      this.requirements = astring;
   }

   public boolean isDone() {
      if (this.requirements.length == 0) {
         return false;
      } else {
         for(String[] astring : this.requirements) {
            boolean flag = false;

            for(String s : astring) {
               CriterionProgress criterionprogress = this.getCriterion(s);
               if (criterionprogress != null && criterionprogress.isDone()) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean hasProgress() {
      for(CriterionProgress criterionprogress : this.criteria.values()) {
         if (criterionprogress.isDone()) {
            return true;
         }
      }

      return false;
   }

   public boolean grantProgress(String s) {
      CriterionProgress criterionprogress = this.criteria.get(s);
      if (criterionprogress != null && !criterionprogress.isDone()) {
         criterionprogress.grant();
         return true;
      } else {
         return false;
      }
   }

   public boolean revokeProgress(String s) {
      CriterionProgress criterionprogress = this.criteria.get(s);
      if (criterionprogress != null && criterionprogress.isDone()) {
         criterionprogress.revoke();
         return true;
      } else {
         return false;
      }
   }

   public String toString() {
      return "AdvancementProgress{criteria=" + this.criteria + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
   }

   public void serializeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeMap(this.criteria, FriendlyByteBuf::writeUtf, (friendlybytebuf1, criterionprogress) -> criterionprogress.serializeToNetwork(friendlybytebuf1));
   }

   public static AdvancementProgress fromNetwork(FriendlyByteBuf friendlybytebuf) {
      Map<String, CriterionProgress> map = friendlybytebuf.readMap(FriendlyByteBuf::readUtf, CriterionProgress::fromNetwork);
      return new AdvancementProgress(map);
   }

   @Nullable
   public CriterionProgress getCriterion(String s) {
      return this.criteria.get(s);
   }

   public float getPercent() {
      if (this.criteria.isEmpty()) {
         return 0.0F;
      } else {
         float f = (float)this.requirements.length;
         float f1 = (float)this.countCompletedRequirements();
         return f1 / f;
      }
   }

   @Nullable
   public String getProgressText() {
      if (this.criteria.isEmpty()) {
         return null;
      } else {
         int i = this.requirements.length;
         if (i <= 1) {
            return null;
         } else {
            int j = this.countCompletedRequirements();
            return j + "/" + i;
         }
      }
   }

   private int countCompletedRequirements() {
      int i = 0;

      for(String[] astring : this.requirements) {
         boolean flag = false;

         for(String s : astring) {
            CriterionProgress criterionprogress = this.getCriterion(s);
            if (criterionprogress != null && criterionprogress.isDone()) {
               flag = true;
               break;
            }
         }

         if (flag) {
            ++i;
         }
      }

      return i;
   }

   public Iterable<String> getRemainingCriteria() {
      List<String> list = Lists.newArrayList();

      for(Map.Entry<String, CriterionProgress> map_entry : this.criteria.entrySet()) {
         if (!map_entry.getValue().isDone()) {
            list.add(map_entry.getKey());
         }
      }

      return list;
   }

   public Iterable<String> getCompletedCriteria() {
      List<String> list = Lists.newArrayList();

      for(Map.Entry<String, CriterionProgress> map_entry : this.criteria.entrySet()) {
         if (map_entry.getValue().isDone()) {
            list.add(map_entry.getKey());
         }
      }

      return list;
   }

   @Nullable
   public Date getFirstProgressDate() {
      Date date = null;

      for(CriterionProgress criterionprogress : this.criteria.values()) {
         if (criterionprogress.isDone() && (date == null || criterionprogress.getObtained().before(date))) {
            date = criterionprogress.getObtained();
         }
      }

      return date;
   }

   public int compareTo(AdvancementProgress advancementprogress) {
      Date date = this.getFirstProgressDate();
      Date date1 = advancementprogress.getFirstProgressDate();
      if (date == null && date1 != null) {
         return 1;
      } else if (date != null && date1 == null) {
         return -1;
      } else {
         return date == null && date1 == null ? 0 : date.compareTo(date1);
      }
   }

   public static class Serializer implements JsonDeserializer<AdvancementProgress>, JsonSerializer<AdvancementProgress> {
      public JsonElement serialize(AdvancementProgress advancementprogress, Type type, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         JsonObject jsonobject1 = new JsonObject();

         for(Map.Entry<String, CriterionProgress> map_entry : advancementprogress.criteria.entrySet()) {
            CriterionProgress criterionprogress = map_entry.getValue();
            if (criterionprogress.isDone()) {
               jsonobject1.add(map_entry.getKey(), criterionprogress.serializeToJson());
            }
         }

         if (!jsonobject1.entrySet().isEmpty()) {
            jsonobject.add("criteria", jsonobject1);
         }

         jsonobject.addProperty("done", advancementprogress.isDone());
         return jsonobject;
      }

      public AdvancementProgress deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "advancement");
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "criteria", new JsonObject());
         AdvancementProgress advancementprogress = new AdvancementProgress();

         for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
            String s = map_entry.getKey();
            advancementprogress.criteria.put(s, CriterionProgress.fromJson(GsonHelper.convertToString(map_entry.getValue(), s)));
         }

         return advancementprogress;
      }
   }
}
