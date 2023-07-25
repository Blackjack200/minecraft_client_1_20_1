package net.minecraft.client.resources.sounds;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import org.apache.commons.lang3.Validate;

public class SoundEventRegistrationSerializer implements JsonDeserializer<SoundEventRegistration> {
   private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);

   public SoundEventRegistration deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
      JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "entry");
      boolean flag = GsonHelper.getAsBoolean(jsonobject, "replace", false);
      String s = GsonHelper.getAsString(jsonobject, "subtitle", (String)null);
      List<Sound> list = this.getSounds(jsonobject);
      return new SoundEventRegistration(list, flag, s);
   }

   private List<Sound> getSounds(JsonObject jsonobject) {
      List<Sound> list = Lists.newArrayList();
      if (jsonobject.has("sounds")) {
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "sounds");

         for(int i = 0; i < jsonarray.size(); ++i) {
            JsonElement jsonelement = jsonarray.get(i);
            if (GsonHelper.isStringValue(jsonelement)) {
               String s = GsonHelper.convertToString(jsonelement, "sound");
               list.add(new Sound(s, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16));
            } else {
               list.add(this.getSound(GsonHelper.convertToJsonObject(jsonelement, "sound")));
            }
         }
      }

      return list;
   }

   private Sound getSound(JsonObject jsonobject) {
      String s = GsonHelper.getAsString(jsonobject, "name");
      Sound.Type sound_type = this.getType(jsonobject, Sound.Type.FILE);
      float f = GsonHelper.getAsFloat(jsonobject, "volume", 1.0F);
      Validate.isTrue(f > 0.0F, "Invalid volume");
      float f1 = GsonHelper.getAsFloat(jsonobject, "pitch", 1.0F);
      Validate.isTrue(f1 > 0.0F, "Invalid pitch");
      int i = GsonHelper.getAsInt(jsonobject, "weight", 1);
      Validate.isTrue(i > 0, "Invalid weight");
      boolean flag = GsonHelper.getAsBoolean(jsonobject, "preload", false);
      boolean flag1 = GsonHelper.getAsBoolean(jsonobject, "stream", false);
      int j = GsonHelper.getAsInt(jsonobject, "attenuation_distance", 16);
      return new Sound(s, ConstantFloat.of(f), ConstantFloat.of(f1), i, sound_type, flag1, flag, j);
   }

   private Sound.Type getType(JsonObject jsonobject, Sound.Type sound_type) {
      Sound.Type sound_type1 = sound_type;
      if (jsonobject.has("type")) {
         sound_type1 = Sound.Type.getByName(GsonHelper.getAsString(jsonobject, "type"));
         Validate.notNull(sound_type1, "Invalid type");
      }

      return sound_type1;
   }
}
