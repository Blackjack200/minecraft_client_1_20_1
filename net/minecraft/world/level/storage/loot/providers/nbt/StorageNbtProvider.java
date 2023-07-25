package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class StorageNbtProvider implements NbtProvider {
   final ResourceLocation id;

   StorageNbtProvider(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public LootNbtProviderType getType() {
      return NbtProviders.STORAGE;
   }

   @Nullable
   public Tag get(LootContext lootcontext) {
      return lootcontext.getLevel().getServer().getCommandStorage().get(this.id);
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<StorageNbtProvider> {
      public void serialize(JsonObject jsonobject, StorageNbtProvider storagenbtprovider, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("source", storagenbtprovider.id.toString());
      }

      public StorageNbtProvider deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         String s = GsonHelper.getAsString(jsonobject, "source");
         return new StorageNbtProvider(new ResourceLocation(s));
      }
   }
}
