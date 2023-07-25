package net.minecraft.client;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;

public class DebugQueryHandler {
   private final ClientPacketListener connection;
   private int transactionId = -1;
   @Nullable
   private Consumer<CompoundTag> callback;

   public DebugQueryHandler(ClientPacketListener clientpacketlistener) {
      this.connection = clientpacketlistener;
   }

   public boolean handleResponse(int i, @Nullable CompoundTag compoundtag) {
      if (this.transactionId == i && this.callback != null) {
         this.callback.accept(compoundtag);
         this.callback = null;
         return true;
      } else {
         return false;
      }
   }

   private int startTransaction(Consumer<CompoundTag> consumer) {
      this.callback = consumer;
      return ++this.transactionId;
   }

   public void queryEntityTag(int i, Consumer<CompoundTag> consumer) {
      int j = this.startTransaction(consumer);
      this.connection.send(new ServerboundEntityTagQuery(j, i));
   }

   public void queryBlockEntityTag(BlockPos blockpos, Consumer<CompoundTag> consumer) {
      int i = this.startTransaction(consumer);
      this.connection.send(new ServerboundBlockEntityTagQuery(i, blockpos));
   }
}
