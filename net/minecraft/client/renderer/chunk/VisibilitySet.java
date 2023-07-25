package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.Set;
import net.minecraft.core.Direction;

public class VisibilitySet {
   private static final int FACINGS = Direction.values().length;
   private final BitSet data = new BitSet(FACINGS * FACINGS);

   public void add(Set<Direction> set) {
      for(Direction direction : set) {
         for(Direction direction1 : set) {
            this.set(direction, direction1, true);
         }
      }

   }

   public void set(Direction direction, Direction direction1, boolean flag) {
      this.data.set(direction.ordinal() + direction1.ordinal() * FACINGS, flag);
      this.data.set(direction1.ordinal() + direction.ordinal() * FACINGS, flag);
   }

   public void setAll(boolean flag) {
      this.data.set(0, this.data.size(), flag);
   }

   public boolean visibilityBetween(Direction direction, Direction direction1) {
      return this.data.get(direction.ordinal() + direction1.ordinal() * FACINGS);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(' ');

      for(Direction direction : Direction.values()) {
         stringbuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
      }

      stringbuilder.append('\n');

      for(Direction direction1 : Direction.values()) {
         stringbuilder.append(direction1.toString().toUpperCase().charAt(0));

         for(Direction direction2 : Direction.values()) {
            if (direction1 == direction2) {
               stringbuilder.append("  ");
            } else {
               boolean flag = this.visibilityBetween(direction1, direction2);
               stringbuilder.append(' ').append((char)(flag ? 'Y' : 'n'));
            }
         }

         stringbuilder.append('\n');
      }

      return stringbuilder.toString();
   }
}
