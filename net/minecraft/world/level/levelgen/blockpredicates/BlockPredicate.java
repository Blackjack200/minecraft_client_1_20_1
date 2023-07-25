package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface BlockPredicate extends BiPredicate<WorldGenLevel, BlockPos> {
   Codec<BlockPredicate> CODEC = BuiltInRegistries.BLOCK_PREDICATE_TYPE.byNameCodec().dispatch(BlockPredicate::type, BlockPredicateType::codec);
   BlockPredicate ONLY_IN_AIR_PREDICATE = matchesBlocks(Blocks.AIR);
   BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = matchesBlocks(Blocks.AIR, Blocks.WATER);

   BlockPredicateType<?> type();

   static BlockPredicate allOf(List<BlockPredicate> list) {
      return new AllOfPredicate(list);
   }

   static BlockPredicate allOf(BlockPredicate... ablockpredicate) {
      return allOf(List.of(ablockpredicate));
   }

   static BlockPredicate allOf(BlockPredicate blockpredicate, BlockPredicate blockpredicate1) {
      return allOf(List.of(blockpredicate, blockpredicate1));
   }

   static BlockPredicate anyOf(List<BlockPredicate> list) {
      return new AnyOfPredicate(list);
   }

   static BlockPredicate anyOf(BlockPredicate... ablockpredicate) {
      return anyOf(List.of(ablockpredicate));
   }

   static BlockPredicate anyOf(BlockPredicate blockpredicate, BlockPredicate blockpredicate1) {
      return anyOf(List.of(blockpredicate, blockpredicate1));
   }

   static BlockPredicate matchesBlocks(Vec3i vec3i, List<Block> list) {
      return new MatchingBlocksPredicate(vec3i, HolderSet.direct(Block::builtInRegistryHolder, list));
   }

   static BlockPredicate matchesBlocks(List<Block> list) {
      return matchesBlocks(Vec3i.ZERO, list);
   }

   static BlockPredicate matchesBlocks(Vec3i vec3i, Block... ablock) {
      return matchesBlocks(vec3i, List.of(ablock));
   }

   static BlockPredicate matchesBlocks(Block... ablock) {
      return matchesBlocks(Vec3i.ZERO, ablock);
   }

   static BlockPredicate matchesTag(Vec3i vec3i, TagKey<Block> tagkey) {
      return new MatchingBlockTagPredicate(vec3i, tagkey);
   }

   static BlockPredicate matchesTag(TagKey<Block> tagkey) {
      return matchesTag(Vec3i.ZERO, tagkey);
   }

   static BlockPredicate matchesFluids(Vec3i vec3i, List<Fluid> list) {
      return new MatchingFluidsPredicate(vec3i, HolderSet.direct(Fluid::builtInRegistryHolder, list));
   }

   static BlockPredicate matchesFluids(Vec3i vec3i, Fluid... afluid) {
      return matchesFluids(vec3i, List.of(afluid));
   }

   static BlockPredicate matchesFluids(Fluid... afluid) {
      return matchesFluids(Vec3i.ZERO, afluid);
   }

   static BlockPredicate not(BlockPredicate blockpredicate) {
      return new NotPredicate(blockpredicate);
   }

   static BlockPredicate replaceable(Vec3i vec3i) {
      return new ReplaceablePredicate(vec3i);
   }

   static BlockPredicate replaceable() {
      return replaceable(Vec3i.ZERO);
   }

   static BlockPredicate wouldSurvive(BlockState blockstate, Vec3i vec3i) {
      return new WouldSurvivePredicate(vec3i, blockstate);
   }

   static BlockPredicate hasSturdyFace(Vec3i vec3i, Direction direction) {
      return new HasSturdyFacePredicate(vec3i, direction);
   }

   static BlockPredicate hasSturdyFace(Direction direction) {
      return hasSturdyFace(Vec3i.ZERO, direction);
   }

   static BlockPredicate solid(Vec3i vec3i) {
      return new SolidPredicate(vec3i);
   }

   static BlockPredicate solid() {
      return solid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid() {
      return noFluid(Vec3i.ZERO);
   }

   static BlockPredicate noFluid(Vec3i vec3i) {
      return matchesFluids(vec3i, Fluids.EMPTY);
   }

   static BlockPredicate insideWorld(Vec3i vec3i) {
      return new InsideWorldBoundsPredicate(vec3i);
   }

   static BlockPredicate alwaysTrue() {
      return TrueBlockPredicate.INSTANCE;
   }
}
