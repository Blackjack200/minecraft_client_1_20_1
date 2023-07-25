package net.minecraft.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final long seed;
   private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

   public RandomSequences(long i) {
      this.seed = i;
   }

   public RandomSource get(ResourceLocation resourcelocation) {
      final RandomSource randomsource = this.sequences.computeIfAbsent(resourcelocation, (resourcelocation1) -> new RandomSequence(this.seed, resourcelocation1)).random();
      return new RandomSource() {
         public RandomSource fork() {
            RandomSequences.this.setDirty();
            return randomsource.fork();
         }

         public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return randomsource.forkPositional();
         }

         public void setSeed(long i) {
            RandomSequences.this.setDirty();
            randomsource.setSeed(i);
         }

         public int nextInt() {
            RandomSequences.this.setDirty();
            return randomsource.nextInt();
         }

         public int nextInt(int i) {
            RandomSequences.this.setDirty();
            return randomsource.nextInt(i);
         }

         public long nextLong() {
            RandomSequences.this.setDirty();
            return randomsource.nextLong();
         }

         public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return randomsource.nextBoolean();
         }

         public float nextFloat() {
            RandomSequences.this.setDirty();
            return randomsource.nextFloat();
         }

         public double nextDouble() {
            RandomSequences.this.setDirty();
            return randomsource.nextDouble();
         }

         public double nextGaussian() {
            RandomSequences.this.setDirty();
            return randomsource.nextGaussian();
         }
      };
   }

   public CompoundTag save(CompoundTag compoundtag) {
      this.sequences.forEach((resourcelocation, randomsequence) -> compoundtag.put(resourcelocation.toString(), RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, randomsequence).result().orElseThrow()));
      return compoundtag;
   }

   public static RandomSequences load(long i, CompoundTag compoundtag) {
      RandomSequences randomsequences = new RandomSequences(i);

      for(String s : compoundtag.getAllKeys()) {
         try {
            RandomSequence randomsequence = RandomSequence.CODEC.decode(NbtOps.INSTANCE, compoundtag.get(s)).result().get().getFirst();
            randomsequences.sequences.put(new ResourceLocation(s), randomsequence);
         } catch (Exception var8) {
            LOGGER.error("Failed to load random sequence {}", s, var8);
         }
      }

      return randomsequences;
   }
}
