package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class StructureUpdater implements SnbtToNbt.Filter {
   private static final Logger LOGGER = LogUtils.getLogger();

   public CompoundTag apply(String s, CompoundTag compoundtag) {
      return s.startsWith("data/minecraft/structures/") ? update(s, compoundtag) : compoundtag;
   }

   public static CompoundTag update(String s, CompoundTag compoundtag) {
      StructureTemplate structuretemplate = new StructureTemplate();
      int i = NbtUtils.getDataVersion(compoundtag, 500);
      int j = 3437;
      if (i < 3437) {
         LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", i, 3437, s);
      }

      CompoundTag compoundtag1 = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), compoundtag, i);
      structuretemplate.load(BuiltInRegistries.BLOCK.asLookup(), compoundtag1);
      return structuretemplate.save(new CompoundTag());
   }
}
