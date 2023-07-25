package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker extends ChunkTracker {
   public static final int MAX_LEVEL = 33;
   private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
   protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
   private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();

   public TickingTracker() {
      super(34, 16, 256);
      this.chunks.defaultReturnValue((byte)33);
   }

   private SortedArraySet<Ticket<?>> getTickets(long i) {
      return this.tickets.computeIfAbsent(i, (j) -> SortedArraySet.create(4));
   }

   private int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedarrayset) {
      return sortedarrayset.isEmpty() ? 34 : sortedarrayset.first().getTicketLevel();
   }

   public void addTicket(long i, Ticket<?> ticket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(i);
      int j = this.getTicketLevelAt(sortedarrayset);
      sortedarrayset.add(ticket);
      if (ticket.getTicketLevel() < j) {
         this.update(i, ticket.getTicketLevel(), true);
      }

   }

   public void removeTicket(long i, Ticket<?> ticket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(i);
      sortedarrayset.remove(ticket);
      if (sortedarrayset.isEmpty()) {
         this.tickets.remove(i);
      }

      this.update(i, this.getTicketLevelAt(sortedarrayset), false);
   }

   public <T> void addTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      this.addTicket(chunkpos.toLong(), new Ticket<>(tickettype, i, object));
   }

   public <T> void removeTicket(TicketType<T> tickettype, ChunkPos chunkpos, int i, T object) {
      Ticket<T> ticket = new Ticket<>(tickettype, i, object);
      this.removeTicket(chunkpos.toLong(), ticket);
   }

   public void replacePlayerTicketsLevel(int i) {
      List<Pair<Ticket<ChunkPos>, Long>> list = new ArrayList<>();

      for(Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> long2objectmap_entry : this.tickets.long2ObjectEntrySet()) {
         for(Ticket<?> ticket : long2objectmap_entry.getValue()) {
            if (ticket.getType() == TicketType.PLAYER) {
               list.add(Pair.of(ticket, long2objectmap_entry.getLongKey()));
            }
         }
      }

      for(Pair<Ticket<ChunkPos>, Long> pair : list) {
         Long olong = pair.getSecond();
         Ticket<ChunkPos> ticket1 = pair.getFirst();
         this.removeTicket(olong, ticket1);
         ChunkPos chunkpos = new ChunkPos(olong);
         TicketType<ChunkPos> tickettype = ticket1.getType();
         this.addTicket(tickettype, chunkpos, i, chunkpos);
      }

   }

   protected int getLevelFromSource(long i) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(i);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().getTicketLevel() : Integer.MAX_VALUE;
   }

   public int getLevel(ChunkPos chunkpos) {
      return this.getLevel(chunkpos.toLong());
   }

   protected int getLevel(long i) {
      return this.chunks.get(i);
   }

   protected void setLevel(long i, int j) {
      if (j > 33) {
         this.chunks.remove(i);
      } else {
         this.chunks.put(i, (byte)j);
      }

   }

   public void runAllUpdates() {
      this.runUpdates(Integer.MAX_VALUE);
   }

   public String getTicketDebugString(long i) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(i);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().toString() : "no_ticket";
   }
}
