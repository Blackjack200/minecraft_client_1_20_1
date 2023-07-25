package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ContextNbtProvider implements NbtProvider {
   private static final String BLOCK_ENTITY_ID = "block_entity";
   private static final ContextNbtProvider.Getter BLOCK_ENTITY_PROVIDER = new ContextNbtProvider.Getter() {
      public Tag get(LootContext lootcontext) {
         BlockEntity blockentity = lootcontext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
         return blockentity != null ? blockentity.saveWithFullMetadata() : null;
      }

      public String getId() {
         return "block_entity";
      }

      public Set<LootContextParam<?>> getReferencedContextParams() {
         return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
      }
   };
   public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
   final ContextNbtProvider.Getter getter;

   private static ContextNbtProvider.Getter forEntity(final LootContext.EntityTarget lootcontext_entitytarget) {
      return new ContextNbtProvider.Getter() {
         @Nullable
         public Tag get(LootContext lootcontext) {
            Entity entity = lootcontext.getParamOrNull(lootcontext_entitytarget.getParam());
            return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
         }

         public String getId() {
            return lootcontext_entitytarget.name();
         }

         public Set<LootContextParam<?>> getReferencedContextParams() {
            return ImmutableSet.of(lootcontext_entitytarget.getParam());
         }
      };
   }

   private ContextNbtProvider(ContextNbtProvider.Getter contextnbtprovider_getter) {
      this.getter = contextnbtprovider_getter;
   }

   public LootNbtProviderType getType() {
      return NbtProviders.CONTEXT;
   }

   @Nullable
   public Tag get(LootContext lootcontext) {
      return this.getter.get(lootcontext);
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.getter.getReferencedContextParams();
   }

   public static NbtProvider forContextEntity(LootContext.EntityTarget lootcontext_entitytarget) {
      return new ContextNbtProvider(forEntity(lootcontext_entitytarget));
   }

   static ContextNbtProvider createFromContext(String s) {
      if (s.equals("block_entity")) {
         return new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
      } else {
         LootContext.EntityTarget lootcontext_entitytarget = LootContext.EntityTarget.getByName(s);
         return new ContextNbtProvider(forEntity(lootcontext_entitytarget));
      }
   }

   interface Getter {
      @Nullable
      Tag get(LootContext lootcontext);

      String getId();

      Set<LootContextParam<?>> getReferencedContextParams();
   }

   public static class InlineSerializer implements GsonAdapterFactory.InlineSerializer<ContextNbtProvider> {
      public JsonElement serialize(ContextNbtProvider contextnbtprovider, JsonSerializationContext jsonserializationcontext) {
         return new JsonPrimitive(contextnbtprovider.getter.getId());
      }

      public ContextNbtProvider deserialize(JsonElement jsonelement, JsonDeserializationContext jsondeserializationcontext) {
         String s = jsonelement.getAsString();
         return ContextNbtProvider.createFromContext(s);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ContextNbtProvider> {
      public void serialize(JsonObject jsonobject, ContextNbtProvider contextnbtprovider, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("target", contextnbtprovider.getter.getId());
      }

      public ContextNbtProvider deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         String s = GsonHelper.getAsString(jsonobject, "target");
         return ContextNbtProvider.createFromContext(s);
      }
   }
}
