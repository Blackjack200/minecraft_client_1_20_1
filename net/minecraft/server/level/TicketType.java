package net.minecraft.server.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;

public class TicketType<T> {
   private final String name;
   private final Comparator<T> comparator;
   private final long timeout;
   public static final TicketType<Unit> START = create("start", (unit, unit1) -> 0);
   public static final TicketType<Unit> DRAGON = create("dragon", (unit, unit1) -> 0);
   public static final TicketType<ChunkPos> PLAYER = create("player", Comparator.comparingLong(ChunkPos::toLong));
   public static final TicketType<ChunkPos> FORCED = create("forced", Comparator.comparingLong(ChunkPos::toLong));
   public static final TicketType<ChunkPos> LIGHT = create("light", Comparator.comparingLong(ChunkPos::toLong));
   public static final TicketType<BlockPos> PORTAL = create("portal", Vec3i::compareTo, 300);
   public static final TicketType<Integer> POST_TELEPORT = create("post_teleport", Integer::compareTo, 5);
   public static final TicketType<ChunkPos> UNKNOWN = create("unknown", Comparator.comparingLong(ChunkPos::toLong), 1);

   public static <T> TicketType<T> create(String s, Comparator<T> comparator) {
      return new TicketType<>(s, comparator, 0L);
   }

   public static <T> TicketType<T> create(String s, Comparator<T> comparator, int i) {
      return new TicketType<>(s, comparator, (long)i);
   }

   protected TicketType(String s, Comparator<T> comparator, long i) {
      this.name = s;
      this.comparator = comparator;
      this.timeout = i;
   }

   public String toString() {
      return this.name;
   }

   public Comparator<T> getComparator() {
      return this.comparator;
   }

   public long timeout() {
      return this.timeout;
   }
}
