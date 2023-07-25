package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
   public static final Codec<TagMatchTest> CODEC = TagKey.codec(Registries.BLOCK).fieldOf("tag").xmap(TagMatchTest::new, (tagmatchtest) -> tagmatchtest.tag).codec();
   private final TagKey<Block> tag;

   public TagMatchTest(TagKey<Block> tagkey) {
      this.tag = tagkey;
   }

   public boolean test(BlockState blockstate, RandomSource randomsource) {
      return blockstate.is(this.tag);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.TAG_TEST;
   }
}
