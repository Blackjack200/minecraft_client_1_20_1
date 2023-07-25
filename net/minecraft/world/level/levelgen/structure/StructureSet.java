package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public record StructureSet(List<StructureSet.StructureSelectionEntry> structures, StructurePlacement placement) {
   public static final Codec<StructureSet> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(StructureSet.StructureSelectionEntry.CODEC.listOf().fieldOf("structures").forGetter(StructureSet::structures), StructurePlacement.CODEC.fieldOf("placement").forGetter(StructureSet::placement)).apply(recordcodecbuilder_instance, StructureSet::new));
   public static final Codec<Holder<StructureSet>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE_SET, DIRECT_CODEC);

   public StructureSet(Holder<Structure> holder, StructurePlacement structureplacement) {
      this(List.of(new StructureSet.StructureSelectionEntry(holder, 1)), structureplacement);
   }

   public static StructureSet.StructureSelectionEntry entry(Holder<Structure> holder, int i) {
      return new StructureSet.StructureSelectionEntry(holder, i);
   }

   public static StructureSet.StructureSelectionEntry entry(Holder<Structure> holder) {
      return new StructureSet.StructureSelectionEntry(holder, 1);
   }

   public static record StructureSelectionEntry(Holder<Structure> structure, int weight) {
      public static final Codec<StructureSet.StructureSelectionEntry> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Structure.CODEC.fieldOf("structure").forGetter(StructureSet.StructureSelectionEntry::structure), ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(StructureSet.StructureSelectionEntry::weight)).apply(recordcodecbuilder_instance, StructureSet.StructureSelectionEntry::new));
   }
}
