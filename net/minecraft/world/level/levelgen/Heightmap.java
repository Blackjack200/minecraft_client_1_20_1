package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;

public class Heightmap {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Predicate<BlockState> NOT_AIR = (blockstate) -> !blockstate.isAir();
   static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = BlockBehaviour.BlockStateBase::blocksMotion;
   private final BitStorage data;
   private final Predicate<BlockState> isOpaque;
   private final ChunkAccess chunk;

   public Heightmap(ChunkAccess chunkaccess, Heightmap.Types heightmap_types) {
      this.isOpaque = heightmap_types.isOpaque();
      this.chunk = chunkaccess;
      int i = Mth.ceillog2(chunkaccess.getHeight() + 1);
      this.data = new SimpleBitStorage(i, 256);
   }

   public static void primeHeightmaps(ChunkAccess chunkaccess, Set<Heightmap.Types> set) {
      int i = set.size();
      ObjectList<Heightmap> objectlist = new ObjectArrayList<>(i);
      ObjectListIterator<Heightmap> objectlistiterator = objectlist.iterator();
      int j = chunkaccess.getHighestSectionPosition() + 16;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            for(Heightmap.Types heightmap_types : set) {
               objectlist.add(chunkaccess.getOrCreateHeightmapUnprimed(heightmap_types));
            }

            for(int i1 = j - 1; i1 >= chunkaccess.getMinBuildHeight(); --i1) {
               blockpos_mutableblockpos.set(k, i1, l);
               BlockState blockstate = chunkaccess.getBlockState(blockpos_mutableblockpos);
               if (!blockstate.is(Blocks.AIR)) {
                  while(objectlistiterator.hasNext()) {
                     Heightmap heightmap = objectlistiterator.next();
                     if (heightmap.isOpaque.test(blockstate)) {
                        heightmap.setHeight(k, l, i1 + 1);
                        objectlistiterator.remove();
                     }
                  }

                  if (objectlist.isEmpty()) {
                     break;
                  }

                  objectlistiterator.back(i);
               }
            }
         }
      }

   }

   public boolean update(int i, int j, int k, BlockState blockstate) {
      int l = this.getFirstAvailable(i, k);
      if (j <= l - 2) {
         return false;
      } else {
         if (this.isOpaque.test(blockstate)) {
            if (j >= l) {
               this.setHeight(i, k, j + 1);
               return true;
            }
         } else if (l - 1 == j) {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

            for(int i1 = j - 1; i1 >= this.chunk.getMinBuildHeight(); --i1) {
               blockpos_mutableblockpos.set(i, i1, k);
               if (this.isOpaque.test(this.chunk.getBlockState(blockpos_mutableblockpos))) {
                  this.setHeight(i, k, i1 + 1);
                  return true;
               }
            }

            this.setHeight(i, k, this.chunk.getMinBuildHeight());
            return true;
         }

         return false;
      }
   }

   public int getFirstAvailable(int i, int j) {
      return this.getFirstAvailable(getIndex(i, j));
   }

   public int getHighestTaken(int i, int j) {
      return this.getFirstAvailable(getIndex(i, j)) - 1;
   }

   private int getFirstAvailable(int i) {
      return this.data.get(i) + this.chunk.getMinBuildHeight();
   }

   private void setHeight(int i, int j, int k) {
      this.data.set(getIndex(i, j), k - this.chunk.getMinBuildHeight());
   }

   public void setRawData(ChunkAccess chunkaccess, Heightmap.Types heightmap_types, long[] along) {
      long[] along1 = this.data.getRaw();
      if (along1.length == along.length) {
         System.arraycopy(along, 0, along1, 0, along.length);
      } else {
         LOGGER.warn("Ignoring heightmap data for chunk " + chunkaccess.getPos() + ", size does not match; expected: " + along1.length + ", got: " + along.length);
         primeHeightmaps(chunkaccess, EnumSet.of(heightmap_types));
      }
   }

   public long[] getRawData() {
      return this.data.getRaw();
   }

   private static int getIndex(int i, int j) {
      return i + j * 16;
   }

   public static enum Types implements StringRepresentable {
      WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
      WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
      OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
      OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
      MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, (blockstate) -> blockstate.blocksMotion() || !blockstate.getFluidState().isEmpty()),
      MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Heightmap.Usage.LIVE_WORLD, (blockstate) -> (blockstate.blocksMotion() || !blockstate.getFluidState().isEmpty()) && !(blockstate.getBlock() instanceof LeavesBlock));

      public static final Codec<Heightmap.Types> CODEC = StringRepresentable.fromEnum(Heightmap.Types::values);
      private final String serializationKey;
      private final Heightmap.Usage usage;
      private final Predicate<BlockState> isOpaque;

      private Types(String s, Heightmap.Usage heightmap_usage, Predicate<BlockState> predicate) {
         this.serializationKey = s;
         this.usage = heightmap_usage;
         this.isOpaque = predicate;
      }

      public String getSerializationKey() {
         return this.serializationKey;
      }

      public boolean sendToClient() {
         return this.usage == Heightmap.Usage.CLIENT;
      }

      public boolean keepAfterWorldgen() {
         return this.usage != Heightmap.Usage.WORLDGEN;
      }

      public Predicate<BlockState> isOpaque() {
         return this.isOpaque;
      }

      public String getSerializedName() {
         return this.serializationKey;
      }
   }

   public static enum Usage {
      WORLDGEN,
      LIVE_WORLD,
      CLIENT;
   }
}
