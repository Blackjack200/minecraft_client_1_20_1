package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public class BlockRotProcessor extends StructureProcessor {
   public static final Codec<BlockRotProcessor> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("rottable_blocks").forGetter((blockrotprocessor1) -> blockrotprocessor1.rottableBlocks), Codec.floatRange(0.0F, 1.0F).fieldOf("integrity").forGetter((blockrotprocessor) -> blockrotprocessor.integrity)).apply(recordcodecbuilder_instance, BlockRotProcessor::new));
   private final Optional<HolderSet<Block>> rottableBlocks;
   private final float integrity;

   public BlockRotProcessor(HolderSet<Block> holderset, float f) {
      this(Optional.of(holderset), f);
   }

   public BlockRotProcessor(float f) {
      this(Optional.empty(), f);
   }

   private BlockRotProcessor(Optional<HolderSet<Block>> optional, float f) {
      this.integrity = f;
      this.rottableBlocks = optional;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      RandomSource randomsource = structureplacesettings.getRandom(structuretemplate_structureblockinfo1.pos());
      return (!this.rottableBlocks.isPresent() || structuretemplate_structureblockinfo.state().is(this.rottableBlocks.get())) && !(randomsource.nextFloat() <= this.integrity) ? null : structuretemplate_structureblockinfo1;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.BLOCK_ROT;
   }
}
