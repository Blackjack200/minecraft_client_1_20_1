package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
   public static final int SECTION_WIDTH = 16;
   public static final int SECTION_HEIGHT = 16;
   public static final int SECTION_SIZE = 4096;
   public static final int BIOME_CONTAINER_BITS = 2;
   private short nonEmptyBlockCount;
   private short tickingBlockCount;
   private short tickingFluidCount;
   private final PalettedContainer<BlockState> states;
   private PalettedContainerRO<Holder<Biome>> biomes;

   public LevelChunkSection(PalettedContainer<BlockState> palettedcontainer, PalettedContainerRO<Holder<Biome>> palettedcontainerro) {
      this.states = palettedcontainer;
      this.biomes = palettedcontainerro;
      this.recalcBlockCounts();
   }

   public LevelChunkSection(Registry<Biome> registry) {
      this.states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
      this.biomes = new PalettedContainer<>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
   }

   public BlockState getBlockState(int i, int j, int k) {
      return this.states.get(i, j, k);
   }

   public FluidState getFluidState(int i, int j, int k) {
      return this.states.get(i, j, k).getFluidState();
   }

   public void acquire() {
      this.states.acquire();
   }

   public void release() {
      this.states.release();
   }

   public BlockState setBlockState(int i, int j, int k, BlockState blockstate) {
      return this.setBlockState(i, j, k, blockstate, true);
   }

   public BlockState setBlockState(int i, int j, int k, BlockState blockstate, boolean flag) {
      BlockState blockstate1;
      if (flag) {
         blockstate1 = this.states.getAndSet(i, j, k, blockstate);
      } else {
         blockstate1 = this.states.getAndSetUnchecked(i, j, k, blockstate);
      }

      FluidState fluidstate = blockstate1.getFluidState();
      FluidState fluidstate1 = blockstate.getFluidState();
      if (!blockstate1.isAir()) {
         --this.nonEmptyBlockCount;
         if (blockstate1.isRandomlyTicking()) {
            --this.tickingBlockCount;
         }
      }

      if (!fluidstate.isEmpty()) {
         --this.tickingFluidCount;
      }

      if (!blockstate.isAir()) {
         ++this.nonEmptyBlockCount;
         if (blockstate.isRandomlyTicking()) {
            ++this.tickingBlockCount;
         }
      }

      if (!fluidstate1.isEmpty()) {
         ++this.tickingFluidCount;
      }

      return blockstate1;
   }

   public boolean hasOnlyAir() {
      return this.nonEmptyBlockCount == 0;
   }

   public boolean isRandomlyTicking() {
      return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
   }

   public boolean isRandomlyTickingBlocks() {
      return this.tickingBlockCount > 0;
   }

   public boolean isRandomlyTickingFluids() {
      return this.tickingFluidCount > 0;
   }

   public void recalcBlockCounts() {
      class BlockCounter implements PalettedContainer.CountConsumer<BlockState> {
         public int nonEmptyBlockCount;
         public int tickingBlockCount;
         public int tickingFluidCount;

         public void accept(BlockState blockstate, int i) {
            FluidState fluidstate = blockstate.getFluidState();
            if (!blockstate.isAir()) {
               this.nonEmptyBlockCount += i;
               if (blockstate.isRandomlyTicking()) {
                  this.tickingBlockCount += i;
               }
            }

            if (!fluidstate.isEmpty()) {
               this.nonEmptyBlockCount += i;
               if (fluidstate.isRandomlyTicking()) {
                  this.tickingFluidCount += i;
               }
            }

         }
      }

      BlockCounter levelchunksection_1blockcounter = new BlockCounter();
      this.states.count(levelchunksection_1blockcounter);
      this.nonEmptyBlockCount = (short)levelchunksection_1blockcounter.nonEmptyBlockCount;
      this.tickingBlockCount = (short)levelchunksection_1blockcounter.tickingBlockCount;
      this.tickingFluidCount = (short)levelchunksection_1blockcounter.tickingFluidCount;
   }

   public PalettedContainer<BlockState> getStates() {
      return this.states;
   }

   public PalettedContainerRO<Holder<Biome>> getBiomes() {
      return this.biomes;
   }

   public void read(FriendlyByteBuf friendlybytebuf) {
      this.nonEmptyBlockCount = friendlybytebuf.readShort();
      this.states.read(friendlybytebuf);
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      palettedcontainer.read(friendlybytebuf);
      this.biomes = palettedcontainer;
   }

   public void readBiomes(FriendlyByteBuf friendlybytebuf) {
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      palettedcontainer.read(friendlybytebuf);
      this.biomes = palettedcontainer;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeShort(this.nonEmptyBlockCount);
      this.states.write(friendlybytebuf);
      this.biomes.write(friendlybytebuf);
   }

   public int getSerializedSize() {
      return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
   }

   public boolean maybeHas(Predicate<BlockState> predicate) {
      return this.states.maybeHas(predicate);
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      return this.biomes.get(i, j, k);
   }

   public void fillBiomesFromNoise(BiomeResolver biomeresolver, Climate.Sampler climate_sampler, int i, int j, int k) {
      PalettedContainer<Holder<Biome>> palettedcontainer = this.biomes.recreate();
      int l = 4;

      for(int i1 = 0; i1 < 4; ++i1) {
         for(int j1 = 0; j1 < 4; ++j1) {
            for(int k1 = 0; k1 < 4; ++k1) {
               palettedcontainer.getAndSetUnchecked(i1, j1, k1, biomeresolver.getNoiseBiome(i + i1, j + j1, k + k1, climate_sampler));
            }
         }
      }

      this.biomes = palettedcontainer;
   }
}
