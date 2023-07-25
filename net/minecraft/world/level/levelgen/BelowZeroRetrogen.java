package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;

public final class BelowZeroRetrogen {
   private static final BitSet EMPTY = new BitSet(0);
   private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM.xmap((longstream) -> BitSet.valueOf(longstream.toArray()), (bitset) -> LongStream.of(bitset.toLongArray()));
   private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = BuiltInRegistries.CHUNK_STATUS.byNameCodec().comapFlatMap((chunkstatus) -> chunkstatus == ChunkStatus.EMPTY ? DataResult.error(() -> "target_status cannot be empty") : DataResult.success(chunkstatus), Function.identity());
   public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(NON_EMPTY_CHUNK_STATUS.fieldOf("target_status").forGetter(BelowZeroRetrogen::targetStatus), BITSET_CODEC.optionalFieldOf("missing_bedrock").forGetter((belowzeroretrogen) -> belowzeroretrogen.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(belowzeroretrogen.missingBedrock))).apply(recordcodecbuilder_instance, BelowZeroRetrogen::new));
   private static final Set<ResourceKey<Biome>> RETAINED_RETROGEN_BIOMES = Set.of(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES);
   public static final LevelHeightAccessor UPGRADE_HEIGHT_ACCESSOR = new LevelHeightAccessor() {
      public int getHeight() {
         return 64;
      }

      public int getMinBuildHeight() {
         return -64;
      }
   };
   private final ChunkStatus targetStatus;
   private final BitSet missingBedrock;

   private BelowZeroRetrogen(ChunkStatus chunkstatus, Optional<BitSet> optional) {
      this.targetStatus = chunkstatus;
      this.missingBedrock = optional.orElse(EMPTY);
   }

   @Nullable
   public static BelowZeroRetrogen read(CompoundTag compoundtag) {
      ChunkStatus chunkstatus = ChunkStatus.byName(compoundtag.getString("target_status"));
      return chunkstatus == ChunkStatus.EMPTY ? null : new BelowZeroRetrogen(chunkstatus, Optional.of(BitSet.valueOf(compoundtag.getLongArray("missing_bedrock"))));
   }

   public static void replaceOldBedrock(ProtoChunk protochunk) {
      int i = 4;
      BlockPos.betweenClosed(0, 0, 0, 15, 4, 15).forEach((blockpos) -> {
         if (protochunk.getBlockState(blockpos).is(Blocks.BEDROCK)) {
            protochunk.setBlockState(blockpos, Blocks.DEEPSLATE.defaultBlockState(), false);
         }

      });
   }

   public void applyBedrockMask(ProtoChunk protochunk) {
      LevelHeightAccessor levelheightaccessor = protochunk.getHeightAccessorForGeneration();
      int i = levelheightaccessor.getMinBuildHeight();
      int j = levelheightaccessor.getMaxBuildHeight() - 1;

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            if (this.hasBedrockHole(k, l)) {
               BlockPos.betweenClosed(k, i, l, k, j, l).forEach((blockpos) -> protochunk.setBlockState(blockpos, Blocks.AIR.defaultBlockState(), false));
            }
         }
      }

   }

   public ChunkStatus targetStatus() {
      return this.targetStatus;
   }

   public boolean hasBedrockHoles() {
      return !this.missingBedrock.isEmpty();
   }

   public boolean hasBedrockHole(int i, int j) {
      return this.missingBedrock.get((j & 15) * 16 + (i & 15));
   }

   public static BiomeResolver getBiomeResolver(BiomeResolver biomeresolver, ChunkAccess chunkaccess) {
      if (!chunkaccess.isUpgrading()) {
         return biomeresolver;
      } else {
         Predicate<ResourceKey<Biome>> predicate = RETAINED_RETROGEN_BIOMES::contains;
         return (i, j, k, climate_sampler) -> {
            Holder<Biome> holder = biomeresolver.getNoiseBiome(i, j, k, climate_sampler);
            return holder.is(predicate) ? holder : chunkaccess.getNoiseBiome(i, 0, k);
         };
      }
   }
}
