package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase extends LootPoolEntryContainer {
   protected final LootPoolEntryContainer[] children;
   private final ComposableEntryContainer composedChildren;

   protected CompositeEntryBase(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition) {
      super(alootitemcondition);
      this.children = alootpoolentrycontainer;
      this.composedChildren = this.compose(alootpoolentrycontainer);
   }

   public void validate(ValidationContext validationcontext) {
      super.validate(validationcontext);
      if (this.children.length == 0) {
         validationcontext.reportProblem("Empty children list");
      }

      for(int i = 0; i < this.children.length; ++i) {
         this.children[i].validate(validationcontext.forChild(".entry[" + i + "]"));
      }

   }

   protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] acomposableentrycontainer);

   public final boolean expand(LootContext lootcontext, Consumer<LootPoolEntry> consumer) {
      return !this.canRun(lootcontext) ? false : this.composedChildren.expand(lootcontext, consumer);
   }

   public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryBase.CompositeEntryConstructor<T> compositeentrybase_compositeentryconstructor) {
      return new LootPoolEntryContainer.Serializer<T>() {
         public void serializeCustom(JsonObject jsonobject, T compositeentrybase, JsonSerializationContext jsonserializationcontext) {
            jsonobject.add("children", jsonserializationcontext.serialize(compositeentrybase.children));
         }

         public final T deserializeCustom(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
            LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(jsonobject, "children", jsondeserializationcontext, LootPoolEntryContainer[].class);
            return compositeentrybase_compositeentryconstructor.create(alootpoolentrycontainer, alootitemcondition);
         }
      };
   }

   @FunctionalInterface
   public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
      T create(LootPoolEntryContainer[] alootpoolentrycontainer, LootItemCondition[] alootitemcondition);
   }
}
