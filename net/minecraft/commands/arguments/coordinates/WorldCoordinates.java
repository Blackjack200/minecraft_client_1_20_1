package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WorldCoordinates implements Coordinates {
   private final WorldCoordinate x;
   private final WorldCoordinate y;
   private final WorldCoordinate z;

   public WorldCoordinates(WorldCoordinate worldcoordinate, WorldCoordinate worldcoordinate1, WorldCoordinate worldcoordinate2) {
      this.x = worldcoordinate;
      this.y = worldcoordinate1;
      this.z = worldcoordinate2;
   }

   public Vec3 getPosition(CommandSourceStack commandsourcestack) {
      Vec3 vec3 = commandsourcestack.getPosition();
      return new Vec3(this.x.get(vec3.x), this.y.get(vec3.y), this.z.get(vec3.z));
   }

   public Vec2 getRotation(CommandSourceStack commandsourcestack) {
      Vec2 vec2 = commandsourcestack.getRotation();
      return new Vec2((float)this.x.get((double)vec2.x), (float)this.y.get((double)vec2.y));
   }

   public boolean isXRelative() {
      return this.x.isRelative();
   }

   public boolean isYRelative() {
      return this.y.isRelative();
   }

   public boolean isZRelative() {
      return this.z.isRelative();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof WorldCoordinates)) {
         return false;
      } else {
         WorldCoordinates worldcoordinates = (WorldCoordinates)object;
         if (!this.x.equals(worldcoordinates.x)) {
            return false;
         } else {
            return !this.y.equals(worldcoordinates.y) ? false : this.z.equals(worldcoordinates.z);
         }
      }
   }

   public static WorldCoordinates parseInt(StringReader stringreader) throws CommandSyntaxException {
      int i = stringreader.getCursor();
      WorldCoordinate worldcoordinate = WorldCoordinate.parseInt(stringreader);
      if (stringreader.canRead() && stringreader.peek() == ' ') {
         stringreader.skip();
         WorldCoordinate worldcoordinate1 = WorldCoordinate.parseInt(stringreader);
         if (stringreader.canRead() && stringreader.peek() == ' ') {
            stringreader.skip();
            WorldCoordinate worldcoordinate2 = WorldCoordinate.parseInt(stringreader);
            return new WorldCoordinates(worldcoordinate, worldcoordinate1, worldcoordinate2);
         } else {
            stringreader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
         }
      } else {
         stringreader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
      }
   }

   public static WorldCoordinates parseDouble(StringReader stringreader, boolean flag) throws CommandSyntaxException {
      int i = stringreader.getCursor();
      WorldCoordinate worldcoordinate = WorldCoordinate.parseDouble(stringreader, flag);
      if (stringreader.canRead() && stringreader.peek() == ' ') {
         stringreader.skip();
         WorldCoordinate worldcoordinate1 = WorldCoordinate.parseDouble(stringreader, false);
         if (stringreader.canRead() && stringreader.peek() == ' ') {
            stringreader.skip();
            WorldCoordinate worldcoordinate2 = WorldCoordinate.parseDouble(stringreader, flag);
            return new WorldCoordinates(worldcoordinate, worldcoordinate1, worldcoordinate2);
         } else {
            stringreader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
         }
      } else {
         stringreader.setCursor(i);
         throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(stringreader);
      }
   }

   public static WorldCoordinates absolute(double d0, double d1, double d2) {
      return new WorldCoordinates(new WorldCoordinate(false, d0), new WorldCoordinate(false, d1), new WorldCoordinate(false, d2));
   }

   public static WorldCoordinates absolute(Vec2 vec2) {
      return new WorldCoordinates(new WorldCoordinate(false, (double)vec2.x), new WorldCoordinate(false, (double)vec2.y), new WorldCoordinate(true, 0.0D));
   }

   public static WorldCoordinates current() {
      return new WorldCoordinates(new WorldCoordinate(true, 0.0D), new WorldCoordinate(true, 0.0D), new WorldCoordinate(true, 0.0D));
   }

   public int hashCode() {
      int i = this.x.hashCode();
      i = 31 * i + this.y.hashCode();
      return 31 * i + this.z.hashCode();
   }
}
