package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class JigsawReplacementProcessor extends StructureProcessor {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<JigsawReplacementProcessor> CODEC = Codec.unit(() -> JigsawReplacementProcessor.INSTANCE);
   public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

   private JigsawReplacementProcessor() {
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      BlockState blockstate = structuretemplate_structureblockinfo1.state();
      if (blockstate.is(Blocks.JIGSAW)) {
         if (structuretemplate_structureblockinfo1.nbt() == null) {
            LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", (Object)blockpos);
            return structuretemplate_structureblockinfo1;
         } else {
            String s = structuretemplate_structureblockinfo1.nbt().getString("final_state");

            BlockState blockstate1;
            try {
               BlockStateParser.BlockResult blockstateparser_blockresult = BlockStateParser.parseForBlock(levelreader.holderLookup(Registries.BLOCK), s, true);
               blockstate1 = blockstateparser_blockresult.blockState();
            } catch (CommandSyntaxException var11) {
               throw new RuntimeException(var11);
            }

            return blockstate1.is(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(structuretemplate_structureblockinfo1.pos(), blockstate1, (CompoundTag)null);
         }
      } else {
         return structuretemplate_structureblockinfo1;
      }
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.JIGSAW_REPLACEMENT;
   }
}
