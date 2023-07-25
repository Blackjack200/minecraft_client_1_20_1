package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

public class MultiVariantGenerator implements BlockStateGenerator {
   private final Block block;
   private final List<Variant> baseVariants;
   private final Set<Property<?>> seenProperties = Sets.newHashSet();
   private final List<PropertyDispatch> declaredPropertySets = Lists.newArrayList();

   private MultiVariantGenerator(Block block, List<Variant> list) {
      this.block = block;
      this.baseVariants = list;
   }

   public MultiVariantGenerator with(PropertyDispatch propertydispatch) {
      propertydispatch.getDefinedProperties().forEach((property) -> {
         if (this.block.getStateDefinition().getProperty(property.getName()) != property) {
            throw new IllegalStateException("Property " + property + " is not defined for block " + this.block);
         } else if (!this.seenProperties.add(property)) {
            throw new IllegalStateException("Values of property " + property + " already defined for block " + this.block);
         }
      });
      this.declaredPropertySets.add(propertydispatch);
      return this;
   }

   public JsonElement get() {
      Stream<Pair<Selector, List<Variant>>> stream = Stream.of(Pair.of(Selector.empty(), this.baseVariants));

      for(PropertyDispatch propertydispatch : this.declaredPropertySets) {
         Map<Selector, List<Variant>> map = propertydispatch.getEntries();
         stream = stream.flatMap((pair1) -> map.entrySet().stream().map((map_entry) -> {
               Selector selector = ((Selector)pair1.getFirst()).extend(map_entry.getKey());
               List<Variant> list = mergeVariants((List)pair1.getSecond(), map_entry.getValue());
               return Pair.of(selector, list);
            }));
      }

      Map<String, JsonElement> map1 = new TreeMap<>();
      stream.forEach((pair) -> map1.put(pair.getFirst().getKey(), Variant.convertList(pair.getSecond())));
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("variants", Util.make(new JsonObject(), (jsonobject1) -> map1.forEach(jsonobject1::add)));
      return jsonobject;
   }

   private static List<Variant> mergeVariants(List<Variant> list, List<Variant> list1) {
      ImmutableList.Builder<Variant> immutablelist_builder = ImmutableList.builder();
      list.forEach((variant) -> list1.forEach((variant2) -> immutablelist_builder.add(Variant.merge(variant, variant2))));
      return immutablelist_builder.build();
   }

   public Block getBlock() {
      return this.block;
   }

   public static MultiVariantGenerator multiVariant(Block block) {
      return new MultiVariantGenerator(block, ImmutableList.of(Variant.variant()));
   }

   public static MultiVariantGenerator multiVariant(Block block, Variant variant) {
      return new MultiVariantGenerator(block, ImmutableList.of(variant));
   }

   public static MultiVariantGenerator multiVariant(Block block, Variant... avariant) {
      return new MultiVariantGenerator(block, ImmutableList.copyOf(avariant));
   }
}
