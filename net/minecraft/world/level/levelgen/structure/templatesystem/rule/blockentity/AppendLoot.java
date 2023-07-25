package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class AppendLoot implements RuleBlockEntityModifier {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<AppendLoot> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter((appendloot) -> appendloot.lootTable)).apply(recordcodecbuilder_instance, AppendLoot::new));
   private final ResourceLocation lootTable;

   public AppendLoot(ResourceLocation resourcelocation) {
      this.lootTable = resourcelocation;
   }

   public CompoundTag apply(RandomSource randomsource, @Nullable CompoundTag compoundtag) {
      CompoundTag compoundtag1 = compoundtag == null ? new CompoundTag() : compoundtag.copy();
      ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.lootTable).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag1.put("LootTable", tag));
      compoundtag1.putLong("LootTableSeed", randomsource.nextLong());
      return compoundtag1;
   }

   public RuleBlockEntityModifierType<?> getType() {
      return RuleBlockEntityModifierType.APPEND_LOOT;
   }
}
