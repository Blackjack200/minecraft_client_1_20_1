package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class SetNameFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final Component name;
   @Nullable
   final LootContext.EntityTarget resolutionContext;

   SetNameFunction(LootItemCondition[] alootitemcondition, @Nullable Component component, @Nullable LootContext.EntityTarget lootcontext_entitytarget) {
      super(alootitemcondition);
      this.name = component;
      this.resolutionContext = lootcontext_entitytarget;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_NAME;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   public static UnaryOperator<Component> createResolver(LootContext lootcontext, @Nullable LootContext.EntityTarget lootcontext_entitytarget) {
      if (lootcontext_entitytarget != null) {
         Entity entity = lootcontext.getParamOrNull(lootcontext_entitytarget.getParam());
         if (entity != null) {
            CommandSourceStack commandsourcestack = entity.createCommandSourceStack().withPermission(2);
            return (component1) -> {
               try {
                  return ComponentUtils.updateForEntity(commandsourcestack, component1, entity, 0);
               } catch (CommandSyntaxException var4) {
                  LOGGER.warn("Failed to resolve text component", (Throwable)var4);
                  return component1;
               }
            };
         }
      }

      return (component) -> component;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (this.name != null) {
         itemstack.setHoverName(createResolver(lootcontext, this.resolutionContext).apply(this.name));
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component component) {
      return simpleBuilder((alootitemcondition) -> new SetNameFunction(alootitemcondition, component, (LootContext.EntityTarget)null));
   }

   public static LootItemConditionalFunction.Builder<?> setName(Component component, LootContext.EntityTarget lootcontext_entitytarget) {
      return simpleBuilder((alootitemcondition) -> new SetNameFunction(alootitemcondition, component, lootcontext_entitytarget));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetNameFunction> {
      public void serialize(JsonObject jsonobject, SetNameFunction setnamefunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setnamefunction, jsonserializationcontext);
         if (setnamefunction.name != null) {
            jsonobject.add("name", Component.Serializer.toJsonTree(setnamefunction.name));
         }

         if (setnamefunction.resolutionContext != null) {
            jsonobject.add("entity", jsonserializationcontext.serialize(setnamefunction.resolutionContext));
         }

      }

      public SetNameFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         Component component = Component.Serializer.fromJson(jsonobject.get("name"));
         LootContext.EntityTarget lootcontext_entitytarget = GsonHelper.getAsObject(jsonobject, "entity", (LootContext.EntityTarget)null, jsondeserializationcontext, LootContext.EntityTarget.class);
         return new SetNameFunction(alootitemcondition, component, lootcontext_entitytarget);
      }
   }
}
