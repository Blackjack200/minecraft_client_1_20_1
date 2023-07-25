package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Optional;
import javax.annotation.Nullable;

public class LastSeenMessagesValidator {
   private final int lastSeenCount;
   private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList<>();
   @Nullable
   private MessageSignature lastPendingMessage;

   public LastSeenMessagesValidator(int i) {
      this.lastSeenCount = i;

      for(int j = 0; j < i; ++j) {
         this.trackedMessages.add((Object)null);
      }

   }

   public void addPending(MessageSignature messagesignature) {
      if (!messagesignature.equals(this.lastPendingMessage)) {
         this.trackedMessages.add(new LastSeenTrackedEntry(messagesignature, true));
         this.lastPendingMessage = messagesignature;
      }

   }

   public int trackedMessagesCount() {
      return this.trackedMessages.size();
   }

   public boolean applyOffset(int i) {
      int j = this.trackedMessages.size() - this.lastSeenCount;
      if (i >= 0 && i <= j) {
         this.trackedMessages.removeElements(0, i);
         return true;
      } else {
         return false;
      }
   }

   public Optional<LastSeenMessages> applyUpdate(LastSeenMessages.Update lastseenmessages_update) {
      if (!this.applyOffset(lastseenmessages_update.offset())) {
         return Optional.empty();
      } else {
         ObjectList<MessageSignature> objectlist = new ObjectArrayList<>(lastseenmessages_update.acknowledged().cardinality());
         if (lastseenmessages_update.acknowledged().length() > this.lastSeenCount) {
            return Optional.empty();
         } else {
            for(int i = 0; i < this.lastSeenCount; ++i) {
               boolean flag = lastseenmessages_update.acknowledged().get(i);
               LastSeenTrackedEntry lastseentrackedentry = this.trackedMessages.get(i);
               if (flag) {
                  if (lastseentrackedentry == null) {
                     return Optional.empty();
                  }

                  this.trackedMessages.set(i, lastseentrackedentry.acknowledge());
                  objectlist.add(lastseentrackedentry.signature());
               } else {
                  if (lastseentrackedentry != null && !lastseentrackedentry.pending()) {
                     return Optional.empty();
                  }

                  this.trackedMessages.set(i, (Object)null);
               }
            }

            return Optional.of(new LastSeenMessages(objectlist));
         }
      }
   }
}
