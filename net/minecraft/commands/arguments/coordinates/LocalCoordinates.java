package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class LocalCoordinates implements Coordinates {
   public static final char PREFIX_LOCAL_COORDINATE = '^';
   private final double left;
   private final double up;
   private final double forwards;

   public LocalCoordinates(double d0, double d1, double d2) {
      this.left = d0;
      this.up = d1;
      this.forwards = d2;
   }

   public Vec3 getPosition(CommandSourceStack commandsourcestack) {
      Vec2 vec2 = commandsourcestack.getRotation();
      Vec3 vec3 = commandsourcestack.getAnchor().apply(commandsourcestack);
      float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
      float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
      float f2 = Mth.cos(-vec2.x * ((float)Math.PI / 180F));
      float f3 = Mth.sin(-vec2.x * ((float)Math.PI / 180F));
      float f4 = Mth.cos((-vec2.x + 90.0F) * ((float)Math.PI / 180F));
      float f5 = Mth.sin((-vec2.x + 90.0F) * ((float)Math.PI / 180F));
      Vec3 vec31 = new Vec3((double)(f * f2), (double)f3, (double)(f1 * f2));
      Vec3 vec32 = new Vec3((double)(f * f4), (double)f5, (double)(f1 * f4));
      Vec3 vec33 = vec31.cross(vec32).scale(-1.0D);
      double d0 = vec31.x * this.forwards + vec32.x * this.up + vec33.x * this.left;
      double d1 = vec31.y * this.forwards + vec32.y * this.up + vec33.y * this.left;
      double d2 = vec31.z * this.forwards + vec32.z * this.up + vec33.z * this.left;
      return new Vec3(vec3.x + d0, vec3.y + d1, vec3.z + d2);
   }

   public Vec2 getRotation(CommandSourceStack commandsourcestack) {
      return Vec2.ZERO;
   }

   public boolean isXRelative() {
      return true;
   }

   public boolean isYRelative() {
      return true;
   }

   public boolean isZRelative() {
      return true;
   }

   public static LocalCoordinates parse(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();
      double d0 = readDouble(stringreader, i);
      if (stringreader.canRead() && stringreader.peek() == ' ') {
         stringreader.skip();
         double d1 = readDouble(stringreader, i);
         if (stringreader.canRead() && stringreader.peek() == ' ') {
            stringreader.skip();
            double d2 = readDouble(stringreader, i);
            return new LocalCoordinates(d0, d1, d2);
         } else {
            stringreader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
         }
      } else {
         stringreader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
      }
   }

   private static double readDouble(StringReader stringreader, int i) throws CommandSyntaxException {
      if (!stringreader.canRead()) {
         throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(stringreader);
      } else if (stringreader.peek() != '^') {
         stringreader.setCursor(i);
         throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(stringreader);
      } else {
         stringreader.skip();
         return stringreader.canRead() && stringreader.peek() != ' ' ? stringreader.readDouble() : 0.0D;
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof LocalCoordinates)) {
         return false;
      } else {
         LocalCoordinates localcoordinates = (LocalCoordinates)object;
         return this.left == localcoordinates.left && this.up == localcoordinates.up && this.forwards == localcoordinates.forwards;
      }
   }

   public int hashCode() {
      return Objects.hash(this.left, this.up, this.forwards);
   }
}
