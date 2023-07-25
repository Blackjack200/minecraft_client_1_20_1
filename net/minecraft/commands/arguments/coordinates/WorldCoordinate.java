package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.Component;

public class WorldCoordinate {
   private static final char PREFIX_RELATIVE = '~';
   public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.double"));
   public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(Component.translatable("argument.pos.missing.int"));
   private final boolean relative;
   private final double value;

   public WorldCoordinate(boolean flag, double d0) {
      this.relative = flag;
      this.value = d0;
   }

   public double get(double d0) {
      return this.relative ? this.value + d0 : this.value;
   }

   public static WorldCoordinate parseDouble(StringReader stringreader, boolean flag) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(stringreader);
      } else if (!stringreader.canRead()) {
         throw ERROR_EXPECTED_DOUBLE.createWithContext(stringreader);
      } else {
         boolean flag1 = isRelative(stringreader);
         int i = stringreader.getCursor();
         double d0 = stringreader.canRead() && stringreader.peek() != ' ' ? stringreader.readDouble() : 0.0D;
         String s = stringreader.getString().substring(i, stringreader.getCursor());
         if (flag1 && s.isEmpty()) {
            return new WorldCoordinate(true, 0.0D);
         } else {
            if (!s.contains(".") && !flag1 && flag) {
               d0 += 0.5D;
            }

            return new WorldCoordinate(flag1, d0);
         }
      }
   }

   public static WorldCoordinate parseInt(StringReader stringreader) throws CommandSyntaxException {
      if (stringreader.canRead() && stringreader.peek() == '^') {
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(stringreader);
      } else if (!stringreader.canRead()) {
         throw ERROR_EXPECTED_INT.createWithContext(stringreader);
      } else {
         boolean flag = isRelative(stringreader);
         double d0;
         if (stringreader.canRead() && stringreader.peek() != ' ') {
            d0 = flag ? stringreader.readDouble() : (double)stringreader.readInt();
         } else {
            d0 = 0.0D;
         }

         return new WorldCoordinate(flag, d0);
      }
   }

   public static boolean isRelative(StringReader stringreader) {
      boolean flag;
      if (stringreader.peek() == '~') {
         flag = true;
         stringreader.skip();
      } else {
         flag = false;
      }

      return flag;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof WorldCoordinate)) {
         return false;
      } else {
         WorldCoordinate worldcoordinate = (WorldCoordinate)object;
         if (this.relative != worldcoordinate.relative) {
            return false;
         } else {
            return Double.compare(worldcoordinate.value, this.value) == 0;
         }
      }
   }

   public int hashCode() {
      int i = this.relative ? 1 : 0;
      long j = Double.doubleToLongBits(this.value);
      return 31 * i + (int)(j ^ j >>> 32);
   }

   public boolean isRelative() {
      return this.relative;
   }
}
