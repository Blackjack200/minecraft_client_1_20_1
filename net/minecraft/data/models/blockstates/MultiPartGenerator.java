package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class MultiPartGenerator implements BlockStateGenerator {
   private final Block block;
   private final List<MultiPartGenerator.Entry> parts = Lists.newArrayList();

   private MultiPartGenerator(Block block) {
      this.block = block;
   }

   public Block getBlock() {
      return this.block;
   }

   public static MultiPartGenerator multiPart(Block block) {
      return new MultiPartGenerator(block);
   }

   public MultiPartGenerator with(List<Variant> list) {
      this.parts.add(new MultiPartGenerator.Entry(list));
      return this;
   }

   public MultiPartGenerator with(Variant variant) {
      return this.with(ImmutableList.of(variant));
   }

   public MultiPartGenerator with(Condition condition, List<Variant> list) {
      this.parts.add(new MultiPartGenerator.ConditionalEntry(condition, list));
      return this;
   }

   public MultiPartGenerator with(Condition condition, Variant... avariant) {
      return this.with(condition, ImmutableList.copyOf(avariant));
   }

   public MultiPartGenerator with(Condition condition, Variant variant) {
      return this.with(condition, ImmutableList.of(variant));
   }

   public JsonElement get() {
      StateDefinition<Block, BlockState> statedefinition = this.block.getStateDefinition();
      this.parts.forEach((multipartgenerator_entry) -> multipartgenerator_entry.validate(statedefinition));
      JsonArray jsonarray = new JsonArray();
      this.parts.stream().map(MultiPartGenerator.Entry::get).forEach(jsonarray::add);
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("multipart", jsonarray);
      return jsonobject;
   }

   static class ConditionalEntry extends MultiPartGenerator.Entry {
      private final Condition condition;

      ConditionalEntry(Condition condition, List<Variant> list) {
         super(list);
         this.condition = condition;
      }

      public void validate(StateDefinition<?, ?> statedefinition) {
         this.condition.validate(statedefinition);
      }

      public void decorate(JsonObject jsonobject) {
         jsonobject.add("when", this.condition.get());
      }
   }

   static class Entry implements Supplier<JsonElement> {
      private final List<Variant> variants;

      Entry(List<Variant> list) {
         this.variants = list;
      }

      public void validate(StateDefinition<?, ?> statedefinition) {
      }

      public void decorate(JsonObject jsonobject) {
      }

      public JsonElement get() {
         JsonObject jsonobject = new JsonObject();
         this.decorate(jsonobject);
         jsonobject.add("apply", Variant.convertList(this.variants));
         return jsonobject;
      }
   }
}
