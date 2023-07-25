package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.slf4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final NumberProvider damage;
   final boolean add;

   SetItemDamageFunction(LootItemCondition[] alootitemcondition, NumberProvider numberprovider, boolean flag) {
      super(alootitemcondition);
      this.damage = numberprovider;
      this.add = flag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_DAMAGE;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.damage.getReferencedContextParams();
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      if (itemstack.isDamageableItem()) {
         int i = itemstack.getMaxDamage();
         float f = this.add ? 1.0F - (float)itemstack.getDamageValue() / (float)i : 0.0F;
         float f1 = 1.0F - Mth.clamp(this.damage.getFloat(lootcontext) + f, 0.0F, 1.0F);
         itemstack.setDamageValue(Mth.floor(f1 * (float)i));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", (Object)itemstack);
      }

      return itemstack;
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberprovider) {
      return simpleBuilder((alootitemcondition) -> new SetItemDamageFunction(alootitemcondition, numberprovider, false));
   }

   public static LootItemConditionalFunction.Builder<?> setDamage(NumberProvider numberprovider, boolean flag) {
      return simpleBuilder((alootitemcondition) -> new SetItemDamageFunction(alootitemcondition, numberprovider, flag));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
      public void serialize(JsonObject jsonobject, SetItemDamageFunction setitemdamagefunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setitemdamagefunction, jsonserializationcontext);
         jsonobject.add("damage", jsonserializationcontext.serialize(setitemdamagefunction.damage));
         jsonobject.addProperty("add", setitemdamagefunction.add);
      }

      public SetItemDamageFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "damage", jsondeserializationcontext, NumberProvider.class);
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "add", false);
         return new SetItemDamageFunction(alootitemcondition, numberprovider, flag);
      }
   }
}
