package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface Serializer<T> {
   void serialize(JsonObject jsonobject, T object, JsonSerializationContext jsonserializationcontext);

   T deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext);
}
