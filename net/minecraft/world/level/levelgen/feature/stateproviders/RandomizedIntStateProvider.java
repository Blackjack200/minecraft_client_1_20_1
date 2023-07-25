package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class RandomizedIntStateProvider extends BlockStateProvider {
   public static final Codec<RandomizedIntStateProvider> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("source").forGetter((randomizedintstateprovider2) -> randomizedintstateprovider2.source), Codec.STRING.fieldOf("property").forGetter((randomizedintstateprovider1) -> randomizedintstateprovider1.propertyName), IntProvider.CODEC.fieldOf("values").forGetter((randomizedintstateprovider) -> randomizedintstateprovider.values)).apply(recordcodecbuilder_instance, RandomizedIntStateProvider::new));
   private final BlockStateProvider source;
   private final String propertyName;
   @Nullable
   private IntegerProperty property;
   private final IntProvider values;

   public RandomizedIntStateProvider(BlockStateProvider blockstateprovider, IntegerProperty integerproperty, IntProvider intprovider) {
      this.source = blockstateprovider;
      this.property = integerproperty;
      this.propertyName = integerproperty.getName();
      this.values = intprovider;
      Collection<Integer> collection = integerproperty.getPossibleValues();

      for(int i = intprovider.getMinValue(); i <= intprovider.getMaxValue(); ++i) {
         if (!collection.contains(i)) {
            throw new IllegalArgumentException("Property value out of range: " + integerproperty.getName() + ": " + i);
         }
      }

   }

   public RandomizedIntStateProvider(BlockStateProvider blockstateprovider, String s, IntProvider intprovider) {
      this.source = blockstateprovider;
      this.propertyName = s;
      this.values = intprovider;
   }

   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.RANDOMIZED_INT_STATE_PROVIDER;
   }

   public BlockState getState(RandomSource randomsource, BlockPos blockpos) {
      BlockState blockstate = this.source.getState(randomsource, blockpos);
      if (this.property == null || !blockstate.hasProperty(this.property)) {
         this.property = findProperty(blockstate, this.propertyName);
      }

      return blockstate.setValue(this.property, Integer.valueOf(this.values.sample(randomsource)));
   }

   private static IntegerProperty findProperty(BlockState blockstate, String s) {
      Collection<Property<?>> collection = blockstate.getProperties();
      Optional<IntegerProperty> optional = collection.stream().filter((property2) -> property2.getName().equals(s)).filter((property1) -> property1 instanceof IntegerProperty).map((property) -> (IntegerProperty)property).findAny();
      return optional.orElseThrow(() -> new IllegalArgumentException("Illegal property: " + s));
   }
}
