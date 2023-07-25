package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

public class AppendStatic implements RuleBlockEntityModifier {
   public static final Codec<AppendStatic> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(CompoundTag.CODEC.fieldOf("data").forGetter((appendstatic) -> appendstatic.tag)).apply(recordcodecbuilder_instance, AppendStatic::new));
   private final CompoundTag tag;

   public AppendStatic(CompoundTag compoundtag) {
      this.tag = compoundtag;
   }

   public CompoundTag apply(RandomSource randomsource, @Nullable CompoundTag compoundtag) {
      return compoundtag == null ? this.tag.copy() : compoundtag.merge(this.tag);
   }

   public RuleBlockEntityModifierType<?> getType() {
      return RuleBlockEntityModifierType.APPEND_STATIC;
   }
}
