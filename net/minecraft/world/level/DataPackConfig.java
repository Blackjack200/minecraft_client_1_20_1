package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public class DataPackConfig {
   public static final DataPackConfig DEFAULT = new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
   public static final Codec<DataPackConfig> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.STRING.listOf().fieldOf("Enabled").forGetter((datapackconfig1) -> datapackconfig1.enabled), Codec.STRING.listOf().fieldOf("Disabled").forGetter((datapackconfig) -> datapackconfig.disabled)).apply(recordcodecbuilder_instance, DataPackConfig::new));
   private final List<String> enabled;
   private final List<String> disabled;

   public DataPackConfig(List<String> list, List<String> list1) {
      this.enabled = ImmutableList.copyOf(list);
      this.disabled = ImmutableList.copyOf(list1);
   }

   public List<String> getEnabled() {
      return this.enabled;
   }

   public List<String> getDisabled() {
      return this.disabled;
   }
}
