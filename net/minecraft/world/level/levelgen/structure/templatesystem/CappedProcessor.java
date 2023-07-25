package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;

public class CappedProcessor extends StructureProcessor {
   public static final Codec<CappedProcessor> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter((cappedprocessor1) -> cappedprocessor1.delegate), IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter((cappedprocessor) -> cappedprocessor.limit)).apply(recordcodecbuilder_instance, CappedProcessor::new));
   private final StructureProcessor delegate;
   private final IntProvider limit;

   public CappedProcessor(StructureProcessor structureprocessor, IntProvider intprovider) {
      this.delegate = structureprocessor;
      this.limit = intprovider;
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.CAPPED;
   }

   public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverlevelaccessor, BlockPos blockpos, BlockPos blockpos1, List<StructureTemplate.StructureBlockInfo> list, List<StructureTemplate.StructureBlockInfo> list1, StructurePlaceSettings structureplacesettings) {
      if (this.limit.getMaxValue() != 0 && !list1.isEmpty()) {
         if (list.size() != list1.size()) {
            Util.logAndPauseIfInIde("Original block info list not in sync with processed list, skipping processing. Original size: " + list.size() + ", Processed size: " + list1.size());
            return list1;
         } else {
            RandomSource randomsource = RandomSource.create(serverlevelaccessor.getLevel().getSeed()).forkPositional().at(blockpos);
            int i = Math.min(this.limit.sample(randomsource), list1.size());
            if (i < 1) {
               return list1;
            } else {
               IntArrayList intarraylist = Util.toShuffledList(IntStream.range(0, list1.size()), randomsource);
               IntIterator intiterator = intarraylist.intIterator();
               int j = 0;

               while(intiterator.hasNext() && j < i) {
                  int k = intiterator.nextInt();
                  StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo = list.get(k);
                  StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1 = list1.get(k);
                  StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo2 = this.delegate.processBlock(serverlevelaccessor, blockpos, blockpos1, structuretemplate_structureblockinfo, structuretemplate_structureblockinfo1, structureplacesettings);
                  if (structuretemplate_structureblockinfo2 != null && !structuretemplate_structureblockinfo1.equals(structuretemplate_structureblockinfo2)) {
                     ++j;
                     list1.set(k, structuretemplate_structureblockinfo2);
                  }
               }

               return list1;
            }
         }
      } else {
         return list1;
      }
   }
}
