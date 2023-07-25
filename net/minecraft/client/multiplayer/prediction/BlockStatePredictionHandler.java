package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockStatePredictionHandler implements AutoCloseable {
   private final Long2ObjectOpenHashMap<BlockStatePredictionHandler.ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap<>();
   private int currentSequenceNr;
   private boolean isPredicting;

   public void retainKnownServerState(BlockPos blockpos, BlockState blockstate, LocalPlayer localplayer) {
      this.serverVerifiedStates.compute(blockpos.asLong(), (olong, blockstatepredictionhandler_serververifiedstate) -> blockstatepredictionhandler_serververifiedstate != null ? blockstatepredictionhandler_serververifiedstate.setSequence(this.currentSequenceNr) : new BlockStatePredictionHandler.ServerVerifiedState(this.currentSequenceNr, blockstate, localplayer.position()));
   }

   public boolean updateKnownServerState(BlockPos blockpos, BlockState blockstate) {
      BlockStatePredictionHandler.ServerVerifiedState blockstatepredictionhandler_serververifiedstate = this.serverVerifiedStates.get(blockpos.asLong());
      if (blockstatepredictionhandler_serververifiedstate == null) {
         return false;
      } else {
         blockstatepredictionhandler_serververifiedstate.setBlockState(blockstate);
         return true;
      }
   }

   public void endPredictionsUpTo(int i, ClientLevel clientlevel) {
      ObjectIterator<Long2ObjectMap.Entry<BlockStatePredictionHandler.ServerVerifiedState>> objectiterator = this.serverVerifiedStates.long2ObjectEntrySet().iterator();

      while(objectiterator.hasNext()) {
         Long2ObjectMap.Entry<BlockStatePredictionHandler.ServerVerifiedState> long2objectmap_entry = objectiterator.next();
         BlockStatePredictionHandler.ServerVerifiedState blockstatepredictionhandler_serververifiedstate = long2objectmap_entry.getValue();
         if (blockstatepredictionhandler_serververifiedstate.sequence <= i) {
            BlockPos blockpos = BlockPos.of(long2objectmap_entry.getLongKey());
            objectiterator.remove();
            clientlevel.syncBlockState(blockpos, blockstatepredictionhandler_serververifiedstate.blockState, blockstatepredictionhandler_serververifiedstate.playerPos);
         }
      }

   }

   public BlockStatePredictionHandler startPredicting() {
      ++this.currentSequenceNr;
      this.isPredicting = true;
      return this;
   }

   public void close() {
      this.isPredicting = false;
   }

   public int currentSequence() {
      return this.currentSequenceNr;
   }

   public boolean isPredicting() {
      return this.isPredicting;
   }

   static class ServerVerifiedState {
      final Vec3 playerPos;
      int sequence;
      BlockState blockState;

      ServerVerifiedState(int i, BlockState blockstate, Vec3 vec3) {
         this.sequence = i;
         this.blockState = blockstate;
         this.playerPos = vec3;
      }

      BlockStatePredictionHandler.ServerVerifiedState setSequence(int i) {
         this.sequence = i;
         return this;
      }

      void setBlockState(BlockState blockstate) {
         this.blockState = blockstate;
      }
   }
}
