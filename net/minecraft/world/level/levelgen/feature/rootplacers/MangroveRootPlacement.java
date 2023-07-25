package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(HolderSet<Block> canGrowThrough, HolderSet<Block> muddyRootsIn, BlockStateProvider muddyRootsProvider, int maxRootWidth, int maxRootLength, float randomSkewChance) {
   public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter((mangroverootplacement5) -> mangroverootplacement5.canGrowThrough), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("muddy_roots_in").forGetter((mangroverootplacement4) -> mangroverootplacement4.muddyRootsIn), BlockStateProvider.CODEC.fieldOf("muddy_roots_provider").forGetter((mangroverootplacement3) -> mangroverootplacement3.muddyRootsProvider), Codec.intRange(1, 12).fieldOf("max_root_width").forGetter((mangroverootplacement2) -> mangroverootplacement2.maxRootWidth), Codec.intRange(1, 64).fieldOf("max_root_length").forGetter((mangroverootplacement1) -> mangroverootplacement1.maxRootLength), Codec.floatRange(0.0F, 1.0F).fieldOf("random_skew_chance").forGetter((mangroverootplacement) -> mangroverootplacement.randomSkewChance)).apply(recordcodecbuilder_instance, MangroveRootPlacement::new));
}
